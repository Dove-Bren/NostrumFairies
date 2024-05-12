package com.smanzana.nostrumfairies.client.gui.container;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogicPanelActionMessage;
import com.smanzana.nostrumfairies.tiles.ILogisticsLogicProvider;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.LogicMode;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.LogicOp;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		
		final int fontHeight = 14; // Minecraft.getInstance().fontRenderer.FONT_HEIGHT
		
		// Have to figure out where slot will be. This is why position and size info needs to be in the container.
		// Slot only shows up in logic mode, so it'll only be in one spot (so we only need one slot).
		// Margin spacing depends on whether we need to show the mode button or not
		final int sections = (comp.isLogicOnly() ? 4 : 5);
		final int minHeight = (comp.isLogicOnly() ? 0 : PANEL_BUTTON_HEIGHT) + PANEL_SLOT_HEIGHT + PANEL_BUTTON_HEIGHT + (fontHeight + 6);
		final int leftover = Math.max(0, height - minHeight);
		this.margin = (leftover / sections);
		this.upperSpace = 5 + (comp.isLogicOnly() ? margin : (margin + PANEL_BUTTON_HEIGHT + margin)); // mode button, but uses BUTTON height
		
		final int slotY = 1 + upperSpace;
		
		this.templateSlot = new HideableSlot(inv, 0, x + (width - GUI_INV_CELL_LENGTH) / 2, slotY);
		this.parent.addSlot(templateSlot);
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
	public boolean handleSlotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slotId == templateSlot.slotNumber) {
			if (player.inventory.getItemStack().isEmpty()) {
				// empty hand. Right-click?
				if (dragType == 1 && clickTypeIn == ClickType.PICKUP) {
					setTemplate(ItemStack.EMPTY);
				}
			} else {
				// Item in hand. Clicking empty templatable slot?
				if (clickTypeIn == ClickType.PICKUP) {
					if (!templateSlot.getHasStack()) {
						ItemStack template = player.inventory.getItemStack().copy();
						template.setCount(1);
						setTemplate(template);
					}
				}
			}
			

			// Always return true here because we want no action taken
			return true;
		}
		
		return false;
	}
	
	protected void setTemplate(@Nonnull ItemStack template) {
		NetworkHandler.sendToServer(new LogicPanelActionMessage(this.logicProvider, template));
		invArray[0] = template;
	}
	
	protected void setOp(LogicOp op) {
		NetworkHandler.sendToServer(new LogicPanelActionMessage(this.logicProvider, op));
	}
	
	protected void setCount(int count) {
		NetworkHandler.sendToServer(new LogicPanelActionMessage(this.logicProvider, count));
	}
	
	protected void setMode(LogicMode mode) {
		NetworkHandler.sendToServer(new LogicPanelActionMessage(this.logicProvider, mode));
	}
	
	protected static class HideableSlot extends Slot {

		protected boolean hidden;
		protected final int originalX;
		protected final int originalY;
		
		public HideableSlot(IInventory inventoryIn, int index, int x, int y) {
			super(inventoryIn, index, x, y);
			this.originalX = x;
			this.originalY = y;
		}
		
		@Override
		public boolean isEnabled() {
			return !hidden;
		}
		
		public void hide(boolean hide) {
			if (hide != hidden) {
				hidden = hide;
				if (hide) {
					this.xPos = -1000;
					this.yPos = -1000;
				} else {
					this.xPos = originalX;
					this.yPos = originalY;
				}
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class LogicPanelGui<T extends LogicGuiContainer<?>> extends Widget {
		
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
		protected final T parent;
		public final boolean drawBackground;
		protected final float colorRed;
		protected final float colorGreen;
		protected final float colorBlue;
		protected final float colorAlpha;
		
		private OpButton opButton;
		private ModeButton modeButton;
		
		private TextFieldWidget criteriaField;
		
		public LogicPanelGui(LogicPanel panel, T parent, int color, boolean drawBackground) {
			super(panel.x, panel.y, panel.width, panel.height, new StringTextComponent("logistics panel"));
			this.panel = panel;
			this.parent = parent;
			this.drawBackground = drawBackground;
			
			colorAlpha = (float) ((color >> 24) & 255) / 255f;
			colorRed = (float) ((color >> 16) & 255) / 255f;
			colorGreen = (float) ((color >> 8) & 255) / 255f;
			colorBlue = (float) ((color >> 0) & 255) / 255f;
			
			final Minecraft mc = Minecraft.getInstance();
			this.criteriaField = new TextFieldWidget(mc.fontRenderer, 0, 0, 30, 5, new StringTextComponent("logic panel value field"));
			this.criteriaField.setText(panel.comp.getLogicCount() + "");
			this.criteriaField.setValidator((s) -> {
				if (s.isEmpty()) {
					return true;
				}
				
				try {
					int val = Integer.parseInt(s);
					return val >= 0 && val <= Integer.MAX_VALUE;
				} catch (Exception e) {
					return false;
				}
			});
			this.criteriaField.setResponder((s) -> {
				panel.setCount(s.isEmpty() ? 0 : Integer.valueOf(s));
			});
			
			
			parent.addButton(criteriaField);
		}
		
		public void init(Minecraft mc, int guiLeft, int guiTop) {
			
			final int left = guiLeft + panel.x;
			final int top = guiTop + panel.y;
			
			opButton = new OpButton(left + (panel.width - PANEL_BUTTON_WIDTH) / 2 - 1,
					top + (panel.upperSpace + GUI_INV_CELL_LENGTH + panel.margin),
					this);
			parent.addButton(opButton);
			
			if (!panel.comp.isLogicOnly()) {
				modeButton = new ModeButton(left + (panel.width - PANEL_BUTTON_WIDTH) / 2 - 1,
						top + (panel.margin) + 5,
						this);
				parent.addButton(modeButton);
			}
			
			final int barWidth = Math.min(panel.width - 12, 100);
			final int barHeight = mc.fontRenderer.FONT_HEIGHT + 2;
			final int barHOffset = left + Math.max(0, ((panel.width - barWidth) / 2));
			final int barVOffset = top + panel.upperSpace + GUI_INV_CELL_LENGTH + panel.margin + PANEL_BUTTON_HEIGHT + panel.margin;
			
			criteriaField.setWidth(barWidth);
			criteriaField.setHeight(barHeight);
			criteriaField.setX(barHOffset);
			criteriaField.y = barVOffset;
			parent.addButton(criteriaField);
			
			final boolean logicMode = (panel.comp.getLogicMode() == LogicMode.LOGIC);
			opButton.visible = logicMode;
			panel.templateSlot.hide(!logicMode);
			criteriaField.setVisible(logicMode);
		}
		
		public void draw(MatrixStack matrixStackIn, Minecraft mc, int guiLeft, int guiTop) {
			final int left = guiLeft + panel.x;
			final int top = guiTop + panel.y;
			//final boolean logicMode = (panel.comp.getLogicMode() == LogicMode.LOGIC);
			
			if (this.drawBackground) {
				mc.getTextureManager().bindTexture(TEXT);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, left, top, 0, 0,
						GUI_PANEL_TEXT_WIDTH, GUI_PANEL_TEXT_HEIGHT, panel.width, panel.height, 256, 256,
						colorRed, colorGreen, colorBlue, colorAlpha);
			}
			
			// Vertical offset and arrangement of stuff depends on whether we allow logic or not
			matrixStackIn.push();
			//GlStateManager.translate(guiLeft + panel.x - (GUI_INV_CELL_LENGTH / 2) - 1, guiTop + panel.y - 1, 0);
			matrixStackIn.translate(guiLeft + panel.templateSlot.xPos - 1, guiTop + panel.templateSlot.yPos - 1, 0);
			drawSlot(matrixStackIn, mc);
			matrixStackIn.pop();
		}
		
		private void drawSlot(MatrixStack matrixStackIn, Minecraft mc) {
			mc.getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, GUI_SLOT_TEXT_HOFFSET, GUI_SLOT_TEXT_VOFFSET, PANEL_SLOT_WIDTH, PANEL_SLOT_HEIGHT, PANEL_SLOT_WIDTH, PANEL_SLOT_HEIGHT, 256, 256,
					colorRed, colorGreen, colorBlue, colorAlpha);
		}
		
//		private void drawInputBar(Minecraft mc) {
//			final int barWidth = Math.min(panel.width - 12, 100);
//			final int centerX = (panel.width / 2);
//			RenderFuncs.drawRect(-1 + centerX - (barWidth / 2), -1, 1 + centerX + (barWidth / 2), mc.fontRenderer.FONT_HEIGHT + 3, 0xFF444444);
//			RenderFuncs.drawRect(centerX - (barWidth / 2), 0, centerX + (barWidth / 2), mc.fontRenderer.FONT_HEIGHT + 2, 0xFF000000);
//			
//			final int width = mc.fontRenderer.getStringWidth(criteriaString);
//			mc.fontRenderer.drawString(criteriaString, (panel.width - width) / 2, 2, 0xFFFFFFFF);
//			
//			if (editSelected) {
//				final long period = 600; // .5 seconds
//				if ((System.currentTimeMillis() % (2 * period)) / period == 1) {
//					final int x = ((panel.width + width) / 2) + 1;
//					//Gui.drawRect(x, 1, x + 1, this.font.FONT_HEIGHT, 0xFFFFFFFF);
//					mc.fontRenderer.drawString("_", x, 2, 0xFFFFFFFF);
//				}
//			}
//		}
		
		private void drawCriteriaMode(MatrixStack matrixStackIn, Minecraft mc, LogicMode mode) {
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
			
			mc.getTextureManager().bindTexture(TEXT);
			RenderSystem.enableBlend();
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 1, 1,
					textX, GUI_MODE_ICON_TEXT_VOFFSET,
					PANEL_MODE_WIDTH, PANEL_MODE_HEIGHT,
					PANEL_BUTTON_WIDTH - 2, PANEL_BUTTON_HEIGHT - 2,
					256, 256);
			RenderSystem.disableBlend();
		}
		
		private void drawCriteriaOp(MatrixStack matrixStackIn, FontRenderer fonter, LogicOp op) {
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
			fonter.drawString(matrixStackIn, s, (PANEL_BUTTON_WIDTH + -sWidth) / 2, 1 + (PANEL_BUTTON_HEIGHT - fonter.FONT_HEIGHT) / 2, 0xFFFFFFFF);
		}
		
		protected boolean mouseClicked(int mouseX, int mouseY, int mouseButton, int guiLeft, int guiTop) throws IOException {
			final Minecraft mc = parent.getMinecraft();
			final int barWidth = Math.min(panel.width - 4, 100);
			final int barVOffset = (panel.upperSpace + GUI_INV_CELL_LENGTH + panel.margin + PANEL_BUTTON_HEIGHT + panel.margin);
			final int xHalf = (panel.width / 2);
			final int minX = guiLeft + panel.x + xHalf - (barWidth / 2);
			final int maxX = guiLeft + panel.x + xHalf + (barWidth / 2);
			final int minY = guiTop + panel.y + barVOffset;
			final int maxY = guiTop + panel.y + barVOffset + mc.fontRenderer.FONT_HEIGHT + 2;
			
			if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY) {
				//editSelected = true;
				return true;
			}
			
			//editSelected = false;
			return false;
		}
		
		protected class ModeButton extends Button {

			private boolean pressed;
			
			public ModeButton(int x, int y, LogicPanelGui<?> gui) {
				super(x, y, PANEL_BUTTON_WIDTH, PANEL_BUTTON_HEIGHT, StringTextComponent.EMPTY, (b) -> {
					LogicMode mode = gui.panel.comp.getLogicMode();
					// Cycle up modes
					mode = (LogicMode.values()[(mode.ordinal() + 1) % LogicMode.values().length]);
					
					gui.panel.setMode(mode);
					
					// Also refresh hidden buttons
					final boolean logicMode = (mode == LogicMode.LOGIC);
					gui.opButton.visible = logicMode;
					gui.panel.templateSlot.hide(!logicMode);
					gui.criteriaField.setVisible(logicMode);
				});
				pressed = false;
			}
			
			@Override
			public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final Minecraft mc = Minecraft.getInstance();
				this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				
				int textX = GUI_BUTTON_TEXT_HOFFSET;
				if (pressed) {
					textX += PANEL_BUTTON_WIDTH * 2;
				} else if (isHovered) {
					textX += PANEL_BUTTON_WIDTH;
				}
				
				mc.getTextureManager().bindTexture(TEXT);
				RenderSystem.enableBlend();
				matrixStackIn.push();
				matrixStackIn.translate(x, y, 0);
				RenderFuncs.blit(matrixStackIn, 0, 0,
						textX, GUI_BUTTON_TEXT_VOFFSET,
						PANEL_BUTTON_WIDTH, PANEL_BUTTON_HEIGHT,
						colorRed, colorGreen, colorBlue, colorAlpha);
				
				// Then draw mode
				LogicMode mode = panel.comp.getLogicMode();
				drawCriteriaMode(matrixStackIn, mc, mode);
				matrixStackIn.pop();
				RenderSystem.disableBlend();
			}
			
			@Override
			public void onRelease(double mouseX, double mouseY) {
				pressed = false;
				super.onRelease(mouseX, mouseY);
			}
			
			@Override
			public void onClick(double mouseX, double mouseY) {
				pressed = true;
				super.onClick(mouseX, mouseY);
			}
			
		}
		
		protected class OpButton extends Button {

			private boolean pressed;
			
			public OpButton(int x, int y, LogicPanelGui<?> gui) {
				super(x, y, PANEL_BUTTON_WIDTH, PANEL_BUTTON_HEIGHT, StringTextComponent.EMPTY, (b) -> {
					LogicOp op = gui.panel.comp.getLogicOp();
					// Cycle up modes
					op = (LogicOp.values()[(op.ordinal() + 1) % LogicOp.values().length]);
					
					gui.panel.setOp(op);
				});
				pressed = false;
			}
			
			@Override
			public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				if (!this.visible) {
					return;
				}

				final Minecraft mc = Minecraft.getInstance();
				this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				
				int textX = GUI_BUTTON_TEXT_HOFFSET;
				if (pressed) {
					textX += PANEL_BUTTON_WIDTH * 2;
				} else if (isHovered) {
					textX += PANEL_BUTTON_WIDTH;
				}
				
				mc.getTextureManager().bindTexture(TEXT);
				RenderSystem.enableBlend();
				matrixStackIn.push();
				matrixStackIn.translate(x, y, 0);
				RenderFuncs.blit(matrixStackIn, 0, 0,
						textX, GUI_BUTTON_TEXT_VOFFSET,
						PANEL_BUTTON_WIDTH, PANEL_BUTTON_HEIGHT,
						colorRed, colorGreen, colorBlue, colorAlpha);
				
				// Then draw mode
				drawCriteriaOp(matrixStackIn, mc.fontRenderer, panel.comp.getLogicOp());
				matrixStackIn.pop();
				RenderSystem.disableBlend();
			}
			
			@Override
			public void onRelease(double mouseX, double mouseY) {
				pressed = false;
				super.onRelease(mouseX, mouseY);
			}
			
			@Override
			public void onClick(double mouseX, double mouseY) {
				pressed = true;
				super.onClick(mouseX, mouseY);
			}
			
		}
		
	}
}
