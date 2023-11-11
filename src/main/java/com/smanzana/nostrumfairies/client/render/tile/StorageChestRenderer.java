package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.StorageChestTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class StorageChestRenderer extends TileEntityLogisticsRenderer<StorageChestTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(StorageChestTileEntity.class,
				new StorageChestRenderer());
	}
}
