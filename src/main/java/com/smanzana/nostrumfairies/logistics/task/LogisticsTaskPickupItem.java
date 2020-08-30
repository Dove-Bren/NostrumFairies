package com.smanzana.nostrumfairies.logistics.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.IncomingItemRecord;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/*
 * Pick up an item and then deposit it somewhere in the network
 */
public class LogisticsTaskPickupItem implements ILogisticsTask {
	
	private static enum Phase {
		IDLE,
		PICKINGUP,
		DELIVERING,
		DONE,
	}
	
	private String displayName;
	private EntityItem item;
	private ILogisticsComponent owningComponent;
	
	private IItemCarrierFey fairy;
	private Phase phase;
	private LogisticsSubTask pickupTask;
	private LogisticsSubTask workTask;
	private LogisticsSubTask deliverTask;
	
	
	private int animCount;
	private @Nullable ILogisticsComponent dropoffComponent;
	private @Nullable IncomingItemRecord deliveryRecord;
	private @Nullable UUID networkCacheKey;

	/**
	 * cached based on networkCacheKey. If inactive, guards checking if the task is possible.
	 * If the task is going, guards checking if the task is still valid.
	 */
	private boolean networkCachedItemResult;
	
	public LogisticsTaskPickupItem(ILogisticsComponent owningComponent, String displayName, EntityItem item) {
		this.displayName = displayName;
		this.item = item;
		this.owningComponent = owningComponent;
		phase = Phase.IDLE;
	}
	
	@Override
	public String getDisplayName() {
		return displayName + " (" + phase.name() + " - " + item + ")";
	}

	@Override
	public boolean canDrop() {
		return false;
	}

	@Override
	public boolean canAccept(IFeyWorker worker) {
		if (!(worker instanceof IItemCarrierFey)) {
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
	public void onDrop(IFeyWorker worker) {
		// TODO some part of this is duping items. The fairy drops the item, and also is still able to deliver it perhaps?
		// Edit: possibly related, shutting things down while a fairy has an item leaves the fairy with the item in their inventory
		
		if (deliveryRecord != null) {
			fairy.getLogisticsNetwork().removeIncomingItem(dropoffComponent, deliveryRecord);
		}
		
		releaseTasks();
		this.fairy = null;
		phase = Phase.IDLE;
	}

	@Override
	public void onAccept(IFeyWorker worker) {
		this.fairy = (IItemCarrierFey) worker;
		phase = Phase.PICKINGUP;
		animCount = 0;
		tryTasks(worker);
		
		if (deliverTask != null) {
			deliveryRecord = fairy.getLogisticsNetwork().addIncomingItem(dropoffComponent, this, new ItemDeepStack(item.getEntityItem()));
		}
		
		this.networkCacheKey = null; //reset so 'isValid' runs fully the first time
	}
	
	@Override
	public void onRevoke() {
		// Item either in inventory OR in fairy carrier inventory which are taken care of.
		// Nothing to do except let the network know the item isn't coming yet after all.
		if (deliveryRecord != null) {
			fairy.getLogisticsNetwork().removeIncomingItem(dropoffComponent, deliveryRecord);
		}
	}

	@Override
	public boolean canMerge(ILogisticsTask other) {
		return false;
	}
	
	@Override
	public ILogisticsTask mergeIn(ILogisticsTask other) {
		return this;
	}

	@Override
	public @Nullable ILogisticsComponent getSourceComponent() {
		return owningComponent;
	}
	
	public @Nullable EntityLivingBase getSourceEntity() {
		return null;
	}

	@Override
	public Collection<ILogisticsTask> unmerge() {
		return Lists.newArrayList(this);
	}
	
	public boolean isActive() {
		return fairy != null;
	}
	
	public @Nullable IItemCarrierFey getCurrentWorker() {
		return fairy;
	}
	
	public EntityItem getEntityItem() {
		return item;
	}
	
	private void releaseTasks() {
		pickupTask = null;
		deliverTask = null;
		workTask = null;
		dropoffComponent = null;
		deliveryRecord = null;
	}
	
	private boolean tryTasks(IFeyWorker fairy) {
		releaseTasks();
		
		// Make deliver task
		deliverTask = null;
		{
			LogisticsNetwork network = fairy.getLogisticsNetwork();
			if (network != null) {
				// Find a place where we can drop off the item after we pick it up.
				dropoffComponent = network.getStorageForItem(item.worldObj, item.getPosition(), item.getEntityItem());
				
				if (dropoffComponent != null) {
					deliverTask = LogisticsSubTask.Move(dropoffComponent.getPosition());
				}
			}
		}
		
		if (deliverTask != null) {
			workTask = LogisticsSubTask.Break(deliverTask.getPos());
			
			// make deliver task
			pickupTask = LogisticsSubTask.Move(item);
		}
		
		return deliverTask != null;
		
	}

	@Override
	public LogisticsSubTask getActiveSubtask() {
		switch (phase) {
		case IDLE:
			return null;
		case PICKINGUP:
			if (animCount == 0) {
				return pickupTask;
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
		case PICKINGUP:
			// Moved to pick up the item. Give it to them and then move on
			if (animCount < 3) { // TODO was 20
				animCount++;
			} else {
				phase = Phase.DELIVERING;
				pickupItem();
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
	
	private void pickupItem() {
		ItemDeepStack giveItem = new ItemDeepStack(item.getEntityItem());
		while (giveItem.getCount() > 0) {
			ItemStack stack = giveItem.splitStack((int) Math.min(giveItem.getTemplate().getMaxStackSize(), giveItem.getCount()));
			fairy.addItem(stack);
		}
		item.worldObj.removeEntity(item);
	}
	
	private void giveItems() {
		IItemCarrierFey worker = fairy; // capture before making changes!
		ItemStack old[] = worker.getCarriedItems();
		ItemStack items[] = Arrays.copyOf(old, old.length);
		
		for (ItemStack stack : items) {
			if (stack == null) {
				continue;
			}
			dropoffComponent.addItem(stack);
			worker.removeItem(stack);
		}
		
		if (deliveryRecord != null) {
			fairy.getLogisticsNetwork().removeIncomingItem(dropoffComponent, deliveryRecord);
		}
	}

	@Override
	public boolean isValid() {
		if (this.deliverTask == null) {
			return false;
		}
		
		if (this.phase == Phase.IDLE || this.phase == Phase.PICKINGUP) {
			if (this.item == null || this.item.isDead) {
				return false;
			}
		}
		
		// could check if there's space to put it, and after some time, drop it on the floor.
		// requester should cancel if the item is taken away before the worker gets there.
		
		return true;
	}
	
	public boolean hasTakenItems() {
		return this.phase == Phase.DELIVERING || this.phase == Phase.DONE;
	}
	
	public @Nullable BlockPos getDestination() {
		if (this.deliverTask != null) {
			return this.deliverTask.getPos();
		}
		
		return null;
	}
	
	@Override
	public BlockPos getStartPosition() {
		return item == null ? null : item.getPosition();
	}
}
