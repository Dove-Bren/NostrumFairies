package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelMiningDwarf extends ModelDwarf<EntityDwarf> {

	public static LayerDefinition createLayer(boolean leftHanded) {
		MeshDefinition mesh = ModelDwarf.createMesh();
		PartDefinition root = mesh.getRoot();
		
		root.getChild("body").getChild(leftHanded ? "armLeft" : "armRight").addOrReplaceChild("pickaxe",
				createPickaxe(),
				PartPose.offsetAndRotation(-.5f, 9f, -.5f, (float) (.5 * Math.PI), 0f, (float) (.5 * Math.PI) - .2f)
		);
		
//		pick.offsetY = (9f / 16f); // height of arm, - a bit
		//pick.setPos(-0.5f, 0, -0.5f);
//		
//		pick.zRot = (float) (.5 * Math.PI) - .2f;
//		pick.xRot = (float) (.5 * Math.PI);
		
		return LayerDefinition.create(mesh, 128, 64);
	}

	public ModelMiningDwarf(ModelPart root) {
		super(root);
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		super.renderToBuffer(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	protected static CubeListBuilder createPickaxe() {
		return CubeListBuilder.create()
				.texOffs(84, 0).addBox(0, -14, 0, 1, 14, 1)
				.texOffs(99, 5)
				.addBox(-1, -15, -.5f, 3, 1, 2)
				.texOffs(95, 2)
				.addBox(-3, -16, -.5f, 7, 1, 2)
				.texOffs(98, 0)
				.addBox(-2, -17, 0, 5, 1, 1)
				.texOffs(91, 5)
				.addBox(3, -15, 0, 2, 1, 1)
				.texOffs(111, 5)
				.addBox(-4, -15, 0, 2, 1, 1)
		;
	}
	
}
