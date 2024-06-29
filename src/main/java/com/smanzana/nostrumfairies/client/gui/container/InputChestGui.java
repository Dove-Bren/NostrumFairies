package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.tiles.InputChestTileEntity;
import com.smanzana.nostrummagica.client.gui.container.AutoGuiContainer;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InputChestGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/input_chest.png");
	private static final int GUI_TEXT_WIDTH = 176;
	private static final int GUI_TEXT_HEIGHT = 168;//222; //54
	private static final int GUI_TOP_INV_HOFFSET = 8;
	private static final int GUI_TOP_INV_VOFFSET = 18;
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 86;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 144;
	//private static final int GUI_INV_CELL_LENGTH = 18;
	//private static final int GUI_INV_CELL_SPACING = 2;

	public static class InputChestContainer extends Container {
		
		public static final String ID = "input_chest";
		
		protected InputChestTileEntity chest;
		//private int chestIDStart;
		
		public InputChestContainer(int windowId, PlayerInventory playerInv, InputChestTileEntity chest) {
			super(FairyContainers.InputChest, windowId);
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
			
			//chestIDStart = this.inventorySlots.size();
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 9; j++) {
					
					this.addSlot(new Slot(chest, i * 9 + j, GUI_TOP_INV_HOFFSET + j * 18, GUI_TOP_INV_VOFFSET + i * 18) {
						public boolean isItemValid(@Nonnull ItemStack stack) {
					        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
					    }
					});
				}
			}
		}
		
		public static InputChestContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			return new InputChestContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(InputChestTileEntity chest) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new InputChestContainer(windowId, playerInv, chest);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, chest);
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
		public boolean canDragIntoSlot(Slot slotIn) {
			return true;
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class InputChestGuiContainer extends AutoGuiContainer<InputChestContainer> {

		//private TestChestContainer container;
		
		public InputChestGuiContainer(InputChestContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			//this.container = container;
			
			this.xSize = GUI_TEXT_WIDTH;
			this.ySize = GUI_TEXT_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
		
	}
	
}
