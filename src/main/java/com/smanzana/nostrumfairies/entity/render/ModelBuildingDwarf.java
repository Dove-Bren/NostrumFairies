package com.smanzana.nostrumfairies.entity.render;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBuildingDwarf extends ModelDwarf {

	private ModelRenderer hammer;
	
	public ModelBuildingDwarf(boolean leftHanded) {
		super(leftHanded);
	}
	
	protected ModelRenderer createHammer() {
		
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		final int textW = 128;
		final int textH = 64;
		
		hammer = new ModelRenderer(this);
		hammer.setTextureSize(textW, textH);
		hammer.setRotationPoint(-0.5F, 0.0F, -0.5F);
		// TODO need to shift x and z by -.5?
		hammer.cubeList.add(new ModelBox(hammer, 88, 15, -4.0F, -18.5F, -2.5F, 7, 4, 6, 0.0F, false));
		hammer.cubeList.add(new ModelBox(hammer, 88, 38, -5.0F, -19.0F, -3.0F, 1, 5, 7, 0.0F, false));
		hammer.cubeList.add(new ModelBox(hammer, 88, 25, 3.0F, -19.0F, -3.0F, 1, 5, 7, 0.0F, false));
		hammer.cubeList.add(new ModelBox(hammer, 84, 15, -1.0F, -15.0F, 0.0F, 1, 15, 1, 0.0F, false));

		hammer.offsetY = (9f / 16f); // height of arm, - a bit
		
		hammer.rotateAngleZ = (float) (.5 * Math.PI) - .2f;
		hammer.rotateAngleX = (float) (.5 * Math.PI);
		
		return hammer;
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, headAngleY, headAngleX, scale);
	}
	
	@Override
	protected ModelRenderer createHeldItem(boolean mainhand) {
		if (mainhand) {
			return createHammer();
		}
		
		return null;
	}
	
}
