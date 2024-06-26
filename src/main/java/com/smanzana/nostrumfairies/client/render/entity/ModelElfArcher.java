package com.smanzana.nostrumfairies.client.render.entity;

import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.BattleStanceElfArcher;
import com.smanzana.nostrumfairies.serializers.BattleStanceShadowFey;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ModelElfArcher<T extends Entity> extends ModelElf<T> {

	private static final int ELF_TEX_W = 64;
	private static final int ELF_TEX_H = 32;
	
	protected OffsetModelRenderer bow;
	protected OffsetModelRenderer dagger;
	protected final boolean leftHanded;
	
	protected boolean useBow; // Dumb variable set up before render calls
	
	public ModelElfArcher(boolean leftHanded) {
		super(leftHanded);
		this.leftHanded = leftHanded;
	}
	
	public ModelElfArcher(boolean leftHanded, Function<ResourceLocation, RenderType> renderTypeMap) {
		super(leftHanded, renderTypeMap);
		this.leftHanded = leftHanded;
	}
	
	protected OffsetModelRenderer makeBow() {
		OffsetModelRenderer bow = new OffsetModelRenderer(this, 42, 18);
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
	
	protected OffsetModelRenderer makeDagger() {
		OffsetModelRenderer dagger = new OffsetModelRenderer(this, 60, 18);
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
	public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headAngleY, float headAngleX) {
		super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, headAngleY, headAngleX);
		
		final boolean isAttacking;
		final boolean useBow;
		
		if (entity instanceof EntityElfArcher) {
			EntityElfArcher elf = (EntityElfArcher) entity;
			isAttacking = elf.getElfPose() == ArmPoseElf.ATTACKING;
			useBow = elf.getStance() == BattleStanceElfArcher.RANGED;
		} else if (entity instanceof EntityShadowFey) {
			EntityShadowFey shadow = (EntityShadowFey) entity;
			isAttacking = shadow.getStance() != BattleStanceShadowFey.IDLE;
			useBow = shadow.getStance() != BattleStanceShadowFey.MELEE;
		} else {
			isAttacking = (entity instanceof MobEntity ? ((MobEntity) entity).getAttackTarget() != null : false);
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
				//LivingEntity target = elf.getAttackTarget();
				OffsetModelRenderer mainArm = this.armLeft;
				OffsetModelRenderer offArm = this.armRight;
				
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
				offArm.offsetX += (sign) * entity.getWidth() / 2;
				offArm.offsetZ -= entity.getWidth() / 4;
				offArm.offsetY += entity.getHeight() / 24;
				
				//if (elf.isSwingInProgress || swingProgress > 0f) {
				if (swingProgress > 0f) {
					// .1 to reach string and notch arrow
					if (swingProgress < .1f) {
						float progress = swingProgress / .1f;
						offArm.offsetX += (sign) * (entity.getWidth() / 3) * Math.sin(Math.PI * .5 * progress);
					} else if (swingProgress < .5f) { // .4 to draw it back
						float progress = (swingProgress - .1f) / .4f;
						offArm.offsetX += (sign) * (entity.getWidth() / 3) * Math.sin(Math.PI * (.5 + .5 * progress));
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
	
	public void setWeaponSelection(T entity) {
		if (entity instanceof EntityElfArcher) {
			EntityElfArcher elf = (EntityElfArcher) entity;
			useBow = elf.getStance() == BattleStanceElfArcher.RANGED;
		} else if (entity instanceof EntityShadowFey) {
			EntityShadowFey shadow = (EntityShadowFey) entity;
			useBow = shadow.getStance() == BattleStanceShadowFey.RANGED;
		} else {
			useBow = false;
		}
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		// Hide whichever weapon we're not using
		bow.showModel = useBow;
		dagger.showModel = !useBow;
		
		super.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	protected OffsetModelRenderer getInHand(boolean mainHand) {
		if (mainHand) {
			dagger = makeDagger();
			return dagger;
		}
		
		bow = makeBow();
		return bow;
	}
	
}
