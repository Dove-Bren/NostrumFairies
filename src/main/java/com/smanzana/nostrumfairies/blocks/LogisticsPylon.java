package com.smanzana.nostrumfairies.blocks;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.tiles.PylonTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class LogisticsPylon extends FeyContainerBlock {
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Lists.newArrayList(Direction.UP, Direction.DOWN));
	public static final String ID = "logistics_pylon";
	
	public LogisticsPylon() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(2.0f, 1.0f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	public Direction getFacing(BlockState state) {
		return state.get(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		if (!Direction.Axis.Y.test(context.getFace())) {
			return null;
		}
		
		BlockPos attachedPos = context.getPos().offset(context.getFace().getOpposite());
		if (!Block.hasSolidSide(context.getWorld().getBlockState(attachedPos), context.getWorld(), attachedPos, context.getFace())) {
			return null;
		}
		
		return this.getDefaultState()
				.with(FACING, context.getFace() == Direction.DOWN ? context.getFace() : Direction.UP);
	}
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
		if (facing != getFacing(stateIn).getOpposite()) {
			return stateIn; // Not what we're attached to
		}
		
		BlockPos attachedPos = pos.offset(facing.getOpposite());
		if (!Block.hasSolidSide(world.getBlockState(attachedPos), world, attachedPos, facing.getOpposite())) {
			return Blocks.AIR.getDefaultState();
		}
		
		return stateIn;
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		return false; // could do a cool ping animation or something
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new PylonTileEntity();
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
		if (ent == null || !(ent instanceof PylonTileEntity))
			return;
		
		PylonTileEntity monitor = (PylonTileEntity) ent;
		monitor.unlinkFromNetwork();
	}
}
