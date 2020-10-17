package com.smanzana.nostrumfairies.client.gui.container;

import java.io.IOException;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.LogisticsSensorBlock.LogisticsSensorTileEntity;
import com.smanzana.nostrumfairies.blocks.LogisticsSensorBlock.LogisticsSensorTileEntity.SensorLogicOp;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsSensorActionMessage;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 3x3 regular sized crafting sensor
 * @author Skyler
 *
 */
public class LogisticsSensorGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/logistics_sensor.png");
	private static final int GUI_TEXT_MAIN_WIDTH = 176;
	private static final int GUI_TEXT_MAIN_HEIGHT = 168;
	
	private static final int GUI_INV_CELL_LENGTH = 18;
	private static final int GUI_TOP_INV_HOFFSET = 80;
	private static final int GUI_TOP_INV_VOFFSET = 20;
	
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 86;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 144;
	
	private static final int GUI_BUTTON_HOFFSET = 0;
	private static final int GUI_BUTTON_VOFFSET = 168;
	private static final int GUI_BUTTON_LENGTH = 18;
	
	public static class LogisticsSensorContainer extends Container {
		
		protected LogisticsSensorTileEntity sensor;
		protected Slot templateSlot;
		protected ItemStack[] invArray;
		protected IInventory inv;
		
		public LogisticsSensorContainer(IInventory playerInv, LogisticsSensorTileEntity sensor) {
			this.sensor = sensor;
			invArray = new ItemStack[1];
			
			invArray[0] = sensor.getLogicTemplate();
			
			inv = new Inventories.ItemStackArrayWrapper(invArray);
						
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlotToContainer(new Slot(playerInv, x + y * 9 + 9, GUI_PLAYER_INV_HOFFSET + (x * 18), GUI_PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlotToContainer(new Slot(playerInv, x, GUI_HOTBAR_INV_HOFFSET + x * 18, GUI_HOTBAR_INV_VOFFSET));
			}
			
			// Sometimes-hidden criteria slot
			this.templateSlot = new Slot(inv, 0, GUI_TOP_INV_HOFFSET, GUI_TOP_INV_VOFFSET);
			this.addSlotToContainer(templateSlot);
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			ItemStack prev = null;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				if (slot.inventory == this.inv) {
					;
				} else {
					// shift-click in player inventory. Just disallow.
					prev = null;
				}
			}
			
			return prev;
		}
		
		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
			if (player.inventory.getItemStack() == null) {
				// empty hand. Right-click?
				if (dragType == 1 && clickTypeIn == ClickType.PICKUP) {
					// Criteria slot?
					if (slotId == templateSlot.slotNumber) {
						setTemplate(null);
						return null;
					}
				}
			} else {
				// Item in hand. Clicking empty templatable slot?
				if (clickTypeIn == ClickType.PICKUP) {
					// Input slot?
					// Criteria slot?
					if (slotId == templateSlot.slotNumber && !templateSlot.getHasStack()) {
						ItemStack template = player.inventory.getItemStack().copy();
						template.stackSize = 1;
						setTemplate(template);
						return null;
					}
				}
			}
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn != templateSlot;
		}
		
		protected void setTemplate(@Nullable ItemStack template) {
			NetworkHandler.getSyncChannel().sendToServer(new LogisticsSensorActionMessage(this.sensor, template));
			invArray[0] = template;
		}
		
		protected void setOp(SensorLogicOp op) {
			NetworkHandler.getSyncChannel().sendToServer(new LogisticsSensorActionMessage(this.sensor, op));
		}
		
		protected void setCount(int count) {
			NetworkHandler.getSyncChannel().sendToServer(new LogisticsSensorActionMessage(this.sensor, count));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class LogisticsSensorGuiContainer extends GuiContainer {

		private LogisticsSensorContainer container;
		private OpButton opButton;
		
		private String criteriaString;
		private boolean editSelected;
		
		public LogisticsSensorGuiContainer(LogisticsSensorContainer container) {
			super(container);
			this.container = container;
			
			this.xSize = GUI_TEXT_MAIN_WIDTH;
			this.ySize = GUI_TEXT_MAIN_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
			
			opButton = new OpButton(1, guiLeft + (GUI_TEXT_MAIN_WIDTH - GUI_INV_CELL_LENGTH) / 2, guiTop + 42);
			this.addButton(opButton);
			
			editSelected = false;
			criteriaString = String.format("%d", container.sensor.getLogicCount());
		}
		
		private void drawCriteriaOp(SensorLogicOp op) {
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
			
			final int sWidth = this.fontRendererObj.getStringWidth(s);
			this.fontRendererObj.drawString(s, (GUI_BUTTON_LENGTH + -sWidth) / 2, 1 + (GUI_BUTTON_LENGTH - fontRendererObj.FONT_HEIGHT) / 2, 0xFFFFFFFF);
		}
		
		private void drawInputBar() {
			final int barWidth = 100;
			final int centerX = (GUI_TEXT_MAIN_WIDTH / 2);
			Gui.drawRect(-1 + centerX - (barWidth / 2), -1, 1 + centerX + (barWidth / 2), this.fontRendererObj.FONT_HEIGHT + 3, 0xFF444444);
			Gui.drawRect(centerX - (barWidth / 2), 0, centerX + (barWidth / 2), this.fontRendererObj.FONT_HEIGHT + 2, 0xFF000000);
			
			final int width = fontRendererObj.getStringWidth(criteriaString);
			fontRendererObj.drawString(criteriaString, (GUI_TEXT_MAIN_WIDTH - width) / 2, 2, 0xFFFFFFFF);
			
			if (editSelected) {
				final long period = 600; // .5 seconds
				if ((System.currentTimeMillis() % (2 * period)) / period == 1) { 
					final int x = ((GUI_TEXT_MAIN_WIDTH + width) / 2) + 1;
					//Gui.drawRect(x, 1, x + 1, this.fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
					fontRendererObj.drawString("_", x, 2, 0xFFFFFFFF);
				}
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = this.guiLeft;
			int verticalMargin = this.guiTop;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_TEXT_MAIN_WIDTH, GUI_TEXT_MAIN_HEIGHT, 256, 256);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop + 65, 0);
			drawInputBar();
			GlStateManager.popMatrix();

			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			;
		}
		
		@Override
		public void actionPerformed(GuiButton button) {
			if (button instanceof OpButton) {
				SensorLogicOp mode = container.sensor.getLogicOp();
				// Cycle up modes
				mode = (SensorLogicOp.values()[(mode.ordinal() + 1) % SensorLogicOp.values().length]);
				
				container.setOp(mode);
			}
		}
		
		@Override
		protected void keyTyped(char typedChar, int keyCode) throws IOException {
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
					container.setCount(val);
					return;
				} else if (keyCode == 28) { // ENTER/return
					this.editSelected = false;
					return;
				} 
			}
