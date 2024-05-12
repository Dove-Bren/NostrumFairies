package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.utils.EntitySpawning;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelFilled(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		return HasStoredFey(stack) ? 1.0F : 0.0F;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static final float ModelType(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
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
		
		CompoundNBT tag = stack.getTag();
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
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		if (type != null) {
			tag.putString(NBT_FEY_TYPE, type.name());
		}
		
		stack.setTag(tag);
	}
	
	protected @Nullable CompoundNBT getFeyData(ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTag()) {
			return null;
		}
		
		return stack.getTag().getCompound(NBT_FEY_DATA);
	}
	
	protected void setFeyData(ItemStack stack, @Nullable CompoundNBT data) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundNBT();
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
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundNBT();
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
		CompoundNBT nbt = null;
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
	
	protected static ItemStack create(SoulStoneType type, String name, ResidentType residentType, CompoundNBT nbt) {
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
	
	public static @Nullable EntityFeyBase spawnStoredEntity(ItemStack stack, World world, double x, double y, double z) {
		EntityFeyBase fey = null;
		CompoundNBT feyData = ((FeySoulStone) stack.getItem()).getFeyData(stack);
		if (feyData != null) {
			Entity entity = EntitySpawning.readEntity(world, feyData, new Vector3d(x, y, z));
			//Entity entity = AnvilChunkLoader.readWorldEntityPos(nbt.getCompound("data"), world, x, y, z, true);
			if (entity == null) {
				;
			} else if (entity instanceof EntityFeyBase) {
				fey = (EntityFeyBase) entity;
				world.addEntity(fey);
			} else {
				entity.remove();//.isDead = true;
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
		((FeySoulStone) stack.getItem()).setFeyData(stack, new CompoundNBT()); // Empty data
		return stack;
	}
	
	public static CompoundNBT getStoredEntityTag(ItemStack stack) {
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
		if (this.hasStoredFey(stack)) {
			// Drop entity at the provided spot
			EntityFeyBase fey = spawnStoredEntity(stack, worldIn, hitPos.x, hitPos.y, hitPos.z);
			if (fey != null) {
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
		
		if (!this.hasStoredFey(stack)) {
			// Pick up fey, if it is one
			if (!(target instanceof EntityFeyBase)) {
				return ActionResultType.FAIL;
			}
			
			if (target instanceof EntityPersonalFairy) {
				return ActionResultType.FAIL;
			}
			
			ItemStack newStack = storeEntity(stack, (EntityFeyBase) target);
			if (!newStack.isEmpty()) {
				playerIn.setHeldItem(hand, newStack);
				target.remove();
				//playerIn.world.removeEntity(target);
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.FAIL;
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (this.hasStoredFey(stack)) {
			String name = this.getFeyName(stack);
			if (name == null || name.isEmpty()) {
				name = "An unknown entity";
			}
			tooltip.add(new StringTextComponent(name).mergeStyle(TextFormatting.AQUA));
			
			ResidentType type = getStoredFeyType(stack);
			if (type != null) {
				tooltip.add(new StringTextComponent(type.getString()).mergeStyle(TextFormatting.DARK_AQUA));
			}
		}
	}
	
}
