package com.smanzana.nostrumfairies.blocks.tiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.tiles.LogisticsLogicComponent.ILogicListener;
import com.smanzana.nostrumfairies.blocks.tiles.LogisticsTileEntity.LogisticsTileEntityComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class OutputChestTileEntity extends LogisticsChestTileEntity implements ILogisticsLogicProvider, ILogicListener {

	private static final int SLOTS = 3;
	private static final String NBT_TEMPLATES = "templates";
	private static final String NBT_TEMPLATE_INDEX = "index";
	private static final String NBT_TEMPLATE_ITEM = "item";
	private static final String NBT_LOGIC_COMP = "logic";
	
	private String displayName;
	private ItemStack[] templates;
	private LogisticsItemWithdrawRequester requester;
	private final LogisticsLogicComponent logicComp;
	
	public OutputChestTileEntity() {
		super();
		displayName = "Output Chest";
		templates = new ItemStack[SLOTS];
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
	
	public void setTemplate(int index, @Nullable ItemStack template) {
		if (index < 0 || index >=  SLOTS) {
			return;
		}
		
		ItemStack temp = template == null ? null : template.copy();
		templates[index] = temp;
		this.markDirty();
	}
	
	public @Nullable ItemStack getTemplate(int index) {
		if (index < 0 || index >=  SLOTS) {
			return null;
		}
		
		return templates[index];
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (!super.isItemValidForSlot(index, stack)) {
			return false;
		}
		
		ItemStack template = getTemplate(index);
		if (template != null) {
			return ItemStacks.stacksMatch(template, stack);
		}
		
		return true;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		// Save templates
		NBTTagList templates = new NBTTagList();
		for (int i = 0; i < SLOTS; i++) {
			ItemStack stack = this.getTemplate(i);
			if (stack == null) {
				continue;
			}
			
			NBTTagCompound template = new NBTTagCompound();
			
			template.setInteger(NBT_TEMPLATE_INDEX, i);
			template.setTag(NBT_TEMPLATE_ITEM, stack.writeToNBT(new NBTTagCompound()));
			
			templates.appendTag(template);
		}
		nbt.setTag(NBT_TEMPLATES, templates);
		nbt.setTag(NBT_LOGIC_COMP, this.logicComp.writeToNBT(new NBTTagCompound()));
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		templates = new ItemStack[SLOTS];
		
		// Reload templates
		NBTTagList list = nbt.getTagList(NBT_TEMPLATES, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound template = list.getCompoundTagAt(i);
			int index = template.getInteger(NBT_TEMPLATE_INDEX);
			
			if (index < 0 || index > SLOTS) {
				NostrumFairies.logger.error("Found serialized template with invalid index! " + index + " outside of " + SLOTS);
				continue;
			}
			
			ItemStack stack = ItemStack.loadItemStackFromNBT(template.getCompoundTag(NBT_TEMPLATE_ITEM));
			
			templates[index] = stack;
		}
		
		NBTTagCompound tag = nbt.getCompoundTag(NBT_LOGIC_COMP);
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
	public void setWorldObj(World worldIn) {
		super.setWorldObj(worldIn);
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
	
	private List<ItemStack> getItemRequests() {
		// Globally return 0 requests if logic says we shouldn't run
		if (!logicComp.isActivated()) {
			return new ArrayList<>(0);
		}
		
		List<ItemStack> requests = new LinkedList<>();
		
		for (int i = 0; i < templates.length; i++) {
			if (templates[i] == null) {
				continue;
			}
			
			ItemStack inSlot = this.getStackInSlot(i);
			int desire = templates[i].stackSize - (inSlot == null ? 0 : inSlot.stackSize);
			if (desire > 0) {
				ItemStack req = templates[i].copy();
				req.stackSize = desire;
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
		for (int i = 0; i < templates.length; i++) {
			if (templates[i] == null) {
				continue;
			}
			
			if (!isItemValidForSlot(i, stack)) {
				// doesn't fit here anyways
				continue;
			}
			
			// if template count != stack count, try to add there
			ItemStack inSlot = this.getStackInSlot(i);
			int desire = templates[i].stackSize - (inSlot == null ? 0 : inSlot.stackSize);
			int amt = Math.min(stack.stackSize, desire);
			if (inSlot == null) {
				// take out template desire amount
				this.setInventorySlotContentsDirty(i, stack.splitStack(amt)); // doesn't set dirty
				anyChanges = true;
			} else {
				stack.stackSize -= amt;
				inSlot.stackSize += amt;
				anyChanges = true;
			}
			
			if (stack.stackSize <= 0) {
				break;
			}
		}
		
		if (anyChanges) {
			this.markDirty();
		}
		
		// Any leftover?
		if (stack != null && stack.stackSize > 0) {
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