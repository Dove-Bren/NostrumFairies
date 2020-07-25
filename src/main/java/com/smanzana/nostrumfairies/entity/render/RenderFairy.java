package com.smanzana.nostrumfairies.entity.render;

import javax.annotation.Nullable;

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
		super(renderManagerIn, new ModelFairy(), .25f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFairy entity) {
		return LOC_BODY;
	}
	
	@Override
	public void doRender(EntityFairy entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.scale(.25, .25, .25);
		x = 0;
		y = 0;
		z = 0;
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		// Now item time!
		@Nullable ItemStack item = (entity.getCarriedItems()[0]);
		if (item != null) {
			GlStateManager.translate(0, 1.25, 0);
			GlStateManager.rotate(-entityYaw, 0, 1, 0);
			Minecraft.getMinecraft().getRenderItem().renderItem(item, TransformType.GROUND);
		}
		
		GlStateManager.popMatrix();
	}
	
}
