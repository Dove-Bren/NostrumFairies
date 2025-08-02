package com.smanzana.nostrumfairies.research;

import com.smanzana.nostrumaetheria.api.lib.AetheriaResearches;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.items.FairyInstrument;
import com.smanzana.nostrumfairies.items.FairyInstrument.InstrumentType;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;
import com.smanzana.nostrummagica.progression.research.NostrumResearch.Size;
import com.smanzana.nostrummagica.progression.research.NostrumResearchTab;
import com.smanzana.nostrummagica.progression.research.NostrumResearches;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FairyResearches {

	public static NostrumResearchTab researchTab = new NostrumResearchTab("fey", () -> FeySoulStone.create(SoulStoneType.GEM));
	
	public static final ResourceLocation ID_Purify_Essence = NostrumFairies.Loc("purify_essence");
	public static final ResourceLocation ID_Fey_Bell = NostrumFairies.Loc("fey_bell");
	public static final ResourceLocation ID_Fey_Flower = NostrumFairies.Loc("fey_flower");
	public static final ResourceLocation ID_Fey_Souls = NostrumFairies.Loc("fey_souls");
	public static final ResourceLocation ID_Fairy_Instruments = NostrumFairies.Loc("fairy_instruments");
	public static final ResourceLocation ID_Fairy_Gael_Aggressive = NostrumFairies.Loc("fairy_gael_aggressive");
	public static final ResourceLocation ID_Fairy_Gael_Logistics = NostrumFairies.Loc("fairy_gael_logistics");
	public static final ResourceLocation ID_Fairy_Gael_Construction = NostrumFairies.Loc("fairy_gael_construction");
	public static final ResourceLocation ID_Logistics = NostrumFairies.Loc("logistics");
	public static final ResourceLocation ID_Dwarves = NostrumFairies.Loc("dwarves");
	public static final ResourceLocation ID_BuildingBlocks = NostrumFairies.Loc("building_blocks");
	public static final ResourceLocation ID_Dwarf_Crafting = NostrumFairies.Loc("dwarf_crafting");
	public static final ResourceLocation ID_Elves = NostrumFairies.Loc("elves");
	public static final ResourceLocation ID_Elf_Crafting = NostrumFairies.Loc("elf_crafting");
	public static final ResourceLocation ID_Gnomes = NostrumFairies.Loc("gnomes");
	public static final ResourceLocation ID_Gathering_Blocks = NostrumFairies.Loc("gathering_blocks");
	public static final ResourceLocation ID_Gnome_Crafting = NostrumFairies.Loc("gnome_crafting");
	public static final ResourceLocation ID_Fairies = NostrumFairies.Loc("fairies");
	public static final ResourceLocation ID_Logistics_Items = NostrumFairies.Loc("logistics_items");
	public static final ResourceLocation ID_Logistics_Items_Adv = NostrumFairies.Loc("adv_logistics_items");
	public static final ResourceLocation ID_Logistics_Storage_Adv = NostrumFairies.Loc("adv_logistics_storage");
	public static final ResourceLocation ID_Logistics_Construction = NostrumFairies.Loc("logistics_construction");
	public static final ResourceLocation ID_Logistics_Crafting = NostrumFairies.Loc("logistics_crafting");
	public static final ResourceLocation ID_Logistics_Relays = NostrumFairies.Loc("logistics_relays");
	public static final ResourceLocation ID_Logistics_Sensors = NostrumFairies.Loc("logistics_sensors");
	public static final ResourceLocation ID_Soul_Jars = NostrumFairies.Loc("soul_jars");
	
	public static NostrumResearch Purify_Essence;
	public static NostrumResearch Fey_Bell;
	public static NostrumResearch Fey_Flower;
	public static NostrumResearch Fey_Souls;
	public static NostrumResearch Fairy_Instruments;
	public static NostrumResearch Fairy_Gael_Aggressive;
	public static NostrumResearch Fairy_Gael_Logistics;
	public static NostrumResearch Fairy_Gael_Construction;
	public static NostrumResearch Logistics;
	public static NostrumResearch Dwarves;
	public static NostrumResearch BuildingBlocks;
	public static NostrumResearch Dwarf_Crafting;
	public static NostrumResearch Elves;
	public static NostrumResearch Elf_Crafting;
	public static NostrumResearch Gnomes;
	public static NostrumResearch Gathering_Blocks;
	public static NostrumResearch Gnome_Crafting;
	public static NostrumResearch Fairies;
	public static NostrumResearch Logistics_Items;
	public static NostrumResearch Logistics_Items_Adv;
	public static NostrumResearch Logistics_Storage_Adv;
	public static NostrumResearch Logistics_Construction;
	public static NostrumResearch Logistics_Crafting;
	public static NostrumResearch Logistics_Relays;
	public static NostrumResearch Logistics_Sensors;
	public static NostrumResearch Soul_Jars;
	
	
	
	public static final void init() {
		Purify_Essence = NostrumResearch.startBuilding()
			.lore(FairyItems.feyCorruptedEssence)
			.hiddenParent(NostrumResearches.ID_Rituals)
			.reference("ritual::purify_essence", "ritual.purify_essence.name")
		.build(ID_Purify_Essence, researchTab, Size.NORMAL, 0, -1, true, FeyResource.create(FeyResourceType.ESSENCE, 1));
		
		Fey_Bell = NostrumResearch.startBuilding()
			.parent(ID_Purify_Essence)
			.hiddenParent(NostrumResearches.ID_Kani)
			.reference("ritual::fey_bell", "ritual.fey_bell.name")
		.build(ID_Fey_Bell, researchTab, Size.NORMAL, -1, 0, true, FeyResource.create(FeyResourceType.BELL, 1));
		
		Fey_Flower = NostrumResearch.startBuilding()
			.parent(ID_Purify_Essence)
			.hiddenParent(ID_Fey_Bell)
			.lore(EntityShadowFey.ShadowFeyConversionLore.instance())
			.reference("ritual::fey_flower", "ritual.fey_flower.name")
		.build(ID_Fey_Flower, researchTab, Size.NORMAL, 1, 0, true, FeyResource.create(FeyResourceType.FLOWER, 1));
		
		Fey_Souls = NostrumResearch.startBuilding()
			.parent(ID_Purify_Essence)
			.parent(ID_Fey_Flower)
			.parent(ID_Fey_Bell)
			.lore(FeyResource.FeyFriendLore.instance())
			.reference("ritual::soul_stone", "ritual.soul_stone.name")
			.reference("ritual::soul_gael", "ritual.soul_gael.name")
		.build(ID_Fey_Souls, researchTab, Size.LARGE, 0, 1, true, FeySoulStone.create(SoulStoneType.GEM));
		
		Fairy_Instruments = NostrumResearch.startBuilding()
			.parent(ID_Fey_Souls)
			.link(ID_Fairy_Gael_Aggressive)
			.reference("ritual::fairy_instrument_lyre", "ritual.fairy_instrument_lyre.name")
			.reference("ritual::fairy_instrument_flute", "ritual.fairy_instrument_flute.name")
			.reference("ritual::fairy_instrument_ocarina", "ritual.fairy_instrument_ocarina.name")
		.build(ID_Fairy_Instruments, researchTab, Size.GIANT, 2, 1, true, FairyInstrument.create(InstrumentType.FLUTE));
		
		Fairy_Gael_Aggressive = NostrumResearch.startBuilding()
			.parent(ID_Fairy_Instruments)
			.reference("ritual::fairy_gael_aggressive", "ritual.fairy_gael_aggressive.name")
		.build(ID_Fairy_Gael_Aggressive, researchTab, Size.NORMAL, 4, 1, true, FairyGael.create(FairyGaelType.ATTACK, null));
		
		Fairy_Gael_Logistics = NostrumResearch.startBuilding()
			.parent(ID_Fairy_Instruments)
			.hiddenParent(ID_Logistics_Items)
			.reference("ritual::fairy_gael_logistics", "ritual.fairy_gael_logistics.name")
		.build(ID_Fairy_Gael_Logistics, researchTab, Size.NORMAL, 3, 2, true, FairyGael.create(FairyGaelType.LOGISTICS, null));
		
		Fairy_Gael_Construction = NostrumResearch.startBuilding()
			.parent(ID_Fairy_Instruments)
			.hiddenParent(ID_Logistics_Construction)
			.reference("ritual::fairy_gael_construction", "ritual.fairy_gael_construction.name")
		.build(ID_Fairy_Gael_Construction, researchTab, Size.NORMAL, 3, 0, true, FairyGael.create(FairyGaelType.BUILD, null));
		
		Logistics = NostrumResearch.startBuilding()
			.parent(ID_Fey_Souls)
			.hiddenParent(AetheriaResearches.ID_Aether_Furnace)
			.hiddenParent(NostrumResearches.ID_Magic_Token)
			.reference("ritual::logistics_tokens", "ritual.logistics_tokens.name")
			.reference("ritual::fey_upgrade.down.ruby", "ritual.fey_upgrade.down.ruby.name")
			.reference("ritual::fey_upgrade.up.ruby", "ritual.fey_upgrade.up.ruby.name")
			.reference("ritual::fey_upgrade.down.sapphire", "ritual.fey_upgrade.down.sapphire.name")
			.reference("ritual::fey_upgrade.up.sapphire", "ritual.fey_upgrade.up.sapphire.name")
		.build(ID_Logistics, researchTab, Size.GIANT, -2, 1, true, FeyResource.create(FeyResourceType.LOGIC_TOKEN, 1));
		
		Dwarves = NostrumResearch.startBuilding()
			.parent(ID_Logistics)
			.reference("ritual::dwarf_home", "ritual.dwarf_home.name")
			.reference("ritual::mining_block", "ritual.mining_block.name")
			.reference("ritual::fey_specialization.aquamarine", "ritual.fey_specialization.aquamarine.name")
		.build(ID_Dwarves, researchTab, Size.LARGE, -4, 0, true, new ItemStack(FairyBlocks.dwarfHome));
		
		// dwarf blocks
		
		BuildingBlocks = NostrumResearch.startBuilding()
			.parent(ID_Dwarves)
			.hiddenParent(ID_Logistics_Construction)
			.reference("ritual::building_block", "ritual.building_block.name")
		.build(ID_BuildingBlocks, researchTab, Size.NORMAL, -4, -1, true, new ItemStack(FairyBlocks.buildingBlock));
		
		Dwarf_Crafting = NostrumResearch.startBuilding()
			.parent(ID_Dwarves)
			.hiddenParent(ID_Logistics_Crafting)
			.reference("ritual::crafting_block.dwarf", "ritual.crafting_block.dwarf.name")
		.build(ID_Dwarf_Crafting, researchTab, Size.NORMAL, -5, -1, true, new ItemStack(FairyBlocks.dwarfCraftingBlock));
		
	
		
		Elves = NostrumResearch.startBuilding()
			.parent(ID_Logistics)
			.reference("ritual::elf_home", "ritual.elf_home.name")
			.reference("ritual::woodcutting_block", "ritual.woodcutting_block.name")
			.reference("ritual::fey_specialization.emerald", "ritual.fey_specialization.emerald.name")
		.build(ID_Elves, researchTab, Size.LARGE, -3, 0, true, new ItemStack(FairyBlocks.elfHome));
		
		// elf blocks
		
		Elf_Crafting = NostrumResearch.startBuilding()
			.parent(ID_Elves)
			.hiddenParent(ID_Logistics_Crafting)
			.reference("ritual::crafting_block.elf", "ritual.crafting_block.elf.name")
		.build(ID_Elf_Crafting, researchTab, Size.NORMAL, -3, -1, true, new ItemStack(FairyBlocks.elfCraftingBlock));
		
		
		
		Gnomes = NostrumResearch.startBuilding()
			.parent(ID_Logistics)
			.reference("ritual::gnome_home", "ritual.gnome_home.name")
			.reference("ritual::farming_block", "ritual.farming_block.name")
			.reference("ritual::fey_specialization.garnet", "ritual.fey_specialization.garnet.name")
		.build(ID_Gnomes, researchTab, Size.LARGE, -4, 2, true, new ItemStack(FairyBlocks.gnomeHome));
		
		// gnome blocks
		
		Gathering_Blocks = NostrumResearch.startBuilding()
			.parent(ID_Gnomes)
			.hiddenParent(ID_Logistics_Items)
			.reference("ritual::gathering_block", "ritual.gathering_block.name")
		.build(ID_Gathering_Blocks, researchTab, Size.NORMAL, -4, 3, true, new ItemStack(FairyBlocks.gatheringBlock));
		
		Gnome_Crafting = NostrumResearch.startBuilding()
			.parent(ID_Gnomes)
			.hiddenParent(ID_Logistics_Crafting)
			.reference("ritual::crafting_block.gnome", "ritual.crafting_block.gnome.name")
		.build(ID_Gnome_Crafting, researchTab, Size.NORMAL, -5, 3, true, new ItemStack(FairyBlocks.gnomeCraftingBlock));
		
		
		Fairies = NostrumResearch.startBuilding()
			.parent(ID_Logistics)
			.reference("ritual::fairy_home", "ritual.fairy_home.name")
		.build(ID_Fairies, researchTab, Size.LARGE, -3, 2, true, new ItemStack(FairyBlocks.fairyHome));
	
		Logistics_Items = NostrumResearch.startBuilding()
			.hiddenParent(ID_Logistics)
			.hiddenParent(ID_Fairies)
			.hiddenParent(ID_Fairy_Instruments)
			.reference("ritual::lchest_storage", "ritual.lchest_storage.name")
		.build(ID_Logistics_Items, researchTab, Size.GIANT, 0, 3, true, new ItemStack(FairyBlocks.storageChest));
	
		Logistics_Items_Adv = NostrumResearch.startBuilding()
			.parent(ID_Logistics_Items)
			.reference("ritual::lchest_buffer", "ritual.lchest_buffer.name")
			.reference("ritual::lchest_output", "ritual.lchest_output.name")
			.reference("ritual::lchest_input", "ritual.lchest_input.name")
		.build(ID_Logistics_Items_Adv, researchTab, Size.LARGE, 0, 4, true, new ItemStack(FairyBlocks.outputChest));
	
		Logistics_Storage_Adv = NostrumResearch.startBuilding()
			.parent(ID_Logistics_Items_Adv)
			.hiddenParent(ID_Logistics_Sensors)
			.reference("ritual::lchest_reinforced_iron", "ritual.lchest_reinforced_iron.name")
			.reference("ritual::lchest_reinforced_gold", "ritual.lchest_reinforced_gold.name")
			.reference("ritual::lchest_reinforced_diamond", "ritual.lchest_reinforced_diamond.name")
		.build(ID_Logistics_Storage_Adv, researchTab, Size.LARGE, 0, 5, true, new ItemStack(FairyBlocks.reinforcedGoldChest));
		
		Logistics_Construction = NostrumResearch.startBuilding()
			.parent(ID_Logistics_Items)
			.hiddenParent(ID_Dwarves)
			.hiddenParent(NostrumResearches.ID_Geogems)
			.hiddenParent(NostrumResearches.ID_Magicfacade)
			.reference("ritual::template_wand", "ritual.template_wand.name")
		.build(ID_Logistics_Construction, researchTab, Size.LARGE, 1, 4, true, new ItemStack(FairyItems.templateWand));
		
		Logistics_Crafting = NostrumResearch.startBuilding()
			.parent(ID_Logistics_Items)
			.hiddenParent(ID_Gnomes)
			.hiddenParent(ID_Dwarves)
			.hiddenParent(ID_Elves)
			.hiddenParent(NostrumResearches.ID_Geogems)
			.link(ID_Dwarf_Crafting)
		.build(ID_Logistics_Crafting, researchTab, Size.LARGE, -1, 4, true, new ItemStack(FairyBlocks.dwarfCraftingBlock));
		
		Logistics_Relays = NostrumResearch.startBuilding()
			.parent(ID_Logistics_Items)
			.hiddenParent(NostrumResearches.ID_Geogems)
			.reference("ritual::logistics_pylon", "ritual.logistics_pylon.name")
		.build(ID_Logistics_Relays, researchTab, Size.NORMAL, -1, 2, true, new ItemStack(FairyBlocks.logisticsPylon));
		
		Logistics_Sensors = NostrumResearch.startBuilding()
			.parent(ID_Logistics_Items)
			.reference("ritual::storage_monitor", "ritual.storage_monitor.name")
			.reference("ritual::storage_sensor", "ritual.storage_sensor.name")
		.build(ID_Logistics_Sensors, researchTab, Size.NORMAL, 1, 2, true, new ItemStack(FairyBlocks.storageMonitor));
		
		Soul_Jars = NostrumResearch.startBuilding()
			.hiddenParent(ID_Fey_Souls)
			.reference("ritual::soul_jar", "ritual.soul_jar.name")
		.build(ID_Soul_Jars, researchTab, Size.NORMAL, 0, 2, true, new ItemStack(FairyItems.soulJar));
	}
	
}
