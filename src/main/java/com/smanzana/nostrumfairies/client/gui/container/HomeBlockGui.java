package com.smanzana.nostrumfairies.client.gui.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.HomeBlockTileEntity.HomeBlockSlotInventory;
import com.smanzana.nostrumfairies.client.gui.FeySlotIcon;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.inventory.FeySlotType;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HomeBlockGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/home_base.png");
	private static final int GUI_TEXT_WIDTH = 202;
	private static final int GUI_TEXT_HEIGHT = 221;
	private static final int GUI_INV_CELL_LENGTH = 18;
	private static final int GUI_TEXT_LIST_ITEM_HOFFSET = 0;
	private static final int GUI_TEXT_LIST_ITEM_vOFFSET = GUI_TEXT_HEIGHT;
	
	private static final int GUI_PLAYER_INV_HOFFSET = 23;
	private static final int GUI_PLAYER_INV_VOFFSET = 140;
	private static final int GUI_HOTBAR_INV_HOFFSET = 23;
	private static final int GUI_HOTBAR_INV_VOFFSET = 198;
	
	private static final int GUI_INFO_HOFFSET = 8;
	private static final int GUI_INFO_VOFFSET = 7;
	//private static final int GUI_INFO_WIDTH = 169;
	//private static final int GUI_INFO_HEIGHT = 37;
	
	private static final int GUI_UPGRADE_HOFFSET = 180;
	private static final int GUI_UPGRADE_VOFFSET = 6;
	
	private static final int GUI_LIST_HOFFSET = 7;
	private static final int GUI_LIST_VOFFSET = 47;
	private static final int GUI_LIST_ITEM_WIDTH = 78;
	private static final int GUI_LIST_ITEM_HEIGHT = 16;
	
	private static final int GUI_DETAILS_HOFFSET = 89;
	private static final int GUI_DETAILS_VOFFSET = 47;
	private static final int GUI_DETAILS_WIDTH = 107;
	private static final int GUI_DETAILS_HEIGHT = 80;

	public static class HomeBlockContainer extends Container {
		
		protected HomeBlockTileEntity home;
		private final int homeIDStart;
		private final int specializationIDStart;
		private final List<ResidentSlot> residentSlots;
		private final List<FeyContainerSlot> upgradeSlots;
		
		public HomeBlockContainer(IInventory playerInv, HomeBlockTileEntity home) {
			this.home = home;
						
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
			
			homeIDStart = this.inventorySlots.size();
			upgradeSlots = new ArrayList<>(2);
			
			// Add upgrade slots, which are static
			for (int i = 0; i < 2; i++) {
				FeyContainerSlot slot = new FeyContainerSlot(home.getUpgradeInventory(), i,
						GUI_UPGRADE_HOFFSET,
						GUI_UPGRADE_VOFFSET + (i * (GUI_INV_CELL_LENGTH + 3)),
						FeySlotType.UPGRADE);
				this.addSlotToContainer(slot);
				upgradeSlots.add(slot);
			}
			
			residentSlots = new ArrayList<>(home.getSlotInventory().getSizeInventory());
			int i;
			for (i = 0; i < home.getSlotInventory().getSizeInventory(); i++) {
				if (!HomeBlockSlotInventory.isSoulSlot(i)) {
					break;
				}
				
				ResidentSlot slot = new ResidentSlot(home, i,
						GUI_LIST_HOFFSET + ((GUI_LIST_ITEM_WIDTH - 16) / 2),
						GUI_LIST_VOFFSET + (i * GUI_LIST_ITEM_HEIGHT));
				this.addSlotToContainer(slot);
				residentSlots.add(slot);
			}
			
			specializationIDStart = i;
			
			for (; i < home.getSlotInventory().getSizeInventory(); i++) {
				ResidentSlot slot = new ResidentSlot(home, i,
						GUI_DETAILS_HOFFSET + (GUI_DETAILS_WIDTH - (GUI_INV_CELL_LENGTH - 2)) / 2,
						GUI_DETAILS_VOFFSET + GUI_DETAILS_HEIGHT - (GUI_INV_CELL_LENGTH * 2));
				this.addSlotToContainer(slot);
				residentSlots.add(slot);
			}
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			return super.transferStackInSlot(playerIn, fromSlot);
//			ItemStack prev = null;	
//			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
//			
//			if (slot != null && slot.getHasStack()) {
//				ItemStack cur = slot.getStack();
//				prev = cur.copy();
//				
//				if (slot.inventory == this.home) {
//					// Trying to take one of our items
//					if (playerIn.inventory.addItemStackToInventory(cur)) {
//						slot.putStack(null);
//						slot.onPickupFromSlot(playerIn, cur);
//					} else {
//						prev = null;
//					}
//				} else {
//					// shift-click in player inventory
//					ItemStack leftover = ItemStacks.addItem(home, cur);
//					slot.putStack(leftover != null && leftover.stackSize <= 0 ? null : leftover);
//					if (leftover != null && leftover.stackSize == prev.stackSize) {
//						prev = null;
//					}
//				}
//				
//			}
//			
//			return prev;
		}
		
		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.slotNumber < homeIDStart;
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	public static class HomeBlockGuiContainer extends GuiContainer {

		private HomeBlockContainer container;
		protected int selection = -1;
		
		protected EntityFeyBase feyArray[];
		private long feyArrayCacheTimer;
		
		public HomeBlockGuiContainer(HomeBlockContainer container) {
			super(container);
			this.container = container;
			
			this.xSize = GUI_TEXT_WIDTH;
			this.ySize = GUI_TEXT_HEIGHT;
			feyArray = null;
		}
		
		@Override
		public void initGui() {
			super.initGui();
		}
		
		private String getAetherDescription(float level) {
			if (level > .9f) {
				return "Healthy Aether";
			} else if (level > .5f) {
				return "Moderate Aether";
			} else if (level > .2f) {
				return "Low Aether";
			} else {
				return "Failing Aether";
			}
		}
		
		private void drawSummary(int x, int y) {
			/* Elven Residence - The beautiful rose
			 * 0/1 Residents      Healthy Aether     5% Growth
			 */
			String name = container.home.getName();
			float scale = .75f;
			int count = 0;
			int maxcount = 0;
			for (EntityFeyBase fey : this.feyArray) {
				if (fey != null) {
					count++;
				}
			}
			for (ResidentSlot slot : container.residentSlots) {
				slot.isItemDisplay = false;
				if (slot.getType() == FeySlotType.SOUL && slot.getHasStack()) {
					maxcount++;
				}
				slot.isItemDisplay = true;
			}
			
			GlStateManager.color(1f, 1f, 1f, 1f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 5, y + 2, 0);
			
			fontRendererObj.drawStringWithShadow(name, 0, 0, 0xFFFFFFFF);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 14, 0);
			GlStateManager.scale(scale, scale, scale);
			fontRendererObj.drawString(String.format("%.0f%% Growth", container.home.getGrowth()),
					0, 0, 0xFFA0A0A0);
			GlStateManager.popMatrix();
			
			
			GlStateManager.pushMatrix();
			
			GlStateManager.translate(0, 26, 0);
			GlStateManager.scale(scale, scale, scale);
			fontRendererObj.drawString(count + "/" + maxcount + " Residents",
					0, 0, 0xFFA0A0A0);
			
			String str = getAetherDescription(container.home.getAetherLevel());
			fontRendererObj.drawString(str,
					215 - (fontRendererObj.getStringWidth(str)), 0, 0xFFA0A0A0);
			GlStateManager.popMatrix();

			GlStateManager.popMatrix();
		}
		
		private void drawListItem(int x, int y, boolean hasStone, boolean mouseOver, @Nullable EntityFeyBase fey) {
			mc.getTextureManager().bindTexture(TEXT);
			GlStateManager.color(1f, 1f, 1f, 1f);
			this.drawTexturedModalRect(x, y, GUI_TEXT_LIST_ITEM_HOFFSET + (mouseOver ? GUI_LIST_ITEM_WIDTH : 0), GUI_TEXT_LIST_ITEM_vOFFSET,
					GUI_LIST_ITEM_WIDTH, GUI_LIST_ITEM_HEIGHT);
			
			if (hasStone) {
				if (fey != null) {
					// display information about the fey for selection
					String name = fey.getName();
					if (fontRendererObj.getStringWidth(name) * .75f > GUI_LIST_ITEM_WIDTH - 4) {
						int len = 0;
						int index = 0;
						len += fontRendererObj.getStringWidth("..."); // offset to include ellipses
						while ((len + fontRendererObj.getCharWidth(name.charAt(index))) * .75f < (GUI_LIST_ITEM_WIDTH - 4)) {
							len += fontRendererObj.getCharWidth(name.charAt(index));
							index++;
						}
						name = name.substring(0, index) + "...";
					}
					
					GlStateManager.pushMatrix();
					GlStateManager.translate(x + 2, y + 1 + ((GUI_LIST_ITEM_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2) / .75f, 0);
					GlStateManager.scale(.75f, .75f, .75f);
					this.fontRendererObj.drawStringWithShadow(name, 0, 0, 0xFFFFFFFF);
					GlStateManager.popMatrix();
				} else {
					// Show a 'VACANT' notice lol
					String str = "Vacant";
					this.fontRendererObj.drawStringWithShadow("Vacant",
							x + (GUI_LIST_ITEM_WIDTH - fontRendererObj.getStringWidth(str)) / 2,
							y + 1 + ((GUI_LIST_ITEM_HEIGHT - fontRendererObj.FONT_HEIGHT) / 2), 0xFFFFFFFF);
				}
			}
		}
		
		private void drawList(int x, int y, EntityFeyBase[] feyArray, int mouseIndex) {
			for (int i = 0; i < container.home.getSlots(); i++) {
				@Nullable EntityFeyBase fey = (i >= feyArray.length ? null : feyArray[i]);
				drawListItem(x, y + (i * GUI_LIST_ITEM_HEIGHT), container.home.getSlotInventory().hasStone(i), mouseIndex == i, fey);
			}
		}
		
		private void drawDetails(int x, int y, @Nullable EntityFeyBase fey) {
			if (fey == null) {
				return;
			}
			
			int previewSize = 24;
			int previewMargin = 2;
			float nameScale = .75f;
			GlStateManager.color(1f, 1f, 1f, 1f);
			
			// Details
			int nameSpace = GUI_DETAILS_WIDTH - (2 + 2 + previewSize + previewMargin + previewMargin);
			
			// -> Backplate
			drawRect(x, y, x + GUI_DETAILS_WIDTH, y + previewSize + previewMargin + previewMargin, 0x40000000);

			// -> Name
			String name = fey.getName();
			if (fontRendererObj.getStringWidth(name) * nameScale > nameSpace) {
				int len = 0;
				int index = 0;
				len += fontRendererObj.getStringWidth("..."); // offset to include ellipses
				while ((len + fontRendererObj.getCharWidth(name.charAt(index))) * nameScale < nameSpace) {
					len += fontRendererObj.getCharWidth(name.charAt(index));
					index++;
				}
				name = name.substring(0, index) + "...";
			}
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 2 + (nameSpace - (fontRendererObj.getStringWidth(name) * nameScale)) / 2, y + 5, 0);
			GlStateManager.scale(nameScale, nameScale, nameScale);
			this.fontRendererObj.drawStringWithShadow(name, 0, 0, 0xFFFFFFFF);
			GlStateManager.popMatrix();
			
			// -> Title
			name = fey.getSpecializationName();
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 2 + (nameSpace - (fontRendererObj.getStringWidth(name) * nameScale)) / 2, y + 5 + 11, 0);
			GlStateManager.scale(nameScale, nameScale, nameScale);
			this.fontRendererObj.drawString(name, 0, 0, 0xFFF0A0FF);
			GlStateManager.popMatrix();
			
			// -> Status
			name = fey.getMoodSummary();
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + (GUI_DETAILS_WIDTH - (fontRendererObj.getStringWidth(name) * nameScale)) / 2, y + 29, 0);
			GlStateManager.scale(nameScale, nameScale, nameScale);
			this.fontRendererObj.drawString(name, 0, 0, 0xFFE0E0E0);
			GlStateManager.popMatrix();
			
			// -> Activity report
			name = fey.getActivitySummary();
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + (GUI_DETAILS_WIDTH - (fontRendererObj.getStringWidth(name) * nameScale)) / 2, y + 37, 0);
			GlStateManager.scale(nameScale, nameScale, nameScale);
			this.fontRendererObj.drawString(name, 0, 0, 0xFFE0E0E0);
			GlStateManager.popMatrix();
			
			// render preview
			drawRect(x + GUI_DETAILS_WIDTH - (previewMargin + previewSize), y + previewMargin,
					x + GUI_DETAILS_WIDTH - (previewMargin), y + (previewMargin + previewSize),
					0xFFAAAAAA);
			//RenderHelper.disableStandardItemLighting();
			// in render terms, 24 is one block, and scale seems to be how big a block is. So figure out how many blocks
			// the fey is, and then make that fit in 24 units.
			float length = Math.max(fey.height, fey.width);
			int scale = (int) Math.floor((previewSize - 2) / (length));
			GlStateManager.color(1f, 1f, 1f, 1f);
			GuiInventory.drawEntityOnScreen(x + GUI_DETAILS_WIDTH - ((previewSize / 2) + previewMargin),
					y + (previewMargin + previewSize),
					scale, 0, 0, fey);
			
			// Render inventory
			if (fey instanceof IItemCarrierFey) {
				IItemCarrierFey carrier = (IItemCarrierFey) fey;
				ItemStack items[] = carrier.getCarriedItems();
				if (items != null && items.length > 0) {
					int cells = Math.min(5, items.length);
					int offsetX = (GUI_DETAILS_WIDTH - (GUI_INV_CELL_LENGTH * cells)) / 2;
					RenderHelper.enableGUIStandardItemLighting();
					GlStateManager.color(1f, 1f, 1f, 1f);
					for (int i = 0; i < cells; i++) {
						int cellX = x + offsetX + (i * GUI_INV_CELL_LENGTH);
						int cellY = y + 62;
						mc.getTextureManager().bindTexture(TEXT);
						Gui.drawModalRectWithCustomSizedTexture(cellX, cellY,
								GUI_TEXT_LIST_ITEM_HOFFSET, GUI_TEXT_LIST_ITEM_vOFFSET + GUI_LIST_ITEM_HEIGHT,
								GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH, 256, 256);
						GlStateManager.enableDepth();
			            this.itemRender.renderItemAndEffectIntoGUI(this.mc.thePlayer, items[i], cellX + 1, cellY + 1);
			            this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, items[i], cellX + 1, cellY + 1, null);
					}
				}
			}
		}
		
		private void drawSlots() {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			GlStateManager.pushMatrix();
			GlStateManager.translate(horizontalMargin, verticalMargin, 0);
			for (ResidentSlot slot : container.residentSlots) {
				if (slot.isActive()) {
					float scale = (slot.isSoul ? (12f / 16f) : 1f);
					GlStateManager.color(1f, 1f, 1f, 1f);
					FeySlotIcon.draw(slot, scale);
				}
			}
			for (FeyContainerSlot slot : container.upgradeSlots) {
				float scale = 1f;
				GlStateManager.color(1f, 1f, 1f, 1f);
				FeySlotIcon.draw(slot, scale);
			}
			GlStateManager.popMatrix();
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			int mouseIndex = getListIndexFromMouse(mouseX, mouseY);
			
			setIsItemRender(true);
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			refreshFeyArray();
			drawSummary(horizontalMargin + GUI_INFO_HOFFSET, verticalMargin + GUI_INFO_VOFFSET);
			drawList(horizontalMargin + GUI_LIST_HOFFSET, verticalMargin + GUI_LIST_VOFFSET, feyArray, mouseIndex);
			drawDetails(horizontalMargin + GUI_DETAILS_HOFFSET, verticalMargin + GUI_DETAILS_VOFFSET, getSelected());
			drawSlots();

			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			setIsItemRender(false);
		}
		
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			if (mouseButton == 0) {
				int index = getListIndexFromMouse(mouseX, mouseY);
				if (index != -1) {
					if (selection != -1) {
						container.residentSlots.get(selection + container.specializationIDStart).isSelected = false;
					}
					container.residentSlots.get(index + container.specializationIDStart).isSelected = true;
					this.selection = index;
					return;
				}
			}
			
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		/**
		 * Returns which list item index is under the mouse.
		 * Returns -1 if no valid list item is underneath.
		 * @param mouseX
		 * @param mouseY
		 * @return
		 */
		protected int getListIndexFromMouse(int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			mouseX -= horizontalMargin;
			mouseY -= verticalMargin;
			if (mouseX > GUI_LIST_HOFFSET && mouseX < GUI_LIST_HOFFSET + GUI_LIST_ITEM_WIDTH
					&& mouseY > GUI_LIST_VOFFSET && mouseY < GUI_LIST_VOFFSET + (GUI_LIST_ITEM_HEIGHT * 5)) {
				int index = (mouseY - (GUI_LIST_VOFFSET + 1)) / GUI_LIST_ITEM_HEIGHT;
				if (index < container.home.getSlots() && container.home.getSlotInventory().hasStone(index)) {
					return index;
				}
			}
			
			return -1;
		}
		
		protected @Nullable EntityFeyBase getSelected() {
			if (selection < 0 || selection >= this.feyArray.length) {
				return null;
			}
			
			return feyArray[selection];
		}
		
		protected void refreshFeyArray() {
			long now = System.currentTimeMillis();
			if (feyArray == null || now - feyArrayCacheTimer > 1000) {
				List<EntityFeyBase> list = container.home.getAvailableFeyEntities();
				feyArray = list == null ? new EntityFeyBase[0] : list.toArray(new EntityFeyBase[list.size()]);
				feyArrayCacheTimer = now;
			}
		}
		
		private void setIsItemRender(boolean isRender) {
			for (ResidentSlot slot : this.container.residentSlots) {
				slot.isItemDisplay = isRender;
			}
		}
	}
	
	private static class ResidentSlot extends FeyContainerSlot {

		private final HomeBlockTileEntity te;
		private final HomeBlockSlotInventory inventory;
		private final boolean isSoul;
		
		protected boolean isSelected;
		protected boolean isItemDisplay;
		
		public ResidentSlot(HomeBlockTileEntity te, int slot, int xPosition, int yPosition) {
			super(te.getSlotInventory(), slot, xPosition, yPosition,
					isSoulSlot(te.getSlotInventory(), slot) ? FeySlotType.SOUL : FeySlotType.SPECIALIZATION);
			this.inventory = te.getSlotInventory();
			this.te = te;
			this.isSoul = isSoulSlot(inventory, slot);
		}
		
		protected static boolean isSoulSlot(HomeBlockSlotInventory inventoryIn, int slot) {
			return HomeBlockSlotInventory.isSoulSlot(slot);
		}
		
		protected static boolean isSlotValid(boolean isSoul, boolean isSelected, HomeBlockTileEntity te, HomeBlockSlotInventory inventoryIn, int slot) {
			// Check if the home block is capable of using this slot.
			// If soul slot, check number of slots and our position (and all previous positions, if applicable).
			// Otherwise, check if a soul stone is socketted && this index selected.
			if (isSoul) {
				final int index = HomeBlockSlotInventory.getIndexFromSlot(slot);
				if (index >= te.getSlots() || inventoryIn.hasStone(index)) {
					return false;
				}
				
				// Make sure predecessors are filled
				for (int i = index-1; i >= 0; i--) {
					if (!inventoryIn.hasStone(i)) {
						return false;
					}
				}
				
				return true;
			} else {
				final int index = HomeBlockSlotInventory.getIndexFromSlot(slot);
				return inventoryIn.hasStone(index) && isSelected;
			}
		}
		
		public boolean isActive() {
			return isSlotValid(isSoul, isSelected, te, inventory, this.getSlotIndex());
		}
		
		@Override
		public boolean isItemValid(@Nullable ItemStack stack) {
			if (!inventory.isItemValidForSlot(this.getSlotIndex(), stack)) {
				return false;
			}
			
			if (!super.isItemValid(stack)) {
				return false;
			}
			
			// accept it as long as this slot is active
			return isActive();
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean canBeHovered() {
			return isActive();
		}
		
		@Override
		public boolean canTakeStack(EntityPlayer playerIn) {
			return !isSoul;
		}
		
		@Override
		public ItemStack getStack() {
			if (isSoul && isItemDisplay) {
				return null;
			}
			
			return super.getStack();
		}
	}
}