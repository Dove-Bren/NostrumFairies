package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	
	public static final String ID = "fey_soul_stone";
	
	private static FeySoulStone instance = null;
	public static FeySoulStone instance() {
		if (instance == null)
			instance = new FeySoulStone();
		
		return instance;
	}
	
	public static void init() {
		;
	}
	
	public FeySoulStone() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
	}
	
	protected static final int metaFromTypes(boolean filled, SoulStoneType type) {
		return (type.ordinal() << 1) | (filled ? 1 : 0);
	}
	
	protected static final boolean filledFromMeta(int meta) {
		return (meta & 1) == 1;
	}
	
	protected static final SoulStoneType typeFromMeta(int meta) {
		int raw = (meta >> 1);
		return SoulStoneType.values()[raw % SoulStoneType.values().length];
	}
	
	public static final SoulStoneType getTypeOf(ItemStack stack) {
		return typeFromMeta(stack.getMetadata());
	}
	
	public static final boolean hasStoredFey(ItemStack stack) {
		return filledFromMeta(stack.getMetadata()) && stack.hasTagCompound();
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		SoulStoneType type = getTypeOf(stack);
		return this.getUnlocalizedName() + "." + type.suffix;
	}
	
	@SideOnly(Side.CLIENT)
	public String getModelName(ItemStack stack) {
		SoulStoneType type = getTypeOf(stack);
		return type.modelName;
	}
	
	@SideOnly(Side.CLIENT)
	public String getModelName(SoulStoneType type) {
		return type.modelName;
	}
	
	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (SoulStoneType type : SoulStoneType.values()) {
			subItems.add(create(type));
		}
	}
	
	public static ItemStack create(SoulStoneType type) {
		return create(type, null, null);
	}
	
	public static @Nullable ItemStack create(SoulStoneType type, EntityFeyBase fey) {
		NBTTagCompound nbt = null;
		String name = null;
		if (fey != null) {
			ResidentType feyType = fey.getHomeType();
			if (!type.canHold(feyType)) {
				return null;
			}
			nbt = fey.serializeNBT();
			name = fey.getName();
		}
		
		return create(type, name, nbt);
	}
	
	protected static ItemStack create(SoulStoneType type, String name, NBTTagCompound nbt) {
		ItemStack stack = new ItemStack(instance(), 1, metaFromTypes(nbt != null, type));
		if (nbt != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("name", name);
			tag.setTag("data", nbt);
			stack.setTagCompound(tag);
		}
		return stack;
	}
	
	public static ItemStack clearEntity(ItemStack stack) {
		return create(getTypeOf(stack));
	}
	
	public static @Nullable EntityFeyBase spawnStoredEntity(ItemStack stack, World world, double x, double y, double z) {
		EntityFeyBase fey = null;
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null && nbt.hasKey("data", NBT.TAG_COMPOUND)) {
			Entity entity = AnvilChunkLoader.readWorldEntityPos(nbt.getCompoundTag("data"), world, x, y, z, true);
			if (entity == null) {
				;
			} else if (entity instanceof EntityFeyBase) {
				fey = (EntityFeyBase) entity;
			} else {
				entity.isDead = true;
				world.removeEntity(entity);
			}
		}
		return fey;
	}
	
	public static @Nullable ItemStack storeEntity(ItemStack stack, EntityFeyBase fey) {
//		ResidentType feyType = fey.getHomeType();
//		SoulStoneType stoneType = getTypeOf(stack);
//		if (!stoneType.canHold(feyType)) {
//			return null;
//		}
//		
//		stack.setTagCompound(fey.writeToNBT(new NBTTagCompound()));
//		return stack;
		SoulStoneType type = getTypeOf(stack);
		return create(type, fey);
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
		return new Lore().add("");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		
		if (filledFromMeta(stack.getMetadata())) {
			// Drop entity at the provided spot
			EntityFeyBase fey = spawnStoredEntity(stack, worldIn, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ);
			if (fey != null) {
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
		if (playerIn.worldObj.isRemote) {
			return true;
		}
		
		if (!filledFromMeta(stack.getMetadata())) {
			// Pick up fey, if it is one
			if (!(target instanceof EntityFeyBase)) {
				return false;
			}
			
			ItemStack newStack = storeEntity(stack, (EntityFeyBase) target);
			if (newStack != null) {
				playerIn.setHeldItem(hand, newStack);
				playerIn.worldObj.removeEntity(target);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (filledFromMeta(stack.getMetadata()) && stack.hasTagCompound()) {
			String name = stack.getTagCompound().getString("name");
			if (name == null || name.isEmpty()) {
				name = "An unknown entity";
			}
			tooltip.add(ChatFormatting.AQUA + name + ChatFormatting.RESET);
		}
	}
	
}
