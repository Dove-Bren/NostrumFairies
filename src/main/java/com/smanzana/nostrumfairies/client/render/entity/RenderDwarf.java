package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.entity.ModelDwarfBeard.Type;
import com.smanzana.nostrumfairies.client.render.entity.layer.LayerDwarfBeard;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderDwarf<T extends EntityDwarf> extends MobRenderer<T, ModelDwarf<T>> {
	
	private static ResourceLocation TEXT_DWARF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_miner_1.png");
	
	protected ModelDwarf<T> modelLeft;
	protected ModelDwarf<T> modelRight;
	
	public RenderDwarf(EntityRendererProvider.Context renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelDwarf<>(true), .25f);
		this.modelLeft = new ModelDwarf<>(true);
		this.modelRight = new ModelDwarf<>(false);
		this.addLayer(new LayerDwarfBeard<>(this, Type.FULL));
		this.addLayer(new LayerDwarfBeard<>(this, Type.LONG));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		// TODO different textures?
		return TEXT_DWARF_1;
	}
	
	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
		{
			this.modelLeft = new ModelDwarf<>(true);
			this.modelRight = new ModelDwarf<>(false);
		}
		
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
