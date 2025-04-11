package com.smanzana.nostrumfairies.blocks;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.tiles.PylonTileEntity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolType;

public class LogisticsPylon extends FeyContainerBlock {
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Lists.newArrayList(Direction.UP, Direction.DOWN));
	public static final String ID = "logistics_pylon";
	
	public LogisticsPylon() {
		super(Block.Properties.of(Material.STONE)
				.strength(2.0f, 1.0f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
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
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (!Direction.Axis.Y.test(context.getClickedFace())) {
			return null;
		}
		
		BlockPos attachedPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
		if (!Block.canSupportCenter(context.getLevel(), attachedPos, context.getClickedFace())) {
			return null;
		}
		
		return this.defaultBlockState()
				.setValue(FACING, context.getClickedFace() == Direction.DOWN ? context.getClickedFace() : Direction.UP);
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
		if (facing != getFacing(stateIn).getOpposite()) {
			return stateIn; // Not what we're attached to
		}
		
		BlockPos attachedPos = pos.relative(facing.getOpposite());
		if (!Block.canSupportCenter(world, attachedPos, facing.getOpposite())) {
			return Blocks.AIR.defaultBlockState();
		}
		
		return stateIn;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		return InteractionResult.PASS; // could do a cool ping animation or something
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return new PylonTileEntity();
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
		if (ent == null || !(ent instanceof PylonTileEntity))
			return;
		
		PylonTileEntity monitor = (PylonTileEntity) ent;
		monitor.unlinkFromNetwork();
	}
}
