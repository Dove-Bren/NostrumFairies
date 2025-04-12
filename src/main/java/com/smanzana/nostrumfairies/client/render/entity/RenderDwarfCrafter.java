package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.model.FairiesModelLayers;
import com.smanzana.nostrumfairies.client.render.entity.layer.LayerDwarfBeard;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfCrafter;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderDwarfCrafter extends MobRenderer<EntityDwarfCrafter, ModelCraftingDwarf> {
	
	private static ResourceLocation TEXT_DWARF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_crafter_1.png");
	
	protected ModelCraftingDwarf modelLeft;
	protected ModelCraftingDwarf modelRight;
	
	protected RenderDwarfCrafter(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn, ModelCraftingDwarf left, ModelCraftingDwarf right) {
		super(renderManagerIn, left, shadowSizeIn);
		this.modelLeft = left;
		this.modelRight = right;
		this.addLayer(new LayerDwarfBeard<>(this, renderManagerIn.getModelSet()));
	}
	
	public RenderDwarfCrafter(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		this(renderManagerIn, .25f,
				new ModelCraftingDwarf(renderManagerIn.bakeLayer(FairiesModelLayers.CraftingDwarfLeft)),
				new ModelCraftingDwarf(renderManagerIn.bakeLayer(FairiesModelLayers.CraftingDwarf))
				);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityDwarfCrafter entity) {
		// TODO different textures?
		return TEXT_DWARF_1;
	}
	
	@Override
	public void render(EntityDwarfCrafter entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
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
