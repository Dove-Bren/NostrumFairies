package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

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

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/*
 * Travel to a location and place a block there
 */
public class LogisticsTaskPlaceBlock implements ILogisticsTask {
	
	protected static enum Phase {
		IDLE,
		PICKUP,
		WAITING,
		MOVING,
		PLACING,
		DONE,
	}
	
	private String displayName;
	private World world;
	private BlockPos block;
	private BlockPos placeAt;
	private @Nonnull ItemStack item = ItemStack.EMPTY; // Item to pickup
	private IBlockState state; // blockstate to put down
	private @Nullable ILogisticsComponent component;
	private @Nullable EntityLivingBase entity;
	
	private IItemCarrierFey fairy;
	protected Phase phase;
	private LogisticsSubTask pickupTask;
	private LogisticsSubTask moveTask;
	private LogisticsSubTask workTask;
	private LogisticsSubTask idleTask;
	
	private long lastPlaceCheck;
	private boolean lastPlaceResult;
	private @Nullable ILogisticsComponent pickupComponent;
	private @Nullable RequestedItemRecord requestRecord;
	private @Nullable UUID networkCacheKey;
	private boolean networkCacheResult;
	private @Nullable EntityLivingBase pickupEntity;
	
	protected int animCount = 0;
	
	protected LogisticsTaskPlaceBlock(@Nullable ILogisticsComponent owningComponent, @Nullable EntityLivingBase entity,
			String displayName, @Nonnull ItemStack item, IBlockState state, World world, BlockPos pos, BlockPos placeAt) {
		Validate.notNull(item);
		this.displayName = displayName;
		this.block = pos;
		this.placeAt = placeAt;
		this.world = world;
		this.component = owningComponent;
		this.entity = entity;
		this.item = item;
		this.state = state;
		phase = Phase.IDLE;
	}

	public LogisticsTaskPlaceBlock(ILogisticsComponent owningComponent, String displayName,
			ItemStack item, IBlockState state,World world, BlockPos pos) {
		this(owningComponent, displayName, item, state, world, pos, pos);
	}
	
	public LogisticsTaskPlaceBlock(ILogisticsComponent owningComponent, String displayName,
			ItemStack item, IBlockState state, World world, BlockPos pos, BlockPos placeAt) {
		this(owningComponent, null, displayName, item, state, world, pos, placeAt);
	}
	
	public LogisticsTaskPlaceBlock(EntityLivingBase entity, String displayName,
			ItemStack item, IBlockState state, World world, BlockPos pos) {
		this(entity, displayName, item, state, world, pos, pos);
	}
	
	public LogisticsTaskPlaceBlock(EntityLivingBase entity, String displayName,
			ItemStack item, IBlockState state, World world, BlockPos pos, BlockPos placeAt) {
		this(null, entity, displayName, item, state, world, pos, placeAt);
	}
	
	
	@Override
	public String getDisplayName() {
		return displayName + " (" + block + " - " + phase.name() + ")";
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
		
		// Also check here if the area around the block is a) loaded and b) exposed
		if (!world.isAreaLoaded(block, 1)) {
			return false;
		}
		
		// We leave this up to the entity, which can do it better
//		BlockPos pos = placeAt;
//		if (world.isAirBlock(pos.north())) {
//			pos = pos.north();
//		} else if (world.isAirBlock(pos.south())) {
//			pos = pos.south();
//		} else if (world.isAirBlock(pos.east())) {
//			pos = pos.east();
//		} else if (world.isAirBlock(pos.west())) {
//			pos = pos.west();
//		} else if (world.isAirBlock(pos.up())) {
//			pos = pos.up();
//		} else {
//			pos = pos.down();
//		}
//		
//		if (!world.isAirBlock(pos)) {
//			return false;
//		}
		
		// Try to create tasks including retrieving the item
		if (this.networkCacheKey == null || !this.networkCacheKey.equals(network.getCacheKey())) {
			networkCacheResult = tryTasks(worker);
			this.networkCacheKey = network.getCacheKey();
		}
		
		if (!this.networkCacheResult) {
			return false;
		}
		
		if (!isValid()) {
			return false;
		}
		
		 return true;
	}

	@Override
	public void onDrop(IFeyWorker worker) {
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
		phase = Phase.PICKUP;
		tryTasks(worker);
		
		if (this.pickupTask != null && this.pickupComponent != null) {
			requestRecord = fairy.getLogisticsNetwork().addRequestedItem(pickupComponent, this, new ItemDeepStack(item));
		}
		
		lastPlaceCheck = 0; //reset so 'isValid' runs fully the first time
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
		return component;
	}
	
	public @Nullable EntityLivingBase getSourceEntity() {
		return entity;
	}

