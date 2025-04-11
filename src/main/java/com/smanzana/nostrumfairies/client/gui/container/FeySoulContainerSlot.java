package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FeySoulContainerSlot extends Slot {
	
	private final SoulStoneType type;

	public FeySoulContainerSlot(Container inventoryIn, int index, int x, int y, SoulStoneType type) {
		super(inventoryIn, index, x, y);
		this.type = type;
	}
	
	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		if (!stack.isEmpty()) {
			if (!(stack.getItem() instanceof FeySoulStone)) {
				return false;
			}
			
			SoulStoneType itemType = FeySoulStone.getTypeOf(stack);
			if (itemType != this.type) {
				return false;
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
	
	public SoulStoneType getType() {
		return type;
	}
}
