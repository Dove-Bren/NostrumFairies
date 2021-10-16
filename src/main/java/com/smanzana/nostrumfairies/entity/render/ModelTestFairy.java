package com.smanzana.nostrumfairies.entity.render;

import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

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
		
		ItemStack stack = ((EntityTestFairy) entity).getCarriedItems().get(0);
		if (!stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 2, 0);
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, TransformType.GROUND);
			GlStateManager.popMatrix();
		}
	}
	
}
