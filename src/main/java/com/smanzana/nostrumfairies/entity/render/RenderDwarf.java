package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.render.ModelDwarfBeard.Type;
import com.smanzana.nostrumfairies.entity.render.layers.LayerDwarfBeard;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDwarf extends RenderLiving<EntityDwarf> {
	
	private static ResourceLocation TEXT_DWARF_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_1.png");
	
	protected ModelDwarf modelLeft;
	protected ModelDwarf modelRight;
	
	public RenderDwarf(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelDwarf(true), .25f);
		this.modelLeft = new ModelDwarf(true);
		this.modelRight = new ModelDwarf(false);
		this.addLayer(new LayerDwarfBeard(this, Type.FULL));
		this.addLayer(new LayerDwarfBeard(this, Type.LONG));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDwarf entity) {
		// TODO different textures?
		return TEXT_DWARF_1;
	}
	
	@Override
	public void doRender(EntityDwarf entity, double x, double y, double z, float entityYaw, float partialTicks) {
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
		x = 0;
		y = 0;
		z = 0;
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		
		GlStateManager.popMatrix();
	}
	
}
