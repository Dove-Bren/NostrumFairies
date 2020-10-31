package com.smanzana.nostrumfairies.client.render;

import com.smanzana.nostrumfairies.blocks.LogisticsSensorBlock.LogisticsSensorTileEntity;

import net.minecraftforge.fml.client.registry.ClientRegistry;

public class LogisticsSensorRenderer extends TileEntityLogisticsRenderer<LogisticsSensorTileEntity> {
	
	public static void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(LogisticsSensorTileEntity.class,
				new LogisticsSensorRenderer());
	}
}
