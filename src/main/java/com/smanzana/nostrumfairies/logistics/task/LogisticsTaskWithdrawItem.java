package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.ItemCacheType;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.RequestedItemRecord;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemDeepStacks;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class LogisticsTaskWithdrawItem implements ILogisticsItemTask {
	
	private static enum Phase {
		IDLE,
		RETRIEVING,
		WAITING,
		DELIVERING,
		DONE,
	}
	
	private String displayName;
	private ItemDeepStack item; // stacksize used for quantity and merge status
	private boolean useBuffers; // can pull from buffer storage?
	private @Nullable ILogisticsComponent component;
	private @Nullable EntityLivingBase entity;
	
	private @Nullable List<ILogisticsTask> mergedTasks;
	private @Nullable LogisticsTaskWithdrawItem compositeTask; // Task we were merged into
	
	private IItemCarrierFey fairy;
	private Phase phase;
	private LogisticsSubTask retrieveTask;
	private LogisticsSubTask workTask;
	private LogisticsSubTask deliverTask;
	private LogisticsSubTask idleTask;
	
	
	private int animCount;
	private @Nullable ILogisticsComponent pickupComponent;
	private @Nullable RequestedItemRecord requestRecord;
	private @Nullable UUID networkCacheKey;

	/**
	 * cached based on networkCacheKey. If inactive, guards checking if the task is possible.
	 * If the task is going, guards checking if the task is still valid.
	 */
	private boolean networkCachedItemResult;
	
	private LogisticsTaskWithdrawItem(String displayName, ItemDeepStack item, boolean useBuffers) {
		this.displayName = displayName;
		this.item = item;
		this.useBuffers = useBuffers;
		phase = Phase.IDLE;
	}
	
	public LogisticsTaskWithdrawItem(ILogisticsComponent owningComponent, String displayName, ItemDeepStack item, boolean useBuffers) {
		this(displayName, item, useBuffers);
		this.component = owningComponent;
	}
	
	public LogisticsTaskWithdrawItem(EntityLivingBase requester, String displayName, ItemDeepStack item, boolean useBuffers) {
		this(displayName, item, useBuffers);
		this.entity = requester;
	}
	
	public LogisticsTaskWithdrawItem(ILogisticsComponent owningComponent, String displayName, ItemStack item, boolean useBuffers) {
		this(owningComponent, displayName, new ItemDeepStack(item, 1), useBuffers);
	}
	
	private static LogisticsTaskWithdrawItem makeComposite(LogisticsTaskWithdrawItem left, LogisticsTaskWithdrawItem right) {
		LogisticsTaskWithdrawItem composite;
		ItemDeepStack item = left.item.copy();
		item.add(right.item);
		if (left.entity == null) {
			composite = new LogisticsTaskWithdrawItem(left.component, left.displayName, item, left.useBuffers);
		} else {
			composite = new LogisticsTaskWithdrawItem(left.entity, left.displayName, item, left.useBuffers);
		}
		
		// pull registry stuff
		composite.fairy = left.fairy;
		
		composite.mergedTasks = new LinkedList<>();
		composite.mergedTasks.add(left);
		composite.mergedTasks.add(right);
		
		composite.phase = Phase.RETRIEVING;
		composite.animCount = 0;
		composite.tryTasks(left.fairy);
		
		left.compositeTask = composite;
		right.compositeTask = composite;
		return composite;
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
		if (requestRecord != null) {
			fairy.getLogisticsNetwork().removeRequestedItem(pickupComponent, requestRecord);
		}
		releaseTasks();
		
		this.fairy = null;
		this.requestRecord = null;
		phase = Phase.IDLE;
	}
	
	@Override
	public void onRevoke() {
		// Only need cleanup if we were being worked, which would call onDrop.
	}

	@Override
	public void onAccept(IFeyWorker worker) {
		this.fairy = (IItemCarrierFey) worker;
		phase = Phase.RETRIEVING;
		animCount = 0;
		tryTasks(worker);
		
		if (retrieveTask != null) {
			requestRecord = fairy.getLogisticsNetwork().addRequestedItem(pickupComponent, this, item);
		}
		
		this.networkCacheKey = null; //reset so 'isValid' runs fully the first time
	}

	@Override
	public boolean canMerge(ILogisticsTask other) {
		if (this.phase == Phase.DELIVERING || this.phase == Phase.DONE) {
			return false;
		}
		
		if (other instanceof LogisticsTaskWithdrawItem) {
			LogisticsTaskWithdrawItem otherTask = (LogisticsTaskWithdrawItem) other;
			
			// Are these requests from the same place?
			if (!Objects.equals(this.entity, otherTask.entity)
					|| !Objects.equals(this.component, otherTask.component)) {
				return false;
			}
			
			// Are these tasks set up to pull from the same place?
			if (this.pickupComponent != otherTask.pickupComponent) {
				return false;
			}
			
			if (otherTask.canUseBuffers() != this.canUseBuffers()) {
				return false;
			}
			
			if (!ItemStacks.stacksMatch(item.getTemplate(), otherTask.item.getTemplate())) {
				return false;
			}
			
			// Make sure our worker would be able to even take it on.
			// We use the fact that we won't merge after the item's been picked up,
			// so the fairy's inventory should be empty still.
			ItemDeepStack result = item.copy();
			result.add(otherTask.item);
			if (!this.getCurrentWorker().canAccept(result)) {
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	private void dropMerged(LogisticsTaskWithdrawItem otherTask) {
		if (this.mergedTasks.remove(otherTask)) {
			this.item.add(-otherTask.item.getCount());
			otherTask.compositeTask = null;
			
			if (!this.mergedTasks.isEmpty()) {
				this.tryTasks(getCurrentWorker());
			}
		}
	}
	
	private void mergeToComposite(LogisticsTaskWithdrawItem otherTask) {
		this.item.add(otherTask.item.getCount());
		mergedTasks.add(otherTask);
		otherTask.compositeTask = this;
		this.tryTasks(getCurrentWorker());
	}

	@Override
	public ILogisticsTask mergeIn(ILogisticsTask other) {
		// If already a composite, just add. Otherwise, make a composite!
		if (this.mergedTasks == null) {
			return makeComposite(this, (LogisticsTaskWithdrawItem) other);
		} //else
		
		this.mergeToComposite((LogisticsTaskWithdrawItem) other);
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
		if (mergedTasks == null) {
			return Lists.newArrayList(this);
		}
		
		return mergedTasks;
	}
	
//	protected LogisticsItemWithdrawTask split(int leftover) {
//		ItemDeepStack newItem = new ItemDeepStack(item.getTemplate().copy(), leftover);
//		
//		if (entity == null) {
//			return new LogisticsItemWithdrawTask(owner, component, displayName, newItem, useBuffers);
//		} else {
//			return new LogisticsItemWithdrawTask(owner, entity, displayName, newItem, useBuffers);
//		}
//	}
	
	public boolean isActive() {
		return fairy != null;
	}
	
	public @Nullable IItemCarrierFey getCurrentWorker() {
		return fairy;
	}
	
	public ItemDeepStack getAttachedItem() {
		return item;
	}
	
	private void releaseTasks() {
		retrieveTask = null;
		deliverTask = null;
		workTask = null;
		requestRecord = null;
		pickupComponent = null;
	}
	
	private boolean tryTasks(IFeyWorker fairy) {
		releaseTasks();
		
		// Make retrieve task
		retrieveTask = null;
		{
			// Find an item to go and pick up
			LogisticsNetwork network = fairy.getLogisticsNetwork();
			if (network != null) {
				// This logic is a bit weird. If we're not a composite task, this can actually look at the network item map
				// and try to find one that matches.
				// If this is composite, we know we only merge in tasks that have the same 'pull from' component, and they all
				// check that it's okay to pull X more out of it.
				// So composites will blindly believe that the component that the tasks its merged out of has enough and make
				// a task to go there.
				if (this.mergedTasks == null) {
					Map<ILogisticsComponent, List<ItemDeepStack>> items = network.getNetworkItems(component == null ? entity.worldObj : component.getWorld(),
							component == null ? entity.getPosition() : component.getPosition(),
							250.0, ItemCacheType.NET);
					ItemDeepStack match = null;
					ILogisticsComponent comp = null;
					
					// Note: item map is sorted so that closer components are earlier in the list.
					// So just find the very first component and go to it.
					
					for (Entry<ILogisticsComponent, List<ItemDeepStack>> entry : items.entrySet()) {
						// Ignore buffer chests if configured so
						if (!this.useBuffers && entry.getKey().isItemBuffer()) {
							continue;
						}
						
						for (ItemDeepStack deep : entry.getValue()) {
							if (this.item.canMerge(deep)) {
								// This item matches. Does it have all that we want to pickup?
								if (deep.getCount() >= this.item.getCount()) {
									match = deep;
									break;
								}
							}
						}
						
						if (match != null) {
							comp = entry.getKey();
							break;
						}
					}
					
					if (comp != null) {
						retrieveTask = LogisticsSubTask.Move(comp.getPosition());
						pickupComponent = comp;
					}
				} else {
					// This is a composite. Use merged tasks' component and don't filter out requested, since merged tasks will
					// have holds in place already.
					pickupComponent = ((LogisticsTaskWithdrawItem) this.mergedTasks.get(0)).pickupComponent;
					retrieveTask = LogisticsSubTask.Move(pickupComponent.getPosition());
				}
			}
		}
		
		if (retrieveTask != null) {
			workTask = LogisticsSubTask.Break(retrieveTask.getPos());
			
			// make deliver task
			if (component == null) {
				deliverTask = LogisticsSubTask.Move(entity);
			} else {
				deliverTask = LogisticsSubTask.Move(component.getPosition());
			}
		}
		
		return retrieveTask != null;
		
	}

	@Override
	public LogisticsSubTask getActiveSubtask() {
		switch (phase) {
		case IDLE:
			return null;
		case WAITING:
			return idleTask;
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
				LogisticsTaskWithdrawItem otherTask = (LogisticsTaskWithdrawItem) task;
				otherTask.phase = this.phase;
			}
		}
	}

	@Override
	public void markSubtaskComplete() {
		// could just call this on all children
		switch (phase) {
		case IDLE:
			; // ?
			break;
		case WAITING:
			// pick random pos, go to it, and then wait a random amount of time.
			// we use animCount to count DOWN
			if (animCount > 0) {
				animCount--;
			} else {
				// Spent some time waiting. Go ahead and try and retrieve again
				phase = Phase.RETRIEVING;
				animCount = 0;
				syncChildPhases();
			}
			break;
		case RETRIEVING:
			// Moved to pick up the item. Give it to them and then move on
			if (canTakeItems()) {
				if (animCount < 3) { // TODO was 20
					animCount++;
				} else {
					takeItems();
					phase = Phase.DELIVERING;
					syncChildPhases();
				}
			} else {
				// Items aren't there yet
				idleTask = LogisticsSubTask.Idle(pickupComponent.getPosition());
				animCount = 20 * (NostrumFairies.random.nextInt(3) + 2);
				phase = Phase.WAITING;
				syncChildPhases();
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
	
	private boolean canTakeItems() {
		Collection<ItemStack> available = pickupComponent.getItems();
		
		return ItemDeepStacks.isSubset(ItemDeepStack.toDeepList(available), Lists.newArrayList(this.item));
	}
	
	private void takeItems() {
		if (this.mergedTasks == null) {
			ItemDeepStack giveItem = item.copy();
			while (giveItem.getCount() > 0) {
				ItemStack stack = giveItem.splitStack((int) Math.min(giveItem.getTemplate().getMaxStackSize(), giveItem.getCount()));
				pickupComponent.takeItem(stack);
				fairy.addItem(stack);
			}
			
			if (requestRecord != null) {
				fairy.getLogisticsNetwork().removeRequestedItem(pickupComponent, requestRecord);
			}
		} else {
			for (ILogisticsTask task : this.mergedTasks) {
				((LogisticsTaskWithdrawItem) task).takeItems();
			}
		}
	}
	
	private void giveItems() {
		IItemCarrierFey worker = fairy; // capture before making changes!
		ItemDeepStack giveItem = item.copy();
		while (giveItem.getCount() > 0) {
			ItemStack stack = giveItem.splitStack(giveItem.getTemplate().getMaxStackSize()); 
			if (entity == null) {
				component.addItem(stack.copy());
			} else {
				if (entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entity;
					player.inventory.addItemStackToInventory(stack.copy());
					player.worldObj.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .75f, 2f);
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
		
		// If this task was a merged one but all things have been pulled out, no longer valid!
		if (this.mergedTasks != null && this.mergedTasks.isEmpty()) {
			return false;
		}
		
		if (this.phase == Phase.RETRIEVING) {
			
			LogisticsNetwork network = fairy.getLogisticsNetwork();
			if (network == null) {
				return false;
			}
			
			if (this.networkCacheKey == null || !this.networkCacheKey.equals(network.getCacheKey())) {
				this.networkCacheKey = network.getCacheKey();
			
				List<ItemDeepStack> items = fairy.getLogisticsNetwork().getNetworkItems(ItemCacheType.GROSS).get(pickupComponent);
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
	
	public @Nullable BlockPos getSource() {
		if (this.retrieveTask != null) {
			return this.retrieveTask.getPos();
		}
		
		return null;
	}

	@Override
	public BlockPos getStartPosition() {
		return null; // Pickup location determined after task is picked up, so a hint location doesn't work.
	}
}
