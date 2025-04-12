package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.GatheringBlockTileEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class GatheringBlockRenderer extends FeySignRenderer<GatheringBlockTileEntity> {

	public GatheringBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
}
