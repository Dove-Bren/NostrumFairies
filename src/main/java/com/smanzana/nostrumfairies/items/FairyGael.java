package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.nostrumaetheria.api.proxy.APIProxy;
import com.smanzana.nostrumaetheria.api.recipes.IAetherRepairerRecipe;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.serializers.FairyJob;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Like SoulStone#Gael's, carry fairies. However, these are solidified and cannot be emptied.
 * @author Skyler
 *
 */
public class FairyGael extends Item implements ILoreTagged {

	public static enum FairyGaelType {
		ATTACK("attack"),
		LOGISTICS("logistics"),
		BUILD("build");
		
		public final String suffix;
		
		private FairyGaelType(String suffix) {
			this.suffix = suffix;
		}
	}
	
	public static final String ID = "fairy_gael";
	
	private static FairyGael instance = null;
	public static FairyGael instance() {
		if (instance == null)
			instance = new FairyGael();
		
		return instance;
	}
	
	public static void registerRecipes() {
		APIProxy.addRepairerRecipe(new IAetherRepairerRecipe() {
			@Override
			public boolean matches(ItemStack stack) {
				return stack.getItem() instanceof FairyGael
						&& isCracked(stack);
			}
			
			@Override
			public int getAetherCost(ItemStack stack) {
				return 500;
			}

			@Override
			public ItemStack repair(ItemStack stack) {
				uncrack(stack);
				return stack;
			}
		});
	}
	
	public FairyGael() {
		super();
		this.setUnlocalizedName(ID);
		this.setRegistryName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		this.setCreativeTab(NostrumFairies.creativeTab);
	}
	
	protected static final int metaFromTypes(boolean cracked, FairyGaelType type) {
		return (type.ordinal() << 1) | (cracked ? 1 : 0);
	}
	
	protected static final FairyGaelType typeFromMeta(int meta) {
		int raw = meta >> 1;
		return FairyGaelType.values()[raw % FairyGaelType.values().length];
	}
	
	protected static final boolean crackedFromMeta(int meta) {
		return (meta & 1) == 1;
	}
	
	public static final FairyGaelType getTypeOf(ItemStack stack) {
		return typeFromMeta(stack.getMetadata());
	}
	
	public static final boolean isCracked(ItemStack stack) {
		return crackedFromMeta(stack.getMetadata());
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		FairyGaelType type = getTypeOf(stack);
		return this.getUnlocalizedName() + "." + type.suffix;
	}
	
	@SideOnly(Side.CLIENT)
	public String getModelName(ItemStack stack) {
		return getModelName(getTypeOf(stack), isCracked(stack));
	}
	
