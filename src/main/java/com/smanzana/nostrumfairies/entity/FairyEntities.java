package com.smanzana.nostrumfairies.entity;

import java.util.Set;

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

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
		registry.register(EntityType.Builder.<EntityDwarf>of(EntityDwarf::new, MobCategory.CREATURE).sized(0.6F, .95F).fireImmune().setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityDwarf.ID));
		registry.register(EntityType.Builder.<EntityDwarfBuilder>of(EntityDwarfBuilder::new, MobCategory.CREATURE).sized(0.6F, .85F).fireImmune().setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityDwarfBuilder.ID));
		registry.register(EntityType.Builder.<EntityDwarfCrafter>of(EntityDwarfCrafter::new, MobCategory.CREATURE).sized(0.6F, .8F).fireImmune().setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityDwarfCrafter.ID));
		registry.register(EntityType.Builder.<EntityElf>of(EntityElf::new, MobCategory.CREATURE).sized(0.6F, .99F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityElf.ID));
		registry.register(EntityType.Builder.<EntityElfArcher>of(EntityElfArcher::new, MobCategory.CREATURE).sized(0.6F, .9F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityElfArcher.ID));
		registry.register(EntityType.Builder.<EntityElfCrafter>of(EntityElfCrafter::new, MobCategory.CREATURE).sized(0.6F, .9F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityElfCrafter.ID));
		registry.register(EntityType.Builder.<EntityFairy>of(EntityFairy::new, MobCategory.CREATURE).sized(0.25F, .25F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityFairy.ID));
		registry.register(EntityType.Builder.<EntityGnome>of(EntityGnome::new, MobCategory.CREATURE).sized(0.3F, .6F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityGnome.ID));
		registry.register(EntityType.Builder.<EntityGnomeCollector>of(EntityGnomeCollector::new, MobCategory.CREATURE).sized(0.45F, .4F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityGnomeCollector.ID));
		registry.register(EntityType.Builder.<EntityGnomeCrafter>of(EntityGnomeCrafter::new, MobCategory.CREATURE).sized(0.45F, .5F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityGnomeCrafter.ID));
		registry.register(EntityType.Builder.<EntityPersonalFairy>of(EntityPersonalFairy::new, MobCategory.CREATURE).sized(0.15F, .15F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityPersonalFairy.ID));
		registry.register(EntityType.Builder.<EntityTestFairy>of(EntityTestFairy::new, MobCategory.CREATURE).sized(0.6F, .6F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityTestFairy.ID));
		registry.register(EntityType.Builder.<EntityArrowEx>of(EntityArrowEx::new, MobCategory.MISC).sized(.5F, .5F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("").setRegistryName(EntityArrowEx.ID));
		
		EntityType<EntityShadowFey> feyType = EntityType.Builder.<EntityShadowFey>of(EntityShadowFey::new, MobCategory.MONSTER).sized(0.6F, .75F).setTrackingRange(128).setUpdateInterval(1).setShouldReceiveVelocityUpdates(false).build("");
		feyType.setRegistryName(EntityShadowFey.ID);
		registry.register(feyType);
	}
	
	@SubscribeEvent
	public static void registerEntityPlacement(FMLCommonSetupEvent event) {
		SpawnPlacements.register(ShadowFey, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
		
		// Can't mix buses, so manually register spawn handling to the game bus
		MinecraftForge.EVENT_BUS.addListener(FairyEntities::registerSpawns);
	}
	
	public static final void registerSpawns(BiomeLoadingEvent event) {
		final Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(ResourceKey.create(Registry.BIOME_REGISTRY, event.getName()));
		
		final boolean magical = types.contains(BiomeDictionary.Type.MAGICAL);
		final boolean forest = types.contains(BiomeDictionary.Type.FOREST);
		final boolean spooky = types.contains(BiomeDictionary.Type.SPOOKY);
		final boolean dense = types.contains(BiomeDictionary.Type.DENSE);
		
		if (magical) {
			addSpawn(event, ShadowFey, MobCategory.MONSTER, 35, 1, 2);
		} else if (forest) {
			addSpawn(event, ShadowFey, MobCategory.MONSTER, 25, 1, 3);
		} else if (spooky) {
			addSpawn(event, ShadowFey, MobCategory.MONSTER, 18, 2, 2);
		} else if (dense) {
			addSpawn(event, ShadowFey, MobCategory.MONSTER, 20, 1, 2);
		}
	}
	
	private static void addSpawn(BiomeLoadingEvent event, EntityType<? extends Mob> entityType, MobCategory classification, int itemWeight, int minGroupCount, int maxGroupCount) {
		event.getSpawns().getSpawner(classification).add(new MobSpawnSettings.SpawnerData(entityType, itemWeight, minGroupCount, maxGroupCount));
	}
	
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(Dwarf, EntityDwarf.BuildAttributes().build());
		event.put(DwarfBuilder, EntityDwarfBuilder.BuildBuilderAttributes().build());
		event.put(DwarfCrafter, EntityDwarfCrafter.BuildCrafterAttributes().build());
		event.put(Elf, EntityElf.BuildAttributes().build());
		event.put(ElfArcher, EntityElfArcher.BuildArcherAttributes().build());
		event.put(ElfCrafter, EntityElfCrafter.BuildCrafterAttributes().build());
		event.put(Fairy, EntityFairy.BuildAttributes().build());
		event.put(Gnome, EntityGnome.BuildAttributes().build());
		event.put(GnomeCollector, EntityGnomeCollector.BuildCollectorAttributes().build());
		event.put(GnomeCrafter, EntityGnomeCrafter.BuildCrafterAttributes().build());
		event.put(PersonalFairy, EntityPersonalFairy.BuildPersonalAttributes().build());
		event.put(ShadowFey, EntityShadowFey.BuildAttributes().build());
		event.put(TestFairy, EntityTestFairy.BuildAttributes().build());
	}
	
}
