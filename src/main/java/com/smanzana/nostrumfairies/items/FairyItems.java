package com.smanzana.nostrumfairies.items;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.items.FairyInstrument.InstrumentType;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FairyItems {

	@ObjectHolder(FairyGael.ID_ATTACK) public static FairyGael attackGael;
	@ObjectHolder(FairyGael.ID_BUILD) public static FairyGael buildGael;
	@ObjectHolder(FairyGael.ID_LOGISTICS) public static FairyGael logisticsGael;
	@ObjectHolder(FairyInstrument.ID_FLUTE) public static FairyInstrument fairyFlute;
	@ObjectHolder(FairyInstrument.ID_HARP) public static FairyInstrument fairyHarp;
	@ObjectHolder(FairyInstrument.ID_OCARINA) public static FairyInstrument fairyOcarina;
	@ObjectHolder(FeyResource.ID_TEARS) public static FeyResource feyTears;
	@ObjectHolder(FeyResource.ID_ESSENCE) public static FeyResource feyEssence;
	@ObjectHolder(FeyResource.ID_ESSENCE_CORRUPTED) public static FeyResource feyCorruptedEssence;
	@ObjectHolder(FeyResource.ID_BELL) public static FeyResource feyBell;
	@ObjectHolder(FeyResource.ID_FLOWER) public static FeyResource feyFlower;
	@ObjectHolder(FeyResource.ID_TABLET) public static FeyResource feyTablet;
	@ObjectHolder(FeyResource.ID_GOLEM_TOKEN) public static FeyResource feyGolemToken;
	@ObjectHolder(FeyResource.ID_LOGIC_TOKEN) public static FeyResource feyLogicToken;
	@ObjectHolder(FeySoulStone.ID_SOUL_GEM) public static FeySoulStone soulGem;
	@ObjectHolder(FeySoulStone.ID_SOUL_GAEL) public static FeySoulStone soulGael;
	@ObjectHolder(FeyStone.ID_SPEC_EMERALD) public static FeyStone stoneSpecEmerald;
	@ObjectHolder(FeyStone.ID_SPEC_GARNET) public static FeyStone stoneSpecGarnet;
	@ObjectHolder(FeyStone.ID_SPEC_AQUAMARINE) public static FeyStone stoneSpecAquamarine;
	@ObjectHolder(FeyStone.ID_SPEC_RUBY) public static FeyStone stoneSpecRuby;
	@ObjectHolder(FeyStone.ID_SPEC_SAPPHIRE) public static FeyStone stoneSpecSapphire;
	@ObjectHolder(FeyStone.ID_UPGRADE_EMERALD) public static FeyStone stoneUpgradeEmerald;
	@ObjectHolder(FeyStone.ID_UPGRADE_GARNET) public static FeyStone stoneUpgradeGarnet;
	@ObjectHolder(FeyStone.ID_UPGRADE_AQUAMARINE) public static FeyStone stoneUpgradeAquamarine;
	@ObjectHolder(FeyStone.ID_UPGRADE_RUBY) public static FeyStone stoneUpgradeRuby;
	@ObjectHolder(FeyStone.ID_UPGRADE_SAPPHIRE) public static FeyStone stoneUpgradeSapphire;
	@ObjectHolder(FeyStone.ID_DOWNGRADE_EMERALD) public static FeyStone stoneDowngradeEmerald;
	@ObjectHolder(FeyStone.ID_DOWNGRADE_GARNET) public static FeyStone stoneDowngradeGarnet;
	@ObjectHolder(FeyStone.ID_DOWNGRADE_AQUAMARINE) public static FeyStone stoneDowngradeAquamarine;
	@ObjectHolder(FeyStone.ID_DOWNGRADE_RUBY) public static FeyStone stoneDowngradeRuby;
	@ObjectHolder(FeyStone.ID_DOWNGRADE_SAPPHIRE) public static FeyStone stoneDowngradeSapphire;
	@ObjectHolder(SoulJar.ID) public static SoulJar soulJar;
	@ObjectHolder(TemplateScroll.ID) public static TemplateScroll templateScroll;
	@ObjectHolder(TemplateWand.ID) public static TemplateWand templateWand;
	
	public static Item.Properties PropBase() {
		return new Item.Properties()
				.group(NostrumFairies.creativeTab)
				;
	}
	
	public static Item.Properties PropUnstackable() {
		return PropBase()
				.maxStackSize(1);
	}
	
	private static final void register(IForgeRegistry<Item> registry, Item item) {
		registry.register(item);
		
		if (item instanceof ILoreTagged) {
			LoreRegistry.instance().register((ILoreTagged) item);
		}
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		
		register(registry, new FairyGael(FairyGaelType.ATTACK).setRegistryName(FairyGael.ID_ATTACK));
		register(registry, new FairyGael(FairyGaelType.BUILD).setRegistryName(FairyGael.ID_BUILD));
		register(registry, new FairyGael(FairyGaelType.LOGISTICS).setRegistryName(FairyGael.ID_LOGISTICS));
		register(registry, new FairyInstrument(InstrumentType.FLUTE).setRegistryName(FairyInstrument.ID_FLUTE));
		register(registry, new FairyInstrument(InstrumentType.HARP).setRegistryName(FairyInstrument.ID_HARP));
		register(registry, new FairyInstrument(InstrumentType.OCARINA).setRegistryName(FairyInstrument.ID_OCARINA));
		register(registry, new FeyResource(FeyResourceType.TEARS).setRegistryName(FeyResource.ID_TEARS));
		register(registry, new FeyResource(FeyResourceType.ESSENCE).setRegistryName(FeyResource.ID_ESSENCE));
		register(registry, new FeyResource(FeyResourceType.ESSENCE_CORRUPTED).setRegistryName(FeyResource.ID_ESSENCE_CORRUPTED));
		register(registry, new FeyResource(FeyResourceType.BELL).setRegistryName(FeyResource.ID_BELL));
		register(registry, new FeyResource(FeyResourceType.FLOWER).setRegistryName(FeyResource.ID_FLOWER));
		register(registry, new FeyResource(FeyResourceType.TABLET).setRegistryName(FeyResource.ID_TABLET));
		register(registry, new FeyResource(FeyResourceType.GOLEM_TOKEN).setRegistryName(FeyResource.ID_GOLEM_TOKEN));
		register(registry, new FeyResource(FeyResourceType.LOGIC_TOKEN).setRegistryName(FeyResource.ID_LOGIC_TOKEN));
		register(registry, new FeySoulStone(SoulStoneType.GEM).setRegistryName(FeySoulStone.ID_SOUL_GEM));
		register(registry, new FeySoulStone(SoulStoneType.GAEL).setRegistryName(FeySoulStone.ID_SOUL_GAEL));
		register(registry, new FeyStone(FeySlotType.SPECIALIZATION, FeyStoneMaterial.EMERALD).setRegistryName(FeyStone.ID_SPEC_EMERALD));
		register(registry, new FeyStone(FeySlotType.SPECIALIZATION, FeyStoneMaterial.GARNET).setRegistryName(FeyStone.ID_SPEC_GARNET));
		register(registry, new FeyStone(FeySlotType.SPECIALIZATION, FeyStoneMaterial.AQUAMARINE).setRegistryName(FeyStone.ID_SPEC_AQUAMARINE));
		register(registry, new FeyStone(FeySlotType.SPECIALIZATION, FeyStoneMaterial.RUBY).setRegistryName(FeyStone.ID_SPEC_RUBY));
		register(registry, new FeyStone(FeySlotType.SPECIALIZATION, FeyStoneMaterial.SAPPHIRE).setRegistryName(FeyStone.ID_SPEC_SAPPHIRE));
		register(registry, new FeyStone(FeySlotType.UPGRADE, FeyStoneMaterial.EMERALD).setRegistryName(FeyStone.ID_UPGRADE_EMERALD));
		register(registry, new FeyStone(FeySlotType.UPGRADE, FeyStoneMaterial.GARNET).setRegistryName(FeyStone.ID_UPGRADE_GARNET));
		register(registry, new FeyStone(FeySlotType.UPGRADE, FeyStoneMaterial.AQUAMARINE).setRegistryName(FeyStone.ID_UPGRADE_AQUAMARINE));
		register(registry, new FeyStone(FeySlotType.UPGRADE, FeyStoneMaterial.RUBY).setRegistryName(FeyStone.ID_UPGRADE_RUBY));
		register(registry, new FeyStone(FeySlotType.UPGRADE, FeyStoneMaterial.SAPPHIRE).setRegistryName(FeyStone.ID_UPGRADE_SAPPHIRE));
		register(registry, new FeyStone(FeySlotType.DOWNGRADE, FeyStoneMaterial.EMERALD).setRegistryName(FeyStone.ID_DOWNGRADE_EMERALD));
		register(registry, new FeyStone(FeySlotType.DOWNGRADE, FeyStoneMaterial.GARNET).setRegistryName(FeyStone.ID_DOWNGRADE_GARNET));
		register(registry, new FeyStone(FeySlotType.DOWNGRADE, FeyStoneMaterial.AQUAMARINE).setRegistryName(FeyStone.ID_DOWNGRADE_AQUAMARINE));
		register(registry, new FeyStone(FeySlotType.DOWNGRADE, FeyStoneMaterial.RUBY).setRegistryName(FeyStone.ID_DOWNGRADE_RUBY));
		register(registry, new FeyStone(FeySlotType.DOWNGRADE, FeyStoneMaterial.SAPPHIRE).setRegistryName(FeyStone.ID_DOWNGRADE_SAPPHIRE));
		register(registry, new SoulJar().setRegistryName(SoulJar.ID));
		register(registry, new TemplateScroll().setRegistryName(TemplateScroll.ID));
		register(registry, new TemplateWand().setRegistryName(TemplateWand.ID));
	}
	
}
