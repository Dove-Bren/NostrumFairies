package com.smanzana.nostrumfairies.entity;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfBuilder;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfCrafter;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityElfCrafter;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.fey.EntityGnomeCollector;
import com.smanzana.nostrumfairies.entity.fey.EntityGnomeCrafter;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FairyEntities {

	@ObjectHolder(EntityDwarf.ID) public static EntityType<EntityDwarf> Dwarf;
	@ObjectHolder(EntityDwarfBuilder.ID) public static EntityType<EntityDwarfBuilder> DwarfBuilder;
	@ObjectHolder(EntityDwarfCrafter.ID) public static EntityType<EntityDwarfCrafter> DwarfCrafter;
	@ObjectHolder(EntityElf.ID) public static EntityType<EntityElf> Elf;
	@ObjectHolder(EntityElfArcher.ID) public static EntityType<EntityElfArcher> ElfArcher;
	@ObjectHolder(EntityElfCrafter.ID) public static EntityType<EntityElfCrafter> ElfCrafter;
	@ObjectHolder(EntityFairy.ID) public static EntityType<EntityFairy> Fairy;
	@ObjectHolder(EntityGnome.ID) public static EntityType<EntityGnome> Gnome;
	@ObjectHolder(EntityGnomeCollector.ID) public static EntityType<EntityGnomeCollector> GnomeCollector;
	@ObjectHolder(EntityGnomeCrafter.ID) public static EntityType<EntityGnomeCrafter> GnomeCrafter;
	@ObjectHolder(EntityPersonalFairy.ID) public static EntityType<EntityPersonalFairy> PersonalFairy;
	@ObjectHolder(EntityShadowFey.ID) public static EntityType<EntityShadowFey> ShadowFey;
	@ObjectHolder(EntityTestFairy.ID) public static EntityType<EntityTestFairy> TestFairy;
	@ObjectHolder(EntityArrowEx.ID) public static EntityType<EntityArrowEx> ArrowEx;
	
	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event) {
		final IForgeRegistry<EntityType<?>> registry = event.getRegistry();
		registry.register(EntityType.Builder.<EntityDwarf>create(EntityDwarf::new, EntityClassification.CREATURE).size(0.6F, .95F).immuneToFire().setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityDwarf.ID));
		registry.register(EntityType.Builder.<EntityDwarfBuilder>create(EntityDwarfBuilder::new, EntityClassification.CREATURE).size(0.6F, .85F).immuneToFire().setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityDwarfBuilder.ID));
		registry.register(EntityType.Builder.<EntityDwarfCrafter>create(EntityDwarfCrafter::new, EntityClassification.CREATURE).size(0.6F, .8F).immuneToFire().setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityDwarfCrafter.ID));
		registry.register(EntityType.Builder.<EntityElf>create(EntityElf::new, EntityClassification.CREATURE).size(0.6F, .99F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityElf.ID));
		registry.register(EntityType.Builder.<EntityElfArcher>create(EntityElfArcher::new, EntityClassification.CREATURE).size(0.6F, .9F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityElfArcher.ID));
		registry.register(EntityType.Builder.<EntityElfCrafter>create(EntityElfCrafter::new, EntityClassification.CREATURE).size(0.6F, .9F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityElfCrafter.ID));
		registry.register(EntityType.Builder.<EntityFairy>create(EntityFairy::new, EntityClassification.CREATURE).size(0.25F, .25F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityFairy.ID));
		registry.register(EntityType.Builder.<EntityGnome>create(EntityGnome::new, EntityClassification.CREATURE).size(0.3F, .6F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityGnome.ID));
		registry.register(EntityType.Builder.<EntityGnomeCollector>create(EntityGnomeCollector::new, EntityClassification.CREATURE).size(0.45F, .3F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityGnomeCollector.ID));
		registry.register(EntityType.Builder.<EntityGnomeCrafter>create(EntityGnomeCrafter::new, EntityClassification.CREATURE).size(0.45F, .3F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityGnomeCrafter.ID));
		registry.register(EntityType.Builder.<EntityPersonalFairy>create(EntityPersonalFairy::new, EntityClassification.CREATURE).size(0.15F, .15F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityPersonalFairy.ID));
		registry.register(EntityType.Builder.<EntityShadowFey>create(EntityShadowFey::new, EntityClassification.MONSTER).size(0.6F, .75F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityShadowFey.ID));
		registry.register(EntityType.Builder.<EntityTestFairy>create(EntityTestFairy::new, EntityClassification.CREATURE).size(0.6F, .6F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityTestFairy.ID));
		registry.register(EntityType.Builder.<EntityArrowEx>create(EntityArrowEx::new, EntityClassification.MISC).size(.5F, .5F).setTrackingRange(64).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityArrowEx.ID));
	}
	
}
