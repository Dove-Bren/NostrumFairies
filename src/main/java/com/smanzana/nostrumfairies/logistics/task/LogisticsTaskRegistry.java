package com.smanzana.nostrumfairies.logistics.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

/**
 * Registry of all active fairy task requesting blocks.
 * Even if the blocks aren't loaded, this presumably has all the blocks 
 * @author Skyler
 */
public class LogisticsTaskRegistry {
	
	private static final class RegistryItem {
		
		private ILogisticsTask task;
		private @Nullable IFeyWorker actor;
		private @Nullable ILogisticsTaskListener listener;
		
		public RegistryItem(ILogisticsTask task, @Nullable ILogisticsTaskListener listener) {
			this.task = task;
			this.listener = listener;
			this.actor = null;
		}
		
		public boolean hasActor() {
			return actor != null;
		}
		
		public void setActor(IFeyWorker actor) {
			this.actor = actor;
		}
		
		public boolean hasListener() {
			return listener != null;
		}
		
		public @Nullable ILogisticsTaskListener getListener() {
			return listener;
		}
	}
	
	public static final class FairyTaskPair {
		
		public final ILogisticsComponent component;
		public final ILogisticsTask task;
		
		public FairyTaskPair(ILogisticsComponent component, ILogisticsTask task) {
			this.component = component;
			this.task = task;
		}
	}
	
	public static interface LogisticsTaskLogger {
		public void LogTaskUpdate(ILogisticsTask task, @Nullable IFeyWorker worker, String msg);
	}
	
	private List<RegistryItem> registry;
	private LogisticsTaskLogger logger;
	
	public LogisticsTaskRegistry(LogisticsTaskLogger logger) {
		registry = new LinkedList<>();
		this.logger = logger;
	}
	
	protected void log(ILogisticsTask task, @Nullable IFeyWorker worker, String msg) {
		this.logger.LogTaskUpdate(task, worker, msg);
	}
	
	public void clear() {
		registry.clear();
	}
	
	/**
	 * Register a task with the registry so that workers can see it and decide to perform it
	 * @param task
	 */
	public void register(ILogisticsTask task, @Nullable ILogisticsTaskListener listener) {
		registry.add(new RegistryItem(task, listener));
		
		if (task.getSourceComponent() == null && task.getSourceEntity() == null) {
			throw new RuntimeException("Logistics task registered without an attached component OR an attached entity");
		}
		
		log(task, null, "Task registered");
	}
	
	/**
	 * Remove a task from the registry.
	 * Note: this method notifies the actor and the task that it has been dropped, if it's active.
	 * @param task
	 */
	public void revoke(ILogisticsTask task) {
		log(task, null, "Revoking task");
		Iterator<RegistryItem> it = registry.iterator();
		while (it.hasNext()) {
			RegistryItem item = it.next();
			if (item.task == task) {
				log(task, null, "Found registry item to revoke");
				IFeyWorker oldActor = item.actor;
				if (item.hasActor()) {
					log(task, oldActor, "Dropping actor during revoke");
					item.setActor(null);
					oldActor.dropTask(task);
				}
				item.task.onDrop(oldActor);
				if (item.hasListener()) {
					item.getListener().onTaskDrop(task, oldActor);
				}
				item.task.onRevoke();
				it.remove();
			}
		}
	}
	
	private @Nullable RegistryItem findTaskItem(ILogisticsTask task) {
		for (RegistryItem item : registry) {
			if (item.task == task) {
				return item;
			}
		}
		
		return null;
	}
	
	public List<ILogisticsTask> findTasks(LogisticsNetwork network, IFeyWorker actor, @Nullable Predicate<ILogisticsTask> filter) {
		final List<ILogisticsTask> list = new LinkedList<>();
		
		registry.forEach((item) -> {
			if (item.hasActor()) {
				return;
			}
			
			if (filter != null && !filter.apply(item.task)) {
				return;
			}
			
			if (!item.task.canAccept(actor)) {
				return;
			}
			
			list.add(item.task);
		});
		
		return list;
	}
	
	/**
	 * Mark the task as being worked on.
	 * @param task
	 * @param actor
	 */
	public void claimTask(ILogisticsTask task, IFeyWorker actor) {
		log(task, actor, "Claiming task");
		RegistryItem item = findTaskItem(task);
		if (item == null) {
			throw new RuntimeException("Attempted to claim a logistics task before it was registered in the registry");
		}
		
		if (item.hasActor()) {
			forfitTask(task);
		}
		
		item.setActor(actor);
		task.onAccept(actor);
		if (item.hasListener()) {
			item.getListener().onTaskAccept(task, actor);
		}
	}
	
	/**
	 * Mark that the task has been dropped.
	 * Note: calls onDrop methods
	 * @param dimension
	 * @param pos
	 */
	public void forfitTask(ILogisticsTask task) {
		RegistryItem item = findTaskItem(task);
		if (item == null) {
			//throw new RuntimeException("Attempted to forfit a logistics task before it was registered in the registry");
			NostrumFairies.logger.error("Attempted to forfit a logistics task before it was registered in the registry");
			return;
		}
		
		IFeyWorker oldActor = item.actor;
		log(task, oldActor, "Forfitting task");
		
		item.setActor(null);
		item.task.onDrop(oldActor);
		if (item.hasListener()) {
			item.getListener().onTaskDrop(task, oldActor);
		}
	}
	
	public void completeTask(ILogisticsTask task) {
		RegistryItem item = findTaskItem(task);
		if (item == null) {
			throw new RuntimeException("Attempted to complete a logistics task before it was registered in the registry");
		}
		
		IFeyWorker actor = item.actor;
		log(task, actor, "Completing task");
		if (item.hasListener()) {
			item.getListener().onTaskComplete(task, actor);
		}
		
		registry.remove(item);
	}

	public Collection<ILogisticsTask> allTasks() {
		// TODO remove me. This sucks.
		List<ILogisticsTask> list = new ArrayList<>(registry.size());
		for (RegistryItem item : registry) {
			list.add(item.task);
		}
		return list;
	}
	
	public @Nullable IFeyWorker getCurrentWorker(ILogisticsTask task) {
		RegistryItem record = findTaskItem(task);
		if (record == null) {
			return null;
		}
		
		return record.actor;
	}
}
