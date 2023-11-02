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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class StorageMonitorTileEntity extends LogisticsTileEntity implements ILogisticsTaskListener {

	private static final String NBT_REQUESTS = "requests";
	
	private LogisticsItemWithdrawRequester requester;
	private NonNullList<ItemStack> requests;

	public StorageMonitorTileEntity() {
		super();
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
			EntityItem ent = new EntityItem(world, pos.getX() + .5, pos.getY() + .2, pos.getZ() + .5, stack);
			world.spawnEntity(ent);
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
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		
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

		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 2);
		
		this.markDirty();
	}
	
	public void addRequest(ItemStack stack) {
		requests.add(stack);
		requester.updateRequestedItems(getItemRequests());

		IBlockState state = world.getBlockState(pos);
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

				IBlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 2);
				
				this.markDirty();
				break;
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		NBTTagList list = new NBTTagList();
		for (ItemStack stack : requests) {
			list.appendTag(stack.serializeNBT());
		}
		nbt.setTag(NBT_REQUESTS, list);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		requests.clear();
		NBTTagList list = nbt.getTagList(NBT_REQUESTS, NBT.TAG_COMPOUND);
		if (list != null && list.tagCount() > 0) {
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				ItemStack stack = new ItemStack(tag);
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