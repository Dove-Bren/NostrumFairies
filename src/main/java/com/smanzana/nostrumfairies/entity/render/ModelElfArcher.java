package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.entity.fey.EntityElf.ArmPose;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher.BattleStance;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class ModelElfArcher extends ModelElf {

	private static final int ELF_TEX_W = 64;
	private static final int ELF_TEX_H = 32;
	
	protected ModelRenderer bow;
	protected ModelRenderer dagger;
	protected final boolean leftHanded;
	
	public ModelElfArcher(boolean leftHanded) {
		super(leftHanded);
		this.leftHanded = leftHanded;
	}
	
	protected ModelRenderer makeBow() {
		ModelRenderer bow = new ModelRenderer(this, 42, 18);
		bow.setTextureSize(ELF_TEX_W, ELF_TEX_H);
		bow.setRotationPoint(0, 0, 0);
		bow.addBox(-3, 0, -.5f, 6, 1, 1);
		
		bow.setTextureOffset(42, 20);
		bow.addBox(-5, -1, -.5f, 3, 1, 1);
		bow.setTextureOffset(42, 22);
		bow.addBox(-6, -2, -.5f, 2, 1, 1);
		bow.setTextureOffset(42, 24);
		bow.addBox(-7, -3, -.5f, 2, 1, 1);
		
		bow.setTextureOffset(50, 20);
		bow.addBox(2, -1, -.5f, 4, 1, 1);
		bow.setTextureOffset(50, 22);
		bow.addBox(5, -2, -.5f, 3, 1, 1);
		bow.setTextureOffset(50, 24);
		bow.addBox(7, -3, -.5f, 2, 1, 1);
		
		bow.offsetY = (11f / 16f); // height of arm, - a bit
		
		return bow;
	}
	
	protected ModelRenderer makeDagger() {
		ModelRenderer dagger = new ModelRenderer(this, 60, 18);
		dagger.setTextureSize(ELF_TEX_W, ELF_TEX_H);
		dagger.setRotationPoint(0, 0, 0);
		dagger.addBox(-.5f, 0, -.5f, 1, 4, 1);
		dagger.setTextureOffset(60, 23);
		dagger.addBox(-1.5f, 3, -.5f, 1, 5, 1);
		dagger.setTextureOffset(60, 29);
		dagger.addBox(-.5f, 7, -.5f, 1, 2, 1);
		
		dagger.offsetY = (10f / 16f); // height of arm, - a bit
		
		return dagger;
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headAngleY, float headAngleX, float scaleFactor, Entity entity) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headAngleY, headAngleX, scaleFactor, entity);
		
		final boolean isAttacking;
		final boolean useBow;
		
		if (entity instanceof EntityElfArcher) {
			EntityElfArcher elf = (EntityElfArcher) entity;
			isAttacking = elf.getPose() == ArmPose.ATTACKING;
			useBow = elf.getStance() == BattleStance.RANGED;
		} else if (entity instanceof EntityShadowFey) {
			EntityShadowFey shadow = (EntityShadowFey) entity;
			isAttacking = shadow.getStance() != EntityShadowFey.BattleStance.IDLE;
			useBow = shadow.getStance() != EntityShadowFey.BattleStance.MELEE;
		} else {
			isAttacking = (entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).getAITarget() != null : false);
			useBow = false;
		}
		
		dagger.rotateAngleZ = 0;
		dagger.rotateAngleY = 0;
		dagger.rotateAngleX = -(float) (Math.PI/2);
		
		bow.rotateAngleX = 0;
		bow.rotateAngleY = -(float) (Math.PI/2);;
		bow.rotateAngleZ = 0;
		
		if (isAttacking) {
			if (useBow) {
				double targetDist = 0;
				//EntityLivingBase target = elf.getAttackTarget();
				ModelRenderer mainArm = this.armLeft;
				ModelRenderer offArm = this.armRight;
				
				if (leftHanded) {
					mainArm = this.armRight;
					offArm = this.armLeft;
				}
				float sign = (leftHanded ? -1 : 1);
//				if (target != null) {
//					targetDist = elf.getDistanceSqToEntity(target);
//				}
				// Target isn't available on client :/ So just hardcode something. Maybe I'll pipe it through...
				targetDist = 16;
				
				// Default position
				mainArm.rotateAngleX = (float) -(Math.PI * (.5 + .2 * Math.min(1, targetDist / 64)));
				offArm.rotateAngleX = (float) -(Math.PI * (.5 + .2 * Math.min(1, targetDist / 64)));
				offArm.rotateAngleY = .1f;
				offArm.offsetX += (sign) * entity.width / 2;
				offArm.offsetZ -= entity.width / 4;
				offArm.offsetY += entity.height / 24;
				
				//if (elf.isSwingInProgress || swingProgress > 0f) {
				if (swingProgress > 0f) {
					// .1 to reach string and notch arrow
					if (swingProgress < .1f) {
						float progress = swingProgress / .1f;
						offArm.offsetX += (sign) * (entity.width / 3) * Math.sin(Math.PI * .5 * progress);
					} else if (swingProgress < .5f) { // .4 to draw it back
						float progress = (swingProgress - .1f) / .4f;
						offArm.offsetX += (sign) * (entity.width / 3) * Math.sin(Math.PI * (.5 + .5 * progress));
					} else {
						; //hold initial position
					}
				}
				
				double tilt = .5;
				mainArm.rotateAngleY += -sign * (float) (Math.PI * tilt);
				offArm.rotateAngleY += -sign * (float) (Math.PI * tilt);
				this.body.rotateAngleY += sign * (float) (Math.PI * tilt);
				this.head.rotateAngleY += -sign * (float) (Math.PI * tilt);
				
				double magnitude = .075;
				float bend = (float) (Math.PI * magnitude);
				float offsetY = (float) magnitude / 8;
				body.offsetY += offsetY;
				body.rotateAngleX = bend;
				legLeft.offsetY -= offsetY;
				legLeft.offsetZ -= offsetY;
				legLeft.rotateAngleX = -bend;
				legRight.offsetY -= offsetY;
				legRight.offsetZ -= offsetY;
				legRight.rotateAngleX = -bend;
				
			} else {
				ModelRenderer mainArm = this.armRight;
				
				if (leftHanded) {
					mainArm = this.armLeft;
				}
				float sign = (leftHanded ? -1 : 1);
				double magnitude = .075;
				float bend = (float) (Math.PI * magnitude);
				float offsetY = (float) magnitude / 8;
				body.offsetY += offsetY;
				body.rotateAngleX = bend;
				legLeft.offsetY -= offsetY;
				legLeft.offsetZ -= offsetY;
				legLeft.rotateAngleX = -bend;
				legRight.offsetY -= offsetY;
				legRight.offsetZ -= offsetY;
				legRight.rotateAngleX = -bend;

				mainArm.rotateAngleZ = sign * (float) -(Math.PI * .25);
				mainArm.rotateAngleY = sign * (float) -(Math.PI * 0);
				mainArm.rotateAngleX = (float) -(Math.PI * .2);
				
				//if (elf.isSwingInProgress || swingProgress > 0f) {
				if (swingProgress > 0f) {
					// .5 to dip in before exploding out
					if (swingProgress < .5f) {
						float progress = swingProgress / .5f;
						mainArm.rotateAngleX += (float) (Math.PI * .05 * Math.sin(progress * (Math.PI / 2)));
						mainArm.rotateAngleY += sign * (float) -(Math.PI * .1 * Math.sin(progress * (Math.PI / 2)));
					} else if (swingProgress < .7f) { // .2 to strike
						float progress = (swingProgress - .5f) / .2f;
						mainArm.rotateAngleX += (float) (Math.PI * (.05 + (-.25 * Math.sin(progress * (Math.PI / 2)))));
						mainArm.rotateAngleY += sign * (float) -(Math.PI * .1);
					} else {
						float progress = (swingProgress - .7f) / .3f;
						mainArm.rotateAngleX += (float) (Math.PI * -.2 * Math.sin((1 + progress) * (Math.PI / 2)));
						mainArm.rotateAngleY += sign * (float) -(Math.PI * .1 * Math.sin((1 + progress) * (Math.PI / 2)));
					}
				} else {
					armRight.rotateAngleX += MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.15F;
					armLeft.rotateAngleX += MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.15F;
				}
			}
		}
		
		
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		
		final boolean useBow;
		
		if (entity instanceof EntityElfArcher) {
			EntityElfArcher elf = (EntityElfArcher) entity;
			useBow = elf.getStance() == BattleStance.RANGED;
		} else if (entity instanceof EntityShadowFey) {
			EntityShadowFey shadow = (EntityShadowFey) entity;
			useBow = shadow.getStance() == EntityShadowFey.BattleStance.RANGED;
		} else {
			useBow = false;
		}
		
		// Hide whichever weapon we're not using
		bow.showModel = useBow;
		dagger.showModel = !useBow;
		
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, headAngleY, headAngleX, scale);
	}
	
	protected ModelRenderer getInHand(boolean mainHand) {
		if (mainHand) {
			dagger = makeDagger();
			return dagger;
		}
		
		bow = makeBow();
		return bow;
	}
	
}
