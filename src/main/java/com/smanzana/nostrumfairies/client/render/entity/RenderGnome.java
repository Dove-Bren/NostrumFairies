package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.entity.ModelGnomeHat.Type;
import com.smanzana.nostrumfairies.client.render.entity.layer.LayerGnomeHat;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.serializers.ArmPoseGnome;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderGnome extends MobRenderer<EntityGnome, ModelGnome> {
	
	private static ResourceLocation TEXT_GNOME_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_1.png");
	private static ResourceLocation TEXT_GNOME_2 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_2.png");
	
	public RenderGnome(EntityRendererManager renderManagerIn, float shadowSizeIn) {
		super(renderManagerIn, new ModelGnome(), .25f);
		this.addLayer(new LayerGnomeHat(this, Type.ERECT));
		this.addLayer(new LayerGnomeHat(this, Type.PLAIN));
		this.addLayer(new LayerGnomeHat(this, Type.LIMP));
		this.addLayer(new LayerGnomeHat(this, Type.SMALL));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGnome entity) {
		// TODO base off of something else
		if ((entity.getUniqueID().getLeastSignificantBits() & 1) == 0) {
			return TEXT_GNOME_1;
		} else {
			return TEXT_GNOME_2;
		}
	}
	
	@Override
	public void doRender(EntityGnome entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		
		// Model is 23/16ths of a block. Want to be .95 (dwarf height).
		float scale = entity.getHeight() / (23f/16f);
		GlStateManager.scalef(scale, scale, scale);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		
		if (entity.getPose() == ArmPoseGnome.CARRYING) {
			ItemStack held = entity.getCarriedItem();
			if (!held.isEmpty()) {
				GlStateManager.pushMatrix();
				GlStateManager.rotated(-entityYaw, 0, 1, 0);
				GlStateManager.translated(0, 1.1, 0.475);
				RenderFuncs.ItemRenderer(held);
				//Minecraft.getInstance().getRenderItem().renderItem(held, TransformType.GROUND);
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
	}
	
}
