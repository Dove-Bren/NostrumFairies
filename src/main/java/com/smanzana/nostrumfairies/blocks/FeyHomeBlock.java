package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumaetheria.api.aether.IAetherFlowHandler.AetherFlowConnection;
import com.smanzana.nostrumaetheria.api.aether.IAetherHandler;
import com.smanzana.nostrumaetheria.api.aether.IAetherHandlerProvider;
import com.smanzana.nostrumaetheria.api.blocks.IAetherCapableBlock;
import com.smanzana.nostrumaetheria.api.component.IAetherComponentListener;
import com.smanzana.nostrumaetheria.component.AetherHandlerComponent;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker.FairyGeneralStatus;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.inventory.IFeySlotted;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Houses fey.
 * Has all the bed and satisfaction and recruiting and job management stuff in it.
 * @author Skyler
 *
 */
public class FeyHomeBlock extends Block implements ITileEntityProvider {
	
	public static enum ResidentType implements IStringSerializable {
		FAIRY,
		ELF,
		DWARF,
		GNOME;

		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.getName();
		}
	}
	
	private static enum BlockFunction implements IStringSerializable {
		CENTER,
		TOP;

		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.getName();
		}
	}
	
	//public static final PropertyEnum<ResidentType> TYPE = PropertyEnum.<ResidentType>create("type", ResidentType.class);
	public static final PropertyEnum<BlockFunction> BLOCKFUNC = PropertyEnum.<BlockFunction>create("func", BlockFunction.class);
	//public static final PropertyInteger Age = PropertyInteger.create("age", 0, 4);
	
	protected static final String ID = "home_block";
	
	private static Map<ResidentType, FeyHomeBlock> instances = null;
	
	public static FeyHomeBlock instance(ResidentType type) {
		if (instances == null) {
			instances = new EnumMap<>(ResidentType.class);
			for (ResidentType t : ResidentType.values()) {
				instances.put(t, new FeyHomeBlock(t));
			}
		}
		
		return instances.get(type);
	}
	
	public static String ID(ResidentType type) {
		return ID + "_" + type.name().toLowerCase();
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(HomeBlockTileEntity.class, "home_block_te");
	}
	
	private final ResidentType type;
	
	private FeyHomeBlock(ResidentType type) {
		super(Material.WOOD, MapColor.BLUE);
		this.setUnlocalizedName(ID(type));
		this.setHardness(0.0f);
		this.setResistance(100.0f);
		this.setLightOpacity(0);
		this.setTickRandomly(true);
		this.setCreativeTab(NostrumFairies.creativeTab);
		
		this.type = type;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BLOCKFUNC);
	}
	
	private BlockFunction functionFromMeta(int meta) {
		return BlockFunction.values()[meta % BlockFunction.values().length];
	}
	
	private int metaFromFunc(BlockFunction func) {
		return (func.ordinal());
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(BLOCKFUNC, functionFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return metaFromFunc(state.getValue(BLOCKFUNC));
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return super.getItemDropped(state, rand, fortune);
		//return null;
    }
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (state == null || !(state.getBlock() instanceof FeyHomeBlock)) {
			return false;
		}
		
		BlockPos center = ((FeyHomeBlock) state.getBlock()).getMasterPos(worldIn, pos, state);
		TileEntity te = worldIn.getTileEntity(center);
		if (te == null || !(te instanceof HomeBlockTileEntity)) {
			return false;
		}
		
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.homeBlockID, worldIn,
				center.getX(), center.getY(), center.getZ());
		
		return true;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isFullyOpaque(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		super.randomTick(worldIn, pos, state, random);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);
	}
	
	public boolean isCenter(IBlockState state) {
		return state.getValue(BLOCKFUNC) == BlockFunction.CENTER;
	}
	
	public ResidentType getType(IBlockState state) {
		return type;
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		TileEntity ent = world.getTileEntity(pos);
		if (ent != null && ent instanceof HomeBlockTileEntity) {
			HomeBlockTileEntity te = (HomeBlockTileEntity) ent;
			te.unlinkFromNetwork();
			
			// Drop inventories on the floor
			// TODO
		}
		world.removeTileEntity(pos);
		
		world.setBlockToAir(getPaired(state, pos));
	}
	
	private BlockPos getPaired(IBlockState state, BlockPos pos) {
		return pos.offset(state.getValue(BLOCKFUNC) == BlockFunction.CENTER ? EnumFacing.UP : EnumFacing.DOWN);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		this.destroy(world, pos, state);
		world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		if (!worldIn.isAirBlock(pos.up()))
			return false;
		
		if (worldIn.getTileEntity(pos) != null)
			return false;
		
		if (worldIn.getTileEntity(pos.up()) != null)
			return false;
		
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return this.getDefaultState()
				.withProperty(BLOCKFUNC, BlockFunction.CENTER);
				
	}
	
	@Override
	public int damageDropped(IBlockState state) {
//		return metaFromType(state.getValue(TYPE));
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		super.getSubBlocks(itemIn, tab, list);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		// This method hopefully is ONLY called when placed manually in the world.
		// Auto-create slave state
		
		worldIn.setBlockState(pos.up(), this.getDefaultState()
				.withProperty(BLOCKFUNC, BlockFunction.TOP));
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		IBlockState state = this.getStateFromMeta(meta);
		if (isCenter(state)) {
			return new HomeBlockTileEntity(type);
		}
		
		return null;
	}
	
	public BlockPos getMasterPos(World world, BlockPos pos, IBlockState state) {
		if (state == null) {
			state = world.getBlockState(pos);
		}
		
		if (isCenter(state)) {
			return pos;
		}
		return pos.down();
	}
	
	public static boolean SpecializationMaterialAllowed(ResidentType type, FeyStoneMaterial material) {
		if (type == ResidentType.FAIRY) {
			return false;
		}
		
		if (!material.existsForSlot(FeySlotType.SPECIALIZATION)) {
			return false;
		}
		
		return true;
	}
	
	protected static FeyStoneMaterial[] GetSpecMaterials(ResidentType type) {
		ArrayList<FeyStoneMaterial> mats = new ArrayList<>();
		for (FeyStoneMaterial mat : FeyStoneMaterial.values()) {
			if (SpecializationMaterialAllowed(type, mat)) {
				mats.add(mat);
			}
		}
		
		return mats.toArray(new FeyStoneMaterial[Math.max(1, mats.size())]);
	}
	
	public static class HomeBlockTileEntity extends LogisticsTileEntity implements ITickable, IAetherHandlerProvider, IAetherComponentListener {
		
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
			
			protected boolean isValidSoulStone(@Nullable ItemStack stack) {
				if (stack != null && stack.getItem() instanceof FeySoulStone) {
					if (!FeySoulStone.getTypeOf(stack).canHold(owner.type)) {
						return false;
					}
					
					// Make sure any entity inside matches, too
					if (FeySoulStone.hasStoredFey(stack)) {
						return FeySoulStone.getStoredEntityType(stack) == owner.type;
					}
					
					return true;
				}
				
				return false;
			}
			
			protected boolean isValidSpecialization(@Nullable ItemStack stack) {
				if (stack != null && stack.getItem() instanceof IFeySlotted) {
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
			
			public @Nullable ItemStack getSoulStone(int index) {
				final int slot = getSoulSlot(index);
				return this.getStackInSlot(slot);
			}
			
			public boolean hasStone(int index) {
				@Nullable ItemStack stone = this.getSoulStone(index);
				return isValidSoulStone(stone);
			}
			
			public @Nullable ItemStack getSpecialization(int index) {
				final int slot = getSpecializationSlot(index);
				return this.getStackInSlot(slot);
			}
			
			@Override
			public boolean isItemValidForSlot(int slot, ItemStack stack) {
				if (stack == null) {
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
			
			public boolean setSoulStone(int index, ItemStack soulStone) {
				if (soulStone != null && !isValidSoulStone(soulStone)) {
					return false;
				}
				
				int slot = getSoulSlot(index);
				this.setInventorySlotContents(slot, soulStone);
				return true;
			}
			
			public boolean setSpecialization(int index, ItemStack specialization) {
				if (specialization != null && !isValidSpecialization(specialization)) {
					return false;
				}
				
				int slot = getSpecializationSlot(index);
				this.setInventorySlotContents(slot, specialization);
				return true;
			}
			
			private static final String NBT_NAME = "name";
			private static final String NBT_CUSTOM = "custom";
			private static final String NBT_ITEMS = "items";
			
			public NBTTagCompound toNBT() {
				NBTTagCompound nbt = new NBTTagCompound();
				
				nbt.setString(NBT_NAME, this.getName());
				nbt.setBoolean(NBT_CUSTOM, this.hasCustomName());
				nbt.setTag(NBT_ITEMS, Inventories.serializeInventory(this));
				
				return nbt;
			}
			
			public static HomeBlockSlotInventory fromNBT(HomeBlockTileEntity owner, NBTTagCompound nbt) {
				String name = nbt.getString(NBT_NAME);
				boolean custom = nbt.getBoolean(NBT_CUSTOM);
				
				HomeBlockSlotInventory inv = new HomeBlockSlotInventory(owner, GetSpecMaterials(owner.type), name, custom);
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
			
			protected boolean isValidUpgrade(@Nullable ItemStack stack) {
				if (stack != null && stack.getItem() instanceof IFeySlotted) {
					IFeySlotted stone = (IFeySlotted) stack.getItem();
					return stone.getFeySlot(stack) == FeySlotType.UPGRADE;
				}
				
				return false;
			}
			
			@Nullable
			public boolean isItemValidForSlot(int slot, ItemStack stack) {
				return slot < MAX_UPGRADES &&
						(stack == null || isValidUpgrade(stack));
			}
			
			@Override
			public void markDirty() {
				super.markDirty();
				owner.dirtyAndUpdate();
			}
			
			private static final String NBT_NAME = "name";
			private static final String NBT_CUSTOM = "custom";
			private static final String NBT_ITEMS = "items";
			
			public NBTTagCompound toNBT() {
				NBTTagCompound nbt = new NBTTagCompound();
				
				nbt.setString(NBT_NAME, this.getName());
				nbt.setBoolean(NBT_CUSTOM, this.hasCustomName());
				nbt.setTag(NBT_ITEMS, Inventories.serializeInventory(this));
				
				return nbt;
			}
			
			public static HomeBlockUpgradeInventory fromNBT(HomeBlockTileEntity owner, NBTTagCompound nbt) {
				String name = nbt.getString(NBT_NAME);
				boolean custom = nbt.getBoolean(NBT_CUSTOM);
				
				HomeBlockUpgradeInventory inv = new HomeBlockUpgradeInventory(owner, name, custom);
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
		protected Map<UUID, FeyAwayRecord> feyMap;
		protected HomeBlockSlotInventory slotInv;
		protected HomeBlockUpgradeInventory upgradeInv;
		private int ticksExisted;
		protected AetherHandlerComponent handler;
		
		private boolean aetherDirtyFlag;
		
		public HomeBlockTileEntity(ResidentType type) {
			this();
			this.type = type;
			this.slotInv = new HomeBlockSlotInventory(this, GetSpecMaterials(type), "", false);
		}
		
		public HomeBlockTileEntity() {
			super();
			//this.feyList = new HashSet<>();
			this.slots = DEFAULT_SLOTS;
			this.slotInv = new HomeBlockSlotInventory(this, GetSpecMaterials(ResidentType.FAIRY), "", false);
			this.upgradeInv = new HomeBlockUpgradeInventory(this, "", false);
			this.name = generateRandomName();
			feyMap = new HashMap<>();
			handler = new AetherHandlerComponent(this, 0, 500);
			handler.configureInOut(true, false);
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
		 * Returns the number of slots that ahve 
		 * @return
		 */
		public int getEffectiveSlots() {
			int count = 0;
			for (int i = 0; i < slots; i++) {
				if (slotInv.hasStone(i)) {
					count++;
				}
			}
			return count;
		}
		
		public int getBonusSlots() {
			return 0; // TODO
		}

		public void setSlots(int slots) {
			this.slots = slots;
		}
		
		public boolean isResident(EntityFeyBase fey) {
			UUID id = fey.getUniqueID();
			return this.feyMap.containsKey(id);
		}
		
		protected void refreshFeyList() {
			for (Entry<UUID, FeyAwayRecord> entry : feyMap.entrySet()) {
				entry.getValue().cache = null;
			}
			
			for (Entity ent : worldObj.loadedEntityList) {
				if (ent instanceof EntityFeyBase && feyMap.containsKey(ent.getUniqueID())) {
					FeyAwayRecord record = feyMap.get(ent.getUniqueID());
					record.cache = (EntityFeyBase) ent;
					record.tickLastSeen = ticksExisted;
				}
			}
		}
		
		protected void purgeFeyList() {
			Set<UUID> ids = Sets.newHashSet(feyMap.keySet());
			for (UUID id : ids) {
				FeyAwayRecord record = feyMap.get(id);
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
			List<EntityFeyBase> list = new ArrayList<>(feyMap.size());
			for (Entry<UUID, FeyAwayRecord> entry : feyMap.entrySet()) {
				list.add(entry.getValue().cache);
			}
			return list;
		}
		
		public Map<UUID, FeyAwayRecord> getFeyEntries() {
			return feyMap;
		}
		
		public boolean canAccept(EntityFeyBase fey) {
			if (feyMap.size() >= this.getEffectiveSlots()) {
				return false;
			}
			
			return fey.getHomeType() == this.type;
		}
		
		public boolean addResident(EntityFeyBase fey) {
			if (!canAccept(fey)) {
				return false;
			}
			
			FeyAwayRecord record = new FeyAwayRecord();
			record.tickLastSeen = ticksExisted;
			record.name = fey.getName();
			record.cache = fey;
			
			this.feyMap.put(fey.getUniqueID(), record);
			dirtyAndUpdate();
			return true;
		}
		
		public void removeResident(UUID id) {
			this.feyMap.remove(id);
			dirtyAndUpdate();
		}
		
		public void removeResident(EntityFeyBase fey) {
			removeResident(fey.getUniqueID());
		}
		
		public HomeBlockSlotInventory getSlotInventory() {
			return this.slotInv;
		}
		
		public HomeBlockUpgradeInventory getUpgradeInventory() {
			return this.upgradeInv;
		}
		
		protected int getAetherCost() {
			int AETHER_PER_TICK = 1;
			return AETHER_PER_TICK * this.getAvailableFeyEntities().size();
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
			
			this.growth += adjustGrowth(growth);
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
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			nbt.setString(NBT_TYPE, type.name());
			nbt.setString(NBT_NAME, getName());
			nbt.setInteger(NBT_SLOT_COUNT, slots);
			nbt.setFloat(NBT_SLOT_GROWTH, growth);
			
			NBTTagList list = new NBTTagList();
			for (Entry<UUID, FeyAwayRecord> entry : feyMap.entrySet()) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString(NBT_FEY_NAME, entry.getValue().name);
				tag.setString(NBT_FEY_UUID, entry.getKey().toString());
				list.appendTag(tag);
			}
			nbt.setTag(NBT_FEY, list);
			
			nbt.setTag(NBT_UPGRADES, this.upgradeInv.toNBT());
			nbt.setTag(NBT_SLOT_INV, slotInv.toNBT());
			nbt.setTag(NBT_HANDLER, handler.writeToNBT(new NBTTagCompound()));
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			String type = nbt.getString(NBT_TYPE);
			this.type = ResidentType.valueOf(ResidentType.class, type.toUpperCase());
			this.name = nbt.getString(NBT_NAME);
			if (nbt.hasKey(NBT_SLOT_COUNT, NBT.TAG_INT)) {
				this.slots = Math.max(1, Math.min(MAX_NATURAL_SLOTS, nbt.getInteger(NBT_SLOT_COUNT)));
			} else {
				this.slots = DEFAULT_SLOTS; 
			}
			this.growth = nbt.getFloat(NBT_SLOT_GROWTH);
			
			feyMap.clear();
			NBTTagList list = nbt.getTagList(NBT_FEY, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				FeyAwayRecord record = new FeyAwayRecord();
				record.tickLastSeen = ticksExisted;
				record.name = tag.getString(NBT_FEY_NAME);
				feyMap.put(UUID.fromString(tag.getString(NBT_FEY_UUID)), record);
			}
			if (this.worldObj != null) {
				this.refreshFeyList();
			}
			
			this.slotInv = HomeBlockSlotInventory.fromNBT(this, nbt.getCompoundTag(NBT_SLOT_INV));
			this.upgradeInv = HomeBlockUpgradeInventory.fromNBT(this, nbt.getCompoundTag(NBT_UPGRADES));
			handler.readFromNBT(nbt.getCompoundTag(NBT_HANDLER));
		}

		@Override
		public void update() {
			ticksExisted++;
			
			if (!worldObj.isRemote && this.ticksExisted == 1) {
				this.handler.setAutoFill(true);
			}
			
			if (this.ticksExisted % 20 == 0) {
				// Check on fey. Are they missing?
				refreshFeyList();
				if (!worldObj.isRemote) {
					purgeFeyList();
				}
			}
			
			// If any soul gems in our inventory have souls in them, free them!
			for (int i = 0; i < this.slotInv.getSizeInventory(); i++) {
				if (HomeBlockSlotInventory.isSoulSlot(i) && slotInv.hasStone(i)) {
					ItemStack stone = slotInv.getStackInSlot(i);
					if (FeySoulStone.hasStoredFey(stone)) {
						EntityFeyBase fey = FeySoulStone.spawnStoredEntity(stone, worldObj, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
						fey.setHome(this.getPos());
						slotInv.setInventorySlotContents(i, FeySoulStone.clearEntity(stone));
					}
				}
			}
			
			if (!worldObj.isRemote) {
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
			
			if (!worldObj.isRemote && aetherDirtyFlag && (ticksExisted == 1 || ticksExisted % 5 == 0)) {
				dirtyAndUpdate();
				aetherDirtyFlag = false;
			}
			
		}
		
		protected void dirtyAndUpdate() {
			markDirty();
			if (worldObj != null && !worldObj.isRemote) {
				worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 2);
			}
		}
		
		@Override
		public void markDirty() {
			super.markDirty();
		}
		
		@Override
		public void addConnections(List<AetherFlowConnection> connections) {
			for (EnumFacing dir : EnumFacing.values()) {
				if (!handler.getSideEnabled(dir)) {
					continue;
				}
				
				BlockPos neighbor = pos.offset(dir);
				
				// First check for a TileEntity
				TileEntity te = worldObj.getTileEntity(neighbor);
				if (te != null && te instanceof IAetherHandler) {
					connections.add(new AetherFlowConnection((IAetherHandler) te, dir.getOpposite()));
					continue;
				}
				if (te != null && te instanceof IAetherHandlerProvider) {
					connections.add(new AetherFlowConnection(((IAetherHandlerProvider) te).getHandler(), dir.getOpposite()));
					continue;
				}
				
				// See if block boasts being able to get us a handler
				IBlockState attachedState = worldObj.getBlockState(neighbor);
				Block attachedBlock = attachedState.getBlock();
				if (attachedBlock instanceof IAetherCapableBlock) {
					connections.add(new AetherFlowConnection(((IAetherCapableBlock) attachedBlock).getAetherHandler(worldObj, attachedState, neighbor, dir), dir.getOpposite()));
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
}
