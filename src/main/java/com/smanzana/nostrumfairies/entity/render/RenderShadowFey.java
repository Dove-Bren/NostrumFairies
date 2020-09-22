package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderShadowFey extends RenderLiving<EntityShadowFey> {
	
	private static ResourceLocation TEXT_SHADOW_FEY = new ResourceLocation(NostrumFairies.MODID, "textures/entity/shadow_fey.png");
	
	protected ModelElf modelLeft;
	protected ModelElf modelRight;
	
	public RenderShadowFey(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelElf(true), .01f);
		this.modelLeft = new ModelElfArcher(true);
		this.modelRight = new ModelElfArcher(false);
	}

	protected ResourceLocation getEntityTexture(EntityShadowFey entity) {
		return TEXT_SHADOW_FEY;
	}
	
	@Override
	public void doRender(EntityShadowFey entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Swap out model based on the elf
		if (entity.isLeftHanded()) {
			this.mainModel = this.modelLeft;
		} else {
			this.mainModel = this.modelRight;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		// Model is 32/16ths of a block. Adjust to height.
		final float scale = entity.height / (32f/16f);
		final boolean morphing = entity.getMorphing();
		final float scaleModX = morphing ? NostrumFairies.random.nextFloat() * .1f : 0;
		final float scaleModY = morphing ? NostrumFairies.random.nextFloat() * .05f : 0;
		final float scaleModZ = morphing ? NostrumFairies.random.nextFloat() * .1f : 0;
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.color(1f, 1f, 1f, .7f);
		GlStateManager.scale(scale + scaleModX, scale + scaleModY, scale + scaleModZ);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		GlStateManager.popMatrix();
	}
	
}
