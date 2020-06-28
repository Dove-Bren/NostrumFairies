package com.smanzana.nostrumfairies.logistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.fairy.IFairyWorker;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsItemRetrievalTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskRegistry;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Wraps requesting a dynamic list of items from the logistics network.
 * @author Skyler
 *
 */
public class LogisticsItemRequester implements ILogisticsTaskListener {

	private @Nullable ILogisticsComponent component;
	private @Nullable EntityLivingBase entity;
	private List<LogisticsItemRetrievalTask> activeTasks;
	
	private LogisticsItemRequester() {
		this.activeTasks = new LinkedList<>();
	}
	
	public LogisticsItemRequester(ILogisticsComponent component) {
		this();
		this.component = component;
	}
	
	public LogisticsItemRequester(EntityLivingBase entityRequester) {
		this();
		this.entity = entityRequester;
	}
	
	public @Nullable ILogisticsComponent getLogisticsComponent() {
		return component;
	}
	
	public @Nullable EntityLivingBase getEntityRequester() {
		return entity;
	}
	
	private void dropRequests(long count, List<LogisticsItemRetrievalTask> taskList) {
		Iterator<LogisticsItemRetrievalTask> it = taskList.iterator();
		while (it.hasNext() && count > 0) {
			LogisticsItemRetrievalTask task = it.next();
			if (task.getAttachedItem().getCount() <= count) {
				count -= task.getAttachedItem().getCount();
				
				if (entity == null) {
					LogisticsTaskRegistry.instance().revoke(task);
				} // else not registered
				it.remove();
				activeTasks.remove(task);
			}
		}
	}
	
	private LogisticsItemRetrievalTask makeTask(String name, ItemDeepStack item) {
		LogisticsItemRetrievalTask task;
		
		// Only register task with registry if it's a block task and has a position.
		// If it's from an entity, dont' register, as the entity's fairies have to do it
		if (entity == null) {
			task = new LogisticsItemRetrievalTask(this, component, name, item);
			LogisticsTaskRegistry.instance().register(task);
		} else {
			task = new LogisticsItemRetrievalTask(this, entity, name, item);
		}
		
		return task;
	}
	
	private void addRequests(ItemDeepStack item) {
		for (long i = 0; i < item.getCount(); i++) {
			LogisticsItemRetrievalTask task = makeTask("ItemRequest: " + item.getTemplate().getUnlocalizedName() + " x " + 1,
					new ItemDeepStack(item.getTemplate(), 1));
			this.activeTasks.add(task);
		}
	}
	
	public void updateRequestedItems(Collection<ItemStack> items) {
		// loop through all tasks and see if we still need them.
		// Then, request items that we don't have tasks for
		
		List<ItemDeepStack> deeps = new LinkedList<>();
		for (ItemStack item : items) {
			if (item == null) {
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
		
		// for each task, subtract how many we have submitted. Negatives will be tasks we need to drop, and
		// positives will be new requests we need to make
		Map<ItemDeepStack, List<LogisticsItemRetrievalTask>> map = new HashMap<>();
		for (LogisticsItemRetrievalTask task : activeTasks) {
			List<LogisticsItemRetrievalTask> list;
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
			
			List<LogisticsItemRetrievalTask> list = map.get(deep);
			if (deep.getCount() < 0) {
				// Too many requests. Gotta drop some!
				dropRequests(-deep.getCount(), list);
			} else {
				// New requests to make!
				addRequests(deep);
			}
			
		}
	}
	
	public void clearRequests() {
		if (entity == null) {
			for (ILogisticsTask task : this.activeTasks) {
				LogisticsTaskRegistry.instance().revoke(task);
			} // else not registered
		}
		activeTasks.clear();
	}

	@Override
	public void onTaskDrop(ILogisticsTask task, IFairyWorker worker) {
		activeTasks.remove(task);
		task.unmerge().forEach((t) -> {
			activeTasks.add((LogisticsItemRetrievalTask) t);
		});
	}

	@Override
	public void onTaskAccept(ILogisticsTask task, IFairyWorker worker) {
		// TODO Auto-generated method stub
		
	}
}
