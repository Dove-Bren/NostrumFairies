package com.smanzana.nostrumfairies.entity.fey;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;

import net.minecraft.util.math.BlockPos;

public interface IFeyWorker {

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
	
	/**
	 * Return the current task a fairy is working on.
	 * It's expected that a status of 'WORKING' means this will return something,
	 * while calling while IDLE would not.
	 * @return The current task if there is one, or null.
	 */
	public @Nullable ILogisticsTask getCurrentTask();
	
	/**
	 * A task claimed by this fairy is no longer needed. Drop it.
	 */
	public void dropTask(ILogisticsTask task);
	
	public @Nullable LogisticsNetwork getLogisticsNetwork();
	
}
