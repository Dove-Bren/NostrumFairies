package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fairy.IFairyWorker;
import com.smanzana.nostrumfairies.entity.fairy.IItemCarrierFairy;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemStacks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class LogisticsItemRetrievalTask implements ILogisticsTask {
	
	private ILogisticsTaskListener owner;
	private String displayName;
	private ItemDeepStack item; // stacksize used for quantity and merge status
	private IItemCarrierFairy fairy;
	
	private @Nullable ILogisticsComponent component;
	private @Nullable EntityLivingBase entity;
	
	private LogisticsItemRetrievalTask(ILogisticsTaskListener owner, String displayName, ItemDeepStack item) {
		this.owner = owner;
		this.displayName = displayName;
		this.item = item;
	}
	
	public LogisticsItemRetrievalTask(ILogisticsTaskListener owner, ILogisticsComponent owningComponent, String displayName, ItemDeepStack item) {
		this(owner, displayName, item);
		this.component = owningComponent;
	}
	
	public LogisticsItemRetrievalTask(ILogisticsTaskListener owner, EntityLivingBase requester, String displayName, ItemDeepStack item) {
		this(owner, displayName, item);
		this.entity = requester;
	}
	
	public LogisticsItemRetrievalTask(ILogisticsTaskListener owner, @Nullable ILogisticsComponent owningComponent, String displayName, ItemStack item) {
		this(owner, owningComponent, displayName, new ItemDeepStack(item, 1));
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean canDrop() {
		return false;
	}

	@Override
	public boolean canAccept(IFairyWorker worker) {
		if (!(worker instanceof IItemCarrierFairy)) {
			return false;
		}
		
		return true;
	}

	@Override
	public void onDrop(IFairyWorker worker) {
		owner.onTaskDrop(this, worker);
		
		// TODO did we merge? Do some work here if we did!
		
		this.fairy = null;
	}

	@Override
	public void onAccept(IFairyWorker worker) {
		owner.onTaskAccept(this, worker);
		this.fairy = (IItemCarrierFairy) worker;
	}

	@Override
	public boolean canMerge(ILogisticsTask other) {
		if (other instanceof LogisticsItemRetrievalTask) {
			LogisticsItemRetrievalTask otherTask = (LogisticsItemRetrievalTask) other;
			return ItemStacks.stacksMatch(item.getTemplate(), otherTask.item.getTemplate());
		}
		
		return false;
	}

	@Override
	public void mergeIn(ILogisticsTask other) {
		LogisticsItemRetrievalTask otherTask = (LogisticsItemRetrievalTask) other;
		this.item.add(otherTask.item.getCount());
	}

	@Override
	public ILogisticsComponent getSourceComponent() {
		return component;
	}

	@Override
	public Collection<ILogisticsTask> unmerge() {
		List<ILogisticsTask> tasks;
		
		if (this.item.getCount() == 1) {
			// never merged
			tasks = Lists.newArrayList(this);
		} else {
			// make new task for each count
			tasks = Lists.newArrayListWithCapacity((int) this.item.getCount());
			for (int i = 0; i < this.item.getCount(); i++) {
				tasks.add(new LogisticsItemRetrievalTask(owner, component, displayName, item.getTemplate()));
			}
		}
		
		return tasks;
	}
	
	public boolean isActive() {
		return fairy != null;
	}
	
	public @Nullable IItemCarrierFairy getCurrentWorker() {
		return fairy;
	}
	
	public ItemDeepStack getAttachedItem() {
		return item;
	}

}
