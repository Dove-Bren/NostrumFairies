package com.smanzana.nostrumfairies.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.util.Inventories;
import com.smanzana.nostrummagica.util.Inventories.ItemStackArrayWrapper;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ItemDeepStacks {
	
	public static final @Nullable List<ItemDeepStack> addAllItems(IInventory inventory, Collection<ItemDeepStack> stacks) {
		// Just go through each and try to add
		List<ItemDeepStack> ret = null;
		
		for (ItemDeepStack deep : stacks) {
			deep = deep.copy();
			while (deep.getCount() > 0) {
				@Nonnull ItemStack stack = deep.splitStack(deep.getTemplate().getMaxStackSize());
				@Nonnull ItemStack leftover = Inventories.addItem(inventory, stack);
				if (!leftover.isEmpty()) {
					// Inventory can't fit anymore of this item!
					// Add back however many we couldn't and push this deep into our ret list
					deep.add(leftover);
					break;
				}
			}
			
			if (deep.getCount() > 0) {
				if (ret == null) {
					ret = new LinkedList<>();
				}
				
				ret.add(deep);
			}
		}
		
		return ret;
	}
	
	public static final @Nullable List<ItemDeepStack> addAllItems(ItemStack[] inventory, Collection<ItemDeepStack> stacks) {
		return addAllItems(new ItemStackArrayWrapper(inventory), stacks);
	}
	
	public static final boolean canFitAll(IInventory inventory, Collection<ItemDeepStack> stacks) {
		// Instead of just trying to add all (with commit = false, which would mean no state saved) we need
		// to try and quantify how much FREE SPACE there is instead.
		
		// Going to try keeping track of empty slots, but otherwise do the regular n * m double loop.
		
		// I'm wondering if it's worth creating a list of deep stacks for the inventory contents.
		// That way n and m would be smaller...
		
		// If I inverted the loop (and looped once first to find empty slots) I could make copies of the deepstacks inside the loop.
		// But oh well.
		List<ItemDeepStack> dupeList = new ArrayList<>(stacks.size());
		for (ItemDeepStack deep : stacks) {
			dupeList.add(deep.copy());
		}
		
		// Condense
		dupeList = ItemDeepStack.toCondensedDeepList(dupeList);
		stacks = dupeList;
		
		int freeSlots = 0;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			@Nonnull ItemStack inSlot = inventory.getStackInSlot(i);
			if (inSlot.isEmpty()) {
				freeSlots++;
			} else if (!stacks.isEmpty()) {
				// Find any of our stacks that match the item in this slot
				int room = Math.min(inventory.getInventoryStackLimit(), inSlot.getMaxStackSize()) - inSlot.getCount();
				
				if (room == 0) {
					continue;
				}
				
				Iterator<ItemDeepStack> iter = stacks.iterator();
				while (iter.hasNext()) {
					ItemDeepStack deep = iter.next();
					if (deep.canMerge(inSlot)) {
						if (room >= deep.getCount()) {
							iter.remove();
							room -= deep.getCount();
						} else {
							deep.add(-room);
							room = 0;
						}
						
						// Since we condensed our deepstack list, no other deepstack will match
						break;
					}
				}
			}
		}
		
		// Spend any free slots we came across to get rid of more of the items
		if (!stacks.isEmpty() && freeSlots > 0) {
			Iterator<ItemDeepStack> iter = stacks.iterator();
			ItemDeepStack deep = iter.next();
			for (; freeSlots > 0 && deep != null; freeSlots--) {
				
				// subtract however many would be able to fit into this slot
				int stack = Math.min(inventory.getInventoryStackLimit(), deep.getTemplate().getMaxStackSize());
				deep.splitStack(stack);
				
				if (deep.getCount() <= 0) {
					// exhausted deep. Get a new one.
					iter.remove();
					if (iter.hasNext()) {
						deep = iter.next();
					} else {
						deep = null;
					}
				}
			}
		}
		
		return stacks.isEmpty();
	}
	
	public static final boolean canFitAll(ItemStack[] inventory, Collection<ItemDeepStack> stacks) {
		return canFitAll(new ItemStackArrayWrapper(inventory), stacks);
	}
	
	/**
	 * Checks whether all items in the subset list are available in the largelist.
	 * <b>Big note</b>: This method assumes the lists are condensed lists.
	 * @param largeList
	 * @param subset
	 * @return
	 */
	public static final boolean isSubset(Collection<ItemDeepStack> largeList, Collection<ItemDeepStack> subset) {
		for (ItemDeepStack deep : subset) {
			boolean found = false;
			
			for (ItemDeepStack largeDeep : largeList) {
				if (largeDeep.canMerge(deep)) {
					if (largeDeep.getCount() < deep.getCount()) {
						return false;
					}
					found = true;
					break;
				}
			}
			
			if (!found) {
				return false;
			}
		}
		
		return true;
	}

	public static final void addAll(List<ItemDeepStack> to, List<ItemDeepStack> from) {
		for (ItemDeepStack stack : from) {
			boolean merged = false;
			for (ItemDeepStack condensed : to) {
				if (condensed.canMerge(stack)) {
					condensed.add(stack);
					merged = true;
					break;
				}
			}
			
			if (!merged) {
				to.add(stack);
			}
		}
	}
}
