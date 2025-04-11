package com.smanzana.nostrumfairies.entity.fey;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.logistics.task.ILogisticsWorker;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;

import net.minecraft.core.BlockPos;

public interface IFeyWorker extends ILogisticsWorker {

	// Suggested maximum fairy work distance.
	// Note: If this is larger, path finding starts to break down since MC limits
	// the amount of iterations in patch finding code to 200, which things start to bump into
	// the further away they are.
	public static final double MAX_FAIRY_DISTANCE_SQ = 32 * 32;
	
	/**
	 * Get current fairy worker status
	 * @return
	 */
	public FairyGeneralStatus getStatus();
	
	/**
	 * Get the fairy's home block.
	 * @return
	 */
	@Nullable
	public BlockPos getHome();
}
