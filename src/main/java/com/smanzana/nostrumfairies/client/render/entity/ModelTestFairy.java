package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelTestFairy extends EntityModel<EntityTestFairy> {

	private ModelRenderer main;
	
	public ModelTestFairy() {
		main = new ModelRenderer(this, 0, 0);
		
		main.setTextureSize(32, 32);
		//main.setRotationPoint(0, 20, 0);
//		main.addBox(-10, 12, -10, 20, 20, 20);
		main.addBox(-5,14,-5,10,10,10);
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
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
	public void setRotationAngles(EntityTestFairy entityIn, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		// TODO Auto-generated method stub
		
	}
	
}
