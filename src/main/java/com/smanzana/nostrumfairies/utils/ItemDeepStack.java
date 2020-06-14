package com.smanzana.nostrumfairies.utils;

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
	
	public void add(ItemStack original) {
		add(original.stackSize);
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
}
