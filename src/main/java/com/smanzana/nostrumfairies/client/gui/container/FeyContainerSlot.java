package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeyStone;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class FeyContainerSlot extends Slot {
	
	private final FeySlotType type;

	public FeyContainerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, FeySlotType type) {
		super(inventoryIn, index, xPosition, yPosition);
		this.type = type;
	}
	
	@Override
	public boolean isItemValid(@Nullable ItemStack stack) {
		if (stack != null) {
			if (!(stack.getItem() instanceof FeyStone)) {
				return false;
			}
			
			FeyStone stone = (FeyStone) stack.getItem();
			if (this.type == FeySlotType.EITHERGRADE) {
				FeySlotType type = stone.getSlot(stack);
				return (type == FeySlotType.UPGRADE || type == FeySlotType.DOWNGRADE);
			} else {
				if (stone.getSlot(stack) != this.type) {
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
	public void putStack(@Nullable ItemStack stack) {
		super.putStack(stack);
	}
	
	public FeySlotType getType() {
		return type;
	}
}
