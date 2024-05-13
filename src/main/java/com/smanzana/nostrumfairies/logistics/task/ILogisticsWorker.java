package com.smanzana.nostrumfairies.logistics.task;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

public interface ILogisticsWorker {
	
	/**
	 * Return a unique identifier for this worker
	 * @return
	 */
	public String getLogisticsID();

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
