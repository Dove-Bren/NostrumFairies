package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;
import com.smanzana.nostrumfairies.tiles.OutputChestTileEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OutputChestGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/output_chest.png");
	private static final int GUI_TEXT_WIDTH = 176;
	private static final int GUI_TEXT_HEIGHT = 132;
	private static final int GUI_PANEL_WIDTH = 80;
	private static final int GUI_PANEL_HEIGHT = 100;
	private static final int GUI_INV_CELL_LENGTH = 18;
	private static final int GUI_TEXT_MISSING_ICON_HOFFSET = GUI_TEXT_WIDTH;
	private static final int GUI_TEXT_WORKING_ICON_HOFFSET = GUI_TEXT_WIDTH + GUI_INV_CELL_LENGTH;
	private static final int GUI_TOP_INV_HOFFSET = 62;
	private static final int GUI_TOP_INV_VOFFSET = 18;
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 50;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 108;

	public static class OutputChestContainer extends LogicContainer {
		
		public static final String ID = "output_chest";
		
		protected OutputChestTileEntity chest;
		protected final LogicPanel logicPanel;
		private int chestIDStart;
		
		public OutputChestContainer(int windowId, PlayerInventory playerInv, OutputChestTileEntity chest) {
			super(FairyContainers.OutputChest, windowId, chest);
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

			logicPanel = new LogicPanel(this, chest, -GUI_PANEL_WIDTH, 0, GUI_PANEL_WIDTH, GUI_PANEL_HEIGHT);
		}
		
		public static OutputChestContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			return new OutputChestContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(OutputChestTileEntity chest) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new OutputChestContainer(windowId, playerInv, chest);
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
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (logicPanel.handleSlotClick(slotId, dragType, clickTypeIn, player)) {
				return ItemStack.EMPTY;
			}
			
			if (player.inventory.getItemStack().isEmpty()) {
				// empty hand. Right-click?
				if (slotId >= chestIDStart && dragType == 1 && clickTypeIn == ClickType.PICKUP && chest.getStackInSlot(slotId - chestIDStart) == null) {
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
	public static class OutputChestGuiContainer extends LogicGuiContainer<OutputChestContainer> {

		private OutputChestContainer container;
		private final LogicPanelGui<OutputChestGuiContainer> panelGui;
		
		public OutputChestGuiContainer(OutputChestContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			this.panelGui = new LogicPanelGui<>(container.logicPanel, this, 0xFFE2E0C3, true);
			
			this.xSize = GUI_TEXT_WIDTH;
			this.ySize = GUI_TEXT_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
			panelGui.init(mc, guiLeft, guiTop);
		}
		
		private void drawStatus(MatrixStack matrixStackIn, float partialTicks, boolean available) {
			float alpha = (float) (.5f + (.25f * Math.sin(Math.PI * (double)(System.currentTimeMillis() % 1000) / 1000.0)));
			mc.getTextureManager().bindTexture(TEXT);
			
			final int text_hoffset = (available ? GUI_TEXT_WORKING_ICON_HOFFSET : GUI_TEXT_MISSING_ICON_HOFFSET);
			final int text_voffset = 0;
			RenderSystem.enableBlend();
			RenderFuncs.blit(matrixStackIn, 0, 0, text_hoffset, text_voffset, GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH,
					1f, 1f, 1f, alpha);
			RenderSystem.disableBlend();
		}
		
		private void drawTemplate(MatrixStack matrixStackIn, float partialTicks, @Nonnull ItemStack template) {
			if (!template.isEmpty()) {
				matrixStackIn.push();
				Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(template, 0, 0);
				matrixStackIn.translate(0, 0, 110);
				if (template.getCount() > 1) {
					final String count = "" + template.getCount();
					
					this.font.drawStringWithShadow(matrixStackIn, "" + template.getCount(),
							GUI_INV_CELL_LENGTH - (this.font.getStringWidth(count) + 1),
							GUI_INV_CELL_LENGTH - (this.font.FONT_HEIGHT),
							0xFFFFFFFF);
				}
//				else {
//					GlStateManager.enableAlphaTest();
//				}
				RenderSystem.enableBlend();
				RenderFuncs.drawRect(matrixStackIn, 0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA0636259);
				RenderSystem.disableBlend();
				matrixStackIn.pop();
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			// Draw templates, if needed
			for (int i = 0; i < container.chest.getSizeInventory(); i++) {
				ItemStack template = container.chest.getTemplate(i);
				ItemStack stack = container.chest.getStackInSlot(i);
				
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + GUI_TOP_INV_HOFFSET + (i * GUI_INV_CELL_LENGTH),
						verticalMargin + GUI_TOP_INV_VOFFSET,
						0);
				
				if (container.chest.getStackInSlot(i).isEmpty()) {
					matrixStackIn.push();
					matrixStackIn.scale(1f, 1f, .05f);
					drawTemplate(matrixStackIn, partialTicks, container.chest.getTemplate(i));
					matrixStackIn.pop();
				}
				
				if (!template.isEmpty() && (stack.isEmpty() || stack.getCount() < template.getCount())) {
					matrixStackIn.translate(0, 0, 100);
					drawStatus(matrixStackIn, partialTicks, true);
				}
				
				matrixStackIn.pop();
			}
			
			panelGui.draw(matrixStackIn, mc, horizontalMargin, verticalMargin);
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
		
	}
	
}
