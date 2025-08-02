package com.smanzana.nostrumfairies.entity.fey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskHarvest;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPickupItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.serializers.ArmPoseGnome;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.Paths;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class EntityGnome extends EntityFeyBase implements IItemCarrierFey {
	
	public static final String ID = "gnome";
	
	protected static final EntityDataAccessor<ArmPoseGnome> POSE  = SynchedEntityData.<ArmPoseGnome>defineId(EntityGnome.class, ArmPoseGnome.instance());
	private static final EntityDataAccessor<ItemStack> DATA_HELD_ITEM = SynchedEntityData.<ItemStack>defineId(EntityGnome.class, EntityDataSerializers.ITEM_STACK);

	private static final String NBT_ITEM = "helditem";
	
	public EntityGnome(EntityType<? extends EntityGnome> type, Level world) {
		super(type, world);
		this.workDistanceSq = 24 * 24;
	}

	public @Nonnull ItemStack getCarriedItem() {
		return this.entityData.get(DATA_HELD_ITEM);
	}

	@Override
	public NonNullList<ItemStack> getCarriedItems() {
		return NonNullList.of(null, getCarriedItem());
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		ItemStack heldItem = getCarriedItem();
		return heldItem.isEmpty() ||
				(ItemStacks.stacksMatch(heldItem, stack) && heldItem.getCount() + stack.getCount() < heldItem.getMaxStackSize());
	}
	
	@Override
	public boolean canAccept(ItemDeepStack stack) {
		// we know we can't if it's more than one regular ItemStack
		if (stack.getCount() > stack.getTemplate().getMaxStackSize()) {
			return false;
		}
		
		// Looks like it's only actually one stack.
		return canAccept(stack.copy().splitStack(stack.getTemplate().getMaxStackSize()));
	}

	@Override
	public void addItem(ItemStack stack) {
		ItemStack heldItem = getCarriedItem();
		if (heldItem.isEmpty()) {
			heldItem = stack.copy();
		} else {
			// Just assume canAccept was called
			heldItem.grow(stack.getCount()); 
		}
		this.entityData.set(DATA_HELD_ITEM, heldItem);
	}
	
	@Override
	public void removeItem(ItemStack stack) {
		ItemStack heldItem = getCarriedItem();
		if (!heldItem.isEmpty()) {
			if (ItemStacks.stacksMatch(stack, heldItem)) {
				heldItem.shrink(stack.getCount());
				if (heldItem.isEmpty()) {
					heldItem = ItemStack.EMPTY;
				}
			}
		}
		this.entityData.set(DATA_HELD_ITEM, heldItem);
	}
	
	protected boolean hasItems() {
		return !getCarriedItem().isEmpty();
	}

	@Override
	protected boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to) {

		// We want to just drop our task if our status changes from WORKING
		if (from == FairyGeneralStatus.WORKING) {
			this.forfitTask();
		}
		
		switch (to) {
		case IDLE:
			setActivitySummary("status.gnome.relax");
			break;
		case REVOLTING:
			setActivitySummary("status.gnome.revolt");
			break;
		case WANDERING:
			setActivitySummary("status.gnome.wander");
			break;
		case WORKING:
			; // set by task
		}
		
		return true;
	}

	@Override
	public ResidentType getHomeType() {
		return ResidentType.GNOME;
	}
	
	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskPlantItem) {
			LogisticsTaskPlantItem plant = (LogisticsTaskPlantItem) task;
			
			if (plant.getWorld() != this.level) {
				return false;
			}
			
			// Check where the spot is
			BlockPos target = plant.getTargetPlaceLoc();
			if (target == null || !this.canReach(target, true)) {
				return false;
			}
			
			// Find a better block to stand, if we weren't told explicitely to stand there
			if (target == plant.getTargetBlock()) {
				target = findEmptySpot(target, true);
				if (target == null) {
					return false;
				}
			}
			
			// Check for pathing
			if (this.getDistanceSq(target) < .2) {
				return true;
			}
			Path currentPath = navigation.getPath();
			boolean success = navigation.moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
			if (success) {
				success = Paths.IsComplete(navigation.getPath(), target, 2);
			}
			if (currentPath == null) {
				if (!success) {
					navigation.moveTo(currentPath, 1.0);
				}
			} else {
				navigation.moveTo(currentPath, 1.0);
			}
			if (success) {
				return true;
			} else if (this.getDistanceSq(target) < 1) {
				// extra case for if the navigator refuses cause we're too close
				return true;
			}
		} else if (task instanceof LogisticsTaskHarvest) {
			LogisticsTaskHarvest harvest = (LogisticsTaskHarvest) task;
			
			if (harvest.getWorld() != this.level) {
				return false;
			}
			
			// Check where the spot is
			BlockPos target = harvest.getCropPos();
			if (target == null || !this.canReach(target, true)) {
				return false;
			}
			
			// Check for pathing
			if (this.getDistanceSq(target) < .2) {
				return true;
			}
			Path currentPath = navigation.getPath();
			boolean success = navigation.moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
			if (success) {
				success = Paths.IsComplete(navigation.getPath(), target, 2);
			}
			if (currentPath == null) {
				if (!success) {
					navigation.moveTo(currentPath, 1.0);
				}
			} else {
				navigation.moveTo(currentPath, 1.0);
			}
			if (success) {
				return true;
			} else if (this.getDistanceSq(target) < 1) {
				// extra case for if the navigator refuses cause we're too close
				return true;
			}
		}
		
		return false;
	}
	
	private void dropItem() {
		ItemEntity item = new ItemEntity(this.level, getX(), getY(), getZ(), getCarriedItem());
		level.addFreshEntity(item);
		this.entityData.set(DATA_HELD_ITEM, ItemStack.EMPTY);
	}

	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		//return this.heldItem.isEmpty();
		return true;
	}

	@Override
	protected void onTaskChange(ILogisticsTask oldTask, ILogisticsTask newTask) {
//		if (oldTask != null && !heldItem().isEmpty()) {
//			// I guess drop our item
//			dropItem();
//		}
		// Task should have checked if we could hold what it needed, if it's item related.
		// Assuming it did, our current inventory is fine. We'll do that task, maybe use our
		// inventory, and then be idle with an item afterwards -- whicih will prompt
		// us to go return it.
		
		if (newTask != null) {
			if (newTask instanceof LogisticsTaskPickupItem) {
				setActivitySummary("status.gnome.work.pickup");
			} else if (newTask instanceof LogisticsTaskHarvest) {
				setActivitySummary("status.gnome.work.harvest");
			} else if (newTask instanceof LogisticsTaskPlantItem) {
				setActivitySummary("status.gnome.work.plant");
			} else if (newTask instanceof LogisticsTaskDepositItem) {
				setActivitySummary("status.generic.return");
			} else {
				setActivitySummary("status.generic.working");
			}
		}
	}
	
	@Override
	protected void onIdleTick() {
		this.setPose(ArmPoseGnome.IDLE);
		
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		if (hasItems()) {
			ItemStack held = getCarriedItem();
			
			LogisticsNetwork network = this.getLogisticsNetwork();
			if (network != null) {
				@Nullable ILogisticsComponent storage = network.getStorageForItem(level, blockPosition(), held);
				if (storage != null) {
					ILogisticsTask task = new LogisticsTaskDepositItem(this, "Returning item", held.copy());
					network.getTaskRegistry().register(task, null);
					network.getTaskRegistry().claimTask(task, this);
					forceSetTask(task);
					return;
				}
			}
			
			// no return means we couldn't set up a task to drop it
			dropItem();
			
		}
		
		// See if we're too far away from our home block
		if (this.navigation.isDone()) {
			BlockPos home = this.getHome();
			if (home != null && !this.canReach(this.blockPosition(), false)) {
				
				// Go to a random place around our home
				final BlockPos center = home;
				BlockPos targ = null;
				int attempts = 20;
				final double maxDistSq = Math.min(25, this.wanderDistanceSq);
				do {
					double dist = this.random.nextDouble() * Math.sqrt(maxDistSq);
					float angle = (float) (this.random.nextDouble() * (2 * Math.PI));
					float tilt = (float) (this.random.nextDouble() * (2 * Math.PI)) * .5f;
					
					targ = new BlockPos(new Vec3(
							center.getX() + (Math.cos(angle) * dist),
							center.getY() + (Math.cos(tilt) * dist),
							center.getZ() + (Math.sin(angle) * dist)));
					while (targ.getY() > 0 && level.isEmptyBlock(targ)) {
						targ = targ.below();
					}
					if (targ.getY() < 256) {
						targ = targ.above();
					}
					
					// We've hit a non-air block. Make sure there's space above it
					BlockPos airBlock = null;
					for (int i = 0; i < Math.ceil(this.getBbHeight()); i++) {
						if (airBlock == null) {
							airBlock = targ.above();
						} else {
							airBlock = airBlock.above();
						}
						
						if (!level.isEmptyBlock(airBlock)) {
							targ = null;
							break;
						}
					}
				} while (targ == null && attempts > 0);
				
				if (targ == null) {
					targ = center.above();
				}
				if (!this.getNavigation().moveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.getMoveControl().setWantedPosition(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
				}
				
			}
		}
	}

	@Override
	protected void onTaskTick(ILogisticsTask task) {
		
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			switch (sub.getType()) {
			case ATTACK:
				this.lookAt(sub.getEntity(), 30, 180);
				break;
			case BREAK: {
				BlockPos pos = sub.getPos();
				double d0 = pos.getX() - this.getX();
		        double d2 = pos.getZ() - this.getZ();
				float desiredYaw = (float)(Mth.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
				
				this.setPose(ArmPoseGnome.WORKING);
				this.setYRot(desiredYaw);
				if (this.swinging) {
					// On the client, spawn some particles if we're using our wand
					// lel what if we sweat? xD
//					if (ticksExisted % 5 == 0 && getPose() == ArmPose.CHOPPING) {
//						world.spawnParticle(EnumParticleTypes.DRAGON_BREATH,
//								getPosX(), getPosY(), getPosZ(),
//								0, 0.3, 0,
//								new int[0]);
//					}
				} else {
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						setPose(ArmPoseGnome.IDLE);
						break;
					}
					this.swing(this.getUsedItemHand());
					NostrumFairiesSounds.GNOME_WORK.play(level, getX(), getY(), getZ());
				}
				break;
			}
			case IDLE:
				if (this.hasItems()) {
					this.setPose(ArmPoseGnome.CARRYING);
				} else {
					this.setPose(ArmPoseGnome.IDLE);
				}
				if (this.navigation.isDone()) {
					if (movePos == null) {
						final BlockPos center = sub.getPos();
						BlockPos targ = null;
						int attempts = 20;
						final double maxDistSq = 25;
						do {
							double dist = this.random.nextDouble() * Math.sqrt(maxDistSq);
							float angle = (float) (this.random.nextDouble() * (2 * Math.PI));
							float tilt = (float) (this.random.nextDouble() * (2 * Math.PI)) * .5f;
							
							targ = new BlockPos(new Vec3(
									center.getX() + (Math.cos(angle) * dist),
									center.getY() + (Math.cos(tilt) * dist),
									center.getZ() + (Math.sin(angle) * dist)));
							while (targ.getY() > 0 && level.isEmptyBlock(targ)) {
								targ = targ.below();
							}
							if (targ.getY() < 256) {
								targ = targ.above();
							}
							
							// We've hit a non-air block. Make sure there's space above it
							BlockPos airBlock = null;
							for (int i = 0; i < Math.ceil(this.getBbHeight()); i++) {
								if (airBlock == null) {
									airBlock = targ.above();
								} else {
									airBlock = airBlock.above();
								}
								
								if (!level.isEmptyBlock(airBlock)) {
									targ = null;
									break;
								}
							}
						} while (targ == null && attempts > 0);
						
						if (targ == null) {
							targ = center.above();
						}
						if (!this.getNavigation().moveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
							this.getMoveControl().setWantedPosition(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
						}
						this.movePos = targ;
					} else {
						task.markSubtaskComplete();
						// Cheat and see if we just finished idling
						if (sub != task.getActiveSubtask()) {
							this.movePos = null;
						}
					}
				}
				break;
			case MOVE:
				{
					if (this.hasItems()) {
						this.setPose(ArmPoseGnome.CARRYING);
					} else {
						this.setPose(ArmPoseGnome.IDLE);
					}
					
					this.feyMoveToTask(task);
				}
				break;
			}
		}
	}

	@Override
	protected void registerGoals() {
		int priority = 0;
		this.goalSelector.addGoal(priority++, new FloatGoal(this));
		; // Could panic when they are attacked!
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return EntityFeyBase.BuildFeyAttributes()
				.add(Attributes.MOVEMENT_SPEED, .21)
				.add(Attributes.MAX_HEALTH, 8.0)
			;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		ItemStack held = getCarriedItem();
		if (!held.isEmpty()) {
			compound.put(NBT_ITEM, held.serializeNBT());
		}
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		if (compound.contains(NBT_ITEM, Tag.TAG_COMPOUND)) {
			entityData.set(DATA_HELD_ITEM, ItemStack.of(compound.getCompound(NBT_ITEM)));
		}
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return !this.hasItems();
	}
	
	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
	}

	@Override
	protected String getRandomName() {
		final String[] names = new String[] {
			"Smookep",
			"Tyrbit",
			"Clynsbyg",
			"Smimtart",
			"Frynsbit",
			"Lampicom",
			"Kneeddnimag",
			"Cabukpert",
			"Laibunsnep",
			"Knidingnap",
			"Slukar",
			"Shigla",
			"Klopryt",
			"Doklu",
			"Fliddwim",
			"Julnubap",
			"Gliddlegol",
			"Blulallba",
			"Bahylmel",
			"Hisellbis",
			"Fnukwop",
			"Clebbnat",
			"Cival",
			"Smemmi",
			"Mansmith",
			"Ginsmeefe",
			"Agnunal",
			"Slilyngnas",
			"Thidoobnyss",
			"Iwinsma",
			"Pepna",
			"Cidnip",
			"Snedbom",
			"Padny",
			"Smebblan",
			"Gnaankosee",
			"Fuknidup",
			"Smeniblyp",
			"Polagneth",
			"Sneehaansnill"
		};
		return names[random.nextInt(names.length)];
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(POSE, ArmPoseGnome.IDLE);
		entityData.define(DATA_HELD_ITEM, ItemStack.EMPTY);
	}
	
	public ArmPoseGnome getGnomePose() {
		return entityData.get(POSE);
	}
	
	public void setPose(ArmPoseGnome pose) {
		this.entityData.set(POSE, pose);
	}

	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 18;
	}
	
	@Override
	protected void onCombatTick() {
		; // No combat
	}

	@Override
	protected void onCientTick() {
		;
	}
	
	@Override
	public String getSpecializationName() {
		return "Garden Gnome";
	}
	
	@Override
	protected String getUnlocPrefix() {
		return "gnome";
	}

	@Override
	protected boolean shouldJoin(BlockPos pos, BlockState state, HomeBlockTileEntity te) {
		return random.nextBoolean() && random.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		if (this.navigation.isDone()) {
			if (!EntityFeyBase.FeyActiveFollowNearby(this, EntityFeyBase.DOMESTIC_FEY_AND_PLAYER_FILTER, 20, 2, 5)) {
				// Go to a random place
				if (tickCount % 60 == 0 && random.nextBoolean() && random.nextBoolean()) {
					EntityFeyBase.FeyWander(this, this.blockPosition(), Math.min(10, Math.sqrt(this.wanderDistanceSq)));
				}
			}
		}
	}

	@Override
	protected void onRevoltTick() {
		// TODO Auto-generated method stub
		;
	}
	
	@Override
	protected float getGrowthForTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskPickupItem) {
			return 0.2f;
		}
		if (task instanceof LogisticsTaskPlantItem) {
			return 0.3f;
		}
		if (task instanceof LogisticsTaskHarvest) {
			return 0.6f;
		}
		if (task instanceof LogisticsTaskWorkBlock) {
			return 0.65f;
		}
		
		return 0f;
	}

	@Override
	public EntityFeyBase switchToSpecialization(FeyStoneMaterial material) {
		if (level.isClientSide) {
			return this;
		}
		
		EntityFeyBase replacement = null;
		if (material != this.getCurrentSpecialization()) {
			if (material == FeyStoneMaterial.EMERALD) {
				// Gathering
				replacement = new EntityGnomeCollector(FairyEntities.GnomeCollector, level);
			} else if (material == FeyStoneMaterial.GARNET) {
				// Crafting
				replacement = new EntityGnomeCrafter(FairyEntities.GnomeCrafter, level);
			} else {
				replacement = new EntityGnome(FairyEntities.Gnome, level);
			}
		}
		
		if (replacement != null) {
			// Kill this entity and add the other one
			replacement.copyFrom(this);
			//this.remove();
			this.discard();
			//((ServerLevel) level).removeEntity(this);
			level.addFreshEntity(replacement);
		}
		
		return replacement == null ? this : replacement;
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return null;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return NostrumFairiesSounds.GNOME_HURT.getEvent();
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return NostrumFairiesSounds.GNOME_DIE.getEvent();
	}
	
	@Override
	protected @Nullable NostrumFairiesSounds getIdleSound() {
		return NostrumFairiesSounds.GNOME_IDLE;
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return GnomeLore.instance;
	}
	
	public static final class GnomeLore implements IEntityLoreTagged<EntityGnome> {
		public static final GnomeLore instance = new GnomeLore();
		
		@Override
		public String getLoreKey() {
			return "gnome";
		}

		@Override
		public String getLoreDisplayName() {
			return "Gnomes";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("Gnomes are small, humble, and plain-headed. They like doing what they need to do, and then relaxing.");
		}

		@Override
		public Lore getDeepLore() {
			return getBasicLore().add("Gnomes can perform small crafting recipes, gather and collect items, and even tend to crops!");
		}
		
		@Override
		public ELoreCategory getCategory() {
			return ELoreCategory.ENTITY;
		}

		@Override
		public EntityType<? extends EntityGnome> getEntityType() {
			return FairyEntities.Gnome;
		}
	}
}
