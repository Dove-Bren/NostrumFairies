package com.smanzana.nostrumfairies.tiles;

public class ReinforcedIronChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 81;

	public ReinforcedIronChestTileEntity() {
		super(FairyTileEntities.ReinforcedIronChestTileEntityType);
	}
	
	@Override
	public int getSizeInventory() {
		return INV_SIZE;
	}
	
}