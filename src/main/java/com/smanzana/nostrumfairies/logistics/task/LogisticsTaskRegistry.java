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
 * Global registry of all active fairy task requesting blocks.
 * Even if the blocks aren't loaded, this presumably has all the blocks 
 * @author Skyler
 */
public class LogisticsTaskRegistry {
	
	private static final class RegistryItem {
		
		private ILogisticsTask task;
		private IFairyWorker actor;
		
		public RegistryItem(ILogisticsTask task) {
			this.task = task;
			this.actor = null;
		}
		
		public boolean hasActor() {
			return actor != null;
		}
		
		public void setActor(IFairyWorker actor) {
			this.actor = actor;
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
	
	private static LogisticsTaskRegistry instance = null;
	
	public static LogisticsTaskRegistry instance() {
		if (instance == null) {
			instance = new LogisticsTaskRegistry();
		}
		
		return instance;
	}
	
	private List<RegistryItem> registry;
	
	private LogisticsTaskRegistry() {
		registry = new LinkedList<>();
	}
	
	public void clear() {
		registry.clear();
	}
	
	/**
	 * Register a task with the registry so that workers can see it and decide to perform it
	 * @param task
	 */
	public void register(ILogisticsTask task) {
		registry.add(new RegistryItem(task));
		
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
		Iterator<RegistryItem> it = registry.iterator();
		while (it.hasNext()) {
			RegistryItem item = it.next();
			if (item.task == task) {
				if (item.hasActor()) {
					IFairyWorker oldActor = item.actor;
					item.setActor(null);
					item.task.onDrop(oldActor);
					oldActor.cancelTask();
				}
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
		RegistryItem item = findTaskItem(task);
		if (item == null) {
			throw new RuntimeException("Attempted to claim a logistics task before it was registered in the registry");
		}
		
		if (item.hasActor()) {
			forfitTask(task);
		}
		
		item.setActor(actor);
		task.onAccept(actor);
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
			throw new RuntimeException("Attempted to forgit a logistics task before it was registered in the registry");
		}
		
		IFairyWorker oldActor = item.actor;
		item.setActor(null);
		item.task.onDrop(oldActor);
		oldActor.cancelTask();
		
		// split up merged tasks
		revoke(task);
		task.unmerge().forEach((t) -> {
			LogisticsTaskRegistry.instance().register(t);
		});
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
