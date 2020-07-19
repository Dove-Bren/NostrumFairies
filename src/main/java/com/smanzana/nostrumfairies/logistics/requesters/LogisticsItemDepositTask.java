package com.smanzana.nostrumfairies.logistics.requesters;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fairy.IFairyWorker;
import com.smanzana.nostrumfairies.entity.fairy.IItemCarrierFairy;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsItemTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemStacks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/*
 * Pick up an item and then deposit it somewhere in the network
 */
public class LogisticsItemDepositTask implements ILogisticsItemTask {
	
	private static enum Phase {
		IDLE,
		RETRIEVING,
		DELIVERING,
		DONE,
	}
	
	private String displayName;
	private ItemStack item; // stacksize used for quantity and merge status
	private @Nullable ILogisticsComponent component;
	private @Nullable EntityLivingBase entity;
	
	private List<ILogisticsTask> mergedTasks;
	
	private IItemCarrierFairy fairy;
	private Phase phase;
	private LogisticsSubTask retrieveTask;
	private LogisticsSubTask workTask;
	private LogisticsSubTask deliverTask;
	
	
	private int animCount;
	private @Nullable ILogisticsComponent dropoffComponent;
	private @Nullable UUID networkCacheKey;

	/**
	 * cached based on networkCacheKey. If inactive, guards checking if the task is possible.
	 * If the task is going, guards checking if the task is still valid.
	 */
	private boolean networkCachedItemResult;
	
	private LogisticsItemDepositTask(String displayName, ItemStack item) {
		this.displayName = displayName;
		this.item = item;
		phase = Phase.IDLE;
	}
	
	public LogisticsItemDepositTask(ILogisticsComponent owningComponent, String displayName, ItemStack item) {
		this(displayName, item);
		this.component = owningComponent;
	}
	
	public LogisticsItemDepositTask(EntityLivingBase requester, String displayName, ItemStack item) {
		this(displayName, item);
		this.entity = requester;
	}
	
	private static LogisticsItemDepositTask makeComposite(LogisticsItemDepositTask left, LogisticsItemDepositTask right) {
		LogisticsItemDepositTask composite;
		ItemStack item = left.item.copy();
		item.stackSize += right.item.stackSize;
		if (left.entity == null) {
			composite = new LogisticsItemDepositTask(left.component, left.displayName, item);
		} else {
			composite = new LogisticsItemDepositTask(left.entity, left.displayName, item);
		}
		
		composite.mergedTasks = new LinkedList<>();
		composite.mergedTasks.add(left);
		composite.mergedTasks.add(right);
		return composite;
	}
	
	@Override
	public String getDisplayName() {
		return displayName + " (" + item.getUnlocalizedName() + " x " + item.stackSize + " - " + phase.name() + ")";
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
		// TODO some part of this is duping items. The fairy drops the item, and also is still able to deliver it perhaps?
		// Edit: possibly related, shutting things down while a fairy has an item leaves the fairy with the item in their inventory
		
		releaseTasks();
		this.fairy = null;
		phase = Phase.IDLE;
	}

	@Override
	public void onAccept(IFairyWorker worker) {
		this.fairy = (IItemCarrierFairy) worker;
		phase = Phase.RETRIEVING;
		animCount = 0;
		tryTasks(worker);
		
		this.networkCacheKey = null; //reset so 'isValid' runs fully the first time
	}
	
	@Override
	public void onRevoke() {
		// Item either in inventory OR in fairy carrier inventory which are taken care of.
		// Nothing to do.
		;
	}

	@Override
	public boolean canMerge(ILogisticsTask other) {
		if (other instanceof LogisticsItemDepositTask) {
			LogisticsItemDepositTask otherTask = (LogisticsItemDepositTask) other;
			
			// Are these requests to the same place?
			if (!Objects.equals(this.entity, otherTask.entity)
					|| !Objects.equals(this.component, otherTask.component)) {
				return false;
			}
			
			return ItemStacks.stacksMatch(item, otherTask.item) && (item.stackSize + otherTask.item.stackSize <= item.getMaxStackSize());
		}
		
		return false;
	}
	
	private void mergeToComposite(LogisticsItemDepositTask otherTask) {
		this.item.stackSize += otherTask.item.stackSize;
		mergedTasks.add(otherTask);
	}

	@Override
	public ILogisticsTask mergeIn(ILogisticsTask other) {
		if (this.mergedTasks == null) {
			return makeComposite(this, (LogisticsItemDepositTask) other);
		} // else
		
		this.mergeToComposite((LogisticsItemDepositTask) other);
		return this;
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
		if (this.mergedTasks == null) {
			return Lists.newArrayList(this);
		}
		
		return this.mergedTasks;
	}
	
	public boolean isActive() {
		return fairy != null;
	}
	
	public @Nullable IItemCarrierFairy getCurrentWorker() {
		return fairy;
	}
	
	public ItemDeepStack getAttachedItem() {
		return new ItemDeepStack(item);
	}
	
	private void releaseTasks() {
		retrieveTask = null;
		deliverTask = null;
		workTask = null;
		dropoffComponent = null;
	}
	
	private boolean tryTasks(IFairyWorker fairy) {
		releaseTasks();
		
		// Make deliver task
		deliverTask = null;
		{
			// Find a place where we can drop off the item after we pick it up
			LogisticsNetwork network = fairy.getLogisticsNetwork();
			if (network != null) {
				dropoffComponent = network.getStorageForItem(component == null ? entity.worldObj : component.getWorld(),
						component == null ? entity.getPosition() : component.getPosition(),
						item);
				if (dropoffComponent != null) {
					deliverTask = LogisticsSubTask.Move(dropoffComponent.getPosition());
				}
			}
		}
		
		if (deliverTask != null) {
			workTask = LogisticsSubTask.Break(deliverTask.getPos());
			
			// make deliver task
			retrieveTask = LogisticsSubTask.Move(component == null ? entity.getPosition() : component.getPosition());
			// TODO make a 'move to entity' subtask?
		}
		
		return deliverTask != null;
		
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
		ItemDeepStack giveItem = new ItemDeepStack(item);
		while (giveItem.getCount() > 0) {
			ItemStack stack = giveItem.splitStack((int) Math.min(giveItem.getTemplate().getMaxStackSize(), giveItem.getCount()));
			
			if (entity == null) {
				component.takeItem(stack);
			} else {
				// take items from the entity
				if (entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entity;
					ItemStacks.remove(player.inventory, stack);
				}
			}
			fairy.addItem(stack);
		}
	}
	
	private void giveItems() {
		IItemCarrierFairy worker = fairy; // capture before making changes!
		ItemStack old[] = worker.getCarriedItems();
		ItemStack items[] = Arrays.copyOf(old, old.length);
		
		for (ItemStack stack : items) {
			dropoffComponent.addItem(stack);
			worker.removeItem(stack);
		}
	}

	@Override
	public boolean isValid() {
		// Check whether the item we want is still available
		if (this.deliverTask == null) {
			return false;
		}
		
		// could check if there's space to put it, and after some time, drop it on the floor.
		// requester should cancel if the item is taken away before the worker gets there.
		
		return true;
	}
}
