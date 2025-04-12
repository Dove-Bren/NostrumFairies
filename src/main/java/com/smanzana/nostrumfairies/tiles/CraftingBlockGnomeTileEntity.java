package com.smanzana.nostrumfairies.tiles;

import com.smanzana.nostrumfairies.NostrumFairies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CraftingBlockGnomeTileEntity extends CraftingBlockTileEntity {

	public CraftingBlockGnomeTileEntity(BlockPos pos, BlockState state) {
		super(FairyTileEntities.CraftingBlockGnomeTileEntityType, pos, state);
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
		
		if (this.hasDowngradeStone()) {
			return .2f;
		}
		
		Item itemBase = item.getItem();
		String unloc = itemBase.getRegistryName().getPath().toLowerCase();
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
				&& !out.getItem().getRegistryName().getPath().toLowerCase().contains("block")
				&& !out.getItem().getRegistryName().getPath().toLowerCase().contains("ingot")
				&& !out.getItem().getRegistryName().getPath().toLowerCase().contains("nugget")) {
			if (hasUpgradeStone()) {
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