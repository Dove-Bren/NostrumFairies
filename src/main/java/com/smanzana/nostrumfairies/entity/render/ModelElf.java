package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityElf.ArmPose;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelElf extends ModelBase {

	private ModelRenderer body;
	private ModelRenderer head;
	private ModelRenderer legLeft;
	private ModelRenderer legRight;
	private ModelRenderer armLeft;
	private ModelRenderer armRight;
	private ModelRenderer wand;
	
	public ModelElf(boolean leftHanded) {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		final int textW = 64;
		final int textH = 32;
		
		body = new ModelRenderer(this, 0, 0);
		body.setTextureSize(textW, textH);
		body.setRotationPoint(0, 7, 0);
		body.addBox(-4, -7, -2, 8, 14, 4);
		
		head = new ModelRenderer(this, 24, 0);
		head.setTextureSize(textW, textH);
		head.setRotationPoint(0, 0, 0);
		head.addBox(-3, -6, -3, 6, 6, 6);
		// left ear
		head.setTextureOffset(24, 12);
		head.addBox(3, -8, 0, 2, 4, 1);
		head.setTextureOffset(30, 12);
		head.addBox(3, -7, -1, 1, 4, 1);
		
		// right ear
		head.setTextureOffset(24, 17);
		head.addBox(-5, -8, 0, 2, 4, 1);
		head.setTextureOffset(30, 17);
		head.addBox(-4, -7, -1, 1, 4, 1);
		
		head.offsetY = (-7f / 16f);
		body.addChild(head);
		
		legLeft = new ModelRenderer(this, 0, 18);
		legLeft.setTextureSize(textW, textH);
		legLeft.setRotationPoint(0, 0, 0);
		legLeft.addBox(-2, 0, -2, 3, 10, 4);
		legLeft.offsetY = (7f / 16f);
		legLeft.offsetX = (3f / 16f);
		body.addChild(legLeft);

		legRight = new ModelRenderer(this, 0, 18);
		legRight.mirror = true;
		legRight.setTextureSize(textW, textH);
		legRight.setRotationPoint(0, 0, 0);
		legRight.addBox(-2, 0, -2, 3, 10, 4);
		legRight.offsetY = (7f / 16f);
		legRight.offsetX = (-2f / 16f);
		body.addChild(legRight);
		
		armLeft = new ModelRenderer(this, 48, 0);
		armLeft.setTextureSize(textW, textH);
		armLeft.setRotationPoint(0, 1, 0);
		armLeft.addBox(-1.5f, -1, -1.5f, 3, 12, 3);
		armLeft.offsetY = (-7f / 16f);
		armLeft.offsetX = ((4 + 1.5f) / 16f);
		body.addChild(armLeft);
		
		armRight = new ModelRenderer(this, 48, 0);
		armRight.mirror = true;
		armRight.setTextureSize(textW, textH);
		armRight.setRotationPoint(0, 1, 0);
		armRight.addBox(-1.5f, -1, -1.5f, 3, 12, 3);
		armRight.offsetY = (-7f / 16f);
		armRight.offsetX = (-(4 + 1.5f) / 16f);
		body.addChild(armRight);
		
		wand = new ModelRenderer(this, 48, 25);
		wand.setTextureSize(textW, textH);
		wand.setRotationPoint(0, 0, 0);
		wand.addBox(-.5f, -6, -.5f, 1, 6, 1);
		wand.setTextureOffset(44, 28);
		wand.addBox(-1.5f, -4, -.5f, 1, 1, 1);
		wand.setTextureOffset(52, 27);
		wand.addBox(0.5f, -8, 0, 1, 4, 1);
		wand.setTextureOffset(52, 25);
		wand.addBox(1.5f, -8, 0, 1, 1, 1);
		wand.setTextureOffset(60, 29);
		wand.addBox(-.5f, -9, 0, 1, 2, 1);
		wand.setTextureOffset(56, 28);
		wand.addBox(-1.5f, -11, -.5f, 1, 3, 1);
		
		wand.offsetY = (10f / 16f); // height of arm, - a bit
		
		if (leftHanded) {
			armLeft.addChild(wand);
		} else {
			armRight.addChild(wand);
		}
		
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		
		EntityElf elf = (EntityElf) entity;
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
		
		if (elf.isSwingInProgress || elf.getPose() != ArmPose.IDLE) {
			wand.rotateAngleX = (float) (.9 * Math.PI);
			//wand.rotateAngleY = 0;
			
			ModelRenderer hand = (elf.isLeftHanded() ? armLeft : armRight);
			
			//if (elf.getPose() == ArmPose.CHOPPING)
			{
				double range = .025;
				hand.rotateAngleZ = 0;
				hand.rotateAngleX = (float) (-(Math.PI * .75) - (Math.PI * range * Math.sin(swingProgress * 2 * Math.PI)));
				hand.rotateAngleY = (float) -(Math.PI * range * Math.sin(swingProgress * 2 * Math.PI));
				
			}
		} else {
			wand.rotateAngleX = (float) (.5 * Math.PI);
			wand.rotateAngleZ = 0; 
			wand.rotateAngleY = 0;
		}
		
		body.render(scale);
	}
	
}
