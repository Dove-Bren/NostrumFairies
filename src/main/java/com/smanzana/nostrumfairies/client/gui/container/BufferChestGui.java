package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.tiles.BufferChestTileEntity;
import com.smanzana.nostrummagica.client.gui.container.AutoGuiContainer;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
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

	public static class BufferChestContainer extends AbstractContainerMenu {
		
		public static final String ID = "buffer_chest";
		
		protected BufferChestTileEntity chest;
		private int chestIDStart;
		
		public BufferChestContainer(int windowId, Inventory playerInv, BufferChestTileEntity chest) {
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
			
			chestIDStart = this.slots.size();
			for (int i = 0; i < chest.getContainerSize(); i++) {
				final int index = i;
				this.addSlot(new Slot(chest, i, GUI_TOP_INV_HOFFSET + i * 18, GUI_TOP_INV_VOFFSET) {
					@Override
					public boolean mayPlace(@Nonnull ItemStack stack) {
				        return this.container.canPlaceItem(this.getSlotIndex(), stack);
				    }
					
					@Override
					public int getMaxStackSize() {
						ItemStack template = chest.getTemplate(index);
						if (template.isEmpty()) {
							return super.getMaxStackSize();
						} else {
							return template.getCount();
						}
					}
					
					@Override
					public void set(@Nonnull ItemStack stack) {
//						ItemStack template = chest.getTemplate(index);
//						if (template.isEmpty()) {
//							chest.setTemplate(index, stack);
//						} else {
							super.set(stack);
//						}
					}
				});
			}
		}
		
		public static BufferChestContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
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
		public ItemStack quickMoveStack(Player playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.slots.get(fromSlot);
			
			if (slot != null && slot.hasItem()) {
				ItemStack cur = slot.getItem();
				prev = cur.copy();
				
				if (slot.container == this.chest) {
					// Trying to take one of our items
					if (playerIn.inventory.add(cur)) {
						slot.set(ItemStack.EMPTY);
						slot.onTake(playerIn, cur);
					} else {
						prev = ItemStack.EMPTY;
					}
				} else {
					// shift-click in player inventory
					ItemStack leftover = Inventories.addItem(chest, cur);
					slot.set(leftover.isEmpty() ? ItemStack.EMPTY : leftover);
					if (!leftover.isEmpty() && leftover.getCount() == prev.getCount()) {
						prev = ItemStack.EMPTY;
					}
				}
				
			}
			
			return prev;
		}
		
		@Override
		public boolean stillValid(Player playerIn) {
			return true;
		}
		
		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
			if (player.inventory.getCarried().isEmpty()) {
				// empty hand. Right-click?
				if (slotId >= chestIDStart && dragType == 1 && clickTypeIn == ClickType.PICKUP && chest.getItem(slotId - chestIDStart).isEmpty()) {
					chest.setTemplate(slotId - chestIDStart, ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			} else {
				// Item in hand. Clicking empty output slot?
				if (slotId >= chestIDStart && clickTypeIn == ClickType.PICKUP && chest.getTemplate(slotId - chestIDStart).isEmpty()) {
					ItemStack template = player.inventory.getCarried();
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
			return super.clicked(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return slotIn.index < chestIDStart;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class BufferChestGuiContainer extends AutoGuiContainer<BufferChestContainer> {

		private BufferChestContainer container;
		
		public BufferChestGuiContainer(BufferChestContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			this.container = container;
			
			this.imageWidth = GUI_TEXT_WIDTH;
			this.imageHeight = GUI_TEXT_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
		}
		
		private void drawStatus(PoseStack matrixStackIn, float partialTicks, boolean available) {
			float alpha = (float) (.5f + (.25f * Math.sin(Math.PI * (double)(System.currentTimeMillis() % 1000) / 1000.0)));
			mc.getTextureManager().bind(TEXT);
			
			final int text_hoffset = (available ? GUI_TEXT_WORKING_ICON_HOFFSET : GUI_TEXT_MISSING_ICON_HOFFSET);
			final int text_voffset = 0;
			RenderSystem.enableBlend();
			RenderFuncs.blit(matrixStackIn, 0, 0, text_hoffset, text_voffset, GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH, 1f, 1f, 1f, alpha);
			RenderSystem.disableBlend();
		}
		
		private void drawTemplate(PoseStack matrixStackIn, float partialTicks, @Nonnull ItemStack template) {
			if (!template.isEmpty()) {
				matrixStackIn.pushPose();
				{
					RenderSystem.pushMatrix();
					RenderSystem.multMatrix(matrixStackIn.last().pose());
					Minecraft.getInstance().getItemRenderer().renderGuiItem(template, 0, 0);
					RenderSystem.popMatrix();
				}
				matrixStackIn.translate(0, 0, 110);
				if (template.getCount() > 1) {
					final String count = "" + template.getCount();
					
					this.font.drawShadow(matrixStackIn, "" + template.getCount(),
							GUI_INV_CELL_LENGTH - (this.font.width(count) + 1),
							GUI_INV_CELL_LENGTH - (this.font.lineHeight),
							0xFFFFFFFF);
				}
//				else {
//					GlStateManager.enableAlphaTest();
//				}
				RenderSystem.enableBlend();
				RenderFuncs.drawRect(matrixStackIn, 0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA0636259);
				RenderSystem.disableBlend();
				matrixStackIn.popPose();
			}
		}
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
			mc.getTextureManager().bind(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			// Draw templates, if needed
			for (int i = 0; i < container.chest.getContainerSize(); i++) {
				ItemStack template = container.chest.getTemplate(i);
				ItemStack stack = container.chest.getItem(i);
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(horizontalMargin + GUI_TOP_INV_HOFFSET + (i * GUI_INV_CELL_LENGTH),
						verticalMargin + GUI_TOP_INV_VOFFSET,
						0);
				
				if (container.chest.getItem(i).isEmpty()) {
					matrixStackIn.pushPose();
					matrixStackIn.scale(1f, 1f, .05f);
					drawTemplate(matrixStackIn, partialTicks, container.chest.getTemplate(i));
					matrixStackIn.popPose();
				}
				
				if (!template.isEmpty() && (stack.isEmpty() || stack.getCount() < template.getCount())) {
					matrixStackIn.translate(0, 0, 100);
					drawStatus(matrixStackIn, partialTicks, true);
				}
				
				matrixStackIn.popPose();
			}
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			;
		}
		
	}
	
}
