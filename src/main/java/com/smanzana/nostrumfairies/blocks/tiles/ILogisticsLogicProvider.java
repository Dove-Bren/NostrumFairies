package com.smanzana.nostrumfairies.blocks.tiles;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILogisticsLogicProvider {

	public LogisticsLogicComponent getLogicComponent();
	
	public BlockPos getPos();
	
	public World getWorld();
	
}
