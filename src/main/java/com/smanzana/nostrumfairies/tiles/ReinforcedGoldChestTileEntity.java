package com.smanzana.nostrumfairies.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedGoldChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 162;

	public ReinforcedGoldChestTileEntity(BlockPos pos, BlockState state) {
		super(FairyTileEntities.ReinforcedGoldChestTileEntityType, pos, state);
	}
	
	@Override
	public int getContainerSize() {
		return INV_SIZE;
	}
	
}