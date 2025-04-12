package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.FeySlotIcon;
import com.smanzana.nostrumfairies.client.gui.container.LogicContainer.LogicGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogicPanel.LogicPanelGui;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.tiles.CraftingBlockTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
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
		
		public CraftingStationContainer(int windowId, Inventory playerInv, CraftingBlockTileEntity station) {
			this(FairyContainers.CraftingStation, windowId, playerInv, station);
		}
		
		protected CraftingStationContainer(MenuType<? extends CraftingStationContainer> type, int windowId, Inventory playerInv, CraftingBlockTileEntity station) {
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
			
			stationInputIDStart = this.slots.size();
			int dim = station.getCraftGridDim();
			stationInputCount = dim * dim;
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < dim; j++) {
					final int index = (i * dim) + j;
					this.addSlot(new Slot(station, index, GUI_TEXT_SIDE_WIDTH + getCraftGridStartX() + j * 18, getCraftGridStartY() + (i * 18)) {
						@Override
						public boolean mayPlace(@Nonnull ItemStack stack) {
					        return this.container.canPlaceItem(this.getSlotIndex(), stack);
					    }
						
						@Override
						public int getMaxStackSize() {
							ItemStack template = station.getTemplate(index);
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
			
			stationInputIDEnd = this.slots.size();
			
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
		
		public static CraftingStationContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
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
		public ItemStack quickMoveStack(Player playerIn, int fromSlot) {
			ItemStack prev = ItemStack.EMPTY;	
			Slot slot = (Slot) this.slots.get(fromSlot);
			
			if (slot != null && slot.hasItem()) {
				ItemStack cur = slot.getItem();
				prev = cur;
				
				if (slot.container == this.station) {
					// Trying to take one of our items.
					// We only allow that with the output slot
					if (slot == outputSlot) {
						if (playerIn.getInventory().add(cur)) {
							slot.set(ItemStack.EMPTY);
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
		public boolean stillValid(Player playerIn) {
			return true;
		}
		
		@Override
		public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
			if (panel.handleSlotClick(slotId, dragType, clickTypeIn, player)) {
				return;
			}
			
			if (getCarried().isEmpty()) {
				// empty hand.
				if (clickTypeIn == ClickType.PICKUP) {
					// Input slot?
					if (slotId >= stationInputIDStart && slotId < stationInputIDEnd
							&& station.getItem(slotId - stationInputIDStart).isEmpty()) {
						
						// Only care of it's a right-click.
						if (dragType == 1) {
							station.setTemplate(slotId - stationInputIDStart, ItemStack.EMPTY);
							return;
						}
					}
				}
			} else {
				// Item in hand. Clicking empty templatable slot?
				if (clickTypeIn == ClickType.PICKUP) {
					// Input slot?
					if (slotId >= stationInputIDStart && slotId < stationInputIDEnd
							&& station.getTemplate(slotId - stationInputIDStart).isEmpty()) {
						ItemStack template = getCarried().copy();
						template.setCount(1);
						station.setTemplate(slotId - stationInputIDStart, template);
						return;
					}
				}
			}
			super.clicked(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return slotIn.index < stationInputIDStart;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class CraftingStationGuiContainer extends LogicGuiContainer<CraftingStationContainer> {

		private CraftingStationContainer container;
		private final LogicPanelGui<CraftingStationGuiContainer> panelGui;
		
		public CraftingStationGuiContainer(CraftingStationContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			this.container = container;
			this.panelGui = new LogicPanelGui<>(container.panel, this, 0xFFE5FFF8, true);
			
			
			this.imageWidth = GUI_TEXT_MAIN_WIDTH + GUI_TEXT_SIDE_WIDTH;
			this.imageHeight = GUI_TEXT_MAIN_HEIGHT;
			
			this.addRenderableWidget(panelGui);
		}
		
		@Override
		public void init() {
			super.init();
			panelGui.init(mc, leftPos, topPos);
		}
		
		protected ResourceLocation getBackgroundTexture() {
			return TEXT;
		}
		
		private void drawProgress(PoseStack matrixStackIn, float progress) {
			RenderSystem.setShaderTexture(0, getBackgroundTexture());
			
			int width = (int) ((float) GUI_PROGRESS_ICON_WIDTH * progress);
			
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
					GUI_PROGRESS_ICON_HOFFSET, GUI_PROGRESS_ICON_VOFFSET,
					width, GUI_PROGRESS_ICON_HEIGHT,
					width, GUI_PROGRESS_ICON_HEIGHT,
					256, 256);
		}
		
		private void drawError(PoseStack matrixStackIn) {
			final long period = 2000L;
			float perc = (float) ((double) (System.currentTimeMillis() % period) / (double) period);
			perc = (float) (.5 * (1 + Math.sin(perc * Math.PI * 2)));
			float alpha = .2f + .3f * perc;
			RenderSystem.setShaderTexture(0, TEXT);
			
			RenderSystem.enableBlend();
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, -1, -1,
					GUI_ERROR_ICON_HOFFSET, GUI_ERROR_ICON_VOFFSET,
					GUI_ERROR_ICON_WIDTH, GUI_ERROR_ICON_HEIGHT,
					GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH,
					256, 256,
					1f, 1f, 1f, alpha);
			RenderSystem.disableBlend();
		}
		
		private void drawBoost(PoseStack matrixStackIn) {
			final long period = 2000L;
			float perc = (float) ((double) (System.currentTimeMillis() % period) / (double) period);
			perc = (float) (.5 * (1 + Math.sin(perc * Math.PI * 2)));
			float alpha = .2f + .3f * perc;
			RenderSystem.setShaderTexture(0, TEXT);
			
			RenderSystem.enableBlend();
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, -1 + ((GUI_INV_CELL_LENGTH * 3) / 4), -1 + ((GUI_INV_CELL_LENGTH * 3) / 4),
					GUI_BOOST_ICON_HOFFSET, GUI_BOOST_ICON_VOFFSET,
					GUI_BOOST_ICON_WIDTH, GUI_BOOST_ICON_HEIGHT,
					GUI_INV_CELL_LENGTH / 4, GUI_INV_CELL_LENGTH / 4,
					256, 256,
					1f, 1f, 1f, alpha);
			RenderSystem.disableBlend();
		}
		
		private void drawTemplate(PoseStack matrixStackIn, @Nonnull ItemStack template) {
			if (!template.isEmpty()) {
				matrixStackIn.pushPose();
				{
//					RenderSystem.pushMatrix();
//					RenderSystem.multMatrix(matrixStackIn.last().pose());
//					Minecraft.getInstance().getItemRenderer().renderGuiItem(template, 0, 0);
//					RenderSystem.popMatrix();
					RenderFuncs.RenderGUIItem(template, matrixStackIn);
				}
				matrixStackIn.translate(0, 0, 110);
				//GlStateManager.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderFuncs.drawRect(matrixStackIn, 0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA05B6460);
				RenderSystem.disableBlend();
				matrixStackIn.popPose();
			}
		}
		
		private void drawRecipe(PoseStack matrixStackIn) {
			CraftingRecipe recipe = container.station.getRecipe();
			if (recipe != null) {
				ItemStack outcome = recipe.getResultItem();
				matrixStackIn.pushPose();
				{
//					RenderSystem.pushMatrix();
//					RenderSystem.multMatrix(matrixStackIn.last().pose());
//					Minecraft.getInstance().getItemRenderer().renderGuiItem(outcome, 0, 0);
//					RenderSystem.popMatrix();
					RenderFuncs.RenderGUIItem(outcome, matrixStackIn);
				}
				matrixStackIn.translate(0, 0, 110);
				//GlStateManager.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderFuncs.drawRect(matrixStackIn, 0, 0, GUI_INV_CELL_LENGTH - 2, GUI_INV_CELL_LENGTH - 2, 0xA05B6460);
				RenderSystem.disableBlend();
				matrixStackIn.popPose();
			}
		}
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = this.leftPos + GUI_TEXT_SIDE_WIDTH;
			int verticalMargin = this.topPos;
			
			RenderSystem.setShaderTexture(0, getBackgroundTexture());
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_MAIN_WIDTH, GUI_TEXT_MAIN_HEIGHT, 256, 256);
			
			// Draw templates and errors, if needed
			for (int i = 0; i < container.stationInputCount; i++) {
				final ItemStack template = container.station.getTemplate(i);
				final ItemStack stack = container.station.getItem(i);
				final boolean error = container.station.getField(i + 3) == -1;
				final boolean bonus = container.station.getField(i + 3) == 1;
				final int dim = container.station.getCraftGridDim();
				final int x = (i % dim);
				final int y = (i / dim);
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(horizontalMargin + container.getCraftGridStartX() + (x * GUI_INV_CELL_LENGTH),
						verticalMargin + container.getCraftGridStartY() + (y * GUI_INV_CELL_LENGTH),
						0);
				
				if (stack.isEmpty()) {
					matrixStackIn.pushPose();
					matrixStackIn.scale(1f, 1f, .05f);
					drawTemplate(matrixStackIn, template);
					matrixStackIn.popPose();
				}
				
				if (error) {
					matrixStackIn.translate(0, 0, 100);
					drawError(matrixStackIn);
				} else if (bonus) {
					matrixStackIn.translate(0, 0, 1000);
					drawBoost(matrixStackIn);
				}
				
				matrixStackIn.popPose();
			}
			
			// Draw upgrade slot
			{
				matrixStackIn.pushPose();
				matrixStackIn.translate(leftPos,
						verticalMargin,
						0);
				FeySlotIcon.draw(matrixStackIn, container.upgradeSlot, 1f);
				//GlStateManager.disableLighting();
				matrixStackIn.popPose();
			}
			
			// Draw progress
			{
				matrixStackIn.pushPose();
				matrixStackIn.translate(horizontalMargin + GUI_PROGRESS_HOFFSET, verticalMargin + GUI_PROGRESS_VOFFSET, 0);
				
				float progress = (float) container.station.getField(0) / 100f;
				this.drawProgress(matrixStackIn, progress);
				
				matrixStackIn.popPose();
			}
			
			// Draw logic panel
			{
				matrixStackIn.pushPose();
				//GlStateManager.translate(this.guiLeft, this.guiTop, 0);
				
				panelGui.draw(matrixStackIn, mc, leftPos, topPos);
				
				matrixStackIn.popPose();
			}
			
			// Draw outcome
			if (container.station.getOutputStack().isEmpty())
			{
				matrixStackIn.pushPose();
				matrixStackIn.translate(horizontalMargin + GUI_OUTPUT_INV_HOFFSET, verticalMargin + GUI_OUTPUT_INV_VOFFSET, 0);
				
				drawRecipe(matrixStackIn);
				
				matrixStackIn.popPose();
			}
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			;
		}
	}
	
}
