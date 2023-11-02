package com.smanzana.nostrumfairies.items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.inventory.IFeySlotted;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FeyStone extends Item implements ILoreTagged, IFeySlotted {

	public static final String ID = "fey_stone";
	
	private static FeyStone instance = null;
	public static FeyStone instance() {
		if (instance == null)
			instance = new FeyStone();
		
		return instance;
	}
	
	public FeyStone() {
		super();
		this.setUnlocalizedName(ID);
		this.setRegistryName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		String suffix = getNameFromMeta(i);
		
		return this.getUnlocalizedName() + "." + suffix;
	}
	
	@OnlyIn(Dist.CLIENT)
	public String getModelName(ItemStack stack) {
		return getNameFromMeta(stack.getMetadata());
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @OnlyIn(Dist.CLIENT)
    @Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    	if (this.isInCreativeTab(tab)) {
	    	for (FeySlotType slot : FeySlotType.values())
	    	for (FeyStoneMaterial material : FeyStoneMaterial.values()) {
	    		if (material.existsForSlot(slot)) {
	    			subItems.add(create(slot, material, 1));
	    		}
	    	}
    	}
	}
    
    public static ItemStack create(FeySlotType slot, FeyStoneMaterial material, int count) {
    	return new ItemStack(instance(), count, getMeta(slot, material));
    }
	
	protected static int getMeta(FeySlotType type, FeyStoneMaterial material) {
		// bottom 3 bits are material. bits above are type.
		return (
				(material.ordinal() & 0x7)
			| 	(type.ordinal() << 3)
			);
	}
	
	protected FeySlotType slotFromMeta(int meta) {
		return FeySlotType.values()[(meta >> 3)];
	}
	
	protected FeyStoneMaterial materialFromMeta(int meta) {
		return FeyStoneMaterial.values()[meta & 0x7];
	}
	
	@Nullable
	public FeyStoneMaterial getStoneMaterial(@Nonnull ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		
		return materialFromMeta(stack.getMetadata());
	}
	
	public String getNameFromMeta(int meta) {
		FeySlotType type = slotFromMeta(meta);
		FeyStoneMaterial material = materialFromMeta(meta);
    	return type.getID() + "_" + material.name().toLowerCase();
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
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}

	@Override
	public FeySlotType getFeySlot(ItemStack stack) {
		if (stack.isEmpty()) {
			return null;
		}
		
		return slotFromMeta(stack.getMetadata());
	}
}
