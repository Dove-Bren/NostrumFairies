package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.OutputChestTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class OutputChestRenderer extends TileEntityLogisticsRenderer<OutputChestTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(OutputChestTileEntity.class,
				new OutputChestRenderer());
	}
}
