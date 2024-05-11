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

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
		if (!world.isRemote) {
			ItemEntity ent = new ItemEntity(world, pos.getX() + .5, pos.getY() + .2, pos.getZ() + .5, stack);
			world.addEntity(ent);
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
		
		if (world != null && !world.isRemote && requester == null) {
			makeRequester();
		}
	}
	
	@Override
	public void setWorldAndPos(World worldIn, BlockPos pos) {
		super.setWorldAndPos(worldIn, pos);
		
		if (this.networkComponent != null && !worldIn.isRemote && requester == null) {
			makeRequester();
		}
	}
	
	@Override
	public void onLeaveNetwork() {
		if (!world.isRemote && requester != null) {
			requester.clearRequests();
			requester.setNetwork(null);
		}
		
		super.onLeaveNetwork();
	}
	
	@Override
	public void onJoinNetwork(LogisticsNetwork network) {
		if (!world.isRemote && requester != null) {
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

		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 2);
		
		this.markDirty();
	}
	
	public void addRequest(ItemStack stack) {
		requests.add(stack);
		requester.updateRequestedItems(getItemRequests());

		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 2);
		
		this.markDirty();
	}
	
	public void removeRequest(ItemStack stack) {
		Iterator<ItemStack> it = requests.iterator();
		while (it.hasNext()) {
			ItemStack cur = it.next();
			if (stack.getItem() == cur.getItem() && stack.getCount() == cur.getCount()) {
				it.remove();
				requester.updateRequestedItems(getItemRequests());

				BlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 2);
				
				this.markDirty();
				break;
			}
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		
		ListNBT list = new ListNBT();
		for (ItemStack stack : requests) {
			list.add(stack.serializeNBT());
		}
		nbt.put(NBT_REQUESTS, list);
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		
		requests.clear();
		ListNBT list = nbt.getList(NBT_REQUESTS, NBT.TAG_COMPOUND);
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				CompoundNBT tag = list.getCompound(i);
				ItemStack stack = ItemStack.read(tag);
				if (!stack.isEmpty()) {
					requests.add(stack);
				}
			}
		}
		
		if (this.world != null && this.requester != null) {
			this.requester.updateRequestedItems(getItemRequests());
		}
	}
}