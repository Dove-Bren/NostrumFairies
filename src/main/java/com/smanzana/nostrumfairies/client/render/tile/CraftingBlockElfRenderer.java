package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.CraftingBlockElfTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class CraftingBlockElfRenderer extends TileEntityLogisticsRenderer<CraftingBlockElfTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockElfTileEntity.class,
				new CraftingBlockElfRenderer());
	}
}