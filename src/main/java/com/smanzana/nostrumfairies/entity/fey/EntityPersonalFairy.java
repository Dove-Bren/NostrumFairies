package com.smanzana.nostrumfairies.entity.fey;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.entity.IEntityListener;
import com.smanzana.nostrumfairies.entity.ITrackableEntity;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory.FairyCastTarget;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory.FairyPlacementTarget;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.serializers.FairyJob;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.gui.petgui.IPetGUISheet;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetGUIStatAdapter;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFlierDiveTask;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOrbitEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.entity.tasks.OwnerHurtByTargetGoalGeneric;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.pet.PetInfo;
import com.smanzana.nostrummagica.pet.PetInfo.ManagedPetInfo;
import com.smanzana.nostrummagica.pet.PetInfo.PetAction;
import com.smanzana.nostrummagica.pet.PetInfo.SecondaryFlavor;
import com.smanzana.nostrummagica.serializers.PetJobSerializer;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.utils.Entities;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityPersonalFairy extends EntityFairy implements IEntityPet, ITrackableEntity<EntityPersonalFairy>, IRangedAttackMob {
	
	public static final String ID = "personal_fairy";
	
	private static final String NBT_OWNER_ID = "owner_uuid";
	private static final String NBT_JOB = "job";
	private static final String NBT_ENERGY = "energy";
	private static final String NBT_MAX_ENERGY = "max_energy";
	private static final DataParameter<Optional<UUID>> DATA_OWNER = EntityDataManager.<Optional<UUID>>createKey(EntityPersonalFairy.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<FairyJob> DATA_JOB = EntityDataManager.<FairyJob>createKey(EntityPersonalFairy.class, FairyJob.instance());
	private static final DataParameter<Float> DATA_ENERGY = EntityDataManager.<Float>createKey(EntityPersonalFairy.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> DATA_MAX_ENERGY = EntityDataManager.<Float>createKey(EntityPersonalFairy.class, DataSerializers.FLOAT);
	private static final DataParameter<PetAction> DATA_PET_ACTION = EntityDataManager.<PetAction>createKey(EntityPersonalFairy.class, PetJobSerializer.instance);
	
	// Transient data, and only useful for Builders
	private static final DataParameter<Optional<BlockPos>> DATA_BUILDER_SPOT = EntityDataManager.<Optional<BlockPos>>createKey(EntityPersonalFairy.class, DataSerializers.OPTIONAL_BLOCK_POS);
	
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	private List<IEntityListener<EntityPersonalFairy>> listeners;
	
	private LivingEntity ownerCache;
	private LogisticsNetwork networkOverride;
	private int idleTicks;
	private ManagedPetInfo infoInst;
	
	private FairyCastTarget castTarget;
	private FairyPlacementTarget placementTarget;
	private Spell castSpell;
	
	private IBuildPump buildPump;
	private int buildTicks;
	
	public EntityPersonalFairy(EntityType<? extends EntityPersonalFairy> type, World world) {
		super(type, world);
		this.workDistanceSq = 20 * 20;
		listeners = new LinkedList<>();
		infoInst = PetInfo.createManaged();
	}
	
	public void setNetwork(LogisticsNetwork network) {
		if (network != this.networkOverride) {
			this.forfitTask();
		}
		this.networkOverride = network;
	}
	
	public void setFairyTargets(@Nullable Spell spell, FairyCastTarget castTarget, FairyPlacementTarget placementTarget) {
		this.castTarget = castTarget;
		this.placementTarget = placementTarget;
		this.castSpell = spell;
	}

	public void setBuildPump(IBuildPump pump) {
		this.buildPump = pump;
	}
	
	public void setBuildSpot(BlockPos pos) {
		dataManager.set(DATA_BUILDER_SPOT, Optional.ofNullable(pos));
	}
	
	public @Nullable BlockPos getBuildSpot() {
		return dataManager.get(DATA_BUILDER_SPOT).orElse(null);
	}
	
	public void cancelBuildTask() {
		setBuildSpot(null);
		this.buildTicks = 0;
	}
	
	public void setOwner(LivingEntity owner) {
		setOwner(owner.getUniqueID());
	}
	
	public void setOwner(UUID ownerID) {
		dataManager.set(DATA_OWNER, Optional.of(ownerID));
	}
	
	public UUID getOwnerId() {
		return dataManager.get(DATA_OWNER).orElse(null);
	}
	
	@Override
	public @Nullable LivingEntity getOwner() {
		if (ownerCache == null || !ownerCache.isAlive()) {
			ownerCache = null;
			UUID id = getOwnerId();
			if (id != null) {
				ownerCache = Entities.FindLiving(world, id);
			}
		}
		return ownerCache;
	}

	@Override
	public boolean isEntitySitting() {
		return false;
	}
	
	@Override
	public boolean isEntityTamed() {
		return getOwnerId() != null;
	}
	
	public FairyJob getJob() {
		return dataManager.get(DATA_JOB);
	}
	
	public void setJob(FairyJob job) {
		FairyJob oldJob = this.getJob();
		dataManager.set(DATA_JOB, job);
		
		if (oldJob != job) {
			resetAttributes(job);
		}
	}
	
	@Override
	public String getLoreKey() {
		return "personal_fairy";
	}

	@Override
	public String getLoreDisplayName() {
		return "testfairy";
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
	
	@Override
	protected boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to) {
//
//		// We want to just drop our task if our status changes from WORKING
//		if (from == FairyGeneralStatus.WORKING) {
//			this.forfitTask();
//		}
//		
//		switch (to) {
//		case IDLE:
//			setActivitySummary("status.fairy.relax");
//		case REVOLTING:
//			setActivitySummary("status.fairy.revolt");
//		case WANDERING:
//			setActivitySummary("status.fairy.wander");
//		case WORKING:
//			setActivitySummary("status.generic.working");
//		}
//		
//		return true;
		
		return super.onStatusChange(from, to);
	}

	@Override
	public ResidentType getHomeType() {
		return ResidentType.FAIRY;
	}

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		LivingEntity owner = this.getOwner();
		if (task.getSourceEntity() != owner) { // can be null, but then this will work great
			return false;
		}
		
		return super.canPerformTask(task);
//		if (task instanceof LogisticsTaskWithdrawItem) {
//			LogisticsTaskWithdrawItem retrieve = (LogisticsTaskWithdrawItem) task;
//			
//			// Check where the retrieval task wants us to go to pick up
//			BlockPos pickup = retrieve.getSource();
//			if (pickup != null && !this.canReach(pickup, true)) {
//				return false;
//			}
//			
//			// Check for pathing
//			ILogisticsComponent source = retrieve.getSourceComponent();
//			if (source == null) {
//				// entity
//				if (this.getDistanceSq(retrieve.getSourceEntity()) < .2) {
//					return true;
//				}
//				
//				return true;
//			} else {
//				BlockPos pos = source.getPosition();
//				
//				if (!world.isAirBlock(pos)) {
//					if (world.isAirBlock(pos.north())) {
//						pos = pos.north();
//					} else if (world.isAirBlock(pos.south())) {
//						pos = pos.south();
//					} else if (world.isAirBlock(pos.east())) {
//						pos = pos.east();
//					} else if (world.isAirBlock(pos.west())) {
//						pos = pos.west();
//					} else {
//						pos = pos.up();
//					}
//				}
//				
//				if (this.getDistanceSq(pos) < .2 || this.getPosition().equals(pos)) {
//					return true;
//				}
//				
//				return true;
//			}
//		} else if (task instanceof LogisticsTaskDepositItem) {
//			LogisticsTaskDepositItem deposit = (LogisticsTaskDepositItem) task;
//			
//			// Check where the retrieval task wants us to go to pick up
//			BlockPos pickup = deposit.getDestination();
//			if (pickup != null && !this.canReach(pickup, true)) {
//				return false;
//			}
//			
//			// Check for pathing
//			ILogisticsComponent source = deposit.getSourceComponent();
//			if (source == null) {
//				// entity
//				if (this.getDistanceSq(deposit.getSourceEntity()) < .2) {
//					return true;
//				}
//				
//				return true;
//			} else {
//				BlockPos pos = source.getPosition();
//				
//				if (!world.isAirBlock(pos)) {
//					if (world.isAirBlock(pos.north())) {
//						pos = pos.north();
//					} else if (world.isAirBlock(pos.south())) {
//						pos = pos.south();
//					} else if (world.isAirBlock(pos.east())) {
//						pos = pos.east();
//					} else if (world.isAirBlock(pos.west())) {
//						pos = pos.west();
//					} else {
//						pos = pos.up();
//					}
//				}
//				
//				if (this.getDistanceSq(pos) < .2 || this.getPosition().equals(pos)) {
//					return true;
//				}
//				
//				return true;
//			}
//		}
//		
//		return false;
	}
	
	@Override
	protected void finishTask() {
		if (this.getCurrentTask() != null) {
			this.setEnergy(this.getEnergy() - 5f);
		}
		super.finishTask();
		
		LivingEntity owner = getOwner();
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(owner);
		if (attr != null) {
			attr.addFairyXP(3);
		}
	}
	
	protected void finishBuild() {
		buildPump.finishTask(this, this.getBuildSpot());
		this.setEnergy(this.getEnergy() - 3.5f);
		setBuildSpot(null);
		
		LivingEntity owner = getOwner();
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(owner);
		if (attr != null) {
			attr.addFairyXP(2);
		}
	}
	
	protected void onBuildTick() {
		if (getOwner() == null) {
			this.setBuildSpot(null);
			return;
		}
		
		LivingEntity owner = this.getOwner();
		BlockPos currentBuild = this.getBuildSpot();
		
		if (currentBuild != null) {
			// Attempt to progress
			if (this.getHeldItem().isEmpty()) {
				// Go pickup item
				if (this.getDistanceSq(owner) > 3) {
					if (!this.getMoveHelper().isUpdating() || this.ticksExisted % 10 == 0) {
						// Move to pickup
						getMoveHelper().setMoveTo(owner.posX, owner.posY, owner.posZ, 1);
					}
				} else {
					// At owner. Pickup item for delivery!
					ItemStack stack = TemplateBlock.GetRequiredItem(TemplateBlock.GetTemplatedState(world, currentBuild));
					if (owner instanceof PlayerEntity) {
						PlayerEntity player = (PlayerEntity) owner;
						if (Inventories.remove(player.inventory, stack).isEmpty()) {
							; // Fall through to be added to inventory
						} else {
							// Owner didn't have item after all!
							buildPump.abandonTask(this, currentBuild);
							setBuildSpot(null);
							buildTicks = 0;
							return;
						}
					} else {
						; // Just pretend for other entities
					}
					
					this.addItem(stack);
				}
			} else if (buildTicks == 0 && this.getDistanceSq(currentBuild) > 3) {
				// Go to build spot
				if (!this.getMoveHelper().isUpdating()) {
					// Move to task
					getMoveHelper().setMoveTo(currentBuild.getX(), currentBuild.getY(), currentBuild.getZ(), 1);
				}
			} else {
				// Build
				this.buildTicks++;
				if (this.buildTicks > (20 * 3)) {
					// finish
					finishBuild();
					setBuildSpot(null);
					buildTicks = 0;
				} else {
					// Animate
					if (!this.getMoveHelper().isUpdating()) {
						EntityFeyBase.FeyWander(this, currentBuild, 1, 3);
					}
				}
			}
			
		}
		
		if (this.getBuildSpot() == null && this.getHeldItem().isEmpty()) {
			setBuildSpot(buildPump.claimTask(this)); // Will start next tick
		}
	}
	
	@Override
	protected void onIdleTick() {
		LivingEntity owner = this.getOwner();
		
		if (owner == null || !owner.isAlive()) {
			this.setPetAction(PetAction.IDLING);
			return;
		}
		
		setEnergy(getEnergy() - this.rand.nextFloat() * .02f);
		
		// Builders operate outside the fairy task system and the MC aI target system :/
		if (this.getJob() == FairyJob.BUILDER) {
			this.onBuildTick();
			
			if (this.getBuildSpot() != null) {
				// not idle
				this.idleTicks = 0;
				this.setPetAction(PetAction.WORKING);
				return;
			}
		}

		this.setPetAction(PetAction.IDLING);
		double distOwnerSq = this.getDistanceSq(owner);
		
		if (distOwnerSq > 1600) {
			// Teleport. Too far.
			this.setPosition(owner.posX, owner.posY, owner.posZ);
			this.getMoveHelper().setMoveTo(owner.posX, owner.posY, owner.posZ, 1);
		}
		
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		ItemStack heldItem = getHeldItem();
		if (!heldItem.isEmpty()) {
			// Move towards owner
			if (distOwnerSq > 2) {
				this.getMoveHelper().setMoveTo(owner.posX, owner.posY, owner.posZ, 1);
			} else {
				if (owner instanceof PlayerEntity) {
					if (((PlayerEntity) owner).inventory.addItemStackToInventory(heldItem.copy())) {
						this.removeItem(heldItem);
						heldItem = ItemStack.EMPTY;
					}
				}
				
				if (!heldItem.isEmpty()) {
					dropItem();
				}
			}
			idleTicks = 0;
			return;
		}
		
		idleTicks++;
		
		// See if we're too far away from our owner
		if (!this.getMoveHelper().isUpdating()) {
			if (owner.getDistanceSq(this) > this.wanderDistanceSq
					|| (idleTicks % 100 == 0 && rand.nextBoolean() && rand.nextBoolean())) {
				
				// Go to a random place around our home
				final BlockPos center = owner.getPosition();
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
//					while (targ.getY() > 0 && world.isAirBlock(targ)) {
//						targ = targ.down();
//					}
//					if (targ.getY() < 256) {
//						targ = targ.up();
//					}
					while (targ.getY() < 256 && !world.isAirBlock(targ)) {
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
				//if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.getMoveHelper().setMoveTo(targ.getX() + .5, targ.getY() + .5, targ.getZ() + .5, 1.0f);
				//}
				
			}
		}
	}

	@Override
	protected void onTaskTick(ILogisticsTask task) {
		super.onTaskTick(task);
		idleTicks = 0;
		this.setPetAction(PetAction.WORKING);
	}
	
	protected INostrumFeyCapability getOwnerAttr() {
		LivingEntity owner = this.getOwner();
		if (owner == null) {
			return null;
		}
		
		return NostrumFairies.getFeyWrapper(owner);
	}

	@Override
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new EntityAIFlierDiveTask<EntityPersonalFairy>(this, 1.0, 20 * 5, 16, true) {
			@Override
			public boolean shouldExecute() {
				if (getJob() != FairyJob.WARRIOR) {
					return false;
				}
				
				if (placementTarget != FairyPlacementTarget.MELEE) {
					return false;
				}
				
				return super.shouldExecute();
			}
			
			@Override
			public void attackTarget(EntityPersonalFairy entity, LivingEntity target) {
				super.attackTarget(entity, target);
				
				LivingEntity owner = getOwner();
				INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(owner);
				if (attr != null) {
					attr.addFairyXP(2);
				}
			}
		});
		
		this.goalSelector.addGoal(priority++, new EntityAIOrbitEntityGeneric<EntityPersonalFairy>(this, null, 2, 20 * 5) {
			@Override
			public boolean shouldExecute() {
				if (getJob() != FairyJob.WARRIOR) {
					return false;
				}
				
				LivingEntity owner = getOwner();
				if (owner == null) {
					return false;
				}
				
				if (getAttackTarget() == null) {
					boolean weaponDrawn = true;
					if (owner instanceof PlayerEntity) {
						ItemStack held = ((PlayerEntity) owner).getHeldItemMainhand();
						weaponDrawn = false;
						if (!held.isEmpty()) {
							if (held.getItem() instanceof SwordItem || held.getItem() instanceof BowItem) {
								weaponDrawn = true;
							} else if (held.getAttributeModifiers(EquipmentSlotType.MAINHAND).containsKey(
									SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
								weaponDrawn = true;
							}
						}
					}
					if (!weaponDrawn) {
						return false;
					}
				}
				
				return super.shouldExecute();
			}
			
			@Override
			public boolean shouldContinueExecuting() {
				LivingEntity owner = getOwner();
				if (owner == null) {
					return false;
				}
				
				if (getAttackTarget() == null) {
					boolean weaponDrawn = true;
					if (owner instanceof PlayerEntity) {
						ItemStack held = ((PlayerEntity) owner).getHeldItemMainhand();
						weaponDrawn = false;
						if (!held.isEmpty()) {
							if (held.getItem() instanceof SwordItem || held.getItem() instanceof BowItem) {
								weaponDrawn = true;
							} else if (held.getAttributeModifiers(EquipmentSlotType.MAINHAND).containsKey(
									SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
								weaponDrawn = true;
							}
						}
					}
					if (!weaponDrawn) {
						return false;
					}
				}
				
				return super.shouldContinueExecuting();
			}
			
			@Override
			protected LivingEntity getOrbitTarget() {
				return getOwner();
			}
		});
		
		this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntityPersonalFairy>(this, 20, 4, true, (fairy) -> {
			return getJob() == FairyJob.WARRIOR
					&& fairy.getAttackTarget() != null
					&& castTarget == FairyCastTarget.TARGET
					&& castSpell != null;
		}, new Spell[0]){
			@Override
			public Spell pickSpell(Spell[] spells, EntityPersonalFairy fairy) {
				// Ignore empty array and use spell from the fairy
				return castSpell;
			}
		});
		
		
		this.goalSelector.addGoal(priority++, new EntitySpellAttackTask<EntityPersonalFairy>(this, 20, 4, true, (fairy) -> {
			return getJob() == FairyJob.WARRIOR
					&& fairy.getAttackTarget() != null
					&& castTarget != FairyCastTarget.TARGET
					&& castSpell != null;
		}, new Spell[0]){
			@Override
			public Spell pickSpell(Spell[] spells, EntityPersonalFairy fairy) {
				// Ignore empty array and use spell from the fairy
				return castSpell;
			}
			
			@Override
			public @Nullable LivingEntity getTarget() {
				if (castTarget == FairyCastTarget.OWNER) {
					return entity.getOwner();
				} else if (castTarget == FairyCastTarget.SELF) {
					return entity;
				}
				return super.getTarget();
			}
		});
		
		priority = 1;
        this.targetSelector.addGoal(priority++, new OwnerHurtByTargetGoalGeneric<EntityPersonalFairy>(this) {
			@Override
			public boolean shouldExecute() {
				if (getJob() != FairyJob.WARRIOR) {
					return false;
				}
				
				return super.shouldExecute();
			}
		});
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this) {
			@Override
			public boolean shouldExecute() {
				if (getJob() != FairyJob.WARRIOR) {
					return false;
				}
				
				return super.shouldExecute();
			}
		}.setCallsForHelp(EntityPersonalFairy.class));
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
		resetAttributes(this.getJob());
	}
	
	protected void resetAttributes(FairyJob job) {
		double speed = .35;
		double health = 8.0;
		double armor = 0;
		double attack = 1;
		
		if (job == FairyJob.WARRIOR) {
			speed = .32;
			health = 12;
			armor = 2;
			attack = 3;
		} else if (job == FairyJob.LOGISTICS) {
			speed = .39;
			health = 6.0;
		}
		
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(armor);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attack);
	}
	
	@Override
	protected void registerData() {
		super.registerData();
		this.dataManager.register(DATA_OWNER, Optional.empty());
		this.dataManager.register(DATA_JOB, FairyJob.WARRIOR);
		this.dataManager.register(DATA_ENERGY, 100f);
		this.dataManager.register(DATA_MAX_ENERGY, 100f);
		this.dataManager.register(DATA_PET_ACTION, PetAction.IDLING);
		this.dataManager.register(DATA_BUILDER_SPOT, Optional.empty());
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		if (getOwnerId() != null) {
			compound.putString(NBT_OWNER_ID, getOwnerId().toString());
		}
		
		compound.putString(NBT_JOB, this.getJob().name());
		compound.putFloat(NBT_ENERGY, getEnergy());
		compound.putFloat(NBT_MAX_ENERGY, this.getMaxEnergy());
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		
		if (compound.contains(NBT_OWNER_ID)) {
			try {
				UUID id = UUID.fromString(compound.getString(NBT_OWNER_ID));
				this.dataManager.set(DATA_OWNER, Optional.ofNullable(id));
			} catch (Exception e) {
				;
			}
		}
		
		this.setJob(FairyJob.valueOf(compound.getString(NBT_JOB).toUpperCase()));
		this.setMaxEnergy(compound.getFloat(NBT_MAX_ENERGY));
		this.setEnergy(compound.getFloat(NBT_ENERGY));
	}
	
	@Override
	public boolean writeUnlessRemoved(CompoundNBT compound) {
		// Do not save in world
		return false;
	}

	@Override
	protected void onCombatTick() {
		if (this.getAttackTarget() != null && this.isOnSameTeam(this.getAttackTarget())) {
			this.setAttackTarget(null);
			return;
		}
		
		if (this.getAttackTarget() != null && !this.getAttackTarget().isAlive()) {
			this.setAttackTarget(null);
			return;
		}
		
		this.setPetAction(PetAction.ATTACKING);
		idleTicks = 0;
	}
	
	@Override
	protected void onCientTick() {
		if (this.getJob() == FairyJob.BUILDER && this.getPetAction() == PetAction.WORKING) {
			// Don't have build ticks, so just guess
			BlockPos pos = this.getBuildSpot();
			if (pos == null || this.getDistanceSq(pos) > 3) {
				return;
			}
			
			if (rand.nextFloat() < .4f) {
				// Building particles
				world.addParticle(ParticleTypes.CLOUD,
						pos.getX() + rand.nextFloat(),
						pos.getY() + .5,
						pos.getZ() + rand.nextFloat(),
						(rand.nextFloat() - .5f) * .4, 0, (rand.nextFloat() - .5f) * .4
						);
			}
			if (this.ticksExisted % 5 == 0 && rand.nextBoolean()) {
				// Building noises
				world.playSound(posX, posY, posZ, SoundEvents.BLOCK_LADDER_STEP, SoundCategory.NEUTRAL, .4f, 2f, false);
			}
		}
		
		// Spawn particles with color based on job
		final int color;
		switch (this.getJob()) {
		case BUILDER:
			color = 0x4066FF90;
			break;
		case LOGISTICS:
			color = 0x400055FF;
			break;
		case WARRIOR:
			color = 0x40FF3333;
			break;
		default:
			color = 0x40CCFFDD; // regular fairy color
			break;
		
		}
		
		NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
				1, posX, posY + getHeight()/2f, posZ, 0, 40, 0,
				new Vec3d(rand.nextFloat() * .025 - .0125, rand.nextFloat() * .025 - .0125, rand.nextFloat() * .025 - .0125), null
				).color(color));
	}
	
	@Override
	public String getSpecializationName() {
		return "Fairy"; // TODO
	}

	@Override
	protected String getUnlocPrefix() {
		return "fairy"; // TODO
	}
	
	@Override
	protected boolean shouldJoin(BlockPos pos, BlockState state, HomeBlockTileEntity te) {
		return false;
	}

	@Override
	protected void onWanderTick() {
		if (this.getOwner() != null) {
			this.changeStatus(FairyGeneralStatus.IDLE);
		} else if (this.ticksExisted > 20) {
			this.remove();
			//TODO cleanup
//			System.out.println("REMOVING CAUSE OWNERLESS (" + this.getOwnerId() + ")");
//			if (this.ownerCache != null) {
//				System.out.println("\t Had cached, but is dead? " + ownerCache);
//			}
		}
	}

	@Override
	protected void onRevoltTick() {
		;
	}
	
	@Override
	protected void verifyHome() {
		; // we don't have block homes
	}
	
	@Override
	protected boolean canWork() {
		return true;
	}
	
	@Override
	public @Nullable LogisticsNetwork getLogisticsNetwork() {
		return this.networkOverride;
	}
	
	@Override
	protected float getGrowthForTask(ILogisticsTask task) {
		return 0f;
	}

	@Override
	public void registerListener(IEntityListener<EntityPersonalFairy> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IEntityListener<EntityPersonalFairy> listener) {
		listeners.remove(listener);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (super.attackEntityFrom(source, amount)) {
			for (IEntityListener<EntityPersonalFairy> listener : this.listeners) {
				listener.onDamage(this, source, amount);
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onDeath(DamageSource cause) {
		for (IEntityListener<EntityPersonalFairy> listener : this.listeners) {
			listener.onDeath(this);
		}
		
		super.onDeath(cause);
	}
	
	public PetAction getPetAction() {
		return dataManager.get(DATA_PET_ACTION);
	}
	
	public void setPetAction(PetAction action) {
		dataManager.set(DATA_PET_ACTION, action);
	}

	public float getEnergy() {
		return dataManager.get(DATA_ENERGY);
	}
	
	public float getMaxEnergy() {
		return dataManager.get(DATA_MAX_ENERGY);
	}
	
	public void setEnergy(float energy) {
		dataManager.set(DATA_ENERGY, Math.min(energy, getMaxEnergy()));
	}
	
	public void setMaxEnergy(float maxEnergy) { 
		dataManager.set(DATA_MAX_ENERGY, maxEnergy);
		setEnergy(getEnergy());
	}
	
	public void regenEnergy() {
		setEnergy(getEnergy() + this.rand.nextFloat() * .5f);
	}
	
	public void increaseMaxEnergy(float amt) {
		setMaxEnergy(getMaxEnergy() + amt);
	}
	
	public int getIdleTicks() {
		return this.idleTicks;
	}
	
	@Override
	protected List<ILogisticsTask> getTaskList() { 
		List<ILogisticsTask> list = super.getTaskList();
		
		if (list != null) {
			list.removeIf((task) -> {
				return (task.getSourceEntity() != this.getOwner());
			});
		}
		
		return list;
	}
	
	@Override
	public boolean canReach(BlockPos pos, boolean work) {
		if (this.getOwner() == null) {
			return false;
		}
		return getOwner().getDistanceSq(pos.getX() + .5, pos.getY(), pos.getZ() + .5) < (work ? workDistanceSq : wanderDistanceSq);
	}

	@Override
	public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
		if (castSpell != null) {
			castSpell.cast(this, 1f);
			
			LivingEntity owner = getOwner();
			INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(owner);
			if (attr != null) {
				attr.addFairyXP(1);
			}
		}
	}

	@Override
	public PetInfo getPetSummary() {
		infoInst.set(getHealth(), getMaxHealth(), getEnergy(), getMaxEnergy(), SecondaryFlavor.GRADUAL_GOOD, getPetAction());
		return infoInst;
	}
	
	@Override
	public boolean isOnSameTeam(Entity entityIn) {
		if (this.isEntityTamed() && entityIn instanceof LivingEntity) {
//			LivingEntity myOwner = this.getOwner();
//
//			if (entityIn == myOwner) {
//				return true;
//			}
//
//			if (myOwner != null) {
//				
//				if (entityIn instanceof IEntityOwnable) {
//					if (((IEntityOwnable) entityIn).getOwner() == myOwner) {
//						return true;
//					}
//				}
//				
//				return myOwner.isOnSameTeam(entityIn);
//			}
			return NostrumMagica.IsSameTeam(this, (LivingEntity) entityIn);
		}

		return super.isOnSameTeam(entityIn);
	}
	
	@Override
	public @Nullable NostrumFairiesSounds getIdleSound() {
		return null;
	}
	
	public static interface IBuildPump {
		public @Nullable BlockPos claimTask(EntityPersonalFairy fairy);
		public void abandonTask(EntityPersonalFairy entityPersonalFairy, BlockPos currentBuild);
		public void finishTask(EntityPersonalFairy fairy, BlockPos pos);
	}

	@Override
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(PlayerEntity player) {
		return null;
	}

	@Override
	public PetGUIStatAdapter<? extends IEntityPet> getGUIAdapter() {
		return null;
	}
}
