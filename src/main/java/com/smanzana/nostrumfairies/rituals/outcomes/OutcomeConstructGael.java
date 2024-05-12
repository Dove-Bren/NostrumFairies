package com.smanzana.nostrumfairies.rituals.outcomes;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.utils.TextUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class OutcomeConstructGael extends OutcomeSpawnItem {

	private final FairyGaelType type;
	
	public OutcomeConstructGael(FairyGaelType type) {
		super(ItemStack.EMPTY);
		this.type = type;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// set up stack and then call super to spawn it
		this.stack = FairyGael.upgrade(type, centerItem);
		
		super.perform(world, player, centerItem, otherItems, center, recipe);
	}

	private static ItemStack RES = ItemStack.EMPTY;
	@Override
	public @Nonnull ItemStack getResult() {
		if (RES.isEmpty())
			RES = FairyGael.create(type, null);
		
		return RES;
	}

	@Override
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.construct_fairy_gael.desc");
	}
	
}
