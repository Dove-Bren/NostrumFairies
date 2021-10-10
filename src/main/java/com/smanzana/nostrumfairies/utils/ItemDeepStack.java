package com.smanzana.nostrumfairies.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Wrapper to ItemStacks that ignore the original itemstack limits and instead permit a limit of a 64-bit signed int.
 * In general, DeepStacks CAN have stack size 0.
 * @author Skyler
 *
 */
public class ItemDeepStack {

	private ItemStack item;
	private long count;
	
	public ItemDeepStack(ItemStack template, long count) {
		this.item = template.copy();
		this.count = count;
		
		this.item.setCount(1);
	}
	
	public ItemDeepStack(ItemStack original) {
		this(original, original.getCount());
	}
	
	public long getCount() {
		return count;
	}
	
	public ItemStack getTemplate() {
		return item;
	}
	
	public boolean canMerge(ItemStack other) {
		return ItemStacks.stacksMatch(item, other);
	}
	
	public boolean canMerge(ItemDeepStack other) {
		return ItemStacks.stacksMatch(item, other.item);
	}
	
	public void add(ItemStack original) {
		add(original.getCount());
	}
	
	public void add(ItemDeepStack original) {
		add(original.count);
	}
	
	public void add(long count) {
		this.count += count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
	public ItemStack splitStack(int requestedCount) {
		ItemStack stack = item.copy();
		
		stack.setCount((int) Math.min(this.count, requestedCount));
		this.count -= stack.getCount();
		
		return stack;
	}
	
	public ItemDeepStack copy() {
		return new ItemDeepStack(item.copy(), count);
	}
	
	public static List<ItemDeepStack> toDeepList(List<ItemDeepStack> out, Iterable<ItemStack> items) {
		// Could make sure both lists are sorted by itemstack, and have iterators on both to make this merge fast.
		// Optimization opportunity!
		for (ItemStack stack : items) {
			if (stack.isEmpty()) {
				continue;
			}
			
			boolean merged = false;
			for (ItemDeepStack condensed : out) {
				if (condensed.canMerge(stack)) {
					condensed.add(stack);
					merged = true;
					break;
				}
			}
			
			if (!merged) {
				out.add(new ItemDeepStack(stack));
			}
		}
		return out;
	}
	
	public static List<ItemDeepStack> toDeepList(Collection<ItemStack> items) {
		List<ItemDeepStack> out = new ArrayList<>(items.size());
		return toDeepList(out, items);
	}
	
	public static List<ItemDeepStack> toDeepList(List<ItemDeepStack> out, InventoryPlayer inventory) {
		out = toDeepList(out, (IInventory) inventory);
		
		final @Nonnull ItemStack heldStack = inventory.getItemStack();
		
		if (!heldStack.isEmpty()) {
			boolean merged = false;
			for (ItemDeepStack condensed : out) {
				if (condensed.canMerge(heldStack)) {
					condensed.add(heldStack);
					merged = true;
					break;
				}
			}
			
			if (!merged) {
				out.add(new ItemDeepStack(heldStack));
			}
		}
		
		return out;
	}
	
	public static List<ItemDeepStack> toDeepList(InventoryPlayer inventory) {
		return toDeepList(new ArrayList<>(inventory.getSizeInventory()), inventory);
	}
	
	public static List<ItemDeepStack> toDeepList(List<ItemDeepStack> out, IInventory inventory) {
		return toDeepList(out, () -> {
			return new Iterator<ItemStack>() {
				
				private int i = 0;
				
				@Override
				public boolean hasNext() {
					return i < inventory.getSizeInventory();
				}

				@Override
				public ItemStack next() {
					return inventory.getStackInSlot(i++);
				}
			};
		});
	}
	
	public static List<ItemDeepStack> toDeepList(IInventory inventory) {
		return toDeepList(new ArrayList<>(inventory.getSizeInventory()), inventory);
	}
	
	public static List<ItemDeepStack> toCondensedDeepList(Collection<ItemDeepStack> deeps) {
		// Could make sure both lists are sorted by itemstack, and have iterators on both to make this merge fast.
		// Optimization oppertunity!
		List<ItemDeepStack> out = new ArrayList<>(deeps.size());
		for (ItemDeepStack deep: deeps) {
			boolean merged = false;
			for (ItemDeepStack condensed : out) {
				if (condensed.canMerge(deep)) {
					condensed.add(deep);
					merged = true;
					break;
				}
			}
			
			if (!merged) {
				out.add(deep);
			}
		}
		return out;
	}
}
