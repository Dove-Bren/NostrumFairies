package com.smanzana.nostrumfairies.proxy;

import com.google.common.base.Predicate;
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
import com.smanzana.nostrumfairies.entity.ItemArraySerializer;
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
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
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
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilitySyncMessage;
import com.smanzana.nostrumfairies.potion.FeyPotionTypes;
import com.smanzana.nostrumfairies.potion.FeyVisibilityPotion;
import com.smanzana.nostrumfairies.rituals.outcomes.OutcomeConstructGael;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.aetheria.items.AetherResourceType;
import com.smanzana.nostrummagica.aetheria.items.NostrumAetherResourceItem;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.items.MageStaff;
import com.smanzana.nostrummagica.items.MirrorItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.ReagentBag;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class CommonProxy {
	
	public CapabilityHandler capabilityHandler;	
	
	public void preinit() {
		CapabilityManager.INSTANCE.register(INostrumFeyCapability.class, new NostrumFeyCapabilityStorage(), NostrumFeyCapability.class);
		CapabilityManager.INSTANCE.register(ITemplateViewerCapability.class, new TemplateViewerCapabilityStorage(), TemplateViewerCapability.class);
		capabilityHandler = new CapabilityHandler();
		NetworkHandler.getInstance();
		NostrumFairiesSounds.registerSounds();
		
		IFeyWorker.FairyGeneralStatus.Init();
		EntityDwarf.ArmPose.Init();
		EntityElf.ArmPose.Init();
		EntityElfArcher.BattleStance.Init();
		EntityShadowFey.BattleStance.Init();
		EntityGnome.ArmPose.Init();
		ItemArraySerializer.Init();
		EntityPersonalFairy.FairyJob.Init();
		
    	
    	int entityID = 0;
    	EntityRegistry.registerModEntity(EntityTestFairy.class, "test_fairy",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityFairy.class, "fairy",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityDwarf.class, "dwarf",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityElf.class, "elf",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGnome.class, "gnome",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityElfArcher.class, "elf_archer",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);

    	EntityRegistry.registerModEntity(EntityShadowFey.class, "shadow_fey",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityPersonalFairy.class, "personal_fairy",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityElfCrafter.class, "elf_crafter",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityDwarfCrafter.class, "dwarf_crafter",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityDwarfBuilder.class, "dwarf_builder",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGnomeCrafter.class, "gnome_crafter",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityGnomeCollector.class, "gnome_collector",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	
    	EntityRegistry.addSpawn(EntityShadowFey.class, 60, 2, 6, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.MAGICAL));
    	EntityRegistry.addSpawn(EntityShadowFey.class, 50, 1, 3, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.FOREST));
    	EntityRegistry.addSpawn(EntityShadowFey.class, 30, 2, 6, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.SPOOKY));
    	EntityRegistry.addSpawn(EntityShadowFey.class, 40, 1, 3, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.DENSE));

    	registerItems();
    	registerBlocks();
    	registerRituals();
    	
    	NostrumMagica.instance.registerResearchReloadHook((i) -> {
    		registerResearch();
    		return 0;
    	});
	}
	
	public void init() {
    	NetworkRegistry.INSTANCE.registerGuiHandler(NostrumFairies.instance, new NostrumFairyGui());
    	
    	registerPotions();
    	registerLore();
    	registerResearch();
	}
	
	public void postinit() {
		TemplateBlock.RegisterBaseOverrides();
	}
    
    private void registerItems() {
    	GameRegistry.register(
    			FeyStone.instance().setRegistryName(FeyStone.ID));
    	FeyStone.init();
    	
    	GameRegistry.register(
    			FeyResource.instance().setRegistryName(FeyResource.ID));
    	FeyResource.init();
    	
    	GameRegistry.register(
    			FeySoulStone.instance().setRegistryName(FeySoulStone.ID));
    	FeySoulStone.init();
    	
    	GameRegistry.register(
    			FairyGael.instance().setRegistryName(FairyGael.ID));
    	FairyGael.init();
    	
    	GameRegistry.register(
    			FairyInstrument.instance().setRegistryName(FairyInstrument.ID));
    	FairyInstrument.init();
    	
    	GameRegistry.register(
    			TemplateWand.instance().setRegistryName(TemplateWand.ID));
    	TemplateWand.init();
    	
    	GameRegistry.register(
    			TemplateScroll.instance().setRegistryName(TemplateScroll.ID));
    	TemplateScroll.init();
    }
    
    private void registerBlocks() {
    	GameRegistry.register(StorageLogisticsChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, StorageLogisticsChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(StorageLogisticsChest.instance())).setRegistryName(StorageLogisticsChest.ID));
    	StorageLogisticsChest.init();
    	
    	GameRegistry.register(BufferLogisticsChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, BufferLogisticsChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(BufferLogisticsChest.instance())).setRegistryName(BufferLogisticsChest.ID));
    	BufferLogisticsChest.init();
    	
    	GameRegistry.register(OutputLogisticsChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, OutputLogisticsChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(OutputLogisticsChest.instance())).setRegistryName(OutputLogisticsChest.ID));
    	OutputLogisticsChest.init();
    	
    	GameRegistry.register(StorageMonitor.instance(),
    			new ResourceLocation(NostrumFairies.MODID, StorageMonitor.ID));
    	GameRegistry.register(
    			(new ItemBlock(StorageMonitor.instance())).setRegistryName(StorageMonitor.ID));
    	StorageMonitor.init();
    	
    	GameRegistry.register(InputLogisticsChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, InputLogisticsChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(InputLogisticsChest.instance())).setRegistryName(InputLogisticsChest.ID));
    	InputLogisticsChest.init();
    	
    	GameRegistry.register(GatheringBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, GatheringBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(GatheringBlock.instance())).setRegistryName(GatheringBlock.ID));
    	GatheringBlock.init();
    	
    	GameRegistry.register(LogisticsPylon.instance(),
    			new ResourceLocation(NostrumFairies.MODID, LogisticsPylon.ID));
    	GameRegistry.register(
    			(new ItemBlock(LogisticsPylon.instance())).setRegistryName(LogisticsPylon.ID));
    	LogisticsPylon.init();
    	
    	GameRegistry.register(WoodcuttingBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, WoodcuttingBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(WoodcuttingBlock.instance())).setRegistryName(WoodcuttingBlock.ID));
    	WoodcuttingBlock.init();
    	
    	GameRegistry.register(MiningBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, MiningBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(MiningBlock.instance())).setRegistryName(MiningBlock.ID));
    	MiningBlock.init();
    	
    	GameRegistry.register(MagicLight.Bright(),
    			new ResourceLocation(NostrumFairies.MODID, MagicLight.BrightID));
    	GameRegistry.register(MagicLight.Medium(),
    			new ResourceLocation(NostrumFairies.MODID, MagicLight.MediumID));
    	GameRegistry.register(MagicLight.Dim(),
    			new ResourceLocation(NostrumFairies.MODID, MagicLight.DimID));
    	GameRegistry.register(MagicLight.Unlit(),
    			new ResourceLocation(NostrumFairies.MODID, MagicLight.UnlitID));
    	
    	GameRegistry.register(FarmingBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, FarmingBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(FarmingBlock.instance())).setRegistryName(FarmingBlock.ID));
    	FarmingBlock.init();
    	
    	for (ResidentType type : ResidentType.values()) {
	    	GameRegistry.register(FeyHomeBlock.instance(type),
	    			new ResourceLocation(NostrumFairies.MODID, FeyHomeBlock.ID(type)));
	    	GameRegistry.register(
	    			(new ItemBlock(FeyHomeBlock.instance(type))).setRegistryName(FeyHomeBlock.ID(type)));
    	}
    	FeyHomeBlock.init();
    	
    	GameRegistry.register(FeyBush.instance(),
    			new ResourceLocation(NostrumFairies.MODID, FeyBush.ID));
    	GameRegistry.register(
    			(new ItemBlock(FeyBush.instance()) {
    				@Override
    				public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
    					return ((FeyBush) this.block).getEntityInteraction(stack, playerIn, target, hand);
    				}
    			}).setRegistryName(FeyBush.ID));
    	FeyBush.init();
    	
    	GameRegistry.register(TemplateBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, TemplateBlock.ID));
    	TemplateBlock.init();
    	
    	GameRegistry.register(BuildingBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, BuildingBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(BuildingBlock.instance())).setRegistryName(BuildingBlock.ID));
    	BuildingBlock.init();
    	
    	GameRegistry.register(CraftingBlockDwarf.instance(),
    			new ResourceLocation(NostrumFairies.MODID, CraftingBlockDwarf.ID));
    	GameRegistry.register(
    			(new ItemBlock(CraftingBlockDwarf.instance())).setRegistryName(CraftingBlockDwarf.ID));
    	CraftingBlockDwarf.init();
    	
    	GameRegistry.register(CraftingBlockElf.instance(),
    			new ResourceLocation(NostrumFairies.MODID, CraftingBlockElf.ID));
    	GameRegistry.register(
    			(new ItemBlock(CraftingBlockElf.instance())).setRegistryName(CraftingBlockElf.ID));
    	CraftingBlockElf.init();
    	
    	GameRegistry.register(CraftingBlockGnome.instance(),
    			new ResourceLocation(NostrumFairies.MODID, CraftingBlockGnome.ID));
    	GameRegistry.register(
    			(new ItemBlock(CraftingBlockGnome.instance())).setRegistryName(CraftingBlockGnome.ID));
    	CraftingBlockGnome.init();
    	
    	GameRegistry.register(LogisticsSensorBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, LogisticsSensorBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(LogisticsSensorBlock.instance())).setRegistryName(LogisticsSensorBlock.ID));
    	LogisticsSensorBlock.init();
    }
    
    private void registerLore() {
    	LoreRegistry.instance().register(FeyResource.instance());
    	LoreRegistry.instance().register(FeyStone.instance());
    	LoreRegistry.instance().register(FairyGael.instance());
    	//LoreRegistry.instance().register(new EntityShadowFey());
    	LoreRegistry.instance().register(EntityShadowFey.ShadowFeyConversionLore.instance());
    	LoreRegistry.instance().register(FeyResource.FeyFriendLore.instance());
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
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.ESSENCE, 1)))
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
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.GNOME))))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("elf_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.ELF)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				FeySoulStone.createFake(ResidentType.ELF),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Blocks.SAPLING, 1, OreDictionary.WILDCARD_VALUE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.ELF))))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("dwarf_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.DWARF)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				FeySoulStone.createFake(ResidentType.DWARF),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Blocks.STONE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.DWARF))))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.FAIRY)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				FeySoulStone.createFake(ResidentType.FAIRY),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1), new ItemStack(Items.GLASS_BOTTLE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("fairies"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.FAIRY))))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_specialization.emerald",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.ELF),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Blocks.VINE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD, 1)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_specialization.garnet",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.GNOME),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.WISP_PEBBLE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Items.FLINT), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("gnomes"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET, 1)))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fey_specialization.aquamarine",
				FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE, 1),
				null,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.BLACK_PEARL, ReagentType.GRAVE_DUST, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(ResidentType.DWARF),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.SPRITE_CORE, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Blocks.OBSIDIAN), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(FeyStone.create(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE, 1)))
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
				new ItemStack[] {null, new ItemStack(Items.DYE, 1, 4), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), null},
				new RRequirementResearch("logistics_items"),
				new OutcomeSpawnItem(new ItemStack(StorageLogisticsChest.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_buffer",
				new ItemStack(BufferLogisticsChest.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Blocks.CHEST),
				new ItemStack[] {null, new ItemStack(Items.DYE, 1, 11), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), null},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(BufferLogisticsChest.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_output",
				new ItemStack(OutputLogisticsChest.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Blocks.CHEST),
				new ItemStack[] {null, new ItemStack(Items.DYE, 1, 1), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), null},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(OutputLogisticsChest.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("lchest_input",
				new ItemStack(InputLogisticsChest.instance()),
				null,
				new ReagentType[] {ReagentType.SPIDER_SILK, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Blocks.CHEST),
				new ItemStack[] {null, new ItemStack(Items.DYE, 1, 2), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), null},
				new RRequirementResearch("adv_logistics_items"),
				new OutcomeSpawnItem(new ItemStack(InputLogisticsChest.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("logistics_pylon",
				new ItemStack(LogisticsPylon.instance()),
				null,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.CRYSTABLOOM, ReagentType.MANDRAKE_ROOT, ReagentType.MANI_DUST},
				new ItemStack(Items.EMERALD),
				new ItemStack[] {null, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.STONE, 1, OreDictionary.WILDCARD_VALUE), null},
				new RRequirementResearch("logistics_relays"),
				new OutcomeSpawnItem(new ItemStack(LogisticsPylon.instance(), 2)))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("storage_monitor",
				new ItemStack(StorageMonitor.instance()),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.GINSENG, ReagentType.MANDRAKE_ROOT, ReagentType.SPIDER_SILK},
				new ItemStack(MirrorItem.instance()),
				new ItemStack[] {null, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.CHEST), null},
				new RRequirementResearch("logistics_sensors"),
				new OutcomeSpawnItem(new ItemStack(StorageMonitor.instance())))
			);

    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("storage_sensor",
				new ItemStack(LogisticsSensorBlock.instance()),
				null,
				new ReagentType[] {ReagentType.SKY_ASH, ReagentType.BLACK_PEARL, ReagentType.MANDRAKE_ROOT, ReagentType.GINSENG},
				new ItemStack(StorageMonitor.instance()),
				new ItemStack[] {null, new ItemStack(Blocks.REDSTONE_BLOCK), new ItemStack(Items.REDSTONE), null},
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
    }
    
    private void registerPotions() {
    	FeyVisibilityPotion.instance();
    	
    	FeyPotionTypes.register();
    	
    	ItemStack ingredStack = FeyResource.create(FeyResourceType.TEARS, 1);
		Predicate<ItemStack> ingred = new PotionHelper.ItemPredicateInstance(ingredStack.getItem(), ingredStack.getMetadata());
		PotionHelper.registerPotionTypeConversion(PotionTypes.THICK, ingred, FeyPotionTypes.FEY_VISIBILITY.getType());
    }

	public EntityPlayer getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public boolean isServer() {
		return true;
	}
	
	public void requestCapabilityRefresh() {
		; // Nothing on server
	}
	
	public void pushCapabilityRefresh(EntityPlayer player) {
		INostrumFeyCapability feyAttr = NostrumFairies.getFeyWrapper(player);
		if (feyAttr != null) {
			NetworkHandler.getSyncChannel().sendTo(new CapabilitySyncMessage(feyAttr), (EntityPlayerMP) player);
		}
	}
}
