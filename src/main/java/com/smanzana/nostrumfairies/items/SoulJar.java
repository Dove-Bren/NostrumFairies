package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.IEntityTameable;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Stores any tamed entity
 * @author Skyler
 *
 */
public class SoulJar extends Item implements ILoreTagged {

	public static final String ID = "soul_jar";
	
	private static SoulJar instance = null;
	public static SoulJar instance() {
		if (instance == null)
			instance = new SoulJar();
		
		return instance;
	}
	
	public static void init() {
		;
	}
	
	public SoulJar() {
		super();
		this.setUnlocalizedName(ID);
		this.setRegistryName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		this.setCreativeTab(NostrumFairies.creativeTab);
	}
	
	protected static final boolean filledFromMeta(int meta) {
		return (meta & 1) == 1;
	}

	protected static final int metaFromFilled(boolean filled) {
		return filled ? 1 : 0;
	}
	
	public static boolean hasEntity(@Nonnull ItemStack stack) {
		return !stack.isEmpty() && filledFromMeta(stack.getMetadata());
	}
	
	@SideOnly(Side.CLIENT)
	public String getModelName(ItemStack stack) {
		return ID + (hasEntity(stack) ? "_filled" : "");
	}
	
	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
			subItems.add(createFake(false));
			subItems.add(createFake(true));
		}
	}
	
	protected static ItemStack createInternal(EntityLivingBase entity) {
		ItemStack stack = createFake(true);
		setStoredEntity(stack, entity);
		return stack;
	}
	
	public static ItemStack create(EntityTameable entity) {
		return createInternal(entity);
	}
	
	public static ItemStack create(IEntityTameable entity) {
		return createInternal((EntityLivingBase) entity);
	}
	
	public static ItemStack createFake(boolean filled) {
		return new ItemStack(instance(), 1, metaFromFilled(filled));
	}
	
	protected static void setStoredEntity(ItemStack stack, @Nullable EntityLivingBase entity) {
		NBTTagCompound tag = null; // create a new one to discard old entity if passed in null
		if (entity != null) {
			tag = new NBTTagCompound();
			tag.setTag("entity", entity.serializeNBT());
			tag.setString("name", entity.getName());
		}
		stack.setTagCompound(tag);
	}
	
	protected static @Nullable EntityLivingBase spawnStoredEntity(ItemStack stack, World world, double x, double y, double z) {
		EntityLivingBase ent = null;
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null && nbt.hasKey("entity", NBT.TAG_COMPOUND)) {
			Entity entity = AnvilChunkLoader.readWorldEntityPos(nbt.getCompoundTag("entity"), world, x, y, z, true);
			if (entity == null) {
				;
			} else if (entity instanceof EntityLivingBase) {
				ent = (EntityLivingBase) entity;
			} else {
				entity.isDead = true;
				world.removeEntity(entity);
			}
		}
		return ent;
	}
	
	public static @Nullable String getStoredEntityName(@Nonnull ItemStack stack) {
		if (stack.isEmpty() || !stack.hasTagCompound()) {
			return null;
		}
		
		return stack.getTagCompound().getString("name");
	}
	
	public static ItemStack clearEntity(ItemStack stack) {
		return createFake(false);
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
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		
		ItemStack stack = playerIn.getHeldItem(hand);
		if (filledFromMeta(stack.getMetadata())) {
			// Drop entity at the provided spot
			EntityLivingBase ent = spawnStoredEntity(stack, worldIn, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ);
			if (ent != null) {
				stack = clearEntity(stack);
				playerIn.setHeldItem(hand, stack);
				return EnumActionResult.SUCCESS;
			} else {
				return EnumActionResult.FAIL;
			}
		}
		return EnumActionResult.PASS;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		if (playerIn.world.isRemote) {
			return true;
		}
		
		if (!filledFromMeta(stack.getMetadata())) {
			// Pick up fey, if it is one
			if (!(target instanceof EntityLivingBase)) {
				return false;
			}
			
			final ItemStack filled;
			if (target instanceof EntityTameable) {
				EntityTameable tameable = (EntityTameable) target;
				if (tameable.isTamed()) {
					filled = create(tameable);
				} else {
					filled = ItemStack.EMPTY;
				}
			} else if (target instanceof IEntityTameable) {
				IEntityTameable tameable = (IEntityTameable) target;
				if (tameable.isTamed()) {
					filled = create(tameable);
				} else {
					filled = ItemStack.EMPTY;
				}
			} else {
				filled = ItemStack.EMPTY;
			}
			
			if (!filled.isEmpty()) {
				playerIn.setHeldItem(hand, filled);
				playerIn.world.removeEntityDangerously(target);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (filledFromMeta(stack.getMetadata()) && stack.hasTagCompound()) {
			String name = stack.getTagCompound().getString("name");
			if (name == null || name.isEmpty()) {
				name = "An unknown entity";
			}
			tooltip.add(ChatFormatting.AQUA + name + ChatFormatting.RESET);
		}
	}
	
}
