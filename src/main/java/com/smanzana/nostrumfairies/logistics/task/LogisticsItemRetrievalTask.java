package com.smanzana.nostrumfairies.logistics.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fairy.IFairyWorker;
import com.smanzana.nostrumfairies.entity.fairy.IItemCarrierFairy;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.RequestedItemRecord;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemStacks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class LogisticsItemRetrievalTask implements ILogisticsTask {
	
	private static enum Phase {
		IDLE,
		RETRIEVING,
		DELIVERING,
		DONE,
	}
	
	private ILogisticsTaskListener owner;
	private String displayName;
	private ItemDeepStack item; // stacksize used for quantity and merge status
	private boolean useBuffers; // can pull from buffer storage?
	private @Nullable ILogisticsComponent component;
	private @Nullable EntityLivingBase entity;
	
	private IItemCarrierFairy fairy;
	private Phase phase;
	private LogisticsSubTask retrieveTask;
	private LogisticsSubTask workTask;
	private LogisticsSubTask deliverTask;
	
	
	private int animCount;
	private @Nullable ILogisticsComponent pickupComponent;
	private @Nullable RequestedItemRecord requestRecord;
	private @Nullable UUID networkCacheKey;

	/**
	 * cached based on networkCacheKey. If inactive, guards checking if the task is possible.
	 * If the task is going, guards checking if the task is still valid.
	 */
	private boolean networkCachedItemResult;
	
	private LogisticsItemRetrievalTask(ILogisticsTaskListener owner, String displayName, ItemDeepStack item, boolean useBuffers) {
		this.owner = owner;
		this.displayName = displayName;
		this.item = item;
		this.useBuffers = useBuffers;
		phase = Phase.IDLE;
	}
	
	public LogisticsItemRetrievalTask(ILogisticsTaskListener owner, ILogisticsComponent owningComponent, String displayName, ItemDeepStack item, boolean useBuffers) {
		this(owner, displayName, item, useBuffers);
		this.component = owningComponent;
	}
	
	public LogisticsItemRetrievalTask(ILogisticsTaskListener owner, EntityLivingBase requester, String displayName, ItemDeepStack item, boolean useBuffers) {
		this(owner, displayName, item, useBuffers);
		this.entity = requester;
	}
	
	public LogisticsItemRetrievalTask(ILogisticsTaskListener owner, @Nullable ILogisticsComponent owningComponent, String displayName, ItemStack item, boolean useBuffers) {
		this(owner, owningComponent, displayName, new ItemDeepStack(item, 1), useBuffers);
	}

	@Override
	public String getDisplayName() {
		return displayName + " (" + item.getTemplate().getUnlocalizedName() + " x " + item.getCount() + " - " + phase.name() + ")";
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
		
		LogisticsNetwork network = worker.getLogisticsNetwork();
		if (network == null) {
			return false;
		}
		
		if (this.networkCacheKey == null || !this.networkCacheKey.equals(network.getCacheKey())) {
			networkCachedItemResult = tryTasks(worker);
			this.networkCacheKey = network.getCacheKey();
		}
		
		return networkCachedItemResult;
	}

	@Override
	public void onDrop(IFairyWorker worker) {
		owner.onTaskDrop(this, worker);
		
		// TODO did we merge? Do some work here if we did!
		
		// TODO some part of this is duping items. The fairy drops the item, and also is still able to deliver it perhaps?
		
		releaseTasks();
		
		this.fairy = null;
		phase = Phase.IDLE;
	}

	@Override
	public void onAccept(IFairyWorker worker) {
		owner.onTaskAccept(this, worker);
		this.fairy = (IItemCarrierFairy) worker;
		phase = Phase.RETRIEVING;
		animCount = 0;
		tryTasks(worker);
		
		if (retrieveTask != null) {
			requestRecord = fairy.getLogisticsNetwork().addRequestedItem(pickupComponent, this, item);
		}
		
		this.networkCacheKey = null; //reset so 'isValid' runs fully the first time
		
		// TODO hook up a way to say 'not from buffer chests!' and make buffer chests do that always, and
		// output chests have an option. Somehow we gotta make sure that workers know that it's not okay
		// too, which makes me thing that it'd be nice to consolidate all of the 'find the items in the network'
		// code
	}

	@Override
	public boolean canMerge(ILogisticsTask other) {
		if (other instanceof LogisticsItemRetrievalTask) {
			LogisticsItemRetrievalTask otherTask = (LogisticsItemRetrievalTask) other;
			
			// Are these requests to the same place?
			if (!Objects.equals(this.entity, otherTask.entity)
					|| !Objects.equals(this.component, otherTask.component)) {
				return false;
			}
			
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
	public @Nullable ILogisticsComponent getSourceComponent() {
		return component;
	}
	
	public @Nullable EntityLivingBase getSourceEntity() {
		return entity;
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
				tasks.add(new LogisticsItemRetrievalTask(owner, component, displayName, item.getTemplate(), useBuffers));
			}
		}
		
		return tasks;
	}
	
	protected LogisticsItemRetrievalTask split(int leftover) {
		ItemDeepStack newItem = new ItemDeepStack(item.getTemplate().copy(), leftover);
		
		if (entity == null) {
			return new LogisticsItemRetrievalTask(owner, component, displayName, newItem, useBuffers);
		} else {
			return new LogisticsItemRetrievalTask(owner, entity, displayName, newItem, useBuffers);
		}
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
	
	private void releaseTasks() {
		if (requestRecord != null) {
			fairy.getLogisticsNetwork().removeRequestedItem(pickupComponent, requestRecord);
		}
		
		retrieveTask = null;
		deliverTask = null;
		workTask = null;
		pickupComponent = null;
		requestRecord = null;
	}
	
	private boolean tryTasks(IFairyWorker fairy) {
		releaseTasks();
		
		// Make retrieve task
		retrieveTask = null;
		{
			// Find an item to go and pick up
			LogisticsNetwork network = fairy.getLogisticsNetwork();
			if (network != null) {
				Map<ILogisticsComponent, List<ItemDeepStack>> items = network.getNetworkItems(component == null ? entity.worldObj : component.getWorld(),
						component == null ? entity.getPosition() : component.getPosition(),
						250.0, false);
				ItemDeepStack most = null;
				ILogisticsComponent comp = null;
				for (Entry<ILogisticsComponent, List<ItemDeepStack>> entry : items.entrySet()) {
					ItemDeepStack item = null;
					
					// Ignore buffer chests if configured so
					if (!this.useBuffers && entry.getKey().isItemBuffer()) {
						continue;
					}
					
					for (ItemDeepStack deep : entry.getValue()) {
						if (this.item.canMerge(deep)) {
							// This item matches. Does it have more in its stack than our most
							if (item == null || item.getCount() < deep.getCount()) {
								item = deep;
							}
						}
					}
					
					// hmm. Should look at most and see if 'item' has more in it, but only if we WANT more than what most has?
					// Also should look at distance of components (and maybe PREFER buffer if useBuffers?) and distance from worker?
					// TODO heuristics!
					if (item != null) {
						most = item;
						comp = entry.getKey();
					}
				}
				
				if (comp != null) {
					retrieveTask = LogisticsSubTask.Move(comp.getPosition());
					pickupComponent = comp;
					
					// Make other tasks for leftovers
					@SuppressWarnings("null")
					long leftover = item.getCount() - most.getCount();
					if (leftover > 0) {
						// Adjust how much we're 'grabbing'
						item.add(-leftover);
						
						LogisticsItemRetrievalTask other = split((int)leftover);
						network.getTaskRegistry().register(other);
					}
				}
			}
		}
		
		if (retrieveTask != null) {
			workTask = LogisticsSubTask.Break(retrieveTask.getPos());
			
			// make deliver task
			deliverTask = LogisticsSubTask.Move(component == null ? entity.getPosition() : component.getPosition());
		}
		
		return retrieveTask != null;
		
	}

	@Override
	public LogisticsSubTask getActiveSubtask() {
		switch (phase) {
		case IDLE:
			return null;
		case RETRIEVING:
			if (animCount == 0) {
				return retrieveTask;
			} else {
				return workTask;
			}
		case DELIVERING:
			return deliverTask;
		case DONE:
			return null;
		}
		
		return null;
	}

	@Override
	public void markSubtaskComplete() {
		switch (phase) {
		case IDLE:
			; // ?
			break;
		case RETRIEVING:
			// Moved to pick up the item. Give it to them and then move on
			if (animCount < 3) { // TODO was 20
				animCount++;
			} else {
				takeItems();
				phase = Phase.DELIVERING;
			}
			break;
		case DELIVERING:
			phase = Phase.DONE;
			giveItems();
			break;
		case DONE:
			break;
		}
	}
	
	@Override
	public boolean isComplete() {
		return phase == Phase.DONE;
	}
	
	private void takeItems() {
		ItemDeepStack giveItem = item.copy();
		while (giveItem.getCount() > 0) {
			ItemStack stack = giveItem.splitStack((int) Math.min(giveItem.getTemplate().getMaxStackSize(), giveItem.getCount()));
			pickupComponent.takeItem(stack);
			fairy.addItem(stack);
		}
	}
	
	private void giveItems() {
		IItemCarrierFairy worker = fairy; // capture before making changes!
		ItemStack old[] = worker.getCarriedItems();
		ItemStack items[] = Arrays.copyOf(old, old.length);
		
		for (ItemStack stack : items) {
			if (entity == null) {
				component.addItem(stack);
			} else {
				if (entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entity;
					player.inventory.addItemStackToInventory(stack);
				} else {
					EntityItem item = new EntityItem(entity.worldObj, entity.posX, entity.posY + .5, entity.posZ, stack);
					entity.worldObj.spawnEntityInWorld(item);
				}
			}
			worker.removeItem(stack);
		}
	}

	@Override
	public boolean isValid() {
		// Check whether the item we want is still available
		if (this.retrieveTask == null) {
			return false;
		} // TODO is this right?
		
		if (this.phase == Phase.RETRIEVING) {
			
			LogisticsNetwork network = fairy.getLogisticsNetwork();
			if (network == null) {
				return false;
			}
			
			if (this.networkCacheKey == null || !this.networkCacheKey.equals(network.getCacheKey())) {
				this.networkCacheKey = network.getCacheKey();
			
				List<ItemDeepStack> items = fairy.getLogisticsNetwork().getNetworkItems(true).get(pickupComponent);
				if (items == null) {
					networkCachedItemResult = false;
				} else {
					long count = this.item.getCount();
					for (ItemDeepStack deep : items) {
						if (deep.canMerge(this.item)) {
							count -= deep.getCount();
							if (count <= 0) {
								break;
							}
						}
					}
					
					networkCachedItemResult = count <= 0;
				}
			}
			
			return networkCachedItemResult;
		}
		
		return true;
	}
	
	public boolean canUseBuffers() {
		return useBuffers;
	}
	
	public void setUseBuffers(boolean useBuffers) {
		this.useBuffers = useBuffers;
	}

}
