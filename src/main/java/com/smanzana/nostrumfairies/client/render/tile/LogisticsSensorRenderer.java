package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.LogisticsSensorTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class LogisticsSensorRenderer extends TileEntityLogisticsRenderer<LogisticsSensorTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(LogisticsSensorTileEntity.class,
				new LogisticsSensorRenderer());
	}
}