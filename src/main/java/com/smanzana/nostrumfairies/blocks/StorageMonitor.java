package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StorageMonitor extends FeyContainerBlock {
	
	// TODO what about viewing tasks? Condensed tasks that is.
	private static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final double BB_MINOR = 2.0;
	private static final VoxelShape AABB_N = Block.box(0, 0, 16 - BB_MINOR, 16, 16, 16);
	private static final VoxelShape AABB_E = Block.box(0, 0, 0, BB_MINOR, 16, 16);
	private static final VoxelShape AABB_S = Block.box(0, 0, 0, 16, 16, BB_MINOR);
	private static final VoxelShape AABB_W = Block.box(16 - BB_MINOR, 0, 0, 16, 16, 16);
	public static final String ID = "logistics_storage_monitor";
	
	public StorageMonitor() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.0f, 1.0f)
				.sound(SoundType.WOOD)
				.lightLevel((s) -> 4)
				.noOcclusion()
				);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	public Direction getFacing(BlockState state) {
		return state.getValue(FACING);
	}
	
	protected boolean canPlaceAt(LevelReader worldIn, BlockPos pos, Direction side) {
		if (!Block.canSupportCenter(worldIn, pos.relative(side.getOpposite()), side)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction side = context.getHorizontalDirection().getOpposite();
		final Level world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		
		if (!this.canPlaceAt(world, pos, side)) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				side = side.getClockWise();
				if (this.canPlaceAt(world, pos, side)) {
					break;
				}
			}
		}
		
		return this.defaultBlockState()
				.setValue(FACING, side);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
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
	public boolean canSurvive(BlockState stateIn, LevelReader worldIn, BlockPos pos) {
		return this.canPlaceAt(worldIn, pos, getFacing(stateIn));
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.DOWN && !canSurvive(stateIn, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return stateIn;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		
		// Kick off a request to refresh info.
		if (worldIn.isClientSide) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te != null && te instanceof StorageMonitorTileEntity) {
				StorageMonitorTileEntity storage = (StorageMonitorTileEntity) te;
				LogisticsNetwork network = storage.getNetwork();
				if (network != null) {
					NetworkHandler.sendToServer(new LogisticsUpdateRequest(network.getUUID()));
				} else {
					NetworkHandler.sendToServer(new LogisticsUpdateRequest(null));
				}
			}
			
			NostrumFairies.proxy.openStorageMonitor(worldIn, pos);
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new StorageMonitorTileEntity(pos, state);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof StorageMonitorTileEntity))
			return;
		
		StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) ent;
		monitor.unlinkFromNetwork();
	}
}
