package com.smanzana.nostrumfairies.entity.fairy;

import net.minecraft.item.ItemStack;

public interface IItemCarrierFairy extends IFairyWorker {
	
	/**
	 * Return all currently-held items
	 * @return
	 */
	public ItemStack[] getCarriedItems();
	
	/**
	 * Check and return whether this fairy can add the provided stack to what it's carrying
	 * @param stack
	 * @return
	 */
	public boolean canAccept(ItemStack stack);
	
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
