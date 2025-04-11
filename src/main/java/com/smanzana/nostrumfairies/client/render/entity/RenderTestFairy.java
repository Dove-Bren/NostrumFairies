package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderTestFairy extends MobRenderer<EntityTestFairy, ModelTestFairy> {

	public RenderTestFairy(EntityRenderDispatcher renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelTestFairy(), shadowSizeIn);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityTestFairy entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/dragon_egg_generic.png"
				);
		// TODO remove me!
	}
	
	@Override
	public void render(EntityTestFairy entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		//this.entityModel = new ModelTestFairy();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
}
