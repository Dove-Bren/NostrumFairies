package com.smanzana.nostrumfairies.potion;

import com.smanzana.nostrumfairies.NostrumFairies;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public enum FeyPotionTypes {

	FEY_VISIBILITY(FeyVisibilityPotion.instance(), 2 * 60),
	FEY_VISIBILITY_EXTENDED("extended_" + FeyVisibilityPotion.instance().getName(), new PotionEffect(FeyVisibilityPotion.instance(), 20 * 8 * 60));
	
	private final PotionType type;
	
	private FeyPotionTypes(String name, PotionEffect ... effects) {
		this.type = new PotionType(name, effects);
		type.setRegistryName(new ResourceLocation(NostrumFairies.MODID, name));
	}
	
	private FeyPotionTypes(Potion pot, float seconds) {
		this(pot.getName(), new PotionEffect(pot, (int) (20 * seconds)));
	}
	
	private FeyPotionTypes(Potion pot) {
		this(pot, 20);
	}
	
	public PotionType getType() {
		return type;
	}
	
	public static void register(IForgeRegistry<PotionType> registry) {
		for (FeyPotionTypes wrapper : FeyPotionTypes.values()) {
			registry.register(wrapper.type);
		}
	}
	
}
