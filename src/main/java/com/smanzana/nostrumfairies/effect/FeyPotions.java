package com.smanzana.nostrumfairies.effect;

import java.util.function.Supplier;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrummagica.effects.NostrumPotions;
import com.smanzana.nostrummagica.effects.NostrumPotions.PotionIngredient;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum FeyPotions {

	FEY_VISIBILITY("fey-visibility", () -> FeyEffects.feyVisibility.getEffectName(), () -> new EffectInstance(FeyEffects.feyVisibility, 2 * 60)),
	FEY_VISIBILITY_EXTENDED("extended_fey-visibility", () -> FeyEffects.feyVisibility.getEffectName(), () -> new EffectInstance(FeyEffects.feyVisibility, 2 * 8 * 60));
	
	private final String registryName;
	private final Supplier<String> effectNameSupp;
	private final Supplier<EffectInstance> effectsSupp;
	
	private Potion type;
	
	private FeyPotions(String registryName, Supplier<String> effectName, Supplier<EffectInstance> effects) {
		// Note: using suppliers because effects won't be set up when enum is done.
		this.registryName = registryName;
		this.effectNameSupp = effectName;
		this.effectsSupp = effects;
	}
	
	protected Potion getTypeInternal() {
		if (type == null) {
			this.type = new Potion(effectNameSupp.get(), effectsSupp.get());
			type.setRegistryName(new ResourceLocation(NostrumFairies.MODID, registryName));
		}
		return this.type;
	}
	
	public Potion getType() {
		return getTypeInternal();
	}
	
	@SubscribeEvent
	public static void register(RegistryEvent.Register<Potion> event) {
		for (FeyPotions wrapper : FeyPotions.values()) {
			event.getRegistry().register(wrapper.getTypeInternal());
		}
	}
	
	@SubscribeEvent
	public static final void registerPotionMixes(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
		// Mana regen potion
	    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(Potions.THICK),
	    			Ingredient.fromItems(FairyItems.feyTears),
	    			NostrumPotions.MakePotion(FeyPotions.FEY_VISIBILITY.getType()));
	    	
	    	BrewingRecipeRegistry.addRecipe(new PotionIngredient(FeyPotions.FEY_VISIBILITY.getType()),
	    			Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE),
	    			NostrumPotions.MakePotion(FeyPotions.FEY_VISIBILITY_EXTENDED.getType()));
		});
	}
}
