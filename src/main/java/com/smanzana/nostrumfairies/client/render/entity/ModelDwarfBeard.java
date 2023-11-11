package com.smanzana.nostrumfairies.client.render.entity;

import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;

public class ModelDwarfBeard<T extends EntityDwarf> extends EntityModel<T> {
	
	public static enum Type {
		FULL,
		LONG,
	}
	
	protected final Type type;

	private RendererModel base;
	
	public ModelDwarfBeard(Type type) {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		this.type = type;
		switch (type) {
		case FULL:
			base = new RendererModel(this, 0, 0);
			base.setTextureSize(32, 32);
			base.addBox(-5, -2, -5, 10, 4, 6);
			base.addBox(-5, 2, -5, 10, 4, 6);
			break;
		case LONG:
			base = new RendererModel(this, 0, 10);
			base.setTextureSize(32, 32);
			base.addBox(-3, -1, -6, 6, 3, 2);
			base.setTextureOffset(16,  10);
			base.addBox(-2, 2, -6, 4, 4, 2);
			break;
		}
	}
	
	@Override
	public void render(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float headAngleY, float headAngleX, float scale) {
		base.rotateAngleX = headAngleX * 0.017453292F;
		base.rotateAngleY = headAngleY * 0.017453292F;
		
		base.render(scale);	
	}
	
}
