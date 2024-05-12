package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderTestFairy extends MobRenderer<EntityTestFairy, ModelTestFairy> {

	public RenderTestFairy(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelTestFairy(), shadowSizeIn);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityTestFairy entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/dragon_egg_generic.png"
				);
		// TODO remove me!
	}
	
	@Override
	public void render(EntityTestFairy entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		//this.entityModel = new ModelTestFairy();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
}
