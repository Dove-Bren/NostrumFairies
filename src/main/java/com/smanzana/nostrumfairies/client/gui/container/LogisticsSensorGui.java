package com.smanzana.nostrumfairies.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;
import com.smanzana.nostrumfairies.tiles.LogisticsSensorTileEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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
		protected final IInventory playerInv;
		
		public LogisticsSensorContainer(int windowId, PlayerInventory playerInv, LogisticsSensorTileEntity sensor) {
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
		
		public static LogisticsSensorContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
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
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
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
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			if (panel.handleSlotClick(slotId, dragType, clickTypeIn, player)) {
				return ItemStack.EMPTY;
			}
			
			// Nothing special to do for sensor
			
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.inventory == playerInv;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class LogisticsSensorGuiContainer extends LogicGuiContainer<LogisticsSensorContainer> {

		//private LogisticsSensorContainer container;
		private final LogicPanelGui<LogisticsSensorGuiContainer> panelGui;
		
		public LogisticsSensorGuiContainer(LogisticsSensorContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			//this.container = container;
			this.panelGui = new LogicPanelGui<>(container.panel, this, 0xFF88C0CC, false);
			
			this.xSize = GUI_TEXT_MAIN_WIDTH;
			this.ySize = GUI_TEXT_MAIN_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
			panelGui.init(mc, guiLeft, guiTop);
		}
		
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = this.guiLeft;
			int verticalMargin = this.guiTop;
			
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_MAIN_WIDTH, GUI_TEXT_MAIN_HEIGHT, 256, 256);
			
			panelGui.draw(matrixStackIn, mc, guiLeft, guiTop);
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
	}
	
}
