package com.smanzana.nostrumfairies.tiles;

public class ReinforcedDiamondChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 324;

	public ReinforcedDiamondChestTileEntity() {
		super();
	}
	
	@Override
	public int getSizeInventory() {
		return INV_SIZE;
	}
	
}