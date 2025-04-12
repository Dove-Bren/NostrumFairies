package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.model.FairiesModelLayers;
import com.smanzana.nostrumfairies.entity.fey.EntityElfCrafter;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderElfCrafter extends MobRenderer<EntityElfCrafter, ModelElf<EntityElfCrafter>> {
	
	private static ResourceLocation TEXT_ELF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/elf_crafter_1.png");
	
	protected ModelElf<EntityElfCrafter> modelLeft;
	protected ModelElf<EntityElfCrafter> modelRight;
	
	protected RenderElfCrafter(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn, ModelElf<EntityElfCrafter> left, ModelElf<EntityElfCrafter> right) {
		super(renderManagerIn, left, shadowSizeIn);
		this.modelLeft = left;
		this.modelRight = right;
	}
	
	public RenderElfCrafter(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, shadowSizeIn,
				new ModelElfMage<>(renderManagerIn.bakeLayer(FairiesModelLayers.MageElfLeft)),
				new ModelElfMage<>(renderManagerIn.bakeLayer(FairiesModelLayers.MageElf)));
	}

	public ResourceLocation getTextureLocation(EntityElfCrafter entity) {
		//return (entity.getPersistentID().getLeastSignificantBits() & 1) == 0 ? TEXT_ELF_1 : TEXT_ELF_2;
		return TEXT_ELF_1;
	}
	
	@Override
	public void render(EntityElfCrafter entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		// Swap out model based on the elf
		if (entityIn.isLeftHanded()) {
			this.model = this.modelLeft;
		} else {
			this.model = this.modelRight;
		}
		
		matrixStackIn.pushPose();
		
		// Model is 32/16ths of a block. Adjust to height.
		float scale = entityIn.getBbHeight() / (32f/16f);
		matrixStackIn.scale(scale, scale, scale);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		matrixStackIn.popPose();
	}
	
}
