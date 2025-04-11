package com.smanzana.nostrumfairies.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.entity.ModelDwarf;
import com.smanzana.nostrumfairies.client.render.entity.ModelDwarfBeard;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class LayerDwarfBeard<T extends EntityDwarf, M extends ModelDwarf<T>> extends RenderLayer<T, M> {

	private static final ResourceLocation TEXT_BEARD_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_beard_1.png");
	private static final ResourceLocation TEXT_BEARD_2 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_beard_2.png");
	private static final ResourceLocation TEXT_BEARD_3 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_beard_3.png");
	private static final ResourceLocation TEXT_BEARD_4 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_beard_4.png");
	
	private ModelDwarfBeard<T> model;
	private ModelDwarfBeard.Type type;
	
	public LayerDwarfBeard(LivingEntityRenderer<T, M> renderer, ModelDwarfBeard.Type type) {
		super(renderer);
		this.model = new ModelDwarfBeard<>(type);
		this.type = type;
	}
	
	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T dwarf, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		
		// TODO maybe make beard style and texture by something that's assigned and persisted to the dwarf?
		if (((dwarf.getUUID().getLeastSignificantBits() >> 3) & 1) != type.ordinal()) { // bit 4
			return;
		}
		
		ResourceLocation text;
		switch ((int) ((dwarf.getUUID().getLeastSignificantBits() >> 1) & 3)) { // bits 2 and 3 (2 + 4)
		case 0:
		default:
			text = TEXT_BEARD_1;
			break;
		case 1:
			text = TEXT_BEARD_2;
			break;
		case 2:
			text = TEXT_BEARD_3;
			break;
		case 3:
			text = TEXT_BEARD_4;
			break;
			
		}
		
		// translate up to head, then render?
		model.setupAnim(dwarf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		model.renderToBuffer(matrixStackIn, bufferIn.getBuffer(model.renderType(text)), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
	}
}
