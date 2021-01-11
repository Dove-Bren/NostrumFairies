package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWithdrawItem;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class StorageMonitor extends BlockContainer {
	
	// TODO what about viewing tasks? Condensed tasks that is.
	private static final PropertyDirection FACING = BlockHorizontal.FACING;
	private static final double BB_MINOR = 2.0 / 16.0;
	private static final AxisAlignedBB AABB_N = new AxisAlignedBB(0, 0, 1 - BB_MINOR, 1, 1, 1);
	private static final AxisAlignedBB AABB_E = new AxisAlignedBB(0, 0, 0, BB_MINOR, 1, 1);
	private static final AxisAlignedBB AABB_S = new AxisAlignedBB(0, 0, 0, 1, 1, BB_MINOR);
	private static final AxisAlignedBB AABB_W = new AxisAlignedBB(1 - BB_MINOR, 0, 0, 1, 1, 1);
	public static final String ID = "logistics_storage_monitor";
	
	private static StorageMonitor instance = null;
	public static StorageMonitor instance() {
		if (instance == null)
			instance = new StorageMonitor();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(StorageMonitorTileEntity.class, "logistics_storage_monitor_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public StorageMonitor() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(2.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setLightOpacity(0);
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
		EnumFacing side = placer.getHorizontalFacing().getOpposite();
		if (!this.canPlaceAt(world, pos, side)) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				side = side.rotateY();
				if (this.canPlaceAt(world, pos, side)) {
					break;
				}
			}
		}
		
		return this.getDefaultState()
				.withProperty(FACING, side);
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
		switch (state.getValue(FACING)) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		switch (blockState.getValue(FACING)) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		
		}
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	protected boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing side) {
		IBlockState state = worldIn.getBlockState(pos.offset(side.getOpposite()));
		if (state == null || !(state.isSideSolid(worldIn, pos.offset(side.getOpposite()), side.getOpposite()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			if (canPlaceAt(worldIn, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		EnumFacing face = state.getValue(FACING);
		if (!canPlaceAt(worldIn, pos, face)) {
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
		
		// Kick off a request to refresh info.
		if (worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof StorageMonitorTileEntity) {
				StorageMonitorTileEntity storage = (StorageMonitorTileEntity) te;
				LogisticsNetwork network = storage.getNetwork();
				if (network != null) {
					NetworkHandler.getSyncChannel().sendToServer(new LogisticsUpdateRequest(network.getUUID()));
				} else {
					NetworkHandler.getSyncChannel().sendToServer(new LogisticsUpdateRequest());
				}
			}
		}
		
		
		// Don't wait, though, and show the UI
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.storageMonitorID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class StorageMonitorTileEntity extends LogisticsTileEntity implements ILogisticsTaskListener {

		private LogisticsItemWithdrawRequester requester;
		private List<ItemStack> requests;

		public StorageMonitorTileEntity() {
			super();
			requests = new ArrayList<>();
		}
		
		@Override
		public double getDefaultLogisticsRange() {
			return 0;
		}

		@Override
		public double getDefaultLinkRange() {
			return 10;
		}

		@Override
		public boolean canAccept(List<ItemDeepStack> stacks) {
			return false;
		}
		
		protected void makeRequester() {
			requester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent); // TODO make using buffer chests configurable!
			requester.addChainListener(this);
			requester.updateRequestedItems(getItemRequests());
		}
		
		@Override
		protected void setNetworkComponent(LogisticsTileEntityComponent component) {
			super.setNetworkComponent(component);
			
			if (worldObj != null && !worldObj.isRemote && requester == null) {
				makeRequester();
			}
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			
			if (this.networkComponent != null && !worldIn.isRemote && requester == null) {
				makeRequester();
			}
		}
		
		@Override
		public void onLeaveNetwork() {
			if (!worldObj.isRemote && requester != null) {
				requester.clearRequests();
				requester.setNetwork(null);
			}
			
			super.onLeaveNetwork();
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			if (!worldObj.isRemote && requester != null) {
				requester.setNetwork(network);
				requester.updateRequestedItems(getItemRequests());
			}
			
			super.onJoinNetwork(network);
		}
		
		public List<ItemStack> getItemRequests() {
			return requests;
		}

		@Override
		public void onTaskDrop(ILogisticsTask task, IFeyWorker worker) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTaskAccept(ILogisticsTask task, IFeyWorker worker) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTaskComplete(ILogisticsTask task, IFeyWorker worker) {
			// Remove item from our request list
			ItemDeepStack fetched = ((LogisticsTaskWithdrawItem) task).getAttachedItem();
			if (fetched != null) {
				Iterator<ItemStack> it = requests.iterator();
				while (fetched.getCount() > 0 && it.hasNext()) {
					ItemStack cur = it.next();
					if (cur == null) {
						continue;
					}
					
					if (fetched.canMerge(cur)) {
						if (cur.stackSize <= fetched.getCount()) {
							it.remove();
							fetched.add(-cur.stackSize);
						} else {
							cur.stackSize -= fetched.getCount();
							fetched.setCount(0);
							break;
						}
					}
				}
			}
		}
		
		public void addRequest(ItemStack stack) {
			requests.add(stack);
		}
		
		public void removeRequest(ItemStack stack) {
			Iterator<ItemStack> it = requests.iterator();
			while (it.hasNext()) {
				ItemStack cur = it.next();
				if (stack.getItem() == cur.getItem() && stack.stackSize == cur.stackSize) {
					it.remove();
					break;
				}
			}
		}
		
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new StorageMonitorTileEntity();
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
		if (ent == null || !(ent instanceof StorageMonitorTileEntity))
			return;
		
		StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) ent;
		monitor.unlinkFromNetwork();
	}
}
