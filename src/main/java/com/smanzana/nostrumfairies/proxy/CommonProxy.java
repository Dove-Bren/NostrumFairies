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
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.items.FairyInstrument;
import com.smanzana.nostrumfairies.items.FairyInstrument.InstrumentType;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilitySyncMessage;
import com.smanzana.nostrumfairies.rituals.outcomes.OutcomeConstructGael;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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
				FeySoulStone.createFake(SoulStoneType.GAEL, true),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), new ItemStack(ThanoPendant.instance()), new ItemStack(Blocks.GLASS), ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1)},
				new RRequirementResearch("fairy_gael_aggressive"),
				new OutcomeConstructGael(FairyGaelType.ATTACK))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_gael_logistics",
				FairyGael.create(FairyGaelType.LOGISTICS, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(SoulStoneType.GAEL, true),
				new ItemStack[] {NostrumResourceItem.getItem(ResourceType.CRYSTAL_SMALL, 1), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), new ItemStack(Blocks.GLASS), ReagentItem.instance().getReagent(ReagentType.SKY_ASH, 1)},
				new RRequirementResearch("fairy_gael_logistics"),
				new OutcomeConstructGael(FairyGaelType.LOGISTICS))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_gael_construction",
				FairyGael.create(FairyGaelType.BUILD, null),
				EMagicElement.LIGHTNING,
				new ReagentType[] {ReagentType.BLACK_PEARL, ReagentType.GINSENG, ReagentType.SKY_ASH, ReagentType.MANDRAKE_ROOT},
				FeySoulStone.createFake(SoulStoneType.GAEL, true),
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
				new ItemStack(Blocks.CRAFTING_TABLE, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Items.CLAY_BALL), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("gnomes"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.GNOME))))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("elf_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.ELF)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				new ItemStack(Blocks.CRAFTING_TABLE, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Blocks.SAPLING, 1, OreDictionary.WILDCARD_VALUE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("elves"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.ELF))))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("dwarf_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.DWARF)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				new ItemStack(Blocks.CRAFTING_TABLE, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Blocks.STONE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("dwarves"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.DWARF))))
			);
    	
    	RitualRegistry.instance().addRitual(
			RitualRecipe.createTier3("fairy_home",
				new ItemStack(FeyHomeBlock.instance(ResidentType.FAIRY)),
				null,
				new ReagentType[] {ReagentType.CRYSTABLOOM, ReagentType.SPIDER_SILK, ReagentType.GRAVE_DUST, ReagentType.GINSENG},
				new ItemStack(Blocks.CRAFTING_TABLE, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1), NostrumResourceItem.getItem(ResourceType.CRYSTAL_MEDIUM, 1), new ItemStack(Items.GLASS_BOTTLE), FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1)},
				new RRequirementResearch("fairies"),
				new OutcomeSpawnItem(new ItemStack(FeyHomeBlock.instance(ResidentType.FAIRY))))
			);
    	
    	// woodcutting block, mining block, gathering block, farming block
    	// buffer chest, storage chest, storage manager, input chest, output chest
    	// logistics pylon
    	
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
		.build("logistics", NostrumFairies.researchTab, Size.GIANT, -2, 1, true, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1));
		
		NostrumResearch.startBuilding()
			.parent("logistics")
			.reference("ritual::logistics_tokens", "ritual.logistics_tokens.name")
		.build("gnomes", NostrumFairies.researchTab, Size.GIANT, -2, 1, true, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1));
		
		
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
