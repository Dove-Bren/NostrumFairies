package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelGnomeHat extends EntityModel<EntityGnome> {
	
	public static enum Type {
		ERECT,
		PLAIN,
		LIMP,
		SMALL
	}
	
	protected final Type type;

	private ModelRenderer base;
	
	public ModelGnomeHat(Type type) {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		int textW = 128;
		int textH = 128;
		// TODO some y offset
		
		this.type = type;
		switch (type) {
		case ERECT:
			base = new ModelRenderer(this, 0, 0);
			base.setTextureSize(textW, textH);
			base.addBox(-3.5f, -8, -3.5f, 7, 2, 7);
			base.setTextureOffset(28, 0);
			base.addBox(-3, -9, -3, 6, 1, 6);
			base.setTextureOffset(28, 7);
			base.addBox(-2, -10, -2, 4, 1, 4);
			base.setTextureOffset(44, 7);
			base.addBox(-1, -12, -1, 2, 2, 2);
			base.setTextureOffset(44, 11);
			base.addBox(-.5f, -15, -.5f, 1, 3, 1);
			break;
		case PLAIN:
			base = new ModelRenderer(this, 0, 18);
			base.setTextureSize(textW, textH);
			base.addBox(-3.5f, -8, -3.5f, 7, 2, 7);
			base.setTextureOffset(28, 18);
			base.addBox(-3, -9, -3, 6, 1, 6);
			base.setTextureOffset(28, 25);
			base.addBox(-2, -10, -2, 4, 1, 4);
			base.setTextureOffset(44, 7);
			base.addBox(-1, -12, -1, 2, 2, 2);
			base.setTextureOffset(44, 29);
			base.addBox(-.5f, -13, -.5f, 2, 1, 2);
			break;
		case LIMP:
			base = new ModelRenderer(this, 0, 36);
			base.setTextureSize(textW, textH);
			base.addBox(-3.5f, -8, -3.5f, 7, 2, 7);
			base.setTextureOffset(28, 36);
			base.addBox(-4, -9, -3, 7, 1, 6);
			
			base.setTextureOffset(28, 43);
			base.addBox(-6, -10, -2, 6, 1, 6);
			base.setTextureOffset(12, 45);
			base.addBox(-5, -11, -1, 4, 1, 4);
			
			base.setTextureOffset(0, 45);
			base.addBox(-7, -9, 2, 2, 2, 3);
			
			base.setTextureOffset(52, 48);
			base.addBox(-7, -7, 3, 1, 1, 1);
			break;
		case SMALL:
			base = new ModelRenderer(this, 0, 52);
			base.setTextureSize(textW, textH);
			base.addBox(-1, -8, -1, 4, 1, 4);
			
			base.setTextureOffset(16, 52);
			base.addBox(-.5f, -9, -.5f, 3, 1, 3);
			base.setTextureOffset(28, 52);
			base.addBox(0, -11, 0, 2, 2, 2);
			base.setTextureOffset(36, 52);
			base.addBox(.5f, -13, .5f, 1, 2, 1);
			break;
		}
	}
	
	public void setRotationAngles(EntityGnome entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		base.rotateAngleX = headPitch * 0.017453292F;
		base.rotateAngleY = netHeadYaw * 0.017453292F;
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		base.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
}
