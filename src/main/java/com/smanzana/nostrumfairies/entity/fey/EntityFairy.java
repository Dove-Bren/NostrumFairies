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
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWithdrawItem;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.IEntityLoreTagged;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.ItemStacks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityFairy extends EntityFeyBase implements IItemCarrierFey {
	
	public static final String ID = "fairy";

	private static final String NBT_ITEM = "helditem";
	private static final EntityDataAccessor<ItemStack> DATA_HELD_ITEM = SynchedEntityData.<ItemStack>defineId(EntityFairy.class, EntityDataSerializers.ITEM_STACK);
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityFairy(EntityType<? extends EntityFairy> type, Level world) {
		super(type, world);
		this.workDistanceSq = 100 * 100;
		this.noPhysics = true;
		
		this.moveControl = new FairyFlyMoveHelper(this);
	}

	protected @Nonnull ItemStack getHeldItem() {
		return this.entityData.get(DATA_HELD_ITEM);
	}

	@Override
	public NonNullList<ItemStack> getCarriedItems() {
		return NonNullList.of(null, getHeldItem());
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		ItemStack heldItem = getHeldItem();
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
		ItemStack heldItem = getHeldItem();
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
		ItemStack heldItem = getHeldItem();
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

	@Override
	protected boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to) {

		// We want to just drop our task if our status changes from WORKING
		if (from == FairyGeneralStatus.WORKING) {
			this.forfitTask();
		}
		
		switch (to) {
		case IDLE:
			setActivitySummary("status.fairy.relax");
		case REVOLTING:
			setActivitySummary("status.fairy.revolt");
		case WANDERING:
			setActivitySummary("status.fairy.wander");
		case WORKING:
			setActivitySummary("status.generic.working");
		}
		
		return true;
	}

	@Override
	public ResidentType getHomeType() {
		return ResidentType.FAIRY;
	}

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskWithdrawItem) {
			LogisticsTaskWithdrawItem retrieve = (LogisticsTaskWithdrawItem) task;
			
			// Check where the retrieval task wants us to go to pick up
			BlockPos pickup = retrieve.getSource();
			if (pickup != null && !this.canReach(pickup, true)) {
				return false;
			}
			
			// Check for pathing
			ILogisticsComponent source = retrieve.getSourceComponent();
			if (source == null) {
				// entity
				if (this.distanceToSqr(retrieve.getSourceEntity()) < .2) {
					return true;
				}
				
				return true;
			} else {
				BlockPos pos = source.getPosition();
				
				if (!level.isEmptyBlock(pos)) {
					if (level.isEmptyBlock(pos.north())) {
						pos = pos.north();
					} else if (level.isEmptyBlock(pos.south())) {
						pos = pos.south();
					} else if (level.isEmptyBlock(pos.east())) {
						pos = pos.east();
					} else if (level.isEmptyBlock(pos.west())) {
						pos = pos.west();
					} else {
						pos = pos.above();
					}
				}
				
				if (this.getDistanceSq(pos) < .2 || this.blockPosition().equals(pos)) {
					return true;
				}
				
				return true;
			}
		} else if (task instanceof LogisticsTaskDepositItem) {
			LogisticsTaskDepositItem deposit = (LogisticsTaskDepositItem) task;
			
			// Check where the retrieval task wants us to go to pick up
			BlockPos pickup = deposit.getDestination();
			if (pickup != null && !this.canReach(pickup, true)) {
				return false;
			}
			
			// Check for pathing
			ILogisticsComponent source = deposit.getSourceComponent();
			if (source == null) {
				// entity
				if (this.distanceToSqr(deposit.getSourceEntity()) < .2) {
					return true;
				}
				
				return true;
			} else {
				BlockPos pos = source.getPosition();
				
				if (!level.isEmptyBlock(pos)) {
					if (level.isEmptyBlock(pos.north())) {
						pos = pos.north();
					} else if (level.isEmptyBlock(pos.south())) {
						pos = pos.south();
					} else if (level.isEmptyBlock(pos.east())) {
						pos = pos.east();
					} else if (level.isEmptyBlock(pos.west())) {
						pos = pos.west();
					} else {
						pos = pos.above();
					}
				}
				
				if (this.getDistanceSq(pos) < .2 || this.blockPosition().equals(pos)) {
					return true;
				}
				
				return true;
			}
		}
