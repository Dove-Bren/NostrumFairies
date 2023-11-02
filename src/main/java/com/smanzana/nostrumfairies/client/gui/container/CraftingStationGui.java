package com.smanzana.nostrumfairies.client.gui.container;

import java.io.IOException;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FeySlotIcon;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.tiles.CraftingBlockTileEntity;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 3x3 regular sized crafting station
 * @author Skyler
 *
 */
public class CraftingStationGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/craft_block.png");
	private static final int GUI_TEXT_MAIN_WIDTH = 176;
	private static final int GUI_TEXT_MAIN_HEIGHT = 168;
	
	private static final int GUI_TEXT_SIDE_WIDTH = 80;
	private static final int GUI_TEXT_SIDE_HEIGHT = 86;
//	private static final int GUI_TEXT_SIDE_HOFFSET = GUI_TEXT_MAIN_WIDTH;
//	private static final int GUI_TEXT_SIDE_VOFFSET = 0;
	
	private static final int GUI_INV_CELL_LENGTH = 18;
	private static final int GUI_TOP_INV_HOFFSET = 31;
	private static final int GUI_TOP_INV_VOFFSET = 18;
	private static final int GUI_OUTPUT_INV_HOFFSET = 126;
	private static final int GUI_OUTPUT_INV_VOFFSET = 36;
	private static final int GUI_UPGRADE_INV_HOFFSET = GUI_TEXT_MAIN_WIDTH - (GUI_INV_CELL_LENGTH + 3);
	private static final int GUI_UPGRADE_INV_VOFFSET = 5;
	
	private static final int GUI_PLAYER_INV_HOFFSET = 8;
	private static final int GUI_PLAYER_INV_VOFFSET = 86;
	private static final int GUI_HOTBAR_INV_HOFFSET = 8;
	private static final int GUI_HOTBAR_INV_VOFFSET = 144;
	
	private static final int GUI_PROGRESS_ICON_HOFFSET = 0;
	private static final int GUI_PROGRESS_ICON_VOFFSET = 200;
	private static final int GUI_PROGRESS_ICON_WIDTH = 22;
	private static final int GUI_PROGRESS_ICON_HEIGHT = 16;
	
	public static final int GUI_PROGRESS_HOFFSET = 92;
	public static final int GUI_PROGRESS_VOFFSET = 36;
	
	private static final int GUI_ERROR_ICON_HOFFSET = 0;
	private static final int GUI_ERROR_ICON_VOFFSET = 220;
	private static final int GUI_ERROR_ICON_WIDTH = 21;
	private static final int GUI_ERROR_ICON_HEIGHT = 21;
	
	private static final int GUI_BOOST_ICON_HOFFSET = GUI_ERROR_ICON_HOFFSET + GUI_ERROR_ICON_WIDTH;
	private static final int GUI_BOOST_ICON_VOFFSET = GUI_ERROR_ICON_VOFFSET;
	private static final int GUI_BOOST_ICON_WIDTH = 21;
	private static final int GUI_BOOST_ICON_HEIGHT = 21;

	public static class CraftingStationContainer extends LogicContainer {
		
		protected CraftingBlockTileEntity station;
		private int stationInputCount;
		protected Slot outputSlot;
		//protected HideableSlot criteriaSlot;
		protected FeyStoneContainerSlot upgradeSlot;
		protected final LogicPanel panel;
		
		private int stationInputIDStart;
		private int stationInputIDEnd;
		
		public CraftingStationContainer(IInventory playerInv, CraftingBlockTileEntity station) {
			super(station);
			this.station = station;
						
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlotToContainer(new Slot(playerInv, x + y * 9 + 9, GUI_TEXT_SIDE_WIDTH + GUI_PLAYER_INV_HOFFSET + (x * 18), GUI_PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlotToContainer(new Slot(playerInv, x, GUI_TEXT_SIDE_WIDTH + GUI_HOTBAR_INV_HOFFSET + x * 18, GUI_HOTBAR_INV_VOFFSET));
			}
			
			stationInputIDStart = this.inventorySlots.size();
			int dim = station.getCraftGridDim();
			stationInputCount = dim * dim;
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < dim; j++) {
					final int index = (i * dim) + j;
					this.addSlotToContainer(new Slot(station, index, GUI_TEXT_SIDE_WIDTH + getCraftGridStartX() + j * 18, getCraftGridStartY() + (i * 18)) {
						@Override
						public boolean isItemValid(@Nonnull ItemStack stack) {
					        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
					    }
						
						@Override
						public int getSlotStackLimit() {
							ItemStack template = station.getTemplate(index);
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
			
			stationInputIDEnd = this.inventorySlots.size();
			
			// Output slot
			this.outputSlot = new Slot(station, stationInputCount, GUI_TEXT_SIDE_WIDTH + GUI_OUTPUT_INV_HOFFSET, GUI_OUTPUT_INV_VOFFSET);
			this.addSlotToContainer(outputSlot);
			
			// Upgrade slot
			this.upgradeSlot = new FeyStoneContainerSlot(station, stationInputCount + 2,
					GUI_TEXT_SIDE_WIDTH + GUI_UPGRADE_INV_HOFFSET,
					GUI_UPGRADE_INV_VOFFSET, FeySlotType.EITHERGRADE);
			this.addSlotToContainer(upgradeSlot);
			
			// Do this here because this constructor adds another slot
			this.panel = new LogicPanel(this, station, 0, 0, GUI_TEXT_SIDE_WIDTH, GUI_TEXT_SIDE_HEIGHT);
		}
		
		protected int getCraftGridStartX() {
			return GUI_TOP_INV_HOFFSET;
		}
		
		protected int getCraftGridStartY() {
			return GUI_TOP_INV_VOFFSET;
		}
		
		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				if (slot.inventory == this.station) {
					// Trying to take one of our items.
					// We only allow that with the output slot
					if (slot == outputSlot) {
						if (playerIn.inventory.addItemStackToInventory(cur)) {
							slot.putStack(ItemStack.EMPTY);
							slot.onTake(playerIn, cur);
						} else {
							prev = ItemStack.EMPTY;
						}
					}
				} else {
					// shift-click in player inventory. Just disallow.
					prev = ItemStack.EMPTY;
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
			if (panel.handleSlotClick(slotId, dragType, clickTypeIn, player)) {
				return ItemStack.EMPTY;
			}
			
			if (player.inventory.getItemStack().isEmpty()) {
				// empty hand.
				if (clickTypeIn == ClickType.PICKUP) {
					// Input slot?
					if (slotId >= stationInputIDStart && slotId < stationInputIDEnd
							&& station.getStackInSlot(slotId - stationInputIDStart).isEmpty()) {
						
						// Only care of it's a right-click.
						if (dragType == 1) {
							station.setTemplate(slotId - stationInputIDStart, ItemStack.EMPTY);
							return ItemStack.EMPTY;
						}
					}
				}
			} else {
				// Item in hand. Clicking empty templatable slot?
				if (clickTypeIn == ClickType.PICKUP) {
					// Input slot?
					if (slotId >= stationInputIDStart && slotId < stationInputIDEnd
							&& station.getTemplate(slotId - stationInputIDStart).isEmpty()) {
						ItemStack template = player.inventory.getItemStack().copy();
						template.setCount(1);
						station.setTemplate(slotId - stationInputIDStart, template);
						return ItemStack.EMPTY;
					}
				}
			}
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.slotNumber < stationInputIDStart;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class CraftingStationGuiContainer extends LogicGuiContainer {

		private CraftingStationContainer container;
		private final LogicPanelGui panelGui;
		
		public CraftingStationGuiContainer(CraftingStationContainer container) {
			super(container);
			this.container = container;
			this.panelGui = new LogicPanelGui(container.panel, this, 0xFFE5FFF8, true);
			
			
			this.xSize = GUI_TEXT_MAIN_WIDTH + GUI_TEXT_SIDE_WIDTH;
			this.ySize = GUI_TEXT_MAIN_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
			panelGui.initGui(mc, guiLeft, guiTop);
		}
		
		protected ResourceLocation getBackgroundTexture() {
			return TEXT;
		}
		
		private void drawProgress(float progress) {
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1f);
			mc.getTextureManager().bindTexture(getBackgroundTexture());
			
			int width = (int) ((float) GUI_PROGRESS_ICON_WIDTH * progress);
			
			GlStateManager.enableBlend();
			drawScaledCustomSizeModalRect(0, 0,
					GUI_PROGRESS_ICON_HOFFSET, GUI_PROGRESS_ICON_VOFFSET,
					width, GUI_PROGRESS_ICON_HEIGHT,
					width, GUI_PROGRESS_ICON_HEIGHT,
					256, 256);
		}
		
		private void drawError() {
			final long period = 2000L;
			float perc = (float) ((double) (System.currentTimeMillis() % period) / (double) period);
			perc = (float) (.5 * (1 + Math.sin(perc * Math.PI * 2)));
			float alpha = .2f + .3f * perc;
			GlStateManager.color(1.0F,  1.0F, 1.0F, alpha);
			mc.getTextureManager().bindTexture(TEXT);
			
			GlStateManager.enableBlend();
			drawScaledCustomSizeModalRect(-1, -1,
					GUI_ERROR_ICON_HOFFSET, GUI_ERROR_ICON_VOFFSET,
					GUI_ERROR_ICON_WIDTH, GUI_ERROR_ICON_HEIGHT,
					GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH,
					256, 256);
		}
		
		private void drawBoost() {
			final long period = 2000L;
			float perc = (float) ((double) (System.currentTimeMillis() % period) / (double) period);
			perc = (float) (.5 * (1 + Math.sin(perc * Math.PI * 2)));
			float alpha = .2f + .3f * perc;
			GlStateManager.color(1.0F,  1.0F, 1.0F, alpha);
			mc.getTextureManager().bindTexture(TEXT);
			
			GlStateManager.enableBlend();
			drawScaledCustomSizeModalRect(-1 + ((GUI_INV_CELL_LENGTH * 3) / 4), -1 + ((GUI_INV_CELL_LENGTH * 3) / 4),
					GUI_BOOST_ICON_HOFFSET, GUI_BOOST_ICON_VOFFSET,
					GUI_BOOST_ICON_WIDTH, GUI_BOOST_ICON_HEIGHT,
					GUI_INV_CELL_LENGTH / 4, GUI_INV_CELL_LENGTH / 4,
					256, 256);
		}
		
		private void drawTemplate(@Nonnull ItemStack template) {
			if (!template.isEmpty()) {
				GlStateManager.pushMatrix();
				this.itemRender.renderItemIntoGUI(template, 0, 0);
				GlStateManager.translate(0, 0, 110);
				GlStateManager.enableAlpha();
				drawRect(0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA05B6460);
				GlStateManager.popMatrix();
			}
		}
		
		private void drawRecipe() {
			IRecipe recipe = container.station.getRecipe();
			if (recipe != null) {
				ItemStack outcome = recipe.getRecipeOutput();
				GlStateManager.pushMatrix();
				this.itemRender.renderItemIntoGUI(outcome, 0, 0);
				GlStateManager.translate(0, 0, 110);
				GlStateManager.enableAlpha();
				drawRect(0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA05B6460);
				GlStateManager.popMatrix();
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = this.guiLeft + GUI_TEXT_SIDE_WIDTH;
			int verticalMargin = this.guiTop;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(getBackgroundTexture());
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_TEXT_MAIN_WIDTH, GUI_TEXT_MAIN_HEIGHT, 256, 256);
			
			// Draw templates and errors, if needed
			for (int i = 0; i < container.stationInputCount; i++) {
				final ItemStack template = container.station.getTemplate(i);
				final ItemStack stack = container.station.getStackInSlot(i);
				final boolean error = container.station.getField(i + 3) == -1;
				final boolean bonus = container.station.getField(i + 3) == 1;
				final int dim = container.station.getCraftGridDim();
				final int x = (i % dim);
				final int y = (i / dim);
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(horizontalMargin + container.getCraftGridStartX() + (x * GUI_INV_CELL_LENGTH),
						verticalMargin + container.getCraftGridStartY() + (y * GUI_INV_CELL_LENGTH),
						0);
				
				if (stack.isEmpty()) {
					GlStateManager.pushMatrix();
					GlStateManager.scale(1f, 1f, .05f);
					drawTemplate(template);
					GlStateManager.popMatrix();
				}
				
				if (error) {
					GlStateManager.translate(0, 0, 100);
					drawError();
				} else if (bonus) {
					GlStateManager.translate(0, 0, 1000);
					drawBoost();
				}
				
				GlStateManager.popMatrix();
			}
			
			// Draw upgrade slot
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft,
						verticalMargin,
						0);
				FeySlotIcon.draw(container.upgradeSlot, 1f);
				GlStateManager.disableLighting();
				GlStateManager.popMatrix();
			}
			
			// Draw progress
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(horizontalMargin + GUI_PROGRESS_HOFFSET, verticalMargin + GUI_PROGRESS_VOFFSET, 0);
				
				float progress = (float) container.station.getField(0) / 100f;
				this.drawProgress(progress);
				
				GlStateManager.popMatrix();
			}
			
			// Draw logic panel
			{
				GlStateManager.pushMatrix();
				//GlStateManager.translate(this.guiLeft, this.guiTop, 0);
				
				panelGui.draw(mc, guiLeft, guiTop);
				
				GlStateManager.popMatrix();
			}
			
			// Draw outcome
			if (container.station.getOutputStack().isEmpty())
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(horizontalMargin + GUI_OUTPUT_INV_HOFFSET, verticalMargin + GUI_OUTPUT_INV_VOFFSET, 0);
				
				drawRecipe();
				
				GlStateManager.popMatrix();
			}

			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			;
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
