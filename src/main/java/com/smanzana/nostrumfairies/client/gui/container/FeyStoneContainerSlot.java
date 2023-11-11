package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.inventory.IFeySlotted;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class FeyStoneContainerSlot extends Slot {
	
	private final FeySlotType type;

	public FeyStoneContainerSlot(IInventory inventoryIn, int index, int x, int y, FeySlotType type) {
		super(inventoryIn, index, x, y);
		this.type = type;
	}
	
	@Override
	public boolean isItemValid(@Nonnull ItemStack stack) {
		if (!stack.isEmpty()) {
			if (!(stack.getItem() instanceof IFeySlotted)) {
				return false;
			}
			
			IFeySlotted stone = (IFeySlotted) stack.getItem();
			if (this.type == FeySlotType.EITHERGRADE) {
				FeySlotType type = stone.getFeySlot(stack);
				return (type == FeySlotType.UPGRADE || type == FeySlotType.DOWNGRADE);
			} else {
				if (stone.getFeySlot(stack) != this.type) {
					return false;
				}
			}
		}
        return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
    }
	
	@Override
	public int getSlotStackLimit() {
		return 1;
	}
	
	@Override
	public void putStack(@Nonnull ItemStack stack) {
		super.putStack(stack);
	}
	
	public FeySlotType getType() {
		return type;
	}
}
