package com.smanzana.nostrumfairies.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedDiamondChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 324;

	public ReinforcedDiamondChestTileEntity(BlockPos pos, BlockState state) {
		super(FairyTileEntities.ReinforcedDiamondChestTileEntityType, pos, state);
	}
	
	@Override
	public int getContainerSize() {
		return INV_SIZE;
	}
	
}