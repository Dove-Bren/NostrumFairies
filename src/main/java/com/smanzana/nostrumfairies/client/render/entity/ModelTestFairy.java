package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.item.ItemStack;

public class ModelTestFairy extends EntityModel<EntityTestFairy> {

	private RendererModel main;
	
	public ModelTestFairy() {
		main = new RendererModel(this, 0, 0);
		
		main.setTextureSize(32, 32);
		//main.setRotationPoint(0, 20, 0);
//		main.addBox(-10, 12, -10, 20, 20, 20);
		main.addBox(-5,14,-5,10,10,10);
	}
	
	@Override
	public void render(EntityTestFairy entity, float time, float swingProgress,
			float swing, float headAngleY, float headAngleX, float scale) {
		setRotationAngles(entity, time, swingProgress, swing, headAngleY, headAngleX, scale);
		main.render(scale);
		
		ItemStack stack = entity.getCarriedItems().get(0);
		if (!stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0, 2, 0);
			RenderFuncs.ItemRenderer(stack);
			GlStateManager.popMatrix();
		}
	}
	
}
