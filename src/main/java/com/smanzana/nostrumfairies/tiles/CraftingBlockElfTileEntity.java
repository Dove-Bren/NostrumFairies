package com.smanzana.nostrumfairies.tiles;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CraftingBlockElfTileEntity extends CraftingBlockTileEntity {

	public CraftingBlockElfTileEntity() {
		super(FairyTileEntities.CraftingBlockElfTileEntityType);
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
		
		// If downgraded, can use any craft (but it's slower)
		if (this.hasDowngradeStone()) {
			return true;
		}
		
		Item itemBase = item.getItem();
		String unloc = itemBase.getRegistryName().getPath().toLowerCase();
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
		
		float buff = hasDowngradeStone() ? .025f : .1f;
		
		String unloc = item.getItem().getRegistryName().getPath().toLowerCase();
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
		return super.getMaxWorkJobs() * (this.hasUpgradeStone() ? 2 : 1);
	}
}