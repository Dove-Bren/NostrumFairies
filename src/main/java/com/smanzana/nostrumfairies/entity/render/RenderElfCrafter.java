package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityElfCrafter;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderElfCrafter extends RenderLiving<EntityElfCrafter> {
	
	private static ResourceLocation TEXT_ELF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/elf_crafter_1.png");
	
	protected ModelElf modelLeft;
	protected ModelElf modelRight;
	
	public RenderElfCrafter(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelElf(true), .25f);
		this.modelLeft = new ModelElf(true);
		this.modelRight = new ModelElf(false);
	}

	protected ResourceLocation getEntityTexture(EntityElfCrafter entity) {
		//return (entity.getPersistentID().getLeastSignificantBits() & 1) == 0 ? TEXT_ELF_1 : TEXT_ELF_2;
		return TEXT_ELF_1;
	}
	
	@Override
	public void doRender(EntityElfCrafter entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Swap out model based on the elf
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
