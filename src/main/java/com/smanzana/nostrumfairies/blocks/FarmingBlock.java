package com.smanzana.nostrumfairies.blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.FeySignRenderer;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskHarvest;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FarmingBlock extends BlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final String ID = "logistics_farming_block";
	
	private static FarmingBlock instance = null;
	public static FarmingBlock instance() {
		if (instance == null)
			instance = new FarmingBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(FarmingBlockTileEntity.class, "logistics_farming_block_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public FarmingBlock() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
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
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	public static class FarmingBlockTileEntity extends LogisticsTileEntity implements ITickable,  ILogisticsTaskListener, IFeySign {

		private int tickCount;
		private Map<BlockPos, ILogisticsTask> taskMap;
		private double radius;
		
		public FarmingBlockTileEntity() {
			this(7);
		}
		
		public FarmingBlockTileEntity(double blockRadius) {
			super();
			taskMap = new HashMap<>();
			this.radius = blockRadius;
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
		
		private void makeHarvestTask(BlockPos base) {
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			if (!taskMap.containsKey(base)) {
				LogisticsTaskHarvest task = new LogisticsTaskHarvest(this.getNetworkComponent(), "Farm Harvest Task", worldObj, base);
				this.taskMap.put(base, task);
				network.getTaskRegistry().register(task, this);
			}
		}
		
		private void makePlantTask(BlockPos base) {
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			if (!taskMap.containsKey(base)) {
				LogisticsTaskPlantItem task = new LogisticsTaskPlantItem(this.networkComponent, "Plant Sapling", this.getSeed(), worldObj, base);
				this.taskMap.put(base, task);
				network.getTaskRegistry().register(task, this);
			}
		}
		
		private void removeTask(BlockPos base) {
			ILogisticsTask task = taskMap.remove(base);
			if (task == null) {
				// wut
				return;
			}
			
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			network.getTaskRegistry().revoke(task);
		}
		
		private void scan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			Set<BlockPos> known = Sets.newHashSet(taskMap.keySet());
			List<BlockPos> emptySpots = new LinkedList<>();
			List<BlockPos> grownCrops = new LinkedList<>();
			
			MutableBlockPos pos = new MutableBlockPos();
			BlockPos center = this.getPos();
			final int startY = (int) Math.max(-pos.getY(), Math.floor(-radius));
			final int endY = (int) Math.min(256 - pos.getY(), Math.ceil(radius));
			for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++)
			for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++)
			for (int y = startY; y < endY; y++) {
				
				pos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
				if (!worldObj.isBlockLoaded(pos)) {
					break; // skip this whole column
				}
				
				if (isGrownCrop(worldObj, pos)) {
					grownCrops.add(pos.toImmutable());
				} else if (isPlantableSpot(worldObj, pos.down(), this.getSeed())) {
					emptySpots.add(pos.toImmutable());
				}
			}
			
			for (BlockPos base : grownCrops) {
				if (known.remove(base)) {
					; // We already knew about it, so don't create a new one
				} else {
					// Didn't know, so record!
					// Don't make task cause we filter better later
					makeHarvestTask(base);
				}
			}
			
			// Repeat for plant tasks
			for (BlockPos base : emptySpots) {
				if (known.remove(base)) {
					; // We already knew about it, so don't create a new one
				} else {
					// Didn't know, so record!
					// Don't make task cause we filter better later
					makePlantTask(base);
				}
			}
			
			// For any left in known, the crop/spot is not there anymore! Remove!
			for (BlockPos base : known) {
				removeTask(base);
			}
			
			final long end = System.currentTimeMillis();
			if (end - startTime >= 5) {
				System.out.println("Took " + (end - startTime) + "ms to scan for crops!");
			}
		}
		
		@Override
		public void update() {
			if (this.worldObj.isRemote) {
				return;
			}
			
			this.tickCount++;
			if (this.tickCount % (20 * 10) == 0) {
				scan();
			}
		}
		
		// TODO make configurable!
		public ItemStack getSeed() {
			return new ItemStack(Items.WHEAT_SEEDS);
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
			// Cleanup used to be automatically handled by the requester. However, we want to
			// queue up replanting sometimes if the tree comes down. So do that here.
			if (task instanceof LogisticsTaskHarvest) {
				LogisticsTaskHarvest harvesetTask = (LogisticsTaskHarvest) task;
				BlockPos pos = harvesetTask.getCropPos();
				taskMap.remove(pos);
			} else if (task instanceof LogisticsTaskPlantItem) {
				LogisticsTaskPlantItem plantTask = (LogisticsTaskPlantItem) task;
				BlockPos pos = plantTask.getTargetBlock();
				taskMap.remove(pos);
			}
		}
		
		private static final ItemStack SIGN_ICON = new ItemStack(Items.IRON_HOE);

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
		public void readFromNBT(NBTTagCompound compound) {
			super.readFromNBT(compound);
			
			if (this.worldObj != null && this.worldObj.isRemote) {
				StaticTESRRenderer.instance.update(worldObj, pos, this);
			}
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
	public static class FarmingBlockRenderer extends FeySignRenderer<FarmingBlockTileEntity> {
		
		public static void init() {
			FeySignRenderer.init(FarmingBlockTileEntity.class, new FarmingBlockRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntity ent = new FarmingBlockTileEntity();
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
		if (ent == null || !(ent instanceof FarmingBlockTileEntity))
			return;
		
		FarmingBlockTileEntity block = (FarmingBlockTileEntity) ent;
		block.unlinkFromNetwork();
		MinecraftForge.TERRAIN_GEN_BUS.unregister(block);
	}
	
	public static boolean isGrownCrop(World world, BlockPos base) {
		if (world == null || base == null) {
			return false;
		}
		
		IBlockState state = world.getBlockState(base);
		if (state == null) {
			return false;
		}
		
		if (!(state.getBlock() instanceof BlockCrops)) {
			return false;
		}
		
		return ((BlockCrops) state.getBlock()).isMaxAge(state);
	}
	
	public static boolean isPlantableSpot(World world, BlockPos base, ItemStack seed) {
		if (world == null || base == null || seed == null) {
			return false;
		}
		
		if (!world.isAirBlock(base.up())) {
			return false;
		}
		
		IPlantable plantable = null;
		if (seed.getItem() instanceof IPlantable) {
			plantable = (IPlantable) seed.getItem();
		} else if (seed.getItem() instanceof ItemBlock && ((ItemBlock) seed.getItem()).getBlock() instanceof IPlantable) {
			plantable = (IPlantable) ((ItemBlock) seed.getItem()).getBlock();
		}
		
		if (plantable == null) {
			return false;
		}
		
		IBlockState state = world.getBlockState(base);
		return state.getBlock().canSustainPlant(state, world, base, EnumFacing.UP, plantable);
	}
}
