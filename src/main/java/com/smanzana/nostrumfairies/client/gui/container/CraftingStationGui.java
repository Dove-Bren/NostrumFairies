package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.FeySlotIcon;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.tiles.CraftingBlockTileEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		
		public static final String ID = "crafting_station";
		
		protected CraftingBlockTileEntity station;
		private int stationInputCount;
		protected Slot outputSlot;
		//protected HideableSlot criteriaSlot;
		protected FeyStoneContainerSlot upgradeSlot;
		protected final LogicPanel panel;
		
		private int stationInputIDStart;
		private int stationInputIDEnd;
		
		public CraftingStationContainer(int windowId, PlayerInventory playerInv, CraftingBlockTileEntity station) {
			this(FairyContainers.CraftingStation, windowId, playerInv, station);
		}
		
		protected CraftingStationContainer(ContainerType<? extends CraftingStationContainer> type, int windowId, PlayerInventory playerInv, CraftingBlockTileEntity station) {
			super(type, windowId, station);
			this.station = station;
						
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, GUI_TEXT_SIDE_WIDTH + GUI_PLAYER_INV_HOFFSET + (x * 18), GUI_PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, GUI_TEXT_SIDE_WIDTH + GUI_HOTBAR_INV_HOFFSET + x * 18, GUI_HOTBAR_INV_VOFFSET));
			}
			
			stationInputIDStart = this.inventorySlots.size();
			int dim = station.getCraftGridDim();
			stationInputCount = dim * dim;
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < dim; j++) {
					final int index = (i * dim) + j;
					this.addSlot(new Slot(station, index, GUI_TEXT_SIDE_WIDTH + getCraftGridStartX() + j * 18, getCraftGridStartY() + (i * 18)) {
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
			this.addSlot(outputSlot);
			
			// Upgrade slot
			this.upgradeSlot = new FeyStoneContainerSlot(station, stationInputCount + 2,
					GUI_TEXT_SIDE_WIDTH + GUI_UPGRADE_INV_HOFFSET,
					GUI_UPGRADE_INV_VOFFSET, FeySlotType.EITHERGRADE);
			this.addSlot(upgradeSlot);
			
			// Do this here because this constructor adds another slot
			this.panel = new LogicPanel(this, station, 0, 0, GUI_TEXT_SIDE_WIDTH, GUI_TEXT_SIDE_HEIGHT);
		}
		
		public static CraftingStationContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			return new CraftingStationContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(CraftingBlockTileEntity hopper) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new CraftingStationContainer(windowId, playerInv, hopper);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, hopper);
			});
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
				prev = cur;
				
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
	public static class CraftingStationGuiContainer extends LogicGuiContainer<CraftingStationContainer> {

		private CraftingStationContainer container;
		private final LogicPanelGui<CraftingStationGuiContainer> panelGui;
		
		public CraftingStationGuiContainer(CraftingStationContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			this.panelGui = new LogicPanelGui<>(container.panel, this, 0xFFE5FFF8, true);
			
			
			this.xSize = GUI_TEXT_MAIN_WIDTH + GUI_TEXT_SIDE_WIDTH;
			this.ySize = GUI_TEXT_MAIN_HEIGHT;
			
			this.addButton(panelGui);
		}
		
		@Override
		public void init() {
			super.init();
			panelGui.init(mc, guiLeft, guiTop);
		}
		
		protected ResourceLocation getBackgroundTexture() {
			return TEXT;
		}
		
		private void drawProgress(MatrixStack matrixStackIn, float progress) {
			mc.getTextureManager().bindTexture(getBackgroundTexture());
			
			int width = (int) ((float) GUI_PROGRESS_ICON_WIDTH * progress);
			
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
					GUI_PROGRESS_ICON_HOFFSET, GUI_PROGRESS_ICON_VOFFSET,
					width, GUI_PROGRESS_ICON_HEIGHT,
					width, GUI_PROGRESS_ICON_HEIGHT,
					256, 256);
		}
		
		private void drawError(MatrixStack matrixStackIn) {
			final long period = 2000L;
			float perc = (float) ((double) (System.currentTimeMillis() % period) / (double) period);
			perc = (float) (.5 * (1 + Math.sin(perc * Math.PI * 2)));
			float alpha = .2f + .3f * perc;
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderSystem.enableBlend();
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, -1, -1,
					GUI_ERROR_ICON_HOFFSET, GUI_ERROR_ICON_VOFFSET,
					GUI_ERROR_ICON_WIDTH, GUI_ERROR_ICON_HEIGHT,
					GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH,
					256, 256,
					1f, 1f, 1f, alpha);
			RenderSystem.disableBlend();
		}
		
		private void drawBoost(MatrixStack matrixStackIn) {
			final long period = 2000L;
			float perc = (float) ((double) (System.currentTimeMillis() % period) / (double) period);
			perc = (float) (.5 * (1 + Math.sin(perc * Math.PI * 2)));
			float alpha = .2f + .3f * perc;
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderSystem.enableBlend();
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, -1 + ((GUI_INV_CELL_LENGTH * 3) / 4), -1 + ((GUI_INV_CELL_LENGTH * 3) / 4),
					GUI_BOOST_ICON_HOFFSET, GUI_BOOST_ICON_VOFFSET,
					GUI_BOOST_ICON_WIDTH, GUI_BOOST_ICON_HEIGHT,
					GUI_INV_CELL_LENGTH / 4, GUI_INV_CELL_LENGTH / 4,
					256, 256,
					1f, 1f, 1f, alpha);
			RenderSystem.disableBlend();
		}
		
		private void drawTemplate(MatrixStack matrixStackIn, @Nonnull ItemStack template) {
			if (!template.isEmpty()) {
				matrixStackIn.push();
				{
					RenderSystem.pushMatrix();
					RenderSystem.multMatrix(matrixStackIn.getLast().getMatrix());
					Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(template, 0, 0);
					RenderSystem.popMatrix();
				}
				matrixStackIn.translate(0, 0, 110);
				//GlStateManager.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderFuncs.drawRect(matrixStackIn, 0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA05B6460);
				RenderSystem.disableBlend();
				matrixStackIn.pop();
			}
		}
		
		private void drawRecipe(MatrixStack matrixStackIn) {
			ICraftingRecipe recipe = container.station.getRecipe();
			if (recipe != null) {
				ItemStack outcome = recipe.getRecipeOutput();
				matrixStackIn.push();
				{
					RenderSystem.pushMatrix();
					RenderSystem.multMatrix(matrixStackIn.getLast().getMatrix());
					Minecraft.getInstance().getItemRenderer().renderItemIntoGUI(outcome, 0, 0);
					RenderSystem.popMatrix();
				}
				matrixStackIn.translate(0, 0, 110);
				//GlStateManager.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderFuncs.drawRect(matrixStackIn, 0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA05B6460);
				RenderSystem.disableBlend();
				matrixStackIn.pop();
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = this.guiLeft + GUI_TEXT_SIDE_WIDTH;
			int verticalMargin = this.guiTop;
			
			mc.getTextureManager().bindTexture(getBackgroundTexture());
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_MAIN_WIDTH, GUI_TEXT_MAIN_HEIGHT, 256, 256);
			
			// Draw templates and errors, if needed
			for (int i = 0; i < container.stationInputCount; i++) {
				final ItemStack template = container.station.getTemplate(i);
				final ItemStack stack = container.station.getStackInSlot(i);
				final boolean error = container.station.getField(i + 3) == -1;
				final boolean bonus = container.station.getField(i + 3) == 1;
				final int dim = container.station.getCraftGridDim();
				final int x = (i % dim);
				final int y = (i / dim);
				
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + container.getCraftGridStartX() + (x * GUI_INV_CELL_LENGTH),
						verticalMargin + container.getCraftGridStartY() + (y * GUI_INV_CELL_LENGTH),
						0);
				
				if (stack.isEmpty()) {
					matrixStackIn.push();
					matrixStackIn.scale(1f, 1f, .05f);
					drawTemplate(matrixStackIn, template);
					matrixStackIn.pop();
				}
				
				if (error) {
					matrixStackIn.translate(0, 0, 100);
					drawError(matrixStackIn);
				} else if (bonus) {
					matrixStackIn.translate(0, 0, 1000);
					drawBoost(matrixStackIn);
				}
				
				matrixStackIn.pop();
			}
			
			// Draw upgrade slot
			{
				matrixStackIn.push();
				matrixStackIn.translate(guiLeft,
						verticalMargin,
						0);
				FeySlotIcon.draw(matrixStackIn, container.upgradeSlot, 1f);
				//GlStateManager.disableLighting();
				matrixStackIn.pop();
			}
			
			// Draw progress
			{
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + GUI_PROGRESS_HOFFSET, verticalMargin + GUI_PROGRESS_VOFFSET, 0);
				
				float progress = (float) container.station.getField(0) / 100f;
				this.drawProgress(matrixStackIn, progress);
				
				matrixStackIn.pop();
			}
			
			// Draw logic panel
			{
				matrixStackIn.push();
				//GlStateManager.translate(this.guiLeft, this.guiTop, 0);
				
				panelGui.draw(matrixStackIn, mc, guiLeft, guiTop);
				
				matrixStackIn.pop();
			}
			
			// Draw outcome
			if (container.station.getOutputStack().isEmpty())
			{
				matrixStackIn.push();
				matrixStackIn.translate(horizontalMargin + GUI_OUTPUT_INV_HOFFSET, verticalMargin + GUI_OUTPUT_INV_VOFFSET, 0);
				
				drawRecipe(matrixStackIn);
				
				matrixStackIn.pop();
			}
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			;
		}
	}
	
}
