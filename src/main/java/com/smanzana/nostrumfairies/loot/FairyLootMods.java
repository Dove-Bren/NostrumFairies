package com.smanzana.nostrumfairies.loot;

import com.smanzana.nostrumfairies.NostrumFairies;

import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FairyLootMods {

	@ObjectHolder(TokenDropMod.Serializer.ID) public static TokenDropMod.Serializer addItem;
	
	@SubscribeEvent
	public static void registerSerializers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
		final IForgeRegistry<GlobalLootModifierSerializer<?>> registry = event.getRegistry();
		
		registry.register(new TokenDropMod.Serializer().setRegistryName(TokenDropMod.Serializer.ID));
	}
	
}
