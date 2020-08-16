package com.smanzana.nostrumfairies.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;

public class ItemStacks {
	
	public static final boolean stacksMatch(@Nullable ItemStack stack1, @Nullable ItemStack stack2) {
		if (stack1 == null || stack2 == null) {
			return stack1 == null && stack2 == null;
		}
		
		return stack1.getItem() == stack2.getItem()
        		&& stack1.getMetadata() == stack2.getMetadata()
        		&& Objects.equals(stack1.getTagCompound(), stack2.getTagCompound());
	}
	
	private static final ItemStack attemptAddToInventory(IInventory inventory, @Nullable ItemStack stack, boolean commit) {
    	if (stack == null) {
    		return null;
    	}
    	
    	ItemStack itemstack = stack.copy();

    	for (int i = 0; i < inventory.getSizeInventory(); ++i) {
    		if (!inventory.isItemValidForSlot(i, itemstack)) {
    			continue;
    		}
    		
            ItemStack itemstack1 = inventory.getStackInSlot(i);

            if (itemstack1 == null) {
            	if (commit) {
	                inventory.setInventorySlotContents(i, itemstack);
	                inventory.markDirty();
            	}
                return null;
            }
            
            if (stacksMatch(itemstack, itemstack1)) {
            	// stacks appear to match. Deduct stack size
            	int room = itemstack1.getMaxStackSize() - itemstack1.stackSize;
            	if (room >= itemstack.stackSize) {
            		if (commit) {
	            		itemstack1.stackSize += itemstack.stackSize;
	            		inventory.markDirty();
            		}
            		return null;
            	} else if (room > 0) {
            		if (commit) {
	            		itemstack1.stackSize += room;
	            		inventory.markDirty();
            		}
            		itemstack.stackSize -= room;
            	}
            }
        }

        return itemstack;
    }
	 
	public static final ItemStack addItem(IInventory inventory, @Nullable ItemStack stack) {
		return attemptAddToInventory(inventory, stack, true);
	}
	
	public static final ItemStack addItem(ItemStack[] inventory, @Nullable ItemStack stack) {
		return addItem(new ItemStackArrayWrapper(inventory), stack);
	}
	
	public static final boolean canFit(IInventory inventory, @Nullable ItemStack stack) {
		return null == attemptAddToInventory(inventory, stack, false);
	}
	
	public static final boolean canFit(ItemStack[] inventory, @Nullable ItemStack stack) {
		return canFit(new ItemStackArrayWrapper(inventory), stack);
	}
	
	public static final @Nullable List<ItemDeepStack> addAllItems(IInventory inventory, Collection<ItemDeepStack> stacks) {
		// Just go through each and try to add
		List<ItemDeepStack> ret = null;
		
		for (ItemDeepStack deep : stacks) {
			deep = deep.copy();
			while (deep.getCount() > 0) {
				ItemStack stack = deep.splitStack(deep.getTemplate().getMaxStackSize());
				ItemStack leftover = addItem(inventory, stack);
				if (leftover != null) {
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
			@Nullable ItemStack inSlot = inventory.getStackInSlot(i);
			if (inSlot == null) {
				freeSlots++;
			} else if (!stacks.isEmpty()) {
				// Find any of our stacks that match the item in this slot
				int room = Math.min(inventory.getInventoryStackLimit(), inSlot.getMaxStackSize()) - inSlot.stackSize;
				
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
	
	private static final ItemStack attemptRemoveFromInventory(IInventory inventory, @Nullable ItemStack stack, boolean commit) {
		if (stack == null) {
    		return null;
    	}
    	
    	ItemStack itemstack = stack.copy();

    	for (int i = 0; i < inventory.getSizeInventory(); ++i) {
    		ItemStack inSlot = inventory.getStackInSlot(i);
    		
    		if (inSlot == null) {
    			continue;
    		}
    		
            if (stacksMatch(itemstack, inSlot)) {
            	// stacks appear to match. Deduct stack size
            	if (inSlot.stackSize > itemstack.stackSize) {
            		if (commit) {
	            		inSlot.stackSize -= itemstack.stackSize;
	            		inventory.markDirty();
            		}
            		return null;
            	} else {
            		itemstack.stackSize -= inSlot.stackSize;
            		if (commit) {
            			inventory.removeStackFromSlot(i);
	            		inventory.markDirty();
            		}
            		
            		if (itemstack.stackSize <= 0) {
            			return null;
            		}
            	}
           	}
        }

        return itemstack;
	}
	
	public static final boolean contains(IInventory inventory, @Nullable ItemStack items) {
		return null == attemptRemoveFromInventory(inventory, items, false);
	}
	
	public static final boolean contains(ItemStack[] inventory, @Nullable ItemStack items) {
		return contains(new ItemStackArrayWrapper(inventory), items);
	}
	
	public static final ItemStack remove(IInventory inventory, @Nullable ItemStack items) {
		return attemptRemoveFromInventory(inventory, items, true);
	}
	
	public static final ItemStack remove(ItemStack[] inventory, @Nullable ItemStack items) {
		return remove(new ItemStackArrayWrapper(inventory), items);
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
	
	public static final NBTBase serializeInventory(IInventory inv) {
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			@Nullable ItemStack stack = inv.getStackInSlot(i);
			if (stack != null) {
				list.appendTag(stack.serializeNBT());
			} else {
				list.appendTag(new NBTTagCompound());
			}
		}
		
		return list;
	}
	
	public static final boolean deserializeInventory(IInventory base, NBTBase nbt) {
		if (base == null || nbt == null || !(nbt instanceof NBTTagList)) {
			return false;
		}
		
		base.clear();
		NBTTagList list = (NBTTagList) nbt;
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			@Nullable ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
			base.setInventorySlotContents(i, stack);
		}
		
		return true;
	}
	
	private static final class ItemStackArrayWrapper implements IInventory {

		private final ItemStack[] array;
		
		public ItemStackArrayWrapper(ItemStack array[]) {
			this.array = array;
		}
		
		@Override
		public String getName() {
			return null;
		}

		@Override
		public boolean hasCustomName() {
			return false;
		}

		@Override
		public ITextComponent getDisplayName() {
			return null;
		}

		@Override
		public int getSizeInventory() {
			return array.length;
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			return array[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			return ItemStackHelper.getAndSplit(array, index, count);
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			ItemStack stack = array[index];
			array[index] = null;
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			array[index] = stack;
		}

		@Override
		public int getInventoryStackLimit() {
			return 256;
		}

		@Override
		public void markDirty() {
			;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return false;
		}

		@Override
		public void openInventory(EntityPlayer player) {
			;
		}

		@Override
		public void closeInventory(EntityPlayer player) {
			;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return true;
		}

		@Override
		public int getField(int id) {
			return 0;
		}

		@Override
		public void setField(int id, int value) {
			;
		}

		@Override
		public int getFieldCount() {
			return 0;
		}

		@Override
		public void clear() {
			for (int i = 0; i < array.length; i++) {
				array[i] = null;
			}
		}
		
	}
}
