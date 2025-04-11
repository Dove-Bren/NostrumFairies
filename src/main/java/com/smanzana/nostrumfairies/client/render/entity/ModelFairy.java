package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelFairy extends EntityModel<EntityFairy> {

	private ModelPart body;
	private ModelPart wingLeft;
	private ModelPart wingLeftBack;
	private ModelPart wingRight;
	private ModelPart wingRightBack;
	
	public ModelFairy() {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		body = new ModelPart(this, 0, 0);
		body.setTexSize(64, 64);
		body.setPos(0, 24 - 16, 0);
		body.addBox(-8,0,-8, 16, 16, 16);
		
		wingLeft = new ModelPart(this, 0, 0); 
		wingLeft.addBox(0, -2f, -4, 14, 4, 8);
		wingLeft.setPos(6.5f, 5f, -2f);
		
		body.addChild(wingLeft);
		
		wingRight = new ModelPart(this, 0, 0); 
		wingRight.addBox(-14, -2, -4, 14, 4, 8);
		wingRight.setPos(-6.5f, 5f, -2f);
		
		body.addChild(wingRight);
		
		wingLeftBack = new ModelPart(this, 0, 0); 
		wingLeftBack.addBox(0, -2f, -4, 14, 4, 8);
		wingLeftBack.setPos(6.5f, 11f, 2f);
		
		body.addChild(wingLeftBack);
		
		
		wingRightBack = new ModelPart(this, 0, 0); 
		wingRightBack.addBox(-14, -2f, -4, 14, 4, 8);
		wingRightBack.setPos(-6.5f, 11f, 2f);
		
		body.addChild(wingRightBack);
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
