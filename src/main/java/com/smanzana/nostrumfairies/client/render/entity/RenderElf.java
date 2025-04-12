package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderElf<T extends EntityElf> extends MobRenderer<T, ModelElf<T>> {
	
	private static ResourceLocation TEXT_ELF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/elf_forester_1.png");
	
	protected ModelElf<T> modelLeft;
	protected ModelElf<T> modelRight;
	
	public RenderElf(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelElf<>(true), .25f);
		this.modelLeft = new ModelElf<>(true);
		this.modelRight = new ModelElf<>(false);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		//return (entity.getPersistentID().getLeastSignificantBits() & 1) == 0 ? TEXT_ELF_1 : TEXT_ELF_2;
		return TEXT_ELF_1;
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		// Swap out model based on the dwarf
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
