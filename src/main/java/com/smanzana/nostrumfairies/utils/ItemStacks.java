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

	public static ItemStack addItem(IInventory inventory, @Nullable ItemStack stack) {
    	if (stack == null) {
    		return null;
    	}
    	
    	ItemStack itemstack = stack.copy();

    	for (int i = 0; i < inventory.getSizeInventory(); ++i) {
            ItemStack itemstack1 = inventory.getStackInSlot(i);

            if (itemstack1 == null) {
                inventory.setInventorySlotContents(i, itemstack);
                inventory.markDirty();
                return null;
            }
            
            if (stacksMatch(itemstack, itemstack1)) {
            	// stacks appear to match. Deduct stack size
            	int room = itemstack1.getMaxStackSize() - itemstack1.stackSize;
            	if (room > itemstack.stackSize) {
            		itemstack1.stackSize += itemstack.stackSize;
            		inventory.markDirty();
            		return null;
            	} else if (room > 0) {
            		inventory.markDirty();
            		itemstack.stackSize -= room;
            		itemstack1.stackSize += room;
            	}
            }
        }

        return itemstack;
    }
	
}
