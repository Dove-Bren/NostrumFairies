package com.smanzana.nostrumfairies.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends GuiComponent {

	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	protected boolean shouldDisplayPreview(Player player) {
		for (ItemStack stack : player.getHandSlots()) {
			if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
				continue;
			}
			
			if (TemplateWand.GetWandMode(stack) != WandMode.SPAWN) {
				continue;
			}
			
			return true;
		}
		
		return false;
	}
	
	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		final PoseStack matrixStackIn = event.getMatrixStack();
		
		// Hook into static TESR renderer
		StaticTESRRenderer.instance.render(matrixStackIn, event.getProjectionMatrix(), mc, player, event.getPartialTicks());
	}
	
	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		final Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		final PoseStack matrixStackIn = event.getMatrixStack();
		
		if (event.getType() == ElementType.CROSSHAIRS) {
			if (shouldDisplayPreview(player)) {
				String name = null;
				for (ItemStack held : player.getHandSlots()) {
					if (held.isEmpty() || !(held.getItem() instanceof TemplateWand)) {
						continue;
					}
					
					if (TemplateWand.GetWandMode(held) != WandMode.SPAWN) {
						continue;
					}
					
					ItemStack templateScroll = TemplateWand.GetSelectedTemplate(held);
					if (!templateScroll.isEmpty()) {
						name = templateScroll.getHoverName().getString();
						break;
					}
				}
				
				if (name != null) {
					
					matrixStackIn.pushPose();
					Window res = event.getWindow();
					matrixStackIn.translate(
							((double) res.getGuiScaledWidth() / 2),
							((double) res.getGuiScaledHeight() / 2) + 10,
							0);
					renderCurrentIndex(matrixStackIn, name);
					matrixStackIn.popPose();
				}
			}
		}
	}
	
	private void renderCurrentIndex(PoseStack matrixStackIn, String name) {
		if (name == null) {
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		
		GlStateManager._disableBlend();
		matrixStackIn.pushPose();
		
		drawCenteredString(matrixStackIn, mc.font, name, 0, 0, 0xFFFFFFFF);
		
		matrixStackIn.popPose();
		GlStateManager._enableBlend();
	}
}
