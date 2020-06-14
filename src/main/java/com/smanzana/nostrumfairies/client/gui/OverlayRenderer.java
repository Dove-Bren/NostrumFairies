package com.smanzana.nostrumfairies.client.gui;

import java.util.Collection;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OverlayRenderer extends Gui {

	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Pre event) {
//		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
//		ScaledResolution scaledRes = event.getResolution();
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Post event) {
//		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
//		ScaledResolution scaledRes = event.getResolution();
		
		Collection<LogisticsNetwork> networks = NostrumFairies.instance.getLogisticsRegistry().getNetworks();
		if (!networks.isEmpty()) {
			Minecraft.getMinecraft().fontRendererObj.drawString("Network(s) ("
					+ networks.size()					
					+ ") alive and well!", 20, 20, 0xFFFFFFFF);
//			try {
//				LogisticsNetwork network = networks.iterator().next();
//				int y = 30;
//				List<ItemDeepStack> items = network.getCondensedNetworkItems();
//				if (!items.isEmpty()) {
//					for (ItemDeepStack stack : items) {
//						Minecraft.getMinecraft().fontRendererObj.drawString("- "
//								+ stack.getTemplate().getUnlocalizedName() + " x" + stack.getCount(),
//								25, y, 0xFFFFFFFF);
//						y += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 2;
//					}
//				}
//			} catch (Exception e) {
//				;
//			}
		}
		
		
		
	}
	
}
