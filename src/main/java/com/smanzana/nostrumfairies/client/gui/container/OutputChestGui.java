package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsChest.OutputChestTileEntity;
import com.smanzana.nostrumfairies.utils.ItemStacks;

import net.minecraft.client.gui.Gui;
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

public class OutputChestGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/output_chest.png");
	private static final int GUI_TEXT_WIDTH = 176;
	private static final int GUI_TEXT_HEIGHT = 132;
	private static final int GUI_INV_CELL_LENGTH = 18;
	private static final int GUI_TEXT_MISSING_ICON_HOFFSET = GUI_TEXT_WIDTH;
	private static final int GUI_TEXT_WORKING_ICON_HOFFSET = GUI_TEXT_WIDTH + GUI_INV_CELL_LENGTH;
	private static final int GUI_TOP_INV_HOFFSET = 62;
	private static final int GUI_TOP_INV_VOFFSET = 18;
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 50;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 108;

	public static class OutputChestContainer extends Container {
		
		protected OutputChestTileEntity chest;
		private int chestIDStart;
		
		public OutputChestContainer(IInventory playerInv, OutputChestTileEntity chest) {
			this.chest = chest;
						
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
			
			chestIDStart = this.inventorySlots.size();
			for (int i = 0; i < chest.getSizeInventory(); i++) {
				final int index = i;
				this.addSlotToContainer(new Slot(chest, i, GUI_TOP_INV_HOFFSET + i * 18, GUI_TOP_INV_VOFFSET) {
					@Override
					public boolean isItemValid(@Nullable ItemStack stack) {
				        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
				    }
					
					@Override
					public int getSlotStackLimit() {
						ItemStack template = chest.getTemplate(index);
						if (template == null) {
							return super.getSlotStackLimit();
						} else {
							return template.stackSize;
						}
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
				
				if (slot.inventory == this.chest) {
					// Trying to take one of our items
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						slot.putStack(null);
						slot.onPickupFromSlot(playerIn, cur);
					} else {
						prev = null;
					}
				} else {
					// shift-click in player inventory
					ItemStack leftover = ItemStacks.addItem(chest, cur);
					slot.putStack(leftover != null && leftover.stackSize <= 0 ? null : leftover);
					if (leftover != null && leftover.stackSize == prev.stackSize) {
						prev = null;
					}
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
				if (slotId >= chestIDStart && dragType == 1 && clickTypeIn == ClickType.PICKUP && chest.getStackInSlot(slotId - chestIDStart) == null) {
					chest.setTemplate(slotId - chestIDStart, null);
					return null;
				}
			} else {
				// Item in hand. Clicking empty output slot?
				if (slotId >= chestIDStart && clickTypeIn == ClickType.PICKUP && chest.getTemplate(slotId - chestIDStart) == null) {
					ItemStack template = player.inventory.getItemStack();
					if (dragType == 1) { // right click
						template = template.copy();
						template.stackSize = 1;
					}
					chest.setTemplate(slotId - chestIDStart, template);
					return null;
				}
//				System.out.println("Clicktype: " + clickTypeIn);
//				System.out.println("dragType: " + dragType);
			}
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.slotNumber < chestIDStart;
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	public static class OutputChestGuiContainer extends GuiContainer {

		private OutputChestContainer container;
		
		public OutputChestGuiContainer(OutputChestContainer container) {
			super(container);
			this.container = container;
			
			this.xSize = GUI_TEXT_WIDTH;
			this.ySize = GUI_TEXT_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
		}
		
		private void drawStatus(float partialTicks, boolean available) {
			float alpha = (float) (.5f + (.25f * Math.sin(Math.PI * (double)(System.currentTimeMillis() % 1000) / 1000.0)));
			GlStateManager.color(1.0F,  1.0F, 1.0F, alpha);
			mc.getTextureManager().bindTexture(TEXT);
			
			final int text_hoffset = (available ? GUI_TEXT_WORKING_ICON_HOFFSET : GUI_TEXT_MISSING_ICON_HOFFSET);
			final int text_voffset = 0;
			GlStateManager.enableBlend();
			this.drawTexturedModalRect(0, 0, text_hoffset, text_voffset, GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH);
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
			for (int i = 0; i < container.chest.getSizeInventory(); i++) {
				ItemStack template = container.chest.getTemplate(i);
				ItemStack stack = container.chest.getStackInSlot(i);
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(horizontalMargin + GUI_TOP_INV_HOFFSET + (i * GUI_INV_CELL_LENGTH),
						verticalMargin + GUI_TOP_INV_VOFFSET,
						0);
				
				if (container.chest.getStackInSlot(i) == null) {
					GlStateManager.pushMatrix();
					GlStateManager.scale(1f, 1f, .05f);
					drawTemplate(partialTicks, container.chest.getTemplate(i));
					GlStateManager.popMatrix();
				}
				
				if (template != null && (stack == null || stack.stackSize < template.stackSize)) {
					GlStateManager.translate(0, 0, 100);
					drawStatus(partialTicks, true);
				}
				
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