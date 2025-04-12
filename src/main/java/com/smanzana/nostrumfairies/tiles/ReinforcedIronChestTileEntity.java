package com.smanzana.nostrumfairies.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedIronChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 81;

	public ReinforcedIronChestTileEntity(BlockPos pos, BlockState state) {
		super(FairyTileEntities.ReinforcedIronChestTileEntityType, pos, state);
	}
	
	@Override
	public int getContainerSize() {
		return INV_SIZE;
	}
	
}