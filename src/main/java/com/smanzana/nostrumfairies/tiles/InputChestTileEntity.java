package com.smanzana.nostrumfairies.tiles;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemDepositRequester;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemDeepStacks;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class InputChestTileEntity extends LogisticsChestTileEntity {

	private static final int SLOTS = 27;
	
	private LogisticsItemDepositRequester requester;
	
	public InputChestTileEntity() {
		super(FairyTileEntities.InputChestTileEntityType);
	}
	
	@Override
	public int getSizeInventory() {
		return SLOTS;
	}
	
	@Override
	public double getDefaultLogisticsRange() {
		return 10;
	}

	@Override
	public double getDefaultLinkRange() {
		return 10;
	}
	
	@Override
	public boolean canAccept(List<ItemDeepStack> stacks) {
		return ItemDeepStacks.canFitAll(this, stacks);
	}

	@Override
	public Collection<ItemStack> getItems() {
		// Input chests don't offer their items to the network
		return LogisticsTileEntity.emptyList;
	}
	
	@Override
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		super.setNetworkComponent(component);
		
		if (world != null && !world.isRemote && requester == null) {
			requester = new LogisticsItemDepositRequester(this.networkComponent.getNetwork(), this.networkComponent); // TODO make using buffer chests configurable!
			requester.updateRequestedItems(getItemRequests());
		}
	}
	
	@Override
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		
		if (this.networkComponent != null && !worldIn.isRemote && requester == null) {
			requester = new LogisticsItemDepositRequester(this.networkComponent.getNetwork(), this.networkComponent);
			requester.updateRequestedItems(getItemRequests());
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
	
	private List<ItemStack> getItemRequests() {
		List<ItemStack> requests = new LinkedList<>();
		
		for (int i = 0; i < SLOTS; i++) {
			requests.add(this.getStackInSlot(i));
		}
		
		return requests;
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		if (world != null && !world.isRemote && requester != null) {
			requester.updateRequestedItems(getItemRequests());
		}
	}
}