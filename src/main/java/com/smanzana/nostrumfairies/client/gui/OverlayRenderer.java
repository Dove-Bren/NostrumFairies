package com.smanzana.nostrumfairies.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends GuiComponent {
	
	protected IIngameOverlay templateNameOverlay;

	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void registerLayers() {
		templateNameOverlay = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CROSSHAIR_ELEMENT, "NostrumFairies::templateNameOverlay", this::renderTemplateNameOverlay);
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
	
	private void renderTemplateNameOverlay(ForgeIngameGui gui, PoseStack matrixStackIn, float partialTicks, int width, int height) {
		final Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		
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
				matrixStackIn.translate(
						((double) width / 2),
						((double) height / 2) + 10,
						0);
				renderCurrentIndex(matrixStackIn, name);
				matrixStackIn.popPose();
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
