package com.smanzana.nostrumfairies.client.render.entity;

import com.smanzana.nostrumfairies.entity.fey.EntityFairy;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelFairy extends EntityModel<EntityFairy> {

	private RendererModel body;
	private RendererModel wingLeft;
	private RendererModel wingLeftBack;
	private RendererModel wingRight;
	private RendererModel wingRightBack;
	
	public ModelFairy() {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		body = new RendererModel(this, 0, 0);
		body.setTextureSize(64, 64);
		body.setRotationPoint(0, 24 - 16, 0);
		body.addBox(-8,0,-8, 16, 16, 16);
		
		wingLeft = new RendererModel(this, 0, 0); 
		wingLeft.addBox(0, -2f, -4, 14, 4, 8);
		wingLeft.setRotationPoint(6.5f, 5f, -2f);
		
		body.addChild(wingLeft);
		
		wingRight = new RendererModel(this, 0, 0); 
		wingRight.addBox(-14, -2, -4, 14, 4, 8);
		wingRight.setRotationPoint(-6.5f, 5f, -2f);
		
		body.addChild(wingRight);
		
		wingLeftBack = new RendererModel(this, 0, 0); 
		wingLeftBack.addBox(0, -2f, -4, 14, 4, 8);
		wingLeftBack.setRotationPoint(6.5f, 11f, 2f);
		
		body.addChild(wingLeftBack);
		
		
		wingRightBack = new RendererModel(this, 0, 0); 
		wingRightBack.addBox(-14, -2f, -4, 14, 4, 8);
		wingRightBack.setRotationPoint(-6.5f, 11f, 2f);
		
		body.addChild(wingRightBack);
	}
	
	@Override
	public void render(EntityFairy entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		
		final float period = (20f * .15f); //.15f
		float progress = (ageInTicks % period) / period;
		
		float angle = (float) (Math.sin(progress * Math.PI * 2) * (Math.PI / 4));
		wingLeft.rotateAngleZ = angle;
		wingLeftBack.rotateAngleZ = angle;
		wingRight.rotateAngleZ = -angle;
		wingRightBack.rotateAngleZ = -angle;
		
		body.render(scale);
	}
	
}
