package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Magical instrument that allows you to open up the fairy GUI and interact with personal fairies
 * @author Skyler
 *
 */
public class FairyInstrument extends Item implements ILoreTagged {

	public static enum InstrumentType {
		FLUTE("flute"),
		HARP("harp"),
		OCARINA("ocarina");
		
		private final String suffix;
		
		private InstrumentType(String suffix) {
			this.suffix = suffix;
		}
		
		public String getSuffix() {
			return suffix;
		}
	}
	
	protected static final String ID_BASE = "fairy_";
	public static final String ID_FLUTE = ID_BASE + "flute";
	public static final String ID_HARP = ID_BASE + "harp";
	public static final String ID_OCARINA = ID_BASE + "ocarina";
	
	private final InstrumentType type;
	
	public FairyInstrument(InstrumentType type) {
		super(FairyItems.PropUnstackable()
				.rarity(Rarity.UNCOMMON));
		this.type = type;
	}
	
	public static Item getItem(InstrumentType type) {
		switch (type) {
		case FLUTE:
			return FairyItems.fairyFlute;
		case HARP:
			return FairyItems.fairyHarp;
		case OCARINA:
			return FairyItems.fairyOcarina;
		}
		
		return null;
	}
	
    public static ItemStack create(InstrumentType type) {
    	return new ItemStack(getItem(type));
    }
	
    @Override
	public String getLoreKey() {
		return "fairy_instrument";
	}

	@Override
	public String getLoreDisplayName() {
		return "Fairy Instruments";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("These instruments are filled with a wild, chaotic, but friendly energy. Simply holding them makes you feel more at peace.", "If what the fairies told you is correct, this instrument should help you balance yourself and allow you to commune with fairies in gaels.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("These instruments are filled with a wild, chaotic, but friendly energy. Simply holding them makes you feel more at peace.", "Like the fairies told you, playing this instrument allows you to commune more easily with fairies in gaels. Gaels that have been sealed and attuned can be arranged. Furthermore, individual fairies can be instructed.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}

	public InstrumentType getType(ItemStack stack) {
		return this.type;
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		return ActionResultType.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack stack = playerIn.getHeldItem(hand);
		InstrumentType type = getType(stack);
		if (!worldIn.isRemote) {
			INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
			if (attr != null && !attr.isUnlocked()) {
				// Possibly unlock
				INostrumMagic magicAttr = NostrumMagica.getMagicWrapper(playerIn);
				if (magicAttr.isUnlocked() && magicAttr.getCompletedResearches().contains("fairy_instruments")) {
					attr.unlock();
					NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
				}
			}
			
			// Check before playing sound whether it's a disable
			if (attr != null && attr.isUnlocked() && playerIn.isSneaking()) {
				// toggle enable
				attr.setEnabled(!attr.isEnabled());
				NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
				NostrumFairiesSounds.BELL.play(playerIn.world, playerIn.posX, playerIn.posY, playerIn.posZ);
				return ActionResult.newResult(ActionResultType.PASS, stack);
			}
				
			
			final NostrumFairiesSounds sound;
			switch (type) {
			case FLUTE:
			default:
				sound = NostrumFairiesSounds.FLUTE;
				break;
			case HARP:
				sound = NostrumFairiesSounds.LYRE;
				break;
			case OCARINA:
				sound = NostrumFairiesSounds.OCARINA;
				break;
			}
			
			sound.play(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ);
			
			if (attr != null && attr.isUnlocked()) {
				// Open gui
				// Would be cooler if the fairies flew in towards you after you called
				attr.deactivateFairies(40);
				
				NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
				
				NostrumMagica.playerListener.registerTimer((t, entity, data) -> {
					if (playerIn.isAlive() && playerIn.getHeldItem(hand) == stack) {
						playerIn.openGui(NostrumFairies.instance, NostrumFairyGui.fairyGuiID, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
					}
					return true;
				}, 30, 0);
			} else {
				playerIn.sendMessage(new TranslationTextComponent("info.instrument.locked"));
			}
		}
		
		return ActionResult.newResult(ActionResultType.PASS, stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(NostrumFairies.proxy.getPlayer());
		if (attr != null && !attr.isEnabled()) {
			tooltip.add(new TranslationTextComponent("info.instrument.disabled").applyTextStyle(TextFormatting.DARK_RED));
		}
	}
	
}