//			else if (editSelected && keyCode == 1) {
//				editSelected = false;
//				return;
//			}
			
			super.keyTyped(typedChar, keyCode);
		}
		
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			final int barWidth = 100;
			final int minX = guiLeft + (GUI_TEXT_MAIN_WIDTH - barWidth) / 2;
			final int maxX = guiLeft + (GUI_TEXT_MAIN_WIDTH + barWidth) / 2;
			final int minY = guiTop + 70;
			final int maxY = guiTop + 70 + fontRendererObj.FONT_HEIGHT + 2;
			
			if (mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY) {
				editSelected = true;
				return;
			}
			
			editSelected = false;
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		protected class OpButton extends GuiButton {

			private boolean pressed;
			
			public OpButton(int buttonId, int x, int y) {
				super(buttonId, x, y, GUI_BUTTON_LENGTH, GUI_BUTTON_LENGTH, "");
				pressed = false;
			}
			
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY) {
				if (!this.visible) {
					return;
				}
				
				this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				
				int textX = GUI_BUTTON_HOFFSET;
				if (pressed) {
					textX += GUI_BUTTON_LENGTH * 2;
				} else if (hovered) {
					textX += GUI_BUTTON_LENGTH;
				}
				
				GlStateManager.color(1.0F,  1.0F, 1.0F, 1f);
				mc.getTextureManager().bindTexture(TEXT);
				GlStateManager.enableBlend();
				GlStateManager.pushMatrix();
				GlStateManager.translate(xPosition, yPosition, 0);
				this.drawTexturedModalRect(0, 0,
						textX, GUI_BUTTON_VOFFSET,
						GUI_BUTTON_LENGTH, GUI_BUTTON_LENGTH);
				
				// Then draw mode
				drawCriteriaOp(container.sensor.getLogicOp());
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
