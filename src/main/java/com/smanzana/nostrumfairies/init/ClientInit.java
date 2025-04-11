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

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
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
		
		MenuScreens.register(FairyContainers.BufferChest, BufferChestGui.BufferChestGuiContainer::new);
		MenuScreens.register(FairyContainers.BuildingBlock, BuildingBlockGui.BuildingBlockGuiContainer::new);
		MenuScreens.register(FairyContainers.CraftingStation, CraftingStationGui.CraftingStationGuiContainer::new);
		MenuScreens.register(FairyContainers.CraftingStationSmall, CraftingStationSmallGui.CraftingStationSmallGuiContainer::new);
		MenuScreens.register(FairyContainers.FairyScreen, FairyScreenGui.FairyScreenGuiContainer::new);
		MenuScreens.register(FairyContainers.HomeBlock, HomeBlockGui.HomeBlockGuiContainer::new);
		MenuScreens.register(FairyContainers.InputChest, InputChestGui.InputChestGuiContainer::new);
		MenuScreens.register(FairyContainers.LogisticsSensor, LogisticsSensorGui.LogisticsSensorGuiContainer::new);
		MenuScreens.register(FairyContainers.OutputChest, OutputChestGui.OutputChestGuiContainer::new);
		MenuScreens.register(FairyContainers.OutputPanel, OutputPanelGui.OutputPanelGuiContainer::new);
		MenuScreens.register(FairyContainers.StorageChest, StorageChestGui.StorageChestGuiContainer::new);
		MenuScreens.register(FairyContainers.TemplateWand, TemplateWandGui.TemplateWandGuiContainer::new);
		
		registerBlockRenderLayer();
		registerEntityRenderers();
		
		event.enqueueWork(ClientInit::registerItemModelProperties);

		ClientProxy proxy = (ClientProxy) NostrumFairies.proxy;
		proxy.initKeybinds();
		
		overlayRenderer = new OverlayRenderer();
	}
	
	private static final void registerItemModelProperties() {
		ItemProperties.register(FairyItems.soulGem, new ResourceLocation("filled"), FeySoulStone::ModelFilled);
		ItemProperties.register(FairyItems.soulGem, new ResourceLocation("type_idx"), FeySoulStone::ModelType);
		ItemProperties.register(FairyItems.soulGael, new ResourceLocation("filled"), FeySoulStone::ModelFilled);
		ItemProperties.register(FairyItems.soulGael, new ResourceLocation("type_idx"), FeySoulStone::ModelType);
		ItemProperties.register(FairyItems.soulJar, new ResourceLocation("filled"), SoulJar::ModelFilled);
		ItemProperties.register(FairyItems.templateWand, new ResourceLocation("mode"), TemplateWand::ModelMode);
	}
	
	private static final void registerBlockRenderLayer() {
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.bufferChest, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.buildingBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.dwarfCraftingBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.elfCraftingBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.gnomeCraftingBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.farmingBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.feyBush, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.dwarfHome, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.elfHome, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.fairyHome, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.gnomeHome, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.gatheringBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.inputChest, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.logisticsPylon, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.logisticsSensor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.magicLightUnlit, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.magicLightDim, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.magicLightMedium, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.magicLightBright, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.miningBlock, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.outputChest, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.outputPanel, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.storageChest, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.reinforcedIronChest, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.reinforcedGoldChest, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.reinforcedDiamondChest, RenderType.solid());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.storageMonitor, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.templateBlock, (l) -> true);
		ItemBlockRenderTypes.setRenderLayer(FairyBlocks.woodcuttingBlock, RenderType.cutout());
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
		RenderingRegistry.registerEntityRenderingHandler(FairyEntities.ArrowEx, (manager) -> new TippableArrowRenderer(manager));
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
		if(event.getMap().location() != TextureAtlas.LOCATION_BLOCKS) {
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
