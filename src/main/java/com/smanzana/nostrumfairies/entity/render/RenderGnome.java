package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.render.ModelGnomeHat.Type;
import com.smanzana.nostrumfairies.entity.render.layers.LayerGnomeHat;
import com.smanzana.nostrumfairies.serializers.ArmPoseGnome;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderGnome extends RenderLiving<EntityGnome> {
	
	private static ResourceLocation TEXT_GNOME_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_1.png");
	private static ResourceLocation TEXT_GNOME_2 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_2.png");
	
	public RenderGnome(RenderManager renderManagerIn, float shadowSizeIn) {
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
		GlStateManager.translate(x, y, z);
		
		// Model is 23/16ths of a block. Want to be .95 (dwarf height).
		float scale = entity.height / (23f/16f);
		GlStateManager.scale(scale, scale, scale);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		
		
		if (entity.getPose() == ArmPoseGnome.CARRYING) {
			ItemStack held = entity.getCarriedItem();
			if (!held.isEmpty()) {
				GlStateManager.pushMatrix();
				GlStateManager.rotate(-entityYaw, 0, 1, 0);
				GlStateManager.translate(0, 1.1, 0.475);
				Minecraft.getInstance().getRenderItem().renderItem(held, TransformType.GROUND);
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
	}
	
}
