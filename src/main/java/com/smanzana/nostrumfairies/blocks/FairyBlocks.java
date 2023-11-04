package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.MagicLight.Brightness;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FairyBlocks {

	@ObjectHolder(BufferLogisticsChest.ID) public static BufferLogisticsChest bufferChest;
	@ObjectHolder(BuildingBlock.ID) public static BuildingBlock buildingBlock;
	@ObjectHolder(CraftingBlockDwarf.ID) public static CraftingBlockDwarf dwarfCraftingBlock;
	@ObjectHolder(CraftingBlockElf.ID) public static CraftingBlockElf elfCraftingBlock;
	@ObjectHolder(CraftingBlockGnome.ID) public static CraftingBlockGnome gnomeCraftingBlock;
	@ObjectHolder(FarmingBlock.ID) public static FarmingBlock farmingBlock;
	@ObjectHolder(FeyBush.ID) public static FeyBush feyBush;
	@ObjectHolder(FeyHomeBlock.ID_DWARF) public static FeyHomeBlock dwarfHome;
	@ObjectHolder(FeyHomeBlock.ID_ELF) public static FeyHomeBlock elfHome;
	@ObjectHolder(FeyHomeBlock.ID_FAIRY) public static FeyHomeBlock fairyHome;
	@ObjectHolder(FeyHomeBlock.ID_GNOME) public static FeyHomeBlock gnomeHome;
	@ObjectHolder(GatheringBlock.ID) public static GatheringBlock gatheringBlock;
	@ObjectHolder(InputLogisticsChest.ID) public static InputLogisticsChest inputChest;
	@ObjectHolder(LogisticsPylon.ID) public static LogisticsPylon logisticsPylon;
	@ObjectHolder(LogisticsSensorBlock.ID) public static LogisticsSensorBlock logisticsSensor;
	@ObjectHolder(MagicLight.ID_BRIGHT) public static MagicLight magicLightBright;
	@ObjectHolder(MagicLight.ID_MEDIUM) public static MagicLight magicLightMedium;
	@ObjectHolder(MagicLight.ID_DIM) public static MagicLight magicLightDim;
	@ObjectHolder(MagicLight.ID_UNLIT) public static MagicLight magicLightUnlit;
	@ObjectHolder(MiningBlock.ID) public static MiningBlock miningBlock;
	@ObjectHolder(OutputLogisticsChest.ID) public static OutputLogisticsChest outputChest;
	@ObjectHolder(OutputLogisticsPanel.ID) public static OutputLogisticsPanel outputPanel;
	@ObjectHolder(ReinforcedStorageLogisticsChest.ID_IRON) public static ReinforcedStorageLogisticsChest reinforcedIronChest;
	@ObjectHolder(ReinforcedStorageLogisticsChest.ID_GOLD) public static ReinforcedStorageLogisticsChest reinforcedGoldChest;
	@ObjectHolder(ReinforcedStorageLogisticsChest.ID_DIAMOND) public static ReinforcedStorageLogisticsChest reinforcedDiamondChest;
	@ObjectHolder(StorageLogisticsChest.ID) public static StorageLogisticsChest storageChest;
	@ObjectHolder(StorageMonitor.ID) public static StorageMonitor storageMonitor;
	@ObjectHolder(TemplateBlock.ID) public static TemplateBlock templateBlock;
	@ObjectHolder(WoodcuttingBlock.ID) public static WoodcuttingBlock woodcuttingBlock;
	
	private static void registerBlockItem(Block block, ResourceLocation registryName, Item.Properties builder, IForgeRegistry<Item> registry) {
		BlockItem item = new BlockItem(block, builder);
    	item.setRegistryName(registryName);
    	registry.register(item);
	}
	
	private static void registerBlockItem(Block block, ResourceLocation registryName, IForgeRegistry<Item> registry) {
		registerBlockItem(block, registryName, FairyItems.PropBase(), registry);
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		
		//registerBlockItem(bufferChest, bufferChest.getRegistryName(), registry);
		registerBlockItem(bufferChest, bufferChest.getRegistryName(), registry);
		registerBlockItem(buildingBlock, buildingBlock.getRegistryName(), registry);
		registerBlockItem(dwarfCraftingBlock, dwarfCraftingBlock.getRegistryName(), registry);
		registerBlockItem(elfCraftingBlock, elfCraftingBlock.getRegistryName(), registry);
		registerBlockItem(gnomeCraftingBlock, gnomeCraftingBlock.getRegistryName(), registry);
		registerBlockItem(farmingBlock, farmingBlock.getRegistryName(), registry);
		registerBlockItem(feyBush, feyBush.getRegistryName(), registry);
		registerBlockItem(dwarfHome, dwarfHome.getRegistryName(), registry);
		registerBlockItem(elfHome, elfHome.getRegistryName(), registry);
		registerBlockItem(fairyHome, fairyHome.getRegistryName(), registry);
		registerBlockItem(gnomeHome, gnomeHome.getRegistryName(), registry);
		registerBlockItem(gatheringBlock, gatheringBlock.getRegistryName(), registry);
		registerBlockItem(inputChest, inputChest.getRegistryName(), registry);
		registerBlockItem(logisticsPylon, logisticsPylon.getRegistryName(), registry);
		registerBlockItem(logisticsSensor, logisticsSensor.getRegistryName(), registry);
		registerBlockItem(magicLightBright, magicLightBright.getRegistryName(), registry);
		registerBlockItem(magicLightMedium, magicLightMedium.getRegistryName(), registry);
		registerBlockItem(magicLightDim, magicLightDim.getRegistryName(), registry);
		registerBlockItem(magicLightUnlit, magicLightUnlit.getRegistryName(), registry);
		registerBlockItem(miningBlock, miningBlock.getRegistryName(), registry);
		registerBlockItem(outputChest, outputChest.getRegistryName(), registry);
		registerBlockItem(outputPanel, outputPanel.getRegistryName(), registry);
		registerBlockItem(reinforcedIronChest, reinforcedIronChest.getRegistryName(), registry);
		registerBlockItem(reinforcedGoldChest, reinforcedGoldChest.getRegistryName(), registry);
		registerBlockItem(reinforcedDiamondChest, reinforcedDiamondChest.getRegistryName(), registry);
		registerBlockItem(storageChest, storageChest.getRegistryName(), registry);
		registerBlockItem(storageMonitor, storageMonitor.getRegistryName(), registry);
		registerBlockItem(templateBlock, templateBlock.getRegistryName(), registry);
		registerBlockItem(woodcuttingBlock, woodcuttingBlock.getRegistryName(), registry);
	}
	
	private static void registerBlock(Block block, String registryName, IForgeRegistry<Block> registry) {
		block.setRegistryName(registryName);
		registry.register(block);
		
		if (block instanceof ILoreTagged) {
			LoreRegistry.instance().register((ILoreTagged)block);
		}
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();
		
		registerBlock(new BufferLogisticsChest(), BufferLogisticsChest.ID, registry);
		registerBlock(new BuildingBlock(), BuildingBlock.ID, registry);
		registerBlock(new CraftingBlockDwarf(), CraftingBlockDwarf.ID, registry);
		registerBlock(new CraftingBlockElf(), CraftingBlockElf.ID, registry);
		registerBlock(new CraftingBlockGnome(), CraftingBlockGnome.ID, registry);
		registerBlock(new FarmingBlock(), FarmingBlock.ID, registry);
		registerBlock(new FeyBush(), FeyBush.ID, registry);
		registerBlock(new FeyHomeBlock(ResidentType.DWARF), FeyHomeBlock.ID_DWARF, registry);
		registerBlock(new FeyHomeBlock(ResidentType.ELF), FeyHomeBlock.ID_ELF, registry);
		registerBlock(new FeyHomeBlock(ResidentType.FAIRY), FeyHomeBlock.ID_FAIRY, registry);
		registerBlock(new FeyHomeBlock(ResidentType.GNOME), FeyHomeBlock.ID_GNOME, registry);
		registerBlock(new GatheringBlock(), GatheringBlock.ID, registry);
		registerBlock(new InputLogisticsChest(), InputLogisticsChest.ID, registry);
		registerBlock(new LogisticsPylon(), LogisticsPylon.ID, registry);
		registerBlock(new LogisticsSensorBlock(), LogisticsSensorBlock.ID, registry);
		registerBlock(new MagicLight(Brightness.BRIGHT), MagicLight.ID_BRIGHT, registry);
		registerBlock(new MagicLight(Brightness.MEDIUM), MagicLight.ID_MEDIUM, registry);
		registerBlock(new MagicLight(Brightness.DIM), MagicLight.ID_DIM, registry);
		registerBlock(new MagicLight(Brightness.UNLIT), MagicLight.ID_UNLIT, registry);
		registerBlock(new MiningBlock(), MiningBlock.ID, registry);
		registerBlock(new OutputLogisticsChest(), OutputLogisticsChest.ID, registry);
		registerBlock(new OutputLogisticsPanel(), OutputLogisticsPanel.ID, registry);
		registerBlock(new ReinforcedStorageLogisticsChest(ReinforcedStorageLogisticsChest.Type.IRON), ReinforcedStorageLogisticsChest.ID_IRON, registry);
		registerBlock(new ReinforcedStorageLogisticsChest(ReinforcedStorageLogisticsChest.Type.GOLD), ReinforcedStorageLogisticsChest.ID_GOLD, registry);
		registerBlock(new ReinforcedStorageLogisticsChest(ReinforcedStorageLogisticsChest.Type.DIAMOND), ReinforcedStorageLogisticsChest.ID_DIAMOND, registry);
		registerBlock(new StorageLogisticsChest(), StorageLogisticsChest.ID, registry);
		registerBlock(new StorageMonitor(), StorageMonitor.ID, registry);
		registerBlock(new TemplateBlock(), TemplateBlock.ID, registry);
		registerBlock(new WoodcuttingBlock(), WoodcuttingBlock.ID, registry);
	}
}
