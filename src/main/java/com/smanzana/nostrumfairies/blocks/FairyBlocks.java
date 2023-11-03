package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
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
	@ObjectHolder(FeyHomeBlock.ID) public static FeyHomeBlock feyHomeBlock;
	@ObjectHolder(GatheringBlock.ID) public static GatheringBlock gatheringBlock;
	@ObjectHolder(InputLogisticsChest.ID) public static InputLogisticsChest inputChest;
	@ObjectHolder(LogisticsPylon.ID) public static LogisticsPylon logisticsPylon;
	@ObjectHolder(LogisticsSensorBlock.ID) public static LogisticsSensorBlock logisticsSensor;
	@ObjectHolder(MagicLight.ID) public static MagicLight magicLight;
	@ObjectHolder(MiningBlock.ID) public static MiningBlock miningBlock;
	@ObjectHolder(OutputLogisticsChest.ID) public static OutputLogisticsChest outputChest;
	@ObjectHolder(ReinforcedStorageLogisticsChest.ID) public static ReinforcedStorageLogisticsChest reinforcedStorageChest;
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
		registerBlockItem(feyHomeBlock, feyHomeBlock.getRegistryName(), registry);
		registerBlockItem(gatheringBlock, gatheringBlock.getRegistryName(), registry);
		registerBlockItem(inputChest, inputChest.getRegistryName(), registry);
		registerBlockItem(logisticsPylon, logisticsPylon.getRegistryName(), registry);
		registerBlockItem(logisticsSensor, logisticsSensor.getRegistryName(), registry);
		registerBlockItem(magicLight, magicLight.getRegistryName(), registry);
		registerBlockItem(miningBlock, miningBlock.getRegistryName(), registry);
		registerBlockItem(outputChest, outputChest.getRegistryName(), registry);
		registerBlockItem(reinforcedStorageChest, reinforcedStorageChest.getRegistryName(), registry);
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
		registerBlock(new FeyHomeBlock(), FeyHomeBlock.ID, registry);
		registerBlock(new GatheringBlock(), GatheringBlock.ID, registry);
		registerBlock(new InputLogisticsChest(), InputLogisticsChest.ID, registry);
		registerBlock(new LogisticsPylon(), LogisticsPylon.ID, registry);
		registerBlock(new LogisticsSensorBlock(), LogisticsSensorBlock.ID, registry);
		registerBlock(new MagicLight(), MagicLight.ID, registry);
		registerBlock(new MiningBlock(), MiningBlock.ID, registry);
		registerBlock(new OutputLogisticsChest(), OutputLogisticsChest.ID, registry);
		registerBlock(new ReinforcedStorageLogisticsChest(), ReinforcedStorageLogisticsChest.ID, registry);
		registerBlock(new StorageLogisticsChest(), StorageLogisticsChest.ID, registry);
		registerBlock(new StorageMonitor(), StorageMonitor.ID, registry);
		registerBlock(new TemplateBlock(), TemplateBlock.ID, registry);
		registerBlock(new WoodcuttingBlock(), WoodcuttingBlock.ID, registry);
	}
}
