package com.smanzana.nostrumfairies.tiles;

import java.util.List;

import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PylonTileEntity extends LogisticsTileEntity {

	public PylonTileEntity(BlockPos pos, BlockState state) {
		super(FairyTileEntities.PylonTileEntityType, pos, state);
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