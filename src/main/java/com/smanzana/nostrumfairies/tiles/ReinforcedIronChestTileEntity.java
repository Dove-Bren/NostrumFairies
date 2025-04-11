package com.smanzana.nostrumfairies.tiles;

public class ReinforcedIronChestTileEntity extends ReinforcedChestTileEntity {
	
	public static final int INV_SIZE = 81;

	public ReinforcedIronChestTileEntity() {
		super(FairyTileEntities.ReinforcedIronChestTileEntityType);
	}
	
	@Override
	public int getContainerSize() {
		return INV_SIZE;
	}
	
}