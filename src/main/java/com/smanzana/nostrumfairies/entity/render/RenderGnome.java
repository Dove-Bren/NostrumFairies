package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.render.ModelGnomeHat.Type;
import com.smanzana.nostrumfairies.entity.render.layers.LayerGnomeHat;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGnome extends RenderLiving<EntityGnome> {
	
	private static ResourceLocation TEXT_GNOME_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_1.png");
	
	public RenderGnome(RenderManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelGnome(), .25f);
		this.addLayer(new LayerGnomeHat(this, Type.ERECT));
		this.addLayer(new LayerGnomeHat(this, Type.PLAIN));
		this.addLayer(new LayerGnomeHat(this, Type.LIMP));
		this.addLayer(new LayerGnomeHat(this, Type.SMALL));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGnome entity) {
		// TODO different textures?
		return TEXT_GNOME_1;
	}
	
	@Override
	public void doRender(EntityGnome entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		
		// Model is 23/16ths of a block. Want to be .95 (dwarf height).
		float scale = entity.height / (23f/16f);
		GlStateManager.scale(scale, scale, scale);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		GlStateManager.popMatrix();
	}
	
}
