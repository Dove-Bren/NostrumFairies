package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.model.FairiesModelLayers;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrummagica.client.model.RenderShivModel;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class RenderShadowFey extends MobRenderer<EntityShadowFey, RenderShivModel<EntityShadowFey>> {
	
	private static ResourceLocation TEXT_SHADOW_FEY = new ResourceLocation(NostrumFairies.MODID, "textures/entity/shadow_fey.png");
	
	protected ModelElfArcher<EntityShadowFey> modelLeft;
	protected ModelElfArcher<EntityShadowFey> modelRight;
	
	protected RenderShadowFey(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn, ModelElfArcher<EntityShadowFey> left, ModelElfArcher<EntityShadowFey> right) {
		super(renderManagerIn, new RenderShivModel<>(RenderType::entityTranslucent), shadowSizeIn);
		this.modelLeft = left;
		this.modelRight = right;
	}
	
	public RenderShadowFey(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, shadowSizeIn,
				new ModelElfArcher<>(renderManagerIn.bakeLayer(FairiesModelLayers.ArcherElfLeft), RenderType::entityTranslucent),
				new ModelElfArcher<>(renderManagerIn.bakeLayer(FairiesModelLayers.ArcherElf), RenderType::entityTranslucent));
	}

	public ResourceLocation getTextureLocation(EntityShadowFey entity) {
		return TEXT_SHADOW_FEY;
	}
	
	@Override
	protected void scale(EntityShadowFey entityIn, PoseStack matrixStackIn, float partialTickTime) {
		// Model is 32/16ths of a block. Adjust to height.
		final float scale = entityIn.getBbHeight() / (32f/16f);
		final boolean morphing = entityIn.getMorphing();
		final float scaleModX = morphing ? NostrumFairies.random.nextFloat() * .1f : 0;
		final float scaleModY = morphing ? NostrumFairies.random.nextFloat() * .05f : 0;
		final float scaleModZ = morphing ? NostrumFairies.random.nextFloat() * .1f : 0;
		matrixStackIn.scale(scale + scaleModX, scale + scaleModY, scale + scaleModZ);
	}
	
	@Override
	public void render(EntityShadowFey entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		// Swap out model based on the elf
		final ModelElfArcher<EntityShadowFey> modelToUse;
		if (entityIn.isLeftHanded()) {
			modelToUse = this.modelLeft;
		} else {
			modelToUse = this.modelRight;
		}
		
		this.model.setPayload((_matrixStackIn, _buffer, _packedLightIn, _packedOverlayIn, _red, _green, _blue, _alpha) -> {
			modelToUse.renderToBuffer(_matrixStackIn, _buffer, _packedLightIn, _packedOverlayIn, _red, _green, _blue, _alpha * .7f);
		});
		
		// Have to dupe this here to work on our real model
		{
			boolean shouldSit = entityIn.isPassenger() && (entityIn.getVehicle() != null && entityIn.getVehicle().shouldRiderSit());
			float limbSwingAmount = 0.0F;
			float limbSwing = 0.0F;
			if (!shouldSit && entityIn.isAlive()) {
				limbSwingAmount = Mth.lerp(partialTicks, entityIn.animationSpeedOld, entityIn.animationSpeed);
				limbSwing = entityIn.animationPosition - entityIn.animationSpeed * (1.0F - partialTicks);
				if (entityIn.isBaby()) {
					limbSwing *= 3.0F;
				}

				if (limbSwingAmount > 1.0F) {
					limbSwingAmount = 1.0F;
				}
			}
			
			modelToUse.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTicks);
			modelToUse.setupAnim(entityIn, limbSwing, limbSwingAmount, this.getBob(entityIn, partialTicks), entityIn.yHeadRot, entityIn.getXRot());
		}
		
		modelToUse.setWeaponSelection(entityIn);
		
		matrixStackIn.pushPose();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.popPose();
	}
	
}
