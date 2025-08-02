package com.smanzana.nostrumfairies.init;

import com.smanzana.nostrumaetheria.api.proxy.AetheriaIDs;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.capabilities.CapabilityHandler;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.templates.ITemplateViewerCapability;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.items.FairyInstrument;
import com.smanzana.nostrumfairies.items.FairyInstrument.InstrumentType;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.items.FeyTablet;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.research.FairyResearches;
import com.smanzana.nostrumfairies.rituals.outcomes.OutcomeConstructGael;
import com.smanzana.nostrumfairies.serializers.ArmPoseDwarf;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.ArmPoseGnome;
import com.smanzana.nostrumfairies.serializers.BattleStanceElfArcher;
import com.smanzana.nostrumfairies.serializers.BattleStanceShadowFey;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.serializers.FairyJob;
import com.smanzana.nostrumfairies.serializers.ItemArraySerializer;
import com.smanzana.nostrumfairies.tiles.LogisticsTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.progression.requirement.ResearchRequirement;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.ritual.RitualRegistry;
import com.smanzana.nostrummagica.ritual.outcome.OutcomeSpawnItem;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Common (client and server) handler for MOD bus events.
 * MOD bus is not game event bus.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModInit {
	
	public static CapabilityHandler capabilityHandler;

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {
		
		// EARLY phase:
		////////////////////////////////////////////
    	// NOTE: These registering methods are on the regular gameplay BUS,
    	// because they depend on data and re-fire when data is reloaded?
		MinecraftForge.EVENT_BUS.addListener(ModInit::registerDefaultRituals);
		
		preinit();
		
		// MID phase:
		////////////////////////////////////////////
		FairyResearches.init();
		NostrumMagica.instance.registerResearchReloadHook(FairyResearches::init);

		init();
		
		// LATE phase:
		//////////////////////////////////////////
		// Used to be two different mod init steps!
		
		postinit();
	}
	
	private static final void preinit() {
		NetworkHandler.getInstance();
		registerLogisticsComponents();
	}
	
	private static final void init() {
		registerLore();
		
    	FairyGael.registerRecipes();
	}
	
	private static final void postinit() {
		TemplateBlock.RegisterBaseOverrides();
	}
    
    private static final void registerLogisticsComponents() {
//    	logisticsComponentRegistry.registerComponentType(StorageLogisticsChest.StorageChestTileEntity.LOGISTICS_TAG,
//    			new StorageLogisticsChest.StorageChestTileEntity.StorageChestTEFactory());
//    	logisticsComponentRegistry.registerComponentType(BufferLogisticsChest.BufferChestTileEntity.LOGISTICS_TAG,
//    			new BufferLogisticsChest.BufferChestTileEntity.BufferChestTEFactory());
//    	logisticsComponentRegistry.registerComponentType(OutputLogisticsChest.OutputChestTileEntity.LOGISTICS_TAG,
//    			new OutputLogisticsChest.OutputChestTileEntity.OutputChestTEFactory());
//    	logisticsComponentRegistry.registerComponentType(StorageMonitor.StorageMonitorTileEntity.LOGISTICS_TAG,
//    			new StorageMonitor.StorageMonitorTileEntity.StorageMonitorTEFactory());
    	NostrumFairies.logisticsComponentRegistry.registerComponentType(LogisticsTileEntity.LogisticsTileEntityComponent.LOGISTICS_TAG,
    			new LogisticsTileEntity.LogisticsTileEntityComponent.ComponentFactory());
    }
	
	private static final void registerLore() {
    	LoreRegistry.instance().register(EntityShadowFey.ShadowFeyConversionLore.instance());
    	LoreRegistry.instance().register(FeyTablet.FeyFriendLore.instance());
    	LoreRegistry.instance().register(EntityDwarf.DwarfLore.instance);
    	LoreRegistry.instance().register(EntityElf.ElfLore.instance);
    	LoreRegistry.instance().register(EntityFairy.FairyLore.instance);
    	LoreRegistry.instance().register(EntityGnome.GnomeLore.instance);
    }
	
	// Aetheria items
	@ObjectHolder("nostrumaetheria:" + AetheriaIDs.GINSENG_FLOWER) public static Item ginsengFlower;
	@ObjectHolder("nostrumaetheria:" + AetheriaIDs.MANDRAKE_FLOWER) public static Item mandrakeFlower;
    
    public static final void registerDefaultRituals(RitualRegistry.RitualRegisterEvent event) {
		final RitualRegistry registry = event.registry;
    	
    	registry.register(
			RitualRecipe.createTier3("purify_essence",
					new ItemStack(FairyItems.feyEssence, 1),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.of(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.of(FairyItems.feyCorruptedEssence), Ingredient.of(Items.WATER_BUCKET), Ingredient.of(FairyItems.feyCorruptedEssence), Ingredient.of(FairyItems.feyCorruptedEssence)},
				new ResearchRequirement(FairyResearches.ID_Purify_Essence),
				new OutcomeSpawnItem(new ItemStack(FairyItems.feyEssence, 3)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_bell",
				new ItemStack(FairyItems.feyBell, 1),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.of(Tags.Items.INGOTS_GOLD),
				new Ingredient[] {Ingredient.of(FairyItems.feyEssence), Ingredient.of(NostrumTags.Items.CrystalMedium), Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(FairyItems.feyEssence)},
				new ResearchRequirement(FairyResearches.ID_Fey_Bell),
				new OutcomeSpawnItem(new ItemStack(FairyItems.feyBell, 1)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_flower",
				new ItemStack(FairyItems.feyFlower, 1),
				EMagicElement.EARTH,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH, ReagentType.SKY_ASH},
				Ingredient.of(Blocks.TALL_GRASS),
				new Ingredient[] {Ingredient.of(FairyItems.feyTears), Ingredient.of(Items.MELON), Ingredient.of(FairyItems.feyEssence), Ingredient.of(FairyItems.feyTears)},
				new ResearchRequirement(FairyResearches.ID_Fey_Flower),
				new OutcomeSpawnItem(new ItemStack(FairyItems.feyFlower, 4)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("soul_stone",
				FeySoulStone.create(SoulStoneType.GEM),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH},
				Ingredient.of(NostrumTags.Items.CrystalMedium),
				new Ingredient[] {Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(FairyItems.feyTears), Ingredient.of(FairyBlocks.feyBush), Ingredient.of(FairyItems.feyCorruptedEssence)},
				new ResearchRequirement(FairyResearches.ID_Fey_Souls),
				new OutcomeSpawnItem(FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("soul_gael",
				FeySoulStone.create(SoulStoneType.GAEL),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH},
				Ingredient.of(NostrumTags.Items.CrystalMedium),
				new Ingredient[] {Ingredient.of(Tags.Items.DUSTS_GLOWSTONE), Ingredient.of(FairyItems.feyTears), Ingredient.of(FairyBlocks.feyBush), Ingredient.of(FairyItems.feyCorruptedEssence)},
				new ResearchRequirement(FairyResearches.ID_Fey_Souls),
				new OutcomeSpawnItem(FeySoulStone.create(SoulStoneType.GAEL)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_instrument_flute",
				FairyInstrument.create(InstrumentType.FLUTE),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				Ingredient.of(ItemTags.LOGS),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(FairyItems.feyEssence), Ingredient.of(Items.SUGAR_CANE), Ingredient.of(NostrumTags.Items.CrystalSmall)},
				new ResearchRequirement(FairyResearches.ID_Fairy_Instruments),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.FLUTE)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_instrument_lyre",
				FairyInstrument.create(InstrumentType.HARP),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				Ingredient.of(Tags.Items.INGOTS_IRON),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(FairyItems.feyEssence), Ingredient.of(Tags.Items.INGOTS_GOLD), Ingredient.of(NostrumTags.Items.CrystalSmall)},
				new ResearchRequirement(FairyResearches.ID_Fairy_Instruments),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.HARP)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_instrument_ocarina",
				FairyInstrument.create(InstrumentType.OCARINA),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				Ingredient.of(Blocks.SMOOTH_STONE),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(FairyItems.feyEssence), Ingredient.of(Items.SUGAR_CANE), Ingredient.of(NostrumTags.Items.CrystalSmall)},
				new ResearchRequirement(FairyResearches.ID_Fairy_Instruments),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.OCARINA)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_gael_aggressive",
				FairyGael.create(FairyGaelType.ATTACK, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				Ingredient.of(FeySoulStone.createFake(ResidentType.FAIRY)),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(NostrumItems.thanoPendant), Ingredient.of(Tags.Items.GLASS), Ingredient.of(NostrumTags.Items.ReagentSkyAsh)},
				new ResearchRequirement(FairyResearches.ID_Fairy_Gael_Aggressive),
				new OutcomeConstructGael(FairyGaelType.ATTACK))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_gael_logistics",
				FairyGael.create(FairyGaelType.LOGISTICS, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				Ingredient.of(FeySoulStone.createFake(ResidentType.FAIRY)),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Tags.Items.GLASS), Ingredient.of(NostrumTags.Items.ReagentSkyAsh)},
				new ResearchRequirement(FairyResearches.ID_Fairy_Gael_Logistics),
				new OutcomeConstructGael(FairyGaelType.LOGISTICS))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_gael_construction",
				FairyGael.create(FairyGaelType.BUILD, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				Ingredient.of(FeySoulStone.createFake(ResidentType.FAIRY)),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL), Ingredient.of(Tags.Items.GLASS), Ingredient.of(NostrumTags.Items.ReagentSkyAsh)},
				new ResearchRequirement(FairyResearches.ID_Fairy_Gael_Construction),
				new OutcomeConstructGael(FairyGaelType.BUILD))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("logistics_tokens",
				new ItemStack(FairyItems.feyLogicToken, 4),
				EMagicElement.FIRE,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				Ingredient.of(FairyItems.feyGolemToken),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.MagicToken), Ingredient.of(Items.REDSTONE), Ingredient.of(NostrumTags.Items.CrystalSmall), Ingredient.of(NostrumTags.Items.MagicToken)},
				new ResearchRequirement(FairyResearches.ID_Logistics),
				new OutcomeSpawnItem(new ItemStack(FairyItems.feyLogicToken, 4)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("gnome_home",
				new ItemStack(FairyBlocks.gnomeHome),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				Ingredient.of(FeySoulStone.createFake(ResidentType.GNOME)),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(NostrumTags.Items.CrystalLarge), Ingredient.of(Items.CLAY_BALL), Ingredient.of(FairyItems.feyLogicToken)},
				new ResearchRequirement(FairyResearches.ID_Gnomes),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.gnomeHome), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("elf_home",
				new ItemStack(FairyBlocks.elfHome),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				Ingredient.of(FeySoulStone.createFake(ResidentType.ELF)),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(NostrumTags.Items.CrystalLarge), Ingredient.of(ItemTags.SAPLINGS), Ingredient.of(FairyItems.feyLogicToken)},
				new ResearchRequirement(FairyResearches.ID_Elves),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.elfHome), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("dwarf_home",
				new ItemStack(FairyBlocks.dwarfHome),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				Ingredient.of(FeySoulStone.createFake(ResidentType.DWARF)),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(NostrumTags.Items.CrystalLarge), Ingredient.of(Tags.Items.STONE), Ingredient.of(FairyItems.feyLogicToken)},
				new ResearchRequirement(FairyResearches.ID_Dwarves),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.dwarfHome), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_home",
				new ItemStack(FairyBlocks.fairyHome),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				Ingredient.of(FeySoulStone.createFake(ResidentType.FAIRY)),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(NostrumTags.Items.CrystalLarge), Ingredient.of(Items.GLASS_BOTTLE), Ingredient.of(FairyItems.feyLogicToken)},
				new ResearchRequirement(FairyResearches.ID_Fairies),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.fairyHome), FeySoulStone.create(SoulStoneType.GAEL)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_specialization.emerald",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				Ingredient.of(FeySoulStone.createFake(ResidentType.ELF)),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.EnderBristle), Ingredient.of(NostrumTags.Items.CrystalMedium), Ingredient.of(Blocks.VINE), Ingredient.of(FairyItems.feyLogicToken)},
				new ResearchRequirement(FairyResearches.ID_Elves),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_specialization.garnet",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				Ingredient.of(FeySoulStone.createFake(ResidentType.GNOME)),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.WispPebble), Ingredient.of(NostrumTags.Items.CrystalMedium), Ingredient.of(Items.FLINT), Ingredient.of(FairyItems.feyLogicToken)},
				new ResearchRequirement(FairyResearches.ID_Gnomes),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_specialization.aquamarine",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				Ingredient.of(FeySoulStone.createFake(ResidentType.DWARF)),
				new Ingredient[] {Ingredient.of(NostrumTags.Items.SpriteCore), Ingredient.of(NostrumTags.Items.CrystalMedium), Ingredient.of(Tags.Items.OBSIDIAN), Ingredient.of(FairyItems.feyLogicToken)},
				new ResearchRequirement(FairyResearches.ID_Dwarves),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_upgrade.up.ruby",
				FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.RUBY, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				Ingredient.of(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.of(FairyItems.feyEssence), Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(ginsengFlower), Ingredient.of(FairyItems.feyFlower)},
				new ResearchRequirement(FairyResearches.ID_Logistics),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.RUBY, 1)))
			);

    	registry.register(
			RitualRecipe.createTier3("fey_upgrade.down.ruby",
				FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				Ingredient.of(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.of(FairyItems.feyEssence), Ingredient.of(FairyItems.feyGolemToken), Ingredient.of(ginsengFlower), Ingredient.of(FairyItems.feyTears)},
				new ResearchRequirement(FairyResearches.ID_Logistics),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY, 1)))
			);

    	registry.register(
			RitualRecipe.createTier3("fey_upgrade.up.sapphire",
				FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				Ingredient.of(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.of(FairyItems.feyEssence), Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(mandrakeFlower), Ingredient.of(NostrumTags.Items.WispPebble)},
				new ResearchRequirement(FairyResearches.ID_Logistics),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE, 1)))
			);

    	registry.register(
			RitualRecipe.createTier3("fey_upgrade.down.sapphire",
				FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.SAPPHIRE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				Ingredient.of(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.of(FairyItems.feyEssence), Ingredient.of(FairyItems.feyGolemToken), Ingredient.of(mandrakeFlower), Ingredient.of(NostrumTags.Items.SpriteCore)},
				new ResearchRequirement(FairyResearches.ID_Logistics),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.SAPPHIRE, 1)))
			);

    	registry.register(
			RitualRecipe.createTier3("template_wand",
				new ItemStack(FairyItems.templateWand),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
				Ingredient.of(NostrumItems.mageStaff),
				new Ingredient[] {Ingredient.of(NostrumBlocks.mimicFacade), Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(NostrumTags.Items.EnderBristle), Ingredient.of(NostrumBlocks.mimicFacade)},
				new ResearchRequirement(FairyResearches.ID_Logistics_Construction),
				new OutcomeSpawnItem(new ItemStack(FairyItems.templateWand)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_storage",
				new ItemStack(FairyBlocks.storageChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(Tags.Items.CHESTS),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(Tags.Items.DYES_BLUE), Ingredient.of(FairyItems.feyLogicToken), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Items),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.storageChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_buffer",
				new ItemStack(FairyBlocks.bufferChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(Tags.Items.CHESTS),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(Tags.Items.DYES_YELLOW), Ingredient.of(FairyItems.feyLogicToken), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Items_Adv),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.bufferChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_output",
				new ItemStack(FairyBlocks.outputChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(Tags.Items.CHESTS),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(Tags.Items.DYES_RED), Ingredient.of(FairyItems.feyLogicToken), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Items_Adv),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.outputChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_input",
				new ItemStack(FairyBlocks.inputChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(Tags.Items.CHESTS),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(Tags.Items.DYES_GREEN), Ingredient.of(FairyItems.feyLogicToken), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Items_Adv),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.inputChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lpanel_output",
				new ItemStack(FairyBlocks.outputPanel),
				null,
				new ReagentType[] {ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST},
				Ingredient.of(FairyBlocks.outputChest),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(FairyItems.feyLogicToken), Ingredient.EMPTY, Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Items_Adv),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.outputPanel, 4)))
			);

    	registry.register(
			RitualRecipe.createTier3("logistics_pylon",
				new ItemStack(FairyBlocks.logisticsPylon),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(Tags.Items.GEMS_EMERALD),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Tags.Items.STONE), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Relays),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.logisticsPylon, 2)))
			);

    	registry.register(
			RitualRecipe.createTier3("storage_monitor",
				new ItemStack(FairyBlocks.storageMonitor),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
				Ingredient.of(NostrumBlocks.mirrorBlock),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Tags.Items.CHESTS), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Sensors),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.storageMonitor)))
			);

    	registry.register(
			RitualRecipe.createTier3("storage_sensor",
				new ItemStack(FairyBlocks.logisticsSensor),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG},
				Ingredient.of(FairyBlocks.storageMonitor),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE), Ingredient.of(Items.REDSTONE), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Sensors),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.logisticsSensor)))
			);

    	registry.register(
			RitualRecipe.createTier3("farming_block",
				new ItemStack(FairyBlocks.farmingBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.of(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Items.WATER_BUCKET), Ingredient.of(Items.DIAMOND_HOE), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement(FairyResearches.ID_Gnomes),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.farmingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("gathering_block",
				new ItemStack(FairyBlocks.gatheringBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.of(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Items.LEATHER), Ingredient.of(NostrumItems.reagentBag), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement(FairyResearches.ID_Gathering_Blocks),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.gatheringBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("woodcutting_block",
				new ItemStack(FairyBlocks.woodcuttingBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.of(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(ItemTags.LEAVES), Ingredient.of(Items.DIAMOND_AXE), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement(FairyResearches.ID_Elves),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.woodcuttingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("mining_block",
				new ItemStack(FairyBlocks.miningBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.of(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Tags.Items.OBSIDIAN), Ingredient.of(Items.DIAMOND_PICKAXE), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement(FairyResearches.ID_Dwarves),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.miningBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("building_block",
				new ItemStack(FairyBlocks.buildingBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.of(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Tags.Items.CHESTS), Ingredient.of(Blocks.BRICKS), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement(FairyResearches.ID_BuildingBlocks),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.buildingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("crafting_block.gnome",
				new ItemStack(FairyBlocks.gnomeCraftingBlock),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				Ingredient.of(Blocks.CRAFTING_TABLE),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Blocks.CRAFTING_TABLE), Ingredient.of(Blocks.STONE), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement(FairyResearches.ID_Gnome_Crafting),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.gnomeCraftingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("crafting_block.elf",
				new ItemStack(FairyBlocks.elfCraftingBlock),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				Ingredient.of(ItemTags.LOGS),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Blocks.CRAFTING_TABLE), Ingredient.of(Blocks.STONE), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement(FairyResearches.ID_Elf_Crafting),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.elfCraftingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("crafting_block.dwarf",
				new ItemStack(FairyBlocks.dwarfCraftingBlock),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				Ingredient.of(Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL),
				new Ingredient[] {Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Blocks.CRAFTING_TABLE), Ingredient.of(Blocks.STONE), Ingredient.of(Tags.Items.INGOTS_GOLD)},
				new ResearchRequirement(FairyResearches.ID_Dwarf_Crafting),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.dwarfCraftingBlock)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("soul_jar",
				new ItemStack(FairyItems.soulJar),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG, ReagentType.SKY_ASH},
				Ingredient.of(FairyItems.soulGem),
				new Ingredient[] {Ingredient.of(Items.GHAST_TEAR), Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.of(Tags.Items.ENDER_PEARLS)},
				new ResearchRequirement(FairyResearches.ID_Soul_Jars),
				new OutcomeSpawnItem(new ItemStack(FairyItems.soulJar)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_reinforced_iron_upgrade", "lchest_reinforced_iron",
				new ItemStack(FairyBlocks.reinforcedIronChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(FairyBlocks.storageChest),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(Tags.Items.CHESTS), Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Storage_Adv),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.reinforcedIronChest)))
			);

    	// shortcut iron recipe
    	registry.register(
			RitualRecipe.createTier3("lchest_reinforced_iron",
				new ItemStack(FairyBlocks.reinforcedIronChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON),
				new Ingredient[] {Ingredient.of(Tags.Items.CHESTS), Ingredient.of(Tags.Items.GEMS_LAPIS), Ingredient.of(FairyItems.feyLogicToken), Ingredient.of(Tags.Items.CHESTS)},
				new ResearchRequirement(FairyResearches.ID_Logistics_Storage_Adv),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.reinforcedIronChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_reinforced_gold",
				new ItemStack(FairyBlocks.reinforcedGoldChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(FairyBlocks.reinforcedIronChest),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(Tags.Items.CHESTS), Ingredient.of(Tags.Items.STORAGE_BLOCKS_GOLD), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Storage_Adv),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.reinforcedGoldChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_reinforced_diamond",
				new ItemStack(FairyBlocks.reinforcedDiamondChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.of(FairyBlocks.reinforcedGoldChest),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.of(Tags.Items.CHESTS), Ingredient.of(Tags.Items.STORAGE_BLOCKS_DIAMOND), Ingredient.EMPTY},
				new ResearchRequirement(FairyResearches.ID_Logistics_Storage_Adv),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.reinforcedDiamondChest)))
			);
    }
    
    @SubscribeEvent
    public static void registerIDataSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
    	final IForgeRegistry<DataSerializerEntry> registry = event.getRegistry();
    	
    	registry.register(new DataSerializerEntry(FairyGeneralStatus.instance()).setRegistryName("nostrum.serial.fairy_status"));
    	registry.register(new DataSerializerEntry(ArmPoseDwarf.instance()).setRegistryName("nostrum.serial.dwarf_arm"));
    	registry.register(new DataSerializerEntry(ArmPoseElf.instance()).setRegistryName("nostrum.serial.elf_arm"));
    	registry.register(new DataSerializerEntry(BattleStanceElfArcher.instance()).setRegistryName("nostrum.serial.elf_archer_stance"));
    	registry.register(new DataSerializerEntry(BattleStanceShadowFey.instance()).setRegistryName("nostrum.serial.shadow_fey_stance"));
    	registry.register(new DataSerializerEntry(ArmPoseGnome.instance()).setRegistryName("nostrum.serial.gnome_arm"));
    	registry.register(new DataSerializerEntry(ItemArraySerializer.instance()).setRegistryName("nostrum.serial.itemarray"));
    	registry.register(new DataSerializerEntry(FairyJob.instance()).setRegistryName("nostrum.serial.fairy_job"));
    }
    
    @SubscribeEvent
	public static final void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(INostrumFeyCapability.class);
		event.register(ITemplateViewerCapability.class);
		capabilityHandler = new CapabilityHandler();
	}
}
