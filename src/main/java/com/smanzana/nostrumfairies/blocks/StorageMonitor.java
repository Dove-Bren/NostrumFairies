package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.StorageMonitorScreen;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class StorageMonitor extends FeyContainerBlock {
	
	// TODO what about viewing tasks? Condensed tasks that is.
	private static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final double BB_MINOR = 2.0;
	private static final VoxelShape AABB_N = Block.makeCuboidShape(0, 0, 16 - BB_MINOR, 16, 16, 16);
	private static final VoxelShape AABB_E = Block.makeCuboidShape(0, 0, 0, BB_MINOR, 16, 16);
	private static final VoxelShape AABB_S = Block.makeCuboidShape(0, 0, 0, 16, 16, BB_MINOR);
	private static final VoxelShape AABB_W = Block.makeCuboidShape(16 - BB_MINOR, 0, 0, 16, 16, 16);
	public static final String ID = "logistics_storage_monitor";
	
	public StorageMonitor() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(2.0f, 1.0f)
				.sound(SoundType.WOOD)
				.lightValue(4)
				);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	public Direction getFacing(BlockState state) {
		return state.get(FACING);
	}
	
	protected boolean canPlaceAt(IWorldReader worldIn, BlockPos pos, Direction side) {
		if (!Block.hasSolidSide(worldIn.getBlockState(pos.offset(side.getOpposite())), worldIn, pos.offset(side.getOpposite()), side)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction side = context.getPlacementHorizontalFacing().getOpposite();
		final World world = context.getWorld();
		final BlockPos pos = context.getPos();
		
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
				.with(FACING, side);
	}
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(FACING)) {
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
	public boolean isValidPosition(BlockState stateIn, IWorldReader worldIn, BlockPos pos) {
		return this.canPlaceAt(worldIn, pos, getFacing(stateIn));
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.DOWN && !isValidPosition(stateIn, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return stateIn;
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		
		// Kick off a request to refresh info.
		if (worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof StorageMonitorTileEntity) {
				StorageMonitorTileEntity storage = (StorageMonitorTileEntity) te;
				LogisticsNetwork network = storage.getNetwork();
				if (network != null) {
					NetworkHandler.sendToServer(new LogisticsUpdateRequest(network.getUUID()));
				} else {
					NetworkHandler.sendToServer(new LogisticsUpdateRequest(null));
				}
			}
			
			// Don't wait, though, and show the UI
			DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> {
				StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) worldIn.getTileEntity(pos);
				Minecraft.getInstance().displayGuiScreen(new StorageMonitorScreen(monitor));
				return 0;
			});
		}
		
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new StorageMonitorTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof StorageMonitorTileEntity))
			return;
		
		StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) ent;
		monitor.unlinkFromNetwork();
	}
}
