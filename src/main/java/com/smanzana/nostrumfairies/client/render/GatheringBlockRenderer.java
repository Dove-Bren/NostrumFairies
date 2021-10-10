package com.smanzana.nostrumfairies.client.render;

import com.smanzana.nostrumfairies.blocks.tiles.GatheringBlockTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class GatheringBlockRenderer extends FeySignRenderer<GatheringBlockTileEntity> {
	
	public static void init() {
		FeySignRenderer.init(GatheringBlockTileEntity.class, new GatheringBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(GatheringBlockTileEntity.class,
				new GatheringBlockRenderer());
	}
}
