package com.smanzana.nostrumfairies.potion;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.potion.Effect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FeyEffects {

	@ObjectHolder(FeyVisibilityEffect.ID) public static FeyVisibilityEffect feyVisibility;
	
	@SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Effect> event) {
    	final IForgeRegistry<Effect> registry = event.getRegistry();
    	
    	registry.register(new FeyVisibilityEffect().setRegistryName(FeyVisibilityEffect.ID));
	}
	
}
