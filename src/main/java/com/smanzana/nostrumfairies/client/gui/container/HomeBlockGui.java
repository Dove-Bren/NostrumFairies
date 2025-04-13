package com.smanzana.nostrumfairies.client.gui.container;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

	public static class HomeBlockContainer extends AbstractContainerMenu {
		
		public static final String ID = "home_block";
		
		protected HomeBlockTileEntity home;
		private final int homeIDStart;
		private final List<ResidentSlot> residentSlots;
		private final List<SpecializationSlot> specializationSlots;
		private final List<FeyStoneContainerSlot> upgradeSlots;
		
		public HomeBlockContainer(int windowId, Inventory playerInv, HomeBlockTileEntity home) {
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
			
			homeIDStart = this.slots.size();
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
			
			residentSlots = new ArrayList<>(home.getSlotInventory().getContainerSize() / 2);
			int i;
			for (i = 0; i < home.getSlotInventory().getContainerSize(); i++) {
				if (!HomeBlockSlotInventory.isSoulSlot(i)) {
					break;
				}
				
				ResidentSlot slot = new ResidentSlot(home, i,
						GUI_LIST_HOFFSET + ((GUI_LIST_ITEM_WIDTH - 16) / 2),
						GUI_LIST_VOFFSET + (i * GUI_LIST_ITEM_HEIGHT));
				this.addSlot(slot);
				residentSlots.add(slot);
			}
			
			specializationSlots = new ArrayList<>(home.getSlotInventory().getContainerSize() / 2);
			for (; i < home.getSlotInventory().getContainerSize(); i++) {
				SpecializationSlot slot = new SpecializationSlot(home, i,
						GUI_DETAILS_HOFFSET + (GUI_DETAILS_WIDTH - (GUI_INV_CELL_LENGTH - 2)) / 2,
						GUI_DETAILS_VOFFSET + GUI_DETAILS_HEIGHT - (GUI_INV_CELL_LENGTH * 2));
				this.addSlot(slot);
				specializationSlots.add(slot);
			}
		}
		
		public static HomeBlockContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
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
		public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
			super.clicked(slotId, dragType, clickTypeIn, player);
		}
		
		@Override
		public ItemStack quickMoveStack(Player playerIn, int fromSlot) {
			return ItemStack.EMPTY;
			//return super.quickMoveStack(playerIn, fromSlot);
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
		public boolean stillValid(Player playerIn) {
			return true;
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return slotIn.index < homeIDStart;
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class HomeBlockGuiContainer extends AutoGuiContainer<HomeBlockContainer> {

		private HomeBlockContainer container;
		protected int selection = -1;
		
		protected FeyAwayRecord feyArray[];
		private long feyArrayCacheTimer;
		
		public HomeBlockGuiContainer(HomeBlockContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			this.container = container;
			
			this.imageWidth = GUI_TEXT_WIDTH;
			this.imageHeight = GUI_TEXT_HEIGHT;
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
		
		private void drawSummary(PoseStack matrixStackIn, int x, int y) {
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
				if (slot.hasItem()) {
					maxcount++;
				}
				slot.isItemDisplay = true;
			}
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(x + 5, y + 2, 0);
			
			font.drawShadow(matrixStackIn, name, 0, 0, 0xFFFFFFFF);
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 14, 0);
			matrixStackIn.scale(scale, scale, scale);
			font.draw(matrixStackIn, String.format("%.0f%% Growth", (container.home.getGrowth() * 100)),
					0, 0, 0xFFA0A0A0);
			matrixStackIn.popPose();
			
			
			matrixStackIn.pushPose();
			
			matrixStackIn.translate(0, 26, 0);
			matrixStackIn.scale(scale, scale, scale);
			font.draw(matrixStackIn, count + "/" + maxcount + " Residents",
					0, 0, 0xFFA0A0A0);
			
			String str = getAetherDescription(container.home.getAetherLevel());
			font.draw(matrixStackIn, str,
					215 - (font.width(str)), 0, 0xFFA0A0A0);
			matrixStackIn.popPose();

			matrixStackIn.popPose();
		}
		
		private void drawListItem(PoseStack matrixStackIn, int x, int y, boolean hasStone, boolean mouseOver, @Nullable FeyAwayRecord record) {
			RenderSystem.setShaderTexture(0, TEXT);
			blit(matrixStackIn, x, y, GUI_TEXT_LIST_ITEM_HOFFSET + (mouseOver ? GUI_LIST_ITEM_WIDTH : 0), GUI_TEXT_LIST_ITEM_vOFFSET,
					GUI_LIST_ITEM_WIDTH, GUI_LIST_ITEM_HEIGHT);
			
			if (hasStone) {
				if (record == null) {
					// Show a 'VACANT' notice lol
					String str = "Vacant";
					this.font.drawShadow(matrixStackIn, "Vacant",
							x + (GUI_LIST_ITEM_WIDTH - font.width(str)) / 2,
							y + 1 + ((GUI_LIST_ITEM_HEIGHT - font.lineHeight) / 2), 0xFFFFFFFF);
				} else {
					// display information about the fey for selection
					String name = record.name;
					if (font.width(name) * .75f > GUI_LIST_ITEM_WIDTH - 4) {
						int len = 0;
						int index = 0;
						len += font.width("..."); // offset to include ellipses
						while ((len + font.width("" + name.charAt(index))) * .75f < (GUI_LIST_ITEM_WIDTH - 4)) {
							len += font.width("" + name.charAt(index));
							index++;
						}
						name = name.substring(0, index) + "...";
					}
					
					matrixStackIn.pushPose();
					matrixStackIn.translate(x + 2, y + 1 + ((GUI_LIST_ITEM_HEIGHT - font.lineHeight) / 2) / .75f, 0);
					matrixStackIn.scale(.75f, .75f, .75f);
					this.font.drawShadow(matrixStackIn, name, 0, 0, 0xFFFFFFFF);
					matrixStackIn.popPose();
				}
			}
		}
		
		private void drawList(PoseStack matrixStackIn, int x, int y, int mouseIndex) {
			for (int i = 0; i < container.home.getTotalSlots(); i++) {
				@Nullable FeyAwayRecord fey = (i >= feyArray.length ? null : feyArray[i]);
				drawListItem(matrixStackIn, x, y + (i * GUI_LIST_ITEM_HEIGHT), container.home.getSlotInventory().hasStone(i), mouseIndex == i, fey);
			}
		}
		
		private void drawDetails(PoseStack matrixStackIn, int x, int y, @Nullable FeyAwayRecord record) {
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
			if (font.width(name) * nameScale > nameSpace) {
				int len = 0;
				int index = 0;
				len += font.width("..."); // offset to include ellipses
				while ((len + font.width("" + name.charAt(index))) * nameScale < nameSpace) {
					len += font.width("" + name.charAt(index));
					index++;
				}
				name = name.substring(0, index) + "...";
			}
			
			matrixStackIn.pushPose();
			matrixStackIn.translate(x + 2 + (nameSpace - (font.width(name) * nameScale)) / 2, y + 5, 0);
			matrixStackIn.scale(nameScale, nameScale, nameScale);
			this.font.drawShadow(matrixStackIn, name, 0, 0, 0xFFFFFFFF);
			matrixStackIn.popPose();
			
			if (record.cache != null) {
				EntityFeyBase fey = record.cache;
				// -> Title
				name = fey.getSpecializationName();
				matrixStackIn.pushPose();
				matrixStackIn.translate(x + 2 + (nameSpace - (font.width(name) * nameScale)) / 2, y + 5 + 11, 0);
				matrixStackIn.scale(nameScale, nameScale, nameScale);
				this.font.draw(matrixStackIn, name, 0, 0, 0xFFF0A0FF);
				matrixStackIn.popPose();
				
				// -> Status
				name = I18n.get(fey.getMoodSummary());
				matrixStackIn.pushPose();
				matrixStackIn.translate(x + (GUI_DETAILS_WIDTH - (font.width(name) * nameScale)) / 2, y + 29, 0);
				matrixStackIn.scale(nameScale, nameScale, nameScale);
				this.font.draw(matrixStackIn, name, 0, 0, 0xFFE0E0E0);
				matrixStackIn.popPose();
				
				// -> Activity report
				name = I18n.get(fey.getActivitySummary());
				matrixStackIn.pushPose();
				matrixStackIn.translate(x + (GUI_DETAILS_WIDTH - (font.width(name) * nameScale)) / 2, y + 37, 0);
				matrixStackIn.scale(nameScale, nameScale, nameScale);
				this.font.draw(matrixStackIn, name, 0, 0, 0xFFE0E0E0);
				matrixStackIn.popPose();
				
				// render preview
				RenderFuncs.drawRect(matrixStackIn, x + GUI_DETAILS_WIDTH - (previewMargin + previewSize), y + previewMargin,
						x + GUI_DETAILS_WIDTH - (previewMargin), y + (previewMargin + previewSize),
						0xFFAAAAAA);
				//RenderHelper.disableStandardItemLighting();
				// in render terms, 24 is one block, and scale seems to be how big a block is. So figure out how many blocks
				// the fey is, and then make that fit in 24 units.
				float length = Math.max(fey.getBbHeight(), fey.getBbWidth());
				int scale = (int) Math.floor((previewSize - 2) / (length));
				{
					RenderSystem.backupProjectionMatrix();
					RenderSystem.getProjectionMatrix().multiply(matrixStackIn.last().pose());
					RenderSystem.applyModelViewMatrix();
					InventoryScreen.renderEntityInInventory(x + GUI_DETAILS_WIDTH - ((previewSize / 2) + previewMargin),
						y + (previewMargin + previewSize),
						scale, 0, 0, fey);
					RenderSystem.restoreProjectionMatrix();
					RenderSystem.applyModelViewMatrix();
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
							RenderSystem.setShaderTexture(0, TEXT);
							RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, cellX, cellY,
									GUI_TEXT_LIST_ITEM_HOFFSET, GUI_TEXT_LIST_ITEM_vOFFSET + GUI_LIST_ITEM_HEIGHT,
									GUI_INV_CELL_LENGTH, GUI_INV_CELL_LENGTH, 256, 256);
				            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(this.mc.player, items.get(i), cellX + 1, cellY + 1, 0);
				            Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(this.font, items.get(i), cellX + 1, cellY + 1, null);
						}
					}
				}
			} else {
				name = "Away";
				matrixStackIn.pushPose();
				matrixStackIn.translate(x + (GUI_DETAILS_WIDTH - (font.width(name) * nameScale)) / 2, y + 29, 0);
				matrixStackIn.scale(nameScale, nameScale, nameScale);
				this.font.draw(matrixStackIn, name, 0, 0, 0xFFE0E0E0);
				matrixStackIn.popPose();
			}
		}
		
		private void drawSlots(PoseStack matrixStackIn) {
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			matrixStackIn.pushPose();
			matrixStackIn.translate(horizontalMargin, verticalMargin, 0);
			for (ResidentSlot slot : container.residentSlots) {
				if (slot.isSlotActive()) {
					float scale = (12f / 16f) ;
					FeySoulIcon.draw(matrixStackIn, slot, scale);
				}
			}
			for (SpecializationSlot slot : container.specializationSlots) {
				if (slot.isSlotActive()) {
					float scale = 1f;
					FeySlotIcon.draw(matrixStackIn, slot, scale);
				}
			}
			for (FeyStoneContainerSlot slot : container.upgradeSlots) {
				float scale = 1f;
				FeySlotIcon.draw(matrixStackIn, slot, scale);
			}
			matrixStackIn.popPose();
		}
		
		private void drawSummaryOverlay(PoseStack matrixStackIn, int mouseX, int mouseY) {
			//26
			//215 - string length x
			
			if (mouseY > (GUI_LIST_VOFFSET + -5 + -10) && mouseX > 115 && mouseX < (GUI_UPGRADE_HOFFSET - 5) && mouseY < (GUI_LIST_VOFFSET - 5)) {
				this.renderTooltip(matrixStackIn, new TextComponent(
						container.home.getAether() + "/" + container.home.getAetherCapacity()
						), mouseX, mouseY);
			}
		}
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			int mouseIndex = getListIndexFromMouse(mouseX, mouseY);
			
			setIsItemRender(true);
			
			RenderSystem.setShaderTexture(0, TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, 256, 256);
			
			refreshFeyArray();
			drawSummary(matrixStackIn, horizontalMargin + GUI_INFO_HOFFSET, verticalMargin + GUI_INFO_VOFFSET);
			drawList(matrixStackIn, horizontalMargin + GUI_LIST_HOFFSET, verticalMargin + GUI_LIST_VOFFSET, mouseIndex);
			drawDetails(matrixStackIn, horizontalMargin + GUI_DETAILS_HOFFSET, verticalMargin + GUI_DETAILS_VOFFSET, getSelected());
			drawSlots(matrixStackIn);
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			//super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
			
			setIsItemRender(false);
			
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
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
						slot.x = -1000;
					}
					slot = container.specializationSlots.get(index);
					slot.isSelected = true;
					slot.x = GUI_DETAILS_HOFFSET + (GUI_DETAILS_WIDTH - (GUI_INV_CELL_LENGTH - 2)) / 2;
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
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
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
		
		public boolean isSlotActive() {
			return isSlotValid(te, inventory, this.getSlotIndex());
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack stack) {
			if (!inventory.canPlaceItem(this.getSlotIndex(), stack)) {
				return false;
			}
			
			if (!super.mayPlace(stack)) {
				return false;
			}
			
			// accept it as long as this slot is active
			return isSlotActive();
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public boolean isActive() {
			return isSlotActive();
		}
		
		@Override
		public boolean mayPickup(Player playerIn) {
			return false;
		}
		
		@Override
		public ItemStack getItem() {
			if (isItemDisplay) {
				return ItemStack.EMPTY;
			}
			
			return super.getItem();
		}
		
		@Override
		public void set(@Nonnull ItemStack stack) {
			EntityFeyBase spawned = null;
			
			if (!stack.isEmpty() && te.getLevel() != null && !te.getLevel().isClientSide) {
				if (FeySoulStone.HasStoredFey(stack)) {
					// They put in a soul stone and it has a fey in it. Automatically spawn them and add them
					// to the entity list
					Level world = te.getLevel();
					BlockPos spot = null;
					BlockPos center = te.getBlockPos();
					for (BlockPos pos : new BlockPos[] {center.north(), center.south(), center.west(), center.east()}) {
						BlockState state = world.getBlockState(pos);
						if (!state.getMaterial().blocksMotion()) {
							spot = pos;
							break;
						}
					}
					
					if (spot == null) {
						spot = center.relative(Direction.UP, 6);
					}
					spawned = FeySoulStone.spawnStoredEntity(stack, te.getLevel(), spot.getX() + .5, spot.getY(), spot.getZ() + .5);
					stack = FeySoulStone.clearEntity(stack);
				}
			}
			
			super.set(stack);
			
			if (spawned != null) {
				//te.addResident(spawned);
				spawned.setHome(te.getBlockPos());
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
			isSelected = (te.getLevel().isClientSide() ? false : true);
		}
		
		protected static boolean isSlotValid(boolean isSelected, HomeBlockTileEntity te, HomeBlockSlotInventory inventoryIn, int slot) {
			final int index = HomeBlockSlotInventory.getIndexFromSlot(slot);
			return inventoryIn.hasStone(index) && isSelected;
		}
		
		public boolean isSlotActive() {
			return isSlotValid(isSelected, te, inventory, this.getSlotIndex());
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack stack) {
			if (!inventory.canPlaceItem(this.getSlotIndex(), stack)) {
				return false;
			}
			
			if (!super.mayPlace(stack)) {
				return false;
			}
			
			// accept it as long as this slot is active
			return isSlotActive();
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public boolean isActive() {
			return isSlotActive();
		}
		
		@Override
		public boolean mayPickup(Player playerIn) {
			return true;
		}
		
		@Override
		public ItemStack getItem() {
			if (!isSelected) {
				return ItemStack.EMPTY;
			}
			return super.getItem();
		}
	}
}
