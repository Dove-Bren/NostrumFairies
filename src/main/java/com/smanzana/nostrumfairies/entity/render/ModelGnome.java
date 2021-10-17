package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.serializers.ArmPoseGnome;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelGnome extends ModelBase {

	private ModelRenderer body;
	private ModelRenderer head;
	private ModelRenderer legLeft;
	private ModelRenderer legRight;
	private ModelRenderer armLeft;
	private ModelRenderer armRight;
	
	public ModelGnome() {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		final int textW = 64;
		final int textH = 32;
		
		body = new ModelRenderer(this, 0, 0);
		body.setTextureSize(textW, textH);
		body.setRotationPoint(0, 13, 0);
		body.addBox(-4, -5, -3, 8, 10, 6);
		
		head = new ModelRenderer(this, 28, 0);
		head.setTextureSize(textW, textH);
		head.setRotationPoint(0, 0, 0);
		head.addBox(-3.5f, -7, -3.5f, 7, 7, 7);
		head.setTextureOffset(28, 14);
		head.addBox(3, -5, -1, 1, 2, 1);
		head.setTextureOffset(28, 17);
		head.addBox(-4, -5, -1, 1, 2, 1);
		head.offsetY = (-5f / 16f);
		body.addChild(head);
		
		legLeft = new ModelRenderer(this, 0, 16);
		legLeft.setTextureSize(textW, textH);
		legLeft.setRotationPoint(0, 0, 0);
		legLeft.addBox(-1.5f, 0, -2, 3, 6, 4);
		legLeft.setTextureOffset(14, 27);
		legLeft.addBox(-1.5f, 4, -5, 3, 2, 3);
		legLeft.setTextureOffset(4, 28);
		legLeft.addBox(-1.5f, 3, -6, 3, 2, 2);
		legLeft.offsetY = (5f / 16f);
		legLeft.offsetX = (2.49f / 16f);
		body.addChild(legLeft);

		legRight = new ModelRenderer(this, 0, 16);
		legRight.mirror = true;
		legRight.setTextureSize(textW, textH);
		legRight.setRotationPoint(0, 0, 0);
		legRight.addBox(-1.5f, 0, -2, 3, 6, 4);
		legRight.setTextureOffset(14, 27);
		legRight.addBox(-1.5f, 4, -5, 3, 2, 3);
		legRight.setTextureOffset(4, 28);
		legRight.addBox(-1.5f, 3, -6, 3, 2, 2);
		legRight.offsetY = (5f / 16f);
		legRight.offsetX = (-2.49f / 16f);
		body.addChild(legRight);
		
		armLeft = new ModelRenderer(this, 48, 16);
		armLeft.setTextureSize(textW, textH);
		armLeft.setRotationPoint(0, 0, 0);
		armLeft.addBox(-1.5f, 0, -1.5f, 3, 7, 3);
		armLeft.offsetY = (-5f / 16f);
		armLeft.offsetX = ((3 + 1.5f) / 16f);
		body.addChild(armLeft);
		
		armRight = new ModelRenderer(this, 48, 16);
		armRight.mirror = true;
		armRight.setTextureSize(textW, textH);
		armRight.setRotationPoint(0, 0, 0);
		armRight.addBox(-1.5f, 0, -1.5f, 3, 7, 3);
		armRight.offsetY = (-5f / 16f);
		armRight.offsetX = (-(3 + 1.5f) / 16f);
		body.addChild(armRight);
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		EntityGnome gnome = (EntityGnome) entity;
		
		head.rotateAngleX = headPitch * 0.017453292F;
		head.rotateAngleY = netHeadYaw * 0.017453292F;
		
		// gnomes move their small legs and arms fast
		limbSwing *= 2;
		
		armRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		armRight.rotateAngleY = (float) -(Math.PI * .05);
		armRight.rotateAngleZ = 0;
		armLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		armLeft.rotateAngleY = (float) (Math.PI * .05);
		armLeft.rotateAngleZ = 0;
		
		legRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		legLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		
		body.offsetY = 0;
		body.rotateAngleX = 0;
		legLeft.offsetY = (5f / 16f);
		legLeft.offsetZ = 0;
		legRight.offsetY = (5f / 16f);
		legRight.offsetZ = 0;
		
		if (gnome.isSwingInProgress || gnome.getPose() != ArmPoseGnome.IDLE) {
			
			// Either squatting down and trying to pick something up, or carrying something.
			if (gnome.getPose() == ArmPoseGnome.WORKING || gnome.isSwingInProgress) {
				float bend = (float) (Math.sin(swingProgress * Math.PI) * (Math.PI * .1));
				float offsetY = (float) (Math.sin(swingProgress * Math.PI) * (1f / 16f));
				body.offsetY += offsetY;
				body.rotateAngleX = bend;
				legLeft.offsetY -= offsetY;
				legLeft.offsetZ -= offsetY;
				legLeft.rotateAngleX = -bend;
				legRight.offsetY -= offsetY;
				legRight.offsetZ -= offsetY;
				legRight.rotateAngleX = -bend;
				
				armRight.rotateAngleX = (float) -(Math.PI * .3);
				armLeft.rotateAngleX = (float) -(Math.PI * .3);
				armRight.rotateAngleY = 0f;
				armLeft.rotateAngleY = 0f;
			} else if (gnome.getPose() == ArmPoseGnome.CARRYING) {
				armRight.rotateAngleX = (float) -(Math.PI * .5);
				armRight.rotateAngleY = (float) -(Math.PI * .1);
				armLeft.rotateAngleX = (float) -(Math.PI * .5);
				armLeft.rotateAngleY = (float) (Math.PI * .1);
			}
		}
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		
		//EntityGnome gnome = (EntityGnome) entity;
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headAngleY, headAngleX, scale, entity);
		
		body.render(scale);
	}
	
}
