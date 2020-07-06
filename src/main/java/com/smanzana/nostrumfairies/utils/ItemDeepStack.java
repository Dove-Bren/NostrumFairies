package com.smanzana.nostrumfairies.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Wrapper to ItemStacks that ignore the original itemstack limits and instead permit a limit of a 64-bit signed int.
 * @author Skyler
 *
 */
public class ItemDeepStack {

	private ItemStack item;
	private long count;
	
	public ItemDeepStack(ItemStack template, long count) {
		this.item = template.copy();
		this.count = count;
		
		this.item.stackSize = 1;
	}
	
	public ItemDeepStack(ItemStack original) {
		this(original, original.stackSize);
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
		add(original.stackSize);
	}
	
	public void add(ItemDeepStack original) {
		add(original.count);
	}
	
	public void add(long count) {
		this.count += count;
	}
	
	public ItemStack splitStack(int requestedCount) {
		ItemStack stack = item.copy();
		
		stack.stackSize = (int) Math.min(this.count, requestedCount);
		this.count -= stack.stackSize;
		
		return stack;
	}
	
	public ItemDeepStack copy() {
		return new ItemDeepStack(item.copy(), count);
	}
	
	public static List<ItemDeepStack> toDeepList(Collection<ItemStack> items) {
		// Could make sure both lists are sorted by itemstack, and have iterators on both to make this merge fast.
		// Optimization oppertunity!
		List<ItemDeepStack> out = new ArrayList<>(items.size());
		for (ItemStack stack : items) {
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
}
