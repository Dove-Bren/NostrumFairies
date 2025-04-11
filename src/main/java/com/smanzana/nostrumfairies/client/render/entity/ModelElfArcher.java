package com.smanzana.nostrumfairies.client.render.entity;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.BattleStanceElfArcher;
import com.smanzana.nostrumfairies.serializers.BattleStanceShadowFey;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

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
		bow.setTexSize(ELF_TEX_W, ELF_TEX_H);
		bow.setPos(0, 0, 0);
		bow.addBox(-3, 0, -.5f, 6, 1, 1);
		
		bow.texOffs(42, 20);
		bow.addBox(-5, -1, -.5f, 3, 1, 1);
		bow.texOffs(42, 22);
		bow.addBox(-6, -2, -.5f, 2, 1, 1);
		bow.texOffs(42, 24);
		bow.addBox(-7, -3, -.5f, 2, 1, 1);
		
		bow.texOffs(50, 20);
		bow.addBox(2, -1, -.5f, 4, 1, 1);
		bow.texOffs(50, 22);
		bow.addBox(5, -2, -.5f, 3, 1, 1);
		bow.texOffs(50, 24);
		bow.addBox(7, -3, -.5f, 2, 1, 1);
		
		bow.offsetY = (11f / 16f); // height of arm, - a bit
		
		return bow;
	}
	
	protected OffsetModelRenderer makeDagger() {
		OffsetModelRenderer dagger = new OffsetModelRenderer(this, 60, 18);
		dagger.setTexSize(ELF_TEX_W, ELF_TEX_H);
		dagger.setPos(0, 0, 0);
		dagger.addBox(-.5f, 0, -.5f, 1, 4, 1);
		dagger.texOffs(60, 23);
		dagger.addBox(-1.5f, 3, -.5f, 1, 5, 1);
		dagger.texOffs(60, 29);
		dagger.addBox(-.5f, 7, -.5f, 1, 2, 1);
		
		dagger.offsetY = (10f / 16f); // height of arm, - a bit
		
		return dagger;
	}
	
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headAngleY, float headAngleX) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, headAngleY, headAngleX);
		
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
			isAttacking = (entity instanceof Mob ? ((Mob) entity).getTarget() != null : false);
			useBow = false;
		}
		
		dagger.zRot = 0;
		dagger.yRot = 0;
		dagger.xRot = -(float) (Math.PI/2);
		
		bow.xRot = 0;
		bow.yRot = -(float) (Math.PI/2);;
		bow.zRot = 0;
		
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
				mainArm.xRot = (float) -(Math.PI * (.5 + .2 * Math.min(1, targetDist / 64)));
				offArm.xRot = (float) -(Math.PI * (.5 + .2 * Math.min(1, targetDist / 64)));
				offArm.yRot = .1f;
				offArm.offsetX += (sign) * entity.getBbWidth() / 2;
				offArm.offsetZ -= entity.getBbWidth() / 4;
				offArm.offsetY += entity.getBbHeight() / 24;
				
				//if (elf.isSwingInProgress || swingProgress > 0f) {
				if (attackTime > 0f) {
					// .1 to reach string and notch arrow
					if (attackTime < .1f) {
						float progress = attackTime / .1f;
						offArm.offsetX += (sign) * (entity.getBbWidth() / 3) * Math.sin(Math.PI * .5 * progress);
					} else if (attackTime < .5f) { // .4 to draw it back
						float progress = (attackTime - .1f) / .4f;
						offArm.offsetX += (sign) * (entity.getBbWidth() / 3) * Math.sin(Math.PI * (.5 + .5 * progress));
					} else {
						; //hold initial position
					}
				}
				
				double tilt = .5;
				mainArm.yRot += -sign * (float) (Math.PI * tilt);
				offArm.yRot += -sign * (float) (Math.PI * tilt);
				this.body.yRot += sign * (float) (Math.PI * tilt);
				this.head.yRot += -sign * (float) (Math.PI * tilt);
				
				double magnitude = .075;
				float bend = (float) (Math.PI * magnitude);
				float offsetY = (float) magnitude / 8;
				body.offsetY += offsetY;
				body.xRot = bend;
				legLeft.offsetY -= offsetY;
				legLeft.offsetZ -= offsetY;
				legLeft.xRot = -bend;
				legRight.offsetY -= offsetY;
				legRight.offsetZ -= offsetY;
				legRight.xRot = -bend;
				
			} else {
				ModelPart mainArm = this.armRight;
				
				if (leftHanded) {
					mainArm = this.armLeft;
				}
				float sign = (leftHanded ? -1 : 1);
				double magnitude = .075;
				float bend = (float) (Math.PI * magnitude);
				float offsetY = (float) magnitude / 8;
				body.offsetY += offsetY;
				body.xRot = bend;
				legLeft.offsetY -= offsetY;
				legLeft.offsetZ -= offsetY;
				legLeft.xRot = -bend;
				legRight.offsetY -= offsetY;
				legRight.offsetZ -= offsetY;
				legRight.xRot = -bend;

				mainArm.zRot = sign * (float) -(Math.PI * .25);
				mainArm.yRot = sign * (float) -(Math.PI * 0);
				mainArm.xRot = (float) -(Math.PI * .2);
				
				//if (elf.isSwingInProgress || swingProgress > 0f) {
				if (attackTime > 0f) {
					// .5 to dip in before exploding out
					if (attackTime < .5f) {
						float progress = attackTime / .5f;
						mainArm.xRot += (float) (Math.PI * .05 * Math.sin(progress * (Math.PI / 2)));
						mainArm.yRot += sign * (float) -(Math.PI * .1 * Math.sin(progress * (Math.PI / 2)));
					} else if (attackTime < .7f) { // .2 to strike
						float progress = (attackTime - .5f) / .2f;
						mainArm.xRot += (float) (Math.PI * (.05 + (-.25 * Math.sin(progress * (Math.PI / 2)))));
						mainArm.yRot += sign * (float) -(Math.PI * .1);
					} else {
						float progress = (attackTime - .7f) / .3f;
						mainArm.xRot += (float) (Math.PI * -.2 * Math.sin((1 + progress) * (Math.PI / 2)));
						mainArm.yRot += sign * (float) -(Math.PI * .1 * Math.sin((1 + progress) * (Math.PI / 2)));
					}
				} else {
					armRight.xRot += Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.15F;
					armLeft.xRot += Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.15F;
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
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		// Hide whichever weapon we're not using
		bow.visible = useBow;
		dagger.visible = !useBow;
		
		super.renderToBuffer(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
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
