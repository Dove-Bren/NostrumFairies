package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
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
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker.FairyGeneralStatus;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.inventory.IFeySlotted;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
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
		return new BlockStateContainer(this, BLOCKFUNC, FACING);
	}
	
	private BlockFunction functionFromMeta(int meta) {
		int raw = (meta & 1);
		return BlockFunction.values()[raw];
	}
	
	private int metaFromFunc(BlockFunction func) {
		return (func.ordinal() & 1);
	}
	
	private EnumFacing facingFromMeta(int meta) {
		int raw = (meta >> 1) & 0x3;
		return EnumFacing.HORIZONTALS[raw];
	}
	
	private int metaFromFacing(EnumFacing facing) {
		return facing.getHorizontalIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(BLOCKFUNC, functionFromMeta(meta))
				.withProperty(FACING, facingFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return metaFromFunc(state.getValue(BLOCKFUNC)) | metaFromFacing(state.getValue(FACING));
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
		return true;
	}
	
	@Override
	public boolean isFullyOpaque(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return true;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
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
	
	@Override
	public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
		return true;
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
			
			// Slot inventory and upgrade inventories
			for (IInventory inv : new IInventory[] {te.slotInv, te.upgradeInv}) {
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					if (inv.getStackInSlot(i) != null) {
						EntityItem item = new EntityItem(
								world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
								inv.removeStackFromSlot(i));
						world.spawnEntityInWorld(item);
					}
				}
			}
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
		
		for (BlockPos cursor : new BlockPos[] {pos, pos.up(), pos.up().up()}) {
			if (cursor.getY() > 255 || cursor.getY() <= 0) {
				return false;
			}
			
			Block block = worldIn.getBlockState(cursor).getBlock();
			if (!block.isReplaceable(worldIn, cursor)
					&& !(block instanceof BlockLeaves)) {
				return false;
			}
			
			if (worldIn.getTileEntity(cursor) != null) {
				return false;
			}
		}
		
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return this.getDefaultState()
				.withProperty(BLOCKFUNC, BlockFunction.CENTER)
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite());
				
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
		EnumFacing facing = state.getValue(FACING);
		this.spawn(worldIn, pos, type, facing);
		
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
	
	/**
	 * Spawns a home block tree at the provided space. Skips the base block.
	 * @param world
	 * @param base
	 * @param type
	 */
	public void spawn(World world, BlockPos base, ResidentType type, EnumFacing direction) {
		//world.setBlockState(base, getDefaultState().withProperty(BLOCKFUNC, BlockFunction.CENTER).withProperty(FACING, direction));
		world.setBlockState(base.up(), getDefaultState().withProperty(BLOCKFUNC, BlockFunction.TOP).withProperty(FACING, direction)); // could be random
		world.setBlockState(base.up().up(), Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK));
		
		IBlockState leaves = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK);
		BlockPos origin = base.up().up();
		for (BlockPos cursor : new BlockPos[] {
				// Layer 1
				origin.north().north(),
				origin.north().east(), origin.north(), origin.north().west(),
				origin.east().east(), origin.east(), origin.west(), origin.west().west(),
				origin.south().east(), origin.south(), origin.south().west(),
				origin.south().south(),
				
				// Layer 2
				origin.up().north().east(), origin.up().north(), origin.up().north().west(),
				origin.up().east().east(), origin.up().east(), origin.up(), origin.up().west(), origin.up().west().west(),
				origin.up().south().east(), origin.up().south(), origin.up().south().west(),
				
				// Layer 3
				origin.up().up().north(),
				origin.up().up().east(), origin.up().up(), origin.up().up().west(),
				origin.up().up().south(),
		}) {
			world.setBlockState(cursor, leaves);
		}
	}
	
	public static boolean SpecializationMaterialAllowed(ResidentType type, FeyStoneMaterial material) {
		if (!material.existsForSlot(FeySlotType.SPECIALIZATION)) {
			return false;
		}
		
		return EntityFeyBase.canUseSpecialization(type, material);
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
						return FeySoulStone.getStoredFeyType(stack) == owner.type;
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
					return stone.getFeySlot(stack) == FeySlotType.UPGRADE
							|| stone.getFeySlot(stack) == FeySlotType.DOWNGRADE;
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
		protected UUID[] feySlots;
		protected HashMap<UUID, FeyAwayRecord> feyCacheMap;
		protected HomeBlockSlotInventory slotInv;
		protected HomeBlockUpgradeInventory upgradeInv;
		private int ticksExisted;
		protected AetherHandlerComponent handler;
		
		private boolean aetherDirtyFlag;
		private List<BlockPos> boostBlockSpots; // TODO make sparkles every once in a while?
		
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
			ItemStack specialization = this.slotInv.getSpecialization(idx);
			FeyStoneMaterial specMat = FeyStone.instance().getStoneMaterial(specialization);
			if (fey.getCurrentSpecialization() != specMat) {
				fey = fey.switchToSpecialization(specMat);
			}
			
			return fey;
		}
		
		protected void refreshFeyList() {
			for (Entry<UUID, FeyAwayRecord> entry : feyCacheMap.entrySet()) {
				entry.getValue().cache = null;
			}
			
			List<Entity> ents = Lists.newArrayList(worldObj.loadedEntityList);
			
			for (Entity ent : ents) {
				if (ent instanceof EntityFeyBase && feyCacheMap.containsKey(ent.getUniqueID())) {
					FeyAwayRecord record = feyCacheMap.get(ent.getUniqueID());
					int idx = findFeySlot(ent.getUniqueID());
					record.cache = refreshFey(idx, (EntityFeyBase) ent);
					record.tickLastSeen = ticksExisted;
				}
			}
		}
		
		protected void purgeFeyList() {
			Set<UUID> ids = Sets.newHashSet(feyCacheMap.keySet());
			for (UUID id : ids) {
				FeyAwayRecord record = feyCacheMap.get(id);
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
			for (Entry<UUID, FeyAwayRecord> entry : feyCacheMap.entrySet()) {
				list.add(entry.getValue().cache);
			}
			return list;
		}
		
		public Map<UUID, FeyAwayRecord> getFeyEntries() {
			return this.feyCacheMap;
		}
		
		public List<FeyAwayRecord> getFeySlots() {
			List<FeyAwayRecord> records = new ArrayList<>(this.getTotalSlots());
			for (int i = 0; i < getTotalSlots(); i++) {
				final FeyAwayRecord record;
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
			if (worldObj == null || worldObj.isRemote) {
				return false;
			}
			
			if (!canAccept(fey)) {
				return false;
			}
			
			int index = findFreeIdx();
			if (index == -1) {
				throw new RuntimeException("Fey cache and slot array are out of sync");
			}
			
			FeyAwayRecord record = new FeyAwayRecord();
			record.tickLastSeen = ticksExisted;
			record.name = fey.getName();
			record.cache = fey;
			
			this.feyCacheMap.put(fey.getUniqueID(), record);
			this.feySlots[index] = fey.getUniqueID();
			dirtyAndUpdate();
			return true;
		}
		
		public void removeResident(UUID id) {
			if (worldObj == null || worldObj.isRemote) {
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
			if (worldObj == null || worldObj.isRemote) {
				return;
			}
			
			if (original == null || replacement == null || !original.getUniqueID().equals(replacement.getUniqueID())) {
				return;
			}
			
			FeyAwayRecord record = new FeyAwayRecord();
			record.tickLastSeen = ticksExisted;
			record.name = replacement.getName();
			record.cache = replacement;
			
			this.feyCacheMap.put(replacement.getUniqueID(), record);

			int idx = findFeySlot(original.getUniqueID());
			this.feySlots[idx] = replacement.getUniqueID();
			dirtyAndUpdate();
		}
		
		public HomeBlockSlotInventory getSlotInventory() {
			return this.slotInv;
		}
		
		public HomeBlockUpgradeInventory getUpgradeInventory() {
			return this.upgradeInv;
		}
		
		public int getUpgradeCount(FeySlotType slot, FeyStoneMaterial material) {
			int count = 0;
			for (int i = 0; i < upgradeInv.getSizeInventory(); i++) {
				ItemStack stack = upgradeInv.getStackInSlot(i);
				if (stack != null && FeyStone.instance().getFeySlot(stack) == slot && FeyStone.instance().getStoneMaterial(stack) == material) {
					count++;
				}
			}
			return count;
		}
		
		protected int getAetherCost() {
			// Ruby downgrades decrease aether cost
			final float skipChance = .3f * getUpgradeCount(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY);
			if (skipChance > 0 && NostrumFairies.random.nextFloat() < skipChance) {
				return 0;
			}
			
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
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			nbt.setString(NBT_TYPE, type.name());
			nbt.setString(NBT_NAME, getName());
			nbt.setInteger(NBT_SLOT_COUNT, slots);
			nbt.setFloat(NBT_SLOT_GROWTH, growth);
			
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < getEffectiveSlots(); i++) {
				UUID id = feySlots[i];
				NBTTagCompound tag = new NBTTagCompound();
				if (id != null) {
					FeyAwayRecord record = this.feyCacheMap.get(id);
					tag.setString(NBT_FEY_NAME, record.name);
					tag.setString(NBT_FEY_UUID, id.toString());
				}
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
			
			this.slotInv = HomeBlockSlotInventory.fromNBT(this, nbt.getCompoundTag(NBT_SLOT_INV));
			this.upgradeInv = HomeBlockUpgradeInventory.fromNBT(this, nbt.getCompoundTag(NBT_UPGRADES));
			
			feyCacheMap.clear();
			NBTTagList list = nbt.getTagList(NBT_FEY, NBT.TAG_COMPOUND);
			for (int i = 0; i < getEffectiveSlots(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				UUID id = null;
				if (tag != null && tag.hasKey(NBT_FEY_UUID)) {
					id = UUID.fromString(tag.getString(NBT_FEY_UUID));
					FeyAwayRecord record = new FeyAwayRecord();
					record.tickLastSeen = ticksExisted;
					record.name = tag.getString(NBT_FEY_NAME);
					feyCacheMap.put(id, record);
				}
				this.feySlots[i] = id;
			}
			if (this.worldObj != null) {
				this.refreshFeyList();
			}
			
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
			
			if (!worldObj.isRemote) {
				//If any soul gems in our inventory have souls in them, free them!
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
			
			if (!worldObj.isRemote && aetherDirtyFlag && (ticksExisted == 1 || ticksExisted % 5 == 0)) {
				dirtyAndUpdate();
				aetherDirtyFlag = false;
			}
			
			if (!worldObj.isRemote && ticksExisted % 100 == 0 && worldObj.isDaytime()) {
				if (NostrumFairies.random.nextFloat() < .2f) {
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
				
				// vfx
				Iterator<BlockPos> it = boostBlockSpots.iterator();
				while (it.hasNext()) {
					BlockPos pos = it.next();
					IBlockState state = worldObj.getBlockState(pos);
					if (getBlockSpawnBonus(pos, state) <= 0) {
						it.remove();
						continue;
					}
					
					if (NostrumFairies.random.nextFloat() < .2f) {
						((WorldServer) worldObj).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
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
				
				IBlockState state = worldObj.getBlockState(cursor);
				final float boost = getBlockSpawnBonus(cursor, state);
				bonus += boost;
				if (boost > 0f) {
					boostBlockSpots.add(cursor.toImmutable());
				}
			}
			return bonus;
		}
		
		protected float getBlockSpawnBonus(BlockPos pos, IBlockState state) {
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
				
				while (targ.getY() > 0 && worldObj.isAirBlock(targ)) {
					targ = targ.down();
				}
				if (targ.getY() < 256) {
					targ = targ.up();
				}
				
				// We've hit a non-air block. Make sure there's space above it
				if (worldObj.isAirBlock(targ.up())) {
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
				if (type == 0) fey = new EntityDwarf(worldObj);
				else if (type == 1) fey = new EntityDwarfBuilder(worldObj);
				else fey = new EntityDwarfCrafter(worldObj);
				break;
			case ELF:
				if (type == 0) fey = new EntityElf(worldObj);
				else if (type == 1) fey = new EntityElfArcher(worldObj);
				else fey = new EntityElfCrafter(worldObj);
				break;
			case FAIRY:
			default:
				fey = new EntityFairy(worldObj);
				break;
			case GNOME:
				if (type == 0) fey = new EntityGnome(worldObj);
				else if (type == 1) fey = new EntityGnomeCrafter(worldObj);
				else fey = new EntityGnomeCollector(worldObj);
				break;
			}
			
			fey.setPosition(pos.getX() + .5, pos.getY() + .05, pos.getZ() + .5);
			worldObj.spawnEntityInWorld(fey);
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
