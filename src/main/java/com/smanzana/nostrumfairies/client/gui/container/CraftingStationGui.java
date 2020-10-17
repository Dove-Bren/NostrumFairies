package com.smanzana.nostrumfairies.client.gui.container;

import java.io.IOException;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.CraftingBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.CraftingBlockTileEntity.CraftingCriteriaMode;
import com.smanzana.nostrumfairies.blocks.CraftingBlockTileEntity.CraftingLogicOp;
import com.smanzana.nostrumfairies.client.gui.FeySlotIcon;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CraftingStationActionMessage;
import com.smanzana.nostrummagica.client.gui.container.AutoContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
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
	private static final int GUI_TEXT_SIDE_HOFFSET = GUI_TEXT_MAIN_WIDTH;
	private static final int GUI_TEXT_SIDE_VOFFSET = 0;
	
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
	
	private static final int GUI_EMPTY_CELL_HOFFSET = 176;
	private static final int GUI_EMPTY_CELL_VOFFSET = 86;
	
	private static final int GUI_BUTTON_HOFFSET = 176;
	private static final int GUI_BUTTON_VOFFSET = 104;
	private static final int GUI_BUTTON_LENGTH = 18;
	
	private static final int GUI_MODE_ICON_HOFFSET = 0;
	private static final int GUI_MODE_ICON_VOFFSET = 168;
	private static final int GUI_MODE_ICON_LENGTH = 32;
	
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

	public static class CraftingStationContainer extends AutoContainer {
		
		protected CraftingBlockTileEntity station;
		private int stationInputCount;
		protected Slot outputSlot;
		protected HideableSlot criteriaSlot;
		protected FeyStoneContainerSlot upgradeSlot;
		
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
						public boolean isItemValid(@Nullable ItemStack stack) {
					        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
					    }
						
						@Override
						public int getSlotStackLimit() {
							ItemStack template = station.getTemplate(index);
							if (template == null) {
								return super.getSlotStackLimit();
							} else {
								return template.stackSize;
							}
						}
						
						@Override
						public void putStack(@Nullable ItemStack stack) {
	//						ItemStack template = chest.getTemplate(index);
	//						if (template == null) {
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
			
			// Sometimes-hidden criteria slot
			this.criteriaSlot = new HideableSlot(station, stationInputCount + 1, 1 + (GUI_TEXT_SIDE_WIDTH - GUI_INV_CELL_LENGTH) / 2, 31);
			this.addSlotToContainer(criteriaSlot);
			
			// Upgrade slot
			this.upgradeSlot = new FeyStoneContainerSlot(station, stationInputCount + 2,
					GUI_TEXT_SIDE_WIDTH + GUI_UPGRADE_INV_HOFFSET,
					GUI_UPGRADE_INV_VOFFSET, FeySlotType.EITHERGRADE);
			this.addSlotToContainer(upgradeSlot);
		}
		
		protected int getCraftGridStartX() {
			return GUI_TOP_INV_HOFFSET;
		}
		
		protected int getCraftGridStartY() {
			return GUI_TOP_INV_VOFFSET;
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			ItemStack prev = null;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				if (slot.inventory == this.station) {
					// Trying to take one of our items.
					// We only allow that with the output slot
					if (slot == outputSlot) {
						if (playerIn.inventory.addItemStackToInventory(cur)) {
							slot.putStack(null);
							slot.onPickupFromSlot(playerIn, cur);
						} else {
							prev = null;
						}
					}
				} else {
					// shift-click in player inventory. Just disallow.
					prev = null;
				}
			}
			
			return prev;
		}
		
		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
			if (player.inventory.getItemStack() == null) {
				// empty hand.
				if (clickTypeIn == ClickType.PICKUP) {
					// Input slot?
					if (slotId >= stationInputIDStart && slotId < stationInputIDEnd
							&& station.getStackInSlot(slotId - stationInputIDStart) == null) {
						
						// Only care of it's a right-click.
						if (dragType == 1) {
							station.setTemplate(slotId - stationInputIDStart, null);
							return null;
						}
					}
					
					// Criteria slot?
					if (slotId == stationInputIDEnd + 1) {
						// If right-click, remove template. Otherwise ignore.
						if (dragType == 1) {
							station.setInventorySlotContents(slotId - stationInputIDStart, null);
						}
						return null;
					}
				}
			} else {
				// Item in hand. Clicking empty templatable slot?
				if (clickTypeIn == ClickType.PICKUP) {
					// Input slot?
					if (slotId >= stationInputIDStart && slotId < stationInputIDEnd
							&& station.getTemplate(slotId - stationInputIDStart) == null) {
						ItemStack template = player.inventory.getItemStack().copy();
						template.stackSize = 1;
						station.setTemplate(slotId - stationInputIDStart, template);
						return null;
					}
					
					// Criteria slot?
					if (slotId == stationInputIDEnd + 1 && station.getStackInSlot(slotId - stationInputIDStart) == null) {
						ItemStack template = player.inventory.getItemStack().copy();
						template.stackSize = 1;
						station.setInventorySlotContents(slotId - stationInputIDStart, template);
						return null;
					}
				}
			}
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.slotNumber < stationInputIDStart;
		}
		
		protected static class HideableSlot extends Slot {

			protected boolean hidden;
			protected final int originalX;
			protected final int originalY;
			
			public HideableSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
				super(inventoryIn, index, xPosition, yPosition);
				this.originalX = xPosition;
				this.originalY = yPosition;
			}
			
			@Override
			public boolean canBeHovered() {
				return !hidden;
			}
			
			public void hide(boolean hide) {
				if (hide != hidden) {
					hidden = hide;
					if (hide) {
						this.xDisplayPosition = -1000;
						this.yDisplayPosition = -1000;
					} else {
						this.xDisplayPosition = originalX;
						this.yDisplayPosition = originalY;
					}
				}
			}
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	public static class CraftingStationGuiContainer extends GuiContainer {

		private CraftingStationContainer container;
		private OpButton opButton;
		
		private String criteriaString;
		private boolean editSelected;
		
		public CraftingStationGuiContainer(CraftingStationContainer container) {
			super(container);
			this.container = container;
			
			this.xSize = GUI_TEXT_MAIN_WIDTH + GUI_TEXT_SIDE_WIDTH;
			this.ySize = GUI_TEXT_MAIN_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
			
			// Center the main window, and then let the other part hand to the left
			this.guiLeft = Math.max(0, ((this.width - GUI_TEXT_MAIN_WIDTH) / 2) - GUI_TEXT_SIDE_WIDTH);
			
			CraftingCriteriaMode mode = CraftingCriteriaMode.values()[container.station.getField(1) % CraftingCriteriaMode.values().length];
			boolean logicMode = (mode == CraftingCriteriaMode.LOGIC);
			
			this.addButton(new ModeButton(0, guiLeft + (GUI_TEXT_SIDE_WIDTH - GUI_INV_CELL_LENGTH) / 2, guiTop + 10));
			opButton = new OpButton(1, guiLeft + (GUI_TEXT_SIDE_WIDTH - GUI_INV_CELL_LENGTH) / 2, guiTop + 50);
			opButton.visible = logicMode;
			this.addButton(opButton);
			
			// Also fix up criteria item slot
			container.criteriaSlot.hidden = !(logicMode);
			
			editSelected = false;
			criteriaString = String.format("%d", container.station.getCriteriaCount());
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
		
		private void drawCriteriaMode(CraftingCriteriaMode mode) {
			int textX = GUI_MODE_ICON_HOFFSET;
			switch (mode) {
			case ALWAYS:
			default:
				;
				break;
			case LOGIC:
				textX += GUI_MODE_ICON_LENGTH;
				break;
			case REDSTONE_LOW:
				textX += GUI_MODE_ICON_LENGTH * 2;
				break;
			case REDSTONE_HIGH:
				textX += GUI_MODE_ICON_LENGTH * 3;
				break;
			
			}
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1f);
			mc.getTextureManager().bindTexture(TEXT);
			GlStateManager.enableBlend();
			drawScaledCustomSizeModalRect(2, 2,
					textX, GUI_MODE_ICON_VOFFSET,
					GUI_MODE_ICON_LENGTH, GUI_MODE_ICON_LENGTH,
					GUI_BUTTON_LENGTH - 4, GUI_BUTTON_LENGTH - 4,
					256, 256);
		}
		
		private void drawCriteriaOp(CraftingLogicOp op) {
			final String s;
			switch (op) {
			case EQUAL:
			default:
				s = "=";
				break;
			case LESS:
				s = "<";
				break;
			case MORE:
				s = ">";
				break;
			}
			
			final int sWidth = this.fontRendererObj.getStringWidth(s);
			this.fontRendererObj.drawString(s, (GUI_BUTTON_LENGTH + -sWidth) / 2, 1 + (GUI_BUTTON_LENGTH - fontRendererObj.FONT_HEIGHT) / 2, 0xFFFFFFFF);
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
		
		private void drawTemplate(@Nullable ItemStack template) {
			if (template != null) {
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
		
		private void drawInputBar() {
			final int margin = 5;
			Gui.drawRect(margin - 1, -1, GUI_TEXT_SIDE_WIDTH - margin + 1, this.fontRendererObj.FONT_HEIGHT + 3, 0xFF444444);
			Gui.drawRect(margin, 0, GUI_TEXT_SIDE_WIDTH - margin, this.fontRendererObj.FONT_HEIGHT + 2, 0xFF000000);
			
			final int width = fontRendererObj.getStringWidth(criteriaString);
			fontRendererObj.drawString(criteriaString, (GUI_TEXT_SIDE_WIDTH - width) / 2, 2, 0xFFFFFFFF);
			
			if (editSelected) {
				final long period = 600; // .5 seconds
				if ((System.currentTimeMillis() % (2 * period)) / period == 1) { 
					final int x = ((GUI_TEXT_SIDE_WIDTH + width) / 2) + 1;
					//Gui.drawRect(x, 1, x + 1, this.fontRendererObj.FONT_HEIGHT, 0xFFFFFFFF);
					fontRendererObj.drawString("_", x, 2, 0xFFFFFFFF);
				}
			}
		}
		
		private void drawSideBar() {
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1f);
			mc.getTextureManager().bindTexture(getBackgroundTexture());
			
			GlStateManager.enableBlend();
			this.drawTexturedModalRect(0, 0,
					GUI_TEXT_SIDE_HOFFSET, GUI_TEXT_SIDE_VOFFSET,
					GUI_TEXT_SIDE_WIDTH, GUI_TEXT_SIDE_HEIGHT);
			
			// If mode is appropriate, draw logic template background and input field
			CraftingCriteriaMode mode = CraftingCriteriaMode.values()[container.station.getField(1) % CraftingCriteriaMode.values().length];
			if (mode == CraftingCriteriaMode.LOGIC) {
				GlStateManager.pushMatrix();
				GlStateManager.translate((GUI_TEXT_SIDE_WIDTH - GUI_INV_CELL_LENGTH) / 2, 30, 0);
				
				this.drawTexturedModalRect(0, 0,
						GUI_EMPTY_CELL_HOFFSET, GUI_EMPTY_CELL_VOFFSET,
						GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH);

				GlStateManager.popMatrix();
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 70, 0);
				drawInputBar();

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
				
				if (stack == null) {
					GlStateManager.pushMatrix();
					GlStateManager.scale(1f, 1f, .05f);
					drawTemplate(template);
					GlStateManager.popMatrix();
				}
				
				if (error) {
					GlStateManager.translate(0, 0, 100);
					drawError();
				} else if (bonus) {
					GlStateManager.translate(0, 0, 100);
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
			
			// Draw side bar
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop, 0);
				
				this.drawSideBar();
				
				GlStateManager.popMatrix();
			}
			
			// Draw outcome
			if (container.station.getOutputStack() == null)
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
			if (button == this.opButton) {
				CraftingLogicOp op = CraftingLogicOp.values()[container.station.getField(2) % CraftingLogicOp.values().length];
				// Cycle up ops
				op = (CraftingLogicOp.values()[(op.ordinal() + 1) % CraftingLogicOp.values().length]);
				
				NetworkHandler.getSyncChannel().sendToServer(new CraftingStationActionMessage(container.station, op));
			} else if (button instanceof ModeButton) {
				CraftingCriteriaMode mode = CraftingCriteriaMode.values()[container.station.getField(1) % CraftingCriteriaMode.values().length];
				// Cycle up modes
				mode = (CraftingCriteriaMode.values()[(mode.ordinal() + 1) % CraftingCriteriaMode.values().length]);
				
				NetworkHandler.getSyncChannel().sendToServer(new CraftingStationActionMessage(container.station, mode));
				
				// Also refresh hidden buttons
				boolean logicMode = (mode == CraftingCriteriaMode.LOGIC);
				opButton.visible = logicMode;
				container.criteriaSlot.hidden = !(logicMode);
			}
		}
		
		@Override
		protected void keyTyped(char typedChar, int keyCode) throws IOException {
			if (this.editSelected) {
				if (Character.isDigit(typedChar) || keyCode == 14) { // 14 = backspace
					final String s;
					if (keyCode == 14) {
						if (!criteriaString.isEmpty()) {
							s = criteriaString.substring(0, criteriaString.length() - 1);
						} else {
							s = "";
						}
					} else {
						s = this.criteriaString + typedChar;
					}
					
					int val;
					try {
						val = Integer.parseInt(s);
					} catch (Exception e) {
						if (s.length() > 2) {
							// Overflowed
							val = Integer.MAX_VALUE;
						} else {
							val = 0;
						}
					}
					
					this.criteriaString = String.format("%d", val);
					NetworkHandler.getSyncChannel().sendToServer(new CraftingStationActionMessage(container.station, val));
					return;
				} else if (keyCode == 28) { // ENTER/return
					this.editSelected = false;
					return;
				} 
			}
//			else if (editSelected && keyCode == 1) {
//				editSelected = false;
//				return;
//			}
			
			super.keyTyped(typedChar, keyCode);
		}
		
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			if (mouseX >= guiLeft + 5 && mouseX <= guiLeft + GUI_TEXT_SIDE_WIDTH - 5
					&& mouseY >= guiTop + 70 && mouseY <= guiTop + 70 + this.fontRendererObj.FONT_HEIGHT + 2) {
				editSelected = true;
				return;
			}
			
			editSelected = false;
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		protected class ModeButton extends GuiButton {

			private boolean pressed;
			
			public ModeButton(int buttonId, int x, int y) {
				super(buttonId, x, y, GUI_BUTTON_LENGTH, GUI_BUTTON_LENGTH, "");
				pressed = false;
			}
			
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY) {
				this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				
				int textX = GUI_BUTTON_HOFFSET;
				if (pressed) {
					textX += GUI_BUTTON_LENGTH * 2;
				} else if (hovered) {
					textX += GUI_BUTTON_LENGTH;
				}
				
				GlStateManager.color(1.0F,  1.0F, 1.0F, 1f);
				mc.getTextureManager().bindTexture(getBackgroundTexture());
				GlStateManager.enableBlend();
				GlStateManager.pushMatrix();
				GlStateManager.translate(xPosition, yPosition, 0);
				this.drawTexturedModalRect(0, 0,
						textX, GUI_BUTTON_VOFFSET,
						GUI_BUTTON_LENGTH, GUI_BUTTON_LENGTH);
				
				// Then draw mode
				CraftingCriteriaMode mode = CraftingCriteriaMode.values()[container.station.getField(1) % CraftingCriteriaMode.values().length];
				drawCriteriaMode(mode);
				GlStateManager.popMatrix();
			}
			
			@Override
			public void mouseReleased(int mouseX, int mouseY) {
				pressed = false;
				super.mouseReleased(mouseX, mouseY);
			}
			
			public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
				boolean ret = super.mousePressed(mc, mouseX, mouseY);
				pressed = ret;
				return ret;
			}
			
		}
		
		protected class OpButton extends GuiButton {

			private boolean pressed;
			
			public OpButton(int buttonId, int x, int y) {
				super(buttonId, x, y, GUI_BUTTON_LENGTH, GUI_BUTTON_LENGTH, "");
				pressed = false;
			}
			
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY) {
				if (!this.visible) {
					return;
				}
				
				this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				
				int textX = GUI_BUTTON_HOFFSET;
				if (pressed) {
					textX += GUI_BUTTON_LENGTH * 2;
				} else if (hovered) {
					textX += GUI_BUTTON_LENGTH;
				}
				
				GlStateManager.color(1.0F,  1.0F, 1.0F, 1f);
				mc.getTextureManager().bindTexture(getBackgroundTexture());
				GlStateManager.enableBlend();
				GlStateManager.pushMatrix();
				GlStateManager.translate(xPosition, yPosition, 0);
				this.drawTexturedModalRect(0, 0,
						textX, GUI_BUTTON_VOFFSET,
						GUI_BUTTON_LENGTH, GUI_BUTTON_LENGTH);
				
				// Then draw mode
				CraftingLogicOp op = CraftingLogicOp.values()[container.station.getField(2) % CraftingLogicOp.values().length];
				drawCriteriaOp(op);
				GlStateManager.popMatrix();
			}
			
			@Override
			public void mouseReleased(int mouseX, int mouseY) {
				pressed = false;
				super.mouseReleased(mouseX, mouseY);
			}
			
			public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
				boolean ret = super.mousePressed(mc, mouseX, mouseY);
				pressed = ret;
				return ret;
			}
			
		}
		
	}
	
}
