package com.smanzana.nostrumfairies.utils;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDict {

	public static boolean stackMatchesOreDict(@Nullable ItemStack stack, String oreDictName, boolean strict) {
		if (stack == null) {
			return false;
		}
		
		List<ItemStack> dictionary = OreDictionary.getOres(oreDictName);
		return OreDictionary.containsMatch(strict, dictionary, stack);
	}
	
	public static boolean stackMatchesAny(@Nullable ItemStack stack, String[] oreDictNames, boolean strict) {
		if (stack == null) {
			return false;
		}
		
		for (String name : oreDictNames) {
			if (stackMatchesOreDict(stack, name, strict)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean blockMatchesOreDict(@Nullable IBlockState state, String oreDictName, boolean strict) {
		if (state == null) {
			return false;
		}
		
		Item item = Item.getItemFromBlock(state.getBlock());
        ItemStack stack = item == null ? null : new ItemStack(item, 1, state.getBlock().damageDropped(state));
		return OreDict.stackMatchesOreDict(stack, oreDictName, strict);
	}
	
	public static boolean blockMatchesAny(@Nullable IBlockState state, String[] oreDictNames, boolean strict) {
		if (state == null) {
			return false;
		}
		
		Item item = Item.getItemFromBlock(state.getBlock());
        ItemStack stack = item == null ? null : new ItemStack(item, 1, state.getBlock().damageDropped(state));
        return OreDict.stackMatchesAny(stack, oreDictNames, strict);
	}
	
}
