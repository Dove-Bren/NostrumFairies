package com.smanzana.nostrumfairies.entity.render;

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
		legLeft.offsetX = (2.5f / 16f);
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
		legRight.offsetX = (-2.5f / 16f);
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
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		
		//EntityGnome gnome = (EntityGnome) entity;
		
		head.rotateAngleX = headAngleX * 0.017453292F;
		head.rotateAngleY = headAngleY * 0.017453292F;
		
		// gnomes move their small legs and arms fast
		limbSwing *= 2;
		
		armRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		armRight.rotateAngleZ = 0;
		armLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		armLeft.rotateAngleZ = 0;
		
		legRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		legLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		
//		if (dwarf.isSwingInProgress || dwarf.getPose() != ArmPose.IDLE) {
//			int sign = 1;//(dwarf.isLeftHanded() ? -1 : 1);
//			ModelRenderer hand = (dwarf.isLeftHanded() ? armLeft : armRight);
//			
//			if (dwarf.getPose() == ArmPose.MINING) {
//				double lowX = -sign * (Math.PI * .75);
//				double diffX = sign * (Math.PI * .4);
//				float periodFirst = .4f;
//				if (this.swingProgress < periodFirst) {
//					float progress = (swingProgress / periodFirst);
//					hand.rotateAngleZ = 0;
//					hand.rotateAngleY = 0;
//					hand.rotateAngleX = (float) (lowX + (diffX * Math.sin(Math.PI * progress)));
//				} else {
//					// Waiting for the next strike
//					hand.rotateAngleZ = 0;
//					hand.rotateAngleX = (float) lowX;
//					hand.rotateAngleY = 0;
//				}
//			} else if (dwarf.getPose() == ArmPose.ATTACKING) {
//				// Have pick raised and do full swings
//				double lowX = -sign * (Math.PI * .95);
//				double diffX = sign * (Math.PI * .8);
//				float periodFirst = .3f;
//				if (this.swingProgress < periodFirst) {
//					float progress = (swingProgress / periodFirst);
//					hand.rotateAngleZ = 0;
//					hand.rotateAngleY = 0;
//					hand.rotateAngleX = (float) (lowX + (diffX * Math.sin(Math.PI * progress)));
//				} else {
//					// Waiting for the next strike
//					hand.rotateAngleZ = 0;
//					hand.rotateAngleX = (float) lowX;
//					hand.rotateAngleY = 0;
//				}
//			} else {
//				final double peakX = -sign * (Math.PI * 1.15);
//				float periodFirst = .2f;
//				float periodSecond = .1f;
//				float periodThird = 1 - (periodFirst + periodSecond);
//				if (this.swingProgress < periodFirst) {
//					// first part. Wind up!
//					// from (0, 0, 0) to (-(PI-peakX), pi, pi)
//					float progress = (swingProgress / periodFirst);
//					hand.rotateAngleZ = 0;
//					hand.rotateAngleY = 0;
//					hand.rotateAngleX = (float) (peakX * Math.sin(.5 * Math.PI * progress));
//				}
//				else if (this.swingProgress < (periodFirst + periodSecond)) {
//	//				// stall and build anticipation
//					hand.rotateAngleZ = 0;//(float) (sign * Math.PI);
//					hand.rotateAngleX = (float) peakX;
//					hand.rotateAngleY = 0;
//				}
//				else {
//					// swing
//					float progress = (swingProgress - (periodFirst + periodSecond)) / periodThird;
//					hand.rotateAngleZ = 0;
//					hand.rotateAngleY = 0;
//					hand.rotateAngleX = (float) (peakX * Math.sin((Math.PI * .5) + (.5 * Math.PI * progress)));
//					
//				}
//			}
//		}
		
		body.render(scale);
	}
	
}
