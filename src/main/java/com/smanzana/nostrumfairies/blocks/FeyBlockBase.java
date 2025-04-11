package com.smanzana.nostrumfairies.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

// Just cause I'm sick of these methods lol
public abstract class FeyBlockBase extends Block {

	protected FeyBlockBase(Block.Properties builder) {
		super(builder);
	}
	
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.breakBlock(world, pos, state);
		}
	}
	
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		world.removeBlockEntity(pos);
	}
}
