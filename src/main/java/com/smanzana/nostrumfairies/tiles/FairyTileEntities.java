package com.smanzana.nostrumfairies.tiles;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
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
	
	@ObjectHolder(ID_BufferChestTileEntity) public static BlockEntityType<BufferChestTileEntity> BufferChestTileEntityType;
	@ObjectHolder(ID_BuildingBlockTileEntity) public static BlockEntityType<BuildingBlockTileEntity> BuildingBlockTileEntityType;
	@ObjectHolder(ID_CraftingBlockDwarfTileEntity) public static BlockEntityType<CraftingBlockDwarfTileEntity> CraftingBlockDwarfTileEntityType;
	@ObjectHolder(ID_CraftingBlockElfTileEntity) public static BlockEntityType<CraftingBlockElfTileEntity> CraftingBlockElfTileEntityType;
	@ObjectHolder(ID_CraftingBlockGnomeTileEntity) public static BlockEntityType<CraftingBlockGnomeTileEntity> CraftingBlockGnomeTileEntityType;
	@ObjectHolder(ID_FarmingBlockTileEntity) public static BlockEntityType<FarmingBlockTileEntity> FarmingBlockTileEntityType;
	@ObjectHolder(ID_GatheringBlockTileEntity) public static BlockEntityType<GatheringBlockTileEntity> GatheringBlockTileEntityType;
	@ObjectHolder(ID_HomeBlockTileEntity) public static BlockEntityType<HomeBlockTileEntity> HomeBlockTileEntityType;
	@ObjectHolder(ID_InputChestTileEntity) public static BlockEntityType<InputChestTileEntity> InputChestTileEntityType;
	@ObjectHolder(ID_LogisticsSensorTileEntity) public static BlockEntityType<LogisticsSensorTileEntity> LogisticsSensorTileEntityType;
	@ObjectHolder(ID_MiningBlockTileEntity) public static BlockEntityType<MiningBlockTileEntity> MiningBlockTileEntityType;
	@ObjectHolder(ID_OutputChestTileEntity) public static BlockEntityType<OutputChestTileEntity> OutputChestTileEntityType;
	@ObjectHolder(ID_OutputPanelTileEntity) public static BlockEntityType<OutputPanelTileEntity> OutputPanelTileEntityType;
	@ObjectHolder(ID_PylonTileEntity) public static BlockEntityType<PylonTileEntity> PylonTileEntityType;
	@ObjectHolder(ID_ReinforcedDiamondChestTileEntity) public static BlockEntityType<ReinforcedDiamondChestTileEntity> ReinforcedDiamondChestTileEntityType;
	@ObjectHolder(ID_ReinforcedGoldChestTileEntity) public static BlockEntityType<ReinforcedGoldChestTileEntity> ReinforcedGoldChestTileEntityType;
	@ObjectHolder(ID_ReinforcedIronChestTileEntity) public static BlockEntityType<ReinforcedIronChestTileEntity> ReinforcedIronChestTileEntityType;
	@ObjectHolder(ID_StorageChestTileEntity) public static BlockEntityType<StorageChestTileEntity> StorageChestTileEntityType;
	@ObjectHolder(ID_StorageMonitorTileEntity) public static BlockEntityType<StorageMonitorTileEntity> StorageMonitorTileEntityType;
	@ObjectHolder(ID_TemplateBlockTileEntity) public static BlockEntityType<TemplateBlockTileEntity> TemplateBlockTileEntityType;
	@ObjectHolder(ID_WoodcuttingBlockTileEntity) public static BlockEntityType<WoodcuttingBlockTileEntity> WoodcuttingBlockTileEntityType;
	
	
	private static void register(IForgeRegistry<BlockEntityType<?>> registry, BlockEntityType<?> type, String ID) {
		registry.register(type.setRegistryName(ID));
	}
	
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
		final IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();
		
		register(registry, BlockEntityType.Builder.of(BufferChestTileEntity::new, FairyBlocks.bufferChest).build(null), ID_BufferChestTileEntity);
		register(registry, BlockEntityType.Builder.of(BuildingBlockTileEntity::new, FairyBlocks.buildingBlock).build(null), ID_BuildingBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(CraftingBlockDwarfTileEntity::new, FairyBlocks.dwarfCraftingBlock).build(null), ID_CraftingBlockDwarfTileEntity);
		register(registry, BlockEntityType.Builder.of(CraftingBlockElfTileEntity::new, FairyBlocks.elfCraftingBlock).build(null), ID_CraftingBlockElfTileEntity);
		register(registry, BlockEntityType.Builder.of(CraftingBlockGnomeTileEntity::new, FairyBlocks.gnomeCraftingBlock).build(null), ID_CraftingBlockGnomeTileEntity);
		register(registry, BlockEntityType.Builder.of(FarmingBlockTileEntity::new, FairyBlocks.farmingBlock).build(null), ID_FarmingBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(GatheringBlockTileEntity::new, FairyBlocks.gatheringBlock).build(null), ID_GatheringBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(HomeBlockTileEntity::new, FairyBlocks.dwarfHome, FairyBlocks.elfHome, FairyBlocks.fairyHome, FairyBlocks.gnomeHome).build(null), ID_HomeBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(InputChestTileEntity::new, FairyBlocks.inputChest).build(null), ID_InputChestTileEntity);
		register(registry, BlockEntityType.Builder.of(LogisticsSensorTileEntity::new, FairyBlocks.logisticsSensor).build(null), ID_LogisticsSensorTileEntity);
		register(registry, BlockEntityType.Builder.of(MiningBlockTileEntity::new, FairyBlocks.miningBlock).build(null), ID_MiningBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(OutputChestTileEntity::new, FairyBlocks.outputChest).build(null), ID_OutputChestTileEntity);
		register(registry, BlockEntityType.Builder.of(OutputPanelTileEntity::new, FairyBlocks.outputPanel).build(null), ID_OutputPanelTileEntity);
		register(registry, BlockEntityType.Builder.of(PylonTileEntity::new, FairyBlocks.logisticsPylon).build(null), ID_PylonTileEntity);
		register(registry, BlockEntityType.Builder.of(ReinforcedDiamondChestTileEntity::new, FairyBlocks.reinforcedDiamondChest).build(null), ID_ReinforcedDiamondChestTileEntity);
		register(registry, BlockEntityType.Builder.of(ReinforcedGoldChestTileEntity::new, FairyBlocks.reinforcedGoldChest).build(null), ID_ReinforcedGoldChestTileEntity);
		register(registry, BlockEntityType.Builder.of(ReinforcedIronChestTileEntity::new, FairyBlocks.reinforcedIronChest).build(null), ID_ReinforcedIronChestTileEntity);
		register(registry, BlockEntityType.Builder.of(StorageChestTileEntity::new, FairyBlocks.storageChest).build(null), ID_StorageChestTileEntity);
		register(registry, BlockEntityType.Builder.of(StorageMonitorTileEntity::new, FairyBlocks.storageMonitor).build(null), ID_StorageMonitorTileEntity);
		register(registry, BlockEntityType.Builder.of(TemplateBlockTileEntity::new, FairyBlocks.templateBlock).build(null), ID_TemplateBlockTileEntity);
		register(registry, BlockEntityType.Builder.of(WoodcuttingBlockTileEntity::new, FairyBlocks.woodcuttingBlock).build(null), ID_WoodcuttingBlockTileEntity);
	}
	
}
