package com.smanzana.nostrumfairies.tiles;

import java.util.List;

import com.smanzana.nostrumfairies.utils.ItemDeepStack;

public class PylonTileEntity extends LogisticsTileEntity {

	public PylonTileEntity() {
		super(FairyTileEntities.PylonTileEntityType);
	}
	
	@Override
	public double getDefaultLogisticsRange() {
		return 0;
	}

	@Override
	public double getDefaultLinkRange() {
		return 20;
	}

	@Override
	public boolean canAccept(List<ItemDeepStack> stacks) {
		return false;
	}
}