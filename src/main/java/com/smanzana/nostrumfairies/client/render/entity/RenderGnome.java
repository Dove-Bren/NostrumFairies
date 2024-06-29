package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.entity.ModelGnomeHat.Type;
import com.smanzana.nostrumfairies.client.render.entity.layer.LayerGnomeHat;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.serializers.ArmPoseGnome;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

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
	public ResourceLocation getEntityTexture(EntityGnome entity) {
		// TODO base off of something else
		if ((entity.getUniqueID().getLeastSignificantBits() & 1) == 0) {
			return TEXT_GNOME_1;
		} else {
			return TEXT_GNOME_2;
		}
	}
	
	@Override
	public void render(EntityGnome entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
		matrixStackIn.push();
		
		// Model is 23/16ths of a block. Want to be .95 (dwarf height).
		float scale = entityIn.getHeight() / (23f/16f);
		matrixStackIn.scale(scale, scale, scale);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		
		
		if (entityIn.getGnomePose() == ArmPoseGnome.CARRYING) {
			ItemStack held = entityIn.getCarriedItem();
			if (!held.isEmpty()) {
				matrixStackIn.push();
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-entityYaw));
				matrixStackIn.translate(0, 1.1, 0.475);
				RenderFuncs.RenderWorldItem(held, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY);
				matrixStackIn.pop();
			}
		}
		matrixStackIn.pop();
	}
	
}
