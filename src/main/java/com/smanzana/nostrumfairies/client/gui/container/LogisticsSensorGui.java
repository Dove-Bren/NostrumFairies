package com.smanzana.nostrumfairies.client.gui.container;

import java.io.IOException;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.tiles.LogisticsSensorTileEntity;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		
		protected LogisticsSensorTileEntity sensor;
		protected final LogicPanel panel;
		protected final IInventory playerInv;
		
		public LogisticsSensorContainer(IInventory playerInv, LogisticsSensorTileEntity sensor) {
			super(null);
			this.sensor = sensor;
			this.playerInv = playerInv;
						
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
			
			// Do this here because this constructor adds another slot
			this.panel = new LogicPanel(this, sensor, 0, 0, GUI_TEXT_MAIN_WIDTH, 90);
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
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
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
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
	
	@SideOnly(Side.CLIENT)
	public static class LogisticsSensorGuiContainer extends LogicGuiContainer {

		//private LogisticsSensorContainer container;
		private final LogicPanelGui panelGui;
		
		public LogisticsSensorGuiContainer(LogisticsSensorContainer container) {
			super(container);
			//this.container = container;
			this.panelGui = new LogicPanelGui(container.panel, this, 0xFF88C0CC, false);
			
			this.xSize = GUI_TEXT_MAIN_WIDTH;
			this.ySize = GUI_TEXT_MAIN_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
			panelGui.initGui(mc, guiLeft, guiTop);
		}
		
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = this.guiLeft;
			int verticalMargin = this.guiTop;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_TEXT_MAIN_WIDTH, GUI_TEXT_MAIN_HEIGHT, 256, 256);
			
			panelGui.draw(mc, guiLeft, guiTop);

			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		}
		
		@Override
		public void actionPerformed(GuiButton button) {
			if (panelGui.actionPerformed(button)) {
				return;
			}
			
			; // No other buttons for sensor
		}
		
		@Override
		protected void keyTyped(char typedChar, int keyCode) throws IOException {
			if (panelGui.keyTyped(typedChar, keyCode)) {
				return;
			}
			
			super.keyTyped(typedChar, keyCode);
		}
		
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			if (panelGui.mouseClicked(mouseX, mouseY, mouseButton, this.guiLeft, this.guiTop)) {
				return;
			}
			
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}
	
}
