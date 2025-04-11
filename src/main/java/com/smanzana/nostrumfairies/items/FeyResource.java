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

import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
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
	
	protected void spawnFey(ServerLevel worldIn, BlockPos at) {
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
		fey.setPos(at.getX() + .5, at.getY(), at.getZ() + .5);
		fey.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(fey.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, null);
		
		worldIn.addFreshEntity(fey);
		
		((ServerLevel) worldIn).sendParticles(ParticleTypes.END_ROD,
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
	public InteractionResult useOn(UseOnContext context) {
		final Player playerIn = context.getPlayer();
		final InteractionHand hand = context.getHand();
		final Level worldIn = context.getLevel();
		final ItemStack stack = playerIn.getItemInHand(hand);
		final BlockPos pos = context.getClickedPos();
		FeyResourceType type = getType(stack);
		
		if (type == FeyResourceType.FLOWER) {
			
			if (!worldIn.isClientSide) {
				spawnFey((ServerLevel) worldIn, pos.above());
				int count = NostrumFairies.random.nextInt(3) + 1;
				BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
				for (int i = 0; i < count; i++) {
					cursor.set(pos.above()).move(Direction.Plane.HORIZONTAL.getRandomDirection(NostrumFairies.random), NostrumFairies.random.nextInt(2) + 1);
					for (int j = 0; j < 5; j++) {
						if (worldIn.isEmptyBlock(cursor)) {
							if (FairyBlocks.feyBush.canSurvive(FairyBlocks.feyBush.defaultBlockState(), worldIn, cursor)) {
								// Found a spot!
								worldIn.setBlockAndUpdate(cursor.immutable(), FairyBlocks.feyBush.defaultBlockState());
								((ServerLevel) worldIn).sendParticles(ParticleTypes.HAPPY_VILLAGER,
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
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final ItemStack stack = playerIn.getItemInHand(hand);
		FeyResourceType type = getType(stack);
		if (type == FeyResourceType.TABLET) {
			if (!worldIn.isClientSide) {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(playerIn);
				if (attr != null) {
					if (!attr.hasLore(FeyFriendLore.instance)) {
						attr.giveFullLore(FeyFriendLore.instance());
						stack.shrink(1);
						return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
					} else {
						playerIn.sendMessage(new TranslatableComponent("info.tablet.fail"), Util.NIL_UUID);
						return new InteractionResultHolder<ItemStack>(InteractionResult.FAIL, stack);
					}
				}
			} else {
				return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
			}
		} else if (type == FeyResourceType.BELL) {
			if (!worldIn.isClientSide) {
				for (EntityShadowFey ent : worldIn.getEntitiesOfClass(EntityShadowFey.class, Shapes.block().bounds().move(playerIn.getX(), playerIn.getY(), playerIn.getZ()).inflate(30))) {
					ent.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 5));
					NostrumMagica.playerListener.registerTimer((/*Event*/ eType, /*LivingEntity*/ entity, /*Object*/ data) -> {
						if (eType == Event.TIME) {
							NostrumFairiesSounds.BELL.play(worldIn, ent.getX(), ent.getY(), ent.getZ());
						}
						return true;
					}, 10, 10);
				}
				
				NostrumFairiesSounds.BELL.play(worldIn, playerIn.getX(), playerIn.getY(), playerIn.getZ());
			}
			return new InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, stack);
		}
		
		return new InteractionResultHolder<ItemStack>(InteractionResult.PASS, stack);
	}
	
	public static void onMobDrop(LivingDropsEvent event) {
		final float chance;
		if (event.getEntityLiving() instanceof IronGolem) {
			chance = .2f;
		} else if (event.getEntityLiving() instanceof AbstractGolem) {
			chance = .01f;
		} else {
			chance = 0f;
		}
		
		if (chance == 0f) {
			return;
		}
		
		for (int i = 0; i <= event.getLootingLevel(); i++) {
			if (NostrumFairies.random.nextFloat() < chance) {
				ItemEntity entity = new ItemEntity(event.getEntity().level,
						event.getEntity().getX(),
						event.getEntity().getY(),
						event.getEntity().getZ(),
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
