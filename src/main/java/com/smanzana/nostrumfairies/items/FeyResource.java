package com.smanzana.nostrumfairies.items;

import java.util.List;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FeyBush;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.fey.EntityShadowFey;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.golem.EntityGolem;
import com.smanzana.nostrummagica.listeners.PlayerListener.Event;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FeyResource extends Item implements ILoreTagged {

	public static enum FeyResourceType {
		TEARS("tears", "fey_tears"),
		ESSENCE("essence", "fey_essence"),
		ESSENCE_CORRUPTED("essence_corrupted", "fey_essence_corrupted"),
		BELL("bell", "fey_bell"),
		FLOWER("flower", "fey_flower"),
		TABLET("tablet", "fey_tablet"),
		GOLEM_TOKEN("golem_token", "golem_token"),
		LOGIC_TOKEN("logic_token", "logic_token");
		
		private final String suffix;
		private final String model;
		
		private FeyResourceType(String suffix, String model) {
			this.suffix = suffix;;
			this.model = model;
		}
		
		public String getSuffix() {
			return suffix;
		}
		
		public String getModelName() {
			return model;
		}
	}
	
	public static final String ID = "fey_resource";
	
	private static FeyResource instance = null;
	public static FeyResource instance() {
		if (instance == null)
			instance = new FeyResource();
		
		return instance;
	}
	
	public static void init() {
		;
	}
	
	public FeyResource() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(64);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setHasSubtypes(true);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		
		String suffix = typeFromMeta(i).getSuffix();
		
		return this.getUnlocalizedName() + "." + suffix;
	}
	
	@SideOnly(Side.CLIENT)
	public String getModelName(FeyResourceType type) {
		return type.getModelName();
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	for (FeyResourceType type: FeyResourceType.values()) {
    		subItems.add(create(type, 1));
    	}
	}
    
    public static ItemStack create(FeyResourceType type, int count) {
    	return new ItemStack(instance(), count, metaFromType(type));
    }
	
	protected static int metaFromType(FeyResourceType type) {
		return type.ordinal();
	}
	
	protected FeyResourceType typeFromMeta(int meta) {
		return FeyResourceType.values()[meta % FeyResourceType.values().length];
	}
	
    @Override
	public String getLoreKey() {
		return "fey_resource";
	}

	@Override
	public String getLoreDisplayName() {
		return "Fey Essences";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("The darkened, corrupted essence you've found on the shadow fey reeks of evil. Yet you can sense something more pure underneath...");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Fey essence (corrupted, or purified) is a wild new substance. You can feel the magical energy radiating off of it.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}

	public FeyResourceType getType(ItemStack stack) {
		return typeFromMeta(stack.getMetadata());
	}
	
	protected void spawnFey(World worldIn, BlockPos at) {
		final EntityFeyBase fey;
		switch (NostrumFairies.random.nextInt(5)) {
		case 0:
			fey = new EntityFairy(worldIn);
			break;
		case 1:
			fey = new EntityDwarf(worldIn);
			break;
		case 2:
			fey = new EntityGnome(worldIn);
			break;
		case 3:
			fey = new EntityElf(worldIn);
			break;
		case 4:
		default:
			fey = new EntityElfArcher(worldIn);
			break;
		}
		fey.setPosition(at.getX() + .5, at.getY(), at.getZ() + .5);
		fey.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(fey)), (IEntityLivingData)null);
		
		worldIn.spawnEntityInWorld(fey);
		
		((WorldServer) worldIn).spawnParticle(EnumParticleTypes.END_ROD,
				at.getX() + .5,
				at.getY() + .25,
				at.getZ() + .5,
				100,
				.25,
				.4,
				.25,
				.05,
				new int[0]);
	}
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		FeyResourceType type = getType(stack);
		
		if (type == FeyResourceType.FLOWER) {
			
			if (!worldIn.isRemote) {
				spawnFey(worldIn, pos.up());
				int count = NostrumFairies.random.nextInt(3) + 1;
				MutableBlockPos cursor = new MutableBlockPos();
				for (int i = 0; i < count; i++) {
					cursor.setPos(pos.up()).move(EnumFacing.HORIZONTALS[NostrumFairies.random.nextInt(4)], NostrumFairies.random.nextInt(2) + 1);
					for (int j = 0; j < 5; j++) {
						if (worldIn.isAirBlock(cursor)) {
							if (FeyBush.instance().canPlaceBlockAt(worldIn, cursor)) {
								// Found a spot!
								worldIn.setBlockState(cursor.toImmutable(), FeyBush.instance().getDefaultState());
								((WorldServer) worldIn).spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
										cursor.getX() + .5,
										cursor.getY() + .25,
										cursor.getZ() + .5,
										20,
										.25,
										1,
										.25,
										0,
										new int[0]);
								break;
							} else {
								cursor.move(EnumFacing.DOWN);
							}
						} else {
							cursor.move(EnumFacing.UP);
						}
					}
					
				}
			}
			
			stack.stackSize--;
			return EnumActionResult.SUCCESS;
		}
		
		return EnumActionResult.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand) {
		FeyResourceType type = getType(stack);
		if (type == FeyResourceType.TABLET) {
			if (!worldIn.isRemote) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
				if (attr != null) {
					if (!attr.hasLore(FeyFriendLore.instance)) {
						attr.giveFullLore(FeyFriendLore.instance());
						stack.stackSize--;
						return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
					} else {
						playerIn.addChatComponentMessage(new TextComponentTranslation("info.tablet.fail"));
						return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
					}
				}
			} else {
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
		} else if (type == FeyResourceType.BELL) {
			if (!worldIn.isRemote) {
				for (EntityShadowFey ent : worldIn.getEntitiesWithinAABB(EntityShadowFey.class, Block.FULL_BLOCK_AABB.offset(playerIn.posX, playerIn.posY, playerIn.posZ).expandXyz(30))) {
					ent.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("glowing"), 20 * 5));
					NostrumMagica.playerListener.registerTimer((/*Event*/ eType, /*EntityLivingBase*/ entity, /*Object*/ data) -> {
						if (eType == Event.TIME) {
							NostrumFairiesSounds.BELL.play(worldIn, ent.posX, ent.posY, ent.posZ);
						}
						return true;
					}, 10, 10);
				}
				
				NostrumFairiesSounds.BELL.play(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ);
			}
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}
		
		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}
	
	@SubscribeEvent
	public void onMobDrop(LivingDropsEvent event) {
		final float chance;
		if (event.getEntityLiving() instanceof EntityIronGolem) {
			chance = .2f;
		} else if (event.getEntityLiving() instanceof EntityGolem) {
			chance = .01f;
		} else {
			chance = 0f;
		}
		
		if (chance == 0f) {
			return;
		}
		
		for (int i = 0; i <= event.getLootingLevel(); i++) {
			if (NostrumFairies.random.nextFloat() < chance) {
				EntityItem entity = new EntityItem(event.getEntity().worldObj,
						event.getEntity().posX,
						event.getEntity().posY,
						event.getEntity().posZ,
						create(FeyResourceType.GOLEM_TOKEN, 1));
				event.getDrops().add(entity);
			}
		}
	}
	
	public static final class FeyFriendLore implements ILoreTagged {
		
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
		public InfoScreenTabs getTab() {
			return InfoScreenTabs.INFO_ENTITY;
		}
	}
}
