package com.smanzana.nostrumfairies.client.gui.container;

import java.io.IOException;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.INostrumFeyCapability;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui.FairyScreenContainer.HideableSlot;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory.FairyCastTarget;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory.FairyPlacementTarget;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.FairyGuiActionMessage;
import com.smanzana.nostrumfairies.network.messages.FairyGuiActionMessage.GuiAction;
import com.smanzana.nostrummagica.client.gui.container.AutoContainer;
import com.smanzana.nostrummagica.utils.Inventories;

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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FairyScreenGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/fairy_container.png");
	private static final int GUI_TEXT_WIDTH = 180;
	private static final int GUI_TEXT_HEIGHT = 168;
	private static final int GUI_WIDTH = 180;
	private static final int GUI_HEIGHT = 168;
	private static final int GUI_PLAYER_INV_HOFFSET = 10;
	private static final int GUI_PLAYER_INV_VOFFSET = 86;
	private static final int GUI_HOTBAR_INV_HOFFSET = 10;
	private static final int GUI_HOTBAR_INV_VOFFSET = 144;
	private static final int GUI_ATTACK_SLOT_HOFFSET = 8;
	private static final int GUI_ATTACK_SLOT_VOFFSET = 18;
	private static final int GUI_BUILD_SLOT_HOFFSET = 64;
	private static final int GUI_BUILD_SLOT_VOFFSET = 18;
	private static final int GUI_LOGISTICS_SLOT_HOFFSET = 120;
	private static final int GUI_LOGISTICS_SLOT_VOFFSET = 18;
	
	private static final int SIDEBAR_LOGISTICS_TEXT_HOFFSET = 180;
	private static final int SIDEBAR_LOGISTICS_TEXT_VOFFSET = 0;
	private static final int SIDEBAR_LOGISTICS_TEXT_WIDTH = 76;
	private static final int SIDEBAR_LOGISTICS_TEXT_HEIGHT = 168;
	private static final int SIDEBAR_LOGISTICS_WIDTH = 76;
	private static final int SIDEBAR_LOGISTICS_HEIGHT = 168;
	private static final int SIDEBAR_LOGISTICS_HOFFSET = -SIDEBAR_LOGISTICS_WIDTH;
	private static final int SIDEBAR_LOGISTICS_VOFFSET = 0;
	private static final int SIDEBAR_LOGISTICS_PULL_SLOT_HOFFSET = 12;
	private static final int SIDEBAR_LOGISTICS_PULL_SLOT_VOFFSET = 36;
	private static final int SIDEBAR_LOGISTICS_PUSH_SLOT_HOFFSET = 48;
	private static final int SIDEBAR_LOGISTICS_PUSH_SLOT_VOFFSET = 36;
	private static final int SIDEBAR_LOGISTICS_GEM_SLOT_HOFFSET = 30;
	private static final int SIDEBAR_LOGISTICS_GEM_SLOT_VOFFSET = 147;
	
	private static final int SIDEBAR_ATTACK_TEXT_HOFFSET = 180;
	private static final int SIDEBAR_ATTACK_TEXT_VOFFSET = 168;
	private static final int SIDEBAR_ATTACK_TEXT_WIDTH = 76;
	private static final int SIDEBAR_ATTACK_TEXT_HEIGHT = 76;
	private static final int SIDEBAR_ATTACK_WIDTH = 76;
	private static final int SIDEBAR_ATTACK_HEIGHT = 76;
	private static final int SIDEBAR_ATTACK_HOFFSET = -SIDEBAR_ATTACK_WIDTH;
	private static final int SIDEBAR_ATTACK_VOFFSET = 0;
	private static final int SIDEBAR_ATTACK_SCROLL_SLOT_HOFFSET = 12;
	private static final int SIDEBAR_ATTACK_SCROLL_SLOT_VOFFSET = 34;
	private static final int SIDEBAR_ATTACK_TARGET_BUTTON_HOFFSET = 34;
	private static final int SIDEBAR_ATTACK_TARGET_BUTTON_VOFFSET = SIDEBAR_ATTACK_SCROLL_SLOT_VOFFSET + 2;
	private static final int SIDEBAR_ATTACK_PLACEMENT_BUTTON_HOFFSET = SIDEBAR_ATTACK_TARGET_BUTTON_HOFFSET + 16;
	private static final int SIDEBAR_ATTACK_PLACEMENT_BUTTON_VOFFSET = SIDEBAR_ATTACK_TARGET_BUTTON_VOFFSET;
	private static final int SIDEBAR_ATTACK_HEALTH_HOFFSET = 6;
	private static final int SIDEBAR_ATTACK_HEALTH_VOFFSET = 56;
	private static final int SIDEBAR_ATTACK_HEALTH_WIDTH = 64;
	private static final int SIDEBAR_ATTACK_HEALTH_HEIGHT = 4;
	private static final int SIDEBAR_ATTACK_ENERGY_HOFFSET = 6;
	private static final int SIDEBAR_ATTACK_ENERGY_VOFFSET = 63;
	private static final int SIDEBAR_ATTACK_ENERGY_WIDTH = 64;
	private static final int SIDEBAR_ATTACK_ENERGY_HEIGHT = 2;
	
	private static final int ICON_SLOTHELPER_TEXT_HOFFSET = 0;
	private static final int ICON_SLOTHELPER_TEXT_VOFFSET = 168;
	private static final int ICON_SLOTHELPER_TEXT_WIDTH = 14;
	private static final int ICON_SLOTHELPER_TEXT_HEIGHT = 14;
	private static final int ICON_SLOTHELPER_WIDTH = 14;
	private static final int ICON_SLOTHELPER_HEIGHT = 14;
	private static final int ICON_TARGET_TEXT_HOFFSET = 0;
	private static final int ICON_TARGET_TEXT_VOFFSET = 184;
	private static final int ICON_TARGET_TEXT_WIDTH = 32;
	private static final int ICON_TARGET_TEXT_HEIGHT = 32;
	private static final int ICON_TARGET_WIDTH = ICON_SLOTHELPER_TEXT_WIDTH - 2;
	private static final int ICON_TARGET_HEIGHT = ICON_SLOTHELPER_TEXT_HEIGHT - 2;
	private static final int ICON_PLACEMENT_TEXT_HOFFSET = 96;
	private static final int ICON_PLACEMENT_TEXT_VOFFSET = 184;
	private static final int ICON_PLACEMENT_TEXT_WIDTH = 32;
	private static final int ICON_PLACEMENT_TEXT_HEIGHT = 32;
	private static final int ICON_PLACEMENT_WIDTH = ICON_SLOTHELPER_WIDTH - 2;
	private static final int ICON_PLACEMENT_HEIGHT = ICON_SLOTHELPER_HEIGHT - 2;
	
	public static class FairyScreenContainer extends AutoContainer {
		
		protected FairyHolderInventory inv;
		protected INostrumFeyCapability capability;
		
		protected HideableSlot[] scrollSlots;
		
		protected HideableSlot[] pullSlots;
		protected HideableSlot[] pushSlots;
		protected HideableSlot gemSlot;
		
		public FairyScreenContainer(IInventory playerInv, FairyHolderInventory chest, INostrumFeyCapability capability) { 
			super(chest);
			this.inv = chest;
			this.capability = capability;
			this.scrollSlots = new HideableSlot[chest.getAttackConfigSize()];
			this.pullSlots = new HideableSlot[chest.getPullTemplateSize()];
			this.pushSlots = new HideableSlot[chest.getPushTemplateSize()];
			
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
			
			// attack
			if (capability.attackFairyUnlocked()) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						this.addSlotToContainer(new Slot(chest, i * 3 + j, GUI_ATTACK_SLOT_HOFFSET + j * 18, GUI_ATTACK_SLOT_VOFFSET + i * 18) {
							public boolean isItemValid(@Nullable ItemStack stack) {
						        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
						    }
						});
					}
				}
			}
			
			// build
			if (capability.builderFairyUnlocked()) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						this.addSlotToContainer(new Slot(chest, (9) + i * 3 + j, GUI_BUILD_SLOT_HOFFSET + j * 18, GUI_BUILD_SLOT_VOFFSET + i * 18) {
							public boolean isItemValid(@Nullable ItemStack stack) {
						        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
						    }
						});
					}
				}
			}
			
			// logistics
			if (capability.logisticsFairyUnlocked()) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						// TODO fancy slots that show the 'FETCH' slot contents in the corner?
						this.addSlotToContainer(new Slot(chest, (18) + i * 3 + j, GUI_LOGISTICS_SLOT_HOFFSET + j * 18, GUI_LOGISTICS_SLOT_VOFFSET + i * 18) {
							public boolean isItemValid(@Nullable ItemStack stack) {
						        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
						    }
						});
					}
				}
			}
			
			// attack fairy slots and buttons
			if (capability.attackFairyUnlocked()) {
				for (int i = 0; i < chest.getAttackConfigSize(); i++) {
					this.scrollSlots[i] = new HideableSlot(chest, (27) + i,
							SIDEBAR_ATTACK_HOFFSET + SIDEBAR_ATTACK_SCROLL_SLOT_HOFFSET,
							SIDEBAR_ATTACK_VOFFSET + SIDEBAR_ATTACK_SCROLL_SLOT_VOFFSET) {
						public boolean isItemValid(@Nullable ItemStack stack) {
					        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
					    }
					};
					this.addSlotToContainer(scrollSlots[i]);
				}
			}
			
			// logistics templates
			if (capability.logisticsFairyUnlocked()) {
				for (int i = 0; i < chest.getPullTemplateSize(); i++) {
					this.pullSlots[i] = new HideableSlot(chest, (45) + i,
							SIDEBAR_LOGISTICS_HOFFSET + SIDEBAR_LOGISTICS_PULL_SLOT_HOFFSET,
							SIDEBAR_LOGISTICS_VOFFSET + SIDEBAR_LOGISTICS_PULL_SLOT_VOFFSET + (i * 18)) {
						public boolean isItemValid(@Nullable ItemStack stack) {
					        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
					    }
					};
					this.addSlotToContainer(pullSlots[i]);
					
					this.pushSlots[i] = new HideableSlot(chest, (51) + i,
							SIDEBAR_LOGISTICS_HOFFSET + SIDEBAR_LOGISTICS_PUSH_SLOT_HOFFSET,
							SIDEBAR_LOGISTICS_VOFFSET + SIDEBAR_LOGISTICS_PUSH_SLOT_VOFFSET + (i * 18)) {
						public boolean isItemValid(@Nullable ItemStack stack) {
					        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
					    }
					};
					this.addSlotToContainer(pushSlots[i]);
				}
				
				this.gemSlot = new HideableSlot(chest, 57,
						SIDEBAR_LOGISTICS_HOFFSET + SIDEBAR_LOGISTICS_GEM_SLOT_HOFFSET,
						SIDEBAR_LOGISTICS_VOFFSET + SIDEBAR_LOGISTICS_GEM_SLOT_VOFFSET) {
					public boolean isItemValid(@Nullable ItemStack stack) {
				        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
				    }
				};
				this.addSlotToContainer(gemSlot);
			}
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			ItemStack prev = null;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				if (slot.inventory == this.inv) {
					// Trying to take one of our items
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						slot.putStack(null);
						slot.onPickupFromSlot(playerIn, cur);
					} else {
						prev = null;
					}
				} else {
					// shift-click in player inventory
					ItemStack leftover = Inventories.addItem(inv, cur);
					slot.putStack(leftover != null && leftover.stackSize <= 0 ? null : leftover);
					if (leftover != null && leftover.stackSize == prev.stackSize) {
						prev = null;
					}
				}
				
			}
			
			return prev;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return false;
		}
		
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
			Slot slot = (slotId > 0 && slotId < this.inventorySlots.size() ? this.inventorySlots.get(slotId) : null);
			if (slot != null) {
				if (slot instanceof HideableSlot && ((HideableSlot) slot).hidden) {
					return null;
				}
				
				int slotIdx = -1;
				boolean isPull = false;
				
				for (int i = 0; i < pullSlots.length; i++) {
					if (pullSlots[i] == slot) {
						// A pull slot!
						isPull = true;
						slotIdx = i;
						break;
					}
				}
				
				if (slotIdx == -1) {
					for (int i = 0; i < pushSlots.length; i++) {
						if (pushSlots[i] == slot) {
							// A push slot!
							isPull = false;
							slotIdx = i;
							break;
						}
					}
				}
				
				if (slotIdx != -1) {
					// template slot
					if (player.inventory.getItemStack() == null) {
						// empty hand. Right-click?
						if (dragType == 1 && clickTypeIn == ClickType.PICKUP) {
							// If template is present, clear it
							if (isPull) {
								inv.setPullTemplate(slotIdx, null);
							} else {
								inv.setPushTemplate(slotIdx, null);
							}
						}
					} else {
						// Item in hand. Clicking empty output slot?
						if (clickTypeIn == ClickType.PICKUP) {
							// Overwrite template
							ItemStack template = player.inventory.getItemStack().copy();
							if (dragType == 1) { // right click
								template.stackSize = 1;
							}
							if (isPull) {
								inv.setPullTemplate(slotIdx, template);
							} else {
								inv.setPushTemplate(slotIdx, template);
							}
						}
					}
					
					return null;
				}
			}
			return super.slotClick(slotId, dragType, clickTypeIn, player); // TODO hook up templates
		}
		
		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}

		public void handleAction(GuiAction action, int slot, int selection) {
			switch (action) {
			case CHANGE_PLACEMENT:
				FairyPlacementTarget placement = FairyPlacementTarget.values()[selection % FairyPlacementTarget.values().length];
				inv.setFairyPlacementTarget(slot, placement);
				break;
			case CHANGE_TARGET:
				FairyCastTarget target = FairyCastTarget.values()[selection % FairyCastTarget.values().length];
				inv.setFairyCastTarget(slot, target);
				break;
			default:
				break;
			}
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
	public static class FairyScreenGuiContainer extends GuiContainer {

		private FairyScreenContainer container;
		protected TargetButton[] targetButtons;
		protected PlacementButton[] placementButtons;
		
		private int selectedSlot;
		private boolean selectedEmpty;
		private String selectedName;
		private float selectedHealth;
		private float selectedEnergy;
		private boolean showAttack;
		private boolean showLogistics;
		
		public FairyScreenGuiContainer(FairyScreenContainer container) {
			super(container);
			this.container = container;
			this.targetButtons = new TargetButton[container.inv.getAttackConfigSize()];
			this.placementButtons = new PlacementButton[container.inv.getAttackConfigSize()];
			
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
		}
		
		@Override
		public void initGui() {
			super.initGui();
			
			final int horizontalMargin = (width - xSize) / 2;
			final int verticalMargin = (height - ySize) / 2;
			
			if (container.capability.attackFairyUnlocked()) {
				for (int i = 0; i < container.inv.getAttackConfigSize(); i++) {
					targetButtons[i] = new TargetButton(i * 2,
							horizontalMargin + SIDEBAR_ATTACK_HOFFSET + SIDEBAR_ATTACK_TARGET_BUTTON_HOFFSET,
							verticalMargin + SIDEBAR_ATTACK_VOFFSET + SIDEBAR_ATTACK_TARGET_BUTTON_VOFFSET,
							i);
					this.addButton(targetButtons[i]);
					placementButtons[i] = new PlacementButton((i * 2) + 1,
							horizontalMargin + SIDEBAR_ATTACK_HOFFSET + SIDEBAR_ATTACK_PLACEMENT_BUTTON_HOFFSET,
							verticalMargin + SIDEBAR_ATTACK_VOFFSET + SIDEBAR_ATTACK_PLACEMENT_BUTTON_VOFFSET,
							i);
					this.addButton(placementButtons[i]);
				}
			}
			
			for (TargetButton butt : targetButtons) {
				butt.visible = showAttack && selectedSlot == butt.slot;
			}
			for (PlacementButton butt : placementButtons) {
				butt.visible = showAttack && selectedSlot == butt.slot;
			}
			for (HideableSlot slot : container.scrollSlots) {
				slot.hide(!showAttack || selectedSlot != slot.getSlotIndex() - 27);
			}
			for (HideableSlot slot : container.pullSlots) {
				slot.hide(!showLogistics);
			}
			for (HideableSlot slot : container.pushSlots) {
				slot.hide(!showLogistics);
			}
			container.gemSlot.hide(!showLogistics);
		}
		
		@Override
		protected void actionPerformed(GuiButton button) throws IOException {
			if (button.visible) {
				if (button instanceof TargetButton) {
					TargetButton butt = (TargetButton) button;
					FairyHolderInventory.FairyCastTarget current = container.inv.getFairyCastTarget(butt.slot);
					NetworkHandler.getSyncChannel().sendToServer(new FairyGuiActionMessage(
							GuiAction.CHANGE_TARGET, butt.slot, current.ordinal() + 1));
				} else if (button instanceof PlacementButton) {
					PlacementButton butt = (PlacementButton) button;
					FairyHolderInventory.FairyPlacementTarget current = container.inv.getFairyPlacementTarget(butt.slot);
					NetworkHandler.getSyncChannel().sendToServer(new FairyGuiActionMessage(
							GuiAction.CHANGE_PLACEMENT, butt.slot, current.ordinal() + 1));
				}
			}
		}
		
		/**
		 * Reset buttons and set things up for when the provided slot was just clicked.
		 * Slot is expected to be a gael slot
		 * @param slotClicked
		 */
		protected void setButtonsTo(int slotClicked) {
			
			if (slotClicked < 0 || slotClicked > container.inv.getGaelSize() * 3) {
				showAttack = showLogistics = false;
			} else {
				if (FairyHolderInventory.slotIsType(FairyGaelType.ATTACK, slotClicked)) {
					showAttack = true;
					showLogistics = false;
					
					ItemStack gael = container.inv.getStackInSlot(slotClicked);
					this.selectedSlot = slotClicked - 0;
					this.selectedEmpty = (gael == null);
					if (gael != null) {
						this.selectedName = FairyGael.getStoredName(gael);
						this.selectedHealth = FairyGael.getStoredHealth(gael);
						this.selectedEnergy = FairyGael.getStoredEnergy(gael);
					}
				} else if (FairyHolderInventory.slotIsType(FairyGaelType.LOGISTICS, slotClicked)) {
					showAttack = false;
					showLogistics = true;
				} else {
					showAttack = false;
					showLogistics = false;
				}
			}
			
			for (TargetButton butt : targetButtons) {
				butt.visible = showAttack && selectedSlot == butt.slot;
			}
			for (PlacementButton butt : placementButtons) {
				butt.visible = showAttack && selectedSlot == butt.slot;
			}
			for (HideableSlot slot : container.scrollSlots) {
				slot.hide(!showAttack || selectedSlot != slot.getSlotIndex() - 27);
			}
			for (HideableSlot slot : container.pullSlots) {
				slot.hide(!showLogistics);
			}
			for (HideableSlot slot : container.pushSlots) {
				slot.hide(!showLogistics);
			}
			container.gemSlot.hide(!showLogistics);
		}
		
		protected void drawAttackSlide(float partialTicks, int mouseX, int mouseY) {
			final int horizontalMargin = (width - xSize) / 2;
			final int verticalMargin = (height - ySize) / 2;
			final int sidebarX = horizontalMargin + SIDEBAR_ATTACK_HOFFSET;
			final int sidebarY = verticalMargin + SIDEBAR_ATTACK_VOFFSET;
			
			// DRAW BARS
			Gui.drawRect(sidebarX + SIDEBAR_ATTACK_HEALTH_HOFFSET,
					sidebarY + SIDEBAR_ATTACK_HEALTH_VOFFSET,
					sidebarX + SIDEBAR_ATTACK_HEALTH_HOFFSET + SIDEBAR_ATTACK_HEALTH_WIDTH,
					sidebarY + SIDEBAR_ATTACK_HEALTH_VOFFSET + SIDEBAR_ATTACK_HEALTH_HEIGHT,
					0xFF444444);
			
			Gui.drawRect(sidebarX + SIDEBAR_ATTACK_ENERGY_HOFFSET,
					sidebarY + SIDEBAR_ATTACK_ENERGY_VOFFSET,
					sidebarX + SIDEBAR_ATTACK_ENERGY_HOFFSET + SIDEBAR_ATTACK_ENERGY_WIDTH,
					sidebarY + SIDEBAR_ATTACK_ENERGY_VOFFSET + SIDEBAR_ATTACK_ENERGY_HEIGHT,
					0xFF444444);
			
			if (!selectedEmpty) {
				int x = (int) Math.round((float) SIDEBAR_ATTACK_HEALTH_WIDTH * (selectedHealth));
				Gui.drawRect(sidebarX + SIDEBAR_ATTACK_HEALTH_HOFFSET,
						sidebarY + SIDEBAR_ATTACK_HEALTH_VOFFSET,
						sidebarX + SIDEBAR_ATTACK_HEALTH_HOFFSET + x,
						sidebarY + SIDEBAR_ATTACK_HEALTH_VOFFSET + SIDEBAR_ATTACK_HEALTH_HEIGHT,
						0xFFAA0000);
				
				x = (int) Math.round((float) SIDEBAR_ATTACK_ENERGY_WIDTH * (selectedEnergy));
				Gui.drawRect(sidebarX + SIDEBAR_ATTACK_ENERGY_HOFFSET,
						sidebarY + SIDEBAR_ATTACK_ENERGY_VOFFSET,
						sidebarX + SIDEBAR_ATTACK_ENERGY_HOFFSET + x,
						sidebarY + SIDEBAR_ATTACK_ENERGY_VOFFSET + SIDEBAR_ATTACK_ENERGY_HEIGHT,
						0xFF444444);
			}
			
			// draw background
			GlStateManager.color(1f, 1f, 1f, 1f);
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			drawScaledCustomSizeModalRect(sidebarX, sidebarY,
					SIDEBAR_ATTACK_TEXT_HOFFSET, SIDEBAR_ATTACK_TEXT_VOFFSET,
					SIDEBAR_ATTACK_TEXT_WIDTH, SIDEBAR_ATTACK_TEXT_HEIGHT,
					SIDEBAR_ATTACK_WIDTH, SIDEBAR_ATTACK_HEIGHT, 256, 256);
			
			if (!selectedEmpty && selectedName != null) {
				final float scale = .8f;
				final String name;
				if (selectedName.length() > 15) {
					name = selectedName.substring(0, 14) + "...";
				} else {
					name = selectedName;
				}
				GlStateManager.pushMatrix();
				GlStateManager.translate(sidebarX + (SIDEBAR_ATTACK_WIDTH / 2), sidebarY + 4, 0); 
				GlStateManager.scale(scale, scale, scale);
				this.drawCenteredString(this.fontRendererObj,
						name,
						0,
						0,
						0xFFFFFFFF);
				GlStateManager.popMatrix();
				GlStateManager.disableAlpha();
				GlStateManager.disableBlend();
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
			}
		}
		
		protected void drawLogisticsSlide(float partialTicks, int mouseX, int mouseY) {
			final int horizontalMargin = (width - xSize) / 2;
			final int verticalMargin = (height - ySize) / 2;
			//final int sidebarX = horizontalMargin + SIDEBAR_LOGISTICS_HOFFSET;
			//final int sidebarY = verticalMargin + SIDEBAR_LOGISTICS_VOFFSET;
			
			drawScaledCustomSizeModalRect(horizontalMargin + SIDEBAR_LOGISTICS_HOFFSET, verticalMargin + SIDEBAR_LOGISTICS_VOFFSET,
					SIDEBAR_LOGISTICS_TEXT_HOFFSET, SIDEBAR_LOGISTICS_TEXT_VOFFSET,
					SIDEBAR_LOGISTICS_TEXT_WIDTH, SIDEBAR_LOGISTICS_TEXT_HEIGHT,
					SIDEBAR_LOGISTICS_WIDTH, SIDEBAR_LOGISTICS_HEIGHT, 256, 256);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			final int horizontalMargin = (width - xSize) / 2;
			final int verticalMargin = (height - ySize) / 2;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			drawScaledCustomSizeModalRect(horizontalMargin, verticalMargin, 0,0, GUI_TEXT_WIDTH, GUI_TEXT_HEIGHT, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			if (showAttack) {
				drawAttackSlide(partialTicks, mouseX, mouseY);
			} else if (showLogistics) {
				drawLogisticsSlide(partialTicks, mouseX, mouseY);
			}
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			; // TODO draw some selection thing over which category was last selected?
		}
		
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			if (mouseButton == 1 || mouseButton == 0) {
				int size = 0;
				if (container.capability.attackFairyUnlocked()) { size += container.inv.getGaelSize(); }
				if (container.capability.builderFairyUnlocked()) { size += container.inv.getGaelSize(); }
				if (container.capability.logisticsFairyUnlocked()) { size += container.inv.getGaelSize(); }
				for (int i = 0; i < size; i++) {
					Slot slot = container.inventorySlots.get(i + (27 + 9));
					if (this.isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY)) {
						if (mouseButton == 1) {
							this.setButtonsTo(i);
							return;
						} else {
							// they're picking it up
							this.selectedEmpty = true;
						}
					}
				}
			}
			
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		@Override
		protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
			super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		}
		
		@Override
		protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
			if (slot != null && slot instanceof HideableSlot && ((HideableSlot) slot).hidden) {
				return;
			}
			
			super.handleMouseClick(slot, slotId, mouseButton, type);
		}
		
		private final class TargetButton extends GuiButton {

			private final int slot;
			
			public TargetButton(int buttonId, int x, int y, int slot) {
				super(buttonId, x, y, ICON_SLOTHELPER_WIDTH, ICON_SLOTHELPER_HEIGHT, null);
				this.slot = slot;
			}
			
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY) {
				if (this.visible) {
					hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
					
					if (this.hovered) {
						GlStateManager.color(.8f, .8f, .8f, 1.0F);
					} else {
						GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
					}
					GlStateManager.disableBlend();
					GlStateManager.enableBlend();
					mc.getTextureManager().bindTexture(TEXT);
					drawScaledCustomSizeModalRect(this.xPosition, this.yPosition,
							ICON_SLOTHELPER_TEXT_HOFFSET, ICON_SLOTHELPER_TEXT_VOFFSET,
							ICON_SLOTHELPER_TEXT_WIDTH, ICON_SLOTHELPER_TEXT_HEIGHT,
							ICON_SLOTHELPER_WIDTH, ICON_SLOTHELPER_HEIGHT, 256, 256);
					
					final int offsetU;
					final FairyCastTarget target = container.inv.getFairyCastTarget(this.slot);
					switch (target) {
					case OWNER:
						offsetU = (2 * ICON_TARGET_TEXT_WIDTH);
						break;
					case SELF:
						offsetU = 0;
						break;
					case TARGET:
					default:
						offsetU = (1 * ICON_TARGET_TEXT_WIDTH);
						break;
					}
					
					drawScaledCustomSizeModalRect(this.xPosition + 1, this.yPosition + 1,
							ICON_TARGET_TEXT_HOFFSET + offsetU, ICON_TARGET_TEXT_VOFFSET,
							ICON_TARGET_TEXT_WIDTH, ICON_TARGET_TEXT_HEIGHT,
							ICON_TARGET_WIDTH, ICON_TARGET_HEIGHT, 256, 256);
				}
			}
			
		}
		
		private final class PlacementButton extends GuiButton {

			private final int slot;
			
			public PlacementButton(int buttonId, int x, int y, int slot) {
				super(buttonId, x, y, ICON_SLOTHELPER_WIDTH, ICON_SLOTHELPER_HEIGHT, null);
				this.slot = slot;
			}
			
			@Override
			public void drawButton(Minecraft mc, int mouseX, int mouseY) {
				if (this.visible) {
					hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
					
					if (this.hovered) {
						GlStateManager.color(.8f, .8f, .8f, 1.0F);
					} else {
						GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
					}
					GlStateManager.disableBlend();
					GlStateManager.enableBlend();
					mc.getTextureManager().bindTexture(TEXT);
					drawScaledCustomSizeModalRect(this.xPosition, this.yPosition,
							ICON_SLOTHELPER_TEXT_HOFFSET, ICON_SLOTHELPER_TEXT_VOFFSET,
							ICON_SLOTHELPER_TEXT_WIDTH, ICON_SLOTHELPER_TEXT_HEIGHT,
							ICON_SLOTHELPER_WIDTH, ICON_SLOTHELPER_HEIGHT, 256, 256);
					
					final int offsetU;
					final FairyPlacementTarget placement = container.inv.getFairyPlacementTarget(this.slot);
					switch (placement) {
					case MELEE:
					default:
						offsetU = 0;
						break;
					case RANGE:
						offsetU = 1 * ICON_TARGET_TEXT_WIDTH;
						break;
					
					}
					
					drawScaledCustomSizeModalRect(this.xPosition + 1, this.yPosition + 1,
							ICON_PLACEMENT_TEXT_HOFFSET + offsetU, ICON_PLACEMENT_TEXT_VOFFSET,
							ICON_PLACEMENT_TEXT_WIDTH, ICON_PLACEMENT_TEXT_HEIGHT,
							ICON_PLACEMENT_WIDTH, ICON_PLACEMENT_HEIGHT, 256, 256);
				}
			}
			
		}
		
	}
	
}
