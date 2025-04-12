package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.StorageChestTileEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class StorageChestRenderer extends TileEntityLogisticsRenderer<StorageChestTileEntity> {

	public StorageChestRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
}
