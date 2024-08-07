package com.smanzana.nostrumfairies.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.FeySlotIcon;
import com.smanzana.nostrumfairies.client.gui.FeySoulIcon;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity.FeyAwayRecord;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity.HomeBlockSlotInventory;
import com.smanzana.nostrummagica.client.gui.container.AutoGuiContainer;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		
		public static final String ID = "home_block";
		
		protected HomeBlockTileEntity home;
		private final int homeIDStart;
		private final List<ResidentSlot> residentSlots;
		private final List<SpecializationSlot> specializationSlots;
		private final List<FeyStoneContainerSlot> upgradeSlots;
		
		public HomeBlockContainer(int windowId, PlayerInventory playerInv, HomeBlockTileEntity home) {
			super(FairyContainers.HomeBlock, windowId);
			this.home = home;
						
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
			
			homeIDStart = this.inventorySlots.size();
			upgradeSlots = new ArrayList<>(2);
			
			// Add upgrade slots, which are static
			for (int i = 0; i < 2; i++) {
				FeyStoneContainerSlot slot = new FeyStoneContainerSlot(home.getUpgradeInventory(), i,
						GUI_UPGRADE_HOFFSET,
						GUI_UPGRADE_VOFFSET + (i * (GUI_INV_CELL_LENGTH + 3)),
						FeySlotType.EITHERGRADE);
				this.addSlot(slot);
				upgradeSlots.add(slot);
			}
			
			residentSlots = new ArrayList<>(home.getSlotInventory().getSizeInventory() / 2);
			int i;
			for (i = 0; i < home.getSlotInventory().getSizeInventory(); i++) {
				if (!HomeBlockSlotInventory.isSoulSlot(i)) {
					break;
				}
				
				ResidentSlot slot = new ResidentSlot(home, i,
						GUI_LIST_HOFFSET + ((GUI_LIST_ITEM_WIDTH - 16) / 2),
						GUI_LIST_VOFFSET + (i * GUI_LIST_ITEM_HEIGHT));
				this.addSlot(slot);
				residentSlots.add(slot);
			}
			
			specializationSlots = new ArrayList<>(home.getSlotInventory().getSizeInventory() / 2);
			for (; i < home.getSlotInventory().getSizeInventory(); i++) {
				SpecializationSlot slot = new SpecializationSlot(home, i,
						GUI_DETAILS_HOFFSET + (GUI_DETAILS_WIDTH - (GUI_INV_CELL_LENGTH - 2)) / 2,
						GUI_DETAILS_VOFFSET + GUI_DETAILS_HEIGHT - (GUI_INV_CELL_LENGTH * 2));
				this.addSlot(slot);
				specializationSlots.add(slot);
			}
		}
		
		public static HomeBlockContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			return new HomeBlockContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(HomeBlockTileEntity home) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new HomeBlockContainer(windowId, playerInv, home);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, home);
			});
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			return super.slotClick(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			return super.transferStackInSlot(playerIn, fromSlot);
//			ItemStack prev = ItemStack.EMPTY;	
//			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
//			
//			if (slot != null && slot.getHasStack()) {
//				ItemStack cur = slot.getStack();
//				prev = cur.copy();
//				
//				if (slot.inventory == this.home) {
//					// Trying to take one of our items
//					if (playerIn.inventory.addItemStackToInventory(cur)) {
//						slot.putStack(ItemStack.EMPTY);
//						slot.onTake(playerIn, cur);
//					} else {
//						prev = ItemStack.EMPTY;
//					}
//				} else {
//					// shift-click in player inventory
//					ItemStack leftover = ItemStacks.addItem(home, cur);
//					slot.putStack(leftover.isEmpty() ? ItemStack.EMPTY : leftover);
//					if (!leftover.isEmpty() && leftover.getCount() == prev.getCount()) {
//						prev = ItemStack.EMPTY;
//					}
//				}
//				
//			}
//			
//			return prev;
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.slotNumber < homeIDStart;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class HomeBlockGuiContainer extends AutoGuiContainer<HomeBlockContainer> {

		private HomeBlockContainer container;
		protected int selection = -1;
		
		protected FeyAwayRecord feyArray[];
		private long feyArrayCacheTimer;
		
		public HomeBlockGuiContainer(HomeBlockContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			
			this.xSize = GUI_TEXT_WIDTH;
			this.ySize = GUI_TEXT_HEIGHT;
			feyArray = null;
		}
		
		@Override
		public void init() {
			super.init();
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
		
		private void drawSummary(MatrixStack matrixStackIn, int x, int y) {
			/* Elven Residence - The beautiful rose
			 * 0/1 Residents      Healthy Aether     5% Growth
			 */
			String name = container.home.getName();
			float scale = .75f;
			int count = 0;
			int maxcount = 0;
			for (FeyAwayRecord fey : this.feyArray) {
				if (fey != null) {
					count++;
				}
			}
			for (ResidentSlot slot : container.residentSlots) {
				slot.isItemDisplay = false;
				if (slot.getHasStack()) {
					maxcount++;
				}
				slot.isItemDisplay = true;
			}
			
			matrixStackIn.push();
			matrixStackIn.translate(x + 5, y + 2, 0);
			
			font.drawStringWithShadow(matrixStackIn, name, 0, 0, 0xFFFFFFFF);
			
			matrixStackIn.push();
			matrixStackIn.translate(0, 14, 0);
			matrixStackIn.scale(scale, scale, scale);
			font.drawString(matrixStackIn, String.format("%.0f%% Growth", (container.home.getGrowth() * 100)),
					0, 0, 0xFFA0A0A0);
			matrixStackIn.pop();
			
			
			matrixStackIn.push();
			
			matrixStackIn.translate(0, 26, 0);
			matrixStackIn.scale(scale, scale, scale);
			font.drawString(matrixStackIn, count + "/" + maxcount + " Residents",
					0, 0, 0xFFA0A0A0);
			
			String str = getAetherDescription(container.home.getAetherLevel());
			font.drawString(matrixStackIn, str,
					215 - (font.getStringWidth(str)), 0, 0xFFA0A0A0);
			matrixStackIn.pop();

			matrixStackIn.pop();
		}
		
		private void drawListItem(MatrixStack matrixStackIn, int x, int y, boolean hasStone, boolean mouseOver, @Nullable FeyAwayRecord record) {
			mc.getTextureManager().bindTexture(TEXT);
			blit(matrixStackIn, x, y, GUI_TEXT_LIST_ITEM_HOFFSET + (mouseOver ? GUI_LIST_ITEM_WIDTH : 0), GUI_TEXT_LIST_ITEM_vOFFSET,
					GUI_LIST_ITEM_WIDTH, GUI_LIST_ITEM_HEIGHT);
			
			if (hasStone) {
				if (record == null) {
					// Show a 'VACANT' notice lol
					String str = "Vacant";
					this.font.drawStringWithShadow(matrixStackIn, "Vacant",
							x + (GUI_LIST_ITEM_WIDTH - font.getStringWidth(str)) / 2,
							y + 1 + ((GUI_LIST_ITEM_HEIGHT - font.FONT_HEIGHT) / 2), 0xFFFFFFFF);
				} else {
					// display information about the fey for selection
					String name = record.name;
					if (font.getStringWidth(name) * .75f > GUI_LIST_ITEM_WIDTH - 4) {
						int len = 0;
						int index = 0;
						len += font.getStringWidth("..."); // offset to include ellipses
						while ((len + font.getStringWidth("" + name.charAt(index))) * .75f < (GUI_LIST_ITEM_WIDTH - 4)) {
							len += font.getStringWidth("" + name.charAt(index));
							index++;
						}
						name = name.substring(0, index) + "...";
					}
					
					matrixStackIn.push();
					matrixStackIn.translate(x + 2, y + 1 + ((GUI_LIST_ITEM_HEIGHT - font.FONT_HEIGHT) / 2) / .75f, 0);
					matrixStackIn.scale(.75f, .75f, .75f);
					this.font.drawStringWithShadow(matrixStackIn, name, 0, 0, 0xFFFFFFFF);
					matrixStackIn.pop();
				}
			}
		}
		
		private void drawList(MatrixStack matrixStackIn, int x, int y, int mouseIndex) {
			for (int i = 0; i < container.home.getTotalSlots(); i++) {
				@Nullable FeyAwayRecord fey = (i >= feyArray.length ? null : feyArray[i]);
				drawListItem(matrixStackIn, x, y + (i * GUI_LIST_ITEM_HEIGHT), container.home.getSlotInventory().hasStone(i), mouseIndex == i, fey);
			}
		}
		
		private void drawDetails(MatrixStack matrixStackIn, int x, int y, @Nullable FeyAwayRecord record) {
			if (record == null) {
				return;
			}
			
			int previewSize = 24;
			int previewMargin = 2;
			float nameScale = .75f;
			
			// Details
			int nameSpace;
			if (record.cache != null) {
				nameSpace = GUI_DETAILS_WIDTH - (2 + 2 + previewSize + previewMargin + previewMargin);
			} else {
				nameSpace = GUI_DETAILS_WIDTH - (2 + 2);
			}
			
			// -> Backplate
			RenderFuncs.drawRect(matrixStackIn, x, y, x + GUI_DETAILS_WIDTH, y + previewSize + previewMargin + previewMargin, 0x40000000);

			// -> Name
			String name = record.name;
			if (font.getStringWidth(name) * nameScale > nameSpace) {
				int len = 0;
				int index = 0;
				len += font.getStringWidth("..."); // offset to include ellipses
				while ((len + font.getStringWidth("" + name.charAt(index))) * nameScale < nameSpace) {
					len += font.getStringWidth("" + name.charAt(index));
					index++;
				}
				name = name.substring(0, index) + "...";
			}
			
			matrixStackIn.push();
			matrixStackIn.translate(x + 2 + (nameSpace - (font.getStringWidth(name) * nameScale)) / 2, y + 5, 0);
			matrixStackIn.scale(nameScale, nameScale, nameScale);
			this.font.drawStringWithShadow(matrixStackIn, name, 0, 0, 0xFFFFFFFF);
			matrixStackIn.pop();
			
			if (record.cache != null) {
				EntityFeyBase fey = record.cache;
				// -> Title
				name = fey.getSpecializationName();
				matrixStackIn.push();
				matrixStackIn.translate(x + 2 + (nameSpace - (font.getStringWidth(name) * nameScale)) / 2, y + 5 + 11, 0);
				matrixStackIn.scale(nameScale, nameScale, nameScale);
				this.font.drawString(matrixStackIn, name, 0, 0, 0xFFF0A0FF);
				matrixStackIn.pop();
				
				// -> Status
				name = I18n.format(fey.getMoodSummary());
				matrixStackIn.push();
				matrixStackIn.translate(x + (GUI_DETAILS_WIDTH - (font.getStringWidth(name) * nameScale)) / 2, y + 29, 0);
				matrixStackIn.scale(nameScale, nameScale, nameScale);
				this.font.drawString(matrixStackIn, name, 0, 0, 0xFFE0E0E0);
				matrixStackIn.pop();
				
				// -> Activity report
				name = I18n.format(fey.getActivitySummary());
				matrixStackIn.push();
				matrixStackIn.translate(x + (GUI_DETAILS_WIDTH - (font.getStringWidth(name) * nameScale)) / 2, y + 37, 0);
				matrixStackIn.scale(nameScale, nameScale, nameScale);
				this.font.drawString(matrixStackIn, name, 0, 0, 0xFFE0E0E0);
				matrixStackIn.pop();
				
				// render preview
				RenderFuncs.drawRect(matrixStackIn, x + GUI_DETAILS_WIDTH - (previewMargin + previewSize), y + previewMargin,
						x + GUI_DETAILS_WIDTH - (previewMargin), y + (previewMargin + previewSize),
						0xFFAAAAAA);
				//RenderHelper.disableStandardItemLighting();
				// in render terms, 24 is one block, and scale seems to be how big a block is. So figure out how many blocks
				// the fey is, and then make that fit in 24 units.
				float length = Math.max(fey.getHeight(), fey.getWidth());
				int scale = (int) Math.floor((previewSize - 2) / (length));
				{
					RenderSystem.pushMatrix();
					RenderSystem.multMatrix(matrixStackIn.getLast().getMatrix());
					InventoryScreen.drawEntityOnScreen(x + GUI_DETAILS_WIDTH - ((previewSize / 2) + previewMargin),
						y + (previewMargin + previewSize),
						scale, 0, 0, fey);
					RenderSystem.popMatrix();
				}
				
				// Render inventory
				if (fey instanceof IItemCarrierFey) {
					IItemCarrierFey carrier = (IItemCarrierFey) fey;
					NonNullList<ItemStack> items = carrier.getCarriedItems();
					if (items != null && items.size() > 0) {
						int cells = Math.min(5, items.size());
						int offsetX = (GUI_DETAILS_WIDTH - (GUI_INV_CELL_LENGTH * cells)) / 2;
						for (int i = 0; i < cells; i++) {
							int cellX = x + offsetX + (i * GUI_INV_CELL_LENGTH);
							int cellY = y + 62;
							mc.getTextureManager().bindTexture(TEXT);
							RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, cellX, cellY,
									GUI_TEXT_LIST_ITEM_HOFFSET, GUI_TEXT_LIST_ITEM_vOFFSET + GUI_LIST_ITEM_HEIGHT,
									GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH, 256, 256);
				            Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(this.mc.player, items.get(i), cellX + 1, cellY + 1);
				            Minecraft.getInstance().getItemRenderer().renderItemOverlayIntoGUI(this.font, items.get(i), cellX + 1, cellY + 1, null);
						}
					}
				}
			} else {
				name = "Away";
				matrixStackIn.push();
				matrixStackIn.translate(x + (GUI_DETAILS_WIDTH - (font.getStringWidth(name) * nameScale)) / 2, y + 29, 0);
				matrixStackIn.scale(nameScale, nameScale, nameScale);
				this.font.drawString(matrixStackIn, name, 0, 0, 0xFFE0E0E0);
				matrixStackIn.pop();
			}
		}
		
		private void drawSlots(MatrixStack matrixStackIn) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			matrixStackIn.push();
			matrixStackIn.translate(horizontalMargin, verticalMargin, 0);
			for (ResidentSlot slot : container.residentSlots) {
				if (slot.isActive()) {
					float scale = (12f / 16f) ;
					FeySoulIcon.draw(matrixStackIn, slot, scale);
				}
			}
			for (SpecializationSlot slot : container.specializationSlots) {
				if (slot.isActive()) {
					float scale = 1f;
					FeySlotIcon.draw(matrixStackIn, slot, scale);
				}
			}
			for (FeyStoneContainerSlot slot : container.upgradeSlots) {
				float scale = 1f;
				FeySlotIcon.draw(matrixStackIn, slot, scale);
			}
			matrixStackIn.pop();
		}
		
		private void drawSummaryOverlay(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			//26
			//215 - string length x
			
			if (mouseY > (GUI_LIST_VOFFSET + -5 + -10) && mouseX > 115 && mouseX < (GUI_UPGRADE_HOFFSET - 5) && mouseY < (GUI_LIST_VOFFSET - 5)) {
				this.renderTooltip(matrixStackIn, new StringTextComponent(
						container.home.getAether() + "/" + container.home.getAetherCapacity()
						), mouseX, mouseY);
			}
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			int mouseIndex = getListIndexFromMouse(mouseX, mouseY);
			
			setIsItemRender(true);
			
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			refreshFeyArray();
			drawSummary(matrixStackIn, horizontalMargin + GUI_INFO_HOFFSET, verticalMargin + GUI_INFO_VOFFSET);
			drawList(matrixStackIn, horizontalMargin + GUI_LIST_HOFFSET, verticalMargin + GUI_LIST_VOFFSET, mouseIndex);
			drawDetails(matrixStackIn, horizontalMargin + GUI_DETAILS_HOFFSET, verticalMargin + GUI_DETAILS_VOFFSET, getSelected());
			drawSlots(matrixStackIn);
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
			
			setIsItemRender(false);
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			if (mouseX >= horizontalMargin + GUI_INFO_HOFFSET && mouseX <= horizontalMargin + GUI_UPGRADE_HOFFSET
					&& mouseY >= verticalMargin + GUI_INFO_VOFFSET && mouseY <= verticalMargin + GUI_LIST_VOFFSET) {
				drawSummaryOverlay(matrixStackIn, mouseX - horizontalMargin, mouseY - verticalMargin);
			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			if (mouseButton == 0) {
				int index = getListIndexFromMouse((int) mouseX, (int) mouseY);
				if (index != -1) {
					SpecializationSlot slot;
					if (selection != -1) {
						slot = container.specializationSlots.get(selection);
						slot.isSelected = false;
						slot.xPos = -1000;
					}
					slot = container.specializationSlots.get(index);
					slot.isSelected = true;
					slot.xPos = GUI_DETAILS_HOFFSET + (GUI_DETAILS_WIDTH - (GUI_INV_CELL_LENGTH - 2)) / 2;
					this.selection = index;
					return true;
				}
			}
			
			return super.mouseClicked(mouseX, mouseY, mouseButton);
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
				if (index < container.home.getEffectiveSlots() && container.home.getSlotInventory().hasStone(index)) {
					return index;
				}
			}
			
			return -1;
		}
		
		protected @Nullable FeyAwayRecord getSelected() {
			if (selection < 0 || selection >= this.feyArray.length) {
				return null;
			}
			
			return feyArray[selection];
		}
		
		protected void refreshFeyArray() {
			long now = System.currentTimeMillis();
			if (feyArray == null || now - feyArrayCacheTimer > 1000) {
				
				List<FeyAwayRecord> records = container.home.getFeySlots();
				//Collections.sort(records, (l, r) -> { return l.name.compareTo(r.name); });
				
				feyArray = records.toArray(new FeyAwayRecord[records.size()]);
				feyArrayCacheTimer = now;
			}
		}
		
		private void setIsItemRender(boolean isRender) {
			for (ResidentSlot slot : this.container.residentSlots) {
				slot.isItemDisplay = isRender;
			}
		}
	}
	
	private static class ResidentSlot extends FeySoulContainerSlot {

		private final HomeBlockTileEntity te;
		private final HomeBlockSlotInventory inventory;
		
		protected boolean isItemDisplay;
		
		public ResidentSlot(HomeBlockTileEntity te, int slot, int x, int y) {
			super(te.getSlotInventory(), slot, x, y, te.getSlotInventory().getPrimarySoulType());
			this.inventory = te.getSlotInventory();
			this.te = te;
		}
		
		protected static boolean isSlotValid(HomeBlockTileEntity te, HomeBlockSlotInventory inventoryIn, int slot) {
			// Check if the home block is capable of using this slot.
			// If soul slot, check number of slots and our position (and all previous positions, if applicable).
			// Otherwise, check if a soul stone is socketted && this index selected.
			final int index = HomeBlockSlotInventory.getIndexFromSlot(slot);
			if (index >= te.getTotalSlots() || inventoryIn.hasStone(index)) {
				return false;
			}
			
			// Make sure predecessors are filled
			for (int i = index-1; i >= 0; i--) {
				if (!inventoryIn.hasStone(i)) {
					return false;
				}
			}
			
			return true;
		}
		
		public boolean isActive() {
			return isSlotValid(te, inventory, this.getSlotIndex());
		}
		
		@Override
		public boolean isItemValid(@Nonnull ItemStack stack) {
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
		@OnlyIn(Dist.CLIENT)
		public boolean isEnabled() {
			return isActive();
		}
		
		@Override
		public boolean canTakeStack(PlayerEntity playerIn) {
			return false;
		}
		
		@Override
		public ItemStack getStack() {
			if (isItemDisplay) {
				return ItemStack.EMPTY;
			}
			
			return super.getStack();
		}
		
		@Override
		public void putStack(@Nonnull ItemStack stack) {
			EntityFeyBase spawned = null;
			
			if (!stack.isEmpty() && te.getWorld() != null && !te.getWorld().isRemote) {
				if (FeySoulStone.HasStoredFey(stack)) {
					// They put in a soul stone and it has a fey in it. Automatically spawn them and add them
					// to the entity list
					World world = te.getWorld();
					BlockPos spot = null;
					BlockPos center = te.getPos();
					for (BlockPos pos : new BlockPos[] {center.north(), center.south(), center.west(), center.east()}) {
						BlockState state = world.getBlockState(pos);
						if (!state.getMaterial().blocksMovement()) {
							spot = pos;
							break;
						}
					}
					
					if (spot == null) {
						spot = center.offset(Direction.UP, 6);
					}
					spawned = FeySoulStone.spawnStoredEntity(stack, te.getWorld(), spot.getX() + .5, spot.getY(), spot.getZ() + .5);
					stack = FeySoulStone.clearEntity(stack);
				}
			}
			
			super.putStack(stack);
			
			if (spawned != null) {
				//te.addResident(spawned);
				spawned.setHome(te.getPos());
			}
		}
	}
	
	private static class SpecializationSlot extends FeyStoneContainerSlot {

		private final HomeBlockTileEntity te;
		private final HomeBlockSlotInventory inventory;
		
		protected boolean isSelected;
		
		public SpecializationSlot(HomeBlockTileEntity te, int slot, int x, int y) {
			super(te.getSlotInventory(), slot, x, y, FeySlotType.SPECIALIZATION);
			this.inventory = te.getSlotInventory();
			this.te = te;
			isSelected = (te.getWorld().isRemote() ? false : true);
		}
		
		protected static boolean isSlotValid(boolean isSelected, HomeBlockTileEntity te, HomeBlockSlotInventory inventoryIn, int slot) {
			final int index = HomeBlockSlotInventory.getIndexFromSlot(slot);
			return inventoryIn.hasStone(index) && isSelected;
		}
		
		public boolean isActive() {
			return isSlotValid(isSelected, te, inventory, this.getSlotIndex());
		}
		
		@Override
		public boolean isItemValid(@Nonnull ItemStack stack) {
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
		@OnlyIn(Dist.CLIENT)
		public boolean isEnabled() {
			return isActive();
		}
		
		@Override
		public boolean canTakeStack(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public ItemStack getStack() {
			if (!isSelected) {
				return ItemStack.EMPTY;
			}
			return super.getStack();
		}
	}
}
