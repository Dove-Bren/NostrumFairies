package com.smanzana.nostrumfairies.tiles;

import java.util.Iterator;
import java.util.List;

import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWithdrawItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;

public class StorageMonitorTileEntity extends LogisticsTileEntity implements ILogisticsTaskListener {

	private static final String NBT_REQUESTS = "requests";
	
	private LogisticsItemWithdrawRequester requester;
	private NonNullList<ItemStack> requests;

	public StorageMonitorTileEntity() {
		super(FairyTileEntities.StorageMonitorTileEntityType);
		requests = NonNullList.create();
	}
	
	@Override
	public double getDefaultLogisticsRange() {
		return 0;
	}

	@Override
	public double getDefaultLinkRange() {
		return 10;
	}

	@Override
	public boolean canAccept(List<ItemDeepStack> stacks) {
		return false;
	}
	
	@Override
	public void addItem(ItemStack stack) {
		if (!level.isClientSide) {
			ItemEntity ent = new ItemEntity(level, worldPosition.getX() + .5, worldPosition.getY() + .2, worldPosition.getZ() + .5, stack);
			level.addFreshEntity(ent);
		}
	}
	
	protected void makeRequester() {
		requester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent); // TODO make using buffer chests configurable!
		requester.addChainListener(this);
		requester.updateRequestedItems(getItemRequests());
	}
	
	@Override
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		super.setNetworkComponent(component);
		
		if (level != null && !level.isClientSide && requester == null) {
			makeRequester();
		}
	}
	
	@Override
	public void setLevelAndPosition(Level worldIn, BlockPos pos) {
		super.setLevelAndPosition(worldIn, pos);
		
		if (this.networkComponent != null && !worldIn.isClientSide && requester == null) {
			makeRequester();
		}
	}
	
	@Override
	public void onLeaveNetwork() {
		if (!level.isClientSide && requester != null) {
			requester.clearRequests();
			requester.setNetwork(null);
		}
		
		super.onLeaveNetwork();
	}
	
	@Override
	public void onJoinNetwork(LogisticsNetwork network) {
		if (!level.isClientSide && requester != null) {
			requester.setNetwork(network);
			requester.updateRequestedItems(getItemRequests());
		}
		
		super.onJoinNetwork(network);
	}
	
	public NonNullList<ItemStack> getItemRequests() {
		return requests;
	}

	@Override
	public void onTaskDrop(ILogisticsTask task, IFeyWorker worker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskAccept(ILogisticsTask task, IFeyWorker worker) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskComplete(ILogisticsTask task, IFeyWorker worker) {
		// Remove item from our request list
		ItemDeepStack fetched = ((LogisticsTaskWithdrawItem) task).getAttachedItem();
		if (fetched != null) {
			Iterator<ItemStack> it = requests.iterator();
			while (fetched.getCount() > 0 && it.hasNext()) {
				ItemStack cur = it.next();
				if (cur.isEmpty()) {
					continue;
				}
				
				if (fetched.canMerge(cur)) {
					if (cur.getCount() <= fetched.getCount()) {
						it.remove();
						fetched.add(-cur.getCount());
					} else {
						cur.shrink((int) fetched.getCount());
						fetched.setCount(0);
						break;
					}
				}
			}
		}

		BlockState state = level.getBlockState(worldPosition);
		level.sendBlockUpdated(worldPosition, state, state, 2);
		
		this.setChanged();
	}
	
	public void addRequest(ItemStack stack) {
		requests.add(stack);
		requester.updateRequestedItems(getItemRequests());

		BlockState state = level.getBlockState(worldPosition);
		level.sendBlockUpdated(worldPosition, state, state, 2);
		
		this.setChanged();
	}
	
	public void removeRequest(ItemStack stack) {
		Iterator<ItemStack> it = requests.iterator();
		while (it.hasNext()) {
			ItemStack cur = it.next();
			if (stack.getItem() == cur.getItem() && stack.getCount() == cur.getCount()) {
				it.remove();
				requester.updateRequestedItems(getItemRequests());

				BlockState state = level.getBlockState(worldPosition);
				level.sendBlockUpdated(worldPosition, state, state, 2);
				
				this.setChanged();
				break;
			}
		}
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		super.save(nbt);
		
		ListTag list = new ListTag();
		for (ItemStack stack : requests) {
			list.add(stack.serializeNBT());
		}
		nbt.put(NBT_REQUESTS, list);
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundTag nbt) {
		super.load(state, nbt);
		
		requests.clear();
		ListTag list = nbt.getList(NBT_REQUESTS, NBT.TAG_COMPOUND);
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				CompoundTag tag = list.getCompound(i);
				ItemStack stack = ItemStack.of(tag);
				if (!stack.isEmpty()) {
					requests.add(stack);
				}
			}
		}
		
		if (this.level != null && this.requester != null) {
			this.requester.updateRequestedItems(getItemRequests());
		}
	}
}