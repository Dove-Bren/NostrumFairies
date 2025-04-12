package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelGnomeHat extends EntityModel<EntityGnome> {
	
	public static LayerDefinition createErectLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		root.addOrReplaceChild("base",
				CubeListBuilder.create()
					.addBox(-3.5f, -8, -3.5f, 7, 2, 7)
					.texOffs(28, 0)
					.addBox(-3, -9, -3, 6, 1, 6)
					.texOffs(28, 7)
					.addBox(-2, -10, -2, 4, 1, 4)
					.texOffs(44, 7)
					.addBox(-1, -12, -1, 2, 2, 2)
					.texOffs(44, 11)
					.addBox(-.5f, -15, -.5f, 1, 3, 1),
				PartPose.offset(0, 0, 0)
		);
		
		return LayerDefinition.create(mesh, 128, 128);
	}
	
	public static LayerDefinition createPlainLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		root.addOrReplaceChild("base",
				CubeListBuilder.create()
					.texOffs(0, 18).addBox(-3.5f, -8, -3.5f, 7, 2, 7)
					.texOffs(28, 18).addBox(-3, -9, -3, 6, 1, 6)
					.texOffs(28, 25).addBox(-2, -10, -2, 4, 1, 4)
					.texOffs(44, 7).addBox(-1, -12, -1, 2, 2, 2)
					.texOffs(44, 29).addBox(-.5f, -13, -.5f, 2, 1, 2)
				,
				PartPose.offset(0, 0, 0)
		);
		
		return LayerDefinition.create(mesh, 128, 128);
	}
	
	public static LayerDefinition createLimpLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		root.addOrReplaceChild("base",
				CubeListBuilder.create()
					.texOffs(0, 36).addBox(-3.5f, -8, -3.5f, 7, 2, 7)
					.texOffs(28, 36).addBox(-4, -9, -3, 7, 1, 6)
					
					.texOffs(28, 43).addBox(-6, -10, -2, 6, 1, 6)
					.texOffs(12, 45).addBox(-5, -11, -1, 4, 1, 4)
					
					.texOffs(0, 45).addBox(-7, -9, 2, 2, 2, 3)
					
					.texOffs(52, 48).addBox(-7, -7, 3, 1, 1, 1)
				,
				PartPose.offset(0, 0, 0)
		);
		
		return LayerDefinition.create(mesh, 128, 128);
	}
	
	public static LayerDefinition createSmallLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		
		root.addOrReplaceChild("base",
				CubeListBuilder.create()
					.texOffs(0, 52).addBox(-1, -8, -1, 4, 1, 4)
					.texOffs(16, 52).addBox(-.5f, -9, -.5f, 3, 1, 3)
					.texOffs(28, 52).addBox(0, -11, 0, 2, 2, 2)
					.texOffs(36, 52).addBox(.5f, -13, .5f, 1, 2, 1)
				,
				PartPose.offset(0, 0, 0)
		);
		
		return LayerDefinition.create(mesh, 128, 128);
	}
	
	private ModelPart base;
	
	public ModelGnomeHat(ModelPart root) {
		this.base = root.getChild("base");
	}
	
	public void setupAnim(EntityGnome entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		base.xRot = headPitch * 0.017453292F;
		base.yRot = netHeadYaw * 0.017453292F;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		base.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
}
