package com.smanzana.nostrumfairies.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ILogisticsLogicProvider {

	public LogisticsLogicComponent getLogicComponent();
	
	public BlockPos getBlockPos();
	
	public Level getLevel();
	
}
