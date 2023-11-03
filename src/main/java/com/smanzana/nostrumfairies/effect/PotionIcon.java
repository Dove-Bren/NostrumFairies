package com.smanzana.nostrumfairies.effect;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum PotionIcon {

	FEY_VISIBILITY(0, 0);
	
	private static final ResourceLocation text = new ResourceLocation(
			NostrumFairies.MODID, "textures/gui/potion_icons.png");
	private static final int TEXT_OFFSETU = 0;
	private static final int TEXT_OFFSETV = 0;
	private static final int TEXT_WIDTH = 256;
	private static final int TEXT_HEIGHT = 256;
	
	private int u;
	private int v;
	
	private PotionIcon(int u, int v) {
		this.u = u;
		this.v = v;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void draw(Minecraft mc, int posX, int posY) {
		mc.textureManager.bindTexture(text);
		
		RenderFuncs.drawScaledCustomSizeModalRect(posX, posY,
				TEXT_OFFSETU + (u * 32), TEXT_OFFSETV + (v * 32),
				32, 32,
				16, 16,
				TEXT_WIDTH, TEXT_HEIGHT);
	}
	
}
