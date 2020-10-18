package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfBuilder;
import com.smanzana.nostrumfairies.entity.render.ModelDwarfBeard.Type;
import com.smanzana.nostrumfairies.entity.render.layers.LayerDwarfBeard;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDwarfBuilder extends RenderLiving<EntityDwarfBuilder> {
	
	private static ResourceLocation TEXT_DWARF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_builder_1.png");
	
	protected ModelDwarf modelLeft;
	protected ModelDwarf modelRight;
	
	public RenderDwarfBuilder(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelDwarf(true), .25f);
		this.modelLeft = new ModelBuildingDwarf(true);
		this.modelRight = new ModelBuildingDwarf(false);
		this.addLayer(new LayerDwarfBeard(this, Type.FULL));
		this.addLayer(new LayerDwarfBeard(this, Type.LONG));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDwarfBuilder entity) {
		// TODO different textures?
		return TEXT_DWARF_1;
	}
	
	@Override
	public void doRender(EntityDwarfBuilder entity, double x, double y, double z, float entityYaw, float partialTicks) {
		// Swap out model based on the dwarf
		if (entity.isLeftHanded()) {
			this.mainModel = this.modelLeft;
		} else {
			this.mainModel = this.modelRight;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		// Model is 30/16ths of a block. Want to be .95 (dwarf height).
		float scale = entity.height / (30f/16f);
		GlStateManager.scale(scale, scale, scale);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		GlStateManager.popMatrix();
	}
	
}
