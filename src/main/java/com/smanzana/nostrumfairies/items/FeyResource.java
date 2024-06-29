package com.smanzana.nostrumfairies.items;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.entity.FairyEntities;
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
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID)
public class FeyResource extends Item implements ILoreTagged {

	public static enum FeyResourceType {
		TEARS("tears"),
		ESSENCE("essence"),
		ESSENCE_CORRUPTED("essence_corrupted"),
		BELL("bell"),
		FLOWER("flower"),
		TABLET("tablet"),
		GOLEM_TOKEN("golem_token"),
		LOGIC_TOKEN("logic_token");
		
		private final String suffix;
		
		private FeyResourceType(String suffix) {
			this.suffix = suffix;;
		}
		
		public String getSuffix() {
			return suffix;
		}
	}
	
	public static final String ID_TEARS = "fey_tears";
	public static final String ID_ESSENCE = "fey_essence";
	public static final String ID_ESSENCE_CORRUPTED = "fey_essence_corrupted";
	public static final String ID_BELL = "fey_bell";
	public static final String ID_FLOWER = "fey_flower";
	public static final String ID_TABLET = "fey_tablet";
	public static final String ID_GOLEM_TOKEN = "golem_token";
	public static final String ID_LOGIC_TOKEN = "logic_token";
	
	private final FeyResourceType type;
	
	public FeyResource(FeyResourceType type) {
		super(FairyItems.PropBase());
		this.type = type;
	}
	
	public static Item getItem(FeyResourceType type) {
		switch (type) {
		case BELL:
			return FairyItems.feyBell;
		case ESSENCE:
			return FairyItems.feyEssence;
		case ESSENCE_CORRUPTED:
			return FairyItems.feyCorruptedEssence;
		case FLOWER:
			return FairyItems.feyFlower;
		case GOLEM_TOKEN:
			return FairyItems.feyGolemToken;
		case LOGIC_TOKEN:
			return FairyItems.feyLogicToken;
		case TABLET:
			return FairyItems.feyTablet;
		case TEARS:
			return FairyItems.feyTears;
		}
		
		return null;
	}
	
    public static ItemStack create(FeyResourceType type, int count) {
    	return new ItemStack(getItem(type), count);
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
		return this.type;
	}
	
	protected void spawnFey(ServerWorld worldIn, BlockPos at) {
		final EntityFeyBase fey;
		switch (NostrumFairies.random.nextInt(5)) {
		case 0:
			fey = new EntityFairy(FairyEntities.Fairy, worldIn);
			break;
		case 1:
			fey = new EntityDwarf(FairyEntities.Dwarf, worldIn);
			break;
		case 2:
			fey = new EntityGnome(FairyEntities.Gnome, worldIn);
			break;
		case 3:
			fey = new EntityElf(FairyEntities.Elf, worldIn);
			break;
		case 4:
		default:
			fey = new EntityElfArcher(FairyEntities.ElfArcher, worldIn);
			break;
		}
		fey.setPosition(at.getX() + .5, at.getY(), at.getZ() + .5);
		fey.onInitialSpawn(worldIn, worldIn.getDifficultyForLocation(fey.getPosition()), SpawnReason.MOB_SUMMONED, (ILivingEntityData)null, null);
		
		worldIn.addEntity(fey);
		
		((ServerWorld) worldIn).spawnParticle(ParticleTypes.END_ROD,
				at.getX() + .5,
				at.getY() + .25,
				at.getZ() + .5,
				100,
				.25,
				.4,
				.25,
				.05);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		final PlayerEntity playerIn = context.getPlayer();
		final Hand hand = context.getHand();
		final World worldIn = context.getWorld();
		final ItemStack stack = playerIn.getHeldItem(hand);
		final BlockPos pos = context.getPos();
		FeyResourceType type = getType(stack);
		
		if (type == FeyResourceType.FLOWER) {
			
			if (!worldIn.isRemote) {
				spawnFey((ServerWorld) worldIn, pos.up());
				int count = NostrumFairies.random.nextInt(3) + 1;
				BlockPos.Mutable cursor = new BlockPos.Mutable();
				for (int i = 0; i < count; i++) {
					cursor.setPos(pos.up()).move(Direction.Plane.HORIZONTAL.random(NostrumFairies.random), NostrumFairies.random.nextInt(2) + 1);
					for (int j = 0; j < 5; j++) {
						if (worldIn.isAirBlock(cursor)) {
							if (FairyBlocks.feyBush.isValidPosition(FairyBlocks.feyBush.getDefaultState(), worldIn, cursor)) {
								// Found a spot!
								worldIn.setBlockState(cursor.toImmutable(), FairyBlocks.feyBush.getDefaultState());
								((ServerWorld) worldIn).spawnParticle(ParticleTypes.HAPPY_VILLAGER,
										cursor.getX() + .5,
										cursor.getY() + .25,
										cursor.getZ() + .5,
										20,
										.25,
										1,
										.25,
										0);
								break;
							} else {
								cursor.move(Direction.DOWN);
							}
						} else {
							cursor.move(Direction.UP);
						}
					}
					
				}
			}
			
			stack.shrink(1);
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.PASS;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack stack = playerIn.getHeldItem(hand);
		FeyResourceType type = getType(stack);
		if (type == FeyResourceType.TABLET) {
			if (!worldIn.isRemote) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
				if (attr != null) {
					if (!attr.hasLore(FeyFriendLore.instance)) {
						attr.giveFullLore(FeyFriendLore.instance());
						stack.shrink(1);
						return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
					} else {
						playerIn.sendMessage(new TranslationTextComponent("info.tablet.fail"), Util.DUMMY_UUID);
						return new ActionResult<ItemStack>(ActionResultType.FAIL, stack);
					}
				}
			} else {
				return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
			}
		} else if (type == FeyResourceType.BELL) {
			if (!worldIn.isRemote) {
				for (EntityShadowFey ent : worldIn.getEntitiesWithinAABB(EntityShadowFey.class, VoxelShapes.fullCube().getBoundingBox().offset(playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ()).grow(30))) {
					ent.addPotionEffect(new EffectInstance(Effects.GLOWING, 20 * 5));
					NostrumMagica.playerListener.registerTimer((/*Event*/ eType, /*LivingEntity*/ entity, /*Object*/ data) -> {
						if (eType == Event.TIME) {
							NostrumFairiesSounds.BELL.play(worldIn, ent.getPosX(), ent.getPosY(), ent.getPosZ());
						}
						return true;
					}, 10, 10);
				}
				
				NostrumFairiesSounds.BELL.play(worldIn, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ());
			}
			return new ActionResult<ItemStack>(ActionResultType.SUCCESS, stack);
		}
		
		return new ActionResult<ItemStack>(ActionResultType.PASS, stack);
	}
	
	public static void onMobDrop(LivingDropsEvent event) {
		final float chance;
		if (event.getEntityLiving() instanceof IronGolemEntity) {
			chance = .2f;
		} else if (event.getEntityLiving() instanceof GolemEntity) {
			chance = .01f;
		} else {
			chance = 0f;
		}
		
		if (chance == 0f) {
			return;
		}
		
		for (int i = 0; i <= event.getLootingLevel(); i++) {
			if (NostrumFairies.random.nextFloat() < chance) {
				ItemEntity entity = new ItemEntity(event.getEntity().world,
						event.getEntity().getPosX(),
						event.getEntity().getPosY(),
						event.getEntity().getPosZ(),
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
