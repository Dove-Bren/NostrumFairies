package com.smanzana.nostrumfairies.client.gui.container;

import java.io.IOException;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.ILogisticsLogicProvider;
import com.smanzana.nostrumfairies.blocks.LogisticsLogicComponent;
import com.smanzana.nostrumfairies.blocks.LogisticsLogicComponent.LogicMode;
import com.smanzana.nostrumfairies.blocks.LogisticsLogicComponent.LogicOp;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogicPanelActionMessage;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LogicPanel {
	
	private static final int GUI_INV_CELL_LENGTH = 18;
	
	private static final int PANEL_SLOT_WIDTH = 18;
	private static final int PANEL_SLOT_HEIGHT = 18;
	
	private static final int PANEL_BUTTON_WIDTH = 18;
	private static final int PANEL_BUTTON_HEIGHT = 18;
	
	private static final int PANEL_MODE_WIDTH = 32;
	private static final int PANEL_MODE_HEIGHT = 32;

	protected final ILogisticsLogicProvider logicProvider;
	protected final LogisticsLogicComponent comp;
	protected final LogicContainer parent;
	
	protected final int x; // left x
	protected final int y; // top y
	protected final int width; // total width of the panel
	protected final int height; // total height
	
	// Calculated vars. Mostly for GUI but offset is used in container
	protected final int margin;
	protected final int upperSpace; // Space for top margin + (maybe) top mode button and margin. Cached for less addition
	
	protected final HideableSlot templateSlot;
	protected final ItemStack[] invArray;
	protected final IInventory inv;
	
	public LogicPanel(LogicContainer parentGui, ILogisticsLogicProvider logicProvider, int x, int y, int width, int height) {
		this.parent = parentGui;
		this.logicProvider = logicProvider;
		this.comp = logicProvider.getLogicComponent();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		invArray = new ItemStack[1];
		invArray[0] = comp.getLogicTemplate();
		inv = new Inventories.ItemStackArrayWrapper(invArray);
		
		// Have to figure out where slot will be. This is why position and size info needs to be in the container.
		// Slot only shows up in logic mode, so it'll only be in one spot (so we only need one slot).
		// Margin spacing depends on whether we need to show the mode button or not
		final int sections = (comp.isLogicOnly() ? 4 : 5);
		final int minHeight = (comp.isLogicOnly() ? 0 : PANEL_BUTTON_HEIGHT) + PANEL_SLOT_HEIGHT + PANEL_BUTTON_HEIGHT + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 6);
		final int leftover = Math.max(0, height - minHeight);
		this.margin = (leftover / sections);
		this.upperSpace = (comp.isLogicOnly() ? margin : (margin + PANEL_BUTTON_HEIGHT + margin)); // mode button, but uses BUTTON height
		
		final int slotY = 1 + upperSpace;
		
		this.templateSlot = new HideableSlot(inv, 0, x + (width - GUI_INV_CELL_LENGTH) / 2, slotY);
		this.parent.addSlotToContainer(templateSlot);
	}
	
	/**
	 * Attempts to handle the slot click action performed by the player. If the slot doesn't work with this panel, returns false
	 * indicating the parent should run its own logic. Returning true indicates action was taken, and the parent should avoid
	 * performing any more logic.
	 * @param slotId
	 * @param dragType
	 * @param clickTypeIn
	 * @param player
	 * @return
	 */
	public boolean handleSlotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		if (slotId == templateSlot.slotNumber) {
			if (player.inventory.getItemStack() == null) {
				// empty hand. Right-click?
				if (dragType == 1 && clickTypeIn == ClickType.PICKUP) {
					setTemplate(null);
				}
			} else {
				// Item in hand. Clicking empty templatable slot?
				if (clickTypeIn == ClickType.PICKUP) {
					if (!templateSlot.getHasStack()) {
						ItemStack template = player.inventory.getItemStack().copy();
						template.stackSize = 1;
						setTemplate(template);
					}
				}
			}
			

			// Always return true here because we want no action taken
			return true;
		}
		
		return false;
	}
	
	protected void setTemplate(@Nullable ItemStack template) {
		NetworkHandler.getSyncChannel().sendToServer(new LogicPanelActionMessage(this.logicProvider, template));
		invArray[0] = template;
	}
	
	protected void setOp(LogicOp op) {
		NetworkHandler.getSyncChannel().sendToServer(new LogicPanelActionMessage(this.logicProvider, op));
	}
	
	protected void setCount(int count) {
		NetworkHandler.getSyncChannel().sendToServer(new LogicPanelActionMessage(this.logicProvider, count));
	}
	
	protected void setMode(LogicMode mode) {
		NetworkHandler.getSyncChannel().sendToServer(new LogicPanelActionMessage(this.logicProvider, mode));
	}
	
	protected static class HideableSlot extends Slot {

		protected boolean hidden;
		protected final int originalX;
		protected final int originalY;
		
		public HideableSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
			this.originalX = xPosition;
			this.originalY = yPosition;
		}
		
		@Override
		public boolean canBeHovered() {
			return !hidden;
		}
		
		public void hide(boolean hide) {
			if (hide != hidden) {
				hidden = hide;
				if (hide) {
					this.xDisplayPosition = -1000;
					this.yDisplayPosition = -1000;
				} else {
					this.xDisplayPosition = originalX;
					this.yDisplayPosition = originalY;
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class LogicPanelGui {
		
		private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/logic_panel.png");
		
		private static final int GUI_INV_CELL_LENGTH = 18;
		
		private static final int GUI_PANEL_TEXT_WIDTH = 80;
		private static final int GUI_PANEL_TEXT_HEIGHT = 80;
		
		private static final int GUI_SLOT_TEXT_HOFFSET = 0;
		private static final int GUI_SLOT_TEXT_VOFFSET = 80;
		
		private static final int GUI_BUTTON_TEXT_HOFFSET = 0;
		private static final int GUI_BUTTON_TEXT_VOFFSET = 98;
		
		private static final int GUI_MODE_ICON_TEXT_HOFFSET = 0;
		private static final int GUI_MODE_ICON_TEXT_VOFFSET = 116;
		
		protected final LogicPanel panel;
		protected final LogicGuiContainer parent;
		public final boolean drawBackground;
		protected final float colorRed;
		protected final float colorGreen;
		protected final float colorBlue;
		protected final float colorAlpha;
		
		private OpButton opButton;
		private ModeButton modeButton;
		
		private String criteriaString;
		private boolean editSelected;
		
		public LogicPanelGui(LogicPanel panel, LogicGuiContainer parent, int color, boolean drawBackground) {
			this.panel = panel;
			this.parent = parent;
			this.drawBackground = drawBackground;
			
			colorAlpha = (float) ((color >> 24) & 255) / 255f;
			colorRed = (float) ((color >> 16) & 255) / 255f;
			colorGreen = (float) ((color >> 8) & 255) / 255f;
			colorBlue = (float) ((color >> 0) & 255) / 255f;
		}
		
		public void initGui(Minecraft mc, int guiLeft, int guiTop) {
			
			opButton = new OpButton(1, guiLeft + panel.x + (panel.width - PANEL_BUTTON_WIDTH) / 2 - 1,
					guiTop + panel.y + (panel.upperSpace + GUI_INV_CELL_LENGTH + panel.margin));
			parent.addButton(opButton);
			
			if (!panel.comp.isLogicOnly()) {
				modeButton = new ModeButton(2, guiLeft + panel.x + (panel.width - PANEL_BUTTON_WIDTH) / 2 - 1,
						guiTop + panel.y + (panel.margin));
				parent.addButton(modeButton);
			}
			
			editSelected = false;
			criteriaString = String.format("%d", panel.comp.getLogicCount());
			
			final boolean logicMode = (panel.comp.getLogicMode() == LogicMode.LOGIC);
			opButton.visible = logicMode;
			panel.templateSlot.hide(!logicMode);
		}
		
		protected void color() {
			GlStateManager.color(colorRed, colorGreen, colorBlue, colorAlpha);
		}
		
		public void draw(Minecraft mc, int guiLeft, int guiTop) {
			final int left = guiLeft + panel.x;
			final int top = guiTop + panel.y;
			final boolean logicMode = (panel.comp.getLogicMode() == LogicMode.LOGIC);
			
			if (this.drawBackground) {
				mc.getTextureManager().bindTexture(TEXT);
				color();
				Gui.drawScaledCustomSizeModalRect(left, top, 0, 0,
						GUI_PANEL_TEXT_WIDTH, GUI_PANEL_TEXT_HEIGHT, panel.width, panel.height, 256, 256);
			}
			
			// Vertical offset and arrangement of stuff depends on whether we allow logic or not
			GlStateManager.pushMatrix();
			//GlStateManager.translate(guiLeft + panel.x - (GUI_INV_CELL_LENGTH / 2) - 1, guiTop + panel.y - 1, 0);
			GlStateManager.translate(guiLeft + panel.templateSlot.xDisplayPosition - 1, guiTop + panel.templateSlot.yDisplayPosition - 1, 0);
			drawSlot(mc);
			GlStateManager.popMatrix();
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			
			if (logicMode) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(left, top + (panel.upperSpace + GUI_INV_CELL_LENGTH + panel.margin + PANEL_BUTTON_HEIGHT + panel.margin), 0);
				drawInputBar(mc);
				GlStateManager.popMatrix();
			}

			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
		}
		
		private void drawSlot(Minecraft mc) {
			mc.getTextureManager().bindTexture(TEXT);
			color();
			Gui.drawScaledCustomSizeModalRect(0, 0, GUI_SLOT_TEXT_HOFFSET, GUI_SLOT_TEXT_VOFFSET, PANEL_SLOT_WIDTH, PANEL_SLOT_HEIGHT, PANEL_SLOT_WIDTH, PANEL_SLOT_HEIGHT, 256, 256);
		}
		
		private void drawInputBar(Minecraft mc) {
			final int barWidth = Math.min(panel.width - 12, 100);
			final int centerX = (panel.width / 2);
			Gui.drawRect(-1 + centerX - (barWidth / 2), -1, 1 + centerX + (barWidth / 2), mc.fontRendererObj.FONT_HEIGHT + 3, 0xFF444444);
			Gui.drawRect(centerX - (barWidth / 2), 0, centerX + (barWidth / 2), mc.fontRendererObj.FONT_HEIGHT + 2, 0xFF000000);
			
			final int width = mc.fontRendererObj.getStringWidth(criteriaString);
			mc.fontRendererObj.drawString(criteriaString, (panel.width - width) / 2, 2, 0xFFFFFFFF);
			
			if (editSelected) {
				final long period = 600; // .5 seconds
				if ((System.currentTimeMillis() % (2 * period)) / period == 1) {
					final int x = ((panel.width + width) / 2) + 1;
					//Gui.drawRect(x, 1, x + 1, this.fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
					mc.fontRendererObj.drawString("_", x, 2, 0xFFFFFFFF);
				}
			}
		}
		
		private void drawCriteriaMode(Minecraft mc, LogicMode mode) {
			int textX = GUI_MODE_ICON_TEXT_HOFFSET;
			switch (mode) {
			case ALWAYS:
			default:
				;
				break;
			case LOGIC:
				textX += PANEL_MODE_WIDTH;
				break;
			case REDSTONE_LOW:
				textX += PANEL_MODE_WIDTH * 2;
				break;
			case REDSTONE_HIGH:
				textX += PANEL_MODE_WIDTH * 3;
				break;
			
			}
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1f);
			mc.getTextureManager().bindTexture(TEXT);
			GlStateManager.enableBlend();
			Gui.drawScaledCustomSizeModalRect(1, 1,
					textX, GUI_MODE_ICON_TEXT_VOFFSET,
					PANEL_MODE_WIDTH, PANEL_MODE_HEIGHT,
					PANEL_BUTTON_WIDTH - 2, PANEL_BUTTON_HEIGHT - 2,
					256, 256);
		}
		
		private void drawCriteriaOp(FontRenderer fonter, LogicOp op) {
			final String s;
			switch (op) {
			case EQUAL:
			default:
				s = "=";
				break;
			case LESS:
				s = "<";
				break;
			case MORE:
				s = ">";
				break;
			}
			
			final int sWidth = fonter.getStringWidth(s);
			fonter.drawString(s, (PANEL_BUTTON_WIDTH + -sWidth) / 2, 1 + (PANEL_BUTTON_HEIGHT - fonter.FONT_HEIGHT) / 2, 0xFFFFFFFF);
		}
		
		public boolean actionPerformed(GuiButton button) {
			if (button == opButton) {
				LogicOp op = panel.comp.getLogicOp();
				// Cycle up modes
				op = (LogicOp.values()[(op.ordinal() + 1) % LogicOp.values().length]);
				
				panel.setOp(op);
				return true;
			}
			
			if (button == modeButton) {
				LogicMode mode = panel.comp.getLogicMode();
				// Cycle up modes
				mode = (LogicMode.values()[(mode.ordinal() + 1) % LogicMode.values().length]);
				
				panel.setMode(mode);
				
				// Also refresh hidden buttons
				final boolean logicMode = (mode == LogicMode.LOGIC);
				opButton.visible = logicMode;
				panel.templateSlot.hide(!logicMode);
				
				return true;
			}
			
			return false;
		}
		
		public boolean keyTyped(char typedChar, int keyCode) throws IOException {
			if (this.editSelected) {
				if (Character.isDigit(typedChar) || keyCode == 14) { // 14 = backspace
					final String s;
					if (keyCode == 14) {
						if (!criteriaString.isEmpty()) {
							s = criteriaString.substring(0, criteriaString.length() - 1);
						} else {
							s = "";
						}
					} else {
						s = this.criteriaString + typedChar;
					}
					
					int val;
					try {
						val = Integer.parseInt(s);
					} catch (Exception e) {
						if (s.length() > 2) {
							// Overflowed
							val = Integer.MAX_VALUE;
						} else {
							val = 0;
						}
					}
					
					this.criteriaString = String.format("%d", val);
					panel.setCount(val);
					return true;
				} else if (keyCode == 28) { // ENTER/return
					this.editSelected = false;
					return true;
				} 
			}
//			else if (editSelected && keyCode == 1) {
//				editSelected = false;
//				return;
//			}
			return false;
		}
		
		protected boolean mouseClicked(int mouseX, int mouseY, int mouseButton, int guiLeft, int guiTop) throws IOException {
			final int barWidth = Math.min(panel.width - 4, 100);
			final int barVOffset = (panel.upperSpace + GUI_INV_CELL_LENGTH + panel.margin + PANEL_BUTTON_HEIGHT + panel.margin);
			final int xHalf = (panel.width / 2);
			final int minX = guiLeft + panel.x + xHalf - (barWidth / 2);
			final int maxX = guiLeft + panel.x + xHalf + (barWidth / 2);
			final int minY = guiTop + panel.y + barVOffset;
			final int maxY = guiTop + panel.y + barVOffset + parent.mc.fontRendererObj.FONT_HEIGHT + 2;
			
			if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY) {
				editSelected = true;
				return true;
			}
			
			editSelected = false;
			return false;
		}
		
		protected class ModeButton extends GuiButton {

			private boolean pressed;
			
			public ModeButton(int buttonId, int x, int y) {
				super(buttonId, x, y, PANEL_BUTTON_WIDTH, PANEL_BUTTON_HEIGHT, "");
				pressed = false;
			}
			
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY) {
				this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				
				int textX = GUI_BUTTON_TEXT_HOFFSET;
				if (pressed) {
					textX += PANEL_BUTTON_WIDTH * 2;
				} else if (hovered) {
					textX += PANEL_BUTTON_WIDTH;
				}
				
				color();
				mc.getTextureManager().bindTexture(TEXT);
				GlStateManager.enableBlend();
				GlStateManager.pushMatrix();
				GlStateManager.translate(xPosition, yPosition, 0);
				this.drawTexturedModalRect(0, 0,
						textX, GUI_BUTTON_TEXT_VOFFSET,
						PANEL_BUTTON_WIDTH, PANEL_BUTTON_HEIGHT);
				
				// Then draw mode
				LogicMode mode = panel.comp.getLogicMode();
				drawCriteriaMode(mc, mode);
				GlStateManager.popMatrix();
			}
			
			@Override
			public void mouseReleased(int mouseX, int mouseY) {
				pressed = false;
				super.mouseReleased(mouseX, mouseY);
			}
			
			public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
				boolean ret = super.mousePressed(mc, mouseX, mouseY);
				pressed = ret;
				return ret;
			}
			
		}
		
		protected class OpButton extends GuiButton {

			private boolean pressed;
			
			public OpButton(int buttonId, int x, int y) {
				super(buttonId, x, y, PANEL_BUTTON_WIDTH, PANEL_BUTTON_HEIGHT, "");
				pressed = false;
			}
			
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY) {
				if (!this.visible) {
					return;
				}
				
				this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				
				int textX = GUI_BUTTON_TEXT_HOFFSET;
				if (pressed) {
					textX += PANEL_BUTTON_WIDTH * 2;
				} else if (hovered) {
					textX += PANEL_BUTTON_WIDTH;
				}
				
				color();
				mc.getTextureManager().bindTexture(TEXT);
				GlStateManager.enableBlend();
				GlStateManager.pushMatrix();
				GlStateManager.translate(xPosition, yPosition, 0);
				this.drawTexturedModalRect(0, 0,
						textX, GUI_BUTTON_TEXT_VOFFSET,
						PANEL_BUTTON_WIDTH, PANEL_BUTTON_HEIGHT);
				
				// Then draw mode
				drawCriteriaOp( mc.fontRendererObj, panel.comp.getLogicOp());
				GlStateManager.popMatrix();
			}
			
			@Override
			public void mouseReleased(int mouseX, int mouseY) {
				pressed = false;
				super.mouseReleased(mouseX, mouseY);
			}
			
			public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
				boolean ret = super.mousePressed(mc, mouseX, mouseY);
				pressed = ret;
				return ret;
			}
			
		}
		
	}
}
