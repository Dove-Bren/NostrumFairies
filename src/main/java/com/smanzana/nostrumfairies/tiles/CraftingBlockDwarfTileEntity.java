package com.smanzana.nostrumfairies.tiles;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CraftingBlockDwarfTileEntity extends CraftingBlockTileEntity {

	public CraftingBlockDwarfTileEntity() {
		super(FairyTileEntities.CraftingBlockDwarfTileEntityType);
	}

	@Override
	public int getCraftGridDim() {
		return 3;
	}
	
	protected boolean isGoodMaterialName(String unloc) {
		return unloc.contains("ingot")
				|| unloc.contains("metal")
				|| unloc.contains("iron")
				|| unloc.contains("gold")
				|| unloc.contains("gear")
				|| unloc.contains("bronze")
				|| unloc.contains("copper")
				|| unloc.contains("tin")
				|| unloc.contains("aluminum")
				|| unloc.contains("titanium")
				|| unloc.contains("rod")
				|| unloc.contains("stone")
				|| unloc.contains("rock")
				|| unloc.contains("machine")
				|| unloc.contains("part")
				|| unloc.contains("cast");
	}
	
	@Override
	protected boolean canCraftWith(ItemStack item) {
		if (item.isEmpty()) {
			return true;
		}
		
		// If downgrade present, can always craft
		if (this.hasDowngradeStone()) {
			return true;
		}
		
		// If upgrade present, material matching is strict
		boolean strict = hasUpgradeStone();
	
		Item itemBase = item.getItem();
		String unloc = itemBase.getRegistryName().getPath().toLowerCase();
		
		if (strict) {
			// HAS to be a friendly material
			if (isGoodMaterialName(unloc)) {
				return true;
			}
			return false;
		}
		
		// if not strict, just can't be a bad material
		if (unloc.contains("log")
				|| unloc.contains("plank")
				|| unloc.contains("wood")
				|| unloc.contains("stick")) {
			return false;
		}
		
		return true;
	}
	
	@Override
	protected float getCraftBonus(ItemStack item) {
		if (item.isEmpty()) {
			return 0f;
		}
		
		float buff = hasUpgradeStone() ? .35f 
				: hasDowngradeStone() ? .025f
				: .1f;
		
		Item itemBase = item.getItem();
		String unloc = itemBase.getRegistryName().getPath().toLowerCase();
		if (isGoodMaterialName(unloc)) {
			return buff;
		}
	
		return 0f;
	}
}