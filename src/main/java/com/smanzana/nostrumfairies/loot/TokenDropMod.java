package com.smanzana.nostrumfairies.loot;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.smanzana.nostrumfairies.items.FairyItems;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class TokenDropMod extends LootModifier {
	
	public TokenDropMod(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}
	
	@Override
	@Nonnull
	public List<ItemStack> doApply(List<ItemStack> loot, LootContext context) {
		loot.add(new ItemStack(FairyItems.feyGolemToken, 1));
		return loot;
	}
	
	public static class Serializer extends GlobalLootModifierSerializer<TokenDropMod> {
		
		public static final String ID = "token_drop";

		@Override
		public TokenDropMod read(ResourceLocation location, JsonObject object, LootItemCondition[] ailootcondition) {
        	return new TokenDropMod(ailootcondition);
		}

		@Override
		public JsonObject write(TokenDropMod instance) {
			return this.makeConditions(instance.conditions);
		}
		
	}
	
}
