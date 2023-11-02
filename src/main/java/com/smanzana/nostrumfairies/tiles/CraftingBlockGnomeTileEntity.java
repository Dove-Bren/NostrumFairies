package com.smanzana.nostrumfairies.tiles;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CraftingBlockGnomeTileEntity extends CraftingBlockTileEntity {

	public CraftingBlockGnomeTileEntity() {
		super();
	}

	@Override
	public int getCraftGridDim() {
		return 2;
	}

	@Override
	protected boolean canCraftWith(ItemStack item) {
		return true;
	}

	@Override
	protected float getCraftBonus(ItemStack item) {
		if (item.isEmpty()) {
			return 0f;
		}
		
		if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.DOWNGRADE
				&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
			return .2f;
		}
		
		Item itemBase = item.getItem();
		String unloc = itemBase.getUnlocalizedName().toLowerCase();
		if (unloc.contains("leaves")
				|| unloc.contains("leaf")
				|| unloc.contains("plant")
				|| unloc.contains("crop")
				|| unloc.contains("seed")
				|| unloc.contains("dirt")
				|| unloc.contains("water")
				|| unloc.contains("flower")) {
			return .3f;
		}
	
		return 0f;
	}
	
	@Override
	protected ItemStack generateOutput() {
		ItemStack out = super.generateOutput();
		if (!out.isEmpty()
				&& !out.getUnlocalizedName().toLowerCase().contains("block")
				&& !out.getUnlocalizedName().toLowerCase().contains("ingot")
				&& !out.getUnlocalizedName().toLowerCase().contains("nugget")) {
			if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.UPGRADE
					&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
				int bonus = 0;
				int chances = 5;
				
				for (int i = 0; i < out.getCount(); i++) {
					if (NostrumFairies.random.nextInt(chances) == 0) {
						bonus++;
						chances += 2;
					}
				}
				
				out.setCount(Math.min(out.getMaxStackSize(), out.getCount() + bonus));
			}
		}
		
		return out;
	}
}