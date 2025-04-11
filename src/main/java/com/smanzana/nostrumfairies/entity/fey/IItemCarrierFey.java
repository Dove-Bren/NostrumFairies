package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

public interface IItemCarrierFey extends IFeyWorker {
	
	/**
	 * Return all currently-held items
	 * @return
	 */
	public NonNullList<ItemStack> getCarriedItems();
	
	/**
	 * Check and return whether this fairy can add the provided stack to what it's carrying
	 * @param stack
	 * @return
	 */
	public boolean canAccept(ItemStack stack);
	
	/**
	 * Check whether this fairy could add all of the provided to what it's carrying.
	 * @param stack
	 * @return
	 */
	public boolean canAccept(ItemDeepStack stack);
	
	/**
	 * Add the provided item to what this fairy is carrying
	 * @param stack
	 */
	public void addItem(ItemStack stack);
	
	/**
	 * Remove an item stack from the fairy.
	 * Note this is 'remove stack.stackSize items of type stack.getItem() + stack.getMetadata()'
	 * @param stack
	 */
	public void removeItem(ItemStack stack);
	
}
