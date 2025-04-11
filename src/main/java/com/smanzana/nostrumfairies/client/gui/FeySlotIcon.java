package com.smanzana.nostrumfairies.client.gui;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.container.FeyStoneContainerSlot;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
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
	
	public static void draw(PoseStack matrixStackIn, int offsetX, int offsetY, float scale, FeySlotType slot) {
		init();

        Minecraft.getInstance().getTextureManager().bind(TEXTS.get(slot));
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(offsetX, offsetY, 0);
		matrixStackIn.scale(scale, scale, scale);
        RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, 0, 0, 0, 0, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        matrixStackIn.popPose();
	}
	
	public static void draw(PoseStack matrixStackIn, FeyStoneContainerSlot slot, float scale) {
		// offset to center based on scale, since slots always have a hover of 16x16
		int offset = (int) (16 * (1f - scale) * .5);
		draw(matrixStackIn, slot.x + offset, slot.y + offset, scale, slot.getType());
	}
}
