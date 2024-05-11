package com.smanzana.nostrumfairies.init;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.SoulJar;
import com.smanzana.nostrumfairies.items.TemplateWand;

import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

/**
 * Client handler for MOD bus events.
 * MOD bus is not game event bus.
 * @author Skyler
 *
 */
@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientInit {

	
	private static final void registerItemModelProperties() {
		ItemModelsProperties.registerProperty(FairyItems.soulGem, new ResourceLocation("filled"), FeySoulStone::ModelFilled);
		ItemModelsProperties.registerProperty(FairyItems.soulGem, new ResourceLocation("type_idx"), FeySoulStone::ModelType);
		ItemModelsProperties.registerProperty(FairyItems.soulGael, new ResourceLocation("filled"), FeySoulStone::ModelFilled);
		ItemModelsProperties.registerProperty(FairyItems.soulGael, new ResourceLocation("type_idx"), FeySoulStone::ModelType);
		ItemModelsProperties.registerProperty(FairyItems.soulJar, new ResourceLocation("filled"), SoulJar::ModelFilled);
		ItemModelsProperties.registerProperty(FairyItems.templateWand, new ResourceLocation("mode"), TemplateWand::ModelMode);
	}
}
