package com.smanzana.nostrumfairies.logistics.task;

import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/*
 * Travel to a location and place a block there
 */
public class LogisticsTaskBuildBlock extends LogisticsTaskPlaceBlock {
	
	public LogisticsTaskBuildBlock(ILogisticsComponent owningComponent, String displayName,
			ItemStack item, BlockState state,Level world, BlockPos pos) {
		super(owningComponent, displayName, item, state, world, pos, pos);
	}
	
	public LogisticsTaskBuildBlock(ILogisticsComponent owningComponent, String displayName,
			ItemStack item, BlockState state, Level world, BlockPos pos, BlockPos placeAt) {
		super(owningComponent, null, displayName, item, state, world, pos, placeAt);
	}
	
	public LogisticsTaskBuildBlock(LivingEntity entity, String displayName,
			ItemStack item, BlockState state, Level world, BlockPos pos) {
		super(entity, displayName, item, state, world, pos, pos);
	}
	
	public LogisticsTaskBuildBlock(LivingEntity entity, String displayName,
			ItemStack item, BlockState state, Level world, BlockPos pos, BlockPos placeAt) {
		super(null, entity, displayName, item, state, world, pos, placeAt);
	}
	
	@Override
	public void markSubtaskComplete() {
		if (phase == Phase.PLACING) {
			// Add animation for building
			if (this.animCount < 0) {
				// Haven't started it
				animCount = 6;
			} else if (animCount == 0) {
				placeBlock();
				phase = Phase.DONE;
			} else {
				animCount--;
			}
		} else {
			super.markSubtaskComplete();
		}
	}
}
