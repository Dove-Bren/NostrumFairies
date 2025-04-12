package com.smanzana.nostrumfairies.tiles;

import java.util.List;

import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemDeepStacks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class StorageChestTileEntity extends LogisticsChestTileEntity {

	private static final int SLOTS = 27;
	
	public StorageChestTileEntity(BlockPos pos, BlockState state) {
		super(FairyTileEntities.StorageChestTileEntityType, pos, state);
	}
	
	@Override
	public int getContainerSize() {
		return SLOTS;
	}
	
	@Override
	public double getDefaultLogisticsRange() {
		return 20;
	}

	@Override
	public double getDefaultLinkRange() {
		return 10;
	}
	
	@Override
	public boolean canAccept(List<ItemDeepStack> stacks) {
		return ItemDeepStacks.canFitAll(this, stacks);
	}
}