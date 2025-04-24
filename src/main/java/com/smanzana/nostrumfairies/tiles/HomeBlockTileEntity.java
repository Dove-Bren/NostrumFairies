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

import com.google.common.collect.Sets;
import com.smanzana.nostrumaetheria.api.aether.IAetherFlowHandler.AetherFlowConnection;
import com.smanzana.nostrumaetheria.api.aether.IAetherHandler;
import com.smanzana.nostrumaetheria.api.aether.IAetherHandlerProvider;
import com.smanzana.nostrumaetheria.api.blocks.IAetherCapableBlock;
import com.smanzana.nostrumaetheria.api.component.IAetherComponentListener;
import com.smanzana.nostrumaetheria.api.component.IAetherHandlerComponent;
import com.smanzana.nostrumaetheria.api.proxy.APIProxy;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FeyBush;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock;
import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.entity.ResidentType;
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
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;
import com.smanzana.nostrummagica.util.Entities;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class HomeBlockTileEntity extends LogisticsTileEntity implements TickableBlockEntity, IAetherHandlerProvider, IAetherComponentListener {
	
	public static class HomeBlockSlotInventory extends SimpleContainer {

		private final HomeBlockTileEntity owner;
		private final FeyStoneMaterial[] allowedSpecs;
		
		public HomeBlockSlotInventory(HomeBlockTileEntity owner, FeyStoneMaterial[] allowedSpecMaterials) {
			super(MAX_TOTAL_SLOTS * 2);
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
				if (FeySoulStone.HasStoredFey(stack)) {
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
			return this.getItem(slot);
		}
		
		public boolean hasStone(int index) {
			@Nonnull ItemStack stone = this.getSoulStone(index);
			return isValidSoulStone(stone);
		}
		
		public @Nonnull ItemStack getSpecialization(int index) {
			final int slot = getSpecializationSlot(index);
			return this.getItem(slot);
		}
		
		@Override
		public boolean canPlaceItem(int slot, @Nonnull ItemStack stack) {
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
		public void setChanged() {
			super.setChanged();
			owner.dirtyAndUpdate();
		}
		
		public boolean setSoulStone(int index, @Nonnull ItemStack soulStone) {
			if (!soulStone.isEmpty() && !isValidSoulStone(soulStone)) {
				return false;
			}
			
			int slot = getSoulSlot(index);
			this.setItem(slot, soulStone);
			return true;
		}
		
		public boolean setSpecialization(int index, ItemStack specialization) {
			if (!specialization.isEmpty() && !isValidSpecialization(specialization)) {
				return false;
			}
			
			int slot = getSpecializationSlot(index);
			this.setItem(slot, specialization);
			return true;
		}
		
		private static final String NBT_ITEMS = "items";
		
		public CompoundTag toNBT() {
			CompoundTag nbt = new CompoundTag();
			
			nbt.put(NBT_ITEMS, Inventories.serializeInventory(this));
			
			return nbt;
		}
		
		public static HomeBlockTileEntity.HomeBlockSlotInventory fromNBT(HomeBlockTileEntity owner, CompoundTag nbt) {
			
			HomeBlockTileEntity.HomeBlockSlotInventory inv = new HomeBlockSlotInventory(owner, FeyHomeBlock.GetSpecMaterials(owner.type));
			Inventories.deserializeInventory(inv, nbt.get(NBT_ITEMS));
			
			return inv;
		}
	}
	
	public static class HomeBlockUpgradeInventory extends SimpleContainer {
		
		private final HomeBlockTileEntity owner;
		
		public HomeBlockUpgradeInventory(HomeBlockTileEntity owner) {
			super(MAX_UPGRADES);
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
		
		public boolean canPlaceItem(int slot, @Nonnull ItemStack stack) {
			return slot < MAX_UPGRADES &&
					(stack.isEmpty() || isValidUpgrade(stack));
		}
		
		@Override
		public void setChanged() {
			super.setChanged();
			owner.dirtyAndUpdate();
		}
		
		private static final String NBT_ITEMS = "items";
		
		public CompoundTag toNBT() {
			CompoundTag nbt = new CompoundTag();
			
			nbt.put(NBT_ITEMS, Inventories.serializeInventory(this));
			
			return nbt;
		}
		
		public static HomeBlockTileEntity.HomeBlockUpgradeInventory fromNBT(HomeBlockTileEntity owner, CompoundTag nbt) {
			HomeBlockTileEntity.HomeBlockUpgradeInventory inv = new HomeBlockUpgradeInventory(owner);
			Inventories.deserializeInventory(inv, nbt.get(NBT_ITEMS));
			
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
	protected IAetherHandlerComponent handler;
	
	private boolean aetherDirtyFlag;
	private List<BlockPos> boostBlockSpots; // TODO make sparkles every once in a while?
	
	public HomeBlockTileEntity(BlockPos pos, BlockState state, ResidentType type) {
		this(pos, state);
		this.type = type;
		this.slotInv = new HomeBlockSlotInventory(this, FeyHomeBlock.GetSpecMaterials(type));
	}
	
	protected HomeBlockTileEntity(BlockPos pos, BlockState state) {
		super(FairyTileEntities.HomeBlockTileEntityType, pos, state);
		//this.feyList = new HashSet<>();
		this.slots = DEFAULT_SLOTS;
		this.slotInv = new HomeBlockSlotInventory(this, FeyHomeBlock.GetSpecMaterials(ResidentType.FAIRY));
		this.upgradeInv = new HomeBlockUpgradeInventory(this);
		this.name = generateRandomName();
		feySlots = new UUID[MAX_TOTAL_SLOTS];
		feyCacheMap = new HashMap<>();
		handler = APIProxy.createHandlerComponent(this, 0, 500);
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
	
	public ResidentType getResidentType() {
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
		return Math.min(MAX_TOTAL_SLOTS, getRawSlots() + getBonusSlots());
	}
	
	public int getBonusSlots() {
		return getUpgradeCount(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE);
	}

	public void setSlots(int slots) {
		this.slots = slots;
	}
	
	public boolean isResident(EntityFeyBase fey) {
		UUID id = fey.getUUID();
		return this.feyCacheMap.containsKey(id);
	}
	
	protected EntityFeyBase refreshFey(int idx, EntityFeyBase fey) {
		// Check specialization
		@Nonnull ItemStack specialization = this.slotInv.getSpecialization(idx);
		FeyStoneMaterial specMat = specialization.isEmpty() ? null : (((IFeySlotted) specialization.getItem()).getStoneMaterial(specialization));
		if (fey.getCurrentSpecialization() != specMat) {
			fey = fey.switchToSpecialization(specMat);
		}
		
		return fey;
	}
	
	protected void refreshFeyList() {
		for (Entry<UUID, HomeBlockTileEntity.FeyAwayRecord> entry : feyCacheMap.entrySet()) {
			entry.getValue().cache = null;
			
			Entity ent = Entities.FindEntity(level, entry.getKey());
			if (ent != null) {
				int idx = findFeySlot(ent.getUUID());
				entry.getValue().cache = refreshFey(idx, (EntityFeyBase) ent);
				entry.getValue().tickLastSeen = ticksExisted;
			}
		}
		
//		((ServerWorld) world).getEntities().forEach((ent) -> {
//			if (ent instanceof EntityFeyBase && feyCacheMap.containsKey(ent.getUniqueID())) {
//				HomeBlockTileEntity.FeyAwayRecord record = feyCacheMap.get(ent.getUniqueID());
//				int idx = findFeySlot(ent.getUniqueID());
//				record.cache = refreshFey(idx, (EntityFeyBase) ent);
//				record.tickLastSeen = ticksExisted;
//			}
//		});
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
		return findFeySlot(fey.getUUID());
	}
	
	public boolean addResident(EntityFeyBase fey) {
		if (level == null || level.isClientSide) {
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
		record.name = fey.getName().getString();
		record.cache = fey;
		
		this.feyCacheMap.put(fey.getUUID(), record);
		this.feySlots[index] = fey.getUUID();
		dirtyAndUpdate();
		return true;
	}
	
	public void removeResident(UUID id) {
		if (level == null || level.isClientSide) {
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
		removeResident(fey.getUUID());
	}
	
	public void replaceResident(EntityFeyBase original, EntityFeyBase replacement) {
		if (level == null || level.isClientSide) {
			return;
		}
		
		if (original == null || replacement == null || !original.getUUID().equals(replacement.getUUID())) {
			return;
		}
		
		HomeBlockTileEntity.FeyAwayRecord record = new FeyAwayRecord();
		record.tickLastSeen = ticksExisted;
		record.name = replacement.getName().getString();
		record.cache = replacement;
		
		this.feyCacheMap.put(replacement.getUUID(), record);

		int idx = findFeySlot(original.getUUID());
		this.feySlots[idx] = replacement.getUUID();
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
		for (int i = 0; i < upgradeInv.getContainerSize(); i++) {
			ItemStack stack = upgradeInv.getItem(i);
			if (!stack.isEmpty() && ((FeyStone) stack.getItem()).getFeySlot(stack) == slot && ((FeyStone) stack.getItem()).getStoneMaterial(stack) == material) {
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
			if (fey != null && fey.isAlive() && (fey.getCurrentTask() != null || fey.getHappiness() < 100f)) {
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
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		
		nbt.putString(NBT_TYPE, type.name());
		nbt.putString(NBT_NAME, getName());
		nbt.putInt(NBT_SLOT_COUNT, slots);
		nbt.putFloat(NBT_SLOT_GROWTH, growth);
		
		ListTag list = new ListTag();
		for (int i = 0; i < getEffectiveSlots(); i++) {
			UUID id = feySlots[i];
			CompoundTag tag = new CompoundTag();
			if (id != null) {
				HomeBlockTileEntity.FeyAwayRecord record = this.feyCacheMap.get(id);
				tag.putString(NBT_FEY_NAME, record.name);
				tag.putString(NBT_FEY_UUID, id.toString());
			}
			list.add(tag);
		}
		nbt.put(NBT_FEY, list);
		
		nbt.put(NBT_UPGRADES, this.upgradeInv.toNBT());
		nbt.put(NBT_SLOT_INV, slotInv.toNBT());
		nbt.put(NBT_HANDLER, handler.writeToNBT(new CompoundTag()));
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		String type = nbt.getString(NBT_TYPE);
		this.type = ResidentType.valueOf(ResidentType.class, type.toUpperCase());
		this.name = nbt.getString(NBT_NAME);
		if (nbt.contains(NBT_SLOT_COUNT, Tag.TAG_INT)) {
			this.slots = Math.max(1, Math.min(MAX_NATURAL_SLOTS, nbt.getInt(NBT_SLOT_COUNT)));
		} else {
			this.slots = DEFAULT_SLOTS; 
		}
		this.growth = nbt.getFloat(NBT_SLOT_GROWTH);
		
		this.slotInv = HomeBlockSlotInventory.fromNBT(this, nbt.getCompound(NBT_SLOT_INV));
		this.upgradeInv = HomeBlockUpgradeInventory.fromNBT(this, nbt.getCompound(NBT_UPGRADES));
		
		feyCacheMap.clear();
		ListTag list = nbt.getList(NBT_FEY, Tag.TAG_COMPOUND);
		for (int i = 0; i < getEffectiveSlots(); i++) {
			CompoundTag tag = list.getCompound(i);
			UUID id = null;
			if (tag != null && tag.contains(NBT_FEY_UUID)) {
				id = UUID.fromString(tag.getString(NBT_FEY_UUID));
				HomeBlockTileEntity.FeyAwayRecord record = new FeyAwayRecord();
				record.tickLastSeen = ticksExisted;
				record.name = tag.getString(NBT_FEY_NAME);
				feyCacheMap.put(id, record);
			}
			this.feySlots[i] = id;
		}
		if (this.level != null) {
			this.refreshFeyList();
		}
		
		handler.readFromNBT(nbt.getCompound(NBT_HANDLER));
	}

	@Override
	public void tick() {
		ticksExisted++;
		
		if (!level.isClientSide && this.ticksExisted == 1) {
			this.handler.setAutoFill(true);
		}
		
		if (this.ticksExisted % 20 == 0) {
			// Check on fey. Are they missing?
			refreshFeyList();
			if (!level.isClientSide) {
				purgeFeyList();
			}
		}
		
		if (!level.isClientSide) {
			//If any soul gems in our inventory have souls in them, free them!
			for (int i = 0; i < this.slotInv.getContainerSize(); i++) {
				if (HomeBlockSlotInventory.isSoulSlot(i) && slotInv.hasStone(i)) {
					ItemStack stone = slotInv.getItem(i);
					if (FeySoulStone.HasStoredFey(stone)) {
						EntityFeyBase fey = FeySoulStone.spawnStoredEntity(stone, level, worldPosition.getX() + .5, worldPosition.getY() + 1, worldPosition.getZ() + .5);
						fey.setHome(this.getBlockPos());
						slotInv.setItem(i, FeySoulStone.clearEntity(stone));
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
		
		if (!level.isClientSide && aetherDirtyFlag && (ticksExisted == 1 || ticksExisted % 5 == 0)) {
			dirtyAndUpdate();
			aetherDirtyFlag = false;
		}
		
		if (!level.isClientSide && ticksExisted % 100 == 0 && level.isDay()) {
			if (NostrumFairies.random.nextFloat() < .2f) {
				// Don't scan if too many are nearby
				if (level.getEntitiesOfClass(EntityFeyBase.class, Shapes.block().bounds().inflate(16)).size() < 8) {
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
				BlockState state = level.getBlockState(pos);
				if (getBlockSpawnBonus(pos, state) <= 0) {
					it.remove();
					continue;
				}
				
				if (NostrumFairies.random.nextFloat() < .2f) {
					((ServerLevel) level).sendParticles(ParticleTypes.HAPPY_VILLAGER,
							pos.getX() + .5,
							pos.getY() + 1,
							pos.getZ() + .5,
							3,
							.25,
							.2,
							.25,
							.1
							);
					break;
				}
			}
		}
		
	}
	
	protected float getAverageHappiness() {
		float sum = 0f;
		int count = 0;
		for (EntityFeyBase fey : getAvailableFeyEntities()) {
			if (fey == null || !fey.isAlive()) {
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
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		final int x = worldPosition.getX();
		final int y = worldPosition.getY();
		final int z = worldPosition.getZ();
		for (int i = -scanRadius; i <= scanRadius; i++)
		for (int j = -1; j <= 1; j++)
		for (int k = -scanRadius; k <= scanRadius; k++) {
			cursor.set(x + i, y + j, z + k);
			if (cursor.getY() < 0 || cursor.getY() > 255) {
				continue;
			}
			
			BlockState state = level.getBlockState(cursor);
			final float boost = getBlockSpawnBonus(cursor, state);
			bonus += boost;
			if (boost > 0f) {
				boostBlockSpots.add(cursor.immutable());
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
			
			targ = new BlockPos(new Vec3(
					worldPosition.getX() + (Math.cos(angle) * dist),
					worldPosition.getY() + (Math.cos(tilt) * dist),
					worldPosition.getZ() + (Math.sin(angle) * dist)));
			
			while (targ.getY() > 0 && level.isEmptyBlock(targ)) {
				targ = targ.below();
			}
			if (targ.getY() < 256) {
				targ = targ.above();
			}
			
			// We've hit a non-air block. Make sure there's space above it
			if (level.isEmptyBlock(targ.above())) {
				break;
			}
		} while (targ == null && attempts > 0);
		
		if (targ == null) {
			targ = worldPosition.above();
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
			if (type == 0) fey = new EntityDwarf(FairyEntities.Dwarf, level);
			else if (type == 1) fey = new EntityDwarfBuilder(FairyEntities.DwarfBuilder, level);
			else fey = new EntityDwarfCrafter(FairyEntities.DwarfCrafter, level);
			break;
		case ELF:
			if (type == 0) fey = new EntityElf(FairyEntities.Elf, level);
			else if (type == 1) fey = new EntityElfArcher(FairyEntities.ElfArcher, level);
			else fey = new EntityElfCrafter(FairyEntities.ElfCrafter, level);
			break;
		case FAIRY:
		default:
			fey = new EntityFairy(FairyEntities.Fairy, level);
			break;
		case GNOME:
			if (type == 0) fey = new EntityGnome(FairyEntities.Gnome, level);
			else if (type == 1) fey = new EntityGnomeCrafter(FairyEntities.GnomeCrafter, level);
			else fey = new EntityGnomeCollector(FairyEntities.GnomeCollector, level);
			break;
		}
		
		fey.setPos(pos.getX() + .5, pos.getY() + .05, pos.getZ() + .5);
		level.addFreshEntity(fey);
	}
	
	protected void dirtyAndUpdate() {
		setChanged();
		if (level != null && !level.isClientSide) {
			level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
		}
	}
	
	@Override
	public void setChanged() {
		super.setChanged();
	}
	
	@Override
	public void addConnections(List<AetherFlowConnection> connections) {
		for (Direction dir : Direction.values()) {
			if (!handler.getSideEnabled(dir)) {
				continue;
			}
			
			BlockPos neighbor = worldPosition.relative(dir);
			
			// First check for a TileEntity
			BlockEntity te = level.getBlockEntity(neighbor);
			if (te != null && te instanceof IAetherHandler) {
				connections.add(new AetherFlowConnection((IAetherHandler) te, dir.getOpposite()));
				continue;
			}
			if (te != null && te instanceof IAetherHandlerProvider) {
				connections.add(new AetherFlowConnection(((IAetherHandlerProvider) te).getHandler(), dir.getOpposite()));
				continue;
			}
			
			// See if block boasts being able to get us a handler
			BlockState attachedState = level.getBlockState(neighbor);
			Block attachedBlock = attachedState.getBlock();
			if (attachedBlock instanceof IAetherCapableBlock) {
				connections.add(new AetherFlowConnection(((IAetherCapableBlock) attachedBlock).getAetherHandler(level, attachedState, neighbor, dir), dir.getOpposite()));
				continue;
			}
		}
	}
	
	@Override
	public void dirty() {
		this.setChanged();
	}
	
	@Override
	public void onAetherFlowTick(int diff, boolean added, boolean taken) {
		aetherDirtyFlag = true;
	}
	
	@Override
	public void setRemoved() {
		super.setRemoved();
		
		// Clean up connections
		handler.clearConnections();
	}
	
//	@Override
//	public void onChunkUnload() {
//		super.onChunkUnload();
//		handler.clearConnections();
//	}
	
	@Override
	public IAetherHandler getHandler() {
		return handler;
	}
}