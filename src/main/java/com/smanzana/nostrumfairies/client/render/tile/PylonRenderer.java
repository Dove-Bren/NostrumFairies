package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.PylonTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class PylonRenderer extends TileEntityLogisticsRenderer<PylonTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(PylonTileEntity.class,
				new PylonRenderer());
	}
}
