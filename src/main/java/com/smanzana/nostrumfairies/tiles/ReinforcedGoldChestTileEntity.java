package com.smanzana.nostrumfairies.tiles;

public class ReinforcedGoldChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 162;

	public ReinforcedGoldChestTileEntity() {
		super();
	}
	
	@Override
	public int getSizeInventory() {
		return INV_SIZE;
	}
	
}