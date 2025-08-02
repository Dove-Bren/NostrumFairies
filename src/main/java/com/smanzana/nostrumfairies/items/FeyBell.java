package com.smanzana.nostrumfairies.items;

import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.api.LoreItem;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.Shapes;

public class FeyBell extends LoreItem {

	public static final String ID = "fey_bell";
	
	public FeyBell(Item.Properties props) {
		super(props);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final ItemStack stack = playerIn.getItemInHand(hand);
		
		if (!worldIn.isClientSide) {
			for (EntityShadowFey ent : worldIn.getEntitiesOfClass(EntityShadowFey.class, Shapes.block().bounds().move(playerIn.getX(), playerIn.getY(), playerIn.getZ()).inflate(30))) {
				ent.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 5));
				NostrumMagica.playerListener.registerTimer((/*Event*/ eType, /*LivingEntity*/ entity, /*Object*/ data) -> {
					if (eType == Event.TIME) {
						NostrumFairiesSounds.BELL.play(worldIn, ent.getX(), ent.getY(), ent.getZ());
					}
					return true;
				}, 10, 10);
			}
			
			NostrumFairiesSounds.BELL.play(worldIn, playerIn.getX(), playerIn.getY(), playerIn.getZ());
		}
		return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
	}
}
