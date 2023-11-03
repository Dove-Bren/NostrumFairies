package com.smanzana.nostrumfairies.proxy;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.BufferLogisticsChest;
import com.smanzana.nostrumfairies.blocks.BuildingBlock;
import com.smanzana.nostrumfairies.blocks.CraftingBlockDwarf;
import com.smanzana.nostrumfairies.blocks.CraftingBlockElf;
import com.smanzana.nostrumfairies.blocks.CraftingBlockGnome;
import com.smanzana.nostrumfairies.blocks.FarmingBlock;
import com.smanzana.nostrumfairies.blocks.FeyBush;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
import com.smanzana.nostrumfairies.blocks.GatheringBlock;
import com.smanzana.nostrumfairies.blocks.InputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.LogisticsPylon;
import com.smanzana.nostrumfairies.blocks.LogisticsSensorBlock;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.blocks.MiningBlock;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsPanel;
import com.smanzana.nostrumfairies.blocks.ReinforcedStorageLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageMonitor;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.blocks.WoodcuttingBlock;
import com.smanzana.nostrumfairies.capabilities.CapabilityHandler;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.fey.NostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.fey.NostrumFeyCapabilityStorage;
import com.smanzana.nostrumfairies.capabilities.templates.ITemplateViewerCapability;
import com.smanzana.nostrumfairies.capabilities.templates.TemplateViewerCapability;
import com.smanzana.nostrumfairies.capabilities.templates.TemplateViewerCapabilityStorage;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.effect.FeyPotions;
import com.smanzana.nostrumfairies.effect.FeyVisibilityEffect;
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
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.items.FairyInstrument;
import com.smanzana.nostrumfairies.items.FairyInstrument.InstrumentType;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.items.SoulJar;
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilitySyncMessage;
import com.smanzana.nostrumfairies.rituals.outcomes.OutcomeConstructGael;
import com.smanzana.nostrumfairies.serializers.ArmPoseDwarf;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.ArmPoseGnome;
import com.smanzana.nostrumfairies.serializers.BattleStanceElfArcher;
import com.smanzana.nostrumfairies.serializers.BattleStanceShadowFey;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.serializers.FairyJob;
import com.smanzana.nostrumfairies.serializers.ItemArraySerializer;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.tiles.BufferChestTileEntity;
import com.smanzana.nostrumfairies.tiles.BuildingBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.CraftingBlockDwarfTileEntity;
import com.smanzana.nostrumfairies.tiles.CraftingBlockElfTileEntity;
import com.smanzana.nostrumfairies.tiles.CraftingBlockGnomeTileEntity;
import com.smanzana.nostrumfairies.tiles.FarmingBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.GatheringBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.InputChestTileEntity;
import com.smanzana.nostrumfairies.tiles.LogisticsSensorTileEntity;
import com.smanzana.nostrumfairies.tiles.MiningBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.OutputChestTileEntity;
import com.smanzana.nostrumfairies.tiles.OutputPanelTileEntity;
import com.smanzana.nostrumfairies.tiles.PylonTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedDiamondChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedGoldChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedIronChestTileEntity;
import com.smanzana.nostrumfairies.tiles.StorageChestTileEntity;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;
import com.smanzana.nostrumfairies.tiles.TemplateBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.WoodcuttingBlockTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.integration.aetheria.items.AetherResourceType;
import com.smanzana.nostrummagica.integration.aetheria.items.NostrumAetherResourceItem;
import com.smanzana.nostrummagica.items.MageStaff;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.ThanoPendant;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.research.NostrumResearch;
import com.smanzana.nostrummagica.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.rituals.RitualRegistry;
import com.smanzana.nostrummagica.rituals.outcomes.OutcomeSpawnItem;
import com.smanzana.nostrummagica.rituals.requirements.RRequirementResearch;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IDataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxy {
	
	public CapabilityHandler capabilityHandler;
	
	public CommonProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void preinit() {
		CapabilityManager.INSTANCE.register(INostrumFeyCapability.class, new NostrumFeyCapabilityStorage(), NostrumFeyCapability::new);
		CapabilityManager.INSTANCE.register(ITemplateViewerCapability.class, new TemplateViewerCapabilityStorage(), TemplateViewerCapability::new);
		capabilityHandler = new CapabilityHandler();
		NetworkHandler.getInstance();
    	
    	NostrumMagica.instance.registerResearchReloadHook((i) -> {
    		registerResearch();
    		return 0;
    	});
	}
	
	public void init() {
    	NetworkRegistry.INSTANCE.registerGuiHandler(NostrumFairies.instance, new NostrumFairyGui());

    	registerRituals();
    	registerLore();
    	registerResearch();
    	
    	FairyGael.registerRecipes();
	}
	
	public void postinit() {
		TemplateBlock.RegisterBaseOverrides();
	}

	@SubscribeEvent
	public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
		final IForgeRegistry<EntityEntry> registry = event.getRegistry();
		int entityID = 0;
		registry.register(EntityEntryBuilder.create()
				.entity(EntityTestFairy.class)
				.id("test_fairy", entityID++)
				.name(NostrumFairies.MODID + ".test_fairy")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityFairy.class)
				.id("fairy", entityID++)
				.name(NostrumFairies.MODID + ".fairy")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityDwarf.class)
				.id("dwarf", entityID++)
				.name(NostrumFairies.MODID + ".dwarf")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityElf.class)
				.id("elf", entityID++)
				.name(NostrumFairies.MODID + ".elf")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityGnome.class)
				.id("gnome", entityID++)
				.name(NostrumFairies.MODID + ".gnome")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityElfArcher.class)
				.id("elf_archer", entityID++)
				.name(NostrumFairies.MODID + ".elf_archer")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityShadowFey.class)
				.id("shadow_fey", entityID++)
				.name(NostrumFairies.MODID + ".shadow_fey")
				.tracker(128, 1, false)
				.spawn(EnumCreatureType.MONSTER, 35, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.MAGICAL))
				.spawn(EnumCreatureType.MONSTER, 25, 1, 3, BiomeDictionary.getBiomes(BiomeDictionary.Type.FOREST))
				.spawn(EnumCreatureType.MONSTER, 18, 2, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.SPOOKY))
				.spawn(EnumCreatureType.MONSTER, 20, 1, 2, BiomeDictionary.getBiomes(BiomeDictionary.Type.DENSE))
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityPersonalFairy.class)
				.id("personal_fairy", entityID++)
				.name(NostrumFairies.MODID + ".personal_fairy")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityElfCrafter.class)
				.id("elf_crafter", entityID++)
				.name(NostrumFairies.MODID + ".elf_crafter")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityDwarfCrafter.class)
				.id("dwarf_crafter", entityID++)
				.name(NostrumFairies.MODID + ".dwarf_crafter")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityDwarfBuilder.class)
				.id("dwarf_builder", entityID++)
				.name(NostrumFairies.MODID + ".dwarf_builder")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityGnomeCrafter.class)
				.id("gnome_crafter", entityID++)
				.name(NostrumFairies.MODID + ".gnome_crafter")
				.tracker(128, 1, false)
			.build());
		registry.register(EntityEntryBuilder.create()
				.entity(EntityGnomeCollector.class)
				.id("gnome_collector", entityID++)
				.name(NostrumFairies.MODID + ".gnome_collector")
				.tracker(128, 1, false)
			.build());
	}
	
	private static void registerBlockItem(Block block, String registryName, IForgeRegistry<Item> registry) {
		ItemBlock item = new ItemBlock(block);
    	item.setRegistryName(registryName);
    	item.setUnlocalizedName(registryName);
    	item.setCreativeTab(NostrumFairies.creativeTab);
    	registry.register(item);
	}
    
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
    	
		registry.register(FeyStone.instance());
		registry.register(FeyResource.instance());
		registry.register(FeySoulStone.instance());
		registry.register(FairyGael.instance());
		registry.register(FairyInstrument.instance());
		registry.register(TemplateWand.instance());
		registry.register(TemplateScroll.instance());
		registry.register(SoulJar.instance());
		
		registerBlockItem(StorageLogisticsChest.instance(), StorageLogisticsChest.ID, registry);
		registerBlockItem(BufferLogisticsChest.instance(), BufferLogisticsChest.ID, registry);
		registerBlockItem(OutputLogisticsChest.instance(), OutputLogisticsChest.ID, registry);
		registerBlockItem(StorageMonitor.instance(), StorageMonitor.ID, registry);
		registerBlockItem(InputLogisticsChest.instance(), InputLogisticsChest.ID, registry);
		registerBlockItem(GatheringBlock.instance(), GatheringBlock.ID, registry);
		registerBlockItem(LogisticsPylon.instance(), LogisticsPylon.ID, registry);
		registerBlockItem(WoodcuttingBlock.instance(), WoodcuttingBlock.ID, registry);
		registerBlockItem(MiningBlock.instance(), MiningBlock.ID, registry);
		registerBlockItem(FarmingBlock.instance(), FarmingBlock.ID, registry);
		registerBlockItem(BuildingBlock.instance(), BuildingBlock.ID, registry);
		registerBlockItem(CraftingBlockDwarf.instance(), CraftingBlockDwarf.ID, registry);
		registerBlockItem(CraftingBlockElf.instance(), CraftingBlockElf.ID, registry);
		registerBlockItem(CraftingBlockGnome.instance(), CraftingBlockGnome.ID, registry);
		registerBlockItem(LogisticsSensorBlock.instance(), LogisticsSensorBlock.ID, registry);
		registerBlockItem(OutputLogisticsPanel.instance(), OutputLogisticsPanel.ID, registry);
		registerBlockItem(ReinforcedStorageLogisticsChest.Iron(), ReinforcedStorageLogisticsChest.Iron().getID(), registry);
		registerBlockItem(ReinforcedStorageLogisticsChest.Gold(), ReinforcedStorageLogisticsChest.Gold().getID(), registry);
		registerBlockItem(ReinforcedStorageLogisticsChest.Diamond(), ReinforcedStorageLogisticsChest.Diamond().getID(), registry);
    	
    	for (ResidentType type : ResidentType.values()) {
    		registerBlockItem(FeyHomeBlock.instance(type), FeyHomeBlock.ID(type), registry);
    	}
    	
    	// Custom item interaction so do it manually
    	{
    		ItemBlock item = new ItemBlock(FeyBush.instance()) {
    			@Override
				public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
					return ((FeyBush) this.block).getEntityInteraction(stack, playerIn, target, hand);
				}
    		};
        	item.setRegistryName(FeyBush.ID);
        	item.setUnlocalizedName(FeyBush.ID);
        	item.setCreativeTab(NostrumFairies.creativeTab);
        	registry.register(item);
    	}
	}
	
	private static final void registerBlock(Block block, String registryName, IForgeRegistry<Block> registry) {
		block.setRegistryName(registryName);
		registry.register(block);
	}

	@SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();
		
		registerBlock(StorageLogisticsChest.instance(), StorageLogisticsChest.ID, registry);
		registerBlock(BufferLogisticsChest.instance(), BufferLogisticsChest.ID, registry);
		registerBlock(OutputLogisticsChest.instance(), OutputLogisticsChest.ID, registry);
		registerBlock(StorageMonitor.instance(), StorageMonitor.ID, registry);
		registerBlock(InputLogisticsChest.instance(), InputLogisticsChest.ID, registry);
		registerBlock(GatheringBlock.instance(), GatheringBlock.ID, registry);
		registerBlock(LogisticsPylon.instance(), LogisticsPylon.ID, registry);
		registerBlock(WoodcuttingBlock.instance(), WoodcuttingBlock.ID, registry);
		registerBlock(MiningBlock.instance(), MiningBlock.ID, registry);
		registerBlock(MagicLight.Bright(), MagicLight.BrightID, registry);
		registerBlock(MagicLight.Medium(), MagicLight.MediumID, registry);
		registerBlock(MagicLight.Dim(), MagicLight.DimID, registry);
		registerBlock(MagicLight.Unlit(), MagicLight.UnlitID, registry);
		registerBlock(FarmingBlock.instance(), FarmingBlock.ID, registry);
		
    	for (ResidentType type : ResidentType.values()) {
    		registerBlock(FeyHomeBlock.instance(type), FeyHomeBlock.ID(type), registry);
    	}
    	
		registerBlock(FeyBush.instance(), FeyBush.ID, registry);
		registerBlock(TemplateBlock.instance(), TemplateBlock.ID, registry);
		registerBlock(BuildingBlock.instance(), BuildingBlock.ID, registry);
		registerBlock(CraftingBlockDwarf.instance(), CraftingBlockDwarf.ID, registry);
		registerBlock(CraftingBlockElf.instance(), CraftingBlockElf.ID, registry);
		registerBlock(CraftingBlockGnome.instance(), CraftingBlockGnome.ID, registry);
		registerBlock(LogisticsSensorBlock.instance(), LogisticsSensorBlock.ID, registry);
		registerBlock(OutputLogisticsPanel.instance(), OutputLogisticsPanel.ID, registry);
		registerBlock(ReinforcedStorageLogisticsChest.Iron(), ReinforcedStorageLogisticsChest.Iron().getID(), registry);
		registerBlock(ReinforcedStorageLogisticsChest.Gold(), ReinforcedStorageLogisticsChest.Gold().getID(), registry);
		registerBlock(ReinforcedStorageLogisticsChest.Diamond(), ReinforcedStorageLogisticsChest.Diamond().getID(), registry);
		
		registerTileEntities();
    }
	
	private void registerTileEntities() {
		GameRegistry.registerTileEntity(StorageChestTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_storage_chest_te"));
		GameRegistry.registerTileEntity(BufferChestTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_buffer_chest_te"));
		GameRegistry.registerTileEntity(OutputChestTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_output_chest_te"));
		GameRegistry.registerTileEntity(StorageMonitorTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_storage_monitor_te"));
		GameRegistry.registerTileEntity(InputChestTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_input_chest_te"));
		GameRegistry.registerTileEntity(GatheringBlockTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_gathering_block_te"));
		GameRegistry.registerTileEntity(PylonTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_pylon_te"));
		GameRegistry.registerTileEntity(WoodcuttingBlockTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_woodcutting_block_te"));
		GameRegistry.registerTileEntity(MiningBlockTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_mining_block_te"));
		GameRegistry.registerTileEntity(FarmingBlockTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_farming_block_te"));
		GameRegistry.registerTileEntity(HomeBlockTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "home_block_te"));
		GameRegistry.registerTileEntity(TemplateBlockTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "template_block_te"));
		GameRegistry.registerTileEntity(BuildingBlockTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_building_block_te"));
		GameRegistry.registerTileEntity(CraftingBlockDwarfTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_crafting_station_dwarf_te"));
		GameRegistry.registerTileEntity(CraftingBlockElfTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_crafting_station_elf_te"));
		GameRegistry.registerTileEntity(CraftingBlockGnomeTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_crafting_station_gnome_te"));
		GameRegistry.registerTileEntity(LogisticsSensorTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_sensor_te"));
		GameRegistry.registerTileEntity(OutputPanelTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_output_panel_te"));
		GameRegistry.registerTileEntity(ReinforcedIronChestTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_reinforced_chest_iron_te"));
		GameRegistry.registerTileEntity(ReinforcedGoldChestTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_reinforced_chest_gold_te"));
		GameRegistry.registerTileEntity(ReinforcedDiamondChestTileEntity.class, new ResourceLocation(NostrumFairies.MODID, "logistics_reinforced_chest_diamond_te"));
	}
    
    private void registerLore() {
    	LoreRegistry.instance().register(FeyResource.instance());
    	LoreRegistry.instance().register(FeyStone.instance());
    	LoreRegistry.instance().register(FairyGael.instance());
    	LoreRegistry.instance().register(FairyInstrument.instance());
    	LoreRegistry.instance().register(FeySoulStone.instance());
    	LoreRegistry.instance().register(TemplateWand.instance());
    	LoreRegistry.instance().register(TemplateScroll.instance());
    	//LoreRegistry.instance().register(new EntityShadowFey());
    	LoreRegistry.instance().register(EntityShadowFey.ShadowFeyConversionLore.instance());
    	LoreRegistry.instance().register(FeyResource.FeyFriendLore.instance());
    	LoreRegistry.instance().register(SoulJar.instance());
    }
    
    private void registerRituals() {
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("purify_essence",
				FeyResource.create(FeyResourceType.ESSENCE, 1),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
				new ItemStack[] {FeyResource.create(FeyResourceType.ESSENCE_CORRUPTED, 1), new ItemStack(Items.WATER_BUCKET), FeyResource.create(FeyResourceType.ESSENCE_CORRUPTED, 1), FeyResource.create(FeyResourceType.ESSENCE_CORRUPTED, 1)},
				new RRequirementResearch("purify_essence"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.ESSENCE, 3)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_bell",
				FeyResource.create(FeyResourceType.BELL, 1),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANI_DUST, ReagentType.SPIDER_SILK},
				new ItemStack(Items.GOLD_INGOT),
				new ItemStack[] {FeyResource.create(FeyResourceType.ESSENCE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Items.GOLD_INGOT), FeyResource.create(FeyResourceType.ESSENCE, 1)},
				new RRequirementResearch("fey_bell"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.BELL, 1)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_flower",
				FeyResource.create(FeyResourceType.FLOWER, 1),
				EMagicElement.EARTH,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH, ReagentType.SKY_ASH},
				new ItemStack(Blocks.TALLGRASS, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {FeyResource.create(FeyResourceType.TEARS, 1), new ItemStack(Items.MELON), FeyResource.create(FeyResourceType.ESSENCE, 1), FeyResource.create(FeyResourceType.TEARS, 1)},
				new RRequirementResearch("fey_flower"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.FLOWER, 4)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("soul_stone",
				FeySoulStone.create(SoulStoneType.GEM),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1),
				new ItemStack[] {new ItemStack(Items.GOLD_INGOT), FeyResource.create(FeyResourceType.TEARS, 1), new ItemStack(FeyBush.instance()), FeyResource.create(FeyResourceType.ESSENCE_CORRUPTED, 1)},
				new RRequirementResearch("fey_souls"),
				new OutcomeSpawnItem(FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("soul_gael",
				FeySoulStone.create(SoulStoneType.GAEL),
				EMagicElement.ICE,
				new ReagentType[] {ReagentType.MANDRAKE_ROOT, ReagentType.BLACK_PEARL, ReagentType.BLACK_PEARL, ReagentType.SKY_ASH},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1),
				new ItemStack[] {new ItemStack(Items.GLOWSTONE_DUST), FeyResource.create(FeyResourceType.TEARS, 1), new ItemStack(FeyBush.instance()), FeyResource.create(FeyResourceType.ESSENCE_CORRUPTED, 1)},
				new RRequirementResearch("fey_souls"),
				new OutcomeSpawnItem(FeySoulStone.create(SoulStoneType.GAEL)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_instrument_flute",
				FairyInstrument.create(InstrumentType.FLUTE),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				new ItemStack(Blocks.LOG, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), FeyResource.create(FeyResourceType.ESSENCE, 1), new ItemStack(Items.REEDS), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("fairy_instruments"),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.FLUTE)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_instrument_lyre",
				FairyInstrument.create(InstrumentType.HARP),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				new ItemStack(Items.IRON_INGOT),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), FeyResource.create(FeyResourceType.ESSENCE, 1), new ItemStack(Items.GOLD_INGOT), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("fairy_instruments"),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.HARP)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_instrument_ocarina",
				FairyInstrument.create(InstrumentType.OCARINA),
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SKY_ASH},
				new ItemStack(Blocks.STONE),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), FeyResource.create(FeyResourceType.ESSENCE, 1), new ItemStack(Items.REEDS), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1)},
				new RRequirementResearch("fairy_instruments"),
				new OutcomeSpawnItem(FairyInstrument.create(InstrumentType.OCARINA)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_gael_aggressive",
				FairyGael.create(FairyGaelType.ATTACK, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.FAIRY),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(ThanoPendant.instance()), new ItemStack(Blocks.GLASS), ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1)},
				new RRequirementResearch("fairy_gael_aggressive"),
				new OutcomeConstructGael(FairyGaelType.ATTACK))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_gael_logistics",
				FairyGael.create(FairyGaelType.LOGISTICS, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.FAIRY),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.GLASS), ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1)},
				new RRequirementResearch("fairy_gael_logistics"),
				new OutcomeConstructGael(FairyGaelType.LOGISTICS))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_gael_construction",
				FairyGael.create(FairyGaelType.BUILD, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.FAIRY),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(Blocks.ANVIL), new ItemStack(Blocks.GLASS), ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1)},
				new RRequirementResearch("fairy_gael_construction"),
				new OutcomeConstructGael(FairyGaelType.BUILD))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("logistics_tokens",
				FeyResource.create(FeyResourceType.LOGIC_TOKEN, 4),
				EMagicElement.FIRE,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				FeyResource.create(FeyResourceType.GOLEM_TOKEN, 1),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.TOKEN, 1), new ItemStack(Items.REDSTONE), NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), NostrumResourceItem.getItem(ResourceType.TOKEN, 1)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.LOGIC_TOKEN, 4)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("gnome_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.GNOME)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				FeySoulStone.createFake(ResidentType.GNOME),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Items.CLAY_BALL), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("gnomes"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.GNOME)), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("elf_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.ELF)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				FeySoulStone.createFake(ResidentType.ELF),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Blocks.SAPLING, 1, OreDictionary.WILDCARD_VALUE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.ELF)), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("dwarf_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.DWARF)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				FeySoulStone.createFake(ResidentType.DWARF),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Blocks.STONE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.DWARF)), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.FAIRY)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				FeySoulStone.createFake(ResidentType.FAIRY),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Items.GLASS_BOTTLE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("fairies"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.FAIRY)), FeySoulStone.create(SoulStoneType.GAEL)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_specialization.emerald",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.ELF),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Blocks.VINE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_specialization.garnet",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.GNOME),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Items.FLINT), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("gnomes"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_specialization.aquamarine",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.DWARF),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Blocks.OBSIDIAN), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE, 1), FeySoulStone.create(SoulStoneType.GEM)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_upgrade.up.ruby",
				FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.RUBY, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
				new ItemStack[] {FeyResource.create(FeyResourceType.ESSENCE, 1), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumAetherResourceItem.getItem(AetherResourceType.FLOWER_GINSENG, 1), FeyResource.create(FeyResourceType.FLOWER, 1)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.RUBY, 1)))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_upgrade.down.ruby",
				FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
				new ItemStack[] {FeyResource.create(FeyResourceType.ESSENCE, 1), FeyResource.create(FeyResourceType.GOLEM_TOKEN, 1), NostrumAetherResourceItem.getItem(AetherResourceType.FLOWER_GINSENG, 1), FeyResource.create(FeyResourceType.TEARS, 1)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY, 1)))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_upgrade.up.sapphire",
				FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
				new ItemStack[] {FeyResource.create(FeyResourceType.ESSENCE, 1), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumAetherResourceItem.getItem(AetherResourceType.FLOWER_MANDRAKE, 1), NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE, 1)))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_upgrade.down.sapphire",
				FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.SAPPHIRE, 1),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.MANI_DUST, ReagentType.GRAVE_DUST, ReagentType.CRYSTABLOOM},
				NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1),
				new ItemStack[] {FeyResource.create(FeyResourceType.ESSENCE, 1), FeyResource.create(FeyResourceType.GOLEM_TOKEN, 1), NostrumAetherResourceItem.getItem(AetherResourceType.FLOWER_MANDRAKE, 1), NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1)},
				new RRequirementResearch("logistics"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.DOWNGRADE, FeyStoneMaterial.SAPPHIRE, 1)))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("template_wand",
				new ItemStack(TemplateWand.instance()),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM},
				new ItemStack(MageStaff.instance()),
				new ItemStack[] {new ItemStack(MimicBlock.facade()), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1), new ItemStack(MimicBlock.facade())},
				new RRequirementResearch("logistics_construction"),
				new OutcomeSpawnItem(new ItemStack(TemplateWand.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_storage",
				new ItemStack(StorageLogisticsChest.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Blocks.CHEST),
				new ItemStack[] {ItemStack.EMPTY, new ItemStack(Items.DYE, 1, 4), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), ItemStack.EMPTY},
				new RRequirementResearch("logistics_items"),
				new OutcomeSpawnItem(new ItemStack(StorageLogisticsChest.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_buffer",
				new ItemStack(BufferLogisticsChest.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Blocks.CHEST),
				new ItemStack[] {ItemStack.EMPTY, new ItemStack(Items.DYE, 1, 11), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), ItemStack.EMPTY},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(BufferLogisticsChest.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_output",
				new ItemStack(OutputLogisticsChest.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Blocks.CHEST),
				new ItemStack[] {ItemStack.EMPTY, new ItemStack(Items.DYE, 1, 1), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), ItemStack.EMPTY},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(OutputLogisticsChest.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_input",
				new ItemStack(InputLogisticsChest.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Blocks.CHEST),
				new ItemStack[] {ItemStack.EMPTY, new ItemStack(Items.DYE, 1, 2), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), ItemStack.EMPTY},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(InputLogisticsChest.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lpanel_output",
				new ItemStack(OutputLogisticsPanel.instance()),
				null,
				new ReagentType[] {ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.MANI_DUST},
				new ItemStack(OutputLogisticsChest.instance()),
				new ItemStack[] {ItemStack.EMPTY, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), ItemStack.EMPTY, ItemStack.EMPTY},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(OutputLogisticsPanel.instance(), 4)))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("logistics_pylon",
				new ItemStack(LogisticsPylon.instance()),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Items.EMERALD),
				new ItemStack[] {ItemStack.EMPTY, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.STONE, 1, OreDictionary.WILDCARD_VALUE), ItemStack.EMPTY},
				new RRequirementResearch("logistics_relays"),
				new OutcomeSpawnItem(new ItemStack(LogisticsPylon.instance(), 2)))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("storage_monitor",
				new ItemStack(StorageMonitor.instance()),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
				new ItemStack(MirrorItem.instance()),
				new ItemStack[] {ItemStack.EMPTY, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.CHEST), ItemStack.EMPTY},
				new RRequirementResearch("logistics_sensors"),
				new OutcomeSpawnItem(new ItemStack(StorageMonitor.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("storage_sensor",
				new ItemStack(LogisticsSensorBlock.instance()),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG},
				new ItemStack(StorageMonitor.instance()),
				new ItemStack[] {ItemStack.EMPTY, new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(Items.REDSTONE), ItemStack.EMPTY},
				new RRequirementResearch("logistics_sensors"),
				new OutcomeSpawnItem(new ItemStack(LogisticsSensorBlock.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("farming_block",
				new ItemStack(FarmingBlock.instance()),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				new ItemStack(Items.SIGN),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.DIAMOND_HOE), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("gnomes"),
				new OutcomeSpawnItem(new ItemStack(FarmingBlock.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("gathering_block",
				new ItemStack(GatheringBlock.instance()),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				new ItemStack(Items.SIGN),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Items.LEATHER), new ItemStack(ReagentBag.instance()), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("gathering_blocks"),
				new OutcomeSpawnItem(new ItemStack(GatheringBlock.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("woodcutting_block",
				new ItemStack(WoodcuttingBlock.instance()),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				new ItemStack(Items.SIGN),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.LEAVES), new ItemStack(Items.DIAMOND_AXE), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(new ItemStack(WoodcuttingBlock.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("mining_block",
				new ItemStack(MiningBlock.instance()),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				new ItemStack(Items.SIGN),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.OBSIDIAN), new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(new ItemStack(MiningBlock.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("building_block",
				new ItemStack(BuildingBlock.instance()),
				null,
				new ReagentType[] {ReagentType.GRAVE_DUST, ReagentType.GINSENG, ReagentType.SPIDER_SILK, ReagentType.SPIDER_SILK},
				new ItemStack(Items.SIGN),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.CHEST), new ItemStack(Blocks.BRICK_BLOCK), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("building_blocks"),
				new OutcomeSpawnItem(new ItemStack(BuildingBlock.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("crafting_block.gnome",
				new ItemStack(CraftingBlockGnome.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				new ItemStack(Blocks.CRAFTING_TABLE),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(Blocks.STONE), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("gnome_crafting"),
				new OutcomeSpawnItem(new ItemStack(CraftingBlockGnome.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("crafting_block.elf",
				new ItemStack(CraftingBlockElf.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				new ItemStack(Blocks.LOG, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(Blocks.STONE), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("elf_crafting"),
				new OutcomeSpawnItem(new ItemStack(CraftingBlockElf.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("crafting_block.dwarf",
				new ItemStack(CraftingBlockDwarf.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.SKY_ASH, ReagentType.BLACK_PEARL},
				new ItemStack(Blocks.ANVIL),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack(Blocks.STONE), new ItemStack(Items.GOLD_INGOT)},
				new RRequirementResearch("dwarf_crafting"),
				new OutcomeSpawnItem(new ItemStack(CraftingBlockDwarf.instance())))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("soul_jar",
				SoulJar.createFake(false),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG, ReagentType.SKY_ASH},
				FeySoulStone.create(SoulStoneType.GEM),
				new ItemStack[] {new ItemStack(Items.GHAST_TEAR), ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.ENDER_PEARL)},
				new RRequirementResearch("soul_jars"),
				new OutcomeSpawnItem(SoulJar.createFake(false)))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_reinforced_iron",
				new ItemStack(ReinforcedStorageLogisticsChest.Iron()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(StorageLogisticsChest.instance()),
				new ItemStack[] {ItemStack.EMPTY, new ItemStack(Blocks.CHEST), new ItemStack(Blocks.IRON_BLOCK), ItemStack.EMPTY},
				new RRequirementResearch("adv_logistics_storage"),
				new OutcomeSpawnItem(new ItemStack(ReinforcedStorageLogisticsChest.Iron())))
			);

    	// shortcut iron recipe
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_reinforced_iron",
				new ItemStack(ReinforcedStorageLogisticsChest.Iron()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Blocks.IRON_BLOCK),
				new ItemStack[] {new ItemStack(Blocks.CHEST), new ItemStack(Items.DYE, 1, 4), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.CHEST)},
				new RRequirementResearch("adv_logistics_storage"),
				new OutcomeSpawnItem(new ItemStack(ReinforcedStorageLogisticsChest.Iron())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_reinforced_gold",
				new ItemStack(ReinforcedStorageLogisticsChest.Gold()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(ReinforcedStorageLogisticsChest.Iron()),
				new ItemStack[] {ItemStack.EMPTY, new ItemStack(Blocks.CHEST), new ItemStack(Blocks.GOLD_BLOCK), ItemStack.EMPTY},
				new RRequirementResearch("adv_logistics_storage"),
				new OutcomeSpawnItem(new ItemStack(ReinforcedStorageLogisticsChest.Gold())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_reinforced_diamond",
				new ItemStack(ReinforcedStorageLogisticsChest.Diamond()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(ReinforcedStorageLogisticsChest.Gold()),
				new ItemStack[] {ItemStack.EMPTY, new ItemStack(Blocks.CHEST), new ItemStack(Blocks.DIAMOND_BLOCK), ItemStack.EMPTY},
				new RRequirementResearch("adv_logistics_storage"),
				new OutcomeSpawnItem(new ItemStack(ReinforcedStorageLogisticsChest.Diamond())))
			);
    	
    }
    
    private void registerResearch() {
		
		NostrumResearch.startBuilding()
			.lore(FeyResource.instance())
			.hiddenParent("rituals")
			.reference("ritual::purify_essence", "ritual.purify_essence.name")
		.build("purify_essence", NostrumFairies.researchTab, Size.NORMAL, 0, -1, true, FeyResource.create(FeyResourceType.ESSENCE, 1));
		
		NostrumResearch.startBuilding()
			.parent("purify_essence")
			.hiddenParent("kani")
			.reference("ritual::fey_bell", "ritual.fey_bell.name")
		.build("fey_bell", NostrumFairies.researchTab, Size.NORMAL, -1, 0, true, FeyResource.create(FeyResourceType.BELL, 1));
		
		NostrumResearch.startBuilding()
			.parent("purify_essence")
			.hiddenParent("fey_bell")
			.lore(EntityShadowFey.ShadowFeyConversionLore.instance())
			.reference("ritual::fey_flower", "ritual.fey_flower.name")
		.build("fey_flower", NostrumFairies.researchTab, Size.NORMAL, 1, 0, true, FeyResource.create(FeyResourceType.FLOWER, 1));
		
		NostrumResearch.startBuilding()
			.parent("purify_essence")
			.parent("fey_flower")
			.parent("fey_bell")
			.lore(FeyResource.FeyFriendLore.instance())
			.reference("ritual::soul_stone", "ritual.soul_stone.name")
			.reference("ritual::soul_gael", "ritual.soul_gael.name")
		.build("fey_souls", NostrumFairies.researchTab, Size.LARGE, 0, 1, true, FeySoulStone.create(SoulStoneType.GEM));
		
		NostrumResearch.startBuilding()
			.parent("fey_souls")
			.link("fairy_gael_aggressive")
			.reference("ritual::fairy_instrument_lyre", "ritual.fairy_instrument_lyre.name")
			.reference("ritual::fairy_instrument_flute", "ritual.fairy_instrument_flute.name")
			.reference("ritual::fairy_instrument_ocarina", "ritual.fairy_instrument_ocarina.name")
		.build("fairy_instruments", NostrumFairies.researchTab, Size.GIANT, 2, 1, true, FairyInstrument.create(InstrumentType.FLUTE));
		
		NostrumResearch.startBuilding()
			.parent("fairy_instruments")
			.reference("ritual::fairy_gael_aggressive", "ritual.fairy_gael_aggressive.name")
		.build("fairy_gael_aggressive", NostrumFairies.researchTab, Size.NORMAL, 4, 1, true, FairyGael.create(FairyGaelType.ATTACK, null));
		
		NostrumResearch.startBuilding()
			.parent("fairy_instruments")
			.hiddenParent("logistics_items")
			.reference("ritual::fairy_gael_logistics", "ritual.fairy_gael_logistics.name")
		.build("fairy_gael_logistics", NostrumFairies.researchTab, Size.NORMAL, 3, 2, true, FairyGael.create(FairyGaelType.LOGISTICS, null));
		
		NostrumResearch.startBuilding()
			.parent("fairy_instruments")
			.hiddenParent("logistics_construction")
			.reference("ritual::fairy_gael_construction", "ritual.fairy_gael_construction.name")
		.build("fairy_gael_construction", NostrumFairies.researchTab, Size.NORMAL, 3, 0, true, FairyGael.create(FairyGaelType.BUILD, null));
		
		NostrumResearch.startBuilding()
			.parent("fey_souls")
			.hiddenParent("aether_furnace")
			.hiddenParent("magic_token")
			.reference("ritual::logistics_tokens", "ritual.logistics_tokens.name")
			.reference("ritual::fey_upgrade.down.ruby", "ritual.fey_upgrade.down.ruby.name")
			.reference("ritual::fey_upgrade.up.ruby", "ritual.fey_upgrade.up.ruby.name")
			.reference("ritual::fey_upgrade.down.sapphire", "ritual.fey_upgrade.down.sapphire.name")
			.reference("ritual::fey_upgrade.up.sapphire", "ritual.fey_upgrade.up.sapphire.name")
		.build("logistics", NostrumFairies.researchTab, Size.GIANT, -2, 1, true, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1));
		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::dwarf_home", "ritual.dwarf_home.name")
			.reference("ritual::mining_block", "ritual.mining_block.name")
			.reference("ritual::fey_specialization.aquamarine", "ritual.fey_specialization.aquamarine.name")
		.build("dwarves", NostrumFairies.researchTab, Size.LARGE, -4, 0, true, new ItemStack(FeyHomeBlock.instance(ResidentType.DWARF)));
		
		// dwarf blocks
		
		NostrumResearch.startBuilding()
			.parent("dwarves")
			.hiddenParent("logistics_construction")
			.reference("ritual::building_block", "ritual.building_block.name")
		.build("building_blocks", NostrumFairies.researchTab, Size.NORMAL, -4, -1, true, new ItemStack(BuildingBlock.instance()));
		
		NostrumResearch.startBuilding()
			.parent("dwarves")
			.hiddenParent("logistics_crafting")
			.reference("ritual::crafting_block.dwarf", "ritual.crafting_block.dwarf.name")
		.build("dwarf_crafting", NostrumFairies.researchTab, Size.NORMAL, -5, -1, true, new ItemStack(CraftingBlockDwarf.instance()));
		

		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::elf_home", "ritual.elf_home.name")
			.reference("ritual::woodcutting_block", "ritual.woodcutting_block.name")
			.reference("ritual::fey_specialization.emerald", "ritual.fey_specialization.emerald.name")
		.build("elves", NostrumFairies.researchTab, Size.LARGE, -3, 0, true, new ItemStack(FeyHomeBlock.instance(ResidentType.ELF)));
		
		// elf blocks
		
		NostrumResearch.startBuilding()
			.parent("elves")
			.hiddenParent("logistics_crafting")
			.reference("ritual::crafting_block.elf", "ritual.crafting_block.elf.name")
		.build("elf_crafting", NostrumFairies.researchTab, Size.NORMAL, -3, -1, true, new ItemStack(CraftingBlockElf.instance()));
		
		
		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::gnome_home", "ritual.gnome_home.name")
			.reference("ritual::farming_block", "ritual.farming_block.name")
			.reference("ritual::fey_specialization.garnet", "ritual.fey_specialization.garnet.name")
		.build("gnomes", NostrumFairies.researchTab, Size.LARGE, -4, 2, true, new ItemStack(FeyHomeBlock.instance(ResidentType.GNOME)));
		
		// gnome blocks
		
		NostrumResearch.startBuilding()
			.parent("gnomes")
			.hiddenParent("logistics_items")
			.reference("ritual::gathering_block", "ritual.gathering_block.name")
		.build("gathering_blocks", NostrumFairies.researchTab, Size.NORMAL, -4, 3, true, new ItemStack(GatheringBlock.instance()));
		
		NostrumResearch.startBuilding()
			.parent("gnomes")
			.hiddenParent("logistics_crafting")
			.reference("ritual::crafting_block.gnome", "ritual.crafting_block.gnome.name")
		.build("gnome_crafting", NostrumFairies.researchTab, Size.NORMAL, -5, 3, true, new ItemStack(CraftingBlockGnome.instance()));
		
		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::fairy_home", "ritual.fairy_home.name")
		.build("fairies", NostrumFairies.researchTab, Size.LARGE, -3, 2, true, new ItemStack(FeyHomeBlock.instance(ResidentType.FAIRY)));

		NostrumResearch.startBuilding()
			.hiddenParent("logistics")
			.hiddenParent("fairies")
			.hiddenParent("fairy_instruments")
			.reference("ritual::lchest_storage", "ritual.lchest_storage.name")
		.build("logistics_items", NostrumFairies.researchTab, Size.GIANT, 0, 3, true, new ItemStack(StorageLogisticsChest.instance()));

		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.reference("ritual::lchest_buffer", "ritual.lchest_buffer.name")
			.reference("ritual::lchest_output", "ritual.lchest_output.name")
			.reference("ritual::lchest_input", "ritual.lchest_input.name")
		.build("adv_logistics_items", NostrumFairies.researchTab, Size.LARGE, 0, 4, true, new ItemStack(OutputLogisticsChest.instance()));

		NostrumResearch.startBuilding()
			.parent("adv_logistics_items")
			.hiddenParent("logistics_sensors")
			.reference("ritual::lchest_reinforced_iron", "ritual.lchest_reinforced_iron.name")
			.reference("ritual::lchest_reinforced_gold", "ritual.lchest_reinforced_gold.name")
			.reference("ritual::lchest_reinforced_diamond", "ritual.lchest_reinforced_diamond.name")
		.build("adv_logistics_storage", NostrumFairies.researchTab, Size.LARGE, 0, 5, true, new ItemStack(ReinforcedStorageLogisticsChest.Gold()));
		
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.hiddenParent("dwarves")
			.hiddenParent("geogems")
			.hiddenParent("magicfacade")
			.reference("ritual::template_wand", "ritual.template_wand.name")
		.build("logistics_construction", NostrumFairies.researchTab, Size.LARGE, 1, 4, true, new ItemStack(TemplateWand.instance()));
		
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.hiddenParent("gnomes")
			.hiddenParent("dwarves")
			.hiddenParent("elves")
			.hiddenParent("geogems")
			.link("dwarf_crafting")
		.build("logistics_crafting", NostrumFairies.researchTab, Size.LARGE, -1, 4, true, new ItemStack(CraftingBlockDwarf.instance()));
		
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.hiddenParent("geogems")
			.reference("ritual::logistics_pylon", "ritual.logistics_pylon.name")
		.build("logistics_relays", NostrumFairies.researchTab, Size.NORMAL, -1, 2, true, new ItemStack(LogisticsPylon.instance()));
		
		NostrumResearch.startBuilding()
			.parent("logistics_items")
			.reference("ritual::storage_monitor", "ritual.storage_monitor.name")
			.reference("ritual::storage_sensor", "ritual.storage_sensor.name")
		.build("logistics_sensors", NostrumFairies.researchTab, Size.NORMAL, 1, 2, true, new ItemStack(StorageMonitor.instance()));
		
		NostrumResearch.startBuilding()
			.hiddenParent("fey_souls")
			.reference("ritual::soul_jar", "ritual.soul_jar.name")
		.build("soul_jars", NostrumFairies.researchTab, Size.NORMAL, 0, 2, true, new ItemStack(SoulJar.instance()));
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
    	NostrumFairiesSounds.registerSounds(event.getRegistry());
    }

	@SubscribeEvent
    public void registerIDataSerializers(RegistryEvent.Register<IDataSerializerEntry> event) {
    	final IForgeRegistry<IDataSerializerEntry> registry = event.getRegistry();
    	
    	registry.register(new IDataSerializerEntry(FairyGeneralStatus.instance()).setRegistryName("nostrum.serial.fairy_status"));
    	registry.register(new IDataSerializerEntry(ArmPoseDwarf.instance()).setRegistryName("nostrum.serial.dwarf_arm"));
    	registry.register(new IDataSerializerEntry(ArmPoseElf.instance()).setRegistryName("nostrum.serial.elf_arm"));
    	registry.register(new IDataSerializerEntry(BattleStanceElfArcher.instance()).setRegistryName("nostrum.serial.elf_archer_stance"));
    	registry.register(new IDataSerializerEntry(BattleStanceShadowFey.instance()).setRegistryName("nostrum.serial.shadow_fey_stance"));
    	registry.register(new IDataSerializerEntry(ArmPoseGnome.instance()).setRegistryName("nostrum.serial.gnome_arm"));
    	registry.register(new IDataSerializerEntry(ItemArraySerializer.instance()).setRegistryName("nostrum.serial.itemarray"));
    	registry.register(new IDataSerializerEntry(FairyJob.instance()).setRegistryName("nostrum.serial.fairy_job"));
    }

	public PlayerEntity getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public boolean isServer() {
		return true;
	}
	
	public void requestCapabilityRefresh() {
		; // Nothing on server
	}
	
	public void pushCapabilityRefresh(PlayerEntity player) {
		INostrumFeyCapability feyAttr = NostrumFairies.getFeyWrapper(player);
		if (feyAttr != null) {
			NetworkHandler.getSyncChannel().sendTo(new CapabilitySyncMessage(feyAttr), (ServerPlayerEntity) player);
		}
	}
}
