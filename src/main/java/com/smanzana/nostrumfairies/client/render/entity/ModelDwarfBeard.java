package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelDwarfBeard<T extends EntityDwarf> extends EntityModel<T> {
	
	public static LayerDefinition createFullLayer() {
		MeshDefinition mesh = ModelDwarf.createMesh();
		PartDefinition root = mesh.getRoot();
		
		root.addOrReplaceChild("base",
				CubeListBuilder.create().addBox(-5, -2, -5, 10, 4, 6).addBox(-5, 2, -5, 10, 4, 6),
				PartPose.ZERO
		);
		
		return LayerDefinition.create(mesh, 32, 32);
	}
	
	public static LayerDefinition createLongLayer() {
		MeshDefinition mesh = ModelDwarf.createMesh();
		PartDefinition root = mesh.getRoot();
		
		root.addOrReplaceChild("base",
				CubeListBuilder.create().texOffs(0, 10).addBox(-3, -1, -6, 6, 3, 2).texOffs(16, 10).addBox(-2, 2, -6, 4, 4, 2),
				PartPose.ZERO
		);
		
		return LayerDefinition.create(mesh, 32, 32);
	}
	
	private ModelPart base;
	
	public ModelDwarfBeard(ModelPart root) {
		this.base = root.getChild("base");
	}
	
	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		base.xRot = headPitch * 0.017453292F;
		base.yRot = netHeadYaw * 0.017453292F;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		base.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
}
