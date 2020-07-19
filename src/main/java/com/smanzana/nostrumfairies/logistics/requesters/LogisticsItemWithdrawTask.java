package com.smanzana.nostrumfairies.logistics.requesters;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
import com.smanzana.nostrumfairies.logistics.task.ILogisticsItemTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemStacks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class LogisticsItemWithdrawTask implements ILogisticsItemTask {
	
	private static enum Phase {
		IDLE,
		RETRIEVING,
		DELIVERING,
		DONE,
	}
	
	private String displayName;
	private ItemDeepStack item; // stacksize used for quantity and merge status
	private boolean useBuffers; // can pull from buffer storage?
	private @Nullable ILogisticsComponent component;
	private @Nullable EntityLivingBase entity;
	
	private List<ILogisticsTask> mergedTasks;
	
	private IItemCarrierFairy fairy;
	private Phase phase;
	private LogisticsSubTask retrieveTask;
	private LogisticsSubTask workTask;
	private LogisticsSubTask deliverTask;
	
	
	private int animCount;
	private @Nullable ILogisticsComponent pickupComponent; // convenient if unmerged. If a composite task, filter possible components when making tasks
	private @Nullable RequestedItemRecord requestRecord;
	private @Nullable UUID networkCacheKey;

	/**
	 * cached based on networkCacheKey. If inactive, guards checking if the task is possible.
	 * If the task is going, guards checking if the task is still valid.
	 */
	private boolean networkCachedItemResult;
	
	private LogisticsItemWithdrawTask(String displayName, ItemDeepStack item, boolean useBuffers) {
		this.displayName = displayName;
		this.item = item;
		this.useBuffers = useBuffers;
		phase = Phase.IDLE;
	}
	
	public LogisticsItemWithdrawTask(ILogisticsComponent owningComponent, String displayName, ItemDeepStack item, boolean useBuffers) {
		this(displayName, item, useBuffers);
		this.component = owningComponent;
	}
	
	public LogisticsItemWithdrawTask(EntityLivingBase requester, String displayName, ItemDeepStack item, boolean useBuffers) {
		this(displayName, item, useBuffers);
		this.entity = requester;
	}
	
	public LogisticsItemWithdrawTask(ILogisticsComponent owningComponent, String displayName, ItemStack item, boolean useBuffers) {
		this(owningComponent, displayName, new ItemDeepStack(item, 1), useBuffers);
	}
	
	private static LogisticsItemWithdrawTask makeComposite(LogisticsItemWithdrawTask left, LogisticsItemWithdrawTask right) {
		LogisticsItemWithdrawTask composite;
		ItemDeepStack item = left.item.copy();
		item.add(right.item);
		if (left.entity == null) {
			composite = new LogisticsItemWithdrawTask(left.component, left.displayName, item, left.useBuffers);
		} else {
			composite = new LogisticsItemWithdrawTask(left.entity, left.displayName, item, left.useBuffers);
		}
		
		// pull registry stuff
		composite.fairy = left.fairy;
		composite.pickupComponent = left.pickupComponent;
		
		composite.mergedTasks = new LinkedList<>();
		composite.mergedTasks.add(left);
		composite.mergedTasks.add(right);
		
		composite.phase = Phase.RETRIEVING;
		composite.animCount = 0;
		composite.tryTasks(left.fairy);
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
		// Only have to clean up logistics item request placeholder, since the actual items are either
		// in the original inventory or in the fairy inventory
		if (requestRecord != null) {
			fairy.getLogisticsNetwork().removeRequestedItem(pickupComponent, requestRecord);
		}
	}

	@Override
	public void onAccept(IFairyWorker worker) {
		this.fairy = (IItemCarrierFairy) worker;
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
		if (other instanceof LogisticsItemWithdrawTask) {
			LogisticsItemWithdrawTask otherTask = (LogisticsItemWithdrawTask) other;
			
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
			
			return ItemStacks.stacksMatch(item.getTemplate(), otherTask.item.getTemplate());
		}
		
		return false;
	}
	
	private void mergeToComposite(LogisticsItemWithdrawTask otherTask) {
		this.item.add(otherTask.item.getCount());
		mergedTasks.add(otherTask);
		this.tryTasks(getCurrentWorker());
	}

	@Override
	public ILogisticsTask mergeIn(ILogisticsTask other) {
		// If already a composite, just add. Otherwise, make a composite!
		if (this.mergedTasks == null) {
			return makeComposite(this, (LogisticsItemWithdrawTask) other);
		} //else
		
		this.mergeToComposite((LogisticsItemWithdrawTask) other);
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
	
	public @Nullable IItemCarrierFairy getCurrentWorker() {
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
	
	private boolean tryTasks(IFairyWorker fairy) {
		releaseTasks();
		
		System.out.println("====================================");
		
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
							250.0, false);
					ItemDeepStack match = null;
					ILogisticsComponent comp = null;
					
					// Note: item map is sorted so that closer components are earlier in the list.
					// So just find the very first component and go to it.
					
					for (Entry<ILogisticsComponent, List<ItemDeepStack>> entry : items.entrySet()) {
						// Ignore buffer chests if configured so
						if (!this.useBuffers && entry.getKey().isItemBuffer()) {
							continue;
						}
						
						System.out.println(entry.getKey().getPosition() + ":");
						System.out.println("Requests:");
						Collection<RequestedItemRecord> records = network.getItemRequests(entry.getKey());
						if (records != null) {
							for (RequestedItemRecord record : records) {
								System.out.println("	- " + record.getItem().getTemplate().getDisplayName() + " x" + record.getItem().getCount());
							}
						}
						
						System.out.println("Available Items:");
						for (ItemDeepStack deep : entry.getValue()) {
							System.out.println("	- " + deep.getTemplate().getDisplayName() + " x" + deep.getCount());
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
					pickupComponent = ((LogisticsItemWithdrawTask) this.mergedTasks.get(0)).pickupComponent;
					retrieveTask = LogisticsSubTask.Move(pickupComponent.getPosition());
				}
			}
		}
		
		if (retrieveTask != null) {
			workTask = LogisticsSubTask.Break(retrieveTask.getPos());
			
			// make deliver task
			deliverTask = LogisticsSubTask.Move(component == null ? entity.getPosition() : component.getPosition());
		}
		
		System.out.println("====================================");
		
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
	
	private void syncChildPhases() {
		if (this.mergedTasks != null) {
			for (ILogisticsTask task : this.mergedTasks) {
				LogisticsItemWithdrawTask otherTask = (LogisticsItemWithdrawTask) task;
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
		case RETRIEVING:
			// Moved to pick up the item. Give it to them and then move on
			if (animCount < 3) { // TODO was 20
				animCount++;
			} else {
				takeItems();
				phase = Phase.DELIVERING;
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
				((LogisticsItemWithdrawTask) task).takeItems();
			}
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
			
				// TODO: Could add the concept of a 'future item' here too that deposit tasks register.
				// So places could go and be waiting at the storage location for something to be deposited.
				// That might make auto crafting easier, as well! And then this would need to make sure to pay attention to those
				// here in the valid check and not drop if there are future items... but 'TakeItems' or w/e that pulls from
				// the inventory would have to stall while it waited for the item. Maybe a 'waiting' phase that we sit in for a bit
				// and then try again.
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