package com.smanzana.nostrumfairies.items;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.item.api.LoreItem;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.IItemLoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FeyTablet extends LoreItem {

	public static final String ID = "fey_tablet";
	
	public FeyTablet(Item.Properties props) {
		super(props);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final ItemStack stack = playerIn.getItemInHand(hand);
		if (!worldIn.isClientSide) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
			if (attr != null) {
				if (!attr.hasLore(FeyFriendLore.instance)) {
					attr.giveFullLore(FeyFriendLore.instance());
					stack.shrink(1);
					return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
				} else {
					playerIn.sendMessage(new TranslatableComponent("info.tablet.fail"), Util.NIL_UUID);
					return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
				}
			}
		} else {
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
	}
	
	public static final class FeyFriendLore implements IItemLoreTagged {
		
		private static FeyFriendLore instance = null;
		public static FeyFriendLore instance() {
			if (instance == null) {
				instance = new FeyFriendLore();
			}
			return instance;
		}

		@Override
		public String getLoreKey() {
			return "lore_fey_friendship";
		}

		@Override
		public String getLoreDisplayName() {
			return "Fey Friendship";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("The fey have revealed to you a method of creating a stone that can store the very essence of the fey itself.", "Using these soul stones, you can effectively pick up and transport fey.", "To what end, you're still not quite sure...");
		}

		@Override
		public Lore getDeepLore() {
			return getBasicLore();
		}

		@Override
		public ELoreCategory getCategory() {
			return ELoreCategory.ENTITY;
		}

		@Override
		public Item getItem() {
			return FairyItems.feyTablet;
		}
	}
}
