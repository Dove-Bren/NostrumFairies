package com.smanzana.nostrumfairies.tiles;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BufferChestTileEntity extends LogisticsChestTileEntity {

	private static final int SLOTS = 9;
	private static final String NBT_TEMPLATES = "templates";
	private static final String NBT_TEMPLATE_INDEX = "index";
	private static final String NBT_TEMPLATE_ITEM = "item";
	
	private NonNullList<ItemStack> templates;
	private LogisticsItemWithdrawRequester requester;
	
	public BufferChestTileEntity(BlockPos pos, BlockState state) {
		super(FairyTileEntities.BufferChestTileEntityType, pos, state);
		templates = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
	}
	
	@Override
	public int getContainerSize() {
		return SLOTS;
	}
	
	@Override
	public double getDefaultLinkRange() {
		return 10;
	}

	@Override
	public double getDefaultLogisticsRange() {
		return 10;
	}
	
	@Override
	public boolean canAccept(List<ItemDeepStack> stacks) {
		return false; // buffer chests aren't for random storage
	}
	
	public void setTemplate(int index, @Nonnull ItemStack template) {
		if (index < 0 || index >=  SLOTS) {
			return;
		}
		
		ItemStack temp = template.isEmpty() ? ItemStack.EMPTY : template.copy();
		templates.set(index, temp);
		this.setChanged();
	}
	
	public @Nonnull ItemStack getTemplate(int index) {
		if (index < 0 || index >=  SLOTS) {
			return ItemStack.EMPTY;
		}
		
		return templates.get(index);
	}
	
	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		if (!super.canPlaceItem(index, stack)) {
			return false;
		}
		
		ItemStack template = getTemplate(index);
		if (!template.isEmpty()) {
			return ItemStacks.stacksMatch(template, stack);
		}
		
		return true;
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		
		// Save templates
		ListTag templates = new ListTag();
		for (int i = 0; i < SLOTS; i++) {
			ItemStack stack = this.getTemplate(i);
			if (stack.isEmpty()) {
				continue;
			}
			
			CompoundTag template = new CompoundTag();
			
			template.putInt(NBT_TEMPLATE_INDEX, i);
			template.put(NBT_TEMPLATE_ITEM, stack.save(new CompoundTag()));
			
			templates.add(template);
		}
		nbt.put(NBT_TEMPLATES, templates);
		
		return nbt;
	}
	
	@Override
	public void load(CompoundTag nbt) {
		templates = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		
		// Reload templates
		ListTag list = nbt.getList(NBT_TEMPLATES, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag template = list.getCompound(i);
			int index = template.getInt(NBT_TEMPLATE_INDEX);
			
			if (index < 0 || index > SLOTS) {
				NostrumFairies.logger.error("Found serialized template with invalid index! " + index + " outside of " + SLOTS);
				continue;
			}
			
			ItemStack stack = ItemStack.of(template.getCompound(NBT_TEMPLATE_ITEM));
			
			templates.set(index, stack);
		}
		
		// Do super afterwards so taht we ahve templates already
		super.load(nbt);
	}
	
	@Override
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		super.setNetworkComponent(component);
		
		if (level != null && !level.isClientSide && requester == null) {
			requester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), false, this.networkComponent);
			requester.updateRequestedItems(getItemRequests());
		}
	}
	
	@Override
	public void setLevel(Level worldIn) {
		super.setLevel(worldIn);
		
		if (this.networkComponent != null && !worldIn.isClientSide && requester == null) {
			requester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), false, this.networkComponent);
			requester.updateRequestedItems(getItemRequests());
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
	
	private List<ItemStack> getItemRequests() {
		List<ItemStack> requests = new LinkedList<>();
		
		for (int i = 0; i < templates.size(); i++) {
			if (templates.get(i).isEmpty()) {
				continue;
			}
			
			ItemStack inSlot = this.getItem(i);
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
			
			if (!canPlaceItem(i, stack)) {
				// doesn't fit here anyways
				continue;
			}
			
			// if template count != stack count, try to add there
			ItemStack inSlot = this.getItem(i);
			int desire = templates.get(i).getCount() - (inSlot.isEmpty() ? 0 : inSlot.getCount());
			int amt = Math.min(stack.getCount(), desire);
			if (inSlot.isEmpty()) {
				// take out template desire amount
				this.setInventorySlotContentsDirty(i, stack.split(amt)); // doesn't set dirty
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
			this.setChanged();
		}
		
		// Any leftover?
		if (!stack.isEmpty()) {
			super.addItem(stack);
		}
	}
	
	@Override
	public void setChanged() {
		super.setChanged();
		if (level != null && !level.isClientSide && requester != null) {
			requester.updateRequestedItems(getItemRequests());
		}
	}
	
	@Override
	public boolean isItemBuffer() {
		return true;
	}
}