package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.BufferChestTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class BufferChestRenderer extends TileEntityLogisticsRenderer<BufferChestTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(BufferChestTileEntity.class,
				new BufferChestRenderer());
	}
}
