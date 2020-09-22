package com.smanzana.nostrumfairies.proxy;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.BufferLogisticsChest;
import com.smanzana.nostrumfairies.blocks.FarmingBlock;
import com.smanzana.nostrumfairies.blocks.FeyBush;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
import com.smanzana.nostrumfairies.blocks.GatheringBlock;
import com.smanzana.nostrumfairies.blocks.InputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.LogisticsPylon;
import com.smanzana.nostrumfairies.blocks.MiningBlock;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageMonitor;
import com.smanzana.nostrumfairies.blocks.WoodcuttingBlock;
import com.smanzana.nostrumfairies.client.gui.OverlayRenderer;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;
import com.smanzana.nostrumfairies.entity.render.RenderDwarf;
import com.smanzana.nostrumfairies.entity.render.RenderElf;
import com.smanzana.nostrumfairies.entity.render.RenderElfArcher;
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
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilityRequest;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {
	
	protected OverlayRenderer overlayRenderer;

	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void preinit() {
		super.preinit();
		
		StorageMonitor.StorageMonitorRenderer.init();
		StorageLogisticsChest.StorageChestRenderer.init();
		BufferLogisticsChest.BufferChestRenderer.init();
		OutputLogisticsChest.OutputChestRenderer.init();
		InputLogisticsChest.InputChestRenderer.init();
		GatheringBlock.GatheringBlockRenderer.init();
		LogisticsPylon.PylonRenderer.init();
		WoodcuttingBlock.WoodcuttingBlockRenderer.init();
		MiningBlock.MiningBlockRenderer.init();
		
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
		
		List<ItemStack> stones = new LinkedList<>();
		FeyStone.instance().getSubItems(FeyStone.instance(), null, stones);
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
		
		variants = new ResourceLocation[SoulStoneType.values().length * 2];
		i = 0;
		for (SoulStoneType type : SoulStoneType.values()) {
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FeySoulStone.instance().getModelName(type));
			variants[i++] = new ResourceLocation(NostrumFairies.MODID,
					FeySoulStone.instance().getModelName(type) + "_filled");
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
	}
	
	@Override
	public void init() {
		super.init();
		
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
		for (ResidentType type : ResidentType.values()) {
			registerModel(Item.getItemFromBlock(FeyHomeBlock.instance(type)),
					0,
					FeyHomeBlock.ID(type));
		}
		
		
		List<ItemStack> stones = new LinkedList<>();
		FeyStone.instance().getSubItems(FeyStone.instance(), null, stones);
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
		
		for (FeySoulStone.SoulStoneType type : FeySoulStone.SoulStoneType.values()) {
			registerModel(FeySoulStone.instance(), FeySoulStone.create(type).getMetadata(), FeySoulStone.instance().getModelName(type));
			registerModel(FeySoulStone.instance(), FeySoulStone.create(type).getMetadata() | 1, FeySoulStone.instance().getModelName(type) + "_filled");
		}
		
		for (FairyGael.FairyGaelType type : FairyGael.FairyGaelType.values()) {
			registerModel(FairyGael.instance(), type.ordinal() << 1, FairyGael.instance().getModelName(type, false));
			registerModel(FairyGael.instance(), type.ordinal() << 1 | 1, FairyGael.instance().getModelName(type, true));
		}
	}
	
	@Override
	public void postinit() {
		super.postinit();
		
		this.overlayRenderer = new OverlayRenderer();
	}
	
	public static void registerModel(Item item, int meta, String modelName) {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
    	.register(item, meta,
    			new ModelResourceLocation(NostrumFairies.MODID + ":" + modelName, "inventory"));
	}
	

	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().thePlayer;
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
	
//	@SubscribeEvent
//	public void onModelBake(ModelBakeEvent event) {
//    	for (ClientEffectIcon icon: ClientEffectIcon.values()) {
//    		IModel model;
//			try {
//				model = ModelLoaderRegistry.getModel(new ResourceLocation(
//						NostrumMagica.MODID, "effect/" + icon.getModelKey()
//						));
//				IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, 
//	    				(location) -> {return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());});
//	    		event.getModelRegistry().putObject(
//	    				new ModelResourceLocation(NostrumMagica.MODID + ":effects/" + icon.getKey(), "normal"),
//	    				bakedModel);
//			} catch (Exception e) {
//				e.printStackTrace();
//				NostrumMagica.logger.warn("Failed to load effect " + icon.getKey());
//			}
//    		
//    	}
//	}
	
	@SubscribeEvent
	public void onClientConnect(EntityJoinWorldEvent event) {
		if (event.getEntity() == Minecraft.getMinecraft().thePlayer) {
			// Every time we join a world, request a copy of its networks
			
			NostrumFairies.logger.info("Requested automatic logistics network refresh");
			NetworkHandler.getSyncChannel().sendToServer(new LogisticsUpdateRequest());
			NostrumFairies.proxy.requestCapabilityRefresh();
		}
	}
	
	@Override
	public void requestCapabilityRefresh() {
		NetworkHandler.getSyncChannel().sendToServer(new CapabilityRequest());
	}
	
	@Override
	public void pushCapabilityRefresh(EntityPlayer player) {
		; // Nothing on client
	}
	
}
