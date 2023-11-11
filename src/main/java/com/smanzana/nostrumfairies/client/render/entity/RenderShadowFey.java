package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderShadowFey extends MobRenderer<EntityShadowFey, ModelElf<EntityShadowFey>> {
	
	private static ResourceLocation TEXT_SHADOW_FEY = new ResourceLocation(NostrumFairies.MODID, "textures/entity/shadow_fey.png");
	
	protected ModelElfArcher<EntityShadowFey> modelLeft;
	protected ModelElfArcher<EntityShadowFey> modelRight;
	
	public RenderShadowFey(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelElfArcher<>(true), .01f);
		this.modelLeft = new ModelElfArcher<>(true);
		this.modelRight = new ModelElfArcher<>(false);
	}

	protected ResourceLocation getEntityTexture(EntityShadowFey entity) {
		return TEXT_SHADOW_FEY;
	}
	
	@Override
	public void doRender(EntityShadowFey entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Swap out model based on the elf
		if (entity.isLeftHanded()) {
			this.entityModel = this.modelLeft;
		} else {
			this.entityModel = this.modelRight;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		
		// Model is 32/16ths of a block. Adjust to height.
		final float scale = entity.getHeight() / (32f/16f);
		final boolean morphing = entity.getMorphing();
		final float scaleModX = morphing ? NostrumFairies.random.nextFloat() * .1f : 0;
		final float scaleModY = morphing ? NostrumFairies.random.nextFloat() * .05f : 0;
		final float scaleModZ = morphing ? NostrumFairies.random.nextFloat() * .1f : 0;
		GlStateManager.disableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.color4f(1f, 1f, 1f, .7f);
		GlStateManager.scalef(scale + scaleModX, scale + scaleModY, scale + scaleModZ);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		GlStateManager.popMatrix();
	}
	
}
