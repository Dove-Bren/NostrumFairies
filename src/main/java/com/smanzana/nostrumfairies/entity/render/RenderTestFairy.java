package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderTestFairy extends RenderLiving<EntityTestFairy> {

	public RenderTestFairy(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelTestFairy(), shadowSizeIn);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTestFairy entity) {
		return new ResourceLocation(NostrumMagica.MODID,
				"textures/entity/dragon_egg_generic.png"
				);
		// TODO remove me!
	}
	
	@Override
	public void doRender(EntityTestFairy entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.mainModel = new ModelTestFairy();
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}
	
}
