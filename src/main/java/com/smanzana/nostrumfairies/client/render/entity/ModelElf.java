package com.smanzana.nostrumfairies.client.render.entity;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.BattleStanceShadowFey;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ModelElf<T extends Entity> extends EntityModel<T> {

	protected OffsetModelRenderer body;
	protected OffsetModelRenderer head;
	protected OffsetModelRenderer legLeft;
	protected OffsetModelRenderer legRight;
	protected OffsetModelRenderer armLeft;
	protected OffsetModelRenderer armRight;
	protected @Nullable OffsetModelRenderer heldMain;
	protected @Nullable OffsetModelRenderer heldOff;
	
	private static final int ELF_TEX_W = 64;
	private static final int ELF_TEX_H = 32;
	
	public ModelElf(boolean leftHanded) {
		this(leftHanded, RenderType::entityCutoutNoCull);
	}
	
	public ModelElf(boolean leftHanded, Function<ResourceLocation, RenderType> renderTypeMap) {
		super(renderTypeMap);
		
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		body = new OffsetModelRenderer(this, 0, 0);
		body.setTexSize(ELF_TEX_W, ELF_TEX_H);
		body.setPos(0, 7, 0);
		body.addBox(-4, -7, -2, 8, 14, 4);
		
		head = new OffsetModelRenderer(this, 24, 0);
		head.setTexSize(ELF_TEX_W, ELF_TEX_H);
		head.setPos(0, 0, 0);
		head.addBox(-3, -6, -3, 6, 6, 6);
		// left ear
		head.texOffs(24, 12);
		head.addBox(3, -8, 0, 2, 4, 1);
		head.texOffs(30, 12);
		head.addBox(3, -7, -1, 1, 4, 1);
		
		// right ear
		head.texOffs(24, 17);
		head.addBox(-5, -8, 0, 2, 4, 1);
		head.texOffs(30, 17);
		head.addBox(-4, -7, -1, 1, 4, 1);
		
		head.offsetY = (-7f / 16f);
		body.addChild(head);
		
		legLeft = new OffsetModelRenderer(this, 0, 18);
		legLeft.setTexSize(ELF_TEX_W, ELF_TEX_H);
		legLeft.setPos(0, 0, 0);
		legLeft.addBox(-2, 0, -2, 3, 10, 4);
		legLeft.offsetY = (7f / 16f);
		legLeft.offsetX = (3f / 16f);
		body.addChild(legLeft);

		legRight = new OffsetModelRenderer(this, 0, 18);
		legRight.mirror = true;
		legRight.setTexSize(ELF_TEX_W, ELF_TEX_H);
		legRight.setPos(0, 0, 0);
		legRight.addBox(-2, 0, -2, 3, 10, 4);
		legRight.offsetY = (7f / 16f);
		legRight.offsetX = (-2f / 16f);
		body.addChild(legRight);
		
		armLeft = new OffsetModelRenderer(this, 48, 0);
		armLeft.setTexSize(ELF_TEX_W, ELF_TEX_H);
		armLeft.setPos(0, 1, 0);
		armLeft.addBox(-1.5f, -1, -1.5f, 3, 12, 3);
		armLeft.offsetY = (-7f / 16f);
		armLeft.offsetX = ((4 + 1.5f) / 16f);
		body.addChild(armLeft);
		
		armRight = new OffsetModelRenderer(this, 48, 0);
		armRight.mirror = true;
		armRight.setTexSize(ELF_TEX_W, ELF_TEX_H);
		armRight.setPos(0, 1, 0);
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
	
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headAngleY, float headAngleX) {
		final boolean isWorking;
		final boolean isIdle;
		final boolean leftHanded;
		
		if (entity instanceof EntityElf) {
			EntityElf elf = (EntityElf) entity;
			isWorking = elf.getElfPose() == ArmPoseElf.WORKING;
			isIdle = elf.getElfPose() == ArmPoseElf.IDLE;
			leftHanded = elf.isLeftHanded();
		} else if (entity instanceof EntityShadowFey) {
			EntityShadowFey shadow = (EntityShadowFey) entity;
			isWorking = false;
			isIdle = shadow.getStance() == BattleStanceShadowFey.IDLE;
			leftHanded = shadow.isLeftHanded();
		} else {
			isWorking = false;
			isIdle = false;
			leftHanded = false; 
		}
		
		body.yRot = 0;
		body.xRot = 0;
		body.offsetY = 0;
		head.xRot = headAngleX * 0.017453292F;
		head.yRot = headAngleY * 0.017453292F;
		
		// Artificially adjust head pitch when working to look up at tree
		if (isWorking) {
			head.xRot -= (float) (Math.PI * .25);
		}
		
		limbSwing *= 2;
		
		armRight.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		armRight.zRot = 0;
		armRight.yRot = 0;
		armRight.offsetY = (-7f / 16f);
		armRight.offsetX = (-(4 + 1.5f) / 16f);
		armRight.offsetZ = 0;
		armLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		armLeft.zRot = 0;
		armLeft.yRot = 0;
		armLeft.offsetY = (-7f / 16f);
		armLeft.offsetX = ((4 + 1.5f) / 16f);
		armLeft.offsetZ = 0;
		
		legRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		legRight.offsetY = (7f / 16f);
		legRight.offsetX = (-2f / 16f);
		legRight.offsetZ = 0;
		legLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		legLeft.offsetY = (7f / 16f);
		legLeft.offsetX = (2f / 16f);
		legLeft.offsetZ = 0;
		
		//if (elf.isSwingInProgress || elf.getPose() != ArmPose.IDLE) {
		if (attackTime > 0 || !isIdle) {
			if (heldMain != null) {
				heldMain.xRot = (float) (.9 * Math.PI);
				//heldMain.rotateAngleY = 0;
			}
			
			ModelPart hand = (leftHanded ? armLeft : armRight);
			
			//if (elf.getPose() == ArmPose.CHOPPING)
			{
				double range = .025;
				hand.zRot = 0;
				hand.xRot = (float) (-(Math.PI * .75) - (Math.PI * range * Math.sin(attackTime * 2 * Math.PI)));
				hand.yRot = (float) -(Math.PI * range * Math.sin(attackTime * 2 * Math.PI));
				
			}
		} else {
			if (heldMain != null) {
				heldMain.xRot = (float) (.5 * Math.PI);
				heldMain.zRot = 0; 
				heldMain.yRot = 0;
			}
		}
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		body.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	protected OffsetModelRenderer getInHand(boolean mainHand) {
		OffsetModelRenderer render = null;
		if (mainHand) {
			render = new OffsetModelRenderer(this, 48, 25);
			render.setTexSize(ELF_TEX_W, ELF_TEX_H);
			render.setPos(0, 0, 0);
			render.addBox(-.5f, -6, -.5f, 1, 6, 1);
			render.texOffs(44, 28);
			render.addBox(-1.5f, -4, -.5f, 1, 1, 1);
			render.texOffs(52, 27);
			render.addBox(0.5f, -8, 0, 1, 4, 1);
			render.texOffs(52, 25);
			render.addBox(1.5f, -8, 0, 1, 1, 1);
			render.texOffs(60, 29);
			render.addBox(-.5f, -9, 0, 1, 2, 1);
			render.texOffs(56, 28);
			render.addBox(-1.5f, -11, -.5f, 1, 3, 1);
			
			render.offsetY = (10f / 16f); // height of arm, - a bit
		}
		
		return render;
	}
	
}
