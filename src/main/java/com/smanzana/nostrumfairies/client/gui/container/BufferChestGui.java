package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.tiles.BufferChestTileEntity;
import com.smanzana.nostrummagica.client.gui.container.AutoGuiContainer;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BufferChestGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/buffer_chest.png");
	private static final int GUI_TEXT_WIDTH = 176;
	private static final int GUI_TEXT_HEIGHT = 132;
	private static final int GUI_INV_CELL_LENGTH = 18;
	private static final int GUI_TEXT_MISSING_ICON_HOFFSET = GUI_TEXT_WIDTH;
	private static final int GUI_TEXT_WORKING_ICON_HOFFSET = GUI_TEXT_WIDTH + GUI_INV_CELL_LENGTH;
	private static final int GUI_TOP_INV_HOFFSET = 8;
	private static final int GUI_TOP_INV_VOFFSET = 18;
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 50;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 108;

	public static class BufferChestContainer extends Container {
		
		public static final String ID = "buffer_chest";
		
		protected BufferChestTileEntity chest;
		private int chestIDStart;
		
		public BufferChestContainer(int windowId, PlayerInventory playerInv, BufferChestTileEntity chest) {
			super(FairyContainers.BufferChest, windowId);
			this.chest = chest;
						
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, GUI_PLAYER_INV_HOFFSET + (x * 18), GUI_PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, GUI_HOTBAR_INV_HOFFSET + x * 18, GUI_HOTBAR_INV_VOFFSET));
			}
			
			chestIDStart = this.inventorySlots.size();
			for (int i = 0; i < chest.getSizeInventory(); i++) {
				final int index = i;
				this.addSlot(new Slot(chest, i, GUI_TOP_INV_HOFFSET + i * 18, GUI_TOP_INV_VOFFSET) {
					@Override
					public boolean isItemValid(@Nonnull ItemStack stack) {
				        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
				    }
					
					@Override
					public int getSlotStackLimit() {
						ItemStack template = chest.getTemplate(index);
						if (template.isEmpty()) {
							return super.getSlotStackLimit();
						} else {
							return template.getCount();
						}
					}
					
					@Override
					public void putStack(@Nonnull ItemStack stack) {
//						ItemStack template = chest.getTemplate(index);
//						if (template.isEmpty()) {
//							chest.setTemplate(index, stack);
//						} else {
							super.putStack(stack);
//						}
					}
				});
			}
		}
		
		public static BufferChestContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			return new BufferChestContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(BufferChestTileEntity hopper) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new BufferChestContainer(windowId, playerInv, hopper);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, hopper);
			});
		}
		
		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				if (slot.inventory == this.chest) {
					// Trying to take one of our items
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						slot.putStack(ItemStack.EMPTY);
						slot.onTake(playerIn, cur);
					} else {
						prev = ItemStack.EMPTY;
					}
				} else {
					// shift-click in player inventory
					ItemStack leftover = Inventories.addItem(chest, cur);
					slot.putStack(leftover.isEmpty() ? ItemStack.EMPTY : leftover);
					if (!leftover.isEmpty() && leftover.getCount() == prev.getCount()) {
						prev = ItemStack.EMPTY;
					}
				}
				
			}
			
			return prev;
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (player.inventory.getItemStack().isEmpty()) {
				// empty hand. Right-click?
				if (slotId >= chestIDStart && dragType == 1 && clickTypeIn == ClickType.PICKUP && chest.getStackInSlot(slotId - chestIDStart).isEmpty()) {
					chest.setTemplate(slotId - chestIDStart, ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			} else {
				// Item in hand. Clicking empty output slot?
				if (slotId >= chestIDStart && clickTypeIn == ClickType.PICKUP && chest.getTemplate(slotId - chestIDStart).isEmpty()) {
					ItemStack template = player.inventory.getItemStack();
					if (dragType == 1) { // right click
						template = template.copy();
						template.setCount(1);
					}
					chest.setTemplate(slotId - chestIDStart, template);
					return ItemStack.EMPTY;
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
	
	@OnlyIn(Dist.CLIENT)
	public static class BufferChestGuiContainer extends AutoGuiContainer<BufferChestContainer> {

		private BufferChestContainer container;
		
		public BufferChestGuiContainer(BufferChestContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			
			this.xSize = GUI_TEXT_WIDTH;
			this.ySize = GUI_TEXT_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
		}
		
		private void drawStatus(float partialTicks, boolean available) {
			float alpha = (float) (.5f + (.25f * Math.sin(Math.PI * (double)(System.currentTimeMillis() % 1000) / 1000.0)));
			GlStateManager.color4f(1.0F,  1.0F, 1.0F, alpha);
			mc.getTextureManager().bindTexture(TEXT);
			
			final int text_hoffset = (available ? GUI_TEXT_WORKING_ICON_HOFFSET : GUI_TEXT_MISSING_ICON_HOFFSET);
			final int text_voffset = 0;
			GlStateManager.enableBlend();
			this.blit(0, 0, text_hoffset, text_voffset, GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH);
		}
		
		private void drawTemplate(float partialTicks, @Nonnull ItemStack template) {
			if (!template.isEmpty()) {
				GlStateManager.pushMatrix();
				Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(template, 0, 0);
				GlStateManager.translated(0, 0, 110);
				if (template.getCount() > 1) {
					final String count = "" + template.getCount();
					
					this.font.drawStringWithShadow("" + template.getCount(),
							GUI_INV_CELL_LENGTH - (this.font.getStringWidth(count) + 1),
							GUI_INV_CELL_LENGTH - (this.font.FONT_HEIGHT),
							0xFFFFFFFF);
				} else {
					GlStateManager.enableAlphaTest();
				}
				RenderFuncs.drawRect(0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA0636259);
				GlStateManager.popMatrix();
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			GlStateManager.color4f(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			// Draw templates, if needed
			for (int i = 0; i < container.chest.getSizeInventory(); i++) {
				ItemStack template = container.chest.getTemplate(i);
				ItemStack stack = container.chest.getStackInSlot(i);
				
				GlStateManager.pushMatrix();
				GlStateManager.translated(horizontalMargin + GUI_TOP_INV_HOFFSET + (i * GUI_INV_CELL_LENGTH),
						verticalMargin + GUI_TOP_INV_VOFFSET,
						0);
				
				if (container.chest.getStackInSlot(i).isEmpty()) {
					GlStateManager.pushMatrix();
					GlStateManager.scalef(1f, 1f, .05f);
					drawTemplate(partialTicks, container.chest.getTemplate(i));
					GlStateManager.popMatrix();
				}
				
				if (!template.isEmpty() && (stack.isEmpty() || stack.getCount() < template.getCount())) {
					GlStateManager.translated(0, 0, 100);
					drawStatus(partialTicks, true);
				}
				
				GlStateManager.popMatrix();
			}

			GlStateManager.enableBlend();
			GlStateManager.enableAlphaTest();
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			;
		}
		
	}
	
}
