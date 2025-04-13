package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.serializers.ArmPoseGnome;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ModelGnome extends EntityModel<EntityGnome> {
	
	public static final LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		PartDefinition body =
		root.addOrReplaceChild("body",
				CubeListBuilder.create().addBox(-4, -5, -3, 8, 10, 6),
				PartPose.offset(0, 13, 0)
		);
		//body.setPos(0, 13, 0);
		
		body.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(28, 0).addBox(-3.5f, -7, -3.5f, 7, 7, 7)
					.texOffs(28, 14).addBox(3, -5, -1, 1, 2, 1)
					.texOffs(28, 17).addBox(-4, -5, -1, 1, 2, 1),
				PartPose.offset(0, -5f, 0)
		);
		//head.offsetY = (-5f / 16f);
		
		body.addOrReplaceChild("legLeft",
				CubeListBuilder.create().texOffs(0, 16).addBox(-1.5f, 0, -2, 3, 6, 4)
					.texOffs(14, 27).addBox(-1.5f, 4, -5, 3, 2, 3)
					.texOffs(4, 28).addBox(-1.5f, 3, -6, 3, 2, 2),
				PartPose.offset(2.49f, 5, 0)
		);
//		legLeft.offsetY = (5f / 16f);
//		legLeft.offsetX = (2.49f / 16f);
		
		body.addOrReplaceChild("legRight",
				CubeListBuilder.create().mirror().texOffs(0, 16).addBox(-1.5f, 0, -2, 3, 6, 4)
					.texOffs(14, 27).addBox(-1.5f, 4, -5, 3, 2, 3)
					.texOffs(4, 28).addBox(-1.5f, 3, -6, 3, 2, 2)
					,
				PartPose.offset(-2.49f, 5, 0)
		);
//		legRight.offsetY = (5f / 16f);
//		legRight.offsetX = (-2.49f / 16f);
		
		body.addOrReplaceChild("armLeft",
				CubeListBuilder.create().texOffs(48, 16).addBox(-1.5f, 0, -1.5f, 3, 7, 3),
				PartPose.offset((3 + 1.5f), -5, 0)
		);
//		armLeft.offsetY = (-5f / 16f);
//		armLeft.offsetX = ((3 + 1.5f) / 16f);
		
		body.addOrReplaceChild("armRight",
				CubeListBuilder.create().mirror().texOffs(48, 16).addBox(-1.5f, 0, -1.5f, 3, 7, 3),
				PartPose.offset(-(3 + 1.5f), -5, 0)
		);
//		armRight.offsetY = (-5f / 16f);
//		armRight.offsetX = (-(3 + 1.5f) / 16f);
		
		return LayerDefinition.create(mesh, 64, 32);
	}

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart legLeft;
	private final ModelPart legRight;
	private final ModelPart armLeft;
	private final ModelPart armRight;
	
	public ModelGnome(ModelPart root) {
		this.body = root.getChild("body");
		this.head = body.getChild("head");
		this.legLeft = body.getChild("legLeft");
		this.legRight = body.getChild("legRight");
		this.armLeft = body.getChild("armLeft");
		this.armRight = body.getChild("armRight");
	}
	
	@Override
	public void setupAnim(EntityGnome gnome, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		
		head.xRot = headPitch * 0.017453292F;
		head.yRot = netHeadYaw * 0.017453292F;
		
		// gnomes move their small legs and arms fast
		limbSwing *= 2;
		
		armRight.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F;
		armRight.yRot = (float) -(Math.PI * .05);
		armRight.zRot = 0;
		armLeft.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		armLeft.yRot = (float) (Math.PI * .05);
		armLeft.zRot = 0;
		
		legRight.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		legLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
		
		body.y = 13;
		body.xRot = 0;
		legLeft.y = 5f;
		legLeft.z = 0;
		legRight.y = 5f;
		legRight.z = 0;
		
		if (gnome.swinging || gnome.getGnomePose() != ArmPoseGnome.IDLE) {
			
			// Either squatting down and trying to pick something up, or carrying something.
			if (gnome.getGnomePose() == ArmPoseGnome.WORKING || gnome.swinging) {
				float bend = (float) (Math.sin(attackTime * Math.PI) * (Math.PI * .1));
				float offsetY = (float) (Math.sin(attackTime * Math.PI));
				body.y += offsetY;
				body.xRot = bend;
				legLeft.y -= offsetY;
				legLeft.z -= offsetY;
				legLeft.xRot = -bend;
				legRight.y -= offsetY;
				legRight.z -= offsetY;
				legRight.xRot = -bend;
				
				armRight.xRot = (float) -(Math.PI * .3);
				armLeft.xRot = (float) -(Math.PI * .3);
				armRight.yRot = 0f;
				armLeft.yRot = 0f;
			} else if (gnome.getGnomePose() == ArmPoseGnome.CARRYING) {
				armRight.xRot = (float) -(Math.PI * .5);
				armRight.yRot = (float) -(Math.PI * .1);
				armLeft.xRot = (float) -(Math.PI * .5);
				armLeft.yRot = (float) (Math.PI * .1);
			}
		}
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		body.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
}
