package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelFairy extends EntityModel<EntityFairy> {
	
	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		PartDefinition body =
		root.addOrReplaceChild("body",
				CubeListBuilder.create().addBox(-8,0,-8, 16, 16, 16),
				PartPose.offset(0, 24 - 16, 0)
		);
		//body.setPos(0, 24 - 16, 0);
		
		body.addOrReplaceChild("wingLeft",
				CubeListBuilder.create().addBox(0, -2f, -4, 14, 4, 8),
				PartPose.offset(6.5f, 5f, -2f)
		);
		//wingLeft.setPos(6.5f, 5f, -2f);
		
		body.addOrReplaceChild("wingRight",
				CubeListBuilder.create().addBox(-14, -2, -4, 14, 4, 8),
				PartPose.offset(-6.5f, 5f, -2f)
		);
		//wingRight.setPos(-6.5f, 5f, -2f);
		
		body.addOrReplaceChild("wingLeftBack",
				CubeListBuilder.create().addBox(0, -2f, -4, 14, 4, 8),
				PartPose.offset(6.5f, 11f, 2f)
		);
		//wingLeftBack.setPos(6.5f, 11f, 2f);
		
		body.addOrReplaceChild("wingRightBack",
				CubeListBuilder.create().addBox(-14, -2f, -4, 14, 4, 8),
				PartPose.offset(-6.5f, 11f, 2f)
		);
		//wingRightBack.setPos(-6.5f, 11f, 2f);
		
		return LayerDefinition.create(mesh, 64, 64);
	}

	private final ModelPart body;
	private final ModelPart wingLeft;
	private final ModelPart wingLeftBack;
	private final ModelPart wingRight;
	private final ModelPart wingRightBack;
	
	public ModelFairy(ModelPart root) {
		this.body = root.getChild("body");
		
		this.wingLeft = body.getChild("wingLeft");
		this.wingLeftBack = body.getChild("wingLeftBack");
		this.wingRight = body.getChild("wingRight");
		this.wingRightBack = body.getChild("wingRightBack");
	}
	
	@Override
	public void setupAnim(EntityFairy entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		final float period = (20f * .15f); //.15f
		float progress = (ageInTicks % period) / period;
		
		float angle = (float) (Math.sin(progress * Math.PI * 2) * (Math.PI / 4));
		wingLeft.zRot = angle;
		wingLeftBack.zRot = angle;
		wingRight.zRot = -angle;
		wingRightBack.zRot = -angle;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		body.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
}
