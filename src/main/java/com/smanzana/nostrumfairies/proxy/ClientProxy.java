package com.smanzana.nostrumfairies.proxy;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;
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
import com.smanzana.nostrumfairies.blocks.MiningBlock;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsPanel;
import com.smanzana.nostrumfairies.blocks.ReinforcedStorageLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageMonitor;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.blocks.WoodcuttingBlock;
import com.smanzana.nostrumfairies.client.gui.OverlayRenderer;
import com.smanzana.nostrumfairies.client.model.TemplateBlockBakedModel;
import com.smanzana.nostrumfairies.client.render.BufferChestRenderer;
import com.smanzana.nostrumfairies.client.render.BuildingBlockRenderer;
import com.smanzana.nostrumfairies.client.render.CraftingBlockDwarfRenderer;
import com.smanzana.nostrumfairies.client.render.CraftingBlockElfRenderer;
import com.smanzana.nostrumfairies.client.render.CraftingBlockGnomeRenderer;
import com.smanzana.nostrumfairies.client.render.FarmingBlockRenderer;
import com.smanzana.nostrumfairies.client.render.GatheringBlockRenderer;
import com.smanzana.nostrumfairies.client.render.InputChestRenderer;
import com.smanzana.nostrumfairies.client.render.LogisticsSensorRenderer;
import com.smanzana.nostrumfairies.client.render.MiningBlockRenderer;
import com.smanzana.nostrumfairies.client.render.OutputChestRenderer;
import com.smanzana.nostrumfairies.client.render.PylonRenderer;
import com.smanzana.nostrumfairies.client.render.StorageChestRenderer;
import com.smanzana.nostrumfairies.client.render.StorageMonitorRenderer;
import com.smanzana.nostrumfairies.client.render.TemplateBlockRenderer;
import com.smanzana.nostrumfairies.client.render.WoodcuttingBlockRenderer;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
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
import com.smanzana.nostrumfairies.entity.render.RenderDwarf;
import com.smanzana.nostrumfairies.entity.render.RenderDwarfBuilder;
import com.smanzana.nostrumfairies.entity.render.RenderDwarfCrafter;
import com.smanzana.nostrumfairies.entity.render.RenderElf;
import com.smanzana.nostrumfairies.entity.render.RenderElfArcher;
import com.smanzana.nostrumfairies.entity.render.RenderElfCrafter;
import com.smanzana.nostrumfairies.entity.render.RenderFairy;
import com.smanzana.nostrumfairies.entity.render.RenderGnome;
import com.smanzana.nostrumfairies.entity.render.RenderShadowFey;
import com.smanzana.nostrumfairies.entity.render.RenderTestFairy;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.items.FairyInstrument;
import com.smanzana.nostrumfairies.items.FairyInstrument.InstrumentType;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.SoulJar;
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilityRequest;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

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
		
		bindingScroll = new KeyBinding("key.wandscroll.desc", Keyboard.KEY_LSHIFT, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingScroll);
		bindingWandModeForward = new KeyBinding("key.wandmode.forward.desc", Keyboard.KEY_RBRACKET, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingWandModeForward);
		bindingWandModeBackward = new KeyBinding("key.wandmode.backward.desc", Keyboard.KEY_LBRACKET, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingWandModeBackward);
		
		StorageMonitorRenderer.init();
		StorageChestRenderer.init();
		BufferChestRenderer.init();
		OutputChestRenderer.init();
		InputChestRenderer.init();
		GatheringBlockRenderer.init();
		FarmingBlockRenderer.init();
		PylonRenderer.init();
		WoodcuttingBlockRenderer.init();
		MiningBlockRenderer.init();
		BuildingBlockRenderer.init();
		TemplateBlockRenderer.init();
		CraftingBlockDwarfRenderer.init();
		CraftingBlockElfRenderer.init();
		CraftingBlockGnomeRenderer.init();
		LogisticsSensorRenderer.init();
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
	
	public static void registerModel(Item item, int meta, String modelName) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(NostrumFairies.MODID + ":" + modelName, "inventory"));
	}
	
	@SubscribeEvent
	public void registerAllModels(ModelRegistryEvent event) {
		
		registerItemVariants(event);
		
		//registerModel(SpellTome.instance(), 0, SpellTome.id);
		registerModel(Item.getItemFromBlock(StorageLogisticsChest.instance()),
				0,
				StorageLogisticsChest.ID);
		registerModel(Item.getItemFromBlock(BufferLogisticsChest.instance()),
				0,
				BufferLogisticsChest.ID);
		registerModel(Item.getItemFromBlock(OutputLogisticsChest.instance()),
				0,
				OutputLogisticsChest.ID);
		registerModel(Item.getItemFromBlock(StorageMonitor.instance()),
				0,
				StorageMonitor.ID);
		registerModel(Item.getItemFromBlock(InputLogisticsChest.instance()),
				0,
				InputLogisticsChest.ID);
		registerModel(Item.getItemFromBlock(GatheringBlock.instance()),
				0,
				GatheringBlock.ID);
		registerModel(Item.getItemFromBlock(LogisticsPylon.instance()),
				0,
				LogisticsPylon.ID);
		registerModel(Item.getItemFromBlock(WoodcuttingBlock.instance()),
				0,
				WoodcuttingBlock.ID);
		registerModel(Item.getItemFromBlock(MiningBlock.instance()),
				0,
				MiningBlock.ID);
		registerModel(Item.getItemFromBlock(FarmingBlock.instance()),
				0,
				FarmingBlock.ID);
		registerModel(Item.getItemFromBlock(BuildingBlock.instance()),
				0,
				BuildingBlock.ID);
		for (ResidentType type : ResidentType.values()) {
			registerModel(Item.getItemFromBlock(FeyHomeBlock.instance(type)),
					0,
					FeyHomeBlock.ID(type));
		}
		
		
		NonNullList<ItemStack> stones = NonNullList.create();
		FeyStone.instance().getSubItems(FeyStone.instance().getCreativeTab(), stones);
		for (ItemStack stone : stones) {
			registerModel(FeyStone.instance(), stone.getMetadata(), FeyStone.instance().getModelName(stone));
		}
		
		for (FeyResourceType type : FeyResourceType.values()) {
			registerModel(FeyResource.instance(), FeyResource.create(type, 1).getMetadata(), FeyResource.instance().getModelName(type));
		}
		
		registerModel(Item.getItemFromBlock(FeyBush.instance()),
				0,
				FeyBush.ID);
		
		for (FairyInstrument.InstrumentType type : FairyInstrument.InstrumentType.values()) {
			registerModel(FairyInstrument.instance(), FairyInstrument.create(type).getMetadata(), FairyInstrument.instance().getModelName(type));
		}
		
		List<ResidentType> soulTypes = Lists.newArrayList(ResidentType.values());
		soulTypes.add(null);
		for (ResidentType type : soulTypes) {
			registerModel(FeySoulStone.instance(), FeySoulStone.createEmpty(type).getMetadata(), FeySoulStone.instance().getModelName(type));
			registerModel(FeySoulStone.instance(), FeySoulStone.createFake(type).getMetadata(), FeySoulStone.instance().getModelName(type) + "_filled");
		}
		
		for (FairyGael.FairyGaelType type : FairyGael.FairyGaelType.values()) {
			registerModel(FairyGael.instance(), type.ordinal() << 1, FairyGael.instance().getModelName(type, false));
			registerModel(FairyGael.instance(), type.ordinal() << 1 | 1, FairyGael.instance().getModelName(type, true));
		}
		
		for (WandMode mode : WandMode.values()) {
			registerModel(TemplateWand.instance(), TemplateWand.metaFromMode(mode), TemplateWand.instance().getModelName(mode));
		}
		
		registerModel(TemplateScroll.instance(), 0, TemplateScroll.ID);
		
		registerModel(Item.getItemFromBlock(CraftingBlockDwarf.instance()),
				0,
				CraftingBlockDwarf.ID);
		registerModel(Item.getItemFromBlock(CraftingBlockElf.instance()),
				0,
				CraftingBlockElf.ID);
		registerModel(Item.getItemFromBlock(CraftingBlockGnome.instance()),
				0,
				CraftingBlockGnome.ID);
		registerModel(Item.getItemFromBlock(LogisticsSensorBlock.instance()),
				0,
				LogisticsSensorBlock.ID);
		
		ItemStack stack = SoulJar.createFake(false);
		registerModel(SoulJar.instance(),
				stack.getMetadata(),
				SoulJar.instance().getModelName(stack));
		stack = SoulJar.createFake(true);
		registerModel(SoulJar.instance(),
				stack.getMetadata(),
				SoulJar.instance().getModelName(stack));
		
		registerModel(Item.getItemFromBlock(OutputLogisticsPanel.instance()),
				0,
				OutputLogisticsPanel.ID);
		
		registerModel(Item.getItemFromBlock(ReinforcedStorageLogisticsChest.Iron()),
				0,
				ReinforcedStorageLogisticsChest.Iron().getID());
		registerModel(Item.getItemFromBlock(ReinforcedStorageLogisticsChest.Gold()),
				0,
				ReinforcedStorageLogisticsChest.Gold().getID());
		registerModel(Item.getItemFromBlock(ReinforcedStorageLogisticsChest.Diamond()),
				0,
				ReinforcedStorageLogisticsChest.Diamond().getID());
		
		registerEntityRenderers();
	}
	
	private void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTestFairy.class, new IRenderFactory<EntityTestFairy>() {
			@Override
			public Render<? super EntityTestFairy> createRenderFor(RenderManager manager) {
				return new RenderTestFairy(manager, 1.0f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityFairy.class, new IRenderFactory<EntityFairy>() {
			@Override
			public Render<? super EntityFairy> createRenderFor(RenderManager manager) {
				return new RenderFairy(manager, 1.0f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityDwarf.class, new IRenderFactory<EntityDwarf>() {
			@Override
			public Render<? super EntityDwarf> createRenderFor(RenderManager manager) {
				return new RenderDwarf(manager, 1.0f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityElf.class, new IRenderFactory<EntityElf>() {
			@Override
			public Render<? super EntityElf> createRenderFor(RenderManager manager) {
				return new RenderElf(manager, 1.0f);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityGnome.class, new IRenderFactory<EntityGnome>() {
			@Override
			public Render<? super EntityGnome> createRenderFor(RenderManager manager) {
				return new RenderGnome(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityElfArcher.class, new IRenderFactory<EntityElfArcher>() {
			@Override
			public Render<? super EntityElfArcher> createRenderFor(RenderManager manager) {
				return new RenderElfArcher(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityShadowFey.class, new IRenderFactory<EntityShadowFey>() {
			@Override
			public Render<? super EntityShadowFey> createRenderFor(RenderManager manager) {
				return new RenderShadowFey(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityElfCrafter.class, new IRenderFactory<EntityElfCrafter>() {
			@Override
			public Render<? super EntityElfCrafter> createRenderFor(RenderManager manager) {
				return new RenderElfCrafter(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityDwarfCrafter.class, new IRenderFactory<EntityDwarfCrafter>() {
			@Override
			public Render<? super EntityDwarfCrafter> createRenderFor(RenderManager manager) {
				return new RenderDwarfCrafter(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityDwarfBuilder.class, new IRenderFactory<EntityDwarfBuilder>() {
			@Override
			public Render<? super EntityDwarfBuilder> createRenderFor(RenderManager manager) {
				return new RenderDwarfBuilder(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityGnomeCrafter.class, new IRenderFactory<EntityGnomeCrafter>() {
			@Override
			public Render<? super EntityGnomeCrafter> createRenderFor(RenderManager manager) {
				return new RenderGnome(manager, 1.0f);
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityGnomeCollector.class, new IRenderFactory<EntityGnomeCollector>() {
			@Override
			public Render<? super EntityGnomeCollector> createRenderFor(RenderManager manager) {
				return new RenderGnome(manager, 1.0f);
			}
		});
	}
	
	private void registerItemVariants(ModelRegistryEvent event) {
		NonNullList<ItemStack> stones = NonNullList.create();
		FeyStone.instance().getSubItems(FeyStone.instance().getCreativeTab(), stones);
		ResourceLocation variants[] = new ResourceLocation[stones.size()];
		int i = 0;
		for (ItemStack stone : stones) {
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FeyStone.instance().getModelName(stone));
		}
		ModelBakery.registerItemVariants(FeyStone.instance(), variants);
		
		variants = new ResourceLocation[FeyResourceType.values().length];
		i = 0;
		for (FeyResourceType type : FeyResourceType.values()) {
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FeyResource.instance().getModelName(type));
		}
		ModelBakery.registerItemVariants(FeyResource.instance(), variants);
		
		variants = new ResourceLocation[(1 + ResidentType.values().length) * 2];
		i = 0;
		for (ResidentType type : ResidentType.values()) {
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FeySoulStone.instance().getModelName(type));
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FeySoulStone.instance().getModelName(type) + "_filled");
		}
		// Repeat for empty
		{
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FeySoulStone.instance().getModelName((ResidentType) null));
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FeySoulStone.instance().getModelName((ResidentType) null) + "_filled");
		}
		ModelBakery.registerItemVariants(FeySoulStone.instance(), variants);
		
		variants = new ResourceLocation[FairyGaelType.values().length * 2];
		i = 0;
		for (FairyGaelType type : FairyGaelType.values()) {
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FairyGael.instance().getModelName(type, false));
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FairyGael.instance().getModelName(type, true));
		}
		ModelBakery.registerItemVariants(FairyGael.instance(), variants);
		
		variants = new ResourceLocation[InstrumentType.values().length];
		i = 0;
		for (InstrumentType type : InstrumentType.values()) {
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FairyInstrument.instance().getModelName(type));
		}
		ModelBakery.registerItemVariants(FairyInstrument.instance(), variants);
		
		variants = new ResourceLocation[WandMode.values().length];
		i = 0;
		for (WandMode type : WandMode.values()) {
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					TemplateWand.instance().getModelName(type));
		}
		ModelBakery.registerItemVariants(TemplateWand.instance(), variants);
		
		variants = new ResourceLocation[2];
		variants[0] = new ResourceLocation(NostrumFairies.MODID,
				SoulJar.instance().getModelName(SoulJar.createFake(false)));
		variants[1] = new ResourceLocation(NostrumFairies.MODID,
				SoulJar.instance().getModelName(SoulJar.createFake(true)));
		ModelBakery.registerItemVariants(SoulJar.instance(), variants);
	}
	

	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().player;
	}
	
//	@SubscribeEvent
//	public void stitchEventPre(TextureStitchEvent.Pre event) {
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/koid"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/golem_ender"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/dragon_C"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/sprite_core"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/sprite_arms"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "entity/magic_blade"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "blocks/portal"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/item/blade"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/item/hilt"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/item/ruby"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/item/wood"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/white"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/crystal"));
//		event.getMap().registerSprite(new ResourceLocation(
//				NostrumMagica.MODID, "models/crystal_blank"));
//	}
	
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		TemplateBlockBakedModel model = new TemplateBlockBakedModel();
		event.getModelRegistry().putObject(new ModelResourceLocation(new ResourceLocation(NostrumFairies.MODID, TemplateBlock.ID), "normal"),
				model);
	}
	
	@SubscribeEvent
	public void onClientConnect(EntityJoinWorldEvent event) {
		if (event.getEntity() == Minecraft.getMinecraft().player) {
			// Every time we join a world, request a copy of its networks
			
			NostrumFairies.logger.info("Requested automatic logistics network refresh");
			NetworkHandler.getSyncChannel().sendToServer(new LogisticsUpdateRequest());
			NostrumFairies.proxy.requestCapabilityRefresh();
			StaticTESRRenderer.instance.clear();
		}
	}
	
	@Override
	public void requestCapabilityRefresh() {
		NetworkHandler.getSyncChannel().sendToServer(new CapabilityRequest());
	}
	
	@Override
	public void pushCapabilityRefresh(EntityPlayer player) {
		if (!player.world.isRemote) {
			super.pushCapabilityRefresh(player);
		}
		; // Nothing on client
	}
	
	@SubscribeEvent
	public void onMouse(MouseEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		int wheel = event.getDwheel();
		if (wheel != 0) {
			if (!NostrumFairies.getFeyWrapper(player)
					.builderFairyUnlocked()) {
				return;
			}
			
			if (bindingScroll.isKeyDown()) {
				ItemStack wand = player.getHeldItemMainhand();
				if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || TemplateWand.getModeOf(wand) != WandMode.SPAWN) {
					wand = player.getHeldItemOffhand();
				}
				
				if (!wand.isEmpty() && wand.getItem() instanceof TemplateWand && TemplateWand.getModeOf(wand) == WandMode.SPAWN) {
					TemplateWand.HandleScroll(player, wand, wheel > 0);
					event.setCanceled(true);
					return;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		EntityPlayer player = Minecraft.getMinecraft().player;
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
