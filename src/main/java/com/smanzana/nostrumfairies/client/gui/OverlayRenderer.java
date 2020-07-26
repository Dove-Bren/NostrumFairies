package com.smanzana.nostrumfairies.client.gui;

import java.util.Collection;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;

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
			try {
				LogisticsNetwork network = networks.iterator().next();
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
				
				Collection<ILogisticsTask> tasks = network.getTaskRegistry().allTasks();
				int y = 40;
				for (ILogisticsTask task : tasks) {
					String str = "Task: " + task.getDisplayName();
					Minecraft.getMinecraft().fontRendererObj.drawString(str, 60, y, 0xFFFFFFFF);
					y += 8;
//					
//					if (task instanceof LogisticsItemWithdrawTask) {
//						LogisticsItemWithdrawTask retrieve = (LogisticsItemWithdrawTask) task;
//						if (retrieve.isActive()) {
//							str = " (ACTIVE: " + retrieve.getCurrentWorker() + ")";
//						} else {
//							str = " (INACTIVE)";
//						}
//						Minecraft.getMinecraft().fontRendererObj.drawString(str, -120, y, 0xFFFFFFFF);
//						y += 8;
//						str = "no subtask";
//						if (retrieve.getActiveSubtask() != null) {
//							str = retrieve.getDisplayName();
//							str += " (" + retrieve.getActiveSubtask().getPos() + ")";
//						}
//						Minecraft.getMinecraft().fontRendererObj.drawString(str, 40, y, 0xFFFFFFFF);
//						y += 8;
//					}
				}
				
			} catch (Exception e) {
				;
			}
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.ICONS);
		
	}
	
}
