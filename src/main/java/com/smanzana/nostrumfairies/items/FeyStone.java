package com.smanzana.nostrumfairies.items;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.inventory.IFeySlotted;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class FeyStone extends Item implements ILoreTagged, IFeySlotted {

	protected static final String ID_PREFIX = "stone_";
	protected static final String ID_SPEC_PREFIX = "spec_";
	protected static final String ID_UPGRADE_PREFIX = "upgrade_";
	protected static final String ID_DOWNGRADE_PREFIX = "downgrade_";
	protected static final String ID_EMERALD_SUFFIX = "emerald";
	protected static final String ID_GARNET_SUFFIX = "garnet";
	protected static final String ID_AQUAMARINE_SUFFIX = "aquamarine";
	protected static final String ID_RUBY_SUFFIX = "ruby";
	protected static final String ID_SAPPHIRE_SUFFIX = "sapphire";
	public static final String ID_SPEC_EMERALD = ID_PREFIX + ID_SPEC_PREFIX + ID_EMERALD_SUFFIX;
	public static final String ID_SPEC_GARNET = ID_PREFIX + ID_SPEC_PREFIX + ID_GARNET_SUFFIX;
	public static final String ID_SPEC_AQUAMARINE = ID_PREFIX + ID_SPEC_PREFIX + ID_AQUAMARINE_SUFFIX;
	public static final String ID_SPEC_RUBY = ID_PREFIX + ID_SPEC_PREFIX + ID_RUBY_SUFFIX;
	public static final String ID_SPEC_SAPPHIRE = ID_PREFIX + ID_SPEC_PREFIX + ID_SAPPHIRE_SUFFIX;
	public static final String ID_UPGRADE_EMERALD = ID_PREFIX + ID_UPGRADE_PREFIX + ID_EMERALD_SUFFIX;
	public static final String ID_UPGRADE_GARNET = ID_PREFIX + ID_UPGRADE_PREFIX + ID_GARNET_SUFFIX;
	public static final String ID_UPGRADE_AQUAMARINE = ID_PREFIX + ID_UPGRADE_PREFIX + ID_AQUAMARINE_SUFFIX;
	public static final String ID_UPGRADE_RUBY = ID_PREFIX + ID_UPGRADE_PREFIX + ID_RUBY_SUFFIX;
	public static final String ID_UPGRADE_SAPPHIRE = ID_PREFIX + ID_UPGRADE_PREFIX + ID_SAPPHIRE_SUFFIX;
	public static final String ID_DOWNGRADE_EMERALD = ID_PREFIX + ID_DOWNGRADE_PREFIX + ID_EMERALD_SUFFIX;
	public static final String ID_DOWNGRADE_GARNET = ID_PREFIX + ID_DOWNGRADE_PREFIX + ID_GARNET_SUFFIX;
	public static final String ID_DOWNGRADE_AQUAMARINE = ID_PREFIX + ID_DOWNGRADE_PREFIX + ID_AQUAMARINE_SUFFIX;
	public static final String ID_DOWNGRADE_RUBY = ID_PREFIX + ID_DOWNGRADE_PREFIX + ID_RUBY_SUFFIX;
	public static final String ID_DOWNGRADE_SAPPHIRE = ID_PREFIX + ID_DOWNGRADE_PREFIX + ID_SAPPHIRE_SUFFIX;
	
	private final FeySlotType slotType;
	private final FeyStoneMaterial stoneMaterial;
	
	public FeyStone(FeySlotType type, FeyStoneMaterial material) {
		super(FairyItems.PropBase().rarity(Rarity.UNCOMMON));
		this.slotType = type;
		this.stoneMaterial = material;
	}
	
	public static Item getItem(FeySlotType slot, FeyStoneMaterial material) {
		switch (slot) {
		case DOWNGRADE:
			switch (material) {
			case AQUAMARINE:
				return null;//FairyItems.stoneDowngradeAquamarine;
			case EMERALD:
				return null;//FairyItems.stoneDowngradeEmerald;
			case GARNET:
				return null;//FairyItems.stoneDowngradeGarnet;
			case RUBY:
				return FairyItems.stoneDowngradeRuby;
			case SAPPHIRE:
				return FairyItems.stoneDowngradeSapphire;
			}
			break;
		case SPECIALIZATION:
			switch (material) {
			case AQUAMARINE:
				return FairyItems.stoneSpecAquamarine;
			case EMERALD:
				return FairyItems.stoneSpecEmerald;
			case GARNET:
				return FairyItems.stoneSpecGarnet;
			case RUBY:
				return null;//FairyItems.stoneSpecRuby;
			case SAPPHIRE:
				return null;//FairyItems.stoneSpecSapphire;
			}
			break;
		case UPGRADE:
			switch (material) {
			case AQUAMARINE:
				return null;//FairyItems.stoneUpgradeAquamarine;
			case EMERALD:
				return null;//FairyItems.stoneUpgradeEmerald;
			case GARNET:
				return null;//FairyItems.stoneUpgradeGarnet;
			case RUBY:
				return FairyItems.stoneUpgradeRuby;
			case SAPPHIRE:
				return FairyItems.stoneUpgradeSapphire;
			}
			break;
		case EITHERGRADE:
			; // No stones for this type; it's a SLOT type. So fall through.
			break;
		}
		
		return null;
	}
	
    public static ItemStack create(FeySlotType slot, FeyStoneMaterial material, int count) {
    	return new ItemStack(getItem(slot, material), count);
    }
	
    @Override
	public FeyStoneMaterial getStoneMaterial(@Nonnull ItemStack stack) {
		return this.stoneMaterial;
	}
	
    @Override
	public FeySlotType getFeySlot(@Nonnull ItemStack stack) {
		return this.slotType;
	}
	
    @Override
	public String getLoreKey() {
		return "fey_stone";
	}

	@Override
	public String getLoreDisplayName() {
		return "Fey Stones";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Fey stones are pure gemstones imbued with fey essence.", "The fey appear to like them!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Fey stones are pure gemstones imbued with fey essence.", "They can be slotted in different machines to improve performance, remove requirements, or add temporary slots!");
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
}
