package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarfBuilder;

public class ModelBuildingDwarf extends ModelDwarf<EntityDwarfBuilder> {

	private OffsetModelRenderer hammer;
	
	public ModelBuildingDwarf(boolean leftHanded) {
		super(leftHanded);
	}
	
	protected OffsetModelRenderer createHammer() {
		
		// 16x16x16 is one block.
		// Y starts at offset 24 and grows down
		final int textW = 128;
		final int textH = 64;
		
		hammer = new OffsetModelRenderer(this);
		hammer.setTexSize(textW, textH);
		hammer.setPos(-0.5F, 0.0F, -0.5F);
		
		// TODO need to shift x and z by -.5?
		hammer.offsetY = (9f / 16f); // height of arm, - a bit
		hammer.addBox("", -4.0F, -18.5F, -2.5F, 7, 4, 6, 0f, 88, 15);
		hammer.addBox("", -5.0F, -19.0F, -3.0F, 1, 5, 7, 0f, 88, 38);
		hammer.addBox("", 3.0F, -19.0F, -3.0F, 1, 5, 7, 0f, 88, 25);
		hammer.addBox("", -1.0F, -15.0F, 0.0F, 1, 15, 1, 0f, 84, 15);

		hammer.zRot = (float) (.5 * Math.PI) - .2f;
		hammer.xRot = (float) (.5 * Math.PI);
		
		return hammer;
	}
	
	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		super.renderToBuffer(matrixStackIn, buffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}
	
	@Override
	protected OffsetModelRenderer createHeldItem(boolean mainhand) {
		if (mainhand) {
			return createHammer();
		}
		
		return null;
	}
	
}
