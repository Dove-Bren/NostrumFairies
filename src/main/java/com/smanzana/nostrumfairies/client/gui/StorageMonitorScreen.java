package com.smanzana.nostrumfairies.client.gui;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Mouse;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.ItemCacheType;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.network.messages.StorageMonitorRequestMessage;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class StorageMonitorScreen extends GuiScreen {
	
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
		this.monitor = monitor;
	}
	
	@Override	
	public void updateScreen() {
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
		
		if (monitor.getWorld().getTotalWorldTime() % 20 == 0) {
			LogisticsNetwork network = monitor.getNetwork();
			if (network != null) {
				NetworkHandler.getSyncChannel().sendToServer(new LogisticsUpdateRequest(network.getUUID()));
			} else {
				NetworkHandler.getSyncChannel().sendToServer(new LogisticsUpdateRequest());
			}
		}
	}
	
	private void drawMenuItem(int x, int y, ItemStack request, boolean mouseOver) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		GlStateManager.enableLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.enableAlpha();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		GlStateManager.color(1.0f, 1.0f, 1.0f, 0.9f);
		final float value = (mouseOver ? .8f : 1f);
		GlStateManager.color(value, value, value, 1.0f);
		this.drawTexturedModalRect(x, y, GUI_TEXT_MENUITEM_HOFFSET, GUI_TEXT_MENUITEM_VOFFSET, GUI_TEXT_MENUITEM_WIDTH, GUI_TEXT_MENUITEM_HEIGHT);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (!request.isEmpty()) {
			RenderHelper.disableStandardItemLighting();
			RenderHelper.enableGUIStandardItemLighting();
			this.itemRender.renderItemIntoGUI(request, x + GUI_TEXT_MENUITEM_SLOT_HOFFSET, y + GUI_TEXT_MENUITEM_SLOT_VOFFSET);
			this.itemRender.renderItemOverlayIntoGUI(fontRenderer, request, x + GUI_TEXT_MENUITEM_SLOT_HOFFSET, y + GUI_TEXT_MENUITEM_SLOT_VOFFSET, request.getCount() + "");
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableTexture2D();
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.enableAlpha();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			
//			GlStateManager.pushMatrix();
//			GlStateManager.translate(0, 0, 1000);
//			Gui.drawRect(x + 1, y + (GUI_INV_CELL_LENGTH - 6) , x + (GUI_INV_CELL_LENGTH - 1), y + (GUI_INV_CELL_LENGTH - 1), 0x60000000);
//			GlStateManager.popMatrix();
		}
		
		if (mouseOver) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 1000);
			Gui.drawRect(x + GUI_TEXT_MENUITEM_SLOT_HOFFSET, y + GUI_TEXT_MENUITEM_SLOT_VOFFSET,
					x + GUI_TEXT_MENUITEM_SLOT_HOFFSET + (GUI_INV_CELL_LENGTH - 1), y + GUI_TEXT_MENUITEM_SLOT_VOFFSET + (GUI_INV_CELL_LENGTH - 1),
					0x60FFFFFF);
			GlStateManager.popMatrix();
		}
	}
	
	private void drawCell(int x, int y, @Nullable ItemDeepStack stack, boolean mouseOver) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 0.9f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.enableAlpha();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		this.drawTexturedModalRect(x, y, GUI_TEXT_CELL_HOFFSET, 0, GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH);
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
			final int width = this.fontRenderer.getStringWidth(count);
			final int height = this.fontRenderer.FONT_HEIGHT;
			RenderHelper.disableStandardItemLighting();
			RenderHelper.enableGUIStandardItemLighting();
			this.itemRender.renderItemIntoGUI(stack.getTemplate(), x + 1, y + 1);
			RenderHelper.disableStandardItemLighting();
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 1000);
			Gui.drawRect(x + 1, y + (GUI_INV_CELL_LENGTH - 6) , x + (GUI_INV_CELL_LENGTH - 1), y + (GUI_INV_CELL_LENGTH - 1), 0x60000000);
			GlStateManager.translate(x + GUI_INV_CELL_LENGTH + (-2) + (-width / 2), y + GUI_INV_CELL_LENGTH + (-height / 2) - 1, 0);
			GlStateManager.scale(.5, .5, 1);
			this.fontRenderer.drawStringWithShadow(count, 0, 0, 0xFFFFFFFF);
			GlStateManager.popMatrix();
		}
		
		if (mouseOver) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 1000);
			Gui.drawRect(x + 1, y + 1 , x + (GUI_INV_CELL_LENGTH - 1), y + (GUI_INV_CELL_LENGTH - 1), 0x60FFFFFF);
			GlStateManager.popMatrix();
		}
	}
	
	private void drawSlider(int x, int y, boolean mouseOver) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		GlStateManager.color(1f, 1f, 1f, 1f);
		this.drawTexturedModalRect(x, y, GUI_TEXT_SLIDER_HOFFSET, mouseOver ? GUI_INV_SLIDER_HEIGHT : 0, GUI_INV_SLIDER_WIDTH, GUI_INV_SLIDER_HEIGHT);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float p_73863_3_) {
		
		final int leftOffset = (this.width - GUI_TEXT_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - GUI_TEXT_HEIGHT) / 2;

		GlStateManager.color(1.0f, 1.0f, 1.0f, 0.9f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableTexture2D();
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		
		if (monitor.getNetwork() != null) {
			final List<ItemDeepStack> items = monitor.getNetwork().getAllCondensedNetworkItems(ItemCacheType.NET);
			final int len = items.size();
			final int rows = (int) Math.ceil((double)len / (double)GUI_CELL_COLS);
			final int spilloverRows = rows - GUI_CELL_ROWS;
			int invOffset = 0;
			
			GlStateManager.pushMatrix();
			
			// Adjust up or down depending on scroll
			if (spilloverRows <= 0 || scrollLag == 0f) {
				; // Do nothing
			} else {
				// adjust invOffset if we've scrolled beyond one row.
				// Then, offset drawing for any fractions remaining
				final int wholes = (int) Math.floor(scrollLag * spilloverRows);
				final double frac = (scrollLag * spilloverRows) - wholes;
				invOffset = wholes;
				GlStateManager.translate(0, -GUI_INV_CELL_LENGTH * frac, 0);
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
				drawCell(x, y, stack,
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
				drawMenuItem(x, y, request, mouseX >= x && mouseX < x + GUI_TEXT_MENUITEM_WIDTH && mouseY >= y && mouseY < y + GUI_TEXT_MENUITEM_HEIGHT);
				i++;
			}
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 1000);
		Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 0.9f);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.enableTexture2D();
		this.drawTexturedModalRect(leftOffset, topOffset, 0, 0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT);
		int sliderX = leftOffset + GUI_INV_SLIDER_HOFFSET;
		int sliderY = topOffset + GUI_INV_SLIDER_VOFFSET + (int) Math.ceil(GUI_INV_SLIDER_TOTAL_HEIGHT * scroll);
		drawSlider(sliderX, sliderY,
				mouseX >= sliderX && mouseX < sliderX + GUI_INV_SLIDER_WIDTH
				&& mouseY >= sliderY && mouseY < sliderY + GUI_INV_SLIDER_HEIGHT);
		GlStateManager.popMatrix();
		
		super.drawScreen(mouseX, mouseY, p_73863_3_);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		final int leftOffset = (this.width - GUI_TEXT_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - GUI_TEXT_HEIGHT) / 2;
		
		// Scroll bar?
		
		int sliderY = GUI_INV_SLIDER_VOFFSET + (int) Math.ceil(GUI_INV_SLIDER_TOTAL_HEIGHT * scroll);
		if (mouseX >= leftOffset + GUI_INV_SLIDER_HOFFSET && mouseX <= leftOffset + GUI_INV_SLIDER_HOFFSET + GUI_INV_SLIDER_WIDTH
				&& mouseY >= topOffset + sliderY && mouseY <= topOffset + sliderY + GUI_INV_SLIDER_HEIGHT) {
			scrollClicked = true;
			mouseClickOffsetY = mouseY - (topOffset + sliderY);
			return;
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
			final int index = (mouseY - topOffset) / GUI_TEXT_MENUITEM_HEIGHT;
			ItemStack req = monitor.getItemRequests().get(index);
			NetworkHandler.getSyncChannel().sendToServer(new StorageMonitorRequestMessage(monitor, req, true));
		}
		
		// Slot?
		if (mouseX >= leftOffset + GUI_TOP_INV_HOFFSET && mouseX <= leftOffset + GUI_TOP_INV_HOFFSET + (GUI_INV_CELL_LENGTH * GUI_CELL_COLS)
				&& mouseY >= topOffset + GUI_TOP_INV_VOFFSET && mouseY <= topOffset + GUI_TOP_INV_VOFFSET + (GUI_INV_CELL_LENGTH * GUI_CELL_ROWS)) {
			// Only left and right mouse buttons
			if (mouseButton != 0 && mouseButton != 1) {
				return;
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
			
			int x = (mouseX - (leftOffset + GUI_TOP_INV_HOFFSET)) / GUI_INV_CELL_LENGTH;
			int y = (mouseY - (topOffset + GUI_TOP_INV_VOFFSET)) / GUI_INV_CELL_LENGTH;
			final int index = (int) (x + (((y + invOffset) * GUI_CELL_COLS)));
			if (index < len) {
				ItemDeepStack clicked = items.get(index);
				ItemStack req = clicked.getTemplate().copy();
				req.setCount(mouseButton == 0
						? (int) Math.min(Math.max(0, clicked.getCount()), clicked.getTemplate().getMaxStackSize())
						: 1);
				//monitor.addRequest(req); local
				NetworkHandler.getSyncChannel().sendToServer(new StorageMonitorRequestMessage(monitor, req, false));
			}
			return;
		}
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
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		// Do final update
		if (scrollClicked) {
			calcScroll(mouseY, true);
		}
		
		scrollClicked = false;
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (scrollClicked) {
			calcScroll(mouseY, false);
			scrollLag = scroll; // Snap to when using the slider
		}
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		int wheel = Mouse.getEventDWheel();
		if (wheel != 0) {
			// 120 seems to be scroll bar rotation magnitude?
			if (monitor.getNetwork() != null) {
				final Collection<ItemDeepStack> items = monitor.getNetwork().getAllCondensedNetworkItems(ItemCacheType.NET);
				final int len = items.size();
				final int rows = (int) Math.ceil((double)len / (double) GUI_CELL_COLS);
				final int spilloverRows = rows - GUI_CELL_ROWS; // scroll bar represents this many rows of pixels
				
				if (spilloverRows > 0) {
					final double amt = ((double) -wheel / 120.0) / (double) spilloverRows;
					scroll = Math.min(1.0, Math.max(0.0, scroll + amt));
					scrollLag = scroll;
				}
			} else {
				scroll = 0.0;
			}
			
		}
		super.handleMouseInput();
	}
	
}
