package com.smanzana.nostrumfairies.client.model;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.entity.ModelBuildingDwarf;
import com.smanzana.nostrumfairies.client.render.entity.ModelCraftingDwarf;
import com.smanzana.nostrumfairies.client.render.entity.ModelDwarfBeard;
import com.smanzana.nostrumfairies.client.render.entity.ModelElfArcher;
import com.smanzana.nostrumfairies.client.render.entity.ModelElfMage;
import com.smanzana.nostrumfairies.client.render.entity.ModelFairy;
import com.smanzana.nostrumfairies.client.render.entity.ModelGnome;
import com.smanzana.nostrumfairies.client.render.entity.ModelGnomeHat;
import com.smanzana.nostrumfairies.client.render.entity.ModelMiningDwarf;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FairiesModelLayers {

	public static final ModelLayerLocation BuildingDwarf = make("dwarf_building");
	public static final ModelLayerLocation BuildingDwarfLeft = make("dwarf_building_left");
	public static final ModelLayerLocation CraftingDwarf = make("dwarf_crafting");
	public static final ModelLayerLocation CraftingDwarfLeft = make("dwarf_crafting_left");
	public static final ModelLayerLocation MiningDwarf = make("dwarf_mining");
	public static final ModelLayerLocation MiningDwarfLeft = make("dwarf_mining_left");
	public static final ModelLayerLocation DwarfBeardFull = make("dwarf_beard_full");
	public static final ModelLayerLocation DwarfBeardLong = make("dwarf_beard_long");
	public static final ModelLayerLocation MageElf = make("elf_mage");
	public static final ModelLayerLocation MageElfLeft = make("elf_mage_left");
	public static final ModelLayerLocation ArcherElf = make("elf_archer");
	public static final ModelLayerLocation ArcherElfLeft = make("elf_archer_left");
	public static final ModelLayerLocation Fairy = make("fairy");
	public static final ModelLayerLocation Gnome = make("gnome");
	public static final ModelLayerLocation GnomeHatErect = make("gnomehat_erect");
	public static final ModelLayerLocation GnomeHatPlain = make("gnomehat_plain");
	public static final ModelLayerLocation GnomeHatLimp = make("gnomehat_limp");
	public static final ModelLayerLocation GnomeHatSmall = make("gnomehat_small");
	
	
	private static final ModelLayerLocation make(String name) {
		return make(name, "main");
	}
	
	private static final ModelLayerLocation make(String name, String layer) {
		return new ModelLayerLocation(new ResourceLocation(NostrumFairies.MODID, name), layer);
	}
	
	@SubscribeEvent
	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(BuildingDwarf, () -> ModelBuildingDwarf.createLayer(false));
		event.registerLayerDefinition(BuildingDwarfLeft, () -> ModelBuildingDwarf.createLayer(true));

		event.registerLayerDefinition(CraftingDwarf, () -> ModelCraftingDwarf.createLayer(false));
		event.registerLayerDefinition(CraftingDwarfLeft, () -> ModelCraftingDwarf.createLayer(true));

		event.registerLayerDefinition(MiningDwarf, () -> ModelMiningDwarf.createLayer(false));
		event.registerLayerDefinition(MiningDwarfLeft, () -> ModelMiningDwarf.createLayer(true));
		
		event.registerLayerDefinition(DwarfBeardFull, ModelDwarfBeard::createFullLayer);
		event.registerLayerDefinition(DwarfBeardLong, ModelDwarfBeard::createLongLayer);

		event.registerLayerDefinition(MageElf, () -> ModelElfMage.createLayer(false));
		event.registerLayerDefinition(MageElfLeft, () -> ModelElfMage.createLayer(true));

		event.registerLayerDefinition(ArcherElf, () -> ModelElfArcher.createLayer(false));
		event.registerLayerDefinition(ArcherElfLeft, () -> ModelElfArcher.createLayer(true));
		
		event.registerLayerDefinition(Fairy, ModelFairy::createLayer);
		event.registerLayerDefinition(Gnome, ModelGnome::createLayer);

		event.registerLayerDefinition(GnomeHatErect, ModelGnomeHat::createErectLayer);
		event.registerLayerDefinition(GnomeHatPlain, ModelGnomeHat::createPlainLayer);
		event.registerLayerDefinition(GnomeHatLimp, ModelGnomeHat::createLimpLayer);
		event.registerLayerDefinition(GnomeHatSmall, ModelGnomeHat::createSmallLayer);
	}
}
