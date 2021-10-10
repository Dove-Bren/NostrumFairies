package com.smanzana.nostrumfairies.blocks.tiles;

import java.util.List;

import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemDeepStacks;

public abstract class ReinforcedChestTileEntity extends LogisticsChestTileEntity {

	private String displayName;
	
	public ReinforcedChestTileEntity() {
		super();
		displayName = "Reinforced Storage Chest";
	}
	
	@Override
	public String getName() {
		return displayName;
	}

	@Override
	public boolean hasCustomName() {
		return false;
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