package com.smanzana.nostrumfairies.init;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.SoulJar;
import com.smanzana.nostrumfairies.items.TemplateWand;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
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
	
	private static final void registerBlockRenderLayer() {
		RenderTypeLookup.setRenderLayer(FairyBlocks.bufferChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.buildingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.dwarfCraftingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.elfCraftingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.gnomeCraftingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.farmingBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.feyBush, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.dwarfHome, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.elfHome, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.fairyHome, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.gnomeHome, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.gatheringBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.inputChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.logisticsPylon, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.logisticsSensor, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.magicLightUnlit, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.magicLightDim, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.magicLightMedium, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.magicLightBright, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.miningBlock, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.outputChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.outputPanel, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.storageChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.reinforcedIronChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.reinforcedGoldChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.reinforcedDiamondChest, RenderType.getSolid());
		RenderTypeLookup.setRenderLayer(FairyBlocks.storageMonitor, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(FairyBlocks.templateBlock, (l) -> true);
		RenderTypeLookup.setRenderLayer(FairyBlocks.woodcuttingBlock, RenderType.getCutout());
	}
}
