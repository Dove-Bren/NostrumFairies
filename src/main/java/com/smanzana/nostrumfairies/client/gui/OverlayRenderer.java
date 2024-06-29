package com.smanzana.nostrumfairies.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends AbstractGui {

	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	protected boolean shouldDisplayPreview(PlayerEntity player) {
		for (ItemStack stack : player.getHeldEquipment()) {
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
		ClientPlayerEntity player = mc.player;
		final MatrixStack matrixStackIn = event.getMatrixStack();
		
		// Hook into static TESR renderer
		StaticTESRRenderer.instance.render(matrixStackIn, event.getProjectionMatrix(), mc, player, event.getPartialTicks());
	}
	
	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		final Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		final MatrixStack matrixStackIn = event.getMatrixStack();
		
		if (event.getType() == ElementType.CROSSHAIRS) {
			if (shouldDisplayPreview(player)) {
				String name = null;
				for (ItemStack held : player.getHeldEquipment()) {
					if (held.isEmpty() || !(held.getItem() instanceof TemplateWand)) {
						continue;
					}
					
					if (TemplateWand.GetWandMode(held) != WandMode.SPAWN) {
						continue;
					}
					
					ItemStack templateScroll = TemplateWand.GetSelectedTemplate(held);
					if (!templateScroll.isEmpty()) {
						name = templateScroll.getDisplayName().getString();
						break;
					}
				}
				
				if (name != null) {
					
					matrixStackIn.push();
					MainWindow res = event.getWindow();
					matrixStackIn.translate(
							((double) res.getScaledWidth() / 2),
							((double) res.getScaledHeight() / 2) + 10,
							0);
					renderCurrentIndex(matrixStackIn, name);
					matrixStackIn.pop();
				}
			}
		}
	}
	
	private void renderCurrentIndex(MatrixStack matrixStackIn, String name) {
		if (name == null) {
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		
		GlStateManager.disableBlend();
		matrixStackIn.push();
		
		drawCenteredString(matrixStackIn, mc.fontRenderer, name, 0, 0, 0xFFFFFFFF);
		
		matrixStackIn.pop();
		GlStateManager.enableBlend();
	}
}
