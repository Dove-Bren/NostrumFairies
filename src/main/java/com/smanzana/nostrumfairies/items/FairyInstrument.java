package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
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
	public InteractionResult useOn(UseOnContext context) {
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final ItemStack stack = playerIn.getItemInHand(hand);
		InstrumentType type = getType(stack);
		if (!worldIn.isClientSide) {
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
			if (attr != null && attr.isUnlocked() && playerIn.isShiftKeyDown()) {
				// toggle enable
				attr.setEnabled(!attr.isEnabled());
				NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
				NostrumFairiesSounds.BELL.play(playerIn.level, playerIn.getX(), playerIn.getY(), playerIn.getZ());
				return InteractionResultHolder.pass(stack);
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
			
			sound.play(worldIn, playerIn.getX(), playerIn.getY(), playerIn.getZ());
			
			if (attr != null && attr.isUnlocked()) {
				// Open gui
				// Would be cooler if the fairies flew in towards you after you called
				attr.deactivateFairies(40);
				
				NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
				
				NostrumMagica.playerListener.registerTimer((t, entity, data) -> {
					if (playerIn.isAlive() && playerIn.getItemInHand(hand) == stack) {
						NostrumMagica.instance.proxy.openContainer(playerIn, FairyScreenGui.FairyScreenContainer.Make());
					}
					return true;
				}, 30, 0);
			} else {
				playerIn.sendMessage(new TranslatableComponent("info.instrument.locked"), Util.NIL_UUID);
			}
		}
		
		return InteractionResultHolder.pass(stack);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(NostrumFairies.proxy.getPlayer());
		if (attr != null && !attr.isEnabled()) {
			tooltip.add(new TranslatableComponent("info.instrument.disabled").withStyle(ChatFormatting.DARK_RED));
		}
	}
	
}
