package com.smanzana.nostrumfairies.init;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.OverlayRenderer;
import com.smanzana.nostrumfairies.client.gui.container.BufferChestGui;
import com.smanzana.nostrumfairies.client.gui.container.BuildingBlockGui;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationGui;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationSmallGui;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui;
import com.smanzana.nostrumfairies.client.gui.container.HomeBlockGui;
import com.smanzana.nostrumfairies.client.gui.container.InputChestGui;
import com.smanzana.nostrumfairies.client.gui.container.LogisticsSensorGui;
import com.smanzana.nostrumfairies.client.gui.container.OutputChestGui;
import com.smanzana.nostrumfairies.client.gui.container.OutputPanelGui;
import com.smanzana.nostrumfairies.client.gui.container.StorageChestGui;
import com.smanzana.nostrumfairies.client.gui.container.TemplateWandGui;
import com.smanzana.nostrumfairies.client.render.entity.RenderDwarf;
import com.smanzana.nostrumfairies.client.render.entity.RenderDwarfBuilder;
import com.smanzana.nostrumfairies.client.render.entity.RenderDwarfCrafter;
import com.smanzana.nostrumfairies.client.render.entity.RenderElf;
import com.smanzana.nostrumfairies.client.render.entity.RenderElfArcher;
import com.smanzana.nostrumfairies.client.render.entity.RenderElfCrafter;
import com.smanzana.nostrumfairies.client.render.entity.RenderFairy;
import com.smanzana.nostrumfairies.client.render.entity.RenderGnome;
import com.smanzana.nostrumfairies.client.render.entity.RenderShadowFey;
import com.smanzana.nostrumfairies.client.render.entity.RenderTestFairy;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.client.render.tile.BufferChestRenderer;
import com.smanzana.nostrumfairies.client.render.tile.BuildingBlockRenderer;
import com.smanzana.nostrumfairies.client.render.tile.CraftingBlockDwarfRenderer;
import com.smanzana.nostrumfairies.client.render.tile.CraftingBlockElfRenderer;
import com.smanzana.nostrumfairies.client.render.tile.CraftingBlockGnomeRenderer;
import com.smanzana.nostrumfairies.client.render.tile.FarmingBlockRenderer;
import com.smanzana.nostrumfairies.client.render.tile.GatheringBlockRenderer;
import com.smanzana.nostrumfairies.client.render.tile.InputChestRenderer;
import com.smanzana.nostrumfairies.client.render.tile.LogisticsSensorRenderer;
import com.smanzana.nostrumfairies.client.render.tile.MiningBlockRenderer;
import com.smanzana.nostrumfairies.client.render.tile.OutputChestRenderer;
import com.smanzana.nostrumfairies.client.render.tile.PylonRenderer;
import com.smanzana.nostrumfairies.client.render.tile.StorageChestRenderer;
import com.smanzana.nostrumfairies.client.render.tile.StorageMonitorRenderer;
import com.smanzana.nostrumfairies.client.render.tile.TemplateBlockRenderer;
import com.smanzana.nostrumfairies.client.render.tile.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.client.render.tile.WoodcuttingBlockRenderer;
import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.SoulJar;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.proxy.ClientProxy;
import com.smanzana.nostrumfairies.tiles.FairyTileEntities;
import com.smanzana.nostrumfairies.tiles.ReinforcedDiamondChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedGoldChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedIronChestTileEntity;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.TippedArrowRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client handler for MOD bus events.
 * MOD bus is not game event bus.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ClientInit {
	
	protected static OverlayRenderer overlayRenderer;

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.StorageMonitorTileEntityType, (manager) -> new StorageMonitorRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.StorageChestTileEntityType, (manager) -> new StorageChestRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.BufferChestTileEntityType, (manager) -> new BufferChestRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.OutputChestTileEntityType, (manager) -> new OutputChestRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.InputChestTileEntityType, (manager) -> new InputChestRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.GatheringBlockTileEntityType, (manager) -> new GatheringBlockRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.FarmingBlockTileEntityType, (manager) -> new FarmingBlockRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.PylonTileEntityType, (manager) -> new PylonRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.WoodcuttingBlockTileEntityType, (manager) -> new WoodcuttingBlockRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.MiningBlockTileEntityType, (manager) -> new MiningBlockRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.BuildingBlockTileEntityType, (manager) -> new BuildingBlockRenderer(manager));
		StaticTESRRenderer.instance.registerRender(FairyTileEntities.TemplateBlockTileEntityType, (manager) -> new TemplateBlockRenderer(manager));
		//ClientRegistry.bindTileEntityRenderer(FairyTileEntities.TemplateBlockTileEntityType, (manager) -> new TemplateBlockRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.CraftingBlockDwarfTileEntityType, (manager) -> new CraftingBlockDwarfRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.CraftingBlockElfTileEntityType, (manager) -> new CraftingBlockElfRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.CraftingBlockGnomeTileEntityType, (manager) -> new CraftingBlockGnomeRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.LogisticsSensorTileEntityType, (manager) -> new LogisticsSensorRenderer(manager));
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.ReinforcedIronChestTileEntityType, (manager) -> new TileEntityLogisticsRenderer<ReinforcedIronChestTileEntity>(manager) {});
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.ReinforcedGoldChestTileEntityType, (manager) -> new TileEntityLogisticsRenderer<ReinforcedGoldChestTileEntity>(manager) {});
		ClientRegistry.bindTileEntityRenderer(FairyTileEntities.ReinforcedDiamondChestTileEntityType, (manager) -> new TileEntityLogisticsRenderer<ReinforcedDiamondChestTileEntity>(manager) {});
		
		ScreenManager.registerFactory(FairyContainers.BufferChest, BufferChestGui.BufferChestGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.BuildingBlock, BuildingBlockGui.BuildingBlockGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.CraftingStation, CraftingStationGui.CraftingStationGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.CraftingStationSmall, CraftingStationSmallGui.CraftingStationSmallGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.FairyScreen, FairyScreenGui.FairyScreenGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.HomeBlock, HomeBlockGui.HomeBlockGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.InputChest, InputChestGui.InputChestGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.LogisticsSensor, LogisticsSensorGui.LogisticsSensorGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.OutputChest, OutputChestGui.OutputChestGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.OutputPanel, OutputPanelGui.OutputPanelGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.StorageChest, StorageChestGui.StorageChestGuiContainer::new);
		ScreenManager.registerFactory(FairyContainers.TemplateWand, TemplateWandGui.TemplateWandGuiContainer::new);
		
		registerBlockRenderLayer();
		registerEntityRenderers();
		
		event.enqueueWork(ClientInit::registerItemModelProperties);

		ClientProxy proxy = (ClientProxy) NostrumFairies.proxy;
		proxy.initKeybinds();
		
		overlayRenderer = new OverlayRenderer();
	}
	
	private static final void registerItemModelProperties() {
		ItemModelsProperties.registerProperty(FairyItems.soulGem, new ResourceLocation("filled"), FeySoulStone::ModelFilled);
		ItemModelsProperties.registerProperty(FairyItems.soulGem, new ResourceLocation("type_idx"), FeySoulStone::ModelType);
		ItemModelsProperties.registerProperty(FairyItems.soulGael, new ResourceLocation("filled"), FeySoulStone::ModelFilled);
		ItemModelsProperties.registerProperty(FairyItems.soulGael, new ResourceLocation("type_idx"), FeySoulStone::ModelType);
		ItemModelsProperties.registerProperty(FairyItems.soulJar, new ResourceLocation("filled"), SoulJar::ModelFilled);
		ItemModelsProperties.registerProperty(FairyItems.templateWand, new ResourceLocation("mode"), TemplateWand::ModelMode);
	}
	
	private static final void registerBlockRenderLayer() {
		RenderTypeLookup.setRenderLayer(FairyBlocks.bufferChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.buildingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.dwarfCraftingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.elfCraftingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.gnomeCraftingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.farmingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.feyBush, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.dwarfHome, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.elfHome, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.fairyHome, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.gnomeHome, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.gatheringBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.inputChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.logisticsPylon, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.logisticsSensor, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.magicLightUnlit, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.magicLightDim, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.magicLightMedium, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.magicLightBright, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.miningBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.outputChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.outputPanel, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.storageChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.reinforcedIronChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.reinforcedGoldChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.reinforcedDiamondChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.storageMonitor, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.templateBlock, (l) -> true);
		RenderTypeLookup.setRenderLayer(FairyBlocks.woodcuttingBlock, RenderType.getCutout());
	}
	
	private static final void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.TestFairy, (manager) -> new RenderTestFairy(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.Fairy, (manager) -> new RenderFairy(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.PersonalFairy, (manager) -> new RenderFairy(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.Dwarf, (manager) -> new RenderDwarf<>(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.Elf, (manager) -> new RenderElf<>(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.Gnome, (manager) -> new RenderGnome(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.ElfArcher, (manager) -> new RenderElfArcher(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.ShadowFey, (manager) -> new RenderShadowFey(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.ElfCrafter, (manager) -> new RenderElfCrafter(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.DwarfCrafter, (manager) -> new RenderDwarfCrafter(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.DwarfBuilder, (manager) -> new RenderDwarfBuilder(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.GnomeCrafter, (manager) -> new RenderGnome(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.GnomeCollector, (manager) -> new RenderGnome(manager, 1.0f));
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.ArrowEx, (manager) -> new TippedArrowRenderer(manager));
	}
	
	@SubscribeEvent
	public static void onModelBake(ModelBakeEvent event) {
		//final Map<ResourceLocation, IBakedModel> registry = event.getModelRegistry();
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void stitchEventPre(TextureStitchEvent.Pre event) {
		// Note: called multiple times for different texture atlases.
		// Using what Botania does
		if(event.getMap().getTextureLocation() != AtlasTexture.LOCATION_BLOCKS_TEXTURE) {
			return;
		}
		
		// We have to request loading textures that aren't explicitly loaded by any of the normal registered models.
		// That means entity OBJ models, or textures we load on the fly, etc.
		event.addSprite(new ResourceLocation(NostrumFairies.MODID, "block/logistics_gathering_block_icon"));
		event.addSprite(new ResourceLocation(NostrumFairies.MODID, "block/logistics_farming_block_icon"));
		event.addSprite(new ResourceLocation(NostrumFairies.MODID, "block/logistics_building_block_icon"));
		event.addSprite(new ResourceLocation(NostrumFairies.MODID, "block/logistics_mining_block_icon"));
		event.addSprite(new ResourceLocation(NostrumFairies.MODID, "block/logistics_woodcutting_block_icon"));
	}
}
