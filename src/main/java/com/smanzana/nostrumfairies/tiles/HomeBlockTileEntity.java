package com.smanzana.nostrumfairies.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.smanzana.nostrumaetheria.api.aether.IAetherFlowHandler.AetherFlowConnection;
import com.smanzana.nostrumaetheria.api.aether.IAetherHandler;
import com.smanzana.nostrumaetheria.api.aether.IAetherHandlerProvider;
import com.smanzana.nostrumaetheria.api.blocks.IAetherCapableBlock;
import com.smanzana.nostrumaetheria.api.component.IAetherComponentListener;
import com.smanzana.nostrumaetheria.component.AetherHandlerComponent;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FeyBush;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfBuilder;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfCrafter;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityElfCrafter;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.fey.EntityGnomeCollector;
import com.smanzana.nostrumfairies.entity.fey.EntityGnomeCrafter;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.inventory.IFeySlotted;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class HomeBlockTileEntity extends LogisticsTileEntity implements ITickable, IAetherHandlerProvider, IAetherComponentListener {
	
	public static class HomeBlockSlotInventory extends InventoryBasic {

		private final HomeBlockTileEntity owner;
		private final FeyStoneMaterial[] allowedSpecs;
		
		public HomeBlockSlotInventory(HomeBlockTileEntity owner, FeyStoneMaterial[] allowedSpecMaterials, String title, boolean customName) {
			super(title, customName, MAX_TOTAL_SLOTS * 2);
			this.owner = owner;
			this.allowedSpecs = allowedSpecMaterials;
		}
		
		protected static final int getSoulSlot(int index) {
			return index;
		}
		
		protected static final int getSpecializationSlot(int index) {
			return index + MAX_TOTAL_SLOTS;
		}
		
		public static final boolean isSoulSlot(int slot) {
			return slot < MAX_TOTAL_SLOTS;
		}
		
		public static final int getIndexFromSlot(int slot) {
			if (isSoulSlot(slot)) {
				return slot;
			} else {
				return slot - MAX_TOTAL_SLOTS;
			}
		}
		
		public SoulStoneType getPrimarySoulType() {
			for (SoulStoneType type : SoulStoneType.values()) {
				if (type.canHold(owner.type)) {
					return type;
				}
			}
			return SoulStoneType.GEM;
		}
		
		protected boolean isValidSoulStone(@Nonnull ItemStack stack) {
			if (!stack.isEmpty() && stack.getItem() instanceof FeySoulStone) {
				if (!FeySoulStone.getTypeOf(stack).canHold(owner.type)) {
					return false;
				}
				
				// Make sure any entity inside matches, too
				if (FeySoulStone.hasStoredFey(stack)) {
					return FeySoulStone.getStoredFeyType(stack) == owner.type;
				}
				
				return true;
			}
			
			return false;
		}
		
		protected boolean isValidSpecialization(@Nonnull ItemStack stack) {
			if (!stack.isEmpty() && stack.getItem() instanceof IFeySlotted) {
				IFeySlotted stone = (IFeySlotted) stack.getItem();
				if (stone.getFeySlot(stack) == FeySlotType.SPECIALIZATION) {
					FeyStoneMaterial material = stone.getStoneMaterial(stack);
					for (FeyStoneMaterial allowed : this.allowedSpecs) {
						if (allowed == material) {
							return true;
						}
					}
				}
			}
			
			return false;
		}
		
		public @Nonnull ItemStack getSoulStone(int index) {
			final int slot = getSoulSlot(index);
			return this.getStackInSlot(slot);
		}
		
		public boolean hasStone(int index) {
			@Nonnull ItemStack stone = this.getSoulStone(index);
			return isValidSoulStone(stone);
		}
		
		public @Nonnull ItemStack getSpecialization(int index) {
			final int slot = getSpecializationSlot(index);
			return this.getStackInSlot(slot);
		}
		
		@Override
		public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
			if (stack.isEmpty()) {
				return true;
			}
			
			if (isSoulSlot(slot)) {
				return isValidSoulStone(stack);
			} else {
				return isValidSpecialization(stack);
			}
		}
		
		@Override
		public void markDirty() {
			super.markDirty();
			owner.dirtyAndUpdate();
		}
		
		public boolean setSoulStone(int index, @Nonnull ItemStack soulStone) {
			if (!soulStone.isEmpty() && !isValidSoulStone(soulStone)) {
				return false;
			}
			
			int slot = getSoulSlot(index);
			this.setInventorySlotContents(slot, soulStone);
			return true;
		}
		
		public boolean setSpecialization(int index, ItemStack specialization) {
			if (!specialization.isEmpty() && !isValidSpecialization(specialization)) {
				return false;
			}
			
			int slot = getSpecializationSlot(index);
			this.setInventorySlotContents(slot, specialization);
			return true;
		}
		
		private static final String NBT_NAME = "name";
		private static final String NBT_CUSTOM = "custom";
		private static final String NBT_ITEMS = "items";
		
		public CompoundNBT toNBT() {
			CompoundNBT nbt = new CompoundNBT();
			
			nbt.setString(NBT_NAME, this.getName());
			nbt.setBoolean(NBT_CUSTOM, this.hasCustomName());
			nbt.put(NBT_ITEMS, Inventories.serializeInventory(this));
			
			return nbt;
		}
		
		public static HomeBlockTileEntity.HomeBlockSlotInventory fromNBT(HomeBlockTileEntity owner, CompoundNBT nbt) {
			String name = nbt.getString(NBT_NAME);
			boolean custom = nbt.getBoolean(NBT_CUSTOM);
			
			HomeBlockTileEntity.HomeBlockSlotInventory inv = new HomeBlockSlotInventory(owner, FeyHomeBlock.GetSpecMaterials(owner.type), name, custom);
			Inventories.deserializeInventory(inv, nbt.getTag(NBT_ITEMS));
			
			return inv;
		}
	}
	
	public static class HomeBlockUpgradeInventory extends InventoryBasic {
		
		private final HomeBlockTileEntity owner;
		
		public HomeBlockUpgradeInventory(HomeBlockTileEntity owner, String title, boolean customName) {
			super(title, customName, MAX_UPGRADES);
			this.owner = owner;
		}
		
		protected boolean isValidUpgrade(@Nonnull ItemStack stack) {
			if (!stack.isEmpty() && stack.getItem() instanceof IFeySlotted) {
				IFeySlotted stone = (IFeySlotted) stack.getItem();
				return stone.getFeySlot(stack) == FeySlotType.UPGRADE
						|| stone.getFeySlot(stack) == FeySlotType.DOWNGRADE;
			}
			
			return false;
		}
		
		public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
			return slot < MAX_UPGRADES &&
					(stack.isEmpty() || isValidUpgrade(stack));
		}
		
		@Override
		public void markDirty() {
			super.markDirty();
			owner.dirtyAndUpdate();
		}
		
		private static final String NBT_NAME = "name";
		private static final String NBT_CUSTOM = "custom";
		private static final String NBT_ITEMS = "items";
		
		public CompoundNBT toNBT() {
			CompoundNBT nbt = new CompoundNBT();
			
			nbt.setString(NBT_NAME, this.getName());
			nbt.setBoolean(NBT_CUSTOM, this.hasCustomName());
			nbt.put(NBT_ITEMS, Inventories.serializeInventory(this));
			
			return nbt;
		}
		
		public static HomeBlockTileEntity.HomeBlockUpgradeInventory fromNBT(HomeBlockTileEntity owner, CompoundNBT nbt) {
			String name = nbt.getString(NBT_NAME);
			boolean custom = nbt.getBoolean(NBT_CUSTOM);
			
			HomeBlockTileEntity.HomeBlockUpgradeInventory inv = new HomeBlockUpgradeInventory(owner, name, custom);
			Inventories.deserializeInventory(inv, nbt.getTag(NBT_ITEMS));
			
			return inv;
		}
	}
	
	public static class FeyAwayRecord {
		public String name;
		public int tickLastSeen;
		public EntityFeyBase cache;
	}

	private static final int MAX_TOTAL_SLOTS = 5;
	private static final int MAX_NATURAL_SLOTS = 5;
	private static final int DEFAULT_SLOTS = 1;
	private static final int MAX_UPGRADES = 2;
	private static final int ABANDON_TICKS = (20 * 120);
	private static final String NBT_TYPE = "type";
	private static final String NBT_NAME = "name";
	private static final String NBT_SLOT_COUNT = "slot_count";
	private static final String NBT_SLOT_GROWTH = "growth";
	private static final String NBT_FEY = "fey";
	private static final String NBT_FEY_UUID = "fey_uuid";
	private static final String NBT_FEY_NAME = "fey_name";
	private static final String NBT_UPGRADES = "upgrades";
	private static final String NBT_SLOT_INV = "slot_inv";
	private static final String NBT_HANDLER = "aether_handler";
	
	private ResidentType type;
	private String name;
	private int slots;
	private float growth;
	protected UUID[] feySlots;
	protected HashMap<UUID, HomeBlockTileEntity.FeyAwayRecord> feyCacheMap;
	protected HomeBlockTileEntity.HomeBlockSlotInventory slotInv;
	protected HomeBlockTileEntity.HomeBlockUpgradeInventory upgradeInv;
	private int ticksExisted;
	protected AetherHandlerComponent handler;
	
	private boolean aetherDirtyFlag;
	private List<BlockPos> boostBlockSpots; // TODO make sparkles every once in a while?
	
	public HomeBlockTileEntity(ResidentType type) {
		this();
		this.type = type;
		this.slotInv = new HomeBlockSlotInventory(this, FeyHomeBlock.GetSpecMaterials(type), "", false);
	}
	
	public HomeBlockTileEntity() {
		super();
		//this.feyList = new HashSet<>();
		this.slots = DEFAULT_SLOTS;
		this.slotInv = new HomeBlockSlotInventory(this, FeyHomeBlock.GetSpecMaterials(ResidentType.FAIRY), "", false);
		this.upgradeInv = new HomeBlockUpgradeInventory(this, "", false);
		this.name = generateRandomName();
		feySlots = new UUID[MAX_TOTAL_SLOTS];
		feyCacheMap = new HashMap<>();
		handler = new AetherHandlerComponent(this, 0, 500);
		handler.configureInOut(true, false);
		boostBlockSpots = new ArrayList<>();
	}

	@Override
	protected double getDefaultLinkRange() {
		return 16;
	}

	@Override
	protected double getDefaultLogisticsRange() {
		return 30;
	}
	
	public ResidentType getType() {
		return type;
	}

	public int getRawSlots() {
		return slots;
	}
	
	/**
	 * Returns the number of slots that are ready to house a fey 
	 * @return
	 */
	public int getEffectiveSlots() {
		int count = 0;
		for (int i = 0; i < getTotalSlots(); i++) {
			if (slotInv.hasStone(i)) {
				count++;
			}
		}
		return count;
	}
	
	public int getTotalSlots() {
		return getRawSlots() + getBonusSlots();
	}
	
	public int getBonusSlots() {
		return getUpgradeCount(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE);
	}

	public void setSlots(int slots) {
		this.slots = slots;
	}
	
	public boolean isResident(EntityFeyBase fey) {
		UUID id = fey.getUniqueID();
		return this.feyCacheMap.containsKey(id);
	}
	
	protected EntityFeyBase refreshFey(int idx, EntityFeyBase fey) {
		// Check specialization
		@Nonnull ItemStack specialization = this.slotInv.getSpecialization(idx);
		FeyStoneMaterial specMat = FeyStone.instance().getStoneMaterial(specialization);
		if (fey.getCurrentSpecialization() != specMat) {
			fey = fey.switchToSpecialization(specMat);
		}
		
		return fey;
	}
	
	protected void refreshFeyList() {
		for (Entry<UUID, HomeBlockTileEntity.FeyAwayRecord> entry : feyCacheMap.entrySet()) {
			entry.getValue().cache = null;
		}
		
		List<Entity> ents = Lists.newArrayList(world.loadedEntityList);
		
		for (Entity ent : ents) {
			if (ent instanceof EntityFeyBase && feyCacheMap.containsKey(ent.getUniqueID())) {
				HomeBlockTileEntity.FeyAwayRecord record = feyCacheMap.get(ent.getUniqueID());
				int idx = findFeySlot(ent.getUniqueID());
				record.cache = refreshFey(idx, (EntityFeyBase) ent);
				record.tickLastSeen = ticksExisted;
			}
		}
	}
	
	protected void purgeFeyList() {
		Set<UUID> ids = Sets.newHashSet(feyCacheMap.keySet());
		for (UUID id : ids) {
			HomeBlockTileEntity.FeyAwayRecord record = feyCacheMap.get(id);
			if (ticksExisted - record.tickLastSeen > ABANDON_TICKS
				|| (record.cache != null && record.cache.getStatus() == FairyGeneralStatus.WANDERING)) {
				System.out.println("Discarding " + id);
				if (ticksExisted - record.tickLastSeen > ABANDON_TICKS) {
					System.out.println("Expired");
				} else {
					System.out.println(record.cache.getStatus());
				}
				removeResident(id);
			}
		}
		
		
	}
	
	/**
	 * Returns a list of fey that live at this block.
	 * Note this is the list of entities is cached. Some of the entities may be marked dead, NULL, etc.
	 * @return
	 */
	public List<EntityFeyBase> getAvailableFeyEntities() {
		List<EntityFeyBase> list = new ArrayList<>(feyCacheMap.size());
		for (Entry<UUID, HomeBlockTileEntity.FeyAwayRecord> entry : feyCacheMap.entrySet()) {
			list.add(entry.getValue().cache);
		}
		return list;
	}
	
	public Map<UUID, HomeBlockTileEntity.FeyAwayRecord> getFeyEntries() {
		return this.feyCacheMap;
	}
	
	public List<HomeBlockTileEntity.FeyAwayRecord> getFeySlots() {
		List<HomeBlockTileEntity.FeyAwayRecord> records = new ArrayList<>(this.getTotalSlots());
		for (int i = 0; i < getTotalSlots(); i++) {
			final HomeBlockTileEntity.FeyAwayRecord record;
			final UUID id = feySlots[i];
			if (id != null) { 
				record = feyCacheMap.get(id);
			} else {
				record = null;
			}
			records.add(record);
		}
		return records;
	}
	
	public boolean canAccept(EntityFeyBase fey) {
		if (feyCacheMap.size() >= this.getEffectiveSlots()) {
			return false;
		}
		
		return fey.getHomeType() == this.type;
	}
	
	private int findFreeIdx() {
		for (int i = 0; i < this.getEffectiveSlots(); i++) {
			if (feySlots[i] == null) {
				return i;
			}
		}
		
		return -1;
	}
	
	protected UUID getFeyInSlot(int idx) {
		return feySlots[idx];
	}
	
	protected int findFeySlot(UUID id) {
		for (int i = 0; i < getTotalSlots(); i++) {
			if (feySlots[i] != null && feySlots[i].equals(id)) {
				return i;
			}
		}
		
		return -1;
	}
	
	protected int findFeySlot(EntityFeyBase fey) {
		return findFeySlot(fey.getUniqueID());
	}
	
	public boolean addResident(EntityFeyBase fey) {
		if (world == null || world.isRemote) {
			return false;
		}
		
		if (!canAccept(fey)) {
			return false;
		}
		
		int index = findFreeIdx();
		if (index == -1) {
			throw new RuntimeException("Fey cache and slot array are out of sync");
		}
		
		HomeBlockTileEntity.FeyAwayRecord record = new FeyAwayRecord();
		record.tickLastSeen = ticksExisted;
		record.name = fey.getName();
		record.cache = fey;
		
		this.feyCacheMap.put(fey.getUniqueID(), record);
		this.feySlots[index] = fey.getUniqueID();
		dirtyAndUpdate();
		return true;
	}
	
	public void removeResident(UUID id) {
		if (world == null || world.isRemote) {
			return;
		}
		
		this.feyCacheMap.remove(id);
		int idx = findFeySlot(id);
		if (idx != -1) {
			this.feySlots[idx] = null;
		}
		dirtyAndUpdate();
	}
	
	public void removeResident(EntityFeyBase fey) {
		removeResident(fey.getUniqueID());
	}
	
	public void replaceResident(EntityFeyBase original, EntityFeyBase replacement) {
		if (world == null || world.isRemote) {
			return;
		}
		
		if (original == null || replacement == null || !original.getUniqueID().equals(replacement.getUniqueID())) {
			return;
		}
		
		HomeBlockTileEntity.FeyAwayRecord record = new FeyAwayRecord();
		record.tickLastSeen = ticksExisted;
		record.name = replacement.getName();
		record.cache = replacement;
		
		this.feyCacheMap.put(replacement.getUniqueID(), record);

		int idx = findFeySlot(original.getUniqueID());
		this.feySlots[idx] = replacement.getUniqueID();
		dirtyAndUpdate();
	}
	
	public HomeBlockTileEntity.HomeBlockSlotInventory getSlotInventory() {
		return this.slotInv;
	}
	
	public HomeBlockTileEntity.HomeBlockUpgradeInventory getUpgradeInventory() {
		return this.upgradeInv;
	}
	
	public int getUpgradeCount(FeySlotType slot, FeyStoneMaterial material) {
		int count = 0;
		for (int i = 0; i < upgradeInv.getSizeInventory(); i++) {
			ItemStack stack = upgradeInv.getStackInSlot(i);
			if (!stack.isEmpty() && FeyStone.instance().getFeySlot(stack) == slot && FeyStone.instance().getStoneMaterial(stack) == material) {
				count++;
			}
		}
		return count;
	}
	
	protected int getAetherCost() {
		if (this.getAvailableFeyEntities().isEmpty()) {
			// No fey means no workers taking aether
		}
		
		// Ruby downgrades decrease aether cost
		final float skipChance = .3f * getUpgradeCount(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY);
		if (skipChance > 0 && NostrumFairies.random.nextFloat() < skipChance) {
			return 0;
		}
		
		// Take aether for all fey that are currently working or re-couping
		int count = 0;
		for (EntityFeyBase fey : this.getAvailableFeyEntities()) {
			if (fey != null && !fey.isDead && (fey.getCurrentTask() != null || fey.getHappiness() < 100f)) {
				count++;
			}
		}
		
		final int AETHER_PER_TICK = 1;
		return AETHER_PER_TICK * count;
	}
	
	/**
	 * Check whether residents of this building should be allowed to work, given aether levels.
	 * @return
	 */
	public boolean canWork() {
		return this.getAether() >= this.getAetherCost();
	}
	
	public String getName() {
		return name;
	}
	
	protected String generateRandomName() {
		final String[] names = {
				"The Real Leg",
				"The Equal Oysters",
				"The Innocent Leopard Tavern",
				"The Frozen Rapier Bar",
				"The Uneven Fowl",
				"The Violent Guinea Pigs Tavern",
				"The Jolly Baker Tavern",
				"The Ghost Dwarf Tavern",
				"The Blushing Twig Tavern",
				"The Psychotic Nutmeg",
				"The Gullible Owl Pub",
				"The Sour Cake Bar",
				"The Infamous Ocean",
				"The Smiling Arm Tavern",
				"The Greasy Coyote",
				"The Cruel Lettuce",
				"The Plain Guinea Pigs Inn",
				"The Far Away Night Inn",
				"The Laughable Duck",
				"The Mixed Buffalo Pub",
				"The Calm Rope Inn",
				"The Thin Whip",
				"The Safe Rope Inn",
				"The Tired Parrot Inn",
				"The Violet Bell Pub",
				"The Better Dragons",
				"The Abstract Elephant Bar",
				"The Fabulous Gang Pub",
				"The Orange Salad Inn",
				"The Cold Whip Bar",
				"The Disarmed Crocodile Bar",
				"The Shouting Apples Inn",
				"The Dramatic Scream Inn",
				"The Running Spoon Bar",
				"The Oceanic Snowfall",
				"The Snoring Cherry Bar",
				"The Itchy Koala",
				"The Nifty Nugget Bar",
				"The Second Trombone"
			};
			return names[NostrumFairies.random.nextInt(names.length)];
	}
	
	public float getGrowth() {
		return growth;
	}
	
	/**
	 * Downscales xp to match level
	 * @param input
	 * @return
	 */
	protected float adjustGrowth(float input) {
		return (float) (input / (100 * Math.pow(2, slots - 1)));
	}
	
	protected void checkLevel() {
		if (slots >= MAX_NATURAL_SLOTS) {
			this.growth = 0f;
			return;
		}
		
		if (this.growth >= 1f) {
			float leftover = growth - 1f;
			slots++;
			this.growth = (leftover / 2f); // each level multiplies the old requirement by 2
			checkLevel();
			dirtyAndUpdate();
		}
	}
	
	/**
	 * Adds some growth to this block. This is an un-adjusted value.
	 * This should roughly be on a scale of '100', which is the total amount needed
	 * to grow from 1 slot to 2.
	 * @param growth
	 */
	public void addGrowth(float growth) {
		if (slots >= MAX_NATURAL_SLOTS) {
			return;
		}
		
		final float upgradeMod = 1f + (.1f * getUpgradeCount(FeySlotType.UPGRADE, FeyStoneMaterial.RUBY));

		this.growth += adjustGrowth(growth) * upgradeMod;
		checkLevel();
		dirtyAndUpdate();
	}
	
	public int getAether() {
		return handler.getAether(null);
	}
	
	public int getAetherCapacity() {
		return handler.getMaxAether(null);
	}
	
	public float getAetherLevel() {
		int aether = getAether();
		int aetherCapacity = getAetherCapacity();
		if (aetherCapacity <= 0 || aether < 0) {
			return 0f;
		}
		
		return (float) aether / (float) aetherCapacity;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		nbt.setString(NBT_TYPE, type.name());
		nbt.setString(NBT_NAME, getName());
		nbt.putInt(NBT_SLOT_COUNT, slots);
		nbt.setFloat(NBT_SLOT_GROWTH, growth);
		
		ListNBT list = new ListNBT();
		for (int i = 0; i < getEffectiveSlots(); i++) {
			UUID id = feySlots[i];
			CompoundNBT tag = new CompoundNBT();
			if (id != null) {
				HomeBlockTileEntity.FeyAwayRecord record = this.feyCacheMap.get(id);
				tag.setString(NBT_FEY_NAME, record.name);
				tag.setString(NBT_FEY_UUID, id.toString());
			}
			list.add(tag);
		}
		nbt.put(NBT_FEY, list);
		
		nbt.put(NBT_UPGRADES, this.upgradeInv.toNBT());
		nbt.put(NBT_SLOT_INV, slotInv.toNBT());
		nbt.put(NBT_HANDLER, handler.writeToNBT(new CompoundNBT()));
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.readFromNBT(nbt);
		
		String type = nbt.getString(NBT_TYPE);
		this.type = ResidentType.valueOf(ResidentType.class, type.toUpperCase());
		this.name = nbt.getString(NBT_NAME);
		if (nbt.hasKey(NBT_SLOT_COUNT, NBT.TAG_INT)) {
			this.slots = Math.max(1, Math.min(MAX_NATURAL_SLOTS, nbt.getInt(NBT_SLOT_COUNT)));
		} else {
			this.slots = DEFAULT_SLOTS; 
		}
		this.growth = nbt.getFloat(NBT_SLOT_GROWTH);
		
		this.slotInv = HomeBlockSlotInventory.fromNBT(this, nbt.getCompoundTag(NBT_SLOT_INV));
		this.upgradeInv = HomeBlockUpgradeInventory.fromNBT(this, nbt.getCompoundTag(NBT_UPGRADES));
		
		feyCacheMap.clear();
		ListNBT list = nbt.getList(NBT_FEY, NBT.TAG_COMPOUND);
		for (int i = 0; i < getEffectiveSlots(); i++) {
			CompoundNBT tag = list.getCompound(i);
			UUID id = null;
			if (tag != null && tag.hasKey(NBT_FEY_UUID)) {
				id = UUID.fromString(tag.getString(NBT_FEY_UUID));
				HomeBlockTileEntity.FeyAwayRecord record = new FeyAwayRecord();
				record.tickLastSeen = ticksExisted;
				record.name = tag.getString(NBT_FEY_NAME);
				feyCacheMap.put(id, record);
			}
			this.feySlots[i] = id;
		}
		if (this.world != null) {
			this.refreshFeyList();
		}
		
		handler.readFromNBT(nbt.getCompoundTag(NBT_HANDLER));
	}

	@Override
	public void update() {
		ticksExisted++;
		
		if (!world.isRemote && this.ticksExisted == 1) {
			this.handler.setAutoFill(true);
		}
		
		if (this.ticksExisted % 20 == 0) {
			// Check on fey. Are they missing?
			refreshFeyList();
			if (!world.isRemote) {
				purgeFeyList();
			}
		}
		
		if (!world.isRemote) {
			//If any soul gems in our inventory have souls in them, free them!
			for (int i = 0; i < this.slotInv.getSizeInventory(); i++) {
				if (HomeBlockSlotInventory.isSoulSlot(i) && slotInv.hasStone(i)) {
					ItemStack stone = slotInv.getStackInSlot(i);
					if (FeySoulStone.hasStoredFey(stone)) {
						EntityFeyBase fey = FeySoulStone.spawnStoredEntity(stone, world, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
						fey.setHome(this.getPos());
						slotInv.setInventorySlotContents(i, FeySoulStone.clearEntity(stone));
					}
				}
			}
			
			// Take aether cost
			final int debt = this.getAetherCost();
			if (this.handler.drawAether(null, debt) != debt) {
				// Didn't have enough. Deactivate!
				//deactivate();
			} else {
				// Try to fill up what we just spent
				this.handler.fillAether(1000);
			}
		}

		handler.tick();
		
		if (!world.isRemote && aetherDirtyFlag && (ticksExisted == 1 || ticksExisted % 5 == 0)) {
			dirtyAndUpdate();
			aetherDirtyFlag = false;
		}
		
		if (!world.isRemote && ticksExisted % 100 == 0 && world.isDaytime()) {
			if (NostrumFairies.random.nextFloat() < .2f) {
				// Don't scan if too many are nearby
				if (world.getEntitiesWithinAABB(EntityFeyBase.class, FeyHomeBlock.FULL_BLOCK_AABB.grow(16)).size() < 8) {
					// Scan for nearby flowers and possible spawn extra fey
					final float happiness = getAverageHappiness();
					if (happiness > 50f && this.getAether() > 0) {
						// Do we have vacancies? And are there spawn-boosting blocks nearby?
						final float chance = Math.min(.75f, .01f + getBlockSpawnBonus() + getVacancyBonus());
						if (NostrumFairies.random.nextFloat() < chance) {
							// Spawn a wild'un!
							spawnWildNearby();
						}
					}
				}
			}
			
			// vfx
			Iterator<BlockPos> it = boostBlockSpots.iterator();
			while (it.hasNext()) {
				BlockPos pos = it.next();
				BlockState state = world.getBlockState(pos);
				if (getBlockSpawnBonus(pos, state) <= 0) {
					it.remove();
					continue;
				}
				
				if (NostrumFairies.random.nextFloat() < .2f) {
					((ServerWorld) world).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
							pos.getX() + .5,
							pos.getY() + 1,
							pos.getZ() + .5,
							3,
							.25,
							.2,
							.25,
							.1,
							new int[0]);
					break;
				}
			}
		}
		
	}
	
	protected float getAverageHappiness() {
		float sum = 0f;
		int count = 0;
		for (EntityFeyBase fey : getAvailableFeyEntities()) {
			if (fey == null || fey.isDead) {
				continue;
			}
			
			count++;
			sum += fey.getHappiness();
		}
		
		if (count == 0) {
			return 51f;
		}
		
		return sum / (float) count;
	}
	
	protected float getBlockSpawnBonus() {
		final int scanRadius = 5;
		
		// Clear out saved copy of these locations
		this.boostBlockSpots.clear();
		
		float bonus = 0f;
		MutableBlockPos cursor = new MutableBlockPos();
		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();
		for (int i = -scanRadius; i <= scanRadius; i++)
		for (int j = -1; j <= 1; j++)
		for (int k = -scanRadius; k <= scanRadius; k++) {
			cursor.setPos(x + i, y + j, z + k);
			if (cursor.getY() < 0 || cursor.getY() > 255) {
				continue;
			}
			
			BlockState state = world.getBlockState(cursor);
			final float boost = getBlockSpawnBonus(cursor, state);
			bonus += boost;
			if (boost > 0f) {
				boostBlockSpots.add(cursor.toImmutable());
			}
		}
		return bonus;
	}
	
	protected float getBlockSpawnBonus(BlockPos pos, BlockState state) {
		if (state != null && state.getBlock() instanceof FeyBush) {
			return .005f;
		}
		
		return 0;
	}
	
	protected float getVacancyBonus() {
		// count vacancies
		int count = 0;
		for (int i = 0; i < getEffectiveSlots(); i++) {
			if (feySlots[i] == null) {
				count++;
			}
		}
		
		return (float) count * .025f; 
	}
	
	protected BlockPos findRandomCenteredSpot() {
		// Find a random spot
		BlockPos targ = null;
		int attempts = 20;
		final Random rand = NostrumFairies.random;
		final double minDist = 5;
		final double maxDist = 15;
		
		do {
			double dist = minDist + (rand.nextDouble() * (maxDist - minDist));
			float angle = (float) (rand.nextDouble() * (2 * Math.PI));
			float tilt = (float) (rand.nextDouble() * (2 * Math.PI)) * .5f;
			
			targ = new BlockPos(new Vec3d(
					pos.getX() + (Math.cos(angle) * dist),
					pos.getY() + (Math.cos(tilt) * dist),
					pos.getZ() + (Math.sin(angle) * dist)));
			
			while (targ.getY() > 0 && world.isAirBlock(targ)) {
				targ = targ.down();
			}
			if (targ.getY() < 256) {
				targ = targ.up();
			}
			
			// We've hit a non-air block. Make sure there's space above it
			if (world.isAirBlock(targ.up())) {
				break;
			}
		} while (targ == null && attempts > 0);
		
		if (targ == null) {
			targ = pos.up();
		}
		
		return targ;
	}
	
	protected @Nullable BlockPos findBushSpot() {
		if (!boostBlockSpots.isEmpty()) {
			return boostBlockSpots.get(NostrumFairies.random.nextInt(boostBlockSpots.size()));
		}
		
		return null;
	}
	
	protected void spawnWildNearby() {
		BlockPos at = findBushSpot();
		if (at == null) {
			at = findRandomCenteredSpot();
		}
		
		spawnWild(at);
	}
	
	protected void spawnWild(BlockPos pos) {
		final EntityFeyBase fey;
		final int type = NostrumFairies.random.nextInt(3);
		switch (this.type) {
		case DWARF:
			if (type == 0) fey = new EntityDwarf(world);
			else if (type == 1) fey = new EntityDwarfBuilder(world);
			else fey = new EntityDwarfCrafter(world);
			break;
		case ELF:
			if (type == 0) fey = new EntityElf(world);
			else if (type == 1) fey = new EntityElfArcher(world);
			else fey = new EntityElfCrafter(world);
			break;
		case FAIRY:
		default:
			fey = new EntityFairy(world);
			break;
		case GNOME:
			if (type == 0) fey = new EntityGnome(world);
			else if (type == 1) fey = new EntityGnomeCrafter(world);
			else fey = new EntityGnomeCollector(world);
			break;
		}
		
		fey.setPosition(pos.getX() + .5, pos.getY() + .05, pos.getZ() + .5);
		world.addEntity(fey);
	}
	
	protected void dirtyAndUpdate() {
		markDirty();
		if (world != null && !world.isRemote) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
		}
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
	}
	
	@Override
	public void addConnections(List<AetherFlowConnection> connections) {
		for (Direction dir : Direction.values()) {
			if (!handler.getSideEnabled(dir)) {
				continue;
			}
			
			BlockPos neighbor = pos.offset(dir);
			
			// First check for a TileEntity
			TileEntity te = world.getTileEntity(neighbor);
			if (te != null && te instanceof IAetherHandler) {
				connections.add(new AetherFlowConnection((IAetherHandler) te, dir.getOpposite()));
				continue;
			}
			if (te != null && te instanceof IAetherHandlerProvider) {
				connections.add(new AetherFlowConnection(((IAetherHandlerProvider) te).getHandler(), dir.getOpposite()));
				continue;
			}
			
			// See if block boasts being able to get us a handler
			BlockState attachedState = world.getBlockState(neighbor);
			Block attachedBlock = attachedState.getBlock();
			if (attachedBlock instanceof IAetherCapableBlock) {
				connections.add(new AetherFlowConnection(((IAetherCapableBlock) attachedBlock).getAetherHandler(world, attachedState, neighbor, dir), dir.getOpposite()));
				continue;
			}
		}
	}
	
	@Override
	public void dirty() {
		this.markDirty();
	}
	
	@Override
	public void onAetherFlowTick(int diff, boolean added, boolean taken) {
		aetherDirtyFlag = true;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		
		// Clean up connections
		handler.clearConnections();
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		handler.clearConnections();
	}
	
	@Override
	public IAetherHandler getHandler() {
		return handler;
	}
}