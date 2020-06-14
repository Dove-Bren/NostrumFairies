package com.smanzana.nostrumfairies.logistics.task;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.fairy.EntityFairyBase;

/**
 * Generic fairy task.
 * This is a job that fairies will do.
 * @author Skyler
 *
 */
public interface ILogisticsTask {

	/**
	 * Return the display name of the task
	 * @return
	 */
	public String getDisplayName();
	
	/**
	 * Whether the task can be dropped by the worker if they find one that's more important.
	 * @return
	 */
	public boolean canDrop();
	
	/**
	 * Check whether the provided worker has all they need in order to accept this task.
	 * This is being written with 'inventory requests' in mind, where this would check
	 * the items the fairy has access to.
	 * @param worker
	 * @return
	 */
	public boolean canAccept(EntityFairyBase worker);
	
	/**
	 * Called when the worker is no longer able or willing to perform the task.
	 * This may include death.
	 */
	public void onDrop(@Nullable EntityFairyBase worker);
	
	/**
	 * Called when a worker has picked up this task and is going to start working on it.
	 * @param worker
	 */
	public void onAccept(EntityFairyBase worker);
	
	/**
	 * Check and return whether the provided task is one that can be taken and done at the same time.
	 * @param other
	 * @return true if the task can be merged
	 */
	public boolean canMerge(ILogisticsTask other);
	
	/**
	 * Merge the provided other task into this same one.
	 * This will only be called if canMerge returns true.
	 * The 'other' task will be destroyed afterwards. The task this is called on
	 * should contain both pieces of work.
	 * Note: tasks that support merging should check if they've been merged onDrop() and
	 * possibly queue up the original tasks instead of just queueing up this one.
	 * @param other
	 */
	public void mergeIn(ILogisticsTask other);
}
