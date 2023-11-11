package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityElfCrafter;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderElfCrafter extends MobRenderer<EntityElfCrafter, ModelElf<EntityElfCrafter>> {
	
	private static ResourceLocation TEXT_ELF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/elf_crafter_1.png");
	
	protected ModelElf<EntityElfCrafter> modelLeft;
	protected ModelElf<EntityElfCrafter> modelRight;
	
	public RenderElfCrafter(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelElf<>(true), .25f);
		this.modelLeft = new ModelElf<>(true);
		this.modelRight = new ModelElf<>(false);
	}

	protected ResourceLocation getEntityTexture(EntityElfCrafter entity) {
		//return (entity.getPersistentID().getLeastSignificantBits() & 1) == 0 ? TEXT_ELF_1 : TEXT_ELF_2;
		return TEXT_ELF_1;
	}
	
	@Override
	public void doRender(EntityElfCrafter entity, double x, double y, double z, float entityYaw, float partialTicks) {
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
