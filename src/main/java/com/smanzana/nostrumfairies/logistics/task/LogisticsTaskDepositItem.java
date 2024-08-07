package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.IncomingItemRecord;
import com.smanzana.nostrumfairies.tiles.InputChestTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/*
 * Pick up an item and then deposit it somewhere in the network
 */
public class LogisticsTaskDepositItem extends LogisticsTaskBase implements ILogisticsItemTask {
	
	private static enum Phase {
		IDLE,
		RETRIEVING,
		DELIVERING,
		DONE,
	}
	
	private String displayName;
	private @Nonnull ItemStack item = ItemStack.EMPTY; // stacksize used for quantity and merge status
	private @Nullable ILogisticsComponent component;
	private @Nullable LivingEntity entity;
	
	private @Nullable List<ILogisticsTask> mergedTasks;
	private LogisticsTaskDepositItem compositeTask;
	
	private IItemCarrierFey fairy;
	private Phase phase;
	private LogisticsSubTask retrieveTask;
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
	
	private LogisticsTaskDepositItem(String displayName, @Nonnull ItemStack item) {
		Validate.notNull(item);
		this.displayName = displayName;
		this.item = item;
		phase = Phase.IDLE;
	}
	
	public LogisticsTaskDepositItem(ILogisticsComponent owningComponent, String displayName, ItemStack item) {
		this(displayName, item);
		this.component = owningComponent;
	}
	
	public LogisticsTaskDepositItem(LivingEntity requester, String displayName, ItemStack item) {
		this(displayName, item);
		this.entity = requester;
	}
	
	private static LogisticsTaskDepositItem makeComposite(LogisticsTaskDepositItem left, LogisticsTaskDepositItem right) {
		LogisticsTaskDepositItem composite;
		ItemStack item = left.item.copy();
		item.grow(right.item.getCount());
		if (left.entity == null) {
			composite = new LogisticsTaskDepositItem(left.component, left.displayName, item);
		} else {
			composite = new LogisticsTaskDepositItem(left.entity, left.displayName, item);
		}
		
		composite.fairy = left.fairy;
		
		composite.mergedTasks = new LinkedList<>();
		composite.mergedTasks.add(left);
		composite.mergedTasks.add(right);
		
		composite.phase = Phase.RETRIEVING;
		composite.animCount = 0;
		composite.tryTasks(composite.fairy);
		
		left.compositeTask = composite;
		right.compositeTask = composite;
		return composite;
	}
	
