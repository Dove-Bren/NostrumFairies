package com.smanzana.nostrumfairies.init;

import com.smanzana.nostrumaetheria.api.proxy.AetheriaIDs;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.capabilities.CapabilityHandler;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.fey.NostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.fey.NostrumFeyCapabilityStorage;
import com.smanzana.nostrumfairies.capabilities.templates.ITemplateViewerCapability;
import com.smanzana.nostrumfairies.capabilities.templates.TemplateViewerCapability;
import com.smanzana.nostrumfairies.capabilities.templates.TemplateViewerCapabilityStorage;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyInstrument;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.items.FairyInstrument.InstrumentType;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry;
import com.smanzana.nostrumfairies.network.NetworkHandler;
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
import com.smanzana.nostrummagica.blocks.NostrumBlocks;
import com.smanzana.nostrummagica.crafting.NostrumTags;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementResearch;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.CapabilityManager;
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
	
	public static NostrumResearchTab researchTab;
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
		researchTab = new NostrumResearchTab("fey", FeySoulStone.create(SoulStoneType.GEM));
		registerDefaultResearch();
		NostrumMagica.instance.registerResearchReloadHook((i) -> {
    		registerDefaultResearch();
    		return 0;
    	});

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
		CapabilityManager.INSTANCE.register(INostrumFeyCapability.class, new NostrumFeyCapabilityStorage(), NostrumFeyCapability::new);
		CapabilityManager.INSTANCE.register(ITemplateViewerCapability.class, new TemplateViewerCapabilityStorage(), TemplateViewerCapability::new);
		capabilityHandler = new CapabilityHandler();
		
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
	
	private static final void registerDefaultResearch() {
		NostrumResearch.startBuilding()
			.lore(FairyItems.feyCorruptedEssence)
			.hiddenParent("rituals")
			.reference("ritual::purify_essence", "ritual.purify_essence.name")
		.build("purify_essence", researchTab, Size.NORMAL, 0, -1, true, FeyResource.create(FeyResourceType.ESSENCE, 1));
		
		NostrumResearch.startBuilding()
			.parent("purify_essence")
			.hiddenParent("kani")
			.reference("ritual::fey_bell", "ritual.fey_bell.name")
		.build("fey_bell", researchTab, Size.NORMAL, -1, 0, true, FeyResource.create(FeyResourceType.BELL, 1));
		
		NostrumResearch.startBuilding()
			.parent("purify_essence")
			.hiddenParent("fey_bell")
			.lore(EntityShadowFey.ShadowFeyConversionLore.instance())
			.reference("ritual::fey_flower", "ritual.fey_flower.name")
		.build("fey_flower", researchTab, Size.NORMAL, 1, 0, true, FeyResource.create(FeyResourceType.FLOWER, 1));
		
		NostrumResearch.startBuilding()
			.parent("purify_essence")
			.parent("fey_flower")
			.parent("fey_bell")
			.lore(FeyResource.FeyFriendLore.instance())
			.reference("ritual::soul_stone", "ritual.soul_stone.name")
			.reference("ritual::soul_gael", "ritual.soul_gael.name")
		.build("fey_souls", researchTab, Size.LARGE, 0, 1, true, FeySoulStone.create(SoulStoneType.GEM));
		
		NostrumResearch.startBuilding()
			.parent("fey_souls")
			.link("fairy_gael_aggressive")
			.reference("ritual::fairy_instrument_lyre", "ritual.fairy_instrument_lyre.name")
			.reference("ritual::fairy_instrument_flute", "ritual.fairy_instrument_flute.name")
			.reference("ritual::fairy_instrument_ocarina", "ritual.fairy_instrument_ocarina.name")
		.build("fairy_instruments", researchTab, Size.GIANT, 2, 1, true, FairyInstrument.create(InstrumentType.FLUTE));
		
		NostrumResearch.startBuilding()
			.parent("fairy_instruments")
			.reference("ritual::fairy_gael_aggressive", "ritual.fairy_gael_aggressive.name")
		.build("fairy_gael_aggressive", researchTab, Size.NORMAL, 4, 1, true, FairyGael.create(FairyGaelType.ATTACK, null));
		
		NostrumResearch.startBuilding()
			.parent("fairy_instruments")
			.hiddenParent("logistics_items")
			.reference("ritual::fairy_gael_logistics", "ritual.fairy_gael_logistics.name")
		.build("fairy_gael_logistics", researchTab, Size.NORMAL, 3, 2, true, FairyGael.create(FairyGaelType.LOGISTICS, null));
		
		NostrumResearch.startBuilding()
			.parent("fairy_instruments")
			.hiddenParent("logistics_construction")
			.reference("ritual::fairy_gael_construction", "ritual.fairy_gael_construction.name")
		.build("fairy_gael_construction", researchTab, Size.NORMAL, 3, 0, true, FairyGael.create(FairyGaelType.BUILD, null));
		
		NostrumResearch.startBuilding()
			.parent("fey_souls")
			.hiddenParent("aether_furnace")
			.hiddenParent("magic_token")
			.reference("ritual::logistics_tokens", "ritual.logistics_tokens.name")
			.reference("ritual::fey_upgrade.down.ruby", "ritual.fey_upgrade.down.ruby.name")
			.reference("ritual::fey_upgrade.up.ruby", "ritual.fey_upgrade.up.ruby.name")
			.reference("ritual::fey_upgrade.down.sapphire", "ritual.fey_upgrade.down.sapphire.name")
			.reference("ritual::fey_upgrade.up.sapphire", "ritual.fey_upgrade.up.sapphire.name")
		.build("logistics", researchTab, Size.GIANT, -2, 1, true, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1));
		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::dwarf_home", "ritual.dwarf_home.name")
			.reference("ritual::mining_block", "ritual.mining_block.name")
			.reference("ritual::fey_specialization.aquamarine", "ritual.fey_specialization.aquamarine.name")
		.build("dwarves", researchTab, Size.LARGE, -4, 0, true, new ItemStack(FairyBlocks.dwarfHome));
		
		// dwarf blocks
		
		NostrumResearch.startBuilding()
			.parent("dwarves")
			.hiddenParent("logistics_construction")
			.reference("ritual::building_block", "ritual.building_block.name")
		.build("building_blocks", researchTab, Size.NORMAL, -4, -1, true, new ItemStack(FairyBlocks.buildingBlock));
		
		NostrumResearch.startBuilding()
			.parent("dwarves")
			.hiddenParent("logistics_crafting")
			.reference("ritual::crafting_block.dwarf", "ritual.crafting_block.dwarf.name")
		.build("dwarf_crafting", researchTab, Size.NORMAL, -5, -1, true, new ItemStack(FairyBlocks.dwarfCraftingBlock));
		
	
		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::elf_home", "ritual.elf_home.name")
			.reference("ritual::woodcutting_block", "ritual.woodcutting_block.name")
			.reference("ritual::fey_specialization.emerald", "ritual.fey_specialization.emerald.name")
		.build("elves", researchTab, Size.LARGE, -3, 0, true, new ItemStack(FairyBlocks.elfHome));
		
		// elf blocks
		
		NostrumResearch.startBuilding()
			.parent("elves")
			.hiddenParent("logistics_crafting")
			.reference("ritual::crafting_block.elf", "ritual.crafting_block.elf.name")
		.build("elf_crafting", researchTab, Size.NORMAL, -3, -1, true, new ItemStack(FairyBlocks.elfCraftingBlock));
		
		
		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::gnome_home", "ritual.gnome_home.name")
			.reference("ritual::farming_block", "ritual.farming_block.name")
			.reference("ritual::fey_specialization.garnet", "ritual.fey_specialization.garnet.name")
		.build("gnomes", researchTab, Size.LARGE, -4, 2, true, new ItemStack(FairyBlocks.gnomeHome));
		
		// gnome blocks
		
		NostrumResearch.startBuilding()
			.parent("gnomes")
			.hiddenParent("logistics_items")
			.reference("ritual::gathering_block", "ritual.gathering_block.name")
		.build("gathering_blocks", researchTab, Size.NORMAL, -4, 3, true, new ItemStack(FairyBlocks.gatheringBlock));
		
		NostrumResearch.startBuilding()
			.parent("gnomes")
			.hiddenParent("logistics_crafting")
			.reference("ritual::crafting_block.gnome", "ritual.crafting_block.gnome.name")
		.build("gnome_crafting", researchTab, Size.NORMAL, -5, 3, true, new ItemStack(FairyBlocks.gnomeCraftingBlock));
		
		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::fairy_home", "ritual.fairy_home.name")
		.build("fairies", researchTab, Size.LARGE, -3, 2, true, new ItemStack(FairyBlocks.fairyHome));
	
		NostrumResearch.startBuilding()
			.hiddenParent("logistics")
			.hiddenParent("fairies")
			.hiddenParent("fairy_instruments")
			.reference("ritual::lchest_storage", "ritual.lchest_storage.name")
		.build("logistics_items", researchTab, Size.GIANT, 0, 3, true, new ItemStack(FairyBlocks.storageChest));
	
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.reference("ritual::lchest_buffer", "ritual.lchest_buffer.name")
			.reference("ritual::lchest_output", "ritual.lchest_output.name")
			.reference("ritual::lchest_input", "ritual.lchest_input.name")
		.build("adv_logistics_items", researchTab, Size.LARGE, 0, 4, true, new ItemStack(FairyBlocks.outputChest));
	
		NostrumResearch.startBuilding()
			.parent("adv_logistics_items")
			.hiddenParent("logistics_sensors")
			.reference("ritual::lchest_reinforced_iron", "ritual.lchest_reinforced_iron.name")
			.reference("ritual::lchest_reinforced_gold", "ritual.lchest_reinforced_gold.name")
			.reference("ritual::lchest_reinforced_diamond", "ritual.lchest_reinforced_diamond.name")
		.build("adv_logistics_storage", researchTab, Size.LARGE, 0, 5, true, new ItemStack(FairyBlocks.reinforcedGoldChest));
		
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.hiddenParent("dwarves")
			.hiddenParent("geogems")
			.hiddenParent("magicfacade")
			.reference("ritual::template_wand", "ritual.template_wand.name")
		.build("logistics_construction", researchTab, Size.LARGE, 1, 4, true, new ItemStack(FairyItems.templateWand));
		
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.hiddenParent("gnomes")
			.hiddenParent("dwarves")
			.hiddenParent("elves")
			.hiddenParent("geogems")
			.link("dwarf_crafting")
		.build("logistics_crafting", researchTab, Size.LARGE, -1, 4, true, new ItemStack(FairyBlocks.dwarfCraftingBlock));
		
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.hiddenParent("geogems")
			.reference("ritual::logistics_pylon", "ritual.logistics_pylon.name")
		.build("logistics_relays", researchTab, Size.NORMAL, -1, 2, true, new ItemStack(FairyBlocks.logisticsPylon));
		
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.reference("ritual::storage_monitor", "ritual.storage_monitor.name")
			.reference("ritual::storage_sensor", "ritual.storage_sensor.name")
		.build("logistics_sensors", researchTab, Size.NORMAL, 1, 2, true, new ItemStack(FairyBlocks.storageMonitor));
		
		NostrumResearch.startBuilding()
			.hiddenParent("fey_souls")
			.reference("ritual::soul_jar", "ritual.soul_jar.name")
		.build("soul_jars", researchTab, Size.NORMAL, 0, 2, true, new ItemStack(FairyItems.soulJar));
	}
	
	private static final void registerLore() {
    	LoreRegistry.instance().register(EntityShadowFey.ShadowFeyConversionLore.instance());
    	LoreRegistry.instance().register(FeyResource.FeyFriendLore.instance());
    }
	
	// Aetheria items
	@ObjectHolder("nostrumaetheria:" + AetheriaIDs.GINSENG_FLOWER) public static Item ginsengFlower;
	@ObjectHolder("nostrumaetheria:" + AetheriaIDs.MANDRAKE_FLOWER) public static Item mandrakeFlower;
    
    public static final void registerDefaultRituals(RitualRegistry.RitualRegisterEvent event) {
		final RitualRegistry registry = event.registry;
    	
    	registry.register(
			RitualRecipe.createTier3("purify_essence",
				FeyResource.create(FeyResourceType.ESSENCE, 1),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyCorruptedEssence), Ingredient.fromItems(Items.WATER_BUCKET), Ingredient.fromItems(FairyItems.feyCorruptedEssence), Ingredient.fromItems(FairyItems.feyCorruptedEssence)},
				new RRequirementResearch("purify_essence"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.ESSENCE, 3)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_bell",
				FeyResource.create(FeyResourceType.BELL, 1),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(Tags.Items.INGOTS_GOLD),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromTag(NostrumTags.Items.CrystalMedium), Ingredient.fromTag(Tags.Items.INGOTS_GOLD), Ingredient.fromItems(FairyItems.feyEssence)},
				new RRequirementResearch("fey_bell"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.BELL, 1)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_flower",
				FeyResource.create(FeyResourceType.FLOWER, 1),
				EMagicElement.EARTH,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH, ReagentType.SKY_ASH},
				Ingredient.fromItems(Blocks.TALL_GRASS),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyTears), Ingredient.fromItems(Items.MELON), Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromItems(FairyItems.feyTears)},
				new RRequirementResearch("fey_flower"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.FLOWER, 4)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("soul_stone",
				FeySoulStone.create(SoulStoneType.GEM),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH},
				Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.INGOTS_GOLD), Ingredient.fromItems(FairyItems.feyTears), Ingredient.fromItems(FairyBlocks.feyBush), Ingredient.fromItems(FairyItems.feyCorruptedEssence)},
				new RRequirementResearch("fey_souls"),
				new OutcomeSpawnItem(FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("soul_gael",
				FeySoulStone.create(SoulStoneType.GAEL),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH},
				Ingredient.fromTag(NostrumTags.Items.CrystalMedium),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.DUSTS_GLOWSTONE), Ingredient.fromItems(FairyItems.feyTears), Ingredient.fromItems(FairyBlocks.feyBush), Ingredient.fromItems(FairyItems.feyCorruptedEssence)},
				new RRequirementResearch("fey_souls"),
				new OutcomeSpawnItem(FeySoulStone.create(SoulStoneType.GAEL)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_instrument_flute",
				FairyInstrument.create(InstrumentType.FLUTE),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				Ingredient.fromTag(ItemTags.LOGS),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromItems(Items.SUGAR_CANE), Ingredient.fromTag(NostrumTags.Items.CrystalSmall)},
				new RRequirementResearch("fairy_instruments"),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.FLUTE)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_instrument_lyre",
				FairyInstrument.create(InstrumentType.HARP),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				Ingredient.fromTag(Tags.Items.INGOTS_IRON),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromTag(Tags.Items.INGOTS_GOLD), Ingredient.fromTag(NostrumTags.Items.CrystalSmall)},
				new RRequirementResearch("fairy_instruments"),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.HARP)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_instrument_ocarina",
				FairyInstrument.create(InstrumentType.OCARINA),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				Ingredient.fromItems(Blocks.SMOOTH_STONE),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromItems(Items.SUGAR_CANE), Ingredient.fromTag(NostrumTags.Items.CrystalSmall)},
				new RRequirementResearch("fairy_instruments"),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.OCARINA)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_gael_aggressive",
				FairyGael.create(FairyGaelType.ATTACK, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.FAIRY)),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromItems(NostrumItems.thanoPendant), Ingredient.fromTag(Tags.Items.GLASS), Ingredient.fromTag(NostrumTags.Items.ReagentSkyAsh)},
				new RRequirementResearch("fairy_gael_aggressive"),
				new OutcomeConstructGael(FairyGaelType.ATTACK))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_gael_logistics",
				FairyGael.create(FairyGaelType.LOGISTICS, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.FAIRY)),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(Tags.Items.GLASS), Ingredient.fromTag(NostrumTags.Items.ReagentSkyAsh)},
				new RRequirementResearch("fairy_gael_logistics"),
				new OutcomeConstructGael(FairyGaelType.LOGISTICS))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_gael_construction",
				FairyGael.create(FairyGaelType.BUILD, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.FAIRY)),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromItems(Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL), Ingredient.fromTag(Tags.Items.GLASS), Ingredient.fromTag(NostrumTags.Items.ReagentSkyAsh)},
				new RRequirementResearch("fairy_gael_construction"),
				new OutcomeConstructGael(FairyGaelType.BUILD))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("logistics_tokens",
				FeyResource.create(FeyResourceType.LOGIC_TOKEN, 4),
				EMagicElement.FIRE,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				Ingredient.fromItems(FairyItems.feyGolemToken),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.MagicToken), Ingredient.fromItems(Items.REDSTONE), Ingredient.fromTag(NostrumTags.Items.CrystalSmall), Ingredient.fromTag(NostrumTags.Items.MagicToken)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.LOGIC_TOKEN, 4)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("gnome_home",
				new ItemStack(FairyBlocks.gnomeHome),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.GNOME)),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(NostrumTags.Items.CrystalLarge), Ingredient.fromItems(Items.CLAY_BALL), Ingredient.fromItems(FairyItems.feyLogicToken)},
				new RRequirementResearch("gnomes"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.gnomeHome), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("elf_home",
				new ItemStack(FairyBlocks.elfHome),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.ELF)),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(NostrumTags.Items.CrystalLarge), Ingredient.fromTag(ItemTags.SAPLINGS), Ingredient.fromItems(FairyItems.feyLogicToken)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.elfHome), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("dwarf_home",
				new ItemStack(FairyBlocks.dwarfHome),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.DWARF)),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(NostrumTags.Items.CrystalLarge), Ingredient.fromTag(Tags.Items.STONE), Ingredient.fromItems(FairyItems.feyLogicToken)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.dwarfHome), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fairy_home",
				new ItemStack(FairyBlocks.fairyHome),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.FAIRY)),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(NostrumTags.Items.CrystalLarge), Ingredient.fromItems(Items.GLASS_BOTTLE), Ingredient.fromItems(FairyItems.feyLogicToken)},
				new RRequirementResearch("fairies"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.fairyHome), FeySoulStone.create(SoulStoneType.GAEL)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_specialization.emerald",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.ELF)),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.EnderBristle), Ingredient.fromTag(NostrumTags.Items.CrystalMedium), Ingredient.fromItems(Blocks.VINE), Ingredient.fromItems(FairyItems.feyLogicToken)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_specialization.garnet",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.GNOME)),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.WispPebble), Ingredient.fromTag(NostrumTags.Items.CrystalMedium), Ingredient.fromItems(Items.FLINT), Ingredient.fromItems(FairyItems.feyLogicToken)},
				new RRequirementResearch("gnomes"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_specialization.aquamarine",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				Ingredient.fromStacks(FeySoulStone.createFake(ResidentType.DWARF)),
				new Ingredient[] {Ingredient.fromTag(NostrumTags.Items.SpriteCore), Ingredient.fromTag(NostrumTags.Items.CrystalMedium), Ingredient.fromTag(Tags.Items.OBSIDIAN), Ingredient.fromItems(FairyItems.feyLogicToken)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("fey_upgrade.up.ruby",
				FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.RUBY, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromItems(ginsengFlower), Ingredient.fromItems(FairyItems.feyFlower)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.RUBY, 1)))
			);

    	registry.register(
			RitualRecipe.createTier3("fey_upgrade.down.ruby",
				FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromItems(FairyItems.feyGolemToken), Ingredient.fromItems(ginsengFlower), Ingredient.fromItems(FairyItems.feyTears)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY, 1)))
			);

    	registry.register(
			RitualRecipe.createTier3("fey_upgrade.up.sapphire",
				FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromItems(mandrakeFlower), Ingredient.fromTag(NostrumTags.Items.WispPebble)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE, 1)))
			);

    	registry.register(
			RitualRecipe.createTier3("fey_upgrade.down.sapphire",
				FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.SAPPHIRE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				Ingredient.fromTag(NostrumTags.Items.CrystalSmall),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyEssence), Ingredient.fromItems(FairyItems.feyGolemToken), Ingredient.fromItems(mandrakeFlower), Ingredient.fromTag(NostrumTags.Items.SpriteCore)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.SAPPHIRE, 1)))
			);

    	registry.register(
			RitualRecipe.createTier3("template_wand",
				new ItemStack(FairyItems.templateWand),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
				Ingredient.fromItems(NostrumItems.mageStaff),
				new Ingredient[] {Ingredient.fromItems(NostrumBlocks.mimicFacade), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(NostrumTags.Items.EnderBristle), Ingredient.fromItems(NostrumBlocks.mimicFacade)},
				new RRequirementResearch("logistics_construction"),
				new OutcomeSpawnItem(new ItemStack(FairyItems.templateWand)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_storage",
				new ItemStack(FairyBlocks.storageChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromTag(Tags.Items.CHESTS),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.DYES_BLUE), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.EMPTY},
				new RRequirementResearch("logistics_items"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.storageChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_buffer",
				new ItemStack(FairyBlocks.bufferChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromTag(Tags.Items.CHESTS),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.DYES_YELLOW), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.EMPTY},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.bufferChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_output",
				new ItemStack(FairyBlocks.outputChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromTag(Tags.Items.CHESTS),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.DYES_RED), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.EMPTY},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.outputChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_input",
				new ItemStack(FairyBlocks.inputChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromTag(Tags.Items.CHESTS),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.DYES_GREEN), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.EMPTY},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.inputChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lpanel_output",
				new ItemStack(FairyBlocks.outputPanel),
				null,
				new ReagentType[] {ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST},
				Ingredient.fromItems(FairyBlocks.outputChest),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.EMPTY, Ingredient.EMPTY},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.outputPanel, 4)))
			);

    	registry.register(
			RitualRecipe.createTier3("logistics_pylon",
				new ItemStack(FairyBlocks.logisticsPylon),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromTag(Tags.Items.GEMS_EMERALD),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(Tags.Items.STONE), Ingredient.EMPTY},
				new RRequirementResearch("logistics_relays"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.logisticsPylon, 2)))
			);

    	registry.register(
			RitualRecipe.createTier3("storage_monitor",
				new ItemStack(FairyBlocks.storageMonitor),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
				Ingredient.fromItems(NostrumItems.mirrorItem),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(Tags.Items.CHESTS), Ingredient.EMPTY},
				new RRequirementResearch("logistics_sensors"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.storageMonitor)))
			);

    	registry.register(
			RitualRecipe.createTier3("storage_sensor",
				new ItemStack(FairyBlocks.logisticsSensor),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG},
				Ingredient.fromItems(FairyBlocks.storageMonitor),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_REDSTONE), Ingredient.fromItems(Items.REDSTONE), Ingredient.EMPTY},
				new RRequirementResearch("logistics_sensors"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.logisticsSensor)))
			);

    	registry.register(
			RitualRecipe.createTier3("farming_block",
				new ItemStack(FairyBlocks.farmingBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromItems(Items.WATER_BUCKET), Ingredient.fromItems(Items.DIAMOND_HOE), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("gnomes"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.farmingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("gathering_block",
				new ItemStack(FairyBlocks.gatheringBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromItems(Items.LEATHER), Ingredient.fromItems(NostrumItems.reagentBag), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("gathering_blocks"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.gatheringBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("woodcutting_block",
				new ItemStack(FairyBlocks.woodcuttingBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(ItemTags.LEAVES), Ingredient.fromItems(Items.DIAMOND_AXE), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.woodcuttingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("mining_block",
				new ItemStack(FairyBlocks.miningBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(Tags.Items.OBSIDIAN), Ingredient.fromItems(Items.DIAMOND_PICKAXE), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.miningBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("building_block",
				new ItemStack(FairyBlocks.buildingBlock),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				Ingredient.fromTag(ItemTags.SIGNS),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(Tags.Items.CHESTS), Ingredient.fromItems(Blocks.BRICKS), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("building_blocks"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.buildingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("crafting_block.gnome",
				new ItemStack(FairyBlocks.gnomeCraftingBlock),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				Ingredient.fromItems(Blocks.CRAFTING_TABLE),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromItems(Blocks.CRAFTING_TABLE), Ingredient.fromItems(Blocks.STONE), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("gnome_crafting"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.gnomeCraftingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("crafting_block.elf",
				new ItemStack(FairyBlocks.elfCraftingBlock),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				Ingredient.fromTag(ItemTags.LOGS),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromItems(Blocks.CRAFTING_TABLE), Ingredient.fromItems(Blocks.STONE), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("elf_crafting"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.elfCraftingBlock)))
			);

    	registry.register(
			RitualRecipe.createTier3("crafting_block.dwarf",
				new ItemStack(FairyBlocks.dwarfCraftingBlock),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				Ingredient.fromItems(Items.ANVIL, Items.CHIPPED_ANVIL, Items.DAMAGED_ANVIL),
				new Ingredient[] {Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromItems(Blocks.CRAFTING_TABLE), Ingredient.fromItems(Blocks.STONE), Ingredient.fromTag(Tags.Items.INGOTS_GOLD)},
				new RRequirementResearch("dwarf_crafting"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.dwarfCraftingBlock)))
			);
    	
    	registry.register(
			RitualRecipe.createTier3("soul_jar",
				new ItemStack(FairyItems.soulJar),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG, ReagentType.SKY_ASH},
				Ingredient.fromItems(FairyItems.soulGem),
				new Ingredient[] {Ingredient.fromItems(Items.GHAST_TEAR), Ingredient.EMPTY, Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.ENDER_PEARLS)},
				new RRequirementResearch("soul_jars"),
				new OutcomeSpawnItem(new ItemStack(FairyItems.soulJar)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_reinforced_iron_upgrade", "lchest_reinforced_iron",
				new ItemStack(FairyBlocks.reinforcedIronChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromItems(FairyBlocks.storageChest),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.CHESTS), Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON), Ingredient.EMPTY},
				new RRequirementResearch("adv_logistics_storage"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.reinforcedIronChest)))
			);

    	// shortcut iron recipe
    	registry.register(
			RitualRecipe.createTier3("lchest_reinforced_iron",
				new ItemStack(FairyBlocks.reinforcedIronChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_IRON),
				new Ingredient[] {Ingredient.fromTag(Tags.Items.CHESTS), Ingredient.fromTag(Tags.Items.GEMS_LAPIS), Ingredient.fromItems(FairyItems.feyLogicToken), Ingredient.fromTag(Tags.Items.CHESTS)},
				new RRequirementResearch("adv_logistics_storage"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.reinforcedIronChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_reinforced_gold",
				new ItemStack(FairyBlocks.reinforcedGoldChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromItems(FairyBlocks.reinforcedIronChest),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.CHESTS), Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_GOLD), Ingredient.EMPTY},
				new RRequirementResearch("adv_logistics_storage"),
				new OutcomeSpawnItem(new ItemStack(FairyBlocks.reinforcedGoldChest)))
			);

    	registry.register(
			RitualRecipe.createTier3("lchest_reinforced_diamond",
				new ItemStack(FairyBlocks.reinforcedDiamondChest),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				Ingredient.fromItems(FairyBlocks.reinforcedGoldChest),
				new Ingredient[] {Ingredient.EMPTY, Ingredient.fromTag(Tags.Items.CHESTS), Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_DIAMOND), Ingredient.EMPTY},
				new RRequirementResearch("adv_logistics_storage"),
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
}
