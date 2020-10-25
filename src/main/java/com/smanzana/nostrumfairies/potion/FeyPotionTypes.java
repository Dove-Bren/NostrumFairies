package com.smanzana.nostrumfairies.potion;

import com.smanzana.nostrumfairies.NostrumFairies;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public enum FeyPotionTypes {

	FEY_VISIBILITY(FeyVisibilityPotion.instance());
	
	private final PotionType type;
	
	private FeyPotionTypes(String name, PotionEffect ... effects) {
		this.type = new PotionType(name, effects);
		type.setRegistryName(new ResourceLocation(NostrumFairies.MODID, name));
		GameRegistry.register(type);
	}
	
	private FeyPotionTypes(Potion pot) {
		this(pot.getName(), new PotionEffect(pot, 20 * 120));
	}
	
	public PotionType getType() {
		return type;
	}
	
	public static void register() {
		for (FeyPotionTypes wrapper : FeyPotionTypes.values()) {
			GameRegistry.register(wrapper.type);
		}
	}
	
}
