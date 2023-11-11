package com.smanzana.nostrumfairies.client.render.tile;

import com.smanzana.nostrumfairies.tiles.WoodcuttingBlockTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class WoodcuttingBlockRenderer extends FeySignRenderer<WoodcuttingBlockTileEntity> {
	
	public static void init() {
		FeySignRenderer.init(WoodcuttingBlockTileEntity.class, new WoodcuttingBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(WoodcuttingBlockTileEntity.class,
				new WoodcuttingBlockRenderer());
	}
}
