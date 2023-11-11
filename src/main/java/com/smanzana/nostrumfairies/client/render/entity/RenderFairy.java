package com.smanzana.nostrumfairies.client.render.entity;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderFairy extends MobRenderer<EntityFairy, ModelFairy> {
	
	private static ResourceLocation LOC_BODY = new ResourceLocation(NostrumFairies.MODID,
			"textures/entity/fairy_body.png"
			);
//	private static ResourceLocation LOC_WINGS = new ResourceLocation(NostrumFairies.MODID,
//			"textures/entity/fairy_wing.png"
//			);
	
	public RenderFairy(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelFairy(), .05f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFairy entity) {
		return LOC_BODY;
	}
	
	@Override
	public void doRender(EntityFairy entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y + .1, z);
		GlStateManager.pushMatrix();
		GlStateManager.scalef(.05f, .05f, .05f);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		GlStateManager.popMatrix();
		
		// Now item time!
		@Nonnull ItemStack item = (entity.getCarriedItems().get(0));
		if (!item.isEmpty()) {
			GlStateManager.translatef(0, .25f, 0);
			GlStateManager.rotatef(-entityYaw, 0, 1, 0);
			RenderFuncs.ItemRenderer(item);
		}
		
		GlStateManager.popMatrix();
	}
	
}
