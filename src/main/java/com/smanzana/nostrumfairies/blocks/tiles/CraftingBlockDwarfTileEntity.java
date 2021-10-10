package com.smanzana.nostrumfairies.blocks.tiles;

import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CraftingBlockDwarfTileEntity extends CraftingBlockTileEntity {

	public CraftingBlockDwarfTileEntity() {
		super();
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
		if (item == null) {
			return true;
		}
		
		boolean strict = false;
		if (FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
			FeySlotType slot = FeyStone.instance().getFeySlot(this.getUpgrade()); 
			if (slot == FeySlotType.DOWNGRADE) {
				return true;
			} else if (slot == FeySlotType.UPGRADE) {
				strict = true;
			}
		}
	
		Item itemBase = item.getItem();
		String unloc = itemBase.getUnlocalizedName();
		
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
		if (item == null) {
			return 0f;
		}
		
		float buff = .1f;
		if (FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
			FeySlotType slot = FeyStone.instance().getFeySlot(this.getUpgrade());
			if (slot == FeySlotType.DOWNGRADE) {
				buff = .025f; // but no disallowed item types
			} else if (slot == FeySlotType.UPGRADE) {
				buff = .35f;
			}
		}
		
		Item itemBase = item.getItem();
		String unloc = itemBase.getUnlocalizedName();
		if (isGoodMaterialName(unloc)) {
			return buff;
		}
	
		return 0f;
	}
}