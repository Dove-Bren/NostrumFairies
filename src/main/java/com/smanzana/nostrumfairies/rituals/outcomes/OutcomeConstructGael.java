package com.smanzana.nostrumfairies.rituals.outcomes;

import java.util.List;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeSpawnItem;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class OutcomeConstructGael extends OutcomeSpawnItem {

	private final FairyGaelType type;
	
	public OutcomeConstructGael(FairyGaelType type) {
		super(ItemStack.EMPTY);
		this.type = type;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout ingredients, RitualRecipe recipe) {
		// set up stack and then call super to spawn it
		this.stack = FairyGael.upgrade(type, ingredients.getCenterItem(world, center));
		
		super.perform(world, player, center, ingredients, recipe);
	}

	private static ItemStack RES = ItemStack.EMPTY;
	@Override
	public @Nonnull ItemStack getResult() {
		if (RES.isEmpty())
			RES = FairyGael.create(type, null);
		
		return RES;
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.construct_fairy_gael.desc");
	}
	
}
