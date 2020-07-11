package com.smanzana.nostrumfairies.logistics.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private IItemCarrierFairy fairy;
	
	private Phase phase;
	private LogisticsSubTask retrieveTask;
	private LogisticsSubTask workTask;
	private LogisticsSubTask deliverTask;
	
	private @Nullable ILogisticsComponent component;
	private @Nullable EntityLivingBase entity;
	
	private int animCount;
	private @Nullable ILogisticsComponent pickupComponent;
	private @Nullable RequestedItemRecord requestRecord;
	
	private LogisticsItemRetrievalTask(ILogisticsTaskListener owner, String displayName, ItemDeepStack item) {
		this.owner = owner;
		this.displayName = displayName;
		this.item = item;
		phase = Phase.IDLE;
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
		
		return true;
	}

	@Override
	public void onDrop(IFairyWorker worker) {
		owner.onTaskDrop(this, worker);
		
		// TODO did we merge? Do some work here if we did!
		
		if (requestRecord != null) {
			fairy.getLogisticsNetwork().removeRequestedItem(pickupComponent, requestRecord);
		}
		
		this.fairy = null;
		phase = Phase.IDLE;
		retrieveTask = null;
		deliverTask = null;
		pickupComponent = null;
		requestRecord = null;
	}

	@Override
	public void onAccept(IFairyWorker worker) {
		owner.onTaskAccept(this, worker);
		this.fairy = (IItemCarrierFairy) worker;
		phase = Phase.RETRIEVING;
		animCount = 0;
		makeTasks();
		
		if (retrieveTask != null) {
			requestRecord = fairy.getLogisticsNetwork().addRequestedItem(pickupComponent, this, item);
		}
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
	
	protected LogisticsItemRetrievalTask split(int leftover) {
		ItemDeepStack newItem = new ItemDeepStack(item.getTemplate().copy(), leftover);
		
		if (entity == null) {
			return new LogisticsItemRetrievalTask(owner, component, displayName, newItem);
		} else {
			return new LogisticsItemRetrievalTask(owner, entity, displayName, newItem);
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
	
	private void makeTasks() {
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
					
					for (ItemDeepStack deep : entry.getValue()) {
						if (item == null || deep.canMerge(item)) {
							// This item matches. Does it have more in its stack than our most
							if (item == null || item.getCount() < deep.getCount()) {
								item = deep;
							}
						}
					}
					
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
						LogisticsTaskRegistry.instance().register(other);
					}
				}
			}
		}
		
		if (retrieveTask != null) {
			workTask = LogisticsSubTask.Break(retrieveTask.getPos());
			
			// make deliver task
			deliverTask = LogisticsSubTask.Move(component == null ? entity.getPosition() : component.getPosition());
		}
		
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

}
