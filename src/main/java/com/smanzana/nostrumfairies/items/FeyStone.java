package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FeyStone extends Item implements ILoreTagged {

	public static enum FeyStoneMaterial {
		EMERALD(FeySlotType.SPECIALIZATION),
		GARNET(FeySlotType.SPECIALIZATION),
		AQUAMARINE(FeySlotType.SPECIALIZATION),
		AMETHYST(FeySlotType.SOUL),
		RUBY(FeySlotType.UPGRADE, FeySlotType.DOWNGRADE),
		SAPPHIRE(FeySlotType.UPGRADE, FeySlotType.DOWNGRADE);
		
		private FeySlotType[] types;
		
		private FeyStoneMaterial(FeySlotType ... matchingTypes) {
			this.types = matchingTypes;
		}
		
		public boolean existsForSlot(FeySlotType slot) {
			for (FeySlotType type : types) {
				if (type == slot) {
					return true;
				}
			}
			
			return false;
		}
	}
	
public static final String ID = "fey_stone";
	
	private static FeyStone instance = null;
	public static FeyStone instance() {
		if (instance == null)
			instance = new FeyStone();
		
		return instance;
	}
	
	public static void init() {
		;
	}
	
	public FeyStone() {
		super();
		this.setUnlocalizedName(ID);
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
	
	@SideOnly(Side.CLIENT)
	public String getModelName(ItemStack stack) {
		return getNameFromMeta(stack.getMetadata());
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	for (FeySlotType slot : FeySlotType.values())
    	for (FeyStoneMaterial material : FeyStoneMaterial.values()) {
    		if (material.existsForSlot(slot)) {
    			subItems.add(new ItemStack(this, 1, getMeta(slot, material)));
    		}
    	}
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
	
	public FeySlotType getSlot(@Nullable ItemStack stack) {
		if (stack == null) {
			return null;
		}
		
		return slotFromMeta(stack.getMetadata());
	}
	
	public FeyStoneMaterial getMaterial(@Nullable ItemStack stack) {
		if (stack == null) {
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
		return new Lore().add("");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
}
