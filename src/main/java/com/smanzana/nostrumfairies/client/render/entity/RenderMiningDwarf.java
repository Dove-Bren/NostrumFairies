package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.model.FairiesModelLayers;
import com.smanzana.nostrumfairies.client.render.entity.layer.LayerDwarfBeard;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderMiningDwarf extends MobRenderer<EntityDwarf, ModelMiningDwarf> {
	
	private static ResourceLocation TEXT_DWARF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_miner_1.png");
	
	protected ModelMiningDwarf modelLeft;
	protected ModelMiningDwarf modelRight;
	
	protected RenderMiningDwarf(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn, ModelMiningDwarf left, ModelMiningDwarf right) {
		super(renderManagerIn, left, shadowSizeIn);
		this.modelLeft = left;
		this.modelRight = right;
		this.addLayer(new LayerDwarfBeard<>(this, renderManagerIn.getModelSet()));
	}
	
	public RenderMiningDwarf(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, .25f,
				new ModelMiningDwarf(renderManagerIn.bakeLayer(FairiesModelLayers.MiningDwarfLeft)),
				new ModelMiningDwarf(renderManagerIn.bakeLayer(FairiesModelLayers.MiningDwarf))
				);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityDwarf entity) {
		// TODO different textures?
		return TEXT_DWARF_1;
	}
	
	@Override
	public void render(EntityDwarf entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
//		{
//			this.modelLeft = new ModelDwarf<>(true);
//			this.modelRight = new ModelDwarf<>(false);
//		}
		
		// Swap out model based on the dwarf
		if (entityIn.isLeftHanded()) {
			this.model = this.modelLeft;
		} else {
			this.model = this.modelRight;
		}
		
		matrixStackIn.pushPose();
		
		// Model is 30/16ths of a block. Want to be .95 (dwarf height).
		float scale = entityIn.getBbHeight() / (30f/16f);
		matrixStackIn.scale(scale, scale, scale);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		matrixStackIn.popPose();
	}
	
}
