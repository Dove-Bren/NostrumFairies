package com.smanzana.nostrumfairies.client.render.entity;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.serializers.BattleStanceShadowFey;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public abstract class ModelElf<T extends Entity> extends EntityModel<T> {
	
	protected static MeshDefinition createMesh(boolean leftHanded) {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		PartDefinition body =
		root.addOrReplaceChild("body",
				CubeListBuilder.create().addBox(-4, -7, -2, 8, 14, 4),
				PartPose.offset(0, 7, 0)
		);
		
		body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(24, 0).addBox(-3, -6, -3, 6, 6, 6)
					// left ear
					.texOffs(24, 12).addBox(3, -8, 0, 2, 4, 1).texOffs(30, 12).addBox(3, -7, -1, 1, 4, 1)
					// right ear
					.texOffs(24, 17).addBox(-5, -8, 0, 2, 4, 1).texOffs(30, 17).addBox(-4, -7, -1, 1, 4, 1)
					,
				PartPose.offset(0, (-7f / 16f), 0)
		);
		//head.offsetY = (-7f / 16f);
		
		body.addOrReplaceChild("legLeft",
				CubeListBuilder.create().texOffs(0, 18).addBox(-2, 0, -2, 3, 10, 4),
				PartPose.offset((2f / 16f), (7f / 16f), 0)
		);
//		legLeft.offsetY = (7f / 16f);
//		legLeft.offsetX = (2f / 16f); originally 3f/16f but render moved it
		
		body.addOrReplaceChild("legRight",
				CubeListBuilder.create().texOffs(0, 18).addBox(-2, 0, -2, 3, 10, 4, true),
				PartPose.offset((-2f / 16f), (7f / 16f), 0)
		);
//		legRight.offsetY = (7f / 16f);
//		legRight.offsetX = (-2f / 16f);
		
		body.addOrReplaceChild(leftHanded ? "armMain" : "armOff",
				CubeListBuilder.create().texOffs(48, 0).addBox(-1.5f, -1, -1.5f, 3, 12, 3),
				PartPose.offset(((4 + 1.5f) / 16f), (-7f / 16f) + 1, 0)
		);
//		armLeft.setPos(0, 1, 0);
//		armLeft.offsetY = (-7f / 16f);
//		armLeft.offsetX = ((4 + 1.5f) / 16f);
		
		body.addOrReplaceChild(leftHanded ? "armOff" : "armMain",
				CubeListBuilder.create().texOffs(48, 0).addBox(-1.5f, -1, -1.5f, 3, 12, 3, true),
				PartPose.offset((-(4 + 1.5f) / 16f), (-7f / 16f) + 1, 0)
		);
//		armRight.setPos(0, 1, 0);
//		armRight.offsetY = (-7f / 16f);
//		armRight.offsetX = (-(4 + 1.5f) / 16f);
		
		return mesh;
	}

	protected final ModelPart body;
	protected final ModelPart head;
	protected final ModelPart legLeft;
	protected final ModelPart legRight;
	protected final ModelPart armMain;
	protected final ModelPart armOff;
	
	public ModelElf(ModelPart root) {
		this(root, RenderType::entityCutoutNoCull);
	}
	
	public ModelElf(ModelPart root, Function<ResourceLocation, RenderType> renderTypeMap) {
		super(renderTypeMap);
		
		body = root.getChild("body");
		head = body.getChild("head");
		legLeft = body.getChild("legLeft");
		legRight = body.getChild("legRight");
		armOff = body.getChild("armOff");
		armMain = body.getChild("armMain");
	}
	
	protected abstract @Nullable ModelPart getHeldMainHand();
	
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headAngleY, float headAngleX) {
		final boolean isWorking;
		final boolean isIdle;
		
		if (entity instanceof EntityElf) {
			EntityElf elf = (EntityElf) entity;
			isWorking = elf.getElfPose() == ArmPoseElf.WORKING;
			isIdle = elf.getElfPose() == ArmPoseElf.IDLE;
		} else if (entity instanceof EntityShadowFey) {
			EntityShadowFey shadow = (EntityShadowFey) entity;
			isWorking = false;
			isIdle = shadow.getStance() == BattleStanceShadowFey.IDLE;
		} else {
			isWorking = false;
			isIdle = false;
		}
		
		body.yRot = 0;
		body.xRot = 0;
		head.xRot = headAngleX * 0.017453292F;
		head.yRot = headAngleY * 0.017453292F;
		
		// Artificially adjust head pitch when working to look up at tree
		if (isWorking) {
			head.xRot -= (float) (Math.PI * .25);
		}
		
		limbSwing *= 2;
		
		armMain.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		armMain.zRot = 0;
		armMain.yRot = 0;
		armOff.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		armOff.zRot = 0;
		armOff.yRot = 0;
		
		
		legRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		legLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		
		//if (elf.isSwingInProgress || elf.getPose() != ArmPose.IDLE) {
		final ModelPart heldMain = this.getHeldMainHand();
		if (attackTime > 0 || !isIdle) {
			if (heldMain != null) {
				heldMain.xRot = (float) (.9 * Math.PI);
				//heldMain.rotateAngleY = 0;
			}
			
			//if (elf.getPose() == ArmPose.CHOPPING)
			{
				double range = .025;
				armMain.zRot = 0;
				armMain.xRot = (float) (-(Math.PI * .75) - (Math.PI * range * Math.sin(attackTime * 2 * Math.PI)));
				armMain.yRot = (float) -(Math.PI * range * Math.sin(attackTime * 2 * Math.PI));
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
}
