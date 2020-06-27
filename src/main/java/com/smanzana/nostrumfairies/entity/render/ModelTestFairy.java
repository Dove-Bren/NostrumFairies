package com.smanzana.nostrumfairies.entity.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTestFairy extends ModelBase {

	private ModelRenderer main;
	
	public ModelTestFairy() {
		main = new ModelRenderer(this, 0, 0);
		
		main.setTextureSize(32, 32);
		//main.setRotationPoint(0, 20, 0);
//		main.addBox(-10, 12, -10, 20, 20, 20);
		main.addBox(-5,14,-5,10,10,10);
	}
	
	@Override
	public void render(Entity entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		setRotationAngles(time, swingProgress, swing, headAngleY, headAngleX, scale, entity);
		main.render(scale);
	}
	
}
