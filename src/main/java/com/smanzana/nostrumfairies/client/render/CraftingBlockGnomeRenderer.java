package com.smanzana.nostrumfairies.client.render;

import com.smanzana.nostrumfairies.blocks.tiles.CraftingBlockGnomeTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class CraftingBlockGnomeRenderer extends TileEntityLogisticsRenderer<CraftingBlockGnomeTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockGnomeTileEntity.class,
				new CraftingBlockGnomeRenderer());
	}
}
