package com.smanzana.nostrumfairies.effect;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FeyVisibilityEffect extends Effect {
	
	public static final String ID = "potions-feyvisibility";
	
	public FeyVisibilityEffect() {
		super(EffectType.BENEFICIAL, 0xFFEFFF63);
		
		//this.setPotionName("potion.fey-visibility.name");
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // we don't actually do anything
	}

	@Override
	public void performEffect(LivingEntity entity, int amp) {
		;
    }
	
	public String getEffectName() {
		return "fey-visibility";
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		PotionIcon.FEY_VISIBILITY.draw(matrixStackIn, Minecraft.getInstance(), x + 6, y + 7);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		PotionIcon.FEY_VISIBILITY.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
	}
	
}
