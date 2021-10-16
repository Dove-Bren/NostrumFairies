package com.smanzana.nostrumfairies.blocks.tiles;

import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CraftingBlockElfTileEntity extends CraftingBlockTileEntity {

	public CraftingBlockElfTileEntity() {
		super();
	}

	@Override
	public int getCraftGridDim() {
		return 3;
	}

	@Override
	protected boolean canCraftWith(ItemStack item) {
		if (item.isEmpty()) {
			return true;
		}
		
		if (!this.getUpgrade().isEmpty()) {
			if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.DOWNGRADE
					&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
				return true;
			}
		}
		
		Item itemBase = item.getItem();
		String unloc = itemBase.getUnlocalizedName().toLowerCase();
		if (unloc.contains("ingot")
				|| unloc.contains("metal")
				|| unloc.contains("iron")
				|| unloc.contains("gold")) {
			return false;
		}
		
		return true;
	}
	
	@Override
	protected float getCraftBonus(ItemStack item) {
		if (item.isEmpty()) {
			return 0f;
		}
		
		float buff = .1f;
		if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.DOWNGRADE
					&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
			buff = .025f;
		}
		
		Item itemBase = item.getItem();
		String unloc = itemBase.getUnlocalizedName().toLowerCase();
		if (unloc.contains("log")
				|| unloc.contains("plank")
				|| unloc.contains("wood")
				|| unloc.contains("stick")) {
			return buff;
		}
	
		return 0f;
	}
	
	@Override
	protected int getMaxWorkJobs() {
		final int base = super.getMaxWorkJobs();
		if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.UPGRADE
				&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
			return 2 * base;
		}
		return base;
	}
}