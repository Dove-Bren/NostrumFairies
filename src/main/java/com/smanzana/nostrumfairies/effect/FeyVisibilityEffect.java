package com.smanzana.nostrumfairies.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class FeyVisibilityEffect extends MobEffect {
	
	public static final String ID = "potions-feyvisibility";
	
	public FeyVisibilityEffect() {
		super(MobEffectCategory.BENEFICIAL, 0xFFEFFF63);
		
		//this.setPotionName("potion.fey-visibility.name");
	}
	
	public boolean isDurationEffectTick(int duration, int amp) {
		return false; // we don't actually do anything
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amp) {
		;
    }
	
	public String getEffectName() {
		return "fey-visibility";
	}
	
//	@OnlyIn(Dist.CLIENT)
//	@Override
//    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack matrixStackIn, int x, int y, float z) {
//		PotionIcon.FEY_VISIBILITY.draw(matrixStackIn, Minecraft.getInstance(), x + 6, y + 7);
//	}
//	
//	@OnlyIn(Dist.CLIENT)
//	@Override
//    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui, PoseStack matrixStackIn, int x, int y, float z, float alpha) {
//		PotionIcon.FEY_VISIBILITY.draw(matrixStackIn, Minecraft.getInstance(), x + 3, y + 3);
//	}
	
}
