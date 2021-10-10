package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.tiles.StorageMonitorTileEntity;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;

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
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
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
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
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
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos posFrom) {
		EnumFacing face = state.getValue(FACING);
		if (!canPlaceAt(worldIn, pos, face)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		
		super.neighborChanged(state, worldIn, pos, blockIn, posFrom);
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		
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
