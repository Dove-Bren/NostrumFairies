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
	            		inventory.markDirty();
	            		itemstack1.stackSize += room;
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
	
}