	@SideOnly(Side.CLIENT)
	public String getModelName(FairyGaelType type, boolean cracked) {
		return ID + "_" + type.suffix + (cracked ? "_cracked" : "");
	}
	
	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (this.isInCreativeTab(tab)) {
			for (FairyGaelType type : FairyGaelType.values()) {
				subItems.add(create(type, null, false));
			}
			
			for (FairyGaelType type : FairyGaelType.values()) {
				subItems.add(create(type, null, true));
			}
		}
	}
	
	public static @Nonnull ItemStack create(FairyGaelType type, EntityPersonalFairy fey) {
		return create(type, fey, false);
	}
	
	public static @Nonnull ItemStack create(FairyGaelType type, EntityPersonalFairy fey, boolean cracked) {
		ItemStack stack = new ItemStack(instance(), 1, metaFromTypes(cracked, type));
		setStoredEntity(stack, fey);
		return stack;
	}
	
	public static void initGael(ItemStack stack, World world) {
		// For some easy creative intergration
		if (!stack.hasTagCompound()) {
			FairyGaelType type = getTypeOf(stack);
			EntityPersonalFairy fairy = new EntityPersonalFairy(world);
			switch (type) {
			case ATTACK:
			default:
				fairy.setJob(FairyJob.WARRIOR);
				break;
			case BUILD:
				fairy.setJob(FairyJob.BUILDER);
				break;
			case LOGISTICS:
				fairy.setJob(FairyJob.LOGISTICS);
				break;
			}
			
			if (world == null) {
				world = DimensionManager.getWorld(0);
			}
			
			fairy.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(0, 10, 0)), (IEntityLivingData)null);
			setStoredEntity(stack, fairy);
		}
	}
	
	public static @Nullable EntityPersonalFairy spawnStoredEntity(ItemStack stack, World world, double x, double y, double z) {
		EntityPersonalFairy fey = null;
		
		initGael(stack, world);
		
		NBTTagCompound nbt = stack.getTagCompound();
		Entity entity = AnvilChunkLoader.readWorldEntityPos(nbt.getCompoundTag("data"), world, x, y, z, true);
		if (entity != null) {
			if (entity instanceof EntityFairy && !(entity instanceof EntityPersonalFairy)) {
				// Upgrade!
				entity = ((EntityFairy) entity).promotoToPersonal();
			}
			
			if (entity instanceof EntityPersonalFairy) {
				fey = (EntityPersonalFairy) entity;
				
				// update energy and health
				float healthPerc = (float) getStoredHealth(stack);
				fey.setHealth(fey.getMaxHealth() * healthPerc);
				float energyPerc = (float) getStoredEnergy(stack);
				fey.setEnergy(fey.getMaxEnergy() * energyPerc);
			} else {
				// WAnt to error, but this will probably spam
				entity.isDead = true;
				world.removeEntity(entity);
			}
		}
		return fey;
	}
	
	public static void setStoredEntity(ItemStack stack, EntityPersonalFairy fey) {
		if (fey != null) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("name", fey.getName());
			tag.setDouble("healthD", (double) fey.getHealth() / Math.max(1, (double) fey.getMaxHealth()));
			tag.setDouble("energyD", (double) fey.getEnergy() / Math.max(1, (double) fey.getMaxEnergy()));
			tag.setTag("data", fey.serializeNBT());
			stack.setTagCompound(tag);
		} else {
			stack.setTagCompound(null);
		}
	}
	
	public static String getStoredName(ItemStack stack) {
		if (stack.hasTagCompound()) {
			return stack.getTagCompound().getString("name");
		}
		
		return null;
	}
	
	public static double getStoredHealth(ItemStack stack) {
		if (stack.hasTagCompound()) {
			return stack.getTagCompound().getDouble("healthD");
		}
		
		return 0f;
	}
	
	public static double getStoredEnergy(ItemStack stack) {
		if (stack.hasTagCompound()) {
			return stack.getTagCompound().getDouble("energyD");
		}
		
		return 0f;
	}
	
	/**
	 * Regenerate some health and energy for the stored gael.
	 * @param gael
	 * @param potency Relative efficiency. 1f is standard.
	 */
	public static void regenFairy(ItemStack gael, float potency) {
		if (gael.isEmpty() || isCracked(gael)) {
			return;
		}
		
		double energy = getStoredEnergy(gael) + (NostrumFairies.random.nextDouble() * .00085 * potency);
		double health = getStoredHealth(gael) + (NostrumFairies.random.nextDouble() * .0002 * potency);
		NBTTagCompound tag = gael.getTagCompound();
		if (tag == null) {
			initGael(gael, null);
			tag = gael.getTagCompound();
		}
		tag.setDouble("energyD", Math.min(1, energy));
		tag.setDouble("healthD", Math.min(1, health));
	}
	
	public static void crack(ItemStack stack) {
		FairyGaelType type = getTypeOf(stack);
		stack.setItemDamage(metaFromTypes(true, type));
	}
	
	public static void uncrack(ItemStack stack) {
		FairyGaelType type = getTypeOf(stack);
		stack.setItemDamage(metaFromTypes(false, type));
	}
	
	public static ItemStack upgrade(FairyGaelType type, ItemStack soulStone) {
		if (soulStone.isEmpty() || !(soulStone.getItem() instanceof FeySoulStone) || !FeySoulStone.hasStoredFey(soulStone)) {
			return ItemStack.EMPTY;
		}
		ItemStack gael = new ItemStack(instance(), 1, metaFromTypes(false, type));
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("data", FeySoulStone.getStoredEntityTag(soulStone));
		tag.setString("name", FeySoulStone.getStoredEntityName(soulStone));
		gael.setTagCompound(tag);
		return gael;
	}
	
	@Override
	public String getLoreKey() {
		return "fairy_gaels";
	}

	@Override
	public String getLoreDisplayName() {
		return "Solidified Fairy Gaels";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("A Fairy allowed you to seal it inside.", "The gael has been infused and tinted.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("A Fairy allowed you to seal it inside.", "The gael has been infused and tinted.", "Sealed gaels can be organized by using a fairy instrument.", "Gaels shatter if the fairy inside runs out of life energy. Luckily, life can be restored by putting the gael in an Aether Repairer!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) { 
		return EnumActionResult.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		return ActionResult.newResult(EnumActionResult.PASS, playerIn.getHeldItem(hand));
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (isCracked(stack)) {
			tooltip.add(ChatFormatting.DARK_RED + I18n.format("info.fairy_gael.cracked") + ChatFormatting.RESET);
		}
		if (stack.hasTagCompound()) {
			String name = getStoredName(stack);
			if (name == null || name.isEmpty()) {
				name = "An unknown entity";
			}
			tooltip.add(ChatFormatting.AQUA + name + ChatFormatting.RESET);
		}
	}
	
}
