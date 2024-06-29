package com.smanzana.nostrumfairies.client.gui;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.ItemCacheType;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.network.messages.StorageMonitorRequestMessage;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class StorageMonitorScreen extends Screen {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/screen/storage_viewer.png");
	private static final int GUI_TEXT_WIDTH = 183;
	private static final int GUI_TEXT_HEIGHT = 162;
	private static final int GUI_TEXT_SLIDER_HOFFSET = 183;
	private static final int GUI_TEXT_CELL_HOFFSET = 191;
	private static final int GUI_TOP_INV_HOFFSET = 7;
	private static final int GUI_TOP_INV_VOFFSET = 17;
	private static final int GUI_TOP_INV_WIDTH = 162;
	private static final int GUI_TOP_INV_HEIGHT = 126;
	private static final int GUI_INV_CELL_LENGTH = 18;
	private static final int GUI_INV_SLIDER_WIDTH = 8;
	private static final int GUI_INV_SLIDER_HEIGHT = 14;
	private static final int GUI_INV_SLIDER_HOFFSET = 170;
	private static final int GUI_INV_SLIDER_VOFFSET = 17;
	private static final int GUI_INV_SLIDER_TOTAL_HEIGHT = 112;
	
	private static final int GUI_TEXT_MENUITEM_HOFFSET = 183;
	private static final int GUI_TEXT_MENUITEM_VOFFSET = 28;
	private static final int GUI_TEXT_MENUITEM_WIDTH = 65;
	private static final int GUI_TEXT_MENUITEM_HEIGHT = 26;
	private static final int GUI_TEXT_MENUITEM_SLOT_HOFFSET = 7;
	private static final int GUI_TEXT_MENUITEM_SLOT_VOFFSET = 5;
	
	private static final int GUI_CELL_ROWS = GUI_TOP_INV_HEIGHT / GUI_INV_CELL_LENGTH;
	private static final int GUI_CELL_COLS = GUI_TOP_INV_WIDTH / GUI_INV_CELL_LENGTH;

	private StorageMonitorTileEntity monitor;
	private double scroll;
	private double scrollLag;
	private int mouseClickOffsetY;
	private boolean scrollClicked;
	
	public StorageMonitorScreen(StorageMonitorTileEntity monitor) {
		super(new StringTextComponent("Storage Monitor"));
		this.monitor = monitor;
	}
	
	@Override	
	public void tick() {
		if (monitor.getNetwork() != null) {
			// TODO toggle excluding 'buffer' chest items
			final Collection<ItemDeepStack> items = monitor.getNetwork().getAllCondensedNetworkItems(ItemCacheType.NET);
			final int len = items.size();
			final int rows = (int) Math.ceil((double)len / (double) GUI_CELL_COLS);
			final int spilloverRows = rows - GUI_CELL_ROWS; // scroll bar represents this many rows of pixels
			if (spilloverRows > 0) {
				final double speedMult = 0.075; // cells/tick
				final double speedAdj = speedMult  / spilloverRows;
				
				final double diff = scroll - scrollLag;
				if (diff != 0) {
					if (Math.abs(diff) < speedAdj) {
						scrollLag = scroll;
					} else {
						scrollLag += Math.signum(diff) * speedAdj;
					}
				}
			}
		}
		
		if (monitor.getWorld().getGameTime() % 20 == 0) {
			LogisticsNetwork network = monitor.getNetwork();
			if (network != null) {
				NetworkHandler.sendToServer(new LogisticsUpdateRequest(network.getUUID()));
			} else {
				NetworkHandler.sendToServer(new LogisticsUpdateRequest(null));
			}
		}
	}
	
	private void drawMenuItem(MatrixStack matrixStackIn, int x, int y, ItemStack request, boolean mouseOver) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		//RenderSystem.disableLighting();

		final float value = (mouseOver ? .8f : 1f);
		RenderFuncs.blit(matrixStackIn, x, y, GUI_TEXT_MENUITEM_HOFFSET, GUI_TEXT_MENUITEM_VOFFSET, GUI_TEXT_MENUITEM_WIDTH, GUI_TEXT_MENUITEM_HEIGHT, value, value, value, 1f);
		if (!request.isEmpty()) {
			Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(request, x + GUI_TEXT_MENUITEM_SLOT_HOFFSET, y + GUI_TEXT_MENUITEM_SLOT_VOFFSET);
			Minecraft.getInstance().getItemRenderer().renderItemOverlayIntoGUI(this.font, request, x + GUI_TEXT_MENUITEM_SLOT_HOFFSET, y + GUI_TEXT_MENUITEM_SLOT_VOFFSET, request.getCount() + "");
		}
		
		if (mouseOver) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 1000);
			RenderFuncs.drawRect(matrixStackIn, x + GUI_TEXT_MENUITEM_SLOT_HOFFSET, y + GUI_TEXT_MENUITEM_SLOT_VOFFSET,
					x + GUI_TEXT_MENUITEM_SLOT_HOFFSET + (GUI_INV_CELL_LENGTH - 1), y + GUI_TEXT_MENUITEM_SLOT_VOFFSET + (GUI_INV_CELL_LENGTH - 1),
					0x60FFFFFF);
			matrixStackIn.pop();
			RenderSystem.disableBlend();
		}
		
	}
	
	private void drawCell(MatrixStack matrixStackIn, int x, int y, @Nullable ItemDeepStack stack, boolean mouseOver) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		//RenderSystem.disableLighting();
		blit(matrixStackIn, x, y, GUI_TEXT_CELL_HOFFSET, 0, GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH);
		if (stack != null) {
			final long countNum = stack.getCount();
			String count;
			if (countNum < 10000) {
				count = "" + countNum;
			} else if (countNum < 1000000) {
				count = (countNum / 1000) + "k";
			} else {
				count = (countNum / 1000000) + "m";
			}
			final int width = this.font.getStringWidth(count);
			final int height = this.font.FONT_HEIGHT;
			Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(stack.getTemplate(), x + 1, y + 1);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 200);
			RenderFuncs.drawRect(matrixStackIn, x + 1, y + (GUI_INV_CELL_LENGTH - 6) , x + (GUI_INV_CELL_LENGTH - 1), y + (GUI_INV_CELL_LENGTH - 1), 0x60000000);
			matrixStackIn.translate(x + GUI_INV_CELL_LENGTH + (-2) + (-width / 2), y + GUI_INV_CELL_LENGTH + (-height / 2) - 1, 5);
			matrixStackIn.scale(.5f, .5f, 1f);
			this.font.drawStringWithShadow(matrixStackIn, count, 0, 0, 0xFFFFFFFF);
			matrixStackIn.pop();
		}
		
		if (mouseOver) {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 0, 1000);
			RenderFuncs.drawRect(matrixStackIn, x + 1, y + 1 , x + (GUI_INV_CELL_LENGTH - 1), y + (GUI_INV_CELL_LENGTH - 1), 0x60FFFFFF);
			matrixStackIn.pop();
			RenderSystem.disableBlend();
		}
	}
	
	private void drawSlider(MatrixStack matrixStackIn, int x, int y, boolean mouseOver) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		blit(matrixStackIn, x, y, GUI_TEXT_SLIDER_HOFFSET, mouseOver ? GUI_INV_SLIDER_HEIGHT : 0, GUI_INV_SLIDER_WIDTH, GUI_INV_SLIDER_HEIGHT);
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float p_73863_3_) {
		
		final int leftOffset = (this.width - GUI_TEXT_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - GUI_TEXT_HEIGHT) / 2;

		//Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		
		if (monitor.getNetwork() != null) {
			final List<ItemDeepStack> items = monitor.getNetwork().getAllCondensedNetworkItems(ItemCacheType.NET);
			final int len = items.size();
			final int rows = (int) Math.ceil((double)len / (double)GUI_CELL_COLS);
			final int spilloverRows = rows - GUI_CELL_ROWS;
			int invOffset = 0;
			
			matrixStackIn.push();
			
			// Adjust up or down depending on scroll
			if (spilloverRows <= 0 || scrollLag == 0f) {
				; // Do nothing
			} else {
				// adjust invOffset if we've scrolled beyond one row.
				// Then, offset drawing for any fractions remaining
				final int wholes = (int) Math.floor(scrollLag * spilloverRows);
				final double frac = (scrollLag * spilloverRows) - wholes;
				invOffset = wholes;
				matrixStackIn.translate(0, -GUI_INV_CELL_LENGTH * frac, 0);
			}
			
			for (int i = 0; i < GUI_CELL_ROWS + 1; i++)
			for (int j = 0; j < GUI_CELL_COLS; j++) {
				final int index = ((i + invOffset) * GUI_CELL_COLS) + j;
				ItemDeepStack stack = null;
				if (index < len) {
					stack = items.get(index);
				}
				
				int x = leftOffset + GUI_TOP_INV_HOFFSET + (j * GUI_INV_CELL_LENGTH);
				int y = topOffset + GUI_TOP_INV_VOFFSET + (i * GUI_INV_CELL_LENGTH);
				drawCell(matrixStackIn, x, y, stack,
						mouseX >= x && mouseX < x + GUI_INV_CELL_LENGTH && mouseY >= y && mouseY < y + GUI_INV_CELL_LENGTH);
			}
			
			// Draw item requests
			final List<ItemStack> requests = monitor.getItemRequests();
			int i = 0;
			for (ItemStack request : requests) {
				if (request.isEmpty()) {
					continue;
				}
				
				int x = leftOffset - (GUI_TEXT_MENUITEM_WIDTH);
				int y = topOffset + (i * GUI_TEXT_MENUITEM_HEIGHT);
				drawMenuItem(matrixStackIn, x, y, request, mouseX >= x && mouseX < x + GUI_TEXT_MENUITEM_WIDTH && mouseY >= y && mouseY < y + GUI_TEXT_MENUITEM_HEIGHT);
				i++;
			}
			
			matrixStackIn.pop();
		}
		
		matrixStackIn.push();
		matrixStackIn.translate(0, 0, 1000);
		//RenderSystem.disableLighting();
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		blit(matrixStackIn, leftOffset, topOffset, 0, 0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT);
		int sliderX = leftOffset + GUI_INV_SLIDER_HOFFSET;
		int sliderY = topOffset + GUI_INV_SLIDER_VOFFSET + (int) Math.ceil(GUI_INV_SLIDER_TOTAL_HEIGHT * scroll);
		drawSlider(matrixStackIn, sliderX, sliderY,
				mouseX >= sliderX && mouseX < sliderX + GUI_INV_SLIDER_WIDTH
				&& mouseY >= sliderY && mouseY < sliderY + GUI_INV_SLIDER_HEIGHT);
		matrixStackIn.pop();
		
		super.render(matrixStackIn, mouseX, mouseY, p_73863_3_);
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		final int leftOffset = (this.width - GUI_TEXT_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - GUI_TEXT_HEIGHT) / 2;
		
		// Scroll bar?
		
		int sliderY = GUI_INV_SLIDER_VOFFSET + (int) Math.ceil(GUI_INV_SLIDER_TOTAL_HEIGHT * scroll);
		if (mouseX >= leftOffset + GUI_INV_SLIDER_HOFFSET && mouseX <= leftOffset + GUI_INV_SLIDER_HOFFSET + GUI_INV_SLIDER_WIDTH
				&& mouseY >= topOffset + sliderY && mouseY <= topOffset + sliderY + GUI_INV_SLIDER_HEIGHT) {
			scrollClicked = true;
			mouseClickOffsetY = (int) (mouseY - (topOffset + sliderY));
			return true;
		}
		
		/*
		 * for (ItemStack request : requests) {
				if (request == null) {
					continue;
				}
				
				int x = leftOffset - (GUI_TEXT_MENUITEM_WIDTH);
				int y = topOffset + (i * GUI_TEXT_MENUITEM_HEIGHT);
				drawMenuItem(x, y, request, mouseX >= x && mouseX < x + GUI_TEXT_MENUITEM_WIDTH && mouseY >= y && mouseY < y + GUI_TEXT_MENUITEM_HEIGHT);
				i++;
			}
		 * 
		 */
		
		// Menu item?
		final int requests = monitor.getItemRequests().size();
		if (mouseX >= leftOffset + -GUI_TEXT_MENUITEM_WIDTH && mouseX <= leftOffset
				&& mouseY >= topOffset && mouseY <= topOffset + (GUI_TEXT_MENUITEM_HEIGHT * requests)) {
			// Which?
			final int index = (int) ((mouseY - topOffset) / GUI_TEXT_MENUITEM_HEIGHT);
			ItemStack req = monitor.getItemRequests().get(index);
			NetworkHandler.sendToServer(new StorageMonitorRequestMessage(monitor, req, true));
			return true;
		}
		
		// Slot?
		if (mouseX >= leftOffset + GUI_TOP_INV_HOFFSET && mouseX <= leftOffset + GUI_TOP_INV_HOFFSET + (GUI_INV_CELL_LENGTH * GUI_CELL_COLS)
				&& mouseY >= topOffset + GUI_TOP_INV_VOFFSET && mouseY <= topOffset + GUI_TOP_INV_VOFFSET + (GUI_INV_CELL_LENGTH * GUI_CELL_ROWS)) {
			// Only left and right mouse buttons
			if (mouseButton != 0 && mouseButton != 1) {
				return false;
			}
			final List<ItemDeepStack> items = monitor.getNetwork().getAllCondensedNetworkItems(ItemCacheType.NET);
			final int len = items.size();
			final int rows = (int) Math.ceil((double)len / (double)GUI_CELL_COLS);
			final int spilloverRows = rows - GUI_CELL_ROWS;
			final double invOffset;
			// Adjust up or down depending on scroll
			if (spilloverRows <= 0 || scrollLag == 0f) {
				invOffset = 0;
			} else {
				invOffset = scrollLag * spilloverRows;
//				// adjust invOffset if we've scrolled beyond one row.
//				// Then, offset drawing for any fractions remaining
//				final int wholes = (int) Math.floor(scrollLag * spilloverRows);
//				final double frac = (scrollLag * spilloverRows) - wholes;
//				invOffset = wholes;
//				GlStateManager.translate(0, -GUI_INV_CELL_LENGTH * frac, 0);
			}
			
			// invOffset is how many rows y=0 is at. Figure how far into cells we've clicked too to get the final cell index
			
			int x = (int) ((mouseX - (leftOffset + GUI_TOP_INV_HOFFSET)) / GUI_INV_CELL_LENGTH);
			int y = (int) ((mouseY - (topOffset + GUI_TOP_INV_VOFFSET)) / GUI_INV_CELL_LENGTH);
			final int index = (int) (x + (((y + invOffset) * GUI_CELL_COLS)));
			if (index < len) {
				ItemDeepStack clicked = items.get(index);
				ItemStack req = clicked.getTemplate().copy();
				req.setCount(mouseButton == 0
						? (int) Math.min(Math.max(0, clicked.getCount()), clicked.getTemplate().getMaxStackSize())
						: 1);
				//monitor.addRequest(req); local
				NetworkHandler.sendToServer(new StorageMonitorRequestMessage(monitor, req, false));
			}
			return true;
		}
		
		return false;
	}
	
	private void calcScroll(int mouseY, boolean finalize) {
		final int topOffset = (this.height - GUI_TEXT_HEIGHT) / 2;
		final int topY = topOffset + GUI_INV_SLIDER_VOFFSET;
		final int bottomY = topOffset + GUI_INV_SLIDER_VOFFSET + GUI_INV_SLIDER_TOTAL_HEIGHT;
		mouseY -= mouseClickOffsetY;
		mouseY = Math.min(Math.max(topY, mouseY), bottomY);
		scroll = (mouseY - topY) / (double) GUI_INV_SLIDER_TOTAL_HEIGHT;
		
		if (finalize) {
			// Round scroll to an even increment for the inventory
			if (monitor.getNetwork() != null) {
				final Collection<ItemDeepStack> items = monitor.getNetwork().getAllCondensedNetworkItems(ItemCacheType.NET);
				final int len = items.size();
				final int rows = (int) Math.ceil((double)len / (double) GUI_CELL_COLS);
				final int spilloverRows = rows - GUI_CELL_ROWS;
				if (spilloverRows <= 0) {
					scroll = 0f;
				} else {
					scroll = Math.round(scroll * spilloverRows) / (double) spilloverRows;
				}
			} else {
				scroll = 0f;
			}
		}
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int state) {
		// Do final update
		if (scrollClicked) {
			calcScroll((int) mouseY, true);
			return true;
		}
		
		scrollClicked = false;
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double dx, double dy) {
		if (scrollClicked) {
			calcScroll((int) mouseY, false);
			scrollLag = scroll; // Snap to when using the slider
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double dx) {
		if (monitor.getNetwork() != null) {
			final Collection<ItemDeepStack> items = monitor.getNetwork().getAllCondensedNetworkItems(ItemCacheType.NET);
			final int len = items.size();
			final int rows = (int) Math.ceil((double)len / (double) GUI_CELL_COLS);
			final int spilloverRows = rows - GUI_CELL_ROWS; // scroll bar represents this many rows of pixels
			
			if (spilloverRows > 0) {
				final double amt = -dx / (double) spilloverRows;
				scroll = Math.min(1.0, Math.max(0.0, scroll + amt));
				scrollLag = scroll;
			}
		} else {
			scroll = 0.0;
		}
		
		return true;
	}
	
}
