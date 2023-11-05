package com.smanzana.nostrumfairies.tiles;

import java.util.List;

import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemDeepStacks;

import net.minecraft.tileentity.TileEntityType;

public abstract class ReinforcedChestTileEntity extends LogisticsChestTileEntity {

	public ReinforcedChestTileEntity(TileEntityType<? extends ReinforcedChestTileEntity> type) {
		super(type);
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