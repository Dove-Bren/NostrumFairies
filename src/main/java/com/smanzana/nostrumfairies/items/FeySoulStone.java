package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.utils.EntitySpawning;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Carries fey.
 * Regular carry all but fairies.
 * Gaels carry fairies.
 * Solidified Gaels are FairyGaels, not soul stones. Soul stones are fillable and emptiable.
 * @author Skyler
 *
 */
public class FeySoulStone extends Item implements ILoreTagged {

	public static enum SoulStoneType {
		GEM("gem", "soul_amethyst", ResidentType.GNOME, ResidentType.ELF, ResidentType.DWARF),
		GAEL("gael", "fairy_gael_basic", ResidentType.FAIRY);
		
		public final String suffix;
		public final String modelName;
		public final ResidentType[] types;
		
		private SoulStoneType(String suffix, String modelName, ResidentType ... types) {
			this.suffix = suffix;
			this.modelName = modelName;
			this.types = types;
		}
		
		public boolean canHold(ResidentType type) {
			for (ResidentType t : types) {
				if (t == type) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	//ItemStack p_174676_, @Nullable ClientLevel p_174677_, @Nullable LivingEntity p_174678_, int p_174679_
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelFilled(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
		return HasStoredFey(stack) ? 1.0F : 0.0F;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelType(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
		float val = 0.0F;
		final ResidentType feyType = getStoredFeyType(stack);
		if (feyType == null) {
			val = 0.0F;
		} else {
			switch (feyType) {
			case DWARF:
				val = 1.0F;
				break;
			case ELF:
				val = 2.0F;
				break;
			case FAIRY:
				val = 3.0F;
				break;
			case GNOME:
				val = 4.0F;
				break;
			}
		}
		
		return val;
	}
	
	public static final String ID_SOUL_GEM = "soul_amethyst";
	public static final String ID_SOUL_GAEL = "fairy_gael_basic";
	
	private static final String NBT_FEY_TYPE = "fey_type";
	private static final String NBT_FEY_NAME = "fey_name";
	private static final String NBT_FEY_DATA = "fey_data";
	
	private final SoulStoneType type;
	
	public FeySoulStone(SoulStoneType type) {
		super(FairyItems.PropUnstackable());
		this.type = type;
	}
	
	public static FeySoulStone getItem(SoulStoneType type) {
		switch (type) {
		case GAEL:
			return FairyItems.soulGael;
		case GEM:
			return FairyItems.soulGem;
		}
		
		return null;
	}
	
	public static final SoulStoneType getTypeOf(ItemStack stack) {
		return ((FeySoulStone) stack.getItem()).type;
	}
	
	protected @Nullable ResidentType getFeyType(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTag()) {
			return null;
		}
		
		CompoundTag tag = stack.getTag();
		ResidentType type = null;
		if (tag.contains(NBT_FEY_TYPE)) {
			try {
				type = ResidentType.valueOf(tag.getString(NBT_FEY_TYPE).toUpperCase());
			} catch (Exception e) {
				type = null;
			}
		}
		
		return type;
	}
	
	protected void setFeyType(ItemStack stack, ResidentType type) {
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		if (type != null) {
			tag.putString(NBT_FEY_TYPE, type.name());
		}
		
		stack.setTag(tag);
	}
	
	protected @Nullable CompoundTag getFeyData(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTag()) {
			return null;
		}
		
		return stack.getTag().getCompound(NBT_FEY_DATA);
	}
	
	protected void setFeyData(ItemStack stack, @Nullable CompoundTag data) {
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		if (data == null) {
			tag.remove(NBT_FEY_DATA);
		} else {
			tag.put(NBT_FEY_DATA, data);
		}
		
		stack.setTag(tag);
	}
	
	protected @Nullable String getFeyName(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTag()) {
			return null;
		}
		
		return stack.getTag().getString(NBT_FEY_NAME);
	}
	
	protected void setFeyName(ItemStack stack, String name) {
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		if (name == null) {
			tag.remove(NBT_FEY_NAME);
		} else {
			tag.putString(NBT_FEY_NAME, name);
		}
		
		stack.setTag(tag);
	}
	
	protected boolean hasStoredFey(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains(NBT_FEY_DATA);
	}
	
	public static final boolean HasStoredFey(ItemStack stack) {
		return ((FeySoulStone) stack.getItem()).hasStoredFey(stack);
	}
	
	public static final ResidentType getStoredFeyType(ItemStack stack) {
		return ((FeySoulStone) stack.getItem()).getFeyType(stack);
	}
	
	public static ItemStack create(SoulStoneType type) {
		return create(type, null, null, null);
	}
	
	public static ItemStack createEmpty(ResidentType type) {
		SoulStoneType soulType = SoulStoneType.GEM;
		for (SoulStoneType t : SoulStoneType.values()) {
			if (t.canHold(type)) {
				soulType = t;
				break;
			}
		}
		
		return create(soulType);
	}
	