//		else if (task instanceof LogisticsTaskPickupItem) {
//			LogisticsTaskPickupItem pickupTask = (LogisticsTaskPickupItem) task;
//			
//			// Check where the retrieval task wants us to go to pick up
//			BlockPos pickup = pickupTask.getDestination();
//			if (pickup != null && !this.canReach(pickup, true)) {
//				return false;
//			}
//			
//			// Check for pathing
//			if (this.getDistanceSq(pickupTask.getItemEntity()) < .2) {
//				return true;
//			}
//			
//			return true;
//		}
		
		return false;
	}
	
	protected void dropItem() {
		ItemEntity item = new ItemEntity(this.level, getX(), getY(), getZ(), getHeldItem());
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
			if (newTask instanceof LogisticsTaskDepositItem) {
				setActivitySummary("status.fairy.work.deposit");
			} else if (newTask instanceof LogisticsTaskWithdrawItem) {
				setActivitySummary("status.fairy.work.withdraw");
			}
		}
	}
	
	@Override
	protected void onIdleTick() {
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		ItemStack heldItem = getHeldItem();
		if (!heldItem.isEmpty()) {
			LogisticsNetwork network = this.getLogisticsNetwork();
			if (network != null) {
				@Nullable ILogisticsComponent storage = network.getStorageForItem(level, blockPosition(), heldItem);
				if (storage != null) {
					ILogisticsTask task = new LogisticsTaskDepositItem(this, "Returning item", heldItem.copy());
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
		if (!this.getMoveControl().hasWanted()) {
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
//					while (targ.getY() > 0 && world.isAirBlock(targ)) {
//						targ = targ.down();
//					}
//					if (targ.getY() < 256) {
//						targ = targ.up();
//					}
					while (targ.getY() < 256 && !level.isEmptyBlock(targ)) {
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
				//if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.getMoveControl().setWantedPosition(targ.getX() + .5, targ.getY() + .5, targ.getZ() + .5, 1.0f);
				//}
				
			}
		}
	}

	@Override
	protected void onTaskTick(ILogisticsTask task) {
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			switch (sub.getType()) {
			case ATTACK:
				break;
			case BREAK:
				// this is where we'd play some animation?
				if (this.random.nextBoolean()) {
					task.markSubtaskComplete();
				}
				break;
			case IDLE:
				if (movePos == null || !this.getMoveControl().hasWanted()) {
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
						//if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
							this.getMoveControl().setWantedPosition(targ.getX() + .5, targ.getY() + .5, targ.getZ() + .5, 1.0f);
						//}
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
					if (movePos == null || !this.getMoveControl().hasWanted()) {
						// First time through?
						if ((movePos != null && this.getDistanceSqToCenter(movePos) < .75)
							|| (moveEntity != null && this.distanceTo(moveEntity) < .75)) {
							task.markSubtaskComplete();
							movePos = null;
							moveEntity = null;
							return;
						}
						movePos = null;
						moveEntity = null;
						
						movePos = sub.getPos();
						if (movePos == null) {
							moveEntity = sub.getEntity();
							//if (!this.getNavigator().tryMoveToEntityLiving(moveEntity,  1)) {
								this.getMoveControl().setWantedPosition(moveEntity.getX(), moveEntity.getY(), moveEntity.getZ(), 1.0f);
							//}
						} else {
							if (!level.isEmptyBlock(movePos)) {
								if (level.isEmptyBlock(movePos.north())) {
									movePos = movePos.north();
								} else if (level.isEmptyBlock(movePos.south())) {
									movePos = movePos.south();
								} else if (level.isEmptyBlock(movePos.east())) {
									movePos = movePos.east();
								} else if (level.isEmptyBlock(movePos.west())) {
									movePos = movePos.west();
								} else {
									movePos = movePos.above();
								}
							}
							//if (!this.getNavigator().tryMoveToXYZ(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5, 1.0f)) {
								this.getMoveControl().setWantedPosition(movePos.getX() + .5, movePos.getY() + .5, movePos.getZ() + .5, 1.0f);
							//}
						}
					}
//					else {
//						// Check if we're close to where we need to be
//						double distSq;
//						if (movePos == null) {
//							distSq = this.getDistanceSq(moveEntity);
//						} else {
//							distSq = this.getDistanceSq(movePos);
//						}
//						
//						if (distSq < .2) {
//							task.markSubtaskComplete();
//							this.navigator.clearPath();
//						}
//					}
					// FIXME this runs every tick. Save pos?
//					BlockPos pos = sub.getPos();
//					if (pos == null) {
//						LivingEntity entity = sub.getEntity();
//						
//					} else {
//						if (world.isAirBlock(pos.north())) {
//							pos = pos.north();
//						} else if (world.isAirBlock(pos.south())) {
//							pos = pos.south();
//						} else if (world.isAirBlock(pos.east())) {
//							pos = pos.east();
//						} else if (world.isAirBlock(pos.west())) {
//							pos = pos.west();
//						}
//						
//						// TODO FIXME I think this is constantly making new paths?
//						if (getPosition().distanceSq(pos) < .2) {
//							task.markSubtaskComplete();
//						} else if (!getMoveHelper().isUpdating()) {
//							if (!this.getNavigator().tryMoveToXYZ(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.0f)) {
//								this.getMoveHelper().setMoveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.0f);
//							}
//						}
//					}
				}
				break;
			}
		}
	}

	@Override
	protected void registerGoals() {
		// TODO Auto-generated method stub
		// I guess we should wander and check if tehre's a home nearby and if so make it our home and stop wandering.
		// Or if we're revolting... just quit for this test one?
		// Or if we're working, dont use AI
		// Or if we're idle... wander?
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return EntityFeyBase.BuildFeyAttributes()
				.add(Attributes.MOVEMENT_SPEED, .4)
				.add(Attributes.MAX_HEALTH, 4.0)
			;
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_HELD_ITEM, ItemStack.EMPTY);
	}
	
	@Override
	public boolean isNoGravity() {
		return true;
	}
	
	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
    public boolean shouldRenderAtSqrDistance(double distance) {
		return distance < 6400;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		if (!getHeldItem().isEmpty()) {
			compound.put(NBT_ITEM, getHeldItem().save(new CompoundTag()));
		}
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		if (compound.contains(NBT_ITEM)) {
			this.entityData.set(DATA_HELD_ITEM, ItemStack.of(compound.getCompound(NBT_ITEM)));
		}
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return this.getHeldItem().isEmpty();
	}
	
	static class FairyFlyMoveHelper extends MoveControl {
		private final Mob parentEntity;
		private double lastDist;
		private int courseChangeCooldown;

		public FairyFlyMoveHelper(Mob entity) {
			super(entity);
			this.parentEntity = entity;
		}

		@Override
		public void tick() {
			if (this.operation == MoveControl.Operation.MOVE_TO) {
				double d0 = this.getWantedX() - this.parentEntity.getX();
				double d1 = this.getWantedY() - this.parentEntity.getY();
				double d2 = this.getWantedZ() - this.parentEntity.getZ();
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				d3 = Math.sqrt(d3);
				
				if (Math.abs(d3) < .25) {
					lastDist = 0.0D;
					this.parentEntity.setDeltaMovement(Vec3.ZERO);
					this.operation = MoveControl.Operation.WAIT;
					return;
				} else if (lastDist != 0.0D && Math.abs(lastDist - d3) < 0.05) {
					courseChangeCooldown--;
				} else {
					courseChangeCooldown = this.parentEntity.getRandom().nextInt(5) + 10;
				}

				if (courseChangeCooldown <= 0) {
					lastDist = 0.0D;
					this.operation = MoveControl.Operation.WAIT;
				} else {
					float speed = (float) this.parentEntity.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
					//speed *= 3f;
					this.parentEntity.setDeltaMovement(
							(d0 / d3) * speed,
							(d1 / d3) * speed,
							(d2 / d3) * speed);
					
					lastDist = d3;
					
					float f9 = (float)(Mth.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					this.parentEntity.setYRot(this.rotlerp(this.parentEntity.getYRot(), f9, 90.0F));
				}
			} else if (this.operation == MoveControl.Operation.STRAFE) {
				this.parentEntity.setXxa(strafeRight);
				this.parentEntity.setZza(strafeForwards);
			}
		}
	}

	@Override
	protected String getRandomName() {
		final String[] names = {
			"Happy Bubblefleck",
			"Storm Silverclover",
			"Sunset Mossflower",
			"Dusk Almondglimmer",
			"Pyro Cottonsprout",
			"Flame Blackpuff",
			"Quicksilver Gigglefly",
			"Sunrise Mistyfoam",
			"Canyon Cuteflame",
			"Tadpole Eveningfur",
			"Octavia Tulipdrop",
			"Cintrine Jinglebud",
			"Relle Spiderroot",
			"Charity Turtlepebbles",
			"Midnight Muddylake",
			"Lapis Driftsky",
			"Spore Mistywhisper",
			"Prise Birdmint",
			"Mildread Birdbriar",
			"Orange Lillygust",
			"Margo Chillyshine",
			"Waterfall Plummeadow",
			"Jillian Willowfeet",
			"Marlie Wonderthorn",
			"Amy Garlicmuse",
			"Salle Bumblewish",
			"June Tigerroot",
			"Swan Lilyflame",
			"Palmera Pollenhorn",
			"Pluma Pinedale",
		};
		return names[random.nextInt(names.length)];
	}

	@Override
	protected void onCombatTick() {
		; // No combat
	}
	
	@Override
	protected void onCientTick() {
		int color = 0x40CCFFDD;
		NostrumParticles.GLOW_ORB.spawn(level, new SpawnParams(
				1, getX(), getY() + getBbHeight()/2f, getZ(), 0, 40, 0,
				new Vec3(random.nextFloat() * .025 - .0125, random.nextFloat() * .025 - .0125, random.nextFloat() * .025 - .0125), null
				).color(color));
	}
	
	@Override
	public String getSpecializationName() {
		return "Fairy";
	}

	@Override
	protected String getUnlocPrefix() {
		return "fairy";
	}
	
	@Override
	protected boolean shouldJoin(BlockPos pos, BlockState state, HomeBlockTileEntity te) {
		return random.nextBoolean() && random.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		//System.out.println(this.getPosition());
		if (this.navigation.isDone() && tickCount % 50 == 0 && random.nextBoolean() && random.nextBoolean()) {
			if (!EntityFeyBase.FeyLazyFollowNearby(this, EntityFeyBase.DOMESTIC_FEY_AND_PLAYER_FILTER, 20, 2, 5)) {
				// Go to a random place
				EntityFeyBase.FeyWander(this, this.blockPosition(), Math.min(10, Math.sqrt(this.wanderDistanceSq)));
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
		return 0.2f;
	}

	@Override
	public EntityFeyBase switchToSpecialization(FeyStoneMaterial material) {
		return this;
	}
	
	public EntityPersonalFairy promotoToPersonal() {
		if (level.isClientSide) {
			return null;
		}
		
		EntityPersonalFairy replacement = new EntityPersonalFairy(FairyEntities.PersonalFairy, level);
		
		// Kill this entity and add the other one
		if (this.getHome() != null) {
			this.setHome(null);
		}
		replacement.copyFrom(this);
		this.discard();
		level.addFreshEntity(replacement);
		
		return replacement;
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return null;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return NostrumFairiesSounds.FAIRY_HURT.getEvent();
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return NostrumFairiesSounds.FAIRY_DIE.getEvent();
	}
	
	@Override
	protected @Nullable NostrumFairiesSounds getIdleSound() {
		return NostrumFairiesSounds.FAIRY_IDLE;
	}
	
	@Override
	public ILoreTagged getLoreTag() {
		return FairyLore.instance;
	}
	
	public static final class FairyLore implements IEntityLoreTagged<EntityFairy> {
		public static final FairyLore instance = new FairyLore();
		
		@Override
		public String getLoreKey() {
			return "fairy";
		}

		@Override
		public String getLoreDisplayName() {
			return "Fairies";
		}

		@Override
		public Lore getBasicLore() {
			return new Lore().add("Fairies are small, quick, and in some instances, full of whimsy. Despite their prankster tendencies, they seem to have a keen interest in helping players.");
		}

		@Override
		public Lore getDeepLore() {
			return getBasicLore().add("Fairies can perform all sorts of logistics tasks. They can also be bound to a player to help them directly.");
		}
		
		@Override
		public ELoreCategory getCategory() {
			return ELoreCategory.ENTITY;
		}

		@Override
		public EntityType<? extends EntityFairy> getEntityType() {
			return FairyEntities.Fairy;
		}
	}
}
