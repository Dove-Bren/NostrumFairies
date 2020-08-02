package com.smanzana.nostrumfairies.entity.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelGnomeHat extends ModelBase {
	
	public static enum Type {
		ERECT,
		PLAIN,
		LIMP,
		SMALL
	}
	
	protected final Type type;

	private ModelRenderer base;
	
	public ModelGnomeHat(Type type) {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		int textW = 128;
		int textH = 128;
		// TODO some y offset
		
		this.type = type;
		switch (type) {
		case ERECT:
		default:
			base = new ModelRenderer(this, 0, 0);
			base.setTextureSize(textW, textH);
			base.addBox(-3.5f, -8, -3.5f, 7, 2, 7);
			base.setTextureOffset(28, 0);
			base.addBox(-3, -9, -3, 6, 1, 6);
			base.setTextureOffset(28, 7);
			base.addBox(-2, -10, -2, 4, 1, 4);
			base.setTextureOffset(44, 7);
			base.addBox(-1, -12, -1, 2, 2, 2);
			base.setTextureOffset(44, 11);
			base.addBox(-.5f, -15, -.5f, 1, 3, 1);
			break;
		}
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		base.rotateAngleX = headAngleX * 0.017453292F;
		base.rotateAngleY = headAngleY * 0.017453292F;
		
		base.render(scale);	
	}
	
}
