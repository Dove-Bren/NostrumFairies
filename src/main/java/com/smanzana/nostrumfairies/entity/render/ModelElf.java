package com.smanzana.nostrumfairies.entity.render;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityElf.ArmPose;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelElf extends ModelBase {

	protected ModelRenderer body;
	protected ModelRenderer head;
	protected ModelRenderer legLeft;
	protected ModelRenderer legRight;
	protected ModelRenderer armLeft;
	protected ModelRenderer armRight;
	protected @Nullable ModelRenderer heldMain;
	protected @Nullable ModelRenderer heldOff;
	
	private static final int ELF_TEX_W = 64;
	private static final int ELF_TEX_H = 32;
	
	public ModelElf(boolean leftHanded) {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		body = new ModelRenderer(this, 0, 0);
		body.setTextureSize(ELF_TEX_W, ELF_TEX_H);
		body.setRotationPoint(0, 7, 0);
		body.addBox(-4, -7, -2, 8, 14, 4);
		
		head = new ModelRenderer(this, 24, 0);
		head.setTextureSize(ELF_TEX_W, ELF_TEX_H);
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
		legLeft.setTextureSize(ELF_TEX_W, ELF_TEX_H);
		legLeft.setRotationPoint(0, 0, 0);
		legLeft.addBox(-2, 0, -2, 3, 10, 4);
		legLeft.offsetY = (7f / 16f);
		legLeft.offsetX = (3f / 16f);
		body.addChild(legLeft);

		legRight = new ModelRenderer(this, 0, 18);
		legRight.mirror = true;
		legRight.setTextureSize(ELF_TEX_W, ELF_TEX_H);
		legRight.setRotationPoint(0, 0, 0);
		legRight.addBox(-2, 0, -2, 3, 10, 4);
		legRight.offsetY = (7f / 16f);
		legRight.offsetX = (-2f / 16f);
		body.addChild(legRight);
		
		armLeft = new ModelRenderer(this, 48, 0);
		armLeft.setTextureSize(ELF_TEX_W, ELF_TEX_H);
		armLeft.setRotationPoint(0, 1, 0);
		armLeft.addBox(-1.5f, -1, -1.5f, 3, 12, 3);
		armLeft.offsetY = (-7f / 16f);
		armLeft.offsetX = ((4 + 1.5f) / 16f);
		body.addChild(armLeft);
		
		armRight = new ModelRenderer(this, 48, 0);
		armRight.mirror = true;
		armRight.setTextureSize(ELF_TEX_W, ELF_TEX_H);
		armRight.setRotationPoint(0, 1, 0);
		armRight.addBox(-1.5f, -1, -1.5f, 3, 12, 3);
		armRight.offsetY = (-7f / 16f);
		armRight.offsetX = (-(4 + 1.5f) / 16f);
		body.addChild(armRight);
		
		heldMain = getInHand(true);
		heldOff = getInHand(false);
		
		if (leftHanded) {
			if (heldMain != null) {
				armLeft.addChild(heldMain);
			}
			if (heldOff != null) {
				armRight.addChild(heldOff);
			}
		} else {
			if (heldMain != null) {
				armRight.addChild(heldMain);
			}
			if (heldOff != null) {
				armLeft.addChild(heldOff);
			}
		}
		
	}
	
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headAngleY, float headAngleX, float scaleFactor, Entity entity) {
		EntityElf elf = (EntityElf) entity;
//		final float period = (20f * .15f); //.15f
//		float progress = (ageInTicks % period) / period;
//		
//		float angle = (float) (Math.sin(progress * Math.PI * 2) * (Math.PI / 4));
//		wingLeft.rotateAngleZ = angle;
//		wingLeftBack.rotateAngleZ = angle;
//		wingRight.rotateAngleZ = -angle;
//		wingRightBack.rotateAngleZ = -angle;
		
		body.rotateAngleY = 0;
		body.rotateAngleX = 0;
		body.offsetY = 0;
		head.rotateAngleX = headAngleX * 0.017453292F;
		head.rotateAngleY = headAngleY * 0.017453292F;
		
		// Artificially adjust head pitch when working to look up at tree
		if (elf.getPose() == ArmPose.WORKING) {
			head.rotateAngleX -= (float) (Math.PI * .25);
		}
		
		// dwarves move their small legs and arms fast
		limbSwing *= 2;
		
		armRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		armRight.rotateAngleZ = 0;
		armRight.rotateAngleY = 0;
		armRight.offsetY = (-7f / 16f);
		armRight.offsetX = (-(4 + 1.5f) / 16f);
		armRight.offsetZ = 0;
		armLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		armLeft.rotateAngleZ = 0;
		armLeft.rotateAngleY = 0;
		armLeft.offsetY = (-7f / 16f);
		armLeft.offsetX = ((4 + 1.5f) / 16f);
		armLeft.offsetZ = 0;
		
		
		legRight.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		legRight.offsetY = (7f / 16f);
		legRight.offsetX = (-2f / 16f);
		legRight.offsetZ = 0;
		legLeft.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		legLeft.offsetY = (7f / 16f);
		legLeft.offsetX = (2f / 16f);
		legLeft.offsetZ = 0;
		
		if (elf.isSwingInProgress || elf.getPose() != ArmPose.IDLE) {
			if (heldMain != null) {
				heldMain.rotateAngleX = (float) (.9 * Math.PI);
				//heldMain.rotateAngleY = 0;
			}
			
			ModelRenderer hand = (elf.isLeftHanded() ? armLeft : armRight);
			
			//if (elf.getPose() == ArmPose.CHOPPING)
			{
				double range = .025;
				hand.rotateAngleZ = 0;
				hand.rotateAngleX = (float) (-(Math.PI * .75) - (Math.PI * range * Math.sin(swingProgress * 2 * Math.PI)));
				hand.rotateAngleY = (float) -(Math.PI * range * Math.sin(swingProgress * 2 * Math.PI));
				
			}
		} else {
			if (heldMain != null) {
				heldMain.rotateAngleX = (float) (.5 * Math.PI);
				heldMain.rotateAngleZ = 0; 
				heldMain.rotateAngleY = 0;
			}
		}
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		body.render(scale);
	}
	
	protected ModelRenderer getInHand(boolean mainHand) {
		ModelRenderer render = null;
		if (mainHand) {
			render = new ModelRenderer(this, 48, 25);
			render.setTextureSize(ELF_TEX_W, ELF_TEX_H);
			render.setRotationPoint(0, 0, 0);
			render.addBox(-.5f, -6, -.5f, 1, 6, 1);
			render.setTextureOffset(44, 28);
			render.addBox(-1.5f, -4, -.5f, 1, 1, 1);
			render.setTextureOffset(52, 27);
			render.addBox(0.5f, -8, 0, 1, 4, 1);
			render.setTextureOffset(52, 25);
			render.addBox(1.5f, -8, 0, 1, 1, 1);
			render.setTextureOffset(60, 29);
			render.addBox(-.5f, -9, 0, 1, 2, 1);
			render.setTextureOffset(56, 28);
			render.addBox(-1.5f, -11, -.5f, 1, 3, 1);
			
			render.offsetY = (10f / 16f); // height of arm, - a bit
		}
		
		return render;
	}
	
}
