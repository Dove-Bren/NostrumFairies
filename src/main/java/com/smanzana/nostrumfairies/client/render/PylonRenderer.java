package com.smanzana.nostrumfairies.client.render;

import com.smanzana.nostrumfairies.blocks.tiles.PylonTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class PylonRenderer extends TileEntityLogisticsRenderer<PylonTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(PylonTileEntity.class,
				new PylonRenderer());
	}
}
