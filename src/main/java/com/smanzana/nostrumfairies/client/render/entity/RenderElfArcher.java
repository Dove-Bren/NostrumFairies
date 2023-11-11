package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;

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

	protected ResourceLocation getEntityTexture(EntityElfArcher entity) {
		//return (entity.getPersistentID().getLeastSignificantBits() & 1) == 0 ? TEXT_ELF_1 : TEXT_ELF_2;
		return TEXT_ELF_1;
	}
	
	@Override
	public void doRender(EntityElfArcher entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Swap out model based on the elf
		if (entity.isLeftHanded()) {
			this.entityModel = this.modelLeft;
		} else {
			this.entityModel = this.modelRight;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		
		// Model is 32/16ths of a block. Adjust to height.
		float scale = entity.getHeight() / (32f/16f);
		GlStateManager.scalef(scale, scale, scale);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		GlStateManager.popMatrix();
	}
	
}
