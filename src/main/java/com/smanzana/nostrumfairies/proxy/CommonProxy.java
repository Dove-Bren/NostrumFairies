package com.smanzana.nostrumfairies.proxy;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.BufferLogisticsChest;
import com.smanzana.nostrumfairies.blocks.FarmingBlock;
import com.smanzana.nostrumfairies.blocks.FeyBush;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
import com.smanzana.nostrumfairies.blocks.GatheringBlock;
import com.smanzana.nostrumfairies.blocks.InputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.LogisticsPylon;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.blocks.MiningBlock;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageMonitor;
import com.smanzana.nostrumfairies.blocks.WoodcuttingBlock;
import com.smanzana.nostrumfairies.capabilities.CapabilityHandler;
import com.smanzana.nostrumfairies.capabilities.INostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.NostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.NostrumFeyCapabilityStorage;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyInstrument;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilitySyncMessage;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
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
    	
    	EntityRegistry.addSpawn(EntityShadowFey.class, 30, 2, 6, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.MAGICAL));
    	EntityRegistry.addSpawn(EntityShadowFey.class, 40, 1, 3, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.FOREST));
    	EntityRegistry.addSpawn(EntityShadowFey.class, 30, 2, 6, EnumCreatureType.MONSTER, 
    			BiomeDictionary.getBiomesForType(BiomeDictionary.Type.SPOOKY));

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
		;
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
    }
    
    private void registerLore() {
    	LoreRegistry.instance().register(FeyResource.instance());
    	LoreRegistry.instance().register(FeyStone.instance());
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
				EMagicElement.WIND,
				new ReagentType[] {ReagentType.MANI_DUST, ReagentType.MANDRAKE_ROOT, ReagentType.SKY_ASH, ReagentType.SKY_ASH},
				new ItemStack(Blocks.TALLGRASS, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack[] {FeyResource.create(FeyResourceType.TEARS, 1), new ItemStack(Items.MELON), FeyResource.create(FeyResourceType.ESSENCE, 1), FeyResource.create(FeyResourceType.TEARS, 1)},
				new RRequirementResearch("fey_flower"),
				new OutcomeSpawnItem(FeyResource.create(FeyResourceType.FLOWER, 4)))
			);
    	
    }
    
    private void registerResearch() {
		
		NostrumResearch.startBuilding()
			.lore(FeyResource.instance())
			.reference("ritual::purify_essence", "ritual.purify_essence.name")
		.build("purify_essence", NostrumFairies.researchTab, Size.NORMAL, 0, -1, true, FeyResource.create(FeyResourceType.ESSENCE, 1));
		
		NostrumResearch.startBuilding()
			.parent("purify_essence")
			.reference("ritual::fey_bell", "ritual.fey_bell.name")
		.build("fey_bell", NostrumFairies.researchTab, Size.NORMAL, 1, 0, true, FeyResource.create(FeyResourceType.BELL, 1));
		
		NostrumResearch.startBuilding()
			.parent("fey_bell")
			.lore(EntityShadowFey.ShadowFeyConversionLore.instance())
			.reference("ritual::fey_flower", "ritual.fey_flower.name")
		.build("fey_flower", NostrumFairies.researchTab, Size.LARGE, 1, 1, true, FeyResource.create(FeyResourceType.FLOWER, 1));
		
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
