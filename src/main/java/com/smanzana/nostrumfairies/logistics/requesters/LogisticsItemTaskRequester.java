package com.smanzana.nostrumfairies.logistics.requesters;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsItemTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Wraps up making requests to the logistics network as determined by a list of itemstacks.
 * @author Skyler
 *
 */
public abstract class LogisticsItemTaskRequester<T extends ILogisticsItemTask> implements ILogisticsTaskListener {

	protected List<T> currentTasks;
	protected @Nullable LogisticsNetwork network;
	private @Nullable ILogisticsComponent component;
	private @Nullable LivingEntity entity;
	private @Nullable ILogisticsTaskListener chainListener;
	
	private LogisticsItemTaskRequester(LogisticsNetwork network) {
		this.currentTasks = new LinkedList<>();
		this.network = network;
	}
	
	public LogisticsItemTaskRequester(LogisticsNetwork network, ILogisticsComponent component) {
		this(network);
		this.component = component;
	}
	
	public LogisticsItemTaskRequester(LogisticsNetwork network, LivingEntity entityRequester) {
		this(network);
		this.entity = entityRequester;
	}
	
	public @Nullable ILogisticsComponent getLogisticsComponent() {
		return component;
	}
	
	public @Nullable LivingEntity getEntityRequester() {
		return entity;
	}
	
	public void addChainListener(ILogisticsTaskListener chainListener) {
		this.chainListener = chainListener;
	}
	
	public void setNetwork(@Nullable LogisticsNetwork network) {
		this.network = network;
	}
	
	public void clearRequests() {
		if (network == null) {
			return;
		}
		
		List<T> list = this.currentTasks;
		currentTasks = new LinkedList<>();
		for (ILogisticsItemTask task : list) {
			network.getTaskRegistry().revoke(task);
		} // else not registered
}
	
	/**
	 * Hook to filter requests if you want the requester to consider some tasks as basically
	 * invisible. In other words, requests can be filtered out of they no longer match to
	 * an item request from the inventory.
	 * @param taskList
	 * @return
	 */
	protected List<T> filterActiveRequests(final List<T> taskList) {
		return taskList;
	}
	
	private void dropRequests(long count, List<T> taskList) {
		// TODO also sort forward tasks that ahven't been started
		Collections.sort(taskList, (left, right) -> {
			return left.isComplete() ? -1
				: right.isComplete() ? 1
				: 0;
		});
		Iterator<T> it = taskList.iterator();
		
		// requests sorted with completed ones first
		while (it.hasNext() && count > 0) {
			T task = it.next();
			if (task.getAttachedItem().getCount() <= count) {
				count -= task.getAttachedItem().getCount();
				
				if (!task.isComplete()) {
					network.getTaskRegistry().revoke(task);
				} // else already unregistered for us
				
				it.remove();
				currentTasks.remove(task);
			}
		}
	}
	
	protected abstract T makeTask(ILogisticsComponent component, ItemDeepStack item);
	
	protected abstract T makeTask(LivingEntity entity, ItemDeepStack item);
	
	protected void registerTask(T task) {
		network.getTaskRegistry().register(task, this);
	}
	
	private void addRequests(ItemDeepStack item) {
		for (long i = 0; i < item.getCount(); i++) {
			T task;
			if (this.entity == null) {
				task = makeTask(component, new ItemDeepStack(item.getTemplate(), 1));
			} else {
				task = makeTask(entity, new ItemDeepStack(item.getTemplate(), 1));
			}
			registerTask(task);
			this.currentTasks.add(task);
		}
	}
	
	public void updateRequestedItems(Collection<ItemStack> items) {
		if (network == null) {
			return;
		}
		
		// loop through all tasks and see if we still need them.
		// Then, request items that we don't have tasks for
		List<ItemDeepStack> deeps = new LinkedList<>();
		
		if (items != null) {
			for (ItemStack item : items) {
				if (item.isEmpty()) {
					continue;
				}
				
				boolean found = false;
				for (ItemDeepStack deep : deeps) {
					if (deep.canMerge(item)) {
						deep.add(item);
						found = true;
						break;
					}
				}
				
				if (!found) {
					deeps.add(new ItemDeepStack(item));
				}
			}
		}
		
		// for each task, subtract how many we have submitted. Negatives will be tasks we need to drop, and
		// positives will be new requests we need to make
		Map<ItemDeepStack, List<T>> map = new HashMap<>();
		for (T task : filterActiveRequests(currentTasks)) {
			List<T> list;
			ItemDeepStack item = task.getAttachedItem();
			boolean found =  false;
			for (ItemDeepStack deep : deeps) {
				if (deep.canMerge(item)) {
					deep.add(-item.getCount());
					found = true;
					
					list = map.get(deep);
					if (list == null) {
						list = new LinkedList<>();
						map.put(deep, list);
					}
					list.add(task);
					
					break;
				}
			}
			
			if (!found) {
				item = new ItemDeepStack(item.getTemplate(), item.getCount());
				item.add(-2 * item.getCount());
				deeps.add(item);
				
				list = new LinkedList<>();
				map.put(item, list);
				list.add(task);
			}
		}
		
		for (ItemDeepStack deep : deeps) {
			if (deep.getCount() == 0) {
				continue; // have an appropriate number of requests!
			}
			
			List<T> list = map.get(deep);
			if (deep.getCount() < 0) {
				// Too many requests. Gotta drop some!
				dropRequests(-deep.getCount(), list);
			} else {
				// New requests to make!
				addRequests(deep);
			}
			
		}
	}
	
	protected List<T> getCurrentTasks() {
		return currentTasks;
	}
	
	@Override
	public void onTaskDrop(ILogisticsTask task, IFeyWorker worker) {
		// Subclasses can extend to get this themselves instead of passing themselves in as chain listeners.
		// Or they can pass in something else as a chain listener.
		if (this.chainListener != null) {
			this.chainListener.onTaskDrop(task, worker);
		}
	}

	@Override
	public void onTaskAccept(ILogisticsTask task, IFeyWorker worker) {
		if (this.chainListener != null) {
			this.chainListener.onTaskAccept(task, worker);
		}
	}
	
	@Override
	public void onTaskComplete(ILogisticsTask task, IFeyWorker worker) {
		if (this.chainListener != null) {
			this.chainListener.onTaskComplete(task, worker);
		}
		
		this.currentTasks.remove(task);
	}
}
