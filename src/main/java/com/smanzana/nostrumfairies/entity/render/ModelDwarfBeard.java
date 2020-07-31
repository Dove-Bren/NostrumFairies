package com.smanzana.nostrumfairies.entity.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelDwarfBeard extends ModelBase {
	
	public static enum Type {
		FULL,
		LONG,
	}
	
	protected final Type type;

	private ModelRenderer base;
	
	public ModelDwarfBeard(Type type) {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		this.type = type;
		switch (type) {
		case FULL:
			base = new ModelRenderer(this, 0, 0);
			base.setTextureSize(32, 32);
			base.addBox(-5, -2, -5, 10, 4, 6);
			base.addBox(-5, 2, -5, 10, 4, 6);
			break;
		case LONG:
			base = new ModelRenderer(this, 0, 10);
			base.setTextureSize(32, 32);
			base.addBox(-3, -1, -6, 6, 3, 2);
			base.setTextureOffset(16,  10);
			base.addBox(-2, 2, -6, 4, 4, 2);
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
