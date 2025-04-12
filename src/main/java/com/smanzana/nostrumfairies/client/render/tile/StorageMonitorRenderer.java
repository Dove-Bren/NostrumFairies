package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class StorageMonitorRenderer extends TileEntityLogisticsRenderer<StorageMonitorTileEntity> {

	public StorageMonitorRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
}
