package com.smanzana.nostrumfairies.client.render.entity;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RenderFairy extends MobRenderer<EntityFairy, ModelFairy> {
	
	private static ResourceLocation LOC_BODY = new ResourceLocation(NostrumFairies.MODID,
			"textures/entity/fairy_body.png"
			);
//	private static ResourceLocation LOC_WINGS = new ResourceLocation(NostrumFairies.MODID,
//			"textures/entity/fairy_wing.png"
//			);
	
	public RenderFairy(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelFairy(), .05f);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityFairy entity) {
		return LOC_BODY;
	}
	
	@Override
	public void render(EntityFairy entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(0, .1, 0);
		matrixStackIn.pushPose();
		matrixStackIn.scale(.05f, .05f, .05f);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		matrixStackIn.popPose();
		
		// Now item time!
		@Nonnull ItemStack item = (entityIn.getCarriedItems().get(0));
		if (!item.isEmpty()) {
			matrixStackIn.translate(0, .25f, 0);
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-entityYaw));
			RenderFuncs.RenderWorldItem(item, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY);
		}
		
		matrixStackIn.popPose();
	}
	
}
