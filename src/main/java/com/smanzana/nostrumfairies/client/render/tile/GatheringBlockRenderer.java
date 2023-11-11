package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.GatheringBlockTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class GatheringBlockRenderer extends FeySignRenderer<GatheringBlockTileEntity> {
	
	public static void init() {
		FeySignRenderer.init(GatheringBlockTileEntity.class, new GatheringBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(GatheringBlockTileEntity.class,
				new GatheringBlockRenderer());
	}
}