package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.container.OutputPanelGui;
import com.smanzana.nostrumfairies.tiles.FairyTileEntities;
import com.smanzana.nostrumfairies.tiles.OutputPanelTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OutputLogisticsPanel extends FeyContainerBlock {
	
	private static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	private static final double BB_MINOR = 1.0;
	private static final double BB_MAJOR = 2.0;
	private static final VoxelShape AABB_N = Block.box(BB_MAJOR, BB_MAJOR, 0, 16 - BB_MAJOR, 16 - BB_MAJOR, BB_MINOR);
	private static final VoxelShape AABB_E = Block.box(16 - BB_MINOR, BB_MAJOR, BB_MAJOR, 16, 16 - BB_MAJOR, 16 - BB_MAJOR);
	private static final VoxelShape AABB_S = Block.box(BB_MAJOR, BB_MAJOR, 16 - BB_MINOR, 16 - BB_MAJOR, 16 - BB_MAJOR, 16);
	private static final VoxelShape AABB_W = Block.box(0, BB_MAJOR, BB_MAJOR, BB_MINOR, 16 - BB_MAJOR, 16 - BB_MAJOR);
	private static final VoxelShape AABB_U = Block.box(BB_MAJOR, 16 - BB_MINOR, BB_MAJOR, 16 - BB_MAJOR, 16, 16 - BB_MAJOR);
	private static final VoxelShape AABB_D = Block.box(BB_MAJOR, 0, BB_MAJOR, 16 - BB_MAJOR, BB_MINOR, 16 - BB_MAJOR);
	public static final String ID = "logistics_output_panel";
	
	public OutputLogisticsPanel() {
		super(Block.Properties.of(Material.WOOD)
				.strength(3.0f, 1.0f)
				.sound(SoundType.WOOD)
				);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	protected static int metaFromFacing(Direction facing) {
		return facing.get3DDataValue();
	}
	
	public Direction getFacing(BlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// Want to point towards the block we clicked
		final Level world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		Direction facing = context.getClickedFace().getOpposite();
		if (!this.canPlaceAt(world, pos, facing) && facing.get3DDataValue() > 1) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				facing = facing.getClockWise();
				if (this.canPlaceAt(world, pos, facing)) {
					break;
				}
			}
		}
		
		return this.defaultBlockState()
				.setValue(FACING, facing);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		switch (state.getValue(FACING)) {
		case NORTH:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		case UP:
			return AABB_U;
		case DOWN:
		default:
			return AABB_D;
		}
	}
	
	protected boolean canPlaceAt(LevelReader worldIn, BlockPos pos, Direction side) {
		BlockState state = worldIn.getBlockState(pos.relative(side));
		if (state == null || !(state.getMaterial().blocksMotion())) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canSurvive(BlockState stateIn, LevelReader worldIn, BlockPos pos) {
		for (Direction side : Direction.values()) {
			if (canPlaceAt(worldIn, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos posFrom, boolean isMoving) {
		Direction face = state.getValue(FACING);
		if (!canPlaceAt(worldIn, pos, face)) {
			worldIn.removeBlock(pos, true);
		} else {
			BlockEntity ent = worldIn.getBlockEntity(pos);
			if (ent != null && ent instanceof OutputPanelTileEntity) {
				((OutputPanelTileEntity) ent).notifyNeighborChanged();
			}
		}
		
		super.neighborChanged(state, worldIn, posFrom, blockIn, posFrom, isMoving);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		OutputPanelTileEntity panel = (OutputPanelTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, OutputPanelGui.OutputPanelContainer.Make(panel));
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OutputPanelTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, FairyTileEntities.OutputPanelTileEntityType);
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
		if (ent == null || !(ent instanceof OutputPanelTileEntity))
			return;
		
		OutputPanelTileEntity table = (OutputPanelTileEntity) ent;
		table.unlinkFromNetwork();
	}
}
