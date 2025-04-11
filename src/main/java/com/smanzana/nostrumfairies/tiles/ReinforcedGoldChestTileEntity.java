package com.smanzana.nostrumfairies.tiles;

public class ReinforcedGoldChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 162;

	public ReinforcedGoldChestTileEntity() {
		super(FairyTileEntities.ReinforcedGoldChestTileEntityType);
	}
	
	@Override
	public int getContainerSize() {
		return INV_SIZE;
	}
	
}