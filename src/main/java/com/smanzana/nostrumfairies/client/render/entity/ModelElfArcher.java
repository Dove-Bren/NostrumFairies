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
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ModelElfArcher<T extends Entity> extends ModelElf<T> {
	
	public static LayerDefinition createLayer(boolean leftHanded) {
		MeshDefinition mesh = ModelElf.createMesh(leftHanded);
		PartDefinition root = mesh.getRoot();
		
		root.getChild("body").getChild("armMain").addOrReplaceChild("dagger",
				makeDagger(),
				PartPose.offset(0, (10f / 16f), 0)
		);
		//dagger.offsetY = (10f / 16f); // height of arm, - a bit
		
		root.getChild("body").getChild("armOff").addOrReplaceChild("bow",
				makeBow(),
				PartPose.offset(0, (11f / 16f), 0)
		);
		//bow.offsetY = (11f / 16f); // height of arm, - a bit
		
		return LayerDefinition.create(mesh, 64, 32);
	}
	
	protected static CubeListBuilder makeBow() {
		return CubeListBuilder.create()
				.texOffs(42, 18).addBox(-3, 0, -.5f, 6, 1, 1)
				.texOffs(42, 20) .addBox(-5, -1, -.5f, 3, 1, 1)
				.texOffs(42, 22).addBox(-6, -2, -.5f, 2, 1, 1)
				.texOffs(42, 24).addBox(-7, -3, -.5f, 2, 1, 1)
				.texOffs(50, 20).addBox(2, -1, -.5f, 4, 1, 1)
				.texOffs(50, 22).addBox(5, -2, -.5f, 3, 1, 1)
				.texOffs(50, 24).addBox(7, -3, -.5f, 2, 1, 1)
		;
	}
	
	protected static CubeListBuilder makeDagger() {
		return CubeListBuilder.create()
				.texOffs(60, 18).addBox(-.5f, 0, -.5f, 1, 4, 1)
				.texOffs(60, 23).addBox(-1.5f, 3, -.5f, 1, 5, 1)
				.texOffs(60, 29).addBox(-.5f, 7, -.5f, 1, 2, 1)
		;
	}
	
	protected ModelPart bow;
	protected ModelPart dagger;
	
	protected boolean useBow; // Dumb variable set up before render calls
	
	public ModelElfArcher(ModelPart root) {
		this(root, RenderType::entityCutoutNoCull);
	}
	
	public ModelElfArcher(ModelPart root, Function<ResourceLocation, RenderType> renderTypeMap) {
		super(root, renderTypeMap);
		
		bow = root.getChild("body").getChild("armOff").getChild("bow");
		dagger = root.getChild("body").getChild("armMain").getChild("dagger");
	}

	@Override
	protected ModelPart getHeldMainHand() {
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
				int unused;
				float sign = 1f;//TODO (leftHanded ? -1 : 1);
//				if (target != null) {
//					targetDist = elf.getDistanceSqToEntity(target);
//				}
				// Target isn't available on client :/ So just hardcode something. Maybe I'll pipe it through...
				targetDist = 16;
				
				// Default position
				armMain.xRot = (float) -(Math.PI * (.5 + .2 * Math.min(1, targetDist / 64)));
				armOff.xRot = (float) -(Math.PI * (.5 + .2 * Math.min(1, targetDist / 64)));
				armOff.yRot = .1f;
				armOff.x += (sign) * entity.getBbWidth() / 2;
				armOff.z -= entity.getBbWidth() / 4;
				armOff.y += entity.getBbHeight() / 24;
				
				//if (elf.isSwingInProgress || swingProgress > 0f) {
				if (attackTime > 0f) {
					// .1 to reach string and notch arrow
					if (attackTime < .1f) {
						float progress = attackTime / .1f;
						armOff.x += (sign) * (entity.getBbWidth() / 3) * Math.sin(Math.PI * .5 * progress);
					} else if (attackTime < .5f) { // .4 to draw it back
						float progress = (attackTime - .1f) / .4f;
						armOff.x += (sign) * (entity.getBbWidth() / 3) * Math.sin(Math.PI * (.5 + .5 * progress));
					} else {
						; //hold initial position
					}
				}
				
				double tilt = .5;
				armMain.yRot += -sign * (float) (Math.PI * tilt);
				armOff.yRot += -sign * (float) (Math.PI * tilt);
				this.body.yRot += sign * (float) (Math.PI * tilt);
				this.head.yRot += -sign * (float) (Math.PI * tilt);
				
				double magnitude = .075;
				float bend = (float) (Math.PI * magnitude);
				float offsetY = (float) magnitude / 8;
				body.y += offsetY;
				body.xRot = bend;
				legLeft.y -= offsetY;
				legLeft.z -= offsetY;
				legLeft.xRot = -bend;
				legRight.y -= offsetY;
				legRight.z -= offsetY;
				legRight.xRot = -bend;
				
			} else {
				int unused;
				float sign = 1;//(leftHanded ? -1 : 1); TODO
				double magnitude = .075;
				float bend = (float) (Math.PI * magnitude);
				float offsetY = (float) magnitude / 8;
				body.y += offsetY;
				body.xRot = bend;
				legLeft.y -= offsetY;
				legLeft.z -= offsetY;
				legLeft.xRot = -bend;
				legRight.y -= offsetY;
				legRight.z -= offsetY;
				legRight.xRot = -bend;

				armMain.zRot = sign * (float) -(Math.PI * .25);
				armMain.yRot = sign * (float) -(Math.PI * 0);
				armMain.xRot = (float) -(Math.PI * .2);
				
				//if (elf.isSwingInProgress || swingProgress > 0f) {
				if (attackTime > 0f) {
					// .5 to dip in before exploding out
					if (attackTime < .5f) {
						float progress = attackTime / .5f;
						armMain.xRot += (float) (Math.PI * .05 * Math.sin(progress * (Math.PI / 2)));
						armMain.yRot += sign * (float) -(Math.PI * .1 * Math.sin(progress * (Math.PI / 2)));
					} else if (attackTime < .7f) { // .2 to strike
						float progress = (attackTime - .5f) / .2f;
						armMain.xRot += (float) (Math.PI * (.05 + (-.25 * Math.sin(progress * (Math.PI / 2)))));
						armMain.yRot += sign * (float) -(Math.PI * .1);
					} else {
						float progress = (attackTime - .7f) / .3f;
						armMain.xRot += (float) (Math.PI * -.2 * Math.sin((1 + progress) * (Math.PI / 2)));
						armMain.yRot += sign * (float) -(Math.PI * .1 * Math.sin((1 + progress) * (Math.PI / 2)));
					}
				} else {
					armMain.xRot += Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.15F;
					armOff.xRot += Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.15F;
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
}
