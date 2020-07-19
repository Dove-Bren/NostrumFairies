package com.smanzana.nostrumfairies.entity.fairy;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;

import net.minecraft.util.math.BlockPos;

public interface IFairyWorker {

	public static enum FairyGeneralStatus {
		WANDERING, // Not attached to a home, and therefore incapable of working
		IDLE, // Able to work, but with no work to do
		WORKING, // Working
		REVOLTING, // Refusing to work
	}
	
	public static final double MAX_FAIRY_DISTANCE_SQ = 144;
	
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
