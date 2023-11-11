package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.InputChestTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class InputChestRenderer extends TileEntityLogisticsRenderer<InputChestTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(InputChestTileEntity.class,
				new InputChestRenderer());
	}
}