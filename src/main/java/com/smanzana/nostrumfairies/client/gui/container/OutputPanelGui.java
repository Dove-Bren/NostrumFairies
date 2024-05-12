package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;
import com.smanzana.nostrumfairies.tiles.OutputPanelTileEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OutputPanelGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/output_chest.png");
	private static final int GUI_TEXT_WIDTH = 176;
	private static final int GUI_TEXT_HEIGHT = 132;
	private static final int GUI_LPANEL_WIDTH = 80;
	private static final int GUI_LPANEL_HEIGHT = 100;
	private static final int GUI_INV_CELL_LENGTH = 18;
//	private static final int GUI_TEXT_MISSING_ICON_HOFFSET = GUI_TEXT_WIDTH;
//	private static final int GUI_TEXT_WORKING_ICON_HOFFSET = GUI_TEXT_WIDTH + GUI_INV_CELL_LENGTH;
	private static final int GUI_TOP_INV_HOFFSET = 62;
	private static final int GUI_TOP_INV_VOFFSET = 18;
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 50;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 108;
	
	public static class OutputPanelContainer extends LogicContainer {
		
		public static final String ID = "output_panel";
		
		private IInventory slots = new Inventory(3);
		protected OutputPanelTileEntity panel;
		protected final LogicPanel logicPanel;
		private int panelIDStart;
		
		public OutputPanelContainer(int windowId, PlayerInventory playerInv, OutputPanelTileEntity panel) {
			super(FairyContainers.OutputPanel, windowId, panel);
			this.panel = panel;
						
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, GUI_PLAYER_INV_HOFFSET + GUI_LPANEL_WIDTH + (x * 18), GUI_PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, GUI_HOTBAR_INV_HOFFSET + GUI_LPANEL_WIDTH + x * 18, GUI_HOTBAR_INV_VOFFSET));
			}
			
			panelIDStart = this.inventorySlots.size();
			for (int i = 0; i < slots.getSizeInventory(); i++) {
				this.addSlot(new Slot(slots, i, GUI_TOP_INV_HOFFSET + GUI_LPANEL_WIDTH + i * 18, GUI_TOP_INV_VOFFSET) {
					@Override
					public boolean isItemValid(@Nonnull ItemStack stack) {
				        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
				    }
					
					@Override
					public int getSlotStackLimit() {
						return 1;
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
			
			logicPanel = new LogicPanel(this, panel, 0, 0, GUI_LPANEL_WIDTH, GUI_LPANEL_HEIGHT);
		}
		
		public static OutputPanelContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			return new OutputPanelContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(OutputPanelTileEntity panel) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new OutputPanelContainer(windowId, playerInv, panel);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, panel);
			});
		}
		
		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
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
//					slot.putStack(leftover.isEmpty() ? ItemStack.EMPTY : leftover);
//					if (!leftover.isEmpty() && leftover.getCount() == prev.getCount()) {
//						prev = ItemStack.EMPTY;
//					}
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
				if (slotId >= panelIDStart && dragType == 1 && clickTypeIn == ClickType.PICKUP) {
					panel.setTemplate(slotId - panelIDStart, ItemStack.EMPTY);
					return ItemStack.EMPTY;
				}
			} else {
				// Item in hand. Clicking in template inventory?
				if (slotId >= panelIDStart) {
					// Clicking empty slot?
					if (clickTypeIn == ClickType.PICKUP && panel.getTemplate(slotId - panelIDStart).isEmpty()) {
						ItemStack template = player.inventory.getItemStack();
						if (dragType == 1) { // right click
							template = template.copy();
							template.setCount(1);
						}
						panel.setTemplate(slotId - panelIDStart, template);
					}
					return ItemStack.EMPTY;
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
	
	@OnlyIn(Dist.CLIENT)
	public static class OutputPanelGuiContainer extends LogicGuiContainer<OutputPanelContainer> {

		private OutputPanelContainer container;
		private final LogicPanelGui<OutputPanelGuiContainer> panelGui;
		
		public OutputPanelGuiContainer(OutputPanelContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			this.panelGui = new LogicPanelGui<>(container.logicPanel, this, 0xFFE2E0C3, true);
			
			
			this.xSize = GUI_TEXT_WIDTH + GUI_LPANEL_WIDTH;
			this.ySize = GUI_TEXT_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
			panelGui.init(mc, guiLeft, guiTop);
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
				RenderFuncs.drawRect(matrixStackIn, 0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA0636259);
				matrixStackIn.pop();
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin + GUI_LPANEL_WIDTH, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			// Draw templates, if needed
			for (int i = 0; i < container.slots.getSizeInventory(); i++) {
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + GUI_LPANEL_WIDTH + GUI_TOP_INV_HOFFSET + (i * GUI_INV_CELL_LENGTH),
						verticalMargin + GUI_TOP_INV_VOFFSET,
						0);
				
				matrixStackIn.push();
				matrixStackIn.scale(1f, 1f, .05f);
				drawTemplate(matrixStackIn, partialTicks, container.panel.getTemplate(i));
				matrixStackIn.pop();
				
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
