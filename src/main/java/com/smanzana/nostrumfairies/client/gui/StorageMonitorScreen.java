package com.smanzana.nostrumfairies.client.gui;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Mouse;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
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
	
	private static final int GUI_CELL_ROWS = GUI_TOP_INV_HEIGHT / GUI_INV_CELL_LENGTH;
	private static final int GUI_CELL_COLS = GUI_TOP_INV_WIDTH / GUI_INV_CELL_LENGTH;

	private LogisticsNetwork network;
	private double scroll;
	private double scrollLag;
	private int mouseClickOffsetY;
	private boolean scrollClicked;
	
	StorageMonitorScreen(LogisticsNetwork network) {
		this.network = network;
	}
	
	@Override	
	public void updateScreen() {
		if (network != null) {
			final List<ItemDeepStack> items = network.getAllCondensedNetworkItems();
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
	}
	
	private void drawCell(int x, int y, @Nullable ItemDeepStack stack, boolean mouseOver) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXT);
		GlStateManager.color(1f, 1f, 1f, 1f);
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
			final int width = this.fontRendererObj.getStringWidth(count);
			final int height = this.fontRendererObj.FONT_HEIGHT;
			this.itemRender.renderItemIntoGUI(stack.getTemplate(), x + 1, y + 1);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 1000);
			Gui.drawRect(x + 1, y + (GUI_INV_CELL_LENGTH - 6) , x + (GUI_INV_CELL_LENGTH - 1), y + (GUI_INV_CELL_LENGTH - 1), 0x60000000);
			GlStateManager.translate(x + GUI_INV_CELL_LENGTH + (-2) + (-width / 2), y + GUI_INV_CELL_LENGTH + (-height / 2) - 1, 0);
			GlStateManager.scale(.5, .5, 1);
			this.fontRendererObj.drawStringWithShadow(count, 0, 0, 0xFFFFFFFF);
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
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXT);
		GlStateManager.color(1f, 1f, 1f, 1f);
		this.drawTexturedModalRect(x, y, GUI_TEXT_SLIDER_HOFFSET, mouseOver ? GUI_INV_SLIDER_HEIGHT : 0, GUI_INV_SLIDER_WIDTH, GUI_INV_SLIDER_HEIGHT);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float p_73863_3_) {
		
		final int leftOffset = (this.width - GUI_TEXT_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - GUI_TEXT_HEIGHT) / 2;
		
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXT);
		
		if (network != null) {
			final List<ItemDeepStack> items = network.getAllCondensedNetworkItems();
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
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 1000);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXT);
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
		return true;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		final int leftOffset = (this.width - GUI_TEXT_WIDTH) / 2; //distance from left
		final int topOffset = (this.height - GUI_TEXT_HEIGHT) / 2;
		
		int sliderY = GUI_INV_SLIDER_VOFFSET + (int) Math.ceil(GUI_INV_SLIDER_TOTAL_HEIGHT * scroll);
		if (mouseX >= leftOffset + GUI_INV_SLIDER_HOFFSET && mouseX <= leftOffset + GUI_INV_SLIDER_HOFFSET + GUI_INV_SLIDER_WIDTH
				&& mouseY >= topOffset + sliderY && mouseY <= topOffset + sliderY + GUI_INV_SLIDER_HEIGHT) {
			scrollClicked = true;
			mouseClickOffsetY = mouseY - (topOffset + sliderY);
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
			if (network != null) {
				final List<ItemDeepStack> items = network.getAllCondensedNetworkItems();
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
			if (network != null) {
				final List<ItemDeepStack> items = network.getAllCondensedNetworkItems();
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
