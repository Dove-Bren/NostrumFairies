package com.smanzana.nostrumfairies.client.render;

import com.smanzana.nostrumfairies.blocks.tiles.CraftingBlockElfTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class CraftingBlockElfRenderer extends TileEntityLogisticsRenderer<CraftingBlockElfTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockElfTileEntity.class,
				new CraftingBlockElfRenderer());
	}
}
