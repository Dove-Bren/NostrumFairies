package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.utils.EntitySpawning;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.petcommand.api.entity.ITameableEntity;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Stores any tamed entity
 * @author Skyler
 *
 */
public class SoulJar extends Item implements ILoreTagged {
	
	public static final float ModelFilled(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
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
	
	public static ItemStack create(TameableEntity entity) {
		return createInternal(entity);
	}
	
	public static ItemStack create(ITameableEntity entity) {
		return createInternal((LivingEntity) entity);
	}
	
	public static ItemStack createFakeFilled() {
		ItemStack stack = new ItemStack(FairyItems.soulJar);
		setStoredEntityData(stack, new CompoundNBT(), null);
		return stack;
	}
	
	protected static void setStoredEntityData(ItemStack stack, @Nullable CompoundNBT data, @Nullable String name) {
		CompoundNBT tag = null; // create a new one to discard old entity if passed in null
		if (data != null) {
			tag = new CompoundNBT();
			tag.put("entity", data);
			tag.putString("name", name == null ? "" : name);
		}
		stack.setTag(tag);
	}
	
	protected static void setStoredEntity(ItemStack stack, @Nullable LivingEntity entity) {
		final CompoundNBT data = entity == null ? null : entity.serializeNBT();
		final String name = entity == null ? null : entity.getDisplayName().getString();
		setStoredEntityData(stack, data, name);
	}
	
	protected static @Nullable LivingEntity spawnStoredEntity(ItemStack stack, World world, double x, double y, double z) {
		LivingEntity ent = null;
		CompoundNBT nbt = stack.getTag();
		if (nbt != null && nbt.contains("entity", NBT.TAG_COMPOUND)) {
			Entity entity = EntitySpawning.readEntity(world, nbt.getCompound("entity"), new Vector3d(x, y, z));
			if (entity == null) {
				;
			} else if (entity instanceof LivingEntity) {
				ent = (LivingEntity) entity;
				world.addEntity(ent);
			} else {
				entity.remove();
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
		return stack.hasTag() && stack.getTag().contains("entity", NBT.TAG_COMPOUND);
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
	public ActionResultType onItemUse(ItemUseContext context) {
		final World worldIn = context.getWorld();
		final PlayerEntity playerIn = context.getPlayer();
		final Hand hand = context.getHand();
		final Vector3d hitPos = context.getHitVec();
		
		if (worldIn.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		ItemStack stack = playerIn.getHeldItem(hand);
		if (isFilled(stack)) {
			// Drop entity at the provided spot
			LivingEntity ent = spawnStoredEntity(stack, worldIn, hitPos.x, hitPos.y, hitPos.z);
			if (ent != null) {
				stack = clearEntity(stack);
				playerIn.setHeldItem(hand, stack);
				return ActionResultType.SUCCESS;
			} else {
				return ActionResultType.FAIL;
			}
		}
		return ActionResultType.PASS;
	}
	
	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		if (playerIn.world.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		if (!isFilled(stack)) {
			// Pick up fey, if it is one
			if (!(target instanceof LivingEntity)) {
				return ActionResultType.FAIL;
			}
			
			final ItemStack filled;
			if (target instanceof TameableEntity) {
				TameableEntity tameable = (TameableEntity) target;
				if (tameable.isTamed()) {
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
				playerIn.setHeldItem(hand, filled);
				//target.remove();
				((ServerWorld) playerIn.world).removeEntity(target);
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.FAIL;
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (isFilled(stack) && stack.hasTag()) {
			String name = stack.getTag().getString("name");
			if (name == null || name.isEmpty()) {
				name = "An unknown entity";
			}
			tooltip.add(new StringTextComponent(name).mergeStyle(TextFormatting.AQUA));
		}
	}
	
}
