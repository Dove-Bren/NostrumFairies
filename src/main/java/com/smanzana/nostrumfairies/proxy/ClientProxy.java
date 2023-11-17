package com.smanzana.nostrumfairies.proxy;

import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.OverlayRenderer;
import com.smanzana.nostrumfairies.client.gui.StorageMonitorScreen;
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
import com.smanzana.nostrumfairies.client.render.tile.FeySignRenderer;
import com.smanzana.nostrumfairies.client.render.tile.GatheringBlockRenderer;
import com.smanzana.nostrumfairies.client.render.tile.InputChestRenderer;
import com.smanzana.nostrumfairies.client.render.tile.LogisticsSensorRenderer;
import com.smanzana.nostrumfairies.client.render.tile.MiningBlockRenderer;
import com.smanzana.nostrumfairies.client.render.tile.OutputChestRenderer;
import com.smanzana.nostrumfairies.client.render.tile.PylonRenderer;
import com.smanzana.nostrumfairies.client.render.tile.StorageChestRenderer;
import com.smanzana.nostrumfairies.client.render.tile.StorageMonitorRenderer;
import com.smanzana.nostrumfairies.client.render.tile.TemplateBlockRenderer;
import com.smanzana.nostrumfairies.client.render.tile.WoodcuttingBlockRenderer;
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
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilityRequest;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.tiles.BufferChestTileEntity;
import com.smanzana.nostrumfairies.tiles.BuildingBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.CraftingBlockDwarfTileEntity;
import com.smanzana.nostrumfairies.tiles.CraftingBlockElfTileEntity;
import com.smanzana.nostrumfairies.tiles.CraftingBlockGnomeTileEntity;
import com.smanzana.nostrumfairies.tiles.FarmingBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.GatheringBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.InputChestTileEntity;
import com.smanzana.nostrumfairies.tiles.LogisticsSensorTileEntity;
import com.smanzana.nostrumfairies.tiles.MiningBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.OutputChestTileEntity;
import com.smanzana.nostrumfairies.tiles.PylonTileEntity;
import com.smanzana.nostrumfairies.tiles.StorageChestTileEntity;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;
import com.smanzana.nostrumfairies.tiles.TemplateBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.WoodcuttingBlockTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.model.MimicBlockBakedModel;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy extends CommonProxy {
	
	protected OverlayRenderer overlayRenderer;
	
	private KeyBinding bindingScroll;
	private KeyBinding bindingWandModeForward;
	private KeyBinding bindingWandModeBackward;

	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void preinit() {
		super.preinit();
		
		bindingScroll = new KeyBinding("key.wandscroll.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingScroll);
		bindingWandModeForward = new KeyBinding("key.wandmode.forward.desc", GLFW.GLFW_KEY_RIGHT_BRACKET, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingWandModeForward);
		bindingWandModeBackward = new KeyBinding("key.wandmode.backward.desc", GLFW.GLFW_KEY_LEFT_BRACKET, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingWandModeBackward);
	}
	
	@Override
	public void init() {
		super.init();
	}
	
	@Override
	public void postinit() {
		super.postinit();
		
		this.overlayRenderer = new OverlayRenderer();
	}
	
	@SubscribeEvent
	public void clientSetup(FMLClientSetupEvent event) {
		
		ClientRegistry.bindTileEntitySpecialRenderer(StorageMonitorTileEntity.class, new StorageMonitorRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(StorageChestTileEntity.class, new StorageChestRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(BufferChestTileEntity.class, new BufferChestRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(OutputChestTileEntity.class, new OutputChestRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(InputChestTileEntity.class, new InputChestRenderer());
		FeySignRenderer.init(GatheringBlockTileEntity.class, new GatheringBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(GatheringBlockTileEntity.class, new GatheringBlockRenderer());
		FeySignRenderer.init(FarmingBlockTileEntity.class, new FarmingBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(FarmingBlockTileEntity.class, new FarmingBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(PylonTileEntity.class, new PylonRenderer());
		FeySignRenderer.init(WoodcuttingBlockTileEntity.class, new WoodcuttingBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(WoodcuttingBlockTileEntity.class, new WoodcuttingBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(MiningBlockTileEntity.class, new MiningBlockRenderer());
		FeySignRenderer.init(MiningBlockTileEntity.class, new MiningBlockRenderer());
		FeySignRenderer.init(BuildingBlockTileEntity.class, new BuildingBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(BuildingBlockTileEntity.class, new BuildingBlockRenderer());
		StaticTESRRenderer.instance.registerRender(TemplateBlockTileEntity.class, new TemplateBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockDwarfTileEntity.class, new CraftingBlockDwarfRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockElfTileEntity.class, new CraftingBlockElfRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockGnomeTileEntity.class, new CraftingBlockGnomeRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(LogisticsSensorTileEntity.class, new LogisticsSensorRenderer());
		
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
	}
	
	@SubscribeEvent
	public void registerColorHandlers(ColorHandlerEvent.Block event) {
		// I could imagine registering the same colorer that mimic block has for template block, since it descends
	}
	
	@SubscribeEvent
	public void registerAllModels(ModelRegistryEvent event) {
		registerEntityRenderers();
	}
	
	private void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTestFairy.class, new IRenderFactory<EntityTestFairy>() {
			@Override
			public EntityRenderer<? super EntityTestFairy> createRenderFor(EntityRendererManager manager) {
				return new RenderTestFairy(manager, 1.0f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityFairy.class, new IRenderFactory<EntityFairy>() {
			@Override
			public EntityRenderer<? super EntityFairy> createRenderFor(EntityRendererManager manager) {
				return new RenderFairy(manager, 1.0f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityDwarf.class, new IRenderFactory<EntityDwarf>() {
			@Override
			public EntityRenderer<? super EntityDwarf> createRenderFor(EntityRendererManager manager) {
				return new RenderDwarf<>(manager, 1.0f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityElf.class, new IRenderFactory<EntityElf>() {
			@Override
			public EntityRenderer<? super EntityElf> createRenderFor(EntityRendererManager manager) {
				return new RenderElf<>(manager, 1.0f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGnome.class, new IRenderFactory<EntityGnome>() {
			@Override
			public EntityRenderer<? super EntityGnome> createRenderFor(EntityRendererManager manager) {
				return new RenderGnome(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityElfArcher.class, new IRenderFactory<EntityElfArcher>() {
			@Override
			public EntityRenderer<? super EntityElfArcher> createRenderFor(EntityRendererManager manager) {
				return new RenderElfArcher(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityShadowFey.class, new IRenderFactory<EntityShadowFey>() {
			@Override
			public EntityRenderer<? super EntityShadowFey> createRenderFor(EntityRendererManager manager) {
				return new RenderShadowFey(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityElfCrafter.class, new IRenderFactory<EntityElfCrafter>() {
			@Override
			public EntityRenderer<? super EntityElfCrafter> createRenderFor(EntityRendererManager manager) {
				return new RenderElfCrafter(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityDwarfCrafter.class, new IRenderFactory<EntityDwarfCrafter>() {
			@Override
			public EntityRenderer<? super EntityDwarfCrafter> createRenderFor(EntityRendererManager manager) {
				return new RenderDwarfCrafter(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityDwarfBuilder.class, new IRenderFactory<EntityDwarfBuilder>() {
			@Override
			public EntityRenderer<? super EntityDwarfBuilder> createRenderFor(EntityRendererManager manager) {
				return new RenderDwarfBuilder(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityGnomeCrafter.class, new IRenderFactory<EntityGnomeCrafter>() {
			@Override
			public EntityRenderer<? super EntityGnomeCrafter> createRenderFor(EntityRendererManager manager) {
				return new RenderGnome(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityGnomeCollector.class, new IRenderFactory<EntityGnomeCollector>() {
			@Override
			public EntityRenderer<? super EntityGnomeCollector> createRenderFor(EntityRendererManager manager) {
				return new RenderGnome(manager, 1.0f);
			}
		});
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public PlayerEntity getPlayer() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		final Map<ResourceLocation, IBakedModel> registry = event.getModelRegistry();
		
		for (BlockState state : FairyBlocks.templateBlock.getStateContainer().getValidStates()) {
			ModelResourceLocation loc = BlockModelShapes.getModelLocation(state);
			registry.put(loc, new MimicBlockBakedModel(registry.get(loc))); // Put a new mimic model wrapped around the default one
		}
	}
	
	@SubscribeEvent
	public void stitchEventPre(TextureStitchEvent.Pre event) {
		// Note: called multiple times for different texture atlases.
		// Using what Botania does
		if(event.getMap() != Minecraft.getInstance().getTextureMap()) {
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
	
	@SubscribeEvent
	public void onClientConnect(EntityJoinWorldEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		if (event.getEntity() == mc.player) {
			// Every time we join a world, request a copy of its networks
			
			NostrumFairies.logger.info("Requested automatic logistics network refresh");
			NetworkHandler.sendToServer(new LogisticsUpdateRequest(null));
			NostrumFairies.proxy.requestCapabilityRefresh();
			StaticTESRRenderer.instance.clear();
		}
	}
	
	@Override
	public void requestCapabilityRefresh() {
		NetworkHandler.sendToServer(new CapabilityRequest());
	}
	
	@Override
	public void pushCapabilityRefresh(PlayerEntity player) {
		if (!player.world.isRemote) {
			super.pushCapabilityRefresh(player);
		}
		; // Nothing on client
	}
	
	@Override
	public void openStorageMonitor(World world, BlockPos pos) {
		if (world.isRemote())
		{
			StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) world.getTileEntity(pos);
			Minecraft.getInstance().displayGuiScreen(new StorageMonitorScreen(monitor));
		}
	}
	
	@SubscribeEvent
	public void onMouse(MouseScrollEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		int wheel = event.getMouseY() < 0 ? -1 : event.getMouseY() > 0 ? 1 : 0;
		if (wheel != 0) {
			if (!NostrumFairies.getFeyWrapper(player)
					.builderFairyUnlocked()) {
				return;
			}
			
			if (bindingScroll.isKeyDown()) {
				ItemStack wand = player.getHeldItemMainhand();
				if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || TemplateWand.GetWandMode(wand) != WandMode.SPAWN) {
					wand = player.getHeldItemOffhand();
				}
				
				if (!wand.isEmpty() && wand.getItem() instanceof TemplateWand && TemplateWand.GetWandMode(wand) == WandMode.SPAWN) {
					TemplateWand.HandleScroll(player, wand, wheel > 0);
					event.setCanceled(true);
					return;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		final boolean forwardPressed = bindingWandModeForward.isPressed(); 
		if (forwardPressed || bindingWandModeBackward.isPressed()) {
			final INostrumMagic magic = NostrumMagica.getMagicWrapper(player);
			if (magic == null || !magic.getCompletedResearches().contains("logistics_construction") ) {
				return;
			}
			
			ItemStack wand = player.getHeldItemMainhand();
			if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand)) {
				wand = player.getHeldItemOffhand();
			}
			
			if (!wand.isEmpty() && wand.getItem() instanceof TemplateWand) {
				TemplateWand.HandleModeChange(player, wand, forwardPressed);
				return;
			}
		}
	}
	
}
