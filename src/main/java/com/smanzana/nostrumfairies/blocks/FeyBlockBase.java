package com.smanzana.nostrumfairies.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// Just cause I'm sick of these methods lol
public abstract class FeyBlockBase extends Block {

	protected FeyBlockBase(Block.Properties builder) {
		super(builder);
	}
	
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.breakBlock(world, pos, state);
		}
	}
	
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		world.removeTileEntity(pos);
	}
}
