package com.smanzana.nostrumfairies.potion;

import com.smanzana.nostrumfairies.NostrumFairies;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FeyVisibilityPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumFairies.MODID, "potions-feyvisibility");
	
	private static FeyVisibilityPotion instance;
	public static FeyVisibilityPotion instance() {
		if (instance == null)
			instance = new FeyVisibilityPotion();
		
		return instance;
	}
	
	private FeyVisibilityPotion() {
		super(true, 0xFFEFFF63);
		
		this.setPotionName("potion.fey-visibility.name");
		this.setRegistryName(Resource);
		this.setBeneficial();
	}
	
	public boolean isReady(int duration, int amp) {
		return false; // we don't actually do anything
	}

	@Override
	public void performEffect(EntityLivingBase entity, int amp) {
		;
    }
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		PotionIcon.FEY_VISIBILITY.draw(mc, x + 6, y + 7);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
		PotionIcon.FEY_VISIBILITY.draw(mc, x + 3, y + 3);
	}
	
}
