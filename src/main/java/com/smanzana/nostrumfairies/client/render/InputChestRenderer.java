package com.smanzana.nostrumfairies.client.render;

import com.smanzana.nostrumfairies.blocks.tiles.InputChestTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class InputChestRenderer extends TileEntityLogisticsRenderer<InputChestTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(InputChestTileEntity.class,
				new InputChestRenderer());
	}
}
