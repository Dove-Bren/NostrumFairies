package com.smanzana.nostrumfairies.tiles;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.ILogicListener;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class OutputChestTileEntity extends LogisticsChestTileEntity implements ILogisticsLogicProvider, ILogicListener {

	private static final int SLOTS = 3;
	private static final String NBT_TEMPLATES = "templates";
	private static final String NBT_TEMPLATE_INDEX = "index";
	private static final String NBT_TEMPLATE_ITEM = "item";
	private static final String NBT_LOGIC_COMP = "logic";
	
	private String displayName;
	private NonNullList<ItemStack> templates;
	private LogisticsItemWithdrawRequester requester;
	private final LogisticsLogicComponent logicComp;
	
	public OutputChestTileEntity() {
		super();
		displayName = "Output Chest";
		templates = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		logicComp = new LogisticsLogicComponent(false, this);
	}
	
	@Override
	public void onStateChange(boolean activated) {
		; // We handle this in a tick loop, which adds lag between redstone but also won't change blockstates
		// multiples times if item count jumps back and forth across a boundary in a single tick
	}

	@Override
	public void onDirty() {
		this.markDirty();
	}
	
	@Override
	public LogisticsLogicComponent getLogicComponent() {
		return this.logicComp;
	}
	
	@Override
	public String getName() {
		return displayName;
	}

	@Override
	public boolean hasCustomName() {
		return false;
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
		return false;
	}

	@Override
	public Collection<ItemStack> getItems() {
		// Output chests don't offer their items to the network
		return LogisticsTileEntity.emptyList;
	}
	
	public void setTemplate(int index, @Nonnull ItemStack template) {
		Validate.notNull(template);
		if (index < 0 || index >=  SLOTS) {
			return;
		}
		
		ItemStack temp = template.isEmpty() ? ItemStack.EMPTY : template.copy();
		templates.set(index, temp);
		this.markDirty();
	}
	
	public @Nonnull ItemStack getTemplate(int index) {
		if (index < 0 || index >=  SLOTS) {
			return ItemStack.EMPTY;
		}
		
		return templates.get(index);
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (!super.isItemValidForSlot(index, stack)) {
			return false;
		}
		
		ItemStack template = getTemplate(index);
		if (!template.isEmpty()) {
			return ItemStacks.stacksMatch(template, stack);
		}
		
		return true;
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		// Save templates
		ListNBT templates = new ListNBT();
		for (int i = 0; i < SLOTS; i++) {
			ItemStack stack = this.getTemplate(i);
			if (stack.isEmpty()) {
				continue;
			}
			
			CompoundNBT template = new CompoundNBT();
			
			template.putInt(NBT_TEMPLATE_INDEX, i);
			template.put(NBT_TEMPLATE_ITEM, stack.writeToNBT(new CompoundNBT()));
			
			templates.add(template);
		}
		nbt.put(NBT_TEMPLATES, templates);
		nbt.put(NBT_LOGIC_COMP, this.logicComp.writeToNBT(new CompoundNBT()));
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt) {
		templates = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		
		// Reload templates
		ListNBT list = nbt.getList(NBT_TEMPLATES, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT template = list.getCompound(i);
			int index = template.getInt(NBT_TEMPLATE_INDEX);
			
			if (index < 0 || index > SLOTS) {
				NostrumFairies.logger.error("Found serialized template with invalid index! " + index + " outside of " + SLOTS);
				continue;
			}
			
			ItemStack stack = new ItemStack(template.getCompoundTag(NBT_TEMPLATE_ITEM));
			
			templates.set(index, stack);
		}
		
		CompoundNBT tag = nbt.getCompoundTag(NBT_LOGIC_COMP);
		if (tag != null) {
			this.logicComp.readFromNBT(tag);
		}
		
		// Do super afterwards so taht we have templates already
		super.readFromNBT(nbt);
	}
	
	@Override
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		super.setNetworkComponent(component);
		logicComp.setNetwork(component.getNetwork());
		
		if (world != null && !world.isRemote && requester == null) {
			requester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent); // TODO make using buffer chests configurable!
			requester.updateRequestedItems(getItemRequests());
		}
	}
	
	@Override
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		logicComp.setLocation(worldIn, pos);
		
		if (this.networkComponent != null && !worldIn.isRemote && requester == null) {
			requester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent);
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
		logicComp.setNetwork(null);
	}
	
	@Override
	public void onJoinNetwork(LogisticsNetwork network) {
		if (!world.isRemote && requester != null) {
			requester.setNetwork(network);
			requester.updateRequestedItems(getItemRequests());
		}
		
		super.onJoinNetwork(network);
		logicComp.setNetwork(network);
	}
	
	private NonNullList<ItemStack> getItemRequests() {
		// Globally return 0 requests if logic says we shouldn't run
		if (!logicComp.isActivated()) {
			return NonNullList.create();
		}
		
		NonNullList<ItemStack> requests = NonNullList.create();
		
		for (int i = 0; i < templates.size(); i++) {
			if (templates.get(i).isEmpty()) {
				continue;
			}
			
			ItemStack inSlot = this.getStackInSlot(i);
			int desire = templates.get(i).getCount() - (inSlot.isEmpty() ? 0 : inSlot.getCount());
			if (desire > 0) {
				ItemStack req = templates.get(i).copy();
				req.setCount(desire);
				requests.add(req);
			}
		}
		
		return requests;
	}
	
	@Override
	public void takeItem(ItemStack stack) {
		// If there's an option, take from slots that don't have templates first
		super.takeItem(stack);
		// TODO
	}
	
	@Override
	public void addItem(ItemStack stack) {
		// If there's a choice, add to slots that have unfufilled templates first
		boolean anyChanges = false;
		for (int i = 0; i < templates.size(); i++) {
			if (templates.get(i).isEmpty()) {
				continue;
			}
			
			if (!isItemValidForSlot(i, stack)) {
				// doesn't fit here anyways
				continue;
			}
			
			// if template count != stack count, try to add there
			ItemStack inSlot = this.getStackInSlot(i);
			int desire = templates.get(i).getCount() - (inSlot.isEmpty() ? 0 : inSlot.getCount());
			int amt = Math.min(stack.getCount(), desire);
			if (inSlot.isEmpty()) {
				// take out template desire amount
				this.setInventorySlotContentsDirty(i, stack.splitStack(amt)); // doesn't set dirty
				anyChanges = true;
			} else {
				stack.shrink(amt);
				inSlot.grow(amt);
				anyChanges = true;
			}
			
			if (stack.isEmpty()) {
				break;
			}
		}
		
		if (anyChanges) {
			this.markDirty();
		}
		
		// Any leftover?
		if (!stack.isEmpty()) {
			super.addItem(stack);
		}
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		if (world != null && !world.isRemote && requester != null) {
			requester.updateRequestedItems(getItemRequests());
		}
	}
	
	public void notifyNeighborChanged() {
		logicComp.onWorldUpdate();
	}
}