package com.smanzana.nostrumfairies.inventory;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.SpellScroll;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants.NBT;

public class FairyHolderInventory implements IInventory {
	
	public static enum FairyCastTarget {
		SELF,
		TARGET,
		OWNER;
		
		private List<String> desc = null;
		private String transName = null;
		
		private FairyCastTarget() {
			
		}
		
		public String getUnlocName() {
			return name().toLowerCase();
		}
		
		public String getName() {
			if (this.transName == null) {
				this.transName = I18n.format("fairytarget." + getUnlocName() + ".name");
			}
			return transName;
		}
		
		public List<String> getDescription() {
			if (this.desc == null) {
				String raw = I18n.format("fairytarget." + getUnlocName() + ".desc", "" + ChatFormatting.DARK_GREEN + ChatFormatting.BOLD, ChatFormatting.RESET);
				String[] lines = raw.split("\\|");
				
				this.desc = Lists.asList("" + ChatFormatting.BLUE + ChatFormatting.BOLD + getName() + ChatFormatting.RESET, lines);
			}
			return desc;
		}
	}
	
	public static enum FairyPlacementTarget {
		MELEE,
		RANGE;
		
		private List<String> desc = null;
		private String transName = null;
		
		private FairyPlacementTarget() {
			
		}
		
		public String getUnlocName() {
			return name().toLowerCase();
		}
		
		public String getName() {
			if (this.transName == null) {
				this.transName = I18n.format("fairyplacement." + getUnlocName() + ".name");
			}
			return transName;
		}
		
		public List<String> getDescription() {
			if (this.desc == null) {
				String raw = I18n.format("fairyplacement." + getUnlocName() + ".desc", "" + ChatFormatting.DARK_GREEN + ChatFormatting.BOLD, ChatFormatting.RESET);
				String[] lines = raw.split("\\|");
				
				this.desc = Lists.asList("" + ChatFormatting.BLUE + ChatFormatting.BOLD + getName() + ChatFormatting.RESET, lines);
			}
			return desc;
		}
	}

	private static final int INV_SIZE_GAEL = 27;
	private static final int INV_SIZE_SCROLL = 9;
	private static final int INV_SIZE_FETCH = 9;
	private static final int INV_SIZE_TEMPLATES = 6 * 2;
	
	private static final int INV_SIZE = INV_SIZE_GAEL + INV_SIZE_SCROLL + INV_SIZE_FETCH + INV_SIZE_TEMPLATES + 1; 
	
	// First 9 are attack, next 9 are build, and last 9 are logistics spots
	private NonNullList<ItemStack> gaelSlots;
	// 9 slots for each of the ATTACK slots (for scroll, target, and placement selections)
	private NonNullList<ItemStack> scrollSlots;
	private FairyCastTarget[] targetSlots;
	private FairyPlacementTarget[] placementSlots;
	// 9 slots for each of the LOGISTICS slots
	private NonNullList<ItemStack> fetchSlots;
	// 6 slots for requests, and 6 for deposits
	private NonNullList<ItemStack> logisticsTemplates;
	// 1 slot for a security pointer to a network
	private ItemStack gemSlot = ItemStack.EMPTY;
	
	private boolean dirty;
	private CompoundNBT nbtCache;
	
	public FairyHolderInventory() {
		clear(); // initializes stuff lol
	}
	
	public static boolean slotIsGael(int slot) {
		return slot >= 0 && slot < INV_SIZE_GAEL;
	}
	
	public static boolean slotIsScroll(int slot) {
		final int offset = INV_SIZE_GAEL;
		return slot >= offset && slot < offset + INV_SIZE_SCROLL; 
	}
	
	public static boolean slotIsFetch(int slot) {
		final int offset = INV_SIZE_GAEL + INV_SIZE_SCROLL;
		return slot >= offset && slot < offset + INV_SIZE_FETCH; 
	}
	
	public static boolean slotIsPullTemplate(int slot) {
		final int offset = INV_SIZE_GAEL + INV_SIZE_SCROLL + INV_SIZE_FETCH;
		return slot >= offset && slot < offset + (INV_SIZE_TEMPLATES/2);
	}
	
	public static boolean slotIsPushTemplate(int slot) {
		final int offset = INV_SIZE_GAEL + INV_SIZE_SCROLL + INV_SIZE_FETCH + (INV_SIZE_TEMPLATES/2);
		return slot >= offset && slot < offset + (INV_SIZE_TEMPLATES/2);
	}
	
	public static boolean slotIsType(FairyGael.FairyGaelType type, int slot) {
		if (!slotIsGael(slot)) {
			return false;
		}
		
		boolean allowed = false;
		switch (type) {
		case ATTACK:
			allowed = (slot < 9);
			break;
		case BUILD:
			allowed = (slot >= 9 && slot < 18);
			break;
		case LOGISTICS:
			allowed = (slot >= 18);
			break;
		}
		return allowed;
	}
	
	/**
	 * Returns the gael in the provided slot.
	 * slot is 0-8 and is unique per type. So ATTACK:1 is a different slot than BUILD:1
	 * @param type
	 * @param slot
	 * @return
	 */
	public @Nonnull ItemStack getGaelByType(FairyGael.FairyGaelType type, int slot) {
		if (slot < 0 || slot >= 9) {
			return ItemStack.EMPTY;
		}
		
		int offset = 0;
		switch (type) {
		case ATTACK:
			offset = 0;
			break;
		case BUILD:
			offset = 9;
			break;
		case LOGISTICS:
			offset = 18;
			break;
		}
		return gaelSlots.get(slot + offset);
	}
	
	public @Nonnull ItemStack getAttackGael(int slot) {
		return getGaelByType(FairyGael.FairyGaelType.ATTACK, slot);
	}
	
	public @Nonnull ItemStack getLogisticsGael(int slot) {
		return getGaelByType(FairyGael.FairyGaelType.LOGISTICS, slot);
	}
	
	public @Nonnull ItemStack getBuildGael(int slot) {
		return getGaelByType(FairyGael.FairyGaelType.BUILD, slot);
	}
	
	public @Nonnull ItemStack getScroll(int slot) {
		if (slot < 0 || slot >= INV_SIZE_SCROLL) {
			return ItemStack.EMPTY;
		}
		
		return scrollSlots.get(slot);
	}
	
	public FairyCastTarget getFairyCastTarget(int slot) {
		if (slot < 0 || slot >= INV_SIZE_SCROLL) {
			return null;
		}
		
		return targetSlots[slot];
	}
	
	public FairyPlacementTarget getFairyPlacementTarget(int slot) {
		if (slot < 0 || slot >= INV_SIZE_SCROLL) {
			return null;
		}
		
		return placementSlots[slot];
	}
	
	public @Nonnull ItemStack getFetchContents(int slot) {
		if (slot < 0 || slot >= INV_SIZE_FETCH) {
			return ItemStack.EMPTY;
		}
		
		return fetchSlots.get(slot);
	}
	
	public @Nonnull ItemStack getPullTemplate(int slot) {
		if (slot < 0 || slot >= (INV_SIZE_TEMPLATES/2)) {
			return ItemStack.EMPTY;
		}
		
		return logisticsTemplates.get(slot);
	}
	
	public @Nonnull ItemStack getPushTemplate(int slot) {
		if (slot < 0 || slot >= (INV_SIZE_TEMPLATES/2)) {
			return ItemStack.EMPTY;
		}
		
		return logisticsTemplates.get((INV_SIZE_TEMPLATES/2) + slot);
	}
	
	public @Nonnull ItemStack getLogisticsGem() {
		return this.gemSlot;
	}
	
	// Could make setters for item slots, but I don't think anything will use them
	
	public void setFairyCastTarget(int slot, FairyCastTarget target) {
		if (slot >= 0 && slot < INV_SIZE_SCROLL && target != null) {
			targetSlots[slot] = target;
			this.markDirty();
		}
	}
	
	public void setFairyPlacementTarget(int slot, FairyPlacementTarget target) {
		if (slot >= 0 && slot < INV_SIZE_SCROLL && target != null) {
			placementSlots[slot] = target;
			this.markDirty();
		}
	}
	
	public void setPullTemplate(int slot, @Nonnull ItemStack template) {
		if (slot < 0 || slot >= INV_SIZE_TEMPLATES / 2) {
			return;
		}
		
		if (!template.isEmpty()) {
			template = template.copy();
		}
		
		this.logisticsTemplates.set(slot, template);
		this.markDirty();
	}
	
	public void setPushTemplate(int slot, @Nonnull ItemStack template) {
		if (slot < 0 || slot >= INV_SIZE_TEMPLATES / 2) {
			return;
		}
		
		if (!template.isEmpty()) {
			template = template.copy();
		}
		
		this.logisticsTemplates.set(INV_SIZE_TEMPLATES / 2 + slot, template);
		this.markDirty();
	}
	
	public void setGaelByType(FairyGaelType type, int slot, @Nonnull ItemStack gael) {
		if (slot < 0 || slot >= 9) {
			return;
		}
		
		int offset = 0;
		switch (type) {
		case ATTACK:
			offset = 0;
			break;
		case BUILD:
			offset = 9;
			break;
		case LOGISTICS:
			offset = 18;
			break;
		}
		
		this.setInventorySlotContents(slot+offset, gael);
	}
	
	@Override
	public String getName() {
		return "Fairy Inventory";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(this.getName());
	}

	@Override
	public int getSizeInventory() {
		return INV_SIZE;
	}
	
	public int getGaelSize() {
		return 9;
	}
	
	public int getAttackConfigSize() {
		return INV_SIZE_SCROLL;
	}
	
	public int getFetchSize() {
		return INV_SIZE_FETCH;
	}
	
	public int getPullTemplateSize() {
		return INV_SIZE_TEMPLATES / 2;
	}
	
	public int getPushTemplateSize() {
		return INV_SIZE_TEMPLATES / 2;
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= INV_SIZE) {
			return ItemStack.EMPTY;
		}
		
		if (index < INV_SIZE_GAEL) {
			return gaelSlots.get(index);			
		}
		index -= INV_SIZE_GAEL;
		
		if (index < INV_SIZE_SCROLL) {
			return scrollSlots.get(index);			
		}
		index -= INV_SIZE_SCROLL;
		
		if (index < INV_SIZE_FETCH) {
			return fetchSlots.get(index);			
		}
		index -= INV_SIZE_FETCH;
		
		if (index < INV_SIZE_TEMPLATES) {
			return logisticsTemplates.get(index);
		}
		index -= INV_SIZE_TEMPLATES;
		
		return gemSlot;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack inSlot = getStackInSlot(index);
		ItemStack removed = ItemStack.EMPTY;
		if (!inSlot.isEmpty()) {
			removed = inSlot.splitStack(count);
			if (inSlot.isEmpty()) {
				setInventorySlotContents(index, ItemStack.EMPTY);
			}
			
			this.markDirty();
		}
		