	@Override
	public String getDisplayName() {
		return displayName + " (" + item.getItem().getRegistryName() + " x " + item.getCount() + " - " + phase.name() + ")";
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
		// If part of a composite, let it know that this subtask has been dropped
		if (this.compositeTask != null) {
			this.compositeTask.dropMerged(this);
		}
		
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
		phase = Phase.RETRIEVING;
		animCount = 0;
		tryTasks(worker);
		
		if (deliverTask != null) {
			deliveryRecord = fairy.getLogisticsNetwork().addIncomingItem(dropoffComponent, this, new ItemDeepStack(item));
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
		if (other instanceof LogisticsTaskDepositItem) {
			LogisticsTaskDepositItem otherTask = (LogisticsTaskDepositItem) other;
			
			// Are these requests FROM the same place?
			if (!Objects.equals(this.entity, otherTask.entity)
					|| !Objects.equals(this.component, otherTask.component)) {
				return false;
			}
			
			// Are these task going to deposit INTO the same place?
			if (this.dropoffComponent != otherTask.dropoffComponent) {
				return false;
			}
			
			return ItemStacks.stacksMatch(item, otherTask.item) && (item.getCount() + otherTask.item.getCount() <= item.getMaxStackSize());
		}
		
		return false;
	}
	
	private void dropMerged(LogisticsTaskDepositItem otherTask) {
		if (this.mergedTasks.remove(otherTask)) {
			this.item.shrink(otherTask.item.getCount());
			otherTask.compositeTask = null;
			
			if (!this.mergedTasks.isEmpty()) {
				this.tryTasks(getCurrentWorker());
			}
		}
	}
	
	private void mergeToComposite(LogisticsTaskDepositItem otherTask) {
		this.item.grow(otherTask.item.getCount());
		mergedTasks.add(otherTask);
		otherTask.compositeTask = this;
		tryTasks(fairy);
	}

	@Override
	public ILogisticsTask mergeIn(ILogisticsTask other) {
		if (this.mergedTasks == null) {
			return makeComposite(this, (LogisticsTaskDepositItem) other);
		} // else
		
		this.mergeToComposite((LogisticsTaskDepositItem) other);
		return this;
	}

	@Override
	public @Nullable ILogisticsComponent getSourceComponent() {
		return component;
	}
	
	public @Nullable LivingEntity getSourceEntity() {
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
	
	public @Nullable IItemCarrierFey getCurrentWorker() {
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
				// If this is a composite task, trust what the merge tasks have picked (and be
				// comfy because we make sure only same-destination tasks can be merged)
				if (this.mergedTasks == null) {
					dropoffComponent = network.getStorageForItem(component == null ? entity.world : component.getWorld(),
							component == null ? entity.getPosition() : component.getPosition(),
							item,
							(comp) -> {
								if (component != null) {
									// Can't be an input chest
									return comp != component && !(comp instanceof InputChestTileEntity);
								}
								
								return true;
							});
					if (dropoffComponent != null) {
						deliverTask = LogisticsSubTask.Move(dropoffComponent.getPosition());
					}
				} else {
					dropoffComponent = ((LogisticsTaskDepositItem) this.mergedTasks.get(0)).dropoffComponent;
					deliverTask = LogisticsSubTask.Move(dropoffComponent.getPosition());
				}
			}
		}
		
		if (deliverTask != null) {
			workTask = LogisticsSubTask.Break(deliverTask.getPos());
			
			// make deliver task
			if (component == null) {
				retrieveTask = LogisticsSubTask.Move(entity);
			} else {
				retrieveTask = LogisticsSubTask.Move(component.getPosition());
			}
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
	
	private void syncChildPhases() {
		if (this.mergedTasks != null) {
			for (ILogisticsTask task : this.mergedTasks) {
				LogisticsTaskDepositItem otherTask = (LogisticsTaskDepositItem) task;
				otherTask.phase = this.phase;
			}
		}
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
				phase = Phase.DELIVERING;
				syncChildPhases();
				takeItems();
			}
			break;
		case DELIVERING:
			phase = Phase.DONE;
			syncChildPhases();
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
				if (entity instanceof PlayerEntity) {
					PlayerEntity player = (PlayerEntity) entity;
					Inventories.remove(player.inventory, stack);
				} else if (entity instanceof IItemCarrierFey) {
					IItemCarrierFey carrier = (IItemCarrierFey) entity;
					carrier.removeItem(stack);
				}
			}
			fairy.addItem(stack);
		}
	}
	
	private void giveItems() {
		if (this.mergedTasks == null) {
			IItemCarrierFey worker = fairy; // capture before making changes!
			ItemStack stack = item.copy();
			
			dropoffComponent.addItem(stack);
			worker.removeItem(stack);
			
			if (deliveryRecord != null) {
				fairy.getLogisticsNetwork().removeIncomingItem(dropoffComponent, deliveryRecord);
			}
		} else {
			// Make the merged tasks do it so they can update the network correctly
			for (ILogisticsTask task : this.mergedTasks) {
				((LogisticsTaskDepositItem) task).giveItems();
			}
		}
		
		// Note: If this ever registers 'incoming' or 'future' items, this is where it should clean it
		// up normally (and in the regular onDrop, etc.).
		// Note: If that happens, make sure that composite tasks call for each child so they can update
		// the registry correctly.
	}

	@Override
	public boolean isValid() {
		// Check whether the item we want is still available
		if (this.deliverTask == null) {
			return false;
		}
		
		if (this.mergedTasks != null && this.mergedTasks.isEmpty()) {
			return false;
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
		return null; // source component/entity is the start location
	}
}
