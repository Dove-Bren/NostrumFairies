package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderElfArcher extends MobRenderer<EntityElfArcher, ModelElfArcher<EntityElfArcher>> {
	
	private static ResourceLocation TEXT_ELF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/elf_archer_1.png");
	
	protected ModelElfArcher<EntityElfArcher> modelLeft;
	protected ModelElfArcher<EntityElfArcher> modelRight;
	
	public RenderElfArcher(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelElfArcher<>(true), .25f);
		this.modelLeft = new ModelElfArcher<>(true);
		this.modelRight = new ModelElfArcher<>(false);
	}

	public ResourceLocation getEntityTexture(EntityElfArcher entity) {
		//return (entity.getPersistentID().getLeastSignificantBits() & 1) == 0 ? TEXT_ELF_1 : TEXT_ELF_2;
		return TEXT_ELF_1;
	}
	
	@Override
	public void render(EntityElfArcher entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// Swap out model based on the elf
		if (entityIn.isLeftHanded()) {
			this.entityModel = this.modelLeft;
		} else {
			this.entityModel = this.modelRight;
		}
		
		matrixStackIn.push();
		
		// Model is 32/16ths of a block. Adjust to height.
		float scale = entityIn.getHeight() / (32f/16f);
		matrixStackIn.scale(scale, scale, scale);
		this.entityModel.setWeaponSelection(entityIn);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		matrixStackIn.pop();
	}
	
}
