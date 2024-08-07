package com.smanzana.nostrumfairies.client.render.entity;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

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
	public ResourceLocation getEntityTexture(EntityFairy entity) {
		return LOC_BODY;
	}
	
	@Override
	public void render(EntityFairy entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		matrixStackIn.push();
		matrixStackIn.translate(0, .1, 0);
		matrixStackIn.push();
		matrixStackIn.scale(.05f, .05f, .05f);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.pop();
		
		// Now item time!
		@Nonnull ItemStack item = (entityIn.getCarriedItems().get(0));
		if (!item.isEmpty()) {
			matrixStackIn.translate(0, .25f, 0);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-entityYaw));
			RenderFuncs.RenderWorldItem(item, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY);
		}
		
		matrixStackIn.pop();
	}
	
}
