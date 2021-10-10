package com.smanzana.nostrumfairies.client.render;

import com.smanzana.nostrumfairies.blocks.tiles.CraftingBlockDwarfTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class CraftingBlockDwarfRenderer extends TileEntityLogisticsRenderer<CraftingBlockDwarfTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockDwarfTileEntity.class,
				new CraftingBlockDwarfRenderer());
	}
}
