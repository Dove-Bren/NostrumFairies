package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.entity.ModelDwarfBeard.Type;
import com.smanzana.nostrumfairies.client.render.entity.layer.LayerDwarfBeard;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderDwarfBuilder extends MobRenderer<EntityDwarfBuilder, ModelBuildingDwarf> {
	
	private static ResourceLocation TEXT_DWARF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_builder_1.png");
	
	protected ModelBuildingDwarf modelLeft;
	protected ModelBuildingDwarf modelRight;
	
	public RenderDwarfBuilder(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelBuildingDwarf(true), .25f);
		this.modelLeft = new ModelBuildingDwarf(true);
		this.modelRight = new ModelBuildingDwarf(false);
		this.addLayer(new LayerDwarfBeard<>(this, Type.FULL));
		this.addLayer(new LayerDwarfBeard<>(this, Type.LONG));
	}

	@Override
	public ResourceLocation getEntityTexture(EntityDwarfBuilder entity) {
		// TODO different textures?
		return TEXT_DWARF_1;
	}
	
	@Override
	public void render(EntityDwarfBuilder entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		// Swap out model based on the dwarf
		if (entityIn.isLeftHanded()) {
			this.entityModel = this.modelLeft;
		} else {
			this.entityModel = this.modelRight;
		}
		
		matrixStackIn.push();
		
		// Model is 30/16ths of a block. Want to be .95 (dwarf height).
		float scale = entityIn.getHeight() / (30f/16f);
		matrixStackIn.scale(scale, scale, scale);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		matrixStackIn.pop();
	}
	
}
