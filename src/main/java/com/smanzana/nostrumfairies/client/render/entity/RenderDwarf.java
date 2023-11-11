package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.entity.ModelDwarfBeard.Type;
import com.smanzana.nostrumfairies.client.render.entity.layer.LayerDwarfBeard;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderDwarf<T extends EntityDwarf> extends MobRenderer<T, ModelDwarf<T>> {
	
	private static ResourceLocation TEXT_DWARF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_miner_1.png");
	
	protected ModelDwarf<T> modelLeft;
	protected ModelDwarf<T> modelRight;
	
	public RenderDwarf(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelDwarf<>(true), .25f);
		this.modelLeft = new ModelDwarf<>(true);
		this.modelRight = new ModelDwarf<>(false);
		this.addLayer(new LayerDwarfBeard<>(this, Type.FULL));
		this.addLayer(new LayerDwarfBeard<>(this, Type.LONG));
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {
		// TODO different textures?
		return TEXT_DWARF_1;
	}
	
	@Override
	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Swap out model based on the dwarf
		if (entity.isLeftHanded()) {
			this.entityModel = this.modelLeft;
		} else {
			this.entityModel = this.modelRight;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		
		// Model is 30/16ths of a block. Want to be .95 (dwarf height).
		float scale = entity.getHeight() / (30f/16f);
		GlStateManager.scalef(scale, scale, scale);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		GlStateManager.popMatrix();
	}
	
}
