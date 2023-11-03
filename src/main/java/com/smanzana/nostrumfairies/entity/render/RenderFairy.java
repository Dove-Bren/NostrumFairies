package com.smanzana.nostrumfairies.entity.render;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderFairy extends RenderLiving<EntityFairy> {
	
	private static ResourceLocation LOC_BODY = new ResourceLocation(NostrumFairies.MODID,
			"textures/entity/fairy_body.png"
			);
//	private static ResourceLocation LOC_WINGS = new ResourceLocation(NostrumFairies.MODID,
//			"textures/entity/fairy_wing.png"
//			);
	
	public RenderFairy(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelFairy(), .05f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFairy entity) {
		return LOC_BODY;
	}
	
	@Override
	public void doRender(EntityFairy entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + .1, z);
		GlStateManager.pushMatrix();
		GlStateManager.scale(.05, .05, .05);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		GlStateManager.popMatrix();
		
		// Now item time!
		@Nonnull ItemStack item = (entity.getCarriedItems().get(0));
		if (!item.isEmpty()) {
			GlStateManager.translate(0, .25, 0);
			GlStateManager.rotate(-entityYaw, 0, 1, 0);
			Minecraft.getInstance().getRenderItem().renderItem(item, TransformType.GROUND);
		}
		
		GlStateManager.popMatrix();
	}
	
}
