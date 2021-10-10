package com.smanzana.nostrumfairies.client.render;

import com.smanzana.nostrumfairies.blocks.tiles.FarmingBlockTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class FarmingBlockRenderer extends FeySignRenderer<FarmingBlockTileEntity> {
	
	public static void init() {
		FeySignRenderer.init(FarmingBlockTileEntity.class, new FarmingBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(FarmingBlockTileEntity.class,
				new FarmingBlockRenderer());
	}
}
