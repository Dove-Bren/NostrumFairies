package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Generic fairy task.
 * This is a job that fairies will do.
 * @author Skyler
 *
 */
public interface ILogisticsTask {
	
	/**
	 * Return a unique-ish ID for this task.
	 * ID should be unique per the network the task is in, which tasks have low visibility into.
	 * @return
	 */
	public int getTaskID();

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
	public boolean canAccept(IFeyWorker worker);
	
	/**
	 * Called when the worker is no longer able or willing to perform the task.
	 * This may include death.
	 */
	public void onDrop(@Nullable IFeyWorker worker);
	
	/**
	 * This task is being removed from the registry all together.
	 * An onDrop call will happen first. Any leftover state should be cleared.
	 */
	public void onRevoke();
	
	/**
	 * Called when a worker has picked up this task and is going to start working on it.
	 * @param worker
	 */
	public void onAccept(IFeyWorker worker);
	
	/**
	 * Check and return whether the provided task is one that can be taken and done at the same time.
	 * @param worker
	 * @param other
	 * @return true if the task can be merged
	 */
	public boolean canMerge(ILogisticsTask other);
	
	/**
	 * Merge the provided other task into this same one.
	 * This will only be called if canMerge returns true.
	 * Calling code will abandon references to the 'this' task and
	 * the 'other' task and use the return instead. Tasks that support merging should
	 * create a new <i>composite</i> task which maintains links to the ones that it's built
	 * out of. This composite task will not be registered in the task registry and will not get
	 * listener events (onDrop, onAccept, etc.) and should be able to be unmerged at any time.
	 * Methods like 'canAccept' and 'GetActiveSubtask' should return one result
	 * for the composite task.
	 * 
	 * Note: all places that interact with the task registry know to call 'unmerge' to get back
	 * the original list of tasks and use those instead of the return of this func.
	 * @param other
	 */
	public ILogisticsTask mergeIn(ILogisticsTask other);
	
	/**
	 * Split out all tasks that were merged into this one.
	 * If this task never merged with another and represents a single task, it should return a collection
	 * with itself in it. Otherwise, it should return the original objects that were merged in.
	 * @return
	 */
	public Collection<ILogisticsTask> unmerge();
	
	/**
	 * Return the Logistics Component that is making this request, if one is.
	 * Note, either this or {@link #getSourceEntity()} is expected to return a non-null result.
	 * @return
	 */
	public @Nullable ILogisticsComponent getSourceComponent();
	
	/**
	 * Return the entity that made this request, if there is one.
	 * Note, either this or {@link #getSourceComponent()} is expected to return a non-null result.
	 * @return
	 */
	public @Nullable LivingEntity getSourceEntity();
	
	/**
	 * Get the current subtask this logistics task is on.
	 * Workers should perform this action.
	 * @return
	 */
	public @Nullable LogisticsSubTask getActiveSubtask();
	
	/**
	 * Call to indicate that a worker believes they've fulfilled the active subtask
	 */
	public void markSubtaskComplete();
	
	/**
	 * Return whether the task is complete
	 * @return
	 */
	public boolean isComplete();
	
	/**
	 * Possibly return a position where a worker could expect to need to go to start the task.
	 * This is used as a hint for sorting jobs to make it easier for workers to pick tasks that have objectives they
	 * are closed to.
	 * @return A hint position, or null if that doesn't make sense for this task.
	 */
	public @Nullable BlockPos getStartPosition();
	
	/**
	 * Check whether the task is still valid, of if it should be dropped.
	 * @return
	 */
	public boolean isValid();
	
	public static Level GetSourceWorld(ILogisticsTask task) {
		Level world;
		ILogisticsComponent comp = task.getSourceComponent();
		if (comp == null) {
			world = task.getSourceEntity().getCommandSenderWorld();
		} else {
			world = comp.getWorld();
		}
		
		return world;
	}
	
	public static BlockPos GetSourcePosition(ILogisticsTask task) {
		BlockPos pos;
		ILogisticsComponent comp = task.getSourceComponent();
		if (comp == null) {
			pos = task.getSourceEntity().blockPosition();
		} else {
			pos = comp.getPosition();
		}
		
		return pos;
	}
	
	public static BlockPos GetStartPosition(ILogisticsTask task) {
		BlockPos pos = task.getStartPosition();
		if (pos == null) {
			pos = GetSourcePosition(task);
		}
		return pos;
	}
}