	public static @Nonnull ItemStack create(SoulStoneType type, EntityFeyBase fey) {
		CompoundTag nbt = null;
		String name = null;
		ResidentType feyType = null;
		if (fey != null) {
			feyType = fey.getHomeType();
			if (!type.canHold(feyType)) {
				return ItemStack.EMPTY;
			}
			nbt = fey.serializeNBT();
			name = fey.getName().getString();
		}
		
		return create(type, name, feyType, nbt);
	}
	
	protected static ItemStack create(SoulStoneType type, String name, ResidentType residentType, CompoundTag nbt) {
		final FeySoulStone item = getItem(type);
		ItemStack stack = new ItemStack(item);
		item.setFeyType(stack, residentType);
		item.setFeyName(stack, name);
		item.setFeyData(stack, nbt);
		return stack;
	}
	
	public static ItemStack clearEntity(ItemStack stack) {
		return create(getTypeOf(stack));
	}
	
	public static @Nullable EntityFeyBase spawnStoredEntity(ItemStack stack, Level world, double x, double y, double z) {
		EntityFeyBase fey = null;
		CompoundTag feyData = ((FeySoulStone) stack.getItem()).getFeyData(stack);
		if (feyData != null) {
			Entity entity = EntitySpawning.readEntity(world, feyData, new Vec3(x, y, z));
			//Entity entity = AnvilChunkLoader.readWorldEntityPos(nbt.getCompound("data"), world, x, y, z, true);
			if (entity == null) {
				;
			} else if (entity instanceof EntityFeyBase) {
				fey = (EntityFeyBase) entity;
				world.addFreshEntity(fey);
			} else {
				entity.discard();//.isDead = true;
				//world.removeEntity(entity);
			}
		}
		return fey;
	}
	
	public static @Nonnull ItemStack storeEntity(ItemStack stack, EntityFeyBase fey) {
//		ResidentType feyType = fey.getHomeType();
//		SoulStoneType stoneType = getTypeOf(stack);
//		if (!stoneType.canHold(feyType)) {
//			return null;
//		}
//		
//		stack.setTagCompound(fey.writeToNBT(new CompoundNBT()));
//		return stack;
		SoulStoneType type = getTypeOf(stack);
		return create(type, fey);
	}
	
	public static ItemStack createFake(ResidentType type) {
		SoulStoneType soulType = SoulStoneType.GEM;
		for (SoulStoneType t : SoulStoneType.values()) {
			if (t.canHold(type)) {
				soulType = t;
				break;
			}
		}
		
		ItemStack stack = create(soulType);
		((FeySoulStone) stack.getItem()).setFeyType(stack, type);
		((FeySoulStone) stack.getItem()).setFeyData(stack, new CompoundTag()); // Empty data
		return stack;
	}
	
	public static CompoundTag getStoredEntityTag(ItemStack stack) {
		return ((FeySoulStone) stack.getItem()).getFeyData(stack);
	}
	
	public static String getStoredEntityName(ItemStack stack) {
		return ((FeySoulStone) stack.getItem()).getFeyName(stack);
	}
	
	@Override
	public String getLoreKey() {
		return "fey_soul_stone";
	}

	@Override
	public String getLoreDisplayName() {
		return "Soul Stones";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Mystical stones that can store the very essence of a fey creature.", "The fey have assured you they are safe to carry this way.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Mystical stones that can store the very essence of a fey creature.", "The fey have assured you they are safe to carry this way.", "Soul gems (empty or filled) can be added to empty slots in fey home blocks.");
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
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
		if (this.hasStoredFey(stack)) {
			// Drop entity at the provided spot
			EntityFeyBase fey = spawnStoredEntity(stack, worldIn, hitPos.x, hitPos.y, hitPos.z);
			if (fey != null) {
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
		
		if (!this.hasStoredFey(stack)) {
			// Pick up fey, if it is one
			if (!(target instanceof EntityFeyBase)) {
				return InteractionResult.FAIL;
			}
			
			if (target instanceof EntityPersonalFairy) {
				return InteractionResult.FAIL;
			}
			
			ItemStack newStack = storeEntity(stack, (EntityFeyBase) target);
			if (!newStack.isEmpty()) {
				playerIn.setItemInHand(hand, newStack);
				target.discard();
				//playerIn.world.removeEntity(target);
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.FAIL;
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (this.hasStoredFey(stack)) {
			String name = this.getFeyName(stack);
			if (name == null || name.isEmpty()) {
				name = "An unknown entity";
			}
			tooltip.add(new TextComponent(name).withStyle(ChatFormatting.AQUA));
			
			ResidentType type = getStoredFeyType(stack);
			if (type != null) {
				tooltip.add(new TextComponent(type.getSerializedName()).withStyle(ChatFormatting.DARK_AQUA));
			}
		}
	}
	
}
