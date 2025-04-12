package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;
import com.smanzana.nostrumfairies.tiles.OutputPanelTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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
		
		private Container slotInv = new SimpleContainer(3);
		protected OutputPanelTileEntity panel;
		protected final LogicPanel logicPanel;
		private int panelIDStart;
		
		public OutputPanelContainer(int windowId, Inventory playerInv, OutputPanelTileEntity panel) {
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
			
			panelIDStart = this.slots.size();
			for (int i = 0; i < slotInv.getContainerSize(); i++) {
				this.addSlot(new Slot(slotInv, i, GUI_TOP_INV_HOFFSET + GUI_LPANEL_WIDTH + i * 18, GUI_TOP_INV_VOFFSET) {
					@Override
					public boolean mayPlace(@Nonnull ItemStack stack) {
				        return this.container.canPlaceItem(this.getSlotIndex(), stack);
				    }
					
					@Override
					public int getMaxStackSize() {
						return 1;
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
			
			logicPanel = new LogicPanel(this, panel, 0, 0, GUI_LPANEL_WIDTH, GUI_LPANEL_HEIGHT);
		}
		
		public static OutputPanelContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
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
		public ItemStack quickMoveStack(Player playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.slots.get(fromSlot);
			
			if (slot != null && slot.hasItem()) {
				ItemStack cur = slot.getItem();
				prev = cur.copy();
				
				if (slot.container == this.slotInv) {
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
		public boolean stillValid(Player playerIn) {
			return true;
		}
		
		@Override
		public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
			if (logicPanel.handleSlotClick(slotId, dragType, clickTypeIn, player)) {
				return;
			}
			
			if (getCarried().isEmpty()) {
				// empty hand. Right-click?
				if (slotId >= panelIDStart && dragType == 1 && clickTypeIn == ClickType.PICKUP) {
					panel.setTemplate(slotId - panelIDStart, ItemStack.EMPTY);
					return;
				}
			} else {
				// Item in hand. Clicking in template inventory?
				if (slotId >= panelIDStart) {
					// Clicking empty slot?
					if (clickTypeIn == ClickType.PICKUP && panel.getTemplate(slotId - panelIDStart).isEmpty()) {
						ItemStack template = getCarried();
						if (dragType == 1) { // right click
							template = template.copy();
							template.setCount(1);
						}
						panel.setTemplate(slotId - panelIDStart, template);
					}
					return;
				}
			}
			
			//return null;
			super.clicked(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return slotIn.index < panelIDStart;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class OutputPanelGuiContainer extends LogicGuiContainer<OutputPanelContainer> {

		private OutputPanelContainer container;
		private final LogicPanelGui<OutputPanelGuiContainer> panelGui;
		
		public OutputPanelGuiContainer(OutputPanelContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			this.container = container;
			this.panelGui = new LogicPanelGui<>(container.logicPanel, this, 0xFFE2E0C3, true);
			
			
			this.imageWidth = GUI_TEXT_WIDTH + GUI_LPANEL_WIDTH;
			this.imageHeight = GUI_TEXT_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
			panelGui.init(mc, leftPos, topPos);
		}
		
		private void drawTemplate(PoseStack matrixStackIn, float partialTicks, @Nonnull ItemStack template) {
			if (!template.isEmpty()) {
				matrixStackIn.pushPose();
				{
					RenderFuncs.RenderGUIItem(template, matrixStackIn);
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
				RenderFuncs.drawRect(matrixStackIn, 0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA0636259);
				matrixStackIn.popPose();
			}
		}
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
			RenderSystem.setShaderTexture(0, TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin + GUI_LPANEL_WIDTH, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			// Draw templates, if needed
			for (int i = 0; i < container.slotInv.getContainerSize(); i++) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(horizontalMargin + GUI_LPANEL_WIDTH + GUI_TOP_INV_HOFFSET + (i * GUI_INV_CELL_LENGTH),
						verticalMargin + GUI_TOP_INV_VOFFSET,
						0);
				
				matrixStackIn.pushPose();
				matrixStackIn.scale(1f, 1f, .05f);
				drawTemplate(matrixStackIn, partialTicks, container.panel.getTemplate(i));
				matrixStackIn.popPose();
				
				matrixStackIn.popPose();
			}
			
			panelGui.draw(matrixStackIn, mc, horizontalMargin, verticalMargin);
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
	}
	
}
