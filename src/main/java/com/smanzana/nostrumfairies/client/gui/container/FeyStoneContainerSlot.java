package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.inventory.IFeySlotted;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FeyStoneContainerSlot extends Slot {
	
	private final FeySlotType type;

	public FeyStoneContainerSlot(Container inventoryIn, int index, int x, int y, FeySlotType type) {
		super(inventoryIn, index, x, y);
		this.type = type;
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
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
        return this.container.canPlaceItem(this.getSlotIndex(), stack);
    }
	
	@Override
	public int getMaxStackSize() {
		return 1;
	}
	
	@Override
	public void set(@Nonnull ItemStack stack) {
		super.set(stack);
	}
	
	public FeySlotType getType() {
		return type;
	}
}
