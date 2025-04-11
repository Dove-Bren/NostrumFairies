package com.smanzana.nostrumfairies.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;
import com.smanzana.nostrumfairies.tiles.LogisticsSensorTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Logistics logic gui
 * @author Skyler
 *
 */
public class LogisticsSensorGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/logistics_sensor.png");
	private static final int GUI_TEXT_MAIN_WIDTH = 176;
	private static final int GUI_TEXT_MAIN_HEIGHT = 168;
	
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 86;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 144;
	
	public static class LogisticsSensorContainer extends LogicContainer {
		
		public static final String ID = "logistics_sensor";
		
		protected LogisticsSensorTileEntity sensor;
		protected final LogicPanel panel;
		protected final Container playerInv;
		
		public LogisticsSensorContainer(int windowId, Inventory playerInv, LogisticsSensorTileEntity sensor) {
			super(FairyContainers.LogisticsSensor, windowId, null);
			this.sensor = sensor;
			this.playerInv = playerInv;
						
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
			
			// Do this here because this constructor adds another slot
			this.panel = new LogicPanel(this, sensor, 0, 0, GUI_TEXT_MAIN_WIDTH, 90);
		}
		
		public static LogisticsSensorContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
			return new LogisticsSensorContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(LogisticsSensorTileEntity sensor) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new LogisticsSensorContainer(windowId, playerInv, sensor);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, sensor);
			});
		}
		
		@Override
		public ItemStack quickMoveStack(Player playerIn, int fromSlot) {
//			ItemStack prev = ItemStack.EMPTY;	
//			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
//			
//			if (slot != null && slot.getHasStack()) {
//				ItemStack cur = slot.getStack();
//				prev = cur.copy();
//				
//				if (slot.inventory == this.inv) {
//					;
//				} else {
//					// shift-click in player inventory. Just disallow.
//					prev = ItemStack.EMPTY;
//				}
//			}
//			
//			return prev;
			return ItemStack.EMPTY;
		}
		
		@Override
		public boolean stillValid(Player playerIn) {
			return true;
		}
		
		@Override
		public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
			if (panel.handleSlotClick(slotId, dragType, clickTypeIn, player)) {
				return ItemStack.EMPTY;
			}
			
			// Nothing special to do for sensor
			
			return super.clicked(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return slotIn.container == playerInv;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class LogisticsSensorGuiContainer extends LogicGuiContainer<LogisticsSensorContainer> {

		//private LogisticsSensorContainer container;
		private final LogicPanelGui<LogisticsSensorGuiContainer> panelGui;
		
		public LogisticsSensorGuiContainer(LogisticsSensorContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			//this.container = container;
			this.panelGui = new LogicPanelGui<>(container.panel, this, 0xFF88C0CC, false);
			
			this.imageWidth = GUI_TEXT_MAIN_WIDTH;
			this.imageHeight = GUI_TEXT_MAIN_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
			panelGui.init(mc, leftPos, topPos);
		}
		
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = this.leftPos;
			int verticalMargin = this.topPos;
			
			mc.getTextureManager().bind(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_MAIN_WIDTH, GUI_TEXT_MAIN_HEIGHT, 256, 256);
			
			panelGui.draw(matrixStackIn, mc, leftPos, topPos);
			
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
	}
	
}
