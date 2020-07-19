package com.smanzana.nostrumfairies.logistics.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrumfairies.entity.fairy.IFairyWorker;
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
		private @Nullable IFairyWorker actor;
		private @Nullable ILogisticsTaskListener listener;
		
		public RegistryItem(ILogisticsTask task, @Nullable ILogisticsTaskListener listener) {
			this.task = task;
			this.listener = listener;
			this.actor = null;
		}
		
		public boolean hasActor() {
			return actor != null;
		}
		
		public void setActor(IFairyWorker actor) {
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
	
	private List<RegistryItem> registry;
	
	public LogisticsTaskRegistry() {
		registry = new LinkedList<>();
	}
	
	public void clear() {
		registry.clear();
	}
	
	/**
	 * Register a task with the registry so that workers can see it and decide to perform it
	 * @param task
	 */
	public void register(ILogisticsTask task, @Nullable ILogisticsTaskListener listener) {
		System.out.println("Registering task " + task.getDisplayName());
		registry.add(new RegistryItem(task, listener));
		
		ILogisticsComponent comp = task.getSourceComponent();
		if (comp == null) {
			throw new RuntimeException("Logistics task registered without an attached component");
		} //donotcheckin
	}
	
	/**
	 * Remove a task from the registry.
	 * Note: this method notifies the actor and the task that it has been dropped, if it's active.
	 * @param task
	 */
	public void revoke(ILogisticsTask task) {
		System.out.println("Revoking task " + task.getDisplayName());
		Iterator<RegistryItem> it = registry.iterator();
		while (it.hasNext()) {
			RegistryItem item = it.next();
			if (item.task == task) {
				IFairyWorker oldActor = item.actor;
				if (item.hasActor()) {
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
	
	private RegistryItem findTaskItem(ILogisticsTask task) {
		for (RegistryItem item : registry) {
			if (item.task == task) {
				return item;
			}
		}
		
		return null;
	}
	
	public List<FairyTaskPair> findTasks(LogisticsNetwork network, IFairyWorker actor, @Nullable Predicate<ILogisticsTask> filter) {
		final List<FairyTaskPair> list = new LinkedList<>();
		
		registry.forEach((item) -> {
			if (item.hasActor()) {
				return;
			}
			
			ILogisticsComponent comp = item.task.getSourceComponent();
			if (comp == null) {
				throw new RuntimeException("Logistics task registered without an attached component");
			}
			
			if (!network.getAllComponents().contains(comp)) {
				return;
			}
			
			if (filter != null && !filter.apply(item.task)) {
				return;
			}
			
			if (!item.task.canAccept(actor)) {
				return;
			}
			
			list.add(new FairyTaskPair(comp, item.task));
		});
		
		return list;
	}
	
	/**
	 * Mark the task as being worked on.
	 * @param task
	 * @param actor
	 */
	public void claimTask(ILogisticsTask task, IFairyWorker actor) {
		System.out.println("Claiming task " + task.getDisplayName() + "(" + actor + ")");
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
		System.out.println("forfitting task " + task.getDisplayName());
		RegistryItem item = findTaskItem(task);
		if (item == null) {
			throw new RuntimeException("Attempted to forfit a logistics task before it was registered in the registry");
		}
		
		IFairyWorker oldActor = item.actor;
		item.setActor(null);
		item.task.onDrop(oldActor);
		if (item.hasListener()) {
			item.getListener().onTaskDrop(task, oldActor);
		}
	}
	
	public void completeTask(ILogisticsTask task) {
		System.out.println("Completing task " + task.getDisplayName());
		RegistryItem item = findTaskItem(task);
		if (item == null) {
			throw new RuntimeException("Attempted to complete a logistics task before it was registered in the registry");
		}
		
		IFairyWorker actor = item.actor;
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
}
