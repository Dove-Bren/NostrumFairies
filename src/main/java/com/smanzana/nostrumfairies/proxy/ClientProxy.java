package com.smanzana.nostrumfairies.proxy;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageMonitor;
import com.smanzana.nostrumfairies.client.gui.OverlayRenderer;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
	}
	
	@Override
	public void init() {
		super.init();
		
		//registerModel(SpellTome.instance(), 0, SpellTome.id);
		registerModel(Item.getItemFromBlock(StorageLogisticsChest.instance()),
				0,
				StorageLogisticsChest.ID);
		registerModel(Item.getItemFromBlock(StorageMonitor.instance()),
				0,
				StorageMonitor.ID);
		
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
		}
	}
	
}
