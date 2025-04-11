package com.smanzana.nostrumfairies.inventory;

import com.smanzana.nostrumfairies.items.FeyStoneMaterial;

import net.minecraft.world.item.ItemStack;

public interface IFeySlotted {

	/**
	 * Return which slot type this item can be socketted into.
	 * @param stack
	 * @return
	 */
	public FeySlotType getFeySlot(ItemStack stack);
	
	/**
	 * Return the material this stone is made out of
	 * @param stack
	 * @return
	 */
	public FeyStoneMaterial getStoneMaterial(ItemStack stack);
	
}
