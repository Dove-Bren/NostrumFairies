package com.smanzana.nostrumfairies.blocks.tiles;

public class ReinforcedIronChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 81;

	public ReinforcedIronChestTileEntity() {
		super();
	}
	
	@Override
	public int getSizeInventory() {
		return INV_SIZE;
	}
	
}