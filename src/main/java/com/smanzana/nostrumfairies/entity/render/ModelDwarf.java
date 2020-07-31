package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf.ArmPose;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelDwarf extends ModelBase {

	private ModelRenderer body;
	private ModelRenderer head;
	private ModelRenderer legLeft;
	private ModelRenderer legRight;
	private ModelRenderer armLeft;
	private ModelRenderer armRight;
	private ModelRenderer pick;
	
	public ModelDwarf(boolean leftHanded) {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		final int textW = 128;
		final int textH = 64;
		
		body = new ModelRenderer(this, 0, 0);
		body.setTextureSize(textW, textH);
		body.setRotationPoint(0, 11, 0);
		body.addBox(-7, -9, -4, 14, 18, 8);
		
		head = new ModelRenderer(this, 44, 0);
		head.setTextureSize(textW, textH);
		head.setRotationPoint(0, 0, 0);
		head.addBox(-4, -8, -4, 8, 8, 8);
		head.setTextureOffset(44, 16);
		head.addBox(4, -6, -1, 1, 2, 1);
		head.setTextureOffset(44, 16);
		head.addBox(-5, -6, -1, 1, 2, 1);
		head.offsetY = (-9f / 16f);
		body.addChild(head);
		
		legLeft = new ModelRenderer(this, 0, 41);
		legLeft.setTextureSize(textW, textH);
		legLeft.setRotationPoint(0, 0, 0);
		legLeft.addBox(-2.5f, 0, -3, 5, 8, 6);
		legLeft.offsetY = (5f / 16f);
		legLeft.offsetX = (3f / 16f);
		body.addChild(legLeft);

		legRight = new ModelRenderer(this, 0, 41);
		legRight.mirror = true;
		legRight.setTextureSize(textW, textH);
		legRight.setRotationPoint(0, 0, 0);
		legRight.addBox(-2.5f, 0, -3, 5, 8, 6);
		legRight.offsetY = (5f / 16f);
		legRight.offsetX = (-3f / 16f);
		body.addChild(legRight);
		
		armLeft = new ModelRenderer(this, 0, 26);
		armLeft.setTextureSize(textW, textH);
		armLeft.setRotationPoint(0, 0, 0);
		armLeft.addBox(-2.5f, 0, -2.5f, 5, 10, 5);
		armLeft.offsetY = (-7f / 16f);
		armLeft.offsetX = ((7 + 2.5f) / 16f);
		body.addChild(armLeft);
		
		armRight = new ModelRenderer(this, 0, 26);
		armRight.mirror = true;
		armRight.setTextureSize(textW, textH);
		armRight.setRotationPoint(0, 0, 0);
		armRight.addBox(-2.5f, 0, -2.5f, 5, 10, 5);
		armRight.offsetY = (-7f / 16f);
		armRight.offsetX = (-(7 + 2.5f) / 16f);
		body.addChild(armRight);
		
		pick = new ModelRenderer(this, 84, 0);
		pick.setTextureSize(textW, textH);
		pick.setRotationPoint(-0.5f, 0, -0.5f);
		pick.addBox(0, -14, 0, 1, 14, 1);
		pick.setTextureOffset(99, 5);
		pick.addBox(-1, -15, -.5f, 3, 1, 2);
		pick.setTextureOffset(95, 2);
		pick.addBox(-3, -16, -.5f, 7, 1, 2);
		pick.setTextureOffset(98, 0);
		pick.addBox(-2, -17, 0, 5, 1, 1);
		pick.setTextureOffset(91, 5);
		pick.addBox(3, -15, 0, 2, 1, 1);
		pick.setTextureOffset(111, 5);
		pick.addBox(-4, -15, 0, 2, 1, 1);
		pick.offsetY = (9f / 16f); // height of arm, - a bit
		
		if (leftHanded) {
			armLeft.addChild(pick);
		} else {
			armRight.addChild(pick);
		}
		
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		
		EntityDwarf dwarf = (EntityDwarf) entity;
//		final float period = (20f * .15f); //.15f
//		float progress = (ageInTicks % period) / period;
//		
//		float angle = (float) (Math.sin(progress * Math.PI * 2) * (Math.PI / 4));
//		wingLeft.rotateAngleZ = angle;
//		wingLeftBack.rotateAngleZ = angle;
//		wingRight.rotateAngleZ = -angle;
//		wingRightBack.rotateAngleZ = -angle;
		
		head.rotateAngleX = headAngleX * 0.017453292F;
		head.rotateAngleY = headAngleY * 0.017453292F;
		
		// dwarves move their small legs and arms fast
		limbSwing *= 2;
		
		armRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		armRight.rotateAngleZ = 0;
		armLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		armLeft.rotateAngleZ = 0;
		
		legRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		legLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		
		if (dwarf.isSwingInProgress || dwarf.getPose() != ArmPose.IDLE) {
			int sign = 1;//(dwarf.isLeftHanded() ? -1 : 1);
			ModelRenderer hand = (dwarf.isLeftHanded() ? armLeft : armRight);
			
			if (dwarf.getPose() == ArmPose.MINING) {
				double lowX = -sign * (Math.PI * .75);
				double diffX = sign * (Math.PI * .4);
				float periodFirst = .7f;
				if (this.swingProgress < periodFirst) {
					float progress = (swingProgress / periodFirst);
					hand.rotateAngleZ = 0;
					hand.rotateAngleY = 0;
					hand.rotateAngleX = (float) (lowX + (diffX * Math.sin(Math.PI * progress)));
				} else {
					// Waiting for the next strike
					hand.rotateAngleZ = 0;
					hand.rotateAngleX = (float) lowX;
					hand.rotateAngleY = 0;
				}
			} else if (dwarf.getPose() == ArmPose.ATTACKING) {
				// Have pick raised and do full swings
				double lowX = -sign * (Math.PI * .95);
				double diffX = sign * (Math.PI * .8);
				float periodFirst = .7f;
				if (this.swingProgress < periodFirst) {
					float progress = (swingProgress / periodFirst);
					hand.rotateAngleZ = 0;
					hand.rotateAngleY = 0;
					hand.rotateAngleX = (float) (lowX + (diffX * Math.sin(Math.PI * progress)));
				} else {
					// Waiting for the next strike
					hand.rotateAngleZ = 0;
					hand.rotateAngleX = (float) lowX;
					hand.rotateAngleY = 0;
				}
			} else {
				final double peakX = -sign * (Math.PI * 1.15);
				float periodFirst = .2f;
				float periodSecond = .1f;
				float periodThird = 1 - (periodFirst + periodSecond);
				if (this.swingProgress < periodFirst) {
					// first part. Wind up!
					// from (0, 0, 0) to (-(PI-peakX), pi, pi)
					float progress = (swingProgress / periodFirst);
					hand.rotateAngleZ = 0;
					hand.rotateAngleY = 0;
					hand.rotateAngleX = (float) (peakX * Math.sin(.5 * Math.PI * progress));
				}
				else if (this.swingProgress < (periodFirst + periodSecond)) {
	//				// stall and build anticipation
					hand.rotateAngleZ = 0;//(float) (sign * Math.PI);
					hand.rotateAngleX = (float) peakX;
					hand.rotateAngleY = 0;
				}
				else {
					// swing
					float progress = (swingProgress - (periodFirst + periodSecond)) / periodThird;
					hand.rotateAngleZ = 0;
					hand.rotateAngleY = 0;
					hand.rotateAngleX = (float) (peakX * Math.sin((Math.PI * .5) + (.5 * Math.PI * progress)));
					
				}
			}
		}
		
		pick.rotateAngleZ = (float) (.5 * Math.PI) - .2f;
		pick.rotateAngleX = (float) (.5 * Math.PI);
		
		body.render(scale);
	}
	
}
