package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.FeySignRenderer;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskMineBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlaceBlock;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.OreDict;
import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.ManiOre;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class MiningBlock extends BlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final String ID = "logistics_mining_block";
	public static final int WORKER_REACH = 16;
	public static final int MAJOR_LEVEL_DIFF = 16;
	public static final int PLATFORM_WIDTH = 3;
	public static final int STAIRCASE_RADIUS = 4;
	public static final int SHAFT_DISTANCE = 4;
	
	private static MiningBlock instance = null;
	public static MiningBlock instance() {
		if (instance == null)
			instance = new MiningBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(MiningBlockTileEntity.class, "logistics_mining_block_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public MiningBlock() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setLightOpacity(2);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	protected static int metaFromFacing(EnumFacing facing) {
		return facing.getHorizontalIndex();
	}
	
	protected static EnumFacing facingFromMeta(int meta) {
		return EnumFacing.getHorizontal(meta);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, facingFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return metaFromFacing(state.getValue(FACING));
	}
	
	public EnumFacing getFacing(IBlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return this.getDefaultState()
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(FACING).getHorizontalIndex() % 2 == 0) {
			return IFeySign.AABB_NS;
		} else {
			return IFeySign.AABB_EW;
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		if (blockState.getValue(FACING).getHorizontalIndex() % 2 == 0) {
			return IFeySign.AABB_NS;
		} else {
			return IFeySign.AABB_EW;
		}
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		IBlockState state = worldIn.getBlockState(pos.down());
		if (state == null || !(state.isSideSolid(worldIn, pos.down(), EnumFacing.UP))) {
			return false;
		}
		
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		if (!canPlaceBlockAt(worldIn, pos) && !state.getBlock().equals(this)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		
		super.neighborChanged(state, worldIn, pos, blockIn);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	public static class MiningBlockTileEntity extends LogisticsTileEntity implements ITickable, ILogisticsTaskListener, IFeySign {

		private int tickCount;
		private Map<BlockPos, ILogisticsTask> taskMap;
		private LogisticsItemWithdrawRequester materialRequester;
		private int radius;
		
		// Persisted data
		private ItemStack buildingMaterial;
		private ItemStack torches;
		private Set<BlockPos> beacons; // Beacons we've placed (for re-placement on load) 
		
		private int chunkXOffset; // rediscovered each time
		private int chunkZOffset; // ^
		private int lowestLevel; // ^
		private int scanLevel; // reset to 0 on load but that should mean 1-time long scan
		private int nextPlatform; // ^
		private int scanProgress; // When we early-out of a scan, where to pick up next time
		private int platformRequests; // Number of platform blocks currently needed
		
		// Rendering variables
		protected Set<BlockPos> oreLocations;
		protected Set<BlockPos> repairLocations;
		
		public MiningBlockTileEntity() {
			this(32);
		}
		
		public MiningBlockTileEntity(int blockRadius) {
			super();
			this.radius = blockRadius;
			taskMap = new HashMap<>();
			oreLocations = new HashSet<>();
			repairLocations = new HashSet<>();
			beacons = new HashSet<>();
			
			lowestLevel = -1;
			scanLevel = 1;
		}
		
		@Override
		public double getDefaultLogisticsRange() {
			return radius;
		}

		@Override
		public double getDefaultLinkRange() {
			return 10;
		}

		@Override
		public boolean canAccept(List<ItemDeepStack> stacks) {
			return false;
		}
		
		@Override
		public void takeItem(ItemStack stack) {
			if (buildingMaterial != null && ItemStacks.stacksMatch(stack, this.buildingMaterial)) {
				this.buildingMaterial.stackSize -= stack.stackSize;
				if (buildingMaterial.stackSize <= 0) {
					buildingMaterial = null;
				}
			} else if (torches != null && ItemStacks.stacksMatch(stack, this.torches)) {
				this.torches.stackSize -= stack.stackSize;
				if (torches.stackSize <= 0) {
					torches = null;
				}
			}
			this.dirty();
		}
		
		@Override
		public void addItem(ItemStack stack) {
			// Less likely to be torches lol
			if (torches != null && ItemStacks.stacksMatch(stack, this.torches)) {
				torches.stackSize = Math.min(torches.getMaxStackSize(), torches.stackSize + stack.stackSize); 
			} else if (buildingMaterial != null && ItemStacks.stacksMatch(stack, this.buildingMaterial)) {
				buildingMaterial.stackSize = Math.min(buildingMaterial.getMaxStackSize(), buildingMaterial.stackSize + stack.stackSize);
			} else if (buildingMaterial == null && isMaterials(stack)) {
				this.buildingMaterial = stack;
			}
			this.dirty();
		}
		
		private void refreshRequester() {
			if (this.materialRequester == null) {
				materialRequester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent); // TODO make using buffer chests configurable!
				materialRequester.addChainListener(this);
			}
			materialRequester.updateRequestedItems(getItemRequests());
		}
		
		@Override
		protected void setNetworkComponent(LogisticsTileEntityComponent component) {
			super.setNetworkComponent(component);
			
			if (worldObj != null && !worldObj.isRemote) {
				if (component != null) {
					if (materialRequester != null) {
						materialRequester.setNetwork(component.getNetwork());
					}
					refreshRequester();
				}
			}
		}
		
		@Override
		public void onLeaveNetwork() {
			if (!worldObj.isRemote) {
				if (materialRequester != null) {
					materialRequester.clearRequests();
					materialRequester.setNetwork(null);
				}
				
				if (this.getNetwork() != null) {
					for (ILogisticsTask task : this.taskMap.values()) {
						this.getNetwork().getTaskRegistry().revoke(task);
					}
					for (BlockPos beacon : beacons) {
						this.getNetwork().removeBeacon(worldObj, beacon);
					}
				}
			}
			
			super.onLeaveNetwork();
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			if (!worldObj.isRemote) {
				if (materialRequester != null) {
					materialRequester.setNetwork(network);
					refreshRequester();
				}
				
				// add tasks and beacons to this network
				for (BlockPos beacon : beacons) {
					this.getNetwork().addBeacon(worldObj, beacon);
				}
				
				for (ILogisticsTask task : this.taskMap.values()) {
					this.getNetwork().getTaskRegistry().register(task, null);
				}
			}
			
			super.onJoinNetwork(network);
		}
		
		protected ItemStack getRepairStack() {
			return new ItemStack(Blocks.COBBLESTONE);
		}
		
		protected List<ItemStack> getItemRequests() {
			ItemStack base;
			LogisticsNetwork network = this.getNetwork();
			
			if (network == null) {
				return new ArrayList<>();
			}
			
			if (this.platformRequests == 0) {
				return new ArrayList<>();
			}
			
			
			int existing = 0;
			if (this.buildingMaterial != null) {
				base = this.buildingMaterial;
				existing = base.stackSize;
			} else {
				// If we're auto-fetching, just take cobble
				base = getRepairStack();
			}
			
			List<ItemStack> list = new ArrayList<>(platformRequests);
			int count = platformRequests - existing;
			while (count > 0) {
				ItemStack stack = base.copy();
				stack.stackSize = Math.min(count, stack.getMaxStackSize());
				count -= stack.stackSize;
				list.add(stack);
			}
			
			return list;
		}
		
		private void addBeacon(BlockPos pos) {
			beacons.add(pos);
			LogisticsNetwork network = this.getNetwork();
			if (network != null) {
				network.addBeacon(this.worldObj, pos);
			}
		}
		
		private void makeMineTask(BlockPos pos, @Nullable LogisticsTaskMineBlock[] prereqs) {
			makeMineTask(pos, null, prereqs);
		} 
		
		private void makeMineTask(BlockPos pos, BlockPos mineAt, @Nullable LogisticsTaskMineBlock[] prereqs) {
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			pos = pos.toImmutable();
			
			if (!taskMap.containsKey(pos)) {
				// Note: I'm using different constructors so that the mine task could change someday to do different things depending
				// on the constructor
				LogisticsTaskMineBlock task;
				if (mineAt == null) {
					task = new LogisticsTaskMineBlock(this.getNetworkComponent(), "Mining Task", worldObj, pos, prereqs);
				} else {
					task = new LogisticsTaskMineBlock(this.getNetworkComponent(), "Mining Task", worldObj, pos, mineAt.toImmutable(), prereqs);
				}
				this.taskMap.put(pos, task);
				network.getTaskRegistry().register(task, null);
				this.markDirty();
			}
		}
		
		private void makeRepairTask(BlockPos pos, @Nullable BlockPos standAt) {
			// Actually need two tasks: one to request the material, and one to go place it
			
			// so make some other list that is our item list and update requester.
			// and hook up to the logistics interface to expose those items to dwarves
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			pos = pos.toImmutable();
			if (!taskMap.containsKey(pos)) {
				// Request platform material
				platformRequests++;
				refreshRequester();
				
				// Request block placement
				LogisticsTaskPlaceBlock task;
				IBlockState state = Blocks.COBBLESTONE.getDefaultState();
				if (this.buildingMaterial != null) {
					if (buildingMaterial.getItem() instanceof ItemBlock) {
						// I don't trust the null entity in there...
						ItemBlock itemBlock = (ItemBlock) buildingMaterial.getItem();
						try {
							int meta = itemBlock.getMetadata(buildingMaterial.getMetadata());
							state = itemBlock.block.getStateForPlacement(worldObj, pos, EnumFacing.UP, 0, 0, 0, meta, null, buildingMaterial.copy());
						} catch (Exception e) {
							// fall back to default state
							state = itemBlock.block.getDefaultState(); 
						}
					}
				}
				if (standAt == null) {
					task = new LogisticsTaskPlaceBlock(this.getNetworkComponent(), "Mine Repair Task",
							getRepairStack(), state,
							worldObj, pos);
				} else {
					task = new LogisticsTaskPlaceBlock(this.getNetworkComponent(), "Mine Repair Task",
							getRepairStack(), state,
							worldObj, pos, standAt.toImmutable());
				}
						
				
				this.taskMap.put(pos, task);
				network.getTaskRegistry().register(task, null);
				this.repairLocations.add(pos);
			}
			
			
		}
		
		private void removeTask(BlockPos base) {
			ILogisticsTask task = taskMap.remove(base);
			if (task == null) {
				// We call this freely from event handling. Ignore it.
				return;
			}
			
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			network.getTaskRegistry().revoke(task);
			oreLocations.remove(base);
			repairLocations.remove(base);
			this.dirty();
		}
		
		private int getYFromLevel(int level) {
			return pos.getY() - level;
		}
		
//		private int levelFromY(int y) {
//			return Math.max(0, pos.getY() - y);
//		}
		
		private int platformToY(int platform) {
			return getYFromLevel(platformToLevel(platform));
		}
		
		private int platformToLevel(int platform) {
			return platform * (MAJOR_LEVEL_DIFF / 2);
		}
		
		/**
		 * Get the PLATFORM level (not the regular level) for the given Y
		 * @param y
		 * @return
		 */
		private int platformForY(int y) {
			// each platform is worth MAJOR_LEVEL_DIFF / 2
			// We want the one below it, but sometimes we can't
			final int lowest = getLowestLevel();
			if (lowest == 0) {
				return 0;
			}
			
			int platform = (int) Math.ceil((pos.getY() - y) / (double) (MAJOR_LEVEL_DIFF / 2));
			int level = platformToLevel(platform); 
			
			// if platform is unavailable, do the next one up
			if (level > lowest) {
				platform -= 1;
			}
			
			return platform;
		}
		
		private boolean forEachOnLayer(int level, Function<MutableBlockPos, Boolean> func) {
			final int startX = this.pos.getX() + chunkXOffset - radius;
			final int endX = this.pos.getX() + chunkXOffset + radius;
			final int startZ = this.pos.getZ() + chunkZOffset - radius;
			final int endZ = this.pos.getZ() + chunkZOffset + radius;
			final int y = getYFromLevel(level);
			
			MutableBlockPos cursor = new MutableBlockPos();
			for (int x = startX; x <= endX; x++)
			for (int z = startZ; z <= endZ; z++) {
				cursor.setPos(x, y, z);
				if (!func.apply(cursor)) {
					return false;
				}
			}
			
			return true;
		}
		
		private boolean isIgnorableBlock(BlockPos pos) {
			IBlockState state = worldObj.getBlockState(pos);
			// We care if it's something that doesn't block movement and isn't a liquid source block
			if (state.getMaterial().blocksMovement()) {
				return false;
			}
			if (state.getMaterial().isLiquid()) {
				if (state.getBlock() == Blocks.FLOWING_WATER || state.getBlock() == Blocks.FLOWING_LAVA) {
					return true;
				}
			}
			
			return true;
		}
		
		private boolean isEmpty(BlockPos pos) {
			return worldObj.isAirBlock(pos)
					|| worldObj.getBlockState(pos).getBlock() instanceof MagicLight
					|| worldObj.getBlockState(pos).getBlock() instanceof BlockTorch
					|| isIgnorableBlock(pos);
		}
		
		/**
		 * Makes sure the provided location is a 1x3x1 space with a block underneath
		 * @param base
		 */
		private boolean clearBlock(BlockPos base, boolean tall, @Nullable BlockPos lastPos) {
			// should base be the block underneath or the first air block??
			
			base = base.toImmutable();
			boolean tasked = false;
			
			// check underneath
			if (!worldObj.isSideSolid(base.down(), EnumFacing.UP)) {
				makeRepairTask(base.down(), lastPos);
				tasked = true;
			}
			
			// And above and sides (but only if there's liquid flowing in
			BlockPos[] around;
			if (lastPos == null) {
				around = new BlockPos[]{tall ? base.up().up().up() : base.up().up()};
			} else {
				// Figure out which ways are the sides
				if (lastPos.getX() == base.getX()) {
					if (tall) {
						around = new BlockPos[] {base.up().up().up(),
								base.east(), base.east().up(), base.east().up().up(),
								base.west(), base.west().up(), base.west().up().up()};
					} else {
						around = new BlockPos[] {base.up().up(),
								base.east(), base.east().up(), base.west(), base.west().up()};
					}
				} else {
					if (tall) {
						around = new BlockPos[] {base.up().up().up(),
								base.north(), base.north().up(), base.north().up().up(),
								base.south(), base.south().up(), base.south().up().up()};
					} else {
						around = new BlockPos[] {base.up().up().up(),
								base.north(), base.north().up(), base.south(), base.south().up()};
					}
				}
				
			}
			for (BlockPos aroundCurs : around) {
				if (worldObj.getBlockState(aroundCurs).getMaterial().isLiquid()) {
					makeRepairTask(aroundCurs, lastPos);
				}
			}
			
			
			// check for 2 or 3 high air
			// Make a prereq from lastPos if it's there.
			// TODO could try and use above and below blocks as prereqs? Would that help?
			MutableBlockPos lastCursor = lastPos == null ? null : new MutableBlockPos(lastPos);
			BlockPos[] range = (tall ? new BlockPos[]{base, base.up(), base.up().up(), base.up().up()} : new BlockPos[]{base, base.up(), base.up()});
			for (BlockPos pos : range) {
				if (!isEmpty(pos)) {
					LogisticsTaskMineBlock[] prereq = null;
					if (lastCursor != null) {
						lastCursor.setY(pos.getY());
						ILogisticsTask otherTask = taskMap.get(lastCursor);
						prereq = (otherTask == null || !(otherTask instanceof LogisticsTaskMineBlock) ? null
								: new LogisticsTaskMineBlock[]{(LogisticsTaskMineBlock)otherTask});
					}
					makeMineTask(pos, prereq);
					tasked = true;
				}
			}
//			if (!isEmpty(base.up())) {
//				ILogisticsTask otherTask = (lastPos == null ? null : taskMap.get(lastCursor));
//				LogisticsTaskMineBlock[] prereq = (otherTask == null || !(otherTask instanceof LogisticsTaskMineBlock) ? null
//						: new LogisticsTaskMineBlock[]{(LogisticsTaskMineBlock)otherTask});
//				
//				makeMineTask(base.up(), prereq);
//				tasked = true;
//			}
//			if (tall) {
//				if (!isEmpty(base.up().up())) {
//					ILogisticsTask otherTask = (lastPos == null ? null : taskMap.get(lastCursor));
//					LogisticsTaskMineBlock[] prereq = (otherTask == null || !(otherTask instanceof LogisticsTaskMineBlock) ? null
//							: new LogisticsTaskMineBlock[]{(LogisticsTaskMineBlock)otherTask});
//					
//					makeMineTask(base.up().up(), prereq);
//					tasked = true;
//				}
//			}
			
			return tasked;
		}
		
		/**
		 * Create the shafts required to get to where we can mine the provided pos
		 * @param pos
		 */
		private void mineTo(BlockPos pos) {
			
			// Get platform level. Then move to the right X. Then mine up to the required Z.
			// If X isn't beyond the platform, make sure to adjust where we 'start' at the right spot.
			// Note X is rounded to 4 to spread shafts out
			MutableBlockPos cursor = new MutableBlockPos();
			final int effX = pos.getX() & ~3; // chop off bottom 2 bits, rounding down to nearest 4 
			int y = platformToY(platformForY(pos.getY()));
			int x;
			int z;
			int centerX = this.pos.getX();
			int centerZ = this.pos.getZ();
			boolean outside = true; // outside the platform ring
			boolean inside = false;
			if (effX >= centerX - PLATFORM_WIDTH && effX <= centerX + PLATFORM_WIDTH
					&& pos.getZ() >= centerZ - PLATFORM_WIDTH && pos.getZ() <= centerZ + PLATFORM_WIDTH) {
				inside = true;
				// inner staircase ring. Mine on perimeter of staircase
				x = (effX < centerX) ? centerX - (PLATFORM_WIDTH + 1) : centerX + (PLATFORM_WIDTH + 1);
				z = centerZ;
			} else if (effX <= centerX - (PLATFORM_WIDTH + STAIRCASE_RADIUS)) {
				x = centerX - (PLATFORM_WIDTH + STAIRCASE_RADIUS);
				z = centerZ;
			} else if (effX >= centerX + (PLATFORM_WIDTH + STAIRCASE_RADIUS)) {
				x = centerX + (PLATFORM_WIDTH + STAIRCASE_RADIUS);
				z = centerZ;
			} else if (pos.getZ() <= centerZ - (PLATFORM_WIDTH + STAIRCASE_RADIUS)) {
				x = effX;
				z = centerZ - (PLATFORM_WIDTH + STAIRCASE_RADIUS);
			} else if (pos.getZ() >= centerZ + (PLATFORM_WIDTH + STAIRCASE_RADIUS)) {
				x = effX;
				z = centerZ + (PLATFORM_WIDTH + STAIRCASE_RADIUS);
			} else {
				// inside the platform area??
				outside = false;
				x = effX;
				z = pos.getZ();
			}
			
			cursor.setPos(x, y, z);
			if (!inside) {
				
				// Set the start location (and create a beacon!)
				if (outside) {
					this.addBeacon(cursor.toImmutable());
				}
				
				// Keep track of last for platform building
				MutableBlockPos last = new MutableBlockPos(cursor);
				
				// First, get to the right x
				int spaces = 0;
				while (cursor.getX() != effX) {
					clearBlock(cursor, false, last);
					last.setPos(cursor);
					cursor.move(effX > cursor.getX() ? EnumFacing.EAST : EnumFacing.WEST);
					
					if (spaces++ % 16 == 0) {
						// Set a new beacon every once in a while
						this.addBeacon(cursor.toImmutable());
					}
				}
				
				// Then walk to Z
				while (cursor.getZ() != pos.getZ()) {
					clearBlock(cursor, false, last);
					last.setPos(cursor);
					cursor.move(pos.getZ() > cursor.getZ() ? EnumFacing.SOUTH : EnumFacing.NORTH);
					
					if (spaces++ % 16 == 0) {
						// Set a new beacon every once in a while
						this.addBeacon(cursor.toImmutable());
					}
				}
				
				// Clear last (or first) block, if there is one
				if (outside) {
					clearBlock(cursor, false, last);
				}
			}
			
			// Then queue up the actual mining task
			makeMineTask(pos, cursor, new LogisticsTaskMineBlock[]{(LogisticsTaskMineBlock) taskMap.get(cursor)});
			
			// And mark that we're going there in the first place
			oreLocations.add(pos.toImmutable());
			this.dirty();
		}
		
		/**
		 * Scan a layer and queue up any tasks to grab ores.
		 * If nothing worth mining is found, returns false. Otherwise, returns true.
		 * @param y
		 * @return
		 */
		private boolean scanLayer(int level) {
			boolean[] result = new boolean[]{false};
			int[] counter = new int[]{0};
			boolean[] earlyOut = new boolean[]{false};
			
			final long startTime = System.currentTimeMillis();
			forEachOnLayer(level, (pos) -> {
				// If we're resuming, skip to the relevant block
				if (scanProgress > counter[0]++) {
					return true;
				}
				
				// Check run time every so often and stop if we've run a while
				if (counter[0] % 256 == 0) {
					final long now = System.currentTimeMillis();
					if (now - startTime > 10) {
						earlyOut[0] = true;
						return false;
					}
				}
				
				// Stop after we have a bunch of tasks queued up
				if (taskMap.size() > 100) {
					earlyOut[0] = true;
					return false;
				}
				
				// Is this something we should mine?
				if (IsOre(worldObj, pos)) {
					mineTo(pos);
					result[0] = true;
				}
				
				return true;
			});
			
			if (earlyOut[0]) {
				result[0] = true; // Not finished with this layer
				this.scanProgress = counter[0];
			} else {
				this.scanProgress = 0;
			}
			
			return result[0];
		}
		
		private boolean makePlatform(int level, boolean upper) {
			// 17x17 ring with a doorway to the north (or south if !whole) that leads in
			
			final int bounds = STAIRCASE_RADIUS + PLATFORM_WIDTH;
			MutableBlockPos cursor = new MutableBlockPos();
			//MutableBlockPos last = null;
			boolean clear = true;
			final int y = getYFromLevel(level);
			
			for (int i = -bounds; i <= bounds; i++)
			for (int j = -bounds; j <= bounds; j++) {
				// east or west ( i == +-bounds)? we fill in all j for these i
				// north or south only clear top and bottom
				if ((i >= -bounds && i <= -bounds + PLATFORM_WIDTH)
					|| (i >= bounds - PLATFORM_WIDTH && i <= bounds)
					|| (j >= -bounds && j <= -bounds + PLATFORM_WIDTH)
					|| (j >= bounds - PLATFORM_WIDTH && j <= bounds)) {
					// Note: we burrow centered on the actual TE but then shift our scan and
					// mine area to chunk boundaries. So these x and z are not shifted.
					final int x = pos.getX() + i;
					final int z = pos.getZ() + j;
					
//					if (last == null) {
//						last = new MutableBlockPos(x, y, z);
//					} else {
//						last.setPos(cursor);
//					}
					cursor.setPos(x, y, z);
					if (clearBlock(cursor, false, null)) {
						clear = false;
					}
				} 
			}
			
			return !clear;
		}
		
		/**
		 * Scans and queues up task for the next stairway segment, starting at the level provided.
		 * The first block dug is actually on level+1.
		 * This should not be called if we can't get all the way to the next platform level.
		 * @param level
		 * @return
		 */
		private boolean makeStaircaseSegment(int level, boolean upper) {
			MutableBlockPos cursor = new MutableBlockPos();
			MutableBlockPos last = new MutableBlockPos();
			boolean clear = true;
			
			// 5x5 spiral staircase 'starting' to the north and descending 16 blocks in
			// one spiral.
			final int perimeter = MAJOR_LEVEL_DIFF;
			// we always go one-half of a side to start
			final int edgeLength = perimeter / 4;
			final int startOffset = edgeLength / 2;
			
			final int startIdx = (upper ? 0 : MAJOR_LEVEL_DIFF / 2);
			final int endIdx = (upper ? MAJOR_LEVEL_DIFF / 2 : MAJOR_LEVEL_DIFF);
			cursor.setPos(pos.getX(), getYFromLevel(level), pos.getZ() + (upper ? -startOffset : startOffset));
			for (int i = startIdx; i < endIdx; i++) {
				EnumFacing side = i < startOffset ? EnumFacing.NORTH
						: (i < startOffset + edgeLength ? EnumFacing.EAST 
						: (i < startOffset + edgeLength + edgeLength ? EnumFacing.SOUTH
						: (i < startOffset + edgeLength + edgeLength + edgeLength ? EnumFacing.WEST
						: EnumFacing.NORTH)));
				
				last.setPos(cursor);
				switch (side) {
				case NORTH:
					cursor.move(EnumFacing.EAST);
					break;
				case EAST:
					cursor.move(EnumFacing.SOUTH);
					break;
				case SOUTH:
					cursor.move(EnumFacing.WEST);
					break;
				case WEST:
				case UP:
				case DOWN:
				default:
					cursor.move(EnumFacing.NORTH);
					break;
				}
				cursor.move(EnumFacing.DOWN);
				
				if (clearBlock(cursor, true, last)) {
					clear = false;
				}
			}
			
			// Finally, the door and beacon
			this.addBeacon(cursor.toImmutable());
			last.setPos(cursor);
			cursor.move(upper ? EnumFacing.SOUTH : EnumFacing.NORTH);
			if (clearBlock(cursor, false, last)) {
				clear = false;
			}
			
			last.setPos(cursor);
			cursor.move(upper ? EnumFacing.SOUTH : EnumFacing.NORTH);
			if (clearBlock(cursor, false, last)) {
				clear = false;
			}
			
			// Make special first-time door if needed?
			// Make this should make a top-level platform instead?
			// TODO
			
			return !clear;
		}
		
		/**
		 * Check and queue up tasks to dig the mine.
		 * If the mine is already dug and looks good, returns false.
		 * @return
		 */
		private boolean makeMine(int platformToMake, boolean repair) {
			// mine holes start to the north.
			// mine is a 5x5 spiral staircase all the way down, with platforms every (MAJOR_LEVEL_DIFF / 2) blocks.
			// note: this is a place where MAJOR_LEVEL_DIFF isn't dynamic.  It needs to be 16 to fit the 5x5.
			
			// Each platform is a 9x9 ring at the same y level connected to the north part of the spiral with a 1x3x1 doorway.
			
			// Each spot should be a 1x3x1 hole. Below it should be a solid block.
			
			// Repairs only take care of the staircase and the doorway.
			final int deepest = this.getLowestLevel();
			if (deepest == 0) {
				return false;
			}
			
			boolean dug = false;
			int level = platformToLevel(platformToMake);
			if (level < deepest) {
				boolean upper = (level % MAJOR_LEVEL_DIFF == 0);
				if (makeStaircaseSegment(level, upper)) {
					dug = true;
				}
				if (!repair) {
					level += MAJOR_LEVEL_DIFF / 2;
					if (makePlatform(level, upper)) {
						dug = true;
					}
				}
			}
			
			return dug;
		}
		
		/**
		 * Looks at platforms up to the provided one and queues up any repair tasks
		 * that are needed. Pesky gravel!
		 * @param deepestPlatform
		 * @return
		 */
		private boolean repairMine(int deepestPlatform) {
			boolean dug = false;
			for (int platform = 0; platform <= deepestPlatform; platform++) {
				dug = makeMine(platform, true) || dug;
			}
			return dug;
		}
		
		/**
		 * Get the lowest level the mine will go. Note this is level, not Y value.
		 * A return of 0 indicates that the mine cannot actually be built, as unbreakables
		 * are getting in the way.
		 * @return
		 */
		public int getLowestLevel() {
			// TODO this should also look in 5x5 (or 9x9 or 13x13?) block down to see if unmineables get in the way?
			
			if (lowestLevel == -1) {
				int y = this.pos.getY();
				lowestLevel = 0;
				while (y > MAJOR_LEVEL_DIFF) {
					y -= MAJOR_LEVEL_DIFF;
					// Check this layer for unbreakables/bedrock
					if (forEachOnLayer(y, (pos) -> {
						IBlockState state = worldObj.getBlockState(pos);
						return state.getBlockHardness(worldObj, pos) >= 0;
					})) {
						lowestLevel += MAJOR_LEVEL_DIFF;
					} else {
						break;
					}
				}
			}
			
			return lowestLevel;
		}
		
		public int getLowestReachableY() {
			int lowestLevel = getLowestLevel();
			if (lowestLevel != 0) {
				return Math.max(0, getYFromLevel(lowestLevel) - WORKER_REACH);
			} else {
				return pos.getY();
			}
		}
		
		private void repairScan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			if (getLowestLevel() == 0) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			if (nextPlatform > 0) {
				repairMine(nextPlatform - 1);
			}
			
			// Also repair any mine shafts we have going (helps with gravel and changing terrain)
			for (BlockPos pos : this.oreLocations.toArray(new BlockPos[oreLocations.size()])) {
				mineTo(pos);
			}
			final long end = System.currentTimeMillis();
			if (end - startTime >= 5) {
				System.out.println("Took " + (end - startTime) + "ms to scan for repairs!");
			}
		}
		
		private void scan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			if (scanLevel > getLowestLevel() || getLowestLevel() == 0) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			boolean didWork = false;
			while (didWork == false) {
				// If scan level is behind platform, scan. Otherwise, make a platform.
				// Keep doing both if they run and find no work to do. Could limit this here. Once per minute seems long tho.
				if (nextPlatform == 0 || scanLevel > platformToLevel(nextPlatform)) {
					// Done scanning for this level.
					// However, we wait to dig the next layer until there aren't very many outstanding tasks.
					// This prevents the mine scanning from running away from the progress of the miners.
					if (this.taskMap.size() > 10) {
						break; // break the whole loop
					}
					
					didWork = makeMine(nextPlatform, false);
					nextPlatform++;
				}
				
				if (!didWork) {
					// If this is the first layer after a platform is made, wait for the platform to finish
					if (scanLevel == platformToLevel(nextPlatform - 1) + 1) {
						if (this.taskMap.size() > 0) {
							break;
						}
					}
					
					didWork = scanLayer(scanLevel);
					if (this.scanProgress == 0) { // if no 'pickup' location was set
						scanLevel++;
					}
				}
			}
			
			final long end = System.currentTimeMillis();
			if (end - startTime >= 15) {
				System.out.println("Took " + (end - startTime) + "ms to scan for ores!");
			}
		}
		
		private void cleanTasks() {
			// Clean out any finished tasks
			List<BlockPos> keys = Lists.newArrayList(taskMap.keySet());
			for (BlockPos pos : keys) {
				ILogisticsTask task = taskMap.get(pos);
				if (task != null && task.isComplete()) {
					removeTask(pos);
					oreLocations.remove(pos);
					repairLocations.remove(pos);
					this.dirty();
				}
			}
		}
		
		@Override
		public void update() {
			if (this.worldObj.isRemote) {
				return;
			}
			
			if (this.getNetwork() == null) {
				return;
			}
			
			if (this.tickCount == 0) {
				for (BlockPos pos : this.beacons) {
					this.getNetwork().addBeacon(worldObj, pos);
				}
			}
			
			if (this.tickCount % 5 == 0) {
				cleanTasks();
			}
			if (this.tickCount % (20) == 0) {
				repairScan();
			}
			int period = (scanLevel >= getLowestLevel()) ? 180
					: (scanProgress > 0 && taskMap.size() < 100 ? 5 : 60);
			if (this.tickCount % (20 * period) == 0) {
				scan();
			}
			this.tickCount++;
		}
		
		@SubscribeEvent
		public void onBreak(BlockEvent.BreakEvent e) {
			if (e.isCanceled()) {
				return;
			}
			
			if (e.getWorld() == this.worldObj) {
				removeTask(e.getPos());
			}
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			if (!worldIn.isRemote) {
				MinecraftForge.EVENT_BUS.register(this);
			}
			
			if (this.networkComponent != null && !worldIn.isRemote && materialRequester == null) {
				refreshRequester();
			}
		}
		
		@Override
		public void setPos(BlockPos posIn) {
			super.setPos(posIn);
			
			chunkXOffset = -((posIn.getX() - radius) & 0xF); // lowest 16 values
			chunkZOffset = -((posIn.getZ() - radius) & 0xF);
		}
		
		public static final String NBT_ORES = "ores";
		public static final String NBT_REPAIRS = "repairs";
		public static final String NBT_BEACONS = "beacons";
		public static final String NBT_PLATFORMS = "platforms";
		public static final String NBT_TORCHES = "torches";
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			// We STORE the UUID of our network... but only so we can communicate it to the client.
			// We hook things back up on the server when we load by position.
			nbt = super.writeToNBT(nbt);
			
			NBTTagList list = new NBTTagList();
			for (BlockPos pos : oreLocations) {
				list.appendTag(new NBTTagLong(pos.toLong()));
			}
			nbt.setTag(NBT_ORES, list);
			
			list = new NBTTagList();
			for (BlockPos pos : repairLocations) {
				list.appendTag(new NBTTagLong(pos.toLong()));
			}
			nbt.setTag(NBT_REPAIRS, list);
			
			list = new NBTTagList();
			for (BlockPos pos : beacons) {
				list.appendTag(new NBTTagLong(pos.toLong()));
			}
			nbt.setTag(NBT_BEACONS, list);
			
			if (this.buildingMaterial != null) {
				nbt.setTag(NBT_PLATFORMS, this.buildingMaterial.serializeNBT());
			}
			if (this.torches != null) {
				nbt.setTag(NBT_TORCHES, this.torches.serializeNBT());
			}
			
			list = new NBTTagList();
			for (BlockPos pos : taskMap.keySet()) {
				list.appendTag(new NBTTagLong(pos.toLong()));
			}
			nbt.setTag("paths", list);
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			this.oreLocations.clear();
			this.repairLocations.clear();
			if (worldObj != null && worldObj.isRemote) {
				NBTTagList list = nbt.getTagList(NBT_ORES, NBT.TAG_LONG);
				for (int i = 0; i < list.tagCount(); i++) {
					BlockPos pos = BlockPos.fromLong( ((NBTTagLong) list.get(i)).getLong());
					oreLocations.add(pos);
				}
				list = nbt.getTagList(NBT_REPAIRS, NBT.TAG_LONG);
				for (int i = 0; i < list.tagCount(); i++) {
					BlockPos pos = BlockPos.fromLong( ((NBTTagLong) list.get(i)).getLong());
					repairLocations.add(pos);
				}
				this.taskMap.clear();
				list = nbt.getTagList("paths", NBT.TAG_LONG);
				for (int i = 0; i < list.tagCount(); i++) {
					BlockPos pos = BlockPos.fromLong( ((NBTTagLong) list.get(i)).getLong());
					taskMap.put(pos, null);
				}
			} else {
				NBTTagList list = nbt.getTagList(NBT_BEACONS, NBT.TAG_LONG);
				for (int i = 0; i < list.tagCount(); i++) {
					BlockPos pos = BlockPos.fromLong( ((NBTTagLong) list.get(i)).getLong());
					beacons.add(pos);
				}
			}
			
			this.buildingMaterial = null;
			if (nbt.hasKey(NBT_PLATFORMS)) {
				this.buildingMaterial = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag(NBT_PLATFORMS));
			}
			this.torches = null;
			if (nbt.hasKey(NBT_TORCHES)) {
				this.torches = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag(NBT_TORCHES));
			}
			
			if (this.worldObj != null && this.worldObj.isRemote) {
				StaticTESRRenderer.instance.update(worldObj, pos, this);
			}
		}

		@Override
		public void onTaskDrop(ILogisticsTask task, IFeyWorker worker) {
			;
		}

		@Override
		public void onTaskAccept(ILogisticsTask task, IFeyWorker worker) {
			;
		}

		@Override
		public void onTaskComplete(ILogisticsTask task, IFeyWorker worker) {
			// If this was a material request, decrement our outstanding count
			if (task instanceof LogisticsTaskPlaceBlock) {
				this.platformRequests--;
			}
		}
		
		public @Nullable ItemStack getMaterials() {
			return this.buildingMaterial;
		}
		
		public boolean isMaterials(@Nullable ItemStack stack) {
			return stack == null || stack.getItem() instanceof ItemBlock;
		}
		
		public void setMaterials(@Nullable ItemStack stack) {
			if (isMaterials(stack)) {
				this.buildingMaterial = stack;
				this.dirty();
			}
		}
		
		public @Nullable ItemStack getTorches() {
			return this.torches;
		}
		
		public boolean isTorches(@Nullable ItemStack stack) {
			return stack == null
				|| (torches.getItem() instanceof ItemBlock && ((ItemBlock) torches.getItem()).getBlock() instanceof BlockTorch);
		}
		
		public void setTorches(@Nullable ItemStack torches) {
			if (isTorches(torches)) {
				this.torches = torches;
				this.dirty();
			}
		}
		
		private void dirty() {
			worldObj.markBlockRangeForRenderUpdate(pos, pos);
			worldObj.notifyBlockUpdate(pos, this.worldObj.getBlockState(pos), this.worldObj.getBlockState(pos), 3);
			worldObj.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
			this.markDirty();
		}
		
		/**
		 * Check whether the provided task will be reachable after all other tasks the provided worker has for this
		 * mine have been completed
		 * @param task
		 * @param worker
		 * @return
		 */
		public boolean taskAccessibleWithTasks(LogisticsTaskMineBlock task, IFeyWorker worker) {
			// Will look at blocks next to this block and see if they have tasks, and if the task is
			// taken by this worker. Could care if it's taken by ANY worker, but that'd mean all workers
			// would be down in the mines waiting, and probably stomp eachother? I might need to experiment...
			BlockPos pos = task.getTargetBlock();
			if (!this.taskMap.containsKey(pos)) {
				return false;
			}
			
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return false;
			}
			
			// We do NOT look to see if there's air here. That would falsely say we could get to any
			// mine task that happens to have air next to it. We need to check pathing for those things.
			// Instead, we're trying to find blocks that are about to have the block next to them mined.
			// The other tasks had a pathing check, so we should be able to get to this one.
			/*
			if (worldObj.isAirBlock(pos.north())) {
				pos = pos.north();
			} else if (worldObj.isAirBlock(pos.south())) {
				pos = pos.south();
			} else if (worldObj.isAirBlock(pos.east())) {
				pos = pos.east();
			} else if (worldObj.isAirBlock(pos.west())) {
				pos = pos.west();
			} else if (worldObj.isAirBlock(pos.up())) {
				pos = pos.up();
			} else {
				pos = pos.down();
			}
			
			if (worldObj.isAirBlock(pos)) {
				// Already air?
				return true;
			}
			*/
			
			for (BlockPos other : new BlockPos[] {pos.up(), pos.north(), pos.south(), pos.east(), pos.west(), pos.down()}) {
				ILogisticsTask otherTask = taskMap.get(other);
				if (otherTask != null && network.getTaskRegistry().getCurrentWorker(otherTask) == worker) {
					return true;
				}
			}
			
			return false;
		}
		
		private static final ItemStack SIGN_ICON = new ItemStack(Items.IRON_PICKAXE);

		@Override
		public ItemStack getSignIcon(IFeySign sign) {
			return SIGN_ICON;
		}
		
		@Override
		public EnumFacing getSignFacing(IFeySign sign) {
			IBlockState state = worldObj.getBlockState(pos);
			return state.getValue(FACING);
		}
		
		@Override
		public void invalidate() {
			super.invalidate();
			if (worldObj != null && worldObj.isRemote) {
				StaticTESRRenderer.instance.update(worldObj, pos, null);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class MiningBlockRenderer extends FeySignRenderer<MiningBlockTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(MiningBlockTileEntity.class,
					new MiningBlockRenderer());
			FeySignRenderer.init(MiningBlockTileEntity.class, new MiningBlockRenderer());
		}
		
		protected void renderCube(BlockPos origin, BlockPos target, float red, float green, float blue, float alpha) {
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			GlStateManager.pushMatrix();
			GlStateManager.translate(target.getX() - origin.getX(), target.getY() - origin.getY(), target.getZ() - origin.getZ());
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			
			buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(0, 0, 1).color(red, green, blue, alpha).endVertex();
			buffer.pos(1, 0, 1).color(red, green, blue, alpha).endVertex();
			buffer.pos(1, 0, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
			
			tessellator.draw();
			
			buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			
			buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(1, 1, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(1, 1, 1).color(red, green, blue, alpha).endVertex();
			buffer.pos(0, 1, 1).color(red, green, blue, alpha).endVertex();
			buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
			
			tessellator.draw();
			
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			
			buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
				buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(0, 0, 1).color(red, green, blue, alpha).endVertex();
				buffer.pos(0, 1, 1).color(red, green, blue, alpha).endVertex();
			buffer.pos(1, 0, 1).color(red, green, blue, alpha).endVertex();
				buffer.pos(1, 1, 1).color(red, green, blue, alpha).endVertex();
			buffer.pos(1, 0, 0).color(red, green, blue, alpha).endVertex();
				buffer.pos(1, 1, 0).color(red, green, blue, alpha).endVertex();
			
			tessellator.draw();
			GlStateManager.popMatrix();
		}
		
		@Override
		public void renderTileEntityAt(MiningBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
			super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
			
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.thePlayer;
			
			// TODO make a capability and see if they can see logistics stuff / its turned on
			if (player != null && player.isSpectator() || player.isCreative()) { // REPLACE ME
				LogisticsNetwork network = te.getNetwork();
				if (network != null) {
					
					BlockPos origin = te.getPos();
					
					GlStateManager.pushMatrix();
					GlStateManager.translate(x, y, z);
					
					GlStateManager.glLineWidth(3f);
					GlStateManager.disableLighting();
					GlStateManager.enableTexture2D();
					GlStateManager.disableTexture2D();
					GlStateManager.enableAlpha();
					GlStateManager.enableBlend();
					GlStateManager.disableAlpha();
					GlStateManager.disableBlend();
					GlStateManager.disableDepth();
					
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
					
					for (BlockPos target : te.oreLocations) {
						renderCube(origin, target, 1, 0, 0, 1);
					}
					
					for (BlockPos target : te.repairLocations) {
						renderCube(origin, target, 0, 1, 0, 1);
					}
					
//					red = 0f;
//					blue = 0f;
//					green = 1f;
//					alpha = .7f;
//					
//					for (BlockPos target : te.taskMap.keySet()) {
//						
//						GlStateManager.pushMatrix();
//						GlStateManager.translate(target.getX() - origin.getX(), target.getY() - origin.getY(), target.getZ() - origin.getZ());
//						buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
//						
//						buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
//						buffer.pos(0, 0, 1).color(red, green, blue, alpha).endVertex();
//						buffer.pos(1, 0, 1).color(red, green, blue, alpha).endVertex();
//						buffer.pos(1, 0, 0).color(red, green, blue, alpha).endVertex();
//						buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
//						
//						tessellator.draw();
//						
//						buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
//						
//						buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
//						buffer.pos(1, 1, 0).color(red, green, blue, alpha).endVertex();
//						buffer.pos(1, 1, 1).color(red, green, blue, alpha).endVertex();
//						buffer.pos(0, 1, 1).color(red, green, blue, alpha).endVertex();
//						buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
//						
//						tessellator.draw();
//						
//						buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//						
//						buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
//							buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
//						buffer.pos(0, 0, 1).color(red, green, blue, alpha).endVertex();
//							buffer.pos(0, 1, 1).color(red, green, blue, alpha).endVertex();
//						buffer.pos(1, 0, 1).color(red, green, blue, alpha).endVertex();
//							buffer.pos(1, 1, 1).color(red, green, blue, alpha).endVertex();
//						buffer.pos(1, 0, 0).color(red, green, blue, alpha).endVertex();
//							buffer.pos(1, 1, 0).color(red, green, blue, alpha).endVertex();
//						
//						tessellator.draw();
//						GlStateManager.popMatrix();
//					}
					
					GlStateManager.enableDepth();
					GlStateManager.enableTexture2D();
					
//					GlStateManager.disableLighting();
//					GlStateManager.disableTexture2D();
//					GlStateManager.disableAlpha();
//					GlStateManager.disableBlend();
//					GlStateManager.disableDepth();
					
					GlStateManager.popMatrix();
					
//					for (ILogisticsComponent component : neighbors) {
//						BlockPos pos = component.getPosition();
//						GlStateManager.glLineWidth(2f);
//						GlStateManager.disableLighting();
//						GlStateManager.disableAlpha();
//						GlStateManager.disableBlend();
//						buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//						
//						buffer.pos(.5, 1.25, .5).color(1f, .2f, .4f, .8f).endVertex();
//						buffer.pos((pos.getX() - origin.getX()) + .5,
//								(pos.getY() - origin.getY()) + 1.25,
//								(pos.getZ() - origin.getZ()) + .5).color(1f, .2f, .4f, .8f).endVertex();
//						
//						tessellator.draw();
//					}
				}
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntity ent = new MiningBlockTileEntity();
		return ent;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof MiningBlockTileEntity))
			return;
		
		MiningBlockTileEntity block = (MiningBlockTileEntity) ent;
		block.unlinkFromNetwork();
		MinecraftForge.EVENT_BUS.unregister(block);
	}
	
	public static boolean IsOre(World world, BlockPos pos) {
		InitExtras();
		
		if (world.isAirBlock(pos)) {
			return false;
		}
		
		IBlockState state = world.getBlockState(pos);
		if (state.getBlockHardness(world, pos) < 0) {
			// unbreakable
			return false;
		}
		
		// See if it's one of the known ores
		Block block = state.getBlock();
		if (block == Blocks.COAL_ORE
			|| block == Blocks.DIAMOND_ORE
			|| block == Blocks.EMERALD_ORE
			|| block == Blocks.GOLD_ORE
			|| block == Blocks.IRON_ORE
			|| block == Blocks.LAPIS_ORE
			|| block == Blocks.LIT_REDSTONE_ORE
			|| block == Blocks.QUARTZ_ORE
			|| block == Blocks.REDSTONE_ORE
			|| block == EssenceOre.instance()
			|| block == ManiOre.instance()
				) {
			return true;
		}
		
		// Check if it's in our extra list
		// First, try ore dictionary
		if (OreDict.blockMatchesAny(state, ExtraCachedArray, false)) {
			return true;
		}
		
		// Then try to see if any are registry names
		String registryName = block.getRegistryName().toString();
		for (String extra : ExtraOres) {
			if (extra.compareToIgnoreCase(registryName) == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	private static Set<String> ExtraOres = null;
	private static String[] ExtraCachedArray = null;
	
	private static void InitExtras() {
		ExtraOres = new HashSet<>();
		
		// Vanilla entries
		ExtraOres.add("oreGold");
		ExtraOres.add("oreIron");
		ExtraOres.add("oreLapis");
		ExtraOres.add("oreDiamond");
		ExtraOres.add("oreRedstone");
		ExtraOres.add("oreEmerald");
		ExtraOres.add("oreQuartz");
		ExtraOres.add("oreCoal");
		
		// Popular mod entries
		ExtraOres.add("oreCopper");
		ExtraOres.add("oreAluminum");
		ExtraOres.add("oreLead");
		ExtraOres.add("oreSteel");
		ExtraOres.add("oreTin");
		ExtraOres.add("oreBronze");
		
		// And, in a ditch effort, look through ore dictionary
		for (String entry : OreDictionary.getOreNames()) {
			if (entry.startsWith("ore")) {
				ExtraOres.add(entry);
			}
		}
		
		ExtraCachedArray = ExtraOres.toArray(new String[ExtraOres.size()]);
	}
	
	public static void AddOreName(String registryOrDictionary) {
		InitExtras();
		ExtraOres.add(registryOrDictionary);
		ExtraCachedArray = ExtraOres.toArray(new String[ExtraOres.size()]);
	}
}
