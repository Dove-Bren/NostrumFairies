package com.smanzana.nostrumfairies.effect;

import com.smanzana.nostrumfairies.NostrumFairies;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FeyEffects {

	@ObjectHolder(FeyVisibilityEffect.ID) public static FeyVisibilityEffect feyVisibility;
	
	@SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<MobEffect> event) {
    	final IForgeRegistry<MobEffect> registry = event.getRegistry();
    	
    	registry.register(new FeyVisibilityEffect().setRegistryName(FeyVisibilityEffect.ID));
	}
	
}
