package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.serializers.ArmPoseDwarf;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public abstract class ModelDwarf<T extends EntityDwarf> extends EntityModel<T> {
	
	protected static LayerDefinition createLayer() {
		return LayerDefinition.create(createMesh(), 128, 64);
	}
	
	protected static MeshDefinition createMesh() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		PartDefinition body =
		root.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 0).addBox(-7, -9, -4, 14, 18, 8),
				PartPose.offset(0, 11, 0)
		);
		
		body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(44, 0).addBox(-4, -8, -4, 8, 8, 8)
					.texOffs(44, 16).addBox(4, -6, -1, 1, 2, 1)
					.texOffs(44, 16).addBox(-5, -6, -1, 1, 2, 1),
				PartPose.offset(0, -9f, 0)
		);
		//head.offsetY = (-9f / 16f);
		
		body.addOrReplaceChild("legLeft",
				CubeListBuilder.create().texOffs(0, 41).addBox(-2.5f, 0, -3, 5, 8, 6),
				PartPose.offset(3f, 5f, 0)
		);
		//legLeft.offsetY = (5f / 16f);
		//legLeft.offsetX = (3f / 16f);
		
		body.addOrReplaceChild("legRight",
				CubeListBuilder.create().texOffs(0, 41).addBox(-2.5f, 0, -3, 5, 8, 6, true),
				PartPose.offset(-3f, 5f, 0)
		);
//		legRight.offsetY = (5f / 16f);
//		legRight.offsetX = (-3f / 16f);
		
		body.addOrReplaceChild("armLeft",
				CubeListBuilder.create().texOffs(0, 26).addBox(-2.5f, 0, -2.5f, 5, 10, 5),
				PartPose.offset(7 + 2.5f, -7f, 0)
		);
//		armLeft.offsetY = (-7f / 16f);
//		armLeft.offsetX = ((7 + 2.5f) / 16f);
		
		body.addOrReplaceChild("armRight",
				CubeListBuilder.create().texOffs(0, 26).addBox(-2.5f, 0, -2.5f, 5, 10, 5, true),
				PartPose.offset(-(7 + 2.5f), -7f, 0)
		);
//		armRight.offsetY = (-7f / 16f);
//		armRight.offsetX = (-(7 + 2.5f) / 16f);
		
		return mesh;
	}

	private ModelPart body;
	private ModelPart head;
	private ModelPart legLeft;
	private ModelPart legRight;
	private ModelPart armLeft;
	private ModelPart armRight;
	
//	private OffsetModelRenderer heldMain;
//	private OffsetModelRenderer heldOff;
	
	//private ModelRenderer pick;
	
	public ModelDwarf(ModelPart root) {
		body = root.getChild("body");
		head = body.getChild("head");
		legLeft = body.getChild("legLeft");
		legRight = body.getChild("legRight");
		armLeft = body.getChild("armLeft");
		armRight = body.getChild("armRight");
		
//		heldMain = createHeldItem(true);
//		heldOff = createHeldItem(false);
//		
//		if (leftHanded) {
//			if (heldMain != null) {
//				armLeft.addChild(heldMain);
//			}
//			if (heldOff != null) {
//				armRight.addChild(heldOff);
//			}
//		} else {
//			if (heldMain != null) {
//				armRight.addChild(heldMain);
//			}
//			if (heldOff != null) {
//				armLeft.addChild(heldOff);
//			}
//		}
		
	}
	
	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		EntityDwarf dwarf = (EntityDwarf) entityIn;
//		final float period = (20f * .15f); //.15f
//		float progress = (ageInTicks % period) / period;
//		
//		float angle = (float) (Math.sin(progress * Math.PI * 2) * (Math.PI / 4));
//		wingLeft.rotateAngleZ = angle;
//		wingLeftBack.rotateAngleZ = angle;
//		wingRight.rotateAngleZ = -angle;
//		wingRightBack.rotateAngleZ = -angle;
		
		head.xRot = headPitch * 0.017453292F;
		head.yRot = netHeadYaw * 0.017453292F;
		
		// dwarves move their small legs and arms fast
		limbSwing *= 2;
		
		armRight.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		armRight.zRot = 0;
		armLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		armLeft.zRot = 0;
		
		legRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		legLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		
		if (dwarf.swinging || dwarf.getDwarfPose() != ArmPoseDwarf.IDLE) {
			int sign = 1;//(dwarf.isLeftHanded() ? -1 : 1);
			ModelPart hand = (dwarf.isLeftHanded() ? armLeft : armRight);
			
			if (dwarf.getDwarfPose() == ArmPoseDwarf.MINING) {
				double lowX = -sign * (Math.PI * .75);
				double diffX = sign * (Math.PI * .4);
				float periodFirst = .4f;
				if (this.attackTime < periodFirst) {
					float progress = (attackTime / periodFirst);
					hand.zRot = 0;
					hand.yRot = 0;
					hand.xRot = (float) (lowX + (diffX * Math.sin(Math.PI * progress)));
				} else {
					// Waiting for the next strike
					hand.zRot = 0;
					hand.xRot = (float) lowX;
					hand.yRot = 0;
				}
			} else if (dwarf.getDwarfPose() == ArmPoseDwarf.ATTACKING) {
				// Have pick raised and do full swings
				double lowX = -sign * (Math.PI * .95);
				double diffX = sign * (Math.PI * .8);
				float periodFirst = .3f;
				if (this.attackTime < periodFirst) {
					float progress = (attackTime / periodFirst);
					hand.zRot = 0;
					hand.yRot = 0;
					hand.xRot = (float) (lowX + (diffX * Math.sin(Math.PI * progress)));
				} else {
					// Waiting for the next strike
					hand.zRot = 0;
					hand.xRot = (float) lowX;
					hand.yRot = 0;
				}
			} else {
				final double peakX = -sign * (Math.PI * 1.15);
				float periodFirst = .2f;
				float periodSecond = .1f;
				float periodThird = 1 - (periodFirst + periodSecond);
				if (this.attackTime < periodFirst) {
					// first part. Wind up!
					// from (0, 0, 0) to (-(PI-peakX), pi, pi)
					float progress = (attackTime / periodFirst);
					hand.zRot = 0;
					hand.yRot = 0;
					hand.xRot = (float) (peakX * Math.sin(.5 * Math.PI * progress));
				}
				else if (this.attackTime < (periodFirst + periodSecond)) {
	//				// stall and build anticipation
					hand.zRot = 0;//(float) (sign * Math.PI);
					hand.xRot = (float) peakX;
					hand.yRot = 0;
				}
				else {
					// swing
					float progress = (attackTime - (periodFirst + periodSecond)) / periodThird;
					hand.zRot = 0;
					hand.yRot = 0;
					hand.xRot = (float) (peakX * Math.sin((Math.PI * .5) + (.5 * Math.PI * progress)));
					
				}
			}
		}
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		body.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
}
