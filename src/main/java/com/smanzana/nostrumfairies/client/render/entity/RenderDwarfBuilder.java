package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.model.FairiesModelLayers;
import com.smanzana.nostrumfairies.client.render.entity.layer.LayerDwarfBeard;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfBuilder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderDwarfBuilder extends MobRenderer<EntityDwarfBuilder, ModelBuildingDwarf> {
	
	private static ResourceLocation TEXT_DWARF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_builder_1.png");
	
	protected ModelBuildingDwarf modelLeft;
	protected ModelBuildingDwarf modelRight;
	
	protected RenderDwarfBuilder(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn, ModelBuildingDwarf left, ModelBuildingDwarf right) {
		super(renderManagerIn, left, shadowSizeIn);
		this.modelLeft = left;
		this.modelRight = right;
		this.addLayer(new LayerDwarfBeard<>(this, renderManagerIn.getModelSet()));
	}
	
	public RenderDwarfBuilder(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, .25f,
				new ModelBuildingDwarf(renderManagerIn.bakeLayer(FairiesModelLayers.BuildingDwarfLeft)),
				new ModelBuildingDwarf(renderManagerIn.bakeLayer(FairiesModelLayers.BuildingDwarf))
				);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityDwarfBuilder entity) {
		// TODO different textures?
		return TEXT_DWARF_1;
	}
	
	@Override
	public void render(EntityDwarfBuilder entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
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
