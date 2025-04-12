package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.utils.EntitySpawning;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Stores any tamed entity
 * @author Skyler
 *
 */
public class SoulJar extends Item implements ILoreTagged {
	
	public static final float ModelFilled(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
		return isFilled(stack) ? 1f : 0f;
	}

	public static final String ID = "soul_jar";
	
	public SoulJar() {
		super(FairyItems.PropUnstackable());
	}
	
	protected static ItemStack createInternal(LivingEntity entity) {
		ItemStack stack = new ItemStack(FairyItems.soulJar);
		setStoredEntity(stack, entity);
		return stack;
	}
	
	public static ItemStack create(TamableAnimal entity) {
		return createInternal(entity);
	}
	
	public static ItemStack create(ITameableEntity entity) {
		return createInternal((LivingEntity) entity);
	}
	
	public static ItemStack createFakeFilled() {
		ItemStack stack = new ItemStack(FairyItems.soulJar);
		setStoredEntityData(stack, new CompoundTag(), null);
		return stack;
	}
	
	protected static void setStoredEntityData(ItemStack stack, @Nullable CompoundTag data, @Nullable String name) {
		CompoundTag tag = null; // create a new one to discard old entity if passed in null
		if (data != null) {
			tag = new CompoundTag();
			tag.put("entity", data);
			tag.putString("name", name == null ? "" : name);
		}
		stack.setTag(tag);
	}
	
	protected static void setStoredEntity(ItemStack stack, @Nullable LivingEntity entity) {
		final CompoundTag data = entity == null ? null : entity.serializeNBT();
		final String name = entity == null ? null : entity.getDisplayName().getString();
		setStoredEntityData(stack, data, name);
	}
	
	protected static @Nullable LivingEntity spawnStoredEntity(ItemStack stack, Level world, double x, double y, double z) {
		LivingEntity ent = null;
		CompoundTag nbt = stack.getTag();
		if (nbt != null && nbt.contains("entity", Tag.TAG_COMPOUND)) {
			Entity entity = EntitySpawning.readEntity(world, nbt.getCompound("entity"), new Vec3(x, y, z));
			if (entity == null) {
				;
			} else if (entity instanceof LivingEntity) {
				ent = (LivingEntity) entity;
				world.addFreshEntity(ent);
			} else {
				entity.discard();
			}
		}
		return ent;
	}
	
	public static @Nullable String getStoredEntityName(@Nonnull ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTag()) {
			return null;
		}
		
		return stack.getTag().getString("name");
	}
	
	public static ItemStack clearEntity(ItemStack stack) {
		if (stack.hasTag()) {
			stack.getTag().remove("name");
			stack.getTag().remove("entity");
		}
		return stack;
	}
	
	public static boolean isFilled(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains("entity", Tag.TAG_COMPOUND);
	}
	
	@Override
	public String getLoreKey() {
		return "soul_jar";
	}

	@Override
	public String getLoreDisplayName() {
		return "Soul Jars";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("This curious jar radiates magic.", "You feel as if the inside is much bigger than the outside.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("This magic jar can store a soul like the soul gems the fey taught you about.", "However, these soul containers are not limitted to fey. They do seem to be limitted in other ways, however...");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		final Player playerIn = context.getPlayer();
		final InteractionHand hand = context.getHand();
		final Vec3 hitPos = context.getClickLocation();
		
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		ItemStack stack = playerIn.getItemInHand(hand);
		if (isFilled(stack)) {
			// Drop entity at the provided spot
			LivingEntity ent = spawnStoredEntity(stack, worldIn, hitPos.x, hitPos.y, hitPos.z);
			if (ent != null) {
				stack = clearEntity(stack);
				playerIn.setItemInHand(hand, stack);
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.FAIL;
			}
		}
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		if (playerIn.level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		if (!isFilled(stack)) {
			// Pick up fey, if it is one
			if (!(target instanceof LivingEntity)) {
				return InteractionResult.FAIL;
			}
			
			final ItemStack filled;
			if (target instanceof TamableAnimal) {
				TamableAnimal tameable = (TamableAnimal) target;
				if (tameable.isTame()) {
					filled = create(tameable);
				} else {
					filled = ItemStack.EMPTY;
				}
			} else if (target instanceof ITameableEntity) {
				ITameableEntity tameable = (ITameableEntity) target;
				if (tameable.isEntityTamed()) {
					filled = create(tameable);
				} else {
					filled = ItemStack.EMPTY;
				}
			} else {
				filled = ItemStack.EMPTY;
			}
			
			if (!filled.isEmpty()) {
				playerIn.setItemInHand(hand, filled);
				//target.remove();
				((ServerLevel) playerIn.level).removeEntity(target);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.FAIL;
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (isFilled(stack) && stack.hasTag()) {
			String name = stack.getTag().getString("name");
			if (name == null || name.isEmpty()) {
				name = "An unknown entity";
			}
			tooltip.add(new TextComponent(name).withStyle(ChatFormatting.AQUA));
		}
	}
	
}
