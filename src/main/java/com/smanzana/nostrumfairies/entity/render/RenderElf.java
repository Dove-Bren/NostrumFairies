package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderElf extends RenderLiving<EntityElf> {
	
	private static ResourceLocation TEXT_ELF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/elf_1.png");
	
	protected ModelElf modelLeft;
	protected ModelElf modelRight;
	
	public RenderElf(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelElf(true), .25f);
		this.modelLeft = new ModelElf(true);
		this.modelRight = new ModelElf(false);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityElf entity) {
		// TODO different textures?
		return TEXT_ELF_1;
	}
	
	@Override
	public void doRender(EntityElf entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Swap out model based on the dwarf
		if (entity.isLeftHanded()) {
			this.mainModel = this.modelLeft;
		} else {
			this.mainModel = this.modelRight;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		// Model is 32/16ths of a block. Adjust to height.
		float scale = entity.height / (32f/16f);
		GlStateManager.scale(scale, scale, scale);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		GlStateManager.popMatrix();
	}
	
}