	@Override
	public Collection<ILogisticsTask> unmerge() {
		return Lists.newArrayList(this);
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public BlockPos getTargetBlock() {
		return block;
	}
	
	public BlockPos getTargetPlaceLoc() {
		return placeAt;
	}
	
	public boolean isActive() {
		return fairy != null;
	}
	
	public @Nullable IItemCarrierFey getCurrentWorker() {
		return fairy;
	}
	
	private void releaseTasks() {
		workTask = null;
		moveTask = null;
		pickupTask = null;
		this.requestRecord = null;
	}
	
	private boolean tryTasks(IFeyWorker fairy) {
		releaseTasks();
		
		pickupTask = null;
		{
			// Find an item to go and pick up
			
			LogisticsNetwork network = fairy.getLogisticsNetwork();
			if (network != null) {
				// Entity tasks can pull from the entity's inventory
				if (entity != null) {
					if (entity instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer) entity;
						if (player.inventory.hasItemStack(item)) {
							pickupTask = LogisticsSubTask.Move(player);
						}
					}
					// else instanceof ICoolInventoryInterface
					// could do 'item carrying fey' but I'm worried there's no protection
					// from grabbing whatever they want out of worker's work inventory
				}
				
				// If we didn't figure it out already, pull from network
				if (pickupTask == null) {
					Map<ILogisticsComponent, List<ItemDeepStack>> items = network.getNetworkItems(component == null ? entity.world : component.getWorld(),
							component == null ? entity.getPosition() : component.getPosition(),
							250.0, ItemCacheType.NET);
					ItemDeepStack match = null;
					ILogisticsComponent comp = null;
					
					// Note: item map is sorted so that closer components are earlier in the list.
					// So just find the very first component and go to it.
					
					for (Entry<ILogisticsComponent, List<ItemDeepStack>> entry : items.entrySet()) {
						for (ItemDeepStack deep : entry.getValue()) {
							if (deep.canMerge(this.item)) {
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
						pickupTask = LogisticsSubTask.Move(comp.getPosition());
						pickupComponent = comp;
					}
				}
				
			}
		}
		
		if (this.pickupTask != null) {
			moveTask = LogisticsSubTask.Move(placeAt);
			workTask = LogisticsSubTask.Break(placeAt);
			idleTask = LogisticsSubTask.Idle(pickupComponent == null ? pickupEntity.getPosition() : pickupComponent.getPosition());
		}
		
		return true;
	}
	
	@Override
	public LogisticsSubTask getActiveSubtask() {
		switch (phase) {
		case IDLE:
			return null;
		case WAITING:
			return idleTask;
		case PICKUP:
			return pickupTask;
		case MOVING:
			return moveTask;
		case PLACING:
			return workTask;
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
		case WAITING:
			// pick random pos, go to it, and then wait a random amount of time.
			// we use animCount to count DOWN
			if (animCount > 0) {
				animCount--;
			} else {
				// Spent some time waiting. Go ahead and try and retrieve again
				phase = Phase.PICKUP;
				animCount = -1;
			}
			break;
		case PICKUP:
			// Moved to pick up the item. Give it to them and then move on
			if (canTakeItems()) {
				takeItems();
				phase = Phase.MOVING;
				animCount = -1;
			} else {
				// Items aren't there yet
				idleTask = LogisticsSubTask.Idle(pickupComponent.getPosition());
				animCount = 20 * (NostrumFairies.random.nextInt(3) + 2);
				phase = Phase.WAITING;
			}
			
			break;
		case MOVING:
			phase = Phase.PLACING;
			break;
		case PLACING:
			placeBlock();
			phase = Phase.DONE;
			break;
		case DONE:
			break;
		}
	}
	
	@Override
	public boolean isComplete() {
		return phase == Phase.DONE;
	}
	
	protected void placeBlock() {
		world.setBlockState(block, state);
		SoundType soundtype = world.getBlockState(block).getBlock().getSoundType(world.getBlockState(block), world, block, null);
		world.playSound(null, block, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
		
		fairy.removeItem(item);
	}
	
	protected boolean canTakeItems() {
		Collection<ItemStack> available = pickupComponent.getItems();
		
		return ItemDeepStacks.isSubset(ItemDeepStack.toDeepList(available), Lists.newArrayList(new ItemDeepStack(this.item)));
	}
	
	protected void takeItems() {
		pickupComponent.takeItem(item.copy());
		fairy.addItem(item.copy());
		
		if (requestRecord != null) {
			fairy.getLogisticsNetwork().removeRequestedItem(pickupComponent, requestRecord);
		}
	}
	
	public boolean placedBlock() {
		return this.phase == Phase.DONE;
	}
	
	protected boolean isSpotValid(World world, BlockPos pos) {
		if (world.isAirBlock(pos)
				|| world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
			return (state.getBlock().canPlaceBlockAt(world, pos));
		}
		
		return false;
	}

	@Override
	public boolean isValid() {
		if (this.pickupTask == null) {
			return false;
		}
		
		// make sure the item we want is still there
		if (this.phase == Phase.PICKUP) {
			LogisticsNetwork network = fairy.getLogisticsNetwork();
			if (network == null) {
				return false;
			}
			
			if (this.networkCacheKey == null || !this.networkCacheKey.equals(network.getCacheKey())) {
				this.networkCacheKey = network.getCacheKey();
			
				List<ItemDeepStack> items = fairy.getLogisticsNetwork().getNetworkItems(ItemCacheType.GROSS).get(pickupComponent);
				if (items == null) {
					networkCacheResult = false;
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
					
					networkCacheResult = count <= 0;
				}
			}
			
			if (!networkCacheResult) {
				return false;
			}
		}
		
		// Make sure the block still needs placing
		if (this.phase != Phase.DONE) {
			if (lastPlaceCheck == 0 || this.world.getTotalWorldTime() - lastPlaceCheck > 60) {
				lastPlaceResult = isSpotValid(world, block);
				lastPlaceCheck = world.getTotalWorldTime();
			}
			
			if (!lastPlaceResult) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public BlockPos getStartPosition() {
		return null; // Pickup location determined after task is picked up, so a hint location doesn't work.
	}
}
