package com.smanzana.nostrumfairies.entity.render.layers;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.render.ModelDwarfBeard;
import com.smanzana.nostrumfairies.entity.render.RenderDwarf;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerDwarfBeard implements LayerRenderer<EntityDwarf> {

	private static final ResourceLocation TEXT_BEARD_1 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_beard_1.png");
	private static final ResourceLocation TEXT_BEARD_2 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_beard_2.png");
	private static final ResourceLocation TEXT_BEARD_3 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_beard_3.png");
	private static final ResourceLocation TEXT_BEARD_4 = new ResourceLocation(NostrumFairies.MODID, "textures/entity/dwarf_beard_4.png");
	
	private ModelDwarfBeard model;
	private RenderDwarf renderer;
	private ModelDwarfBeard.Type type;
	
	public LayerDwarfBeard(RenderDwarf renderer, ModelDwarfBeard.Type type) {
		this.model = new ModelDwarfBeard(type);
		this.renderer = renderer;
		this.type = type;
	}
	
	@Override
	public void doRenderLayer(EntityDwarf dwarf, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		
		// TODO maybe make beard style and texture by something that's assigned and persisted to the dwarf?
		if (((dwarf.getUniqueID().getLeastSignificantBits() >> 3) & 1) != type.ordinal()) { // bit 4
			return;
		}
		
		ResourceLocation text;
		switch ((int) ((dwarf.getUniqueID().getLeastSignificantBits() >> 1) & 3)) { // bits 2 and 3 (2 + 4)
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
		
		renderer.bindTexture(text);
		
		// translate up to head, then render?
		model.render(dwarf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

}