		return removed;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack inSlot = getStackInSlot(index);
		if (!inSlot.isEmpty()) {
			this.setInventorySlotContents(index, ItemStack.EMPTY);
			this.markDirty();
		}
		return inSlot;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index < 0 || index >= INV_SIZE) {
			return;
		}
		
		if (index < INV_SIZE_GAEL) {
			gaelSlots.set(index, stack);
			this.markDirty();
			return;
		}
		index -= INV_SIZE_GAEL;
		
		if (index < INV_SIZE_SCROLL) {
			scrollSlots.set(index, stack);
			this.markDirty();
			return;
		}
		index -= INV_SIZE_SCROLL;
		
		if (index < INV_SIZE_FETCH) {
			fetchSlots.set(index, stack);
			this.markDirty();
			return;
		}
		index -= INV_SIZE_FETCH;
		
		if (index < INV_SIZE_TEMPLATES) {
			logisticsTemplates.set(index, stack);
			this.markDirty();
			return;
		}
		index -= INV_SIZE_TEMPLATES;
		
		gemSlot = stack;
		this.markDirty();
		return;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public void markDirty() {
		this.dirty = true;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}

	@Override
	public void openInventory(PlayerEntity player) {
		;
	}

	@Override
	public void closeInventory(PlayerEntity player) {
		;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index < 0 || index > INV_SIZE) {
			return false;
		}
		
		if (stack.isEmpty()) {
			return true;
		}
		
		
		if (index < INV_SIZE_GAEL) {
			if (!(stack.getItem() instanceof FairyGael)) {
				return false;
			}
			
			FairyGaelType type = FairyGael.getTypeOf(stack);
			return slotIsType(type, index);
		}
		index -= INV_SIZE_GAEL;
		
		if (index < INV_SIZE_SCROLL) {
			if (!(stack.getItem() instanceof SpellScroll)) {
				return false;
			}
			
			return (SpellScroll.getSpell(stack) != null);
		}
		index -= INV_SIZE_SCROLL;
		
		if (index < INV_SIZE_FETCH) {
			return true;
		}
		index -= INV_SIZE_FETCH;
		
		if (index < INV_SIZE_TEMPLATES) {
			return true;
		}
		index -= INV_SIZE_TEMPLATES;
		
		// Gem slot
		if (stack.getItem() instanceof PositionCrystal && PositionCrystal.getBlockPosition(stack) != null) {
			return true;
		}
		
		return false;
	}

	@Override
	public int getField(int id) {
		final int slot = (id / 2);
		if (id % 2 == 0) {
			// Cast target
			return getFairyCastTarget(slot).ordinal();
		} else {
			// Placement target
			return getFairyPlacementTarget(slot).ordinal();
		}
	}

	@Override
	public void setField(int id, int value) {
		final int slot = (id / 2);
		if (id % 2 == 0) {
			// Cast target
			FairyCastTarget target = FairyCastTarget.values()[value % FairyCastTarget.values().length];
			setFairyCastTarget(slot, target);
		} else {
			// Placement target
			FairyPlacementTarget target = FairyPlacementTarget.values()[value % FairyPlacementTarget.values().length];
			setFairyPlacementTarget(slot, target);
		}
	}

	@Override
	public int getFieldCount() {
		return 2 * INV_SIZE_SCROLL; // placement and target options for each scroll slot
	}

	@Override
	public void clear() {
		gaelSlots = NonNullList.withSize(INV_SIZE_GAEL, ItemStack.EMPTY);
		scrollSlots = NonNullList.withSize(INV_SIZE_SCROLL, ItemStack.EMPTY);
		targetSlots = new FairyCastTarget[INV_SIZE_SCROLL];
		placementSlots = new FairyPlacementTarget[INV_SIZE_SCROLL];
		fetchSlots = NonNullList.withSize(INV_SIZE_FETCH, ItemStack.EMPTY);
		logisticsTemplates = NonNullList.withSize(INV_SIZE_TEMPLATES, ItemStack.EMPTY);
		
		for (int i = 0; i < INV_SIZE_SCROLL; i++) {
			targetSlots[i] = FairyCastTarget.TARGET;
			placementSlots[i] = FairyPlacementTarget.MELEE;
		}
		
		this.markDirty();
	}
	
	public CompoundNBT toNBT() {
		if (this.dirty || this.nbtCache == null) {
			this.nbtCache = new CompoundNBT();
			
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < INV_SIZE; i++) {
				ItemStack inSlot = getStackInSlot(i);
				CompoundNBT tag = new CompoundNBT();
				if (!inSlot.isEmpty()) {
					inSlot.writeToNBT(tag);
				}
				list.appendTag(tag);
			}
			nbtCache.setTag("contents", list);
			
			list = new NBTTagList();
			for (int i = 0; i < INV_SIZE_SCROLL; i++) {
				FairyCastTarget target = targetSlots[i];
				FairyPlacementTarget placement = placementSlots[i];
				CompoundNBT tag = new CompoundNBT();
				tag.setString("target", target.name());
				tag.setString("placement", placement.name());
				list.appendTag(tag);
			}
			nbtCache.setTag("targets", list);
			
//			list = new NBTTagList();
//			for (int i = 0; i < INV_SIZE_TEMPLATES; i++) {
//				ItemStack inSlot = logisticsTemplates[i];
//				CompoundNBT tag = new CompoundNBT();
//				if (!inSlot.isEmpty()) {
//					inSlot.writeToNBT(tag);
//				}
//				list.appendTag(tag);
//			}
//			nbtCache.setTag("pull_templates", list);
//			
//			list = new NBTTagList();
//			for (int i = 0; i < INV_SIZE_TEMPLATES; i++) {
//				ItemStack inSlot = logisticsTemplates[i + INV_SIZE_TEMPLATES];
//				CompoundNBT tag = new CompoundNBT();
//				if (!inSlot.isEmpty()) {
//					inSlot.writeToNBT(tag);
//				}
//				list.appendTag(tag);
//			}
//			nbtCache.setTag("push_templates", list);
			
			this.dirty = false;
		}
		
		return nbtCache;
	}
	
	public void readNBT(CompoundNBT nbt) {
		this.dirty = false;
		this.nbtCache = nbt.copy();
		
		this.clear();
		NBTTagList list = nbt.getTagList("contents", NBT.TAG_COMPOUND);
		for (int i = 0; i < INV_SIZE && i < list.tagCount(); i++) {
			CompoundNBT tag = list.getCompoundTagAt(i);
			@Nonnull ItemStack stack = new ItemStack(tag);
			this.setInventorySlotContents(i, stack); // May be empty :)
		}
		
		list = nbt.getTagList("targets", NBT.TAG_COMPOUND);
		for (int i = 0; i < INV_SIZE_SCROLL && i < list.tagCount(); i++) {
			CompoundNBT tag = list.getCompoundTagAt(i);
			try {
				targetSlots[i] = FairyCastTarget.valueOf(tag.getString("target"));
				placementSlots[i] = FairyPlacementTarget.valueOf(tag.getString("placement"));
			} catch (Exception e) {
				NostrumFairies.logger.warn("Failed to read in fairy target configuration for slot " + i);
				// clear() set up good defaults, but default may be different from NBT, so mark dirty
				this.markDirty();
			}
		}
		
//		list = nbt.getTagList("pull_templates", NBT.TAG_COMPOUND);
//		for (int i = 0; i < INV_SIZE && i < list.tagCount(); i++) {
//			CompoundNBT tag = list.getCompoundTagAt(i);
//			@Nullable ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
//			this.setInventorySlotContents(i, stack); // May be null :)
//		}
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < INV_SIZE; i++) {
			if (!this.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

}
