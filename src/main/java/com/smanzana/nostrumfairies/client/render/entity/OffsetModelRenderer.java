package com.smanzana.nostrumfairies.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * ModelRenderer that adds back the ability to set offsets to ease in porting.
 * Elsewhere I was baking the static offsets into the box's coords, but in Fairies I
 * animate by moving the offsets in certain situations.
 * I think I could render the parts manually and adjust the MatrixStack, but then
 * I don't get to use any parent rotation or offsetting anymore.
 * @author Skyler
 *
 */
public class OffsetModelRenderer extends ModelRenderer {
	
	public float offsetX;
	public float offsetY;
	public float offsetZ;
	
	public OffsetModelRenderer(Model base) {
		super(base);
	}
	
	public OffsetModelRenderer(Model base, int textOffX, int textOffY) {
		this(base);
		this.setTextureOffset(textOffX, textOffY);
	}
	
	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
		
		// Not sure order. May have to translate after rotating...
		matrixStackIn.push();
		matrixStackIn.translate(offsetX, offsetY, offsetZ);
		super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
		matrixStackIn.pop();
		
//		if (this.showModel) {
//			if (!this.cubeList.isEmpty() || !this.childModels.isEmpty()) {
//				matrixStackIn.push();
//				this.translateRotate(matrixStackIn);
//				this.doRender(matrixStackIn.getLast(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//
//				for(ModelRenderer modelrenderer : this.childModels) {
//					modelrenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
//				}
//
//				matrixStackIn.pop();
//			}
//		}
	}

}
