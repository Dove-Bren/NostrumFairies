package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class FeySoulContainerSlot extends Slot {
	
	private final SoulStoneType type;

	public FeySoulContainerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, SoulStoneType type) {
		super(inventoryIn, index, xPosition, yPosition);
		this.type = type;
	}
	
	@Override
	public boolean isItemValid(@Nullable ItemStack stack) {
		if (stack != null) {
			if (!(stack.getItem() instanceof FeySoulStone)) {
				return false;
			}
			
			SoulStoneType itemType = FeySoulStone.getTypeOf(stack);
			if (itemType != this.type) {
				return false;
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
	
	public SoulStoneType getType() {
		return type;
	}
}
