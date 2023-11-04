package com.smanzana.nostrumfairies.tiles;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FairyTileEntities {

	private static final String ID_BufferChestTileEntity = "buffer_chest";
	private static final String ID_BuildingBlockTileEntity = "building_block";
	private static final String ID_CraftingBlockDwarfTileEntity = "crafting_dwarf";
	private static final String ID_CraftingBlockElfTileEntity = "crafting_elf";
	private static final String ID_CraftingBlockGnomeTileEntity = "crafting_gnome";
	private static final String ID_FarmingBlockTileEntity = "farming_block";
	private static final String ID_GatheringBlockTileEntity = "gathering_block";
	private static final String ID_HomeBlockTileEntity = "home_block";
	private static final String ID_InputChestTileEntity = "input_chest";
	private static final String ID_LogisticsSensorTileEntity = "logistics_sensor";
	private static final String ID_MiningBlockTileEntity = "mining_block";
	private static final String ID_OutputChestTileEntity = "output_chest";
	private static final String ID_OutputPanelTileEntity = "output_panel";
	private static final String ID_PylonTileEntity = "pylon";
	private static final String ID_ReinforcedDiamondChestTileEntity = "reinforced_diamond";
	private static final String ID_ReinforcedGoldChestTileEntity = "reinforced_gold";
	private static final String ID_ReinforcedIronChestTileEntity = "reinforced_iron";
	private static final String ID_StorageChestTileEntity = "storage_chest";
	private static final String ID_StorageMonitorTileEntity = "storage_monitor";
	private static final String ID_TemplateBlockTileEntity = "template_block";
	private static final String ID_WoodcuttingBlockTileEntity = "woodcutting_block";
	
	@ObjectHolder(ID_BufferChestTileEntity) public static TileEntityType<BufferChestTileEntity> BufferChestTileEntityType;
	@ObjectHolder(ID_BuildingBlockTileEntity) public static TileEntityType<BuildingBlockTileEntity> BuildingBlockTileEntityType;
	@ObjectHolder(ID_CraftingBlockDwarfTileEntity) public static TileEntityType<CraftingBlockDwarfTileEntity> CraftingBlockDwarfTileEntityType;
	@ObjectHolder(ID_CraftingBlockElfTileEntity) public static TileEntityType<CraftingBlockElfTileEntity> CraftingBlockElfTileEntityType;
	@ObjectHolder(ID_CraftingBlockGnomeTileEntity) public static TileEntityType<CraftingBlockGnomeTileEntity> CraftingBlockGnomeTileEntityType;
	@ObjectHolder(ID_FarmingBlockTileEntity) public static TileEntityType<FarmingBlockTileEntity> FarmingBlockTileEntityType;
	@ObjectHolder(ID_GatheringBlockTileEntity) public static TileEntityType<GatheringBlockTileEntity> GatheringBlockTileEntityType;
	@ObjectHolder(ID_HomeBlockTileEntity) public static TileEntityType<HomeBlockTileEntity> HomeBlockTileEntityType;
	@ObjectHolder(ID_InputChestTileEntity) public static TileEntityType<InputChestTileEntity> InputChestTileEntityType;
	@ObjectHolder(ID_LogisticsSensorTileEntity) public static TileEntityType<LogisticsSensorTileEntity> LogisticsSensorTileEntityType;
	@ObjectHolder(ID_MiningBlockTileEntity) public static TileEntityType<MiningBlockTileEntity> MiningBlockTileEntityType;
	@ObjectHolder(ID_OutputChestTileEntity) public static TileEntityType<OutputChestTileEntity> OutputChestTileEntityType;
	@ObjectHolder(ID_OutputPanelTileEntity) public static TileEntityType<OutputPanelTileEntity> OutputPanelTileEntityType;
	@ObjectHolder(ID_PylonTileEntity) public static TileEntityType<PylonTileEntity> PylonTileEntityType;
	@ObjectHolder(ID_ReinforcedDiamondChestTileEntity) public static TileEntityType<ReinforcedDiamondChestTileEntity> ReinforcedDiamondChestTileEntityType;
	@ObjectHolder(ID_ReinforcedGoldChestTileEntity) public static TileEntityType<ReinforcedGoldChestTileEntity> ReinforcedGoldChestTileEntityType;
	@ObjectHolder(ID_ReinforcedIronChestTileEntity) public static TileEntityType<ReinforcedIronChestTileEntity> ReinforcedIronChestTileEntityType;
	@ObjectHolder(ID_StorageChestTileEntity) public static TileEntityType<StorageChestTileEntity> StorageChestTileEntityType;
	@ObjectHolder(ID_StorageMonitorTileEntity) public static TileEntityType<StorageMonitorTileEntity> StorageMonitorTileEntityType;
	@ObjectHolder(ID_TemplateBlockTileEntity) public static TileEntityType<TemplateBlockTileEntity> TemplateBlockTileEntityType;
	@ObjectHolder(ID_WoodcuttingBlockTileEntity) public static TileEntityType<WoodcuttingBlockTileEntity> WoodcuttingBlockTileEntityType;
	
	
	private static void register(IForgeRegistry<TileEntityType<?>> registry, TileEntityType<?> type, String ID) {
		registry.register(type.setRegistryName(ID));
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		final IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
		
		register(registry, TileEntityType.Builder.create(BufferChestTileEntity::new, FairyBlocks.bufferChest).build(null), ID_BufferChestTileEntity);
		register(registry, TileEntityType.Builder.create(BuildingBlockTileEntity::new, FairyBlocks.buildingBlock).build(null), ID_BuildingBlockTileEntity);
		register(registry, TileEntityType.Builder.create(CraftingBlockDwarfTileEntity::new, FairyBlocks.dwarfCraftingBlock).build(null), ID_CraftingBlockDwarfTileEntity);
		register(registry, TileEntityType.Builder.create(CraftingBlockElfTileEntity::new, FairyBlocks.elfCraftingBlock).build(null), ID_CraftingBlockElfTileEntity);
		register(registry, TileEntityType.Builder.create(CraftingBlockGnomeTileEntity::new, FairyBlocks.gnomeCraftingBlock).build(null), ID_CraftingBlockGnomeTileEntity);
		register(registry, TileEntityType.Builder.create(FarmingBlockTileEntity::new, FairyBlocks.farmingBlock).build(null), ID_FarmingBlockTileEntity);
		register(registry, TileEntityType.Builder.create(GatheringBlockTileEntity::new, FairyBlocks.gatheringBlock).build(null), ID_GatheringBlockTileEntity);
		register(registry, TileEntityType.Builder.create(HomeBlockTileEntity::new, FairyBlocks.dwarfHome, FairyBlocks.elfHome, FairyBlocks.fairyHome, FairyBlocks.gnomeHome).build(null), ID_HomeBlockTileEntity);
		register(registry, TileEntityType.Builder.create(InputChestTileEntity::new, FairyBlocks.inputChest).build(null), ID_InputChestTileEntity);
		register(registry, TileEntityType.Builder.create(LogisticsSensorTileEntity::new, FairyBlocks.logisticsSensor).build(null), ID_LogisticsSensorTileEntity);
		register(registry, TileEntityType.Builder.create(MiningBlockTileEntity::new, FairyBlocks.miningBlock).build(null), ID_MiningBlockTileEntity);
		register(registry, TileEntityType.Builder.create(OutputChestTileEntity::new, FairyBlocks.outputChest).build(null), ID_OutputChestTileEntity);
		register(registry, TileEntityType.Builder.create(OutputPanelTileEntity::new, FairyBlocks.outputPanel).build(null), ID_OutputPanelTileEntity);
		register(registry, TileEntityType.Builder.create(PylonTileEntity::new, FairyBlocks.logisticsPylon).build(null), ID_PylonTileEntity);
		register(registry, TileEntityType.Builder.create(ReinforcedDiamondChestTileEntity::new, FairyBlocks.reinforcedDiamondChest).build(null), ID_ReinforcedDiamondChestTileEntity);
		register(registry, TileEntityType.Builder.create(ReinforcedGoldChestTileEntity::new, FairyBlocks.reinforcedGoldChest).build(null), ID_ReinforcedGoldChestTileEntity);
		register(registry, TileEntityType.Builder.create(ReinforcedIronChestTileEntity::new, FairyBlocks.reinforcedIronChest).build(null), ID_ReinforcedIronChestTileEntity);
		register(registry, TileEntityType.Builder.create(StorageChestTileEntity::new, FairyBlocks.storageChest).build(null), ID_StorageChestTileEntity);
		register(registry, TileEntityType.Builder.create(StorageMonitorTileEntity::new, FairyBlocks.storageMonitor).build(null), ID_StorageMonitorTileEntity);
		register(registry, TileEntityType.Builder.create(TemplateBlockTileEntity::new, FairyBlocks.templateBlock).build(null), ID_TemplateBlockTileEntity);
		register(registry, TileEntityType.Builder.create(WoodcuttingBlockTileEntity::new, FairyBlocks.woodcuttingBlock).build(null), ID_WoodcuttingBlockTileEntity);
	}
	
}
