package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class StorageMonitorRenderer extends TileEntityLogisticsRenderer<StorageMonitorTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(StorageMonitorTileEntity.class,
				new StorageMonitorRenderer());
	}
}
