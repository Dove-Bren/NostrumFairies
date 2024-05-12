package com.smanzana.nostrumfairies.client.render.entity.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.entity.ModelGnome;
import com.smanzana.nostrumfairies.client.render.entity.ModelGnomeHat;
import com.smanzana.nostrumfairies.client.render.entity.RenderGnome;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class LayerGnomeHat extends LayerRenderer<EntityGnome, ModelGnome> {

	private static final ResourceLocation TEXT_HAT_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_hat_1.png");
	private static final ResourceLocation TEXT_HAT_2 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_hat_2.png");
	private static final ResourceLocation TEXT_HAT_3 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_hat_3.png");
	private static final ResourceLocation TEXT_HAT_4 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/gnome_hat_4.png");
	
	private ModelGnomeHat model;
	private ModelGnomeHat.Type type;
	
	public LayerGnomeHat(RenderGnome renderer, ModelGnomeHat.Type type) {
		super(renderer);
		this.model = new ModelGnomeHat(type);
		this.type = type;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityGnome gnome, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		// TODO maybe make beard style and texture by something that's assigned and persisted to the dwarf?
		if (((gnome.getUniqueID().getLeastSignificantBits() >> 5) & 3) != type.ordinal()) { // bits 6/7
			return;
		}
		
		ResourceLocation text;
		switch ((int) ((gnome.getUniqueID().getLeastSignificantBits() >> 1) & 3)) { // bits 2 and 3 (2 + 4)
		case 0:
		default:
			text = TEXT_HAT_1;
			break;
		case 1:
			text = TEXT_HAT_2;
			break;
		case 2:
			text = TEXT_HAT_3;
			break;
		case 3:
			text = TEXT_HAT_4;
			break;
			
		}
		
		final IVertexBuilder buffer = bufferIn.getBuffer(model.getRenderType(text));
		model.setRotationAngles(gnome, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		
		// translate up to head, then render.
		// Unfortunately, has a copy of rotations from animations to match the head :/
		//float bend = (float) (Math.sin(gnome.getSwingProgress(partialTicks) * Math.PI) * (Math.PI * .1));
		//float offsetY = (float) (Math.sin(gnome.getSwingProgress(partialTicks * Math.PI) * (1f / 16f));
		float bend = (float) (Math.sin(gnome.getSwingProgress(partialTicks) * Math.PI) * (180 * .1));
		float offsetY = (float) (Math.sin(gnome.getSwingProgress(partialTicks) * Math.PI) * (1f / 16f));
		matrixStackIn.push();
		matrixStackIn.scale(1.05f, 1.05f, 1.05f);
		matrixStackIn.translate(0, ((8 + 5) / 16f) + offsetY, 0);
		matrixStackIn.rotate(Vector3f.XP.rotationDegrees(bend));
		matrixStackIn.translate(0, (-5f / 16f), 0);
		model.render(matrixStackIn, buffer, packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
		matrixStackIn.pop();
	}
}
