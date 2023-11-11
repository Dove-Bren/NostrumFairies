package com.smanzana.nostrumfairies.client.gui;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.container.FeyStoneContainerSlot;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FeySlotIcon {

	private static final int ICON_WIDTH = 16;
	private static final int ICON_HEIGHT = 16;
	private static Map<FeySlotType, ResourceLocation> TEXTS = null;
	
	private static void init() {
		if (TEXTS == null) {
			TEXTS = new EnumMap<>(FeySlotType.class);
			for (FeySlotType slot : FeySlotType.values()) {
				TEXTS.put(slot, new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/slot_" + slot.getID() + ".png"));
			}
		}
	}
	
	public static void draw(int offsetX, int offsetY, float scale, FeySlotType slot) {
		init();

        Minecraft.getInstance().getTextureManager().bindTexture(TEXTS.get(slot));
		GlStateManager.disableLighting();
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(offsetX, offsetY, 0);
		GlStateManager.scalef(scale, scale, scale);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
        RenderFuncs.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        GlStateManager.popMatrix();
        
        GlStateManager.enableLighting();
	}
	
	public static void draw(FeyStoneContainerSlot slot, float scale) {
		// offset to center based on scale, since slots always have a hover of 16x16
		int offset = (int) (16 * (1f - scale) * .5);
		draw(slot.xPos + offset, slot.yPos + offset, scale, slot.getType());
	}
}
