package com.smanzana.nostrumfairies.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDict {

	public static boolean stackMatchesOreDict(@Nullable ItemStack stack, String oreDictName, boolean strict) {
		if (stack.isEmpty()) {
			return false;
		}
		
		NonNullList<ItemStack> dictionary = OreDictionary.getOres(oreDictName);
		return OreDictionary.containsMatch(strict, dictionary, stack);
	}
	
	public static boolean stackMatchesAny(@Nonnull ItemStack stack, String[] oreDictNames, boolean strict) {
		if (stack.isEmpty()) {
			return false;
		}
		
		for (String name : oreDictNames) {
			if (stackMatchesOreDict(stack, name, strict)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean blockMatchesOreDict(@Nullable BlockState state, String oreDictName, boolean strict) {
		if (state == null) {
			return false;
		}
		
		Item item = Item.getItemFromBlock(state.getBlock());
        @Nonnull ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item, 1, state.getBlock().damageDropped(state));
		return OreDict.stackMatchesOreDict(stack, oreDictName, strict);
	}
	
	public static boolean blockMatchesAny(@Nullable BlockState state, String[] oreDictNames, boolean strict) {
		if (state == null) {
			return false;
		}
		
		Item item = Item.getItemFromBlock(state.getBlock());
        ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item, 1, state.getBlock().damageDropped(state));
        return OreDict.stackMatchesAny(stack, oreDictNames, strict);
	}
	
}
