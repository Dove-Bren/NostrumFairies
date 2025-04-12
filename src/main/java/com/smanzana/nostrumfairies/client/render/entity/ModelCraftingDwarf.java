package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfCrafter;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelCraftingDwarf extends ModelDwarf<EntityDwarfCrafter> {

	public static LayerDefinition createLayer(boolean leftHanded) {
		MeshDefinition mesh = ModelDwarf.createMesh();
		PartDefinition root = mesh.getRoot();
		
		final String handName;
		if (leftHanded) {
			handName = "armLeft";
		} else {
			handName = "armRight";
		}
		
		root.getChild("body").getChild(handName).addOrReplaceChild("hammer",
				createHammer(),
				PartPose.offsetAndRotation(-0.5f, 9f/16f, -.5f, (float) (.5 * Math.PI), 0f, (float) (.5 * Math.PI) - .2f)
		);
		
//		hammer.setPos(-0.5F, 0.0F, -0.5F);
//		hammer.offsetY = (9f / 16f); // height of arm, - a bit
//		
//		hammer.zRot = (float) (.5 * Math.PI) - .2f;
//		hammer.xRot = (float) (.5 * Math.PI);
		
		return LayerDefinition.create(mesh, 128, 64);
	}
	
	public ModelCraftingDwarf(ModelPart root) {
		super(root);
	}
	
	protected static CubeListBuilder createHammer() {
		return CubeListBuilder.create()
				.texOffs(88, 15).addBox(-4.0F, -18.5F, -2.5F, 7, 4, 6)
				.texOffs(88, 38).addBox(-5.0F, -19.0F, -3.0F, 1, 5, 7)
				.texOffs(88, 25).addBox(3.0F, -19.0F, -3.0F, 1, 5, 7)
				.texOffs(84, 15).addBox(-1.0F, -15.0F, 0.0F, 1, 15, 1)
				;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		super.renderToBuffer(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
}
