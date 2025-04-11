package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelDwarfBeard<T extends EntityDwarf> extends EntityModel<T> {
	
	public static enum Type {
		FULL,
		LONG,
	}
	
	protected final Type type;

	private ModelPart base;
	
	public ModelDwarfBeard(Type type) {
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		
		this.type = type;
		switch (type) {
		case FULL:
			base = new ModelPart(this, 0, 0);
			base.setTexSize(32, 32);
			base.addBox(-5, -2, -5, 10, 4, 6);
			base.addBox(-5, 2, -5, 10, 4, 6);
			break;
		case LONG:
			base = new ModelPart(this, 0, 10);
			base.setTexSize(32, 32);
			base.addBox(-3, -1, -6, 6, 3, 2);
			base.texOffs(16,  10);
			base.addBox(-2, 2, -6, 4, 4, 2);
			break;
		}
	}
	
	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		base.xRot = headPitch * 0.017453292F;
		base.yRot = netHeadYaw * 0.017453292F;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		base.render(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
}
