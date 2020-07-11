package com.smanzana.nostrumfairies.utils;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ItemStacks {
	
	public static boolean stacksMatch(@Nullable ItemStack stack1, @Nullable ItemStack stack2) {
		if (stack1 == null || stack2 == null) {
			return stack1 == null && stack2 == null;
		}
		
		return stack1.getItem() == stack2.getItem()
        		&& stack1.getMetadata() == stack2.getMetadata()
        		&& Objects.equals(stack1.getTagCompound(), stack2.getTagCompound());
	}

	private static ItemStack attemptAddToInventory(IInventory inventory, @Nullable ItemStack stack, boolean commit) {
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
	 
	public static ItemStack addItem(IInventory inventory, @Nullable ItemStack stack) {
		return attemptAddToInventory(inventory, stack, true);
	}
	
	public static boolean canFit(IInventory inventory, @Nullable ItemStack stack) {
		return null == attemptAddToInventory(inventory, stack, false);
	}
	
	private static ItemStack attemptRemoveFromInventory(IInventory inventory, @Nullable ItemStack stack, boolean commit) {
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
	
	public static boolean contains(IInventory inventory, @Nullable ItemStack items) {
		return null == attemptRemoveFromInventory(inventory, items, false);
	}
	
	public static ItemStack remove(IInventory inventory, @Nullable ItemStack items) {
		return attemptRemoveFromInventory(inventory, items, true);
	}
	
}
