package com.smanzana.nostrumfairies.client.gui;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.container.FeySoulContainerSlot;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FeySoulIcon {

	private static final int ICON_WIDTH = 16;
	private static final int ICON_HEIGHT = 16;
	private static Map<SoulStoneType, ResourceLocation> TEXTS = null;
	
	private static void init() {
		if (TEXTS == null) {
			TEXTS = new EnumMap<>(SoulStoneType.class);
			for (SoulStoneType slot : SoulStoneType.values()) {
				TEXTS.put(slot, new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/slot_" + slot.suffix + ".png"));
			}
		}
	}
	
	public static void draw(MatrixStack matrixStackIn, int offsetX, int offsetY, float scale, SoulStoneType slot) {
		init();

        Minecraft.getInstance().getTextureManager().bindTexture(TEXTS.get(slot));
		
        matrixStackIn.push();
		matrixStackIn.translate(offsetX, offsetY, 0);
		matrixStackIn.scale(scale, scale, scale);
        RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, 0, 0, 0, 0, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
        matrixStackIn.pop();
	}
	
	public static void draw(MatrixStack matrixStackIn, FeySoulContainerSlot slot, float scale) {
		// offset to center based on scale, since slots always have a hover of 16x16
		int offset = (int) (16 * (1f - scale) * .5);
		draw(matrixStackIn, slot.xPos + offset, slot.yPos + offset, scale, slot.getType());
	}
}
