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
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityGnome extends EntityFeyBase implements IItemCarrierFey {
	
	public static final String ID = "gnome";
	
	protected static final DataParameter<ArmPoseGnome> POSE  = EntityDataManager.<ArmPoseGnome>createKey(EntityGnome.class, ArmPoseGnome.instance());
	private static final DataParameter<ItemStack> DATA_HELD_ITEM = EntityDataManager.<ItemStack>createKey(EntityGnome.class, DataSerializers.ITEMSTACK);

	private static final String NBT_ITEM = "helditem";
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityGnome(EntityType<? extends EntityGnome> type, World world) {
		super(type, world);
		this.workDistanceSq = 24 * 24;
	}

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
		return new Lore("test lore for test fairy lol");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore("test lore for test fairy lol");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	public @Nonnull ItemStack getCarriedItem() {
		return this.dataManager.get(DATA_HELD_ITEM);
	}

	@Override
	public NonNullList<ItemStack> getCarriedItems() {
		return NonNullList.from(null, getCarriedItem());
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
		this.dataManager.set(DATA_HELD_ITEM, heldItem);
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
		this.dataManager.set(DATA_HELD_ITEM, heldItem);
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
			
			if (plant.getWorld() != this.world) {
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
			Path currentPath = navigator.getPath();
			boolean success = navigator.tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0);
			if (success) {
				success = Paths.IsComplete(navigator.getPath(), target, 2);
			}
			if (currentPath == null) {
				if (!success) {
					navigator.setPath(currentPath, 1.0);
				}
			} else {
				navigator.setPath(currentPath, 1.0);
			}
			if (success) {
				return true;
			} else if (this.getDistanceSq(target) < 1) {
				// extra case for if the navigator refuses cause we're too close
				return true;
			}
		} else if (task instanceof LogisticsTaskHarvest) {
			LogisticsTaskHarvest harvest = (LogisticsTaskHarvest) task;
			
			if (harvest.getWorld() != this.world) {
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
			Path currentPath = navigator.getPath();
			boolean success = navigator.tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0);
			if (success) {
				success = Paths.IsComplete(navigator.getPath(), target, 2);
			}
			if (currentPath == null) {
				if (!success) {
					navigator.setPath(currentPath, 1.0);
				}
			} else {
				navigator.setPath(currentPath, 1.0);
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
		ItemEntity item = new ItemEntity(this.world, posX, posY, posZ, getCarriedItem());
		world.addEntity(item);
		this.dataManager.set(DATA_HELD_ITEM, ItemStack.EMPTY);
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
				@Nullable ILogisticsComponent storage = network.getStorageForItem(world, getPosition(), held);
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
		if (this.navigator.noPath()) {
			BlockPos home = this.getHome();
			if (home != null && !this.canReach(this.getPosition(), false)) {
				
				// Go to a random place around our home
				final BlockPos center = home;
				BlockPos targ = null;
				int attempts = 20;
				final double maxDistSq = Math.min(25, this.wanderDistanceSq);
				do {
					double dist = this.rand.nextDouble() * Math.sqrt(maxDistSq);
					float angle = (float) (this.rand.nextDouble() * (2 * Math.PI));
					float tilt = (float) (this.rand.nextDouble() * (2 * Math.PI)) * .5f;
					
					targ = new BlockPos(new Vec3d(
							center.getX() + (Math.cos(angle) * dist),
							center.getY() + (Math.cos(tilt) * dist),
							center.getZ() + (Math.sin(angle) * dist)));
					while (targ.getY() > 0 && world.isAirBlock(targ)) {
						targ = targ.down();
					}
					if (targ.getY() < 256) {
						targ = targ.up();
					}
					
					// We've hit a non-air block. Make sure there's space above it
					BlockPos airBlock = null;
					for (int i = 0; i < Math.ceil(this.getHeight()); i++) {
						if (airBlock == null) {
							airBlock = targ.up();
						} else {
							airBlock = airBlock.up();
						}
						
						if (!world.isAirBlock(airBlock)) {
							targ = null;
							break;
						}
					}
				} while (targ == null && attempts > 0);
				
				if (targ == null) {
					targ = center.up();
				}
				if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.getMoveHelper().setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
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
				this.faceEntity(sub.getEntity(), 30, 180);
				break;
			case BREAK: {
				BlockPos pos = sub.getPos();
				double d0 = pos.getX() - this.posX;
		        double d2 = pos.getZ() - this.posZ;
				float desiredYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
				
				this.setPose(ArmPoseGnome.WORKING);
				this.rotationYaw = desiredYaw;
				if (this.isSwingInProgress) {
					// On the client, spawn some particles if we're using our wand
					// lel what if we sweat? xD
//					if (ticksExisted % 5 == 0 && getPose() == ArmPose.CHOPPING) {
//						world.spawnParticle(EnumParticleTypes.DRAGON_BREATH,
//								posX, posY, posZ,
//								0, 0.3, 0,
//								new int[0]);
//					}
				} else {
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						setPose(ArmPoseGnome.IDLE);
						break;
					}
					this.swingArm(this.getActiveHand());
					NostrumFairiesSounds.GNOME_WORK.play(world, posX, posY, posZ);
				}
				break;
			}
			case IDLE:
				if (this.hasItems()) {
					this.setPose(ArmPoseGnome.CARRYING);
				} else {
					this.setPose(ArmPoseGnome.IDLE);
				}
				if (this.navigator.noPath()) {
					if (movePos == null) {
						final BlockPos center = sub.getPos();
						BlockPos targ = null;
						int attempts = 20;
						final double maxDistSq = 25;
						do {
							double dist = this.rand.nextDouble() * Math.sqrt(maxDistSq);
							float angle = (float) (this.rand.nextDouble() * (2 * Math.PI));
							float tilt = (float) (this.rand.nextDouble() * (2 * Math.PI)) * .5f;
							
							targ = new BlockPos(new Vec3d(
									center.getX() + (Math.cos(angle) * dist),
									center.getY() + (Math.cos(tilt) * dist),
									center.getZ() + (Math.sin(angle) * dist)));
							while (targ.getY() > 0 && world.isAirBlock(targ)) {
								targ = targ.down();
							}
							if (targ.getY() < 256) {
								targ = targ.up();
							}
							
							// We've hit a non-air block. Make sure there's space above it
							BlockPos airBlock = null;
							for (int i = 0; i < Math.ceil(this.getHeight()); i++) {
								if (airBlock == null) {
									airBlock = targ.up();
								} else {
									airBlock = airBlock.up();
								}
								
								if (!world.isAirBlock(airBlock)) {
									targ = null;
									break;
								}
							}
						} while (targ == null && attempts > 0);
						
						if (targ == null) {
							targ = center.up();
						}
						if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
							this.getMoveHelper().setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
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
					if (this.navigator.noPath()) {
						// First time through?
						if ((movePos != null && this.getDistanceSqToCenter(movePos) < 2)
							|| (moveEntity != null && this.getDistance(moveEntity) < 2)) {
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
							if (!this.getNavigator().tryMoveToEntityLiving(moveEntity,  1)) {
								this.getMoveHelper().setMoveTo(moveEntity.posX, moveEntity.posY, moveEntity.posZ, 1.0f);
							}
						} else {
							movePos = findEmptySpot(movePos, false);
							
							// Is the block we shifted to where we are?
							if (!this.getPosition().equals(movePos) && this.getDistanceSqToCenter(movePos) > 1) {
								if (!this.getNavigator().tryMoveToXYZ(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f)) {
									this.getMoveHelper().setMoveTo(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f);
								}
							}
						}
					}
				}
				break;
			}
		}
	}

	@Override
	protected void registerGoals() {
		int priority = 0;
		this.goalSelector.addGoal(priority++, new SwimGoal(this));
		; // Could panic when they are attacked!
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.21D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		//this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		ItemStack held = getCarriedItem();
		if (!held.isEmpty()) {
			compound.put(NBT_ITEM, held.serializeNBT());
		}
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		
		if (compound.contains(NBT_ITEM, NBT.TAG_COMPOUND)) {
			dataManager.set(DATA_HELD_ITEM, ItemStack.read(compound.getCompound(NBT_ITEM)));
		}
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return !this.hasItems();
	}
	
	@Override
	protected void collideWithEntity(Entity entityIn) {
		super.collideWithEntity(entityIn);
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
		return names[rand.nextInt(names.length)];
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(POSE, ArmPoseGnome.IDLE);
		dataManager.register(DATA_HELD_ITEM, ItemStack.EMPTY);
	}
	
	public ArmPoseGnome getGnomePose() {
		return dataManager.get(POSE);
	}
	
	public void setPose(ArmPoseGnome pose) {
		this.dataManager.set(POSE, pose);
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
		return rand.nextBoolean() && rand.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		if (this.navigator.noPath()) {
			if (!EntityFeyBase.FeyActiveFollowNearby(this, EntityFeyBase.DOMESTIC_FEY_AND_PLAYER_FILTER, 20, 2, 5)) {
				// Go to a random place
				if (ticksExisted % 60 == 0 && rand.nextBoolean() && rand.nextBoolean()) {
					EntityFeyBase.FeyWander(this, this.getPosition(), Math.min(10, Math.sqrt(this.wanderDistanceSq)));
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
		if (world.isRemote) {
			return this;
		}
		
		EntityFeyBase replacement = null;
		if (material != this.getCurrentSpecialization()) {
			if (material == FeyStoneMaterial.EMERALD) {
				// Gathering
				replacement = new EntityGnomeCollector(FairyEntities.GnomeCollector, world);
			} else if (material == FeyStoneMaterial.GARNET) {
				// Crafting
				replacement = new EntityGnomeCrafter(FairyEntities.GnomeCrafter, world);
			} else {
				replacement = new EntityGnome(FairyEntities.Gnome, world);
			}
		}
		
		if (replacement != null) {
			// Kill this entity and add the other one
			replacement.copyFrom(this);
			//this.remove();
			((ServerWorld) world).removeEntity(this);
			world.addEntity(replacement);
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
}
