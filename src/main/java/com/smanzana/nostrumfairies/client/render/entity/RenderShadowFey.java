package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrummagica.client.model.ModelRenderShiv;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderShadowFey extends MobRenderer<EntityShadowFey, ModelRenderShiv<EntityShadowFey>> {
	
	private static ResourceLocation TEXT_SHADOW_FEY = new ResourceLocation(NostrumFairies.MODID, "textures/entity/shadow_fey.png");
	
	protected ModelElfArcher<EntityShadowFey> modelLeft;
	protected ModelElfArcher<EntityShadowFey> modelRight;
	
	public RenderShadowFey(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelRenderShiv<>(RenderType::getEntityTranslucent), .01f);
		this.modelLeft = new ModelElfArcher<>(true, RenderType::getEntityTranslucent);
		this.modelRight = new ModelElfArcher<>(false, RenderType::getEntityTranslucent);
	}

	public ResourceLocation getEntityTexture(EntityShadowFey entity) {
		return TEXT_SHADOW_FEY;
	}
	
	@Override
	protected void preRenderCallback(EntityShadowFey entityIn, MatrixStack matrixStackIn, float partialTickTime) {
		// Model is 32/16ths of a block. Adjust to height.
		final float scale = entityIn.getHeight() / (32f/16f);
		final boolean morphing = entityIn.getMorphing();
		final float scaleModX = morphing ? NostrumFairies.random.nextFloat() * .1f : 0;
		final float scaleModY = morphing ? NostrumFairies.random.nextFloat() * .05f : 0;
		final float scaleModZ = morphing ? NostrumFairies.random.nextFloat() * .1f : 0;
		matrixStackIn.scale(scale + scaleModX, scale + scaleModY, scale + scaleModZ);
	}
	
	@Override
	public void render(EntityShadowFey entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// Swap out model based on the elf
		final ModelElfArcher<EntityShadowFey> modelToUse;
		if (entityIn.isLeftHanded()) {
			modelToUse = this.modelLeft;
		} else {
			modelToUse = this.modelRight;
		}
		
		this.entityModel.setPayload((_matrixStackIn, _buffer, _packedLightIn, _packedOverlayIn, _red, _green, _blue, _alpha) -> {
			modelToUse.render(_matrixStackIn, _buffer, _packedLightIn, _packedOverlayIn, _red, _green, _blue, _alpha * .7f);
		});
		
		// Have to dupe this here to work on our real model
		{
			boolean shouldSit = entityIn.isPassenger() && (entityIn.getRidingEntity() != null && entityIn.getRidingEntity().shouldRiderSit());
			float limbSwingAmount = 0.0F;
			float limbSwing = 0.0F;
			if (!shouldSit && entityIn.isAlive()) {
				limbSwingAmount = MathHelper.lerp(partialTicks, entityIn.prevLimbSwingAmount, entityIn.limbSwingAmount);
				limbSwing = entityIn.limbSwing - entityIn.limbSwingAmount * (1.0F - partialTicks);
				if (entityIn.isChild()) {
					limbSwing *= 3.0F;
				}

				if (limbSwingAmount > 1.0F) {
					limbSwingAmount = 1.0F;
				}
			}
			
			modelToUse.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTicks);
			modelToUse.setRotationAngles(entityIn, limbSwing, limbSwingAmount, this.handleRotationFloat(entityIn, partialTicks), entityIn.rotationYawHead, entityIn.rotationPitch);
		}
		
		modelToUse.setWeaponSelection(entityIn);
		
		matrixStackIn.push();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.pop();
	}
	
}
