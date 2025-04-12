package com.smanzana.nostrumfairies.blocks;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FeyContainerBlock extends FeyBlockBase implements EntityBlock {

	public FeyContainerBlock(Block.Properties builder) {
		super(builder);
	}
	
	@Override
	public abstract @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state);
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int id, int param) {
		super.triggerEvent(state, worldIn, pos, id, param);
		BlockEntity tileentity = worldIn.getBlockEntity(pos);
		return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}
}
