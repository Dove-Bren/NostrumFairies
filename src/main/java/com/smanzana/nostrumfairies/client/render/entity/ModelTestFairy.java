package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelTestFairy extends EntityModel<EntityTestFairy> {

	private ModelPart main;
	
	public ModelTestFairy() {
		main = new ModelPart(this, 0, 0);
		
		main.setTexSize(32, 32);
		//main.setRotationPoint(0, 20, 0);
//		main.addBox(-10, 12, -10, 20, 20, 20);
		main.addBox(-5,14,-5,10,10,10);
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		main.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		
//		ItemStack stack = entity.getCarriedItems().get(0);
//		if (!stack.isEmpty()) {
//			matrixStackIn.push();
//			matrixStackIn.translate(0, 2, 0);
//			RenderFuncs.ItemRenderer(stack);
//			matrixStackIn.pop();
//		}
	}

	@Override
	public void setupAnim(EntityTestFairy entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
	
}
