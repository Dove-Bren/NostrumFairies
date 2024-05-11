package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumaetheria.api.proxy.APIProxy;
import com.smanzana.nostrumaetheria.api.recipes.IAetherRepairerRecipe;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.serializers.FairyJob;
import com.smanzana.nostrumfairies.utils.EntitySpawning;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	
	protected static final String ID_BASE = "fairy_gael_";
	public static final String ID_ATTACK = ID_BASE + "attack";
	public static final String ID_LOGISTICS = ID_BASE + "logistics";
	public static final String ID_BUILD = ID_BASE + "build";
	
	private static final String NBT_CRACKED = "cracked";
	
	public static void registerRecipes() {
		APIProxy.addRepairerRecipe(new IAetherRepairerRecipe() {
			@Override
			public boolean matches(ItemStack stack) {
				return stack.getItem() instanceof FairyGael
						&& ((FairyGael) stack.getItem()).isCracked(stack);
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
	
	private final FairyGaelType type;
	
	public FairyGael(FairyGaelType type) {
		super(FairyItems.PropUnstackable());
		this.type = type;
	}
	
	public boolean isCracked(ItemStack stack) {
		return !stack.isEmpty() && stack.hasTag()
				&& stack.getTag().getBoolean(NBT_CRACKED);
	}
	
	public void setCracked(ItemStack stack, boolean cracked) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		tag.putBoolean(NBT_CRACKED, cracked);
		stack.setTag(tag);
	}
	
	public static final FairyGaelType getTypeOf(ItemStack stack) {
		return ((FairyGael) stack.getItem()).type;
	}
	
	public static FairyGael getItem(FairyGaelType type) {
		switch (type) {
		case ATTACK:
			return FairyItems.attackGael;
		case BUILD:
			return FairyItems.buildGael;
		case LOGISTICS:
			return FairyItems.logisticsGael;
		}
		
		return null;
	}
	
	public static @Nonnull ItemStack create(FairyGaelType type, EntityPersonalFairy fey) {
		return create(type, fey, false);
	}
	
	public static @Nonnull ItemStack create(FairyGaelType type, EntityPersonalFairy fey, boolean cracked) {
		FairyGael item = getItem(type);
		ItemStack stack = new ItemStack(item);
		item.setCracked(stack, cracked);
		setStoredEntity(stack, fey);
		return stack;
	}
	
	public static void initGael(ItemStack stack, @Nonnull ServerWorld world) {
		// For some easy creative intergration
		if (!stack.hasTag()) {
			FairyGaelType type = getTypeOf(stack);
			EntityPersonalFairy fairy = new EntityPersonalFairy(FairyEntities.PersonalFairy, world);
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
			
			fairy.onInitialSpawn(world, world.getDifficultyForLocation(new BlockPos(0, 10, 0)), SpawnReason.MOB_SUMMONED, (ILivingEntityData) null, null);
			setStoredEntity(stack, fairy);
		}
	}
	
	public static @Nullable EntityPersonalFairy spawnStoredEntity(ItemStack stack, ServerWorld world, double x, double y, double z) {
		EntityPersonalFairy fey = null;
		
		initGael(stack, world);
		
		CompoundNBT nbt = stack.getTag();
		Entity entity = EntitySpawning.readEntity(world, nbt.getCompound("data"), new Vector3d(x, y, z));
		if (entity != null) {
			world.addEntity(entity);
			
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
				entity.remove();
				//world.removeEntity(entity);
			}
		}
		return fey;
	}
	
	public static void setStoredEntity(ItemStack stack, EntityPersonalFairy fey) {
		if (fey != null) {
			CompoundNBT tag = new CompoundNBT();
			tag.putString("name", fey.getName().getString());
			tag.putDouble("healthD", (double) fey.getHealth() / Math.max(1, (double) fey.getMaxHealth()));
			tag.putDouble("energyD", (double) fey.getEnergy() / Math.max(1, (double) fey.getMaxEnergy()));
			tag.put("data", fey.serializeNBT());
			stack.setTag(tag);
		} else {
			stack.setTag(null);
		}
	}
	
	public static String getStoredName(ItemStack stack) {
		if (stack.hasTag()) {
			return stack.getTag().getString("name");
		}
		
		return null;
	}
	
	public static double getStoredHealth(ItemStack stack) {
		if (stack.hasTag()) {
			return stack.getTag().getDouble("healthD");
		}
		
		return 0f;
	}
	
	public static double getStoredEnergy(ItemStack stack) {
		if (stack.hasTag()) {
			return stack.getTag().getDouble("energyD");
		}
		
		return 0f;
	}
	
	/**
	 * Regenerate some health and energy for the stored gael.
	 * @param gael
	 * @param potency Relative efficiency. 1f is standard.
	 */
	public static void regenFairy(ServerWorld world, ItemStack gael, float potency) {
		if (gael.isEmpty() || ((FairyGael) gael.getItem()).isCracked(gael)) {
			return;
		}
		
		double energy = getStoredEnergy(gael) + (NostrumFairies.random.nextDouble() * .00085 * potency);
		double health = getStoredHealth(gael) + (NostrumFairies.random.nextDouble() * .0002 * potency);
		CompoundNBT tag = gael.getTag();
		if (tag == null) {
			initGael(gael, world);
			tag = gael.getTag();
		}
		tag.putDouble("energyD", Math.min(1, energy));
		tag.putDouble("healthD", Math.min(1, health));
	}
	
	public static void crack(ItemStack stack) {
		((FairyGael) stack.getItem()).setCracked(stack, true);
	}
	
	public static void uncrack(ItemStack stack) {
		((FairyGael) stack.getItem()).setCracked(stack, false);
	}
	
	public static ItemStack upgrade(FairyGaelType type, ItemStack soulStone) {
		if (soulStone.isEmpty() || !(soulStone.getItem() instanceof FeySoulStone) || !((FeySoulStone) soulStone.getItem()).hasStoredFey(soulStone)) {
			return ItemStack.EMPTY;
		}
		ItemStack gael = new ItemStack(getItem(type));
		CompoundNBT tag = new CompoundNBT();
		tag.put("data", FeySoulStone.getStoredEntityTag(soulStone));
		tag.putString("name", FeySoulStone.getStoredEntityName(soulStone));
		gael.setTag(tag);
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
	public ActionResultType onItemUse(ItemUseContext context) { 
		return ActionResultType.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		return ActionResult.resultPass(playerIn.getHeldItem(hand));
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (isCracked(stack)) {
			tooltip.add(new TranslationTextComponent("info.fairy_gael.cracked").mergeStyle(TextFormatting.DARK_RED));
		}
		if (stack.hasTag()) {
			String name = getStoredName(stack);
			if (name == null || name.isEmpty()) {
				name = "An unknown entity";
			}
			tooltip.add(new StringTextComponent(name).mergeStyle(TextFormatting.AQUA));
		}
	}
	
}
