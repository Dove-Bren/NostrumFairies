package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsPanel.OutputPanelTileEntity;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OutputPanelGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/output_chest.png");
	private static final int GUI_TEXT_WIDTH = 176;
	private static final int GUI_TEXT_HEIGHT = 132;
	private static final int GUI_INV_CELL_LENGTH = 18;
//	private static final int GUI_TEXT_MISSING_ICON_HOFFSET = GUI_TEXT_WIDTH;
//	private static final int GUI_TEXT_WORKING_ICON_HOFFSET = GUI_TEXT_WIDTH + GUI_INV_CELL_LENGTH;
	private static final int GUI_TOP_INV_HOFFSET = 62;
	private static final int GUI_TOP_INV_VOFFSET = 18;
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 50;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 108;
	
	public static class OutputPanelContainer extends Container {
		
		private IInventory slots = new InventoryBasic("Output Panel", false, 3);
		protected OutputPanelTileEntity panel;
		private int panelIDStart;
		
		public OutputPanelContainer(IInventory playerInv, OutputPanelTileEntity panel) {
			this.panel = panel;
						
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
			
			panelIDStart = this.inventorySlots.size();
			for (int i = 0; i < slots.getSizeInventory(); i++) {
				this.addSlotToContainer(new Slot(slots, i, GUI_TOP_INV_HOFFSET + i * 18, GUI_TOP_INV_VOFFSET) {
					@Override
					public boolean isItemValid(@Nullable ItemStack stack) {
				        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
				    }
					
					@Override
					public int getSlotStackLimit() {
						return 1;
					}
					
					@Override
					public void putStack(@Nullable ItemStack stack) {
//						ItemStack template = chest.getTemplate(index);
//						if (template == null) {
//							chest.setTemplate(index, stack);
//						} else {
							super.putStack(stack);
//						}
					}
				});
			}
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			ItemStack prev = null;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				if (slot.inventory == this.slots) {
					// Clicked in our slots but ignore that
				} else {
					// shift-click in player inventory
					// Find available template slot
					
					// TODO
//					ItemStack leftover = Inventories.addItem(chest, cur);
//					slot.putStack(leftover != null && leftover.stackSize <= 0 ? null : leftover);
//					if (leftover != null && leftover.stackSize == prev.stackSize) {
//						prev = null;
//					}
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
				if (slotId >= panelIDStart && dragType == 1 && clickTypeIn == ClickType.PICKUP) {
					panel.setTemplate(slotId - panelIDStart, null);
					return null;
				}
			} else {
				// Item in hand. Clicking in template inventory?
				if (slotId >= panelIDStart) {
					// Clicking empty slot?
					if (clickTypeIn == ClickType.PICKUP && panel.getTemplate(slotId - panelIDStart) == null) {
						ItemStack template = player.inventory.getItemStack();
						if (dragType == 1) { // right click
							template = template.copy();
							template.stackSize = 1;
						}
						panel.setTemplate(slotId - panelIDStart, template);
					}
					return null;
				}
			}
			
			//return null;
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.slotNumber < panelIDStart;
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	public static class OutputPanelGuiContainer extends GuiContainer {

		private OutputPanelContainer container;
		
		public OutputPanelGuiContainer(OutputPanelContainer container) {
			super(container);
			this.container = container;
			
			this.xSize = GUI_TEXT_WIDTH;
			this.ySize = GUI_TEXT_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
		}
		
		private void drawTemplate(float partialTicks, @Nullable ItemStack template) {
			if (template != null) {
				GlStateManager.pushMatrix();
				this.itemRender.renderItemIntoGUI(template, 0, 0);
				GlStateManager.translate(0, 0, 110);
				if (template.stackSize > 1) {
					final String count = "" + template.stackSize;
					
					this.fontRendererObj.drawStringWithShadow("" + template.stackSize,
							GUI_INV_CELL_LENGTH - (this.fontRendererObj.getStringWidth(count) + 1),
							GUI_INV_CELL_LENGTH - (this.fontRendererObj.FONT_HEIGHT),
							0xFFFFFFFF);
				} else {
					GlStateManager.enableAlpha();
				}
				drawRect(0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA0636259);
				GlStateManager.popMatrix();
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			// Draw templates, if needed
			for (int i = 0; i < container.slots.getSizeInventory(); i++) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(horizontalMargin + GUI_TOP_INV_HOFFSET + (i * GUI_INV_CELL_LENGTH),
						verticalMargin + GUI_TOP_INV_VOFFSET,
						0);
				
				GlStateManager.pushMatrix();
				GlStateManager.scale(1f, 1f, .05f);
				drawTemplate(partialTicks, container.panel.getTemplate(i));
				GlStateManager.popMatrix();
				
				GlStateManager.popMatrix();
			}

			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			;
		}
		
	}
	
}
