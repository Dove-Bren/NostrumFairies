package com.smanzana.nostrumfairies.entity.fey;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.entity.IEntityListener;
import com.smanzana.nostrumfairies.entity.ITrackableEntity;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory.FairyCastTarget;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory.FairyPlacementTarget;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.PetInfo;
import com.smanzana.nostrummagica.entity.PetInfo.ManagedPetInfo;
import com.smanzana.nostrummagica.entity.PetInfo.PetAction;
import com.smanzana.nostrummagica.entity.PetInfo.SecondaryFlavor;
import com.smanzana.nostrummagica.entity.tasks.EntityAIFlierDiveTask;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOrbitEntityGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOwnerHurtByTargetGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntityAIOwnerHurtTargetGeneric;
import com.smanzana.nostrummagica.entity.tasks.EntitySpellAttackTask;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.oredict.OreDictionary;

public class EntityPersonalFairy extends EntityFairy implements IEntityPet, ITrackableEntity<EntityPersonalFairy>, IRangedAttackMob {
	
	public static enum FairyJob {
		WARRIOR,
		BUILDER,
		LOGISTICS;
		
		public final static class JobSerializer implements DataSerializer<FairyJob> {
			
			private JobSerializer() {
				DataSerializers.registerSerializer(this);
			}
			
			@Override
			public void write(PacketBuffer buf, FairyJob value) {
				buf.writeEnumValue(value);
			}

			@Override
			public FairyJob read(PacketBuffer buf) throws IOException {
				return buf.readEnumValue(FairyJob.class);
			}

			@Override
			public DataParameter<FairyJob> createKey(int id) {
				return new DataParameter<>(id, this);
			}
		}
		
		public static final JobSerializer Serializer = new JobSerializer();
	}

	private static final String NBT_OWNER_ID = "owner_uuid";
	private static final String NBT_JOB = "job";
	private static final String NBT_ENERGY = "energy";
	private static final String NBT_MAX_ENERGY = "max_energy";
	private static final DataParameter<Optional<UUID>> DATA_OWNER = EntityDataManager.<Optional<UUID>>createKey(EntityPersonalFairy.class, DataSerializers.OPTIONAL_UNIQUE_ID);
	private static final DataParameter<FairyJob> DATA_JOB = EntityDataManager.<FairyJob>createKey(EntityPersonalFairy.class, FairyJob.Serializer);
	private static final DataParameter<Float> DATA_ENERGY = EntityDataManager.<Float>createKey(EntityPersonalFairy.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> DATA_MAX_ENERGY = EntityDataManager.<Float>createKey(EntityPersonalFairy.class, DataSerializers.FLOAT);
	private static final DataParameter<PetAction> DATA_PET_ACTION = EntityDataManager.<PetAction>createKey(EntityPersonalFairy.class, PetAction.Serializer);
	
	// Transient data, and only useful for Builders
	private static final DataParameter<Optional<BlockPos>> DATA_BUILDER_SPOT = EntityDataManager.<Optional<BlockPos>>createKey(EntityPersonalFairy.class, DataSerializers.OPTIONAL_BLOCK_POS);
	
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	private List<IEntityListener<EntityPersonalFairy>> listeners;
	
	private EntityLivingBase ownerCache;
	private LogisticsNetwork networkOverride;
	private int idleTicks;
	private ManagedPetInfo infoInst;
	
	private FairyCastTarget castTarget;
	private FairyPlacementTarget placementTarget;
	private Spell castSpell;
	
	private IBuildPump buildPump;
	private int buildTicks;
	
	public EntityPersonalFairy(World world) {
		super(world);
		this.height = .15f;
		this.width = .15f;
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
		dataManager.set(DATA_BUILDER_SPOT, Optional.fromNullable(pos));
	}
	
	public @Nullable BlockPos getBuildSpot() {
		return dataManager.get(DATA_BUILDER_SPOT).orNull();
	}
	
	public void cancelBuildTask() {
		setBuildSpot(null);
		this.buildTicks = 0;
	}
	
	public void setOwner(EntityLivingBase owner) {
		setOwner(owner.getUniqueID());
	}
	
	public void setOwner(UUID ownerID) {
		dataManager.set(DATA_OWNER, Optional.of(ownerID));
	}
	
	@Override
	public UUID getOwnerId() {
		return dataManager.get(DATA_OWNER).orNull();
	}
	
	@Override
	public @Nullable EntityLivingBase getOwner() {
		if (ownerCache == null || ownerCache.isDead) {
			ownerCache = null;
			UUID id = getOwnerId();
			if (id != null) {
				for (World world : DimensionManager.getWorlds()) {
					for (Entity e : world.loadedEntityList) {
						if (e instanceof EntityLivingBase && e.getUniqueID().equals(id)) {
							ownerCache = (EntityLivingBase) e;
							break;
						}
					}
				}
			}
		}
		return ownerCache;
	}
	
	@Override
	public boolean isTamed() {
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
		EntityLivingBase owner = this.getOwner();
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
//				if (this.getDistanceSqToEntity(retrieve.getSourceEntity()) < .2) {
//					return true;
//				}
//				
//				return true;
//			} else {
//				BlockPos pos = source.getPosition();
//				
//				if (!worldObj.isAirBlock(pos)) {
//					if (worldObj.isAirBlock(pos.north())) {
//						pos = pos.north();
//					} else if (worldObj.isAirBlock(pos.south())) {
//						pos = pos.south();
//					} else if (worldObj.isAirBlock(pos.east())) {
//						pos = pos.east();
//					} else if (worldObj.isAirBlock(pos.west())) {
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
//				if (this.getDistanceSqToEntity(deposit.getSourceEntity()) < .2) {
//					return true;
//				}
//				
//				return true;
//			} else {
//				BlockPos pos = source.getPosition();
//				
//				if (!worldObj.isAirBlock(pos)) {
//					if (worldObj.isAirBlock(pos.north())) {
//						pos = pos.north();
//					} else if (worldObj.isAirBlock(pos.south())) {
//						pos = pos.south();
//					} else if (worldObj.isAirBlock(pos.east())) {
//						pos = pos.east();
//					} else if (worldObj.isAirBlock(pos.west())) {
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
		
		EntityLivingBase owner = getOwner();
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(owner);
		if (attr != null) {
			attr.addFairyXP(3);
		}
	}
	
	protected void finishBuild() {
		buildPump.finishTask(this, this.getBuildSpot());
		this.setEnergy(this.getEnergy() - 3.5f);
		setBuildSpot(null);
		
		EntityLivingBase owner = getOwner();
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
		
		EntityLivingBase owner = this.getOwner();
		BlockPos currentBuild = this.getBuildSpot();
		
		if (currentBuild != null) {
			// Attempt to progress
			if (this.getHeldItem() == null) {
				// Go pickup item
				if (this.getDistanceSqToEntity(owner) > 3) {
					if (!this.moveHelper.isUpdating() || this.ticksExisted % 10 == 0) {
						// Move to pickup
						moveHelper.setMoveTo(owner.posX, owner.posY, owner.posZ, 1);
					}
				} else {
					// At owner. Pickup item for delivery!
					ItemStack stack = TemplateBlock.GetRequiredItem(TemplateBlock.GetTemplatedState(worldObj, currentBuild));
					if (owner instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer) owner;
						if (Inventories.remove(player.inventory, stack) == null) {
							if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
								stack.setItemDamage(0);
							}
							
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
				if (!this.moveHelper.isUpdating()) {
					// Move to task
					moveHelper.setMoveTo(currentBuild.getX(), currentBuild.getY(), currentBuild.getZ(), 1);
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
					if (!this.moveHelper.isUpdating()) {
						EntityFeyBase.FeyWander(this, currentBuild, 1, 3);
					}
				}
			}
			
		}
		
		if (this.getBuildSpot() == null && this.getHeldItem() == null) {
			setBuildSpot(buildPump.claimTask(this)); // Will start next tick
		}
	}
	
	@Override
	protected void onIdleTick() {
		EntityLivingBase owner = this.getOwner();
		
		if (owner == null || owner.isDead) {
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
		double distOwnerSq = this.getDistanceSqToEntity(owner);
		
		if (distOwnerSq > 1600) {
			// Teleport. Too far.
			this.setPosition(owner.posX, owner.posY, owner.posZ);
			this.moveHelper.setMoveTo(owner.posX, owner.posY, owner.posZ, 1);
		}
		
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		ItemStack heldItem = getHeldItem();
		if (heldItem != null) {
			// Move towards owner
			if (distOwnerSq > 2) {
				this.moveHelper.setMoveTo(owner.posX, owner.posY, owner.posZ, 1);
			} else {
				if (owner instanceof EntityPlayer) {
					if (((EntityPlayer) owner).inventory.addItemStackToInventory(heldItem.copy())) {
						this.removeItem(heldItem);
						heldItem = null;
					}
				}
				
				if (heldItem != null) {
					dropItem();
				}
			}
			idleTicks = 0;
			return;
		}
		
		idleTicks++;
		
		// See if we're too far away from our owner
		if (!this.moveHelper.isUpdating()) {
			if (owner.getDistanceSqToEntity(this) > this.wanderDistanceSq
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
//					while (targ.getY() > 0 && worldObj.isAirBlock(targ)) {
//						targ = targ.down();
//					}
//					if (targ.getY() < 256) {
//						targ = targ.up();
//					}
					while (targ.getY() < 256 && !worldObj.isAirBlock(targ)) {
						targ = targ.up();
					}
					
					// We've hit a non-air block. Make sure there's space above it
					BlockPos airBlock = null;
					for (int i = 0; i < Math.ceil(this.height); i++) {
						if (airBlock == null) {
							airBlock = targ.up();
						} else {
							airBlock = airBlock.up();
						}
						
						if (!worldObj.isAirBlock(airBlock)) {
							targ = null;
							break;
						}
					}
				} while (targ == null && attempts > 0);
				
				if (targ == null) {
					targ = center.up();
				}
				//if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.moveHelper.setMoveTo(targ.getX() + .5, targ.getY() + .5, targ.getZ() + .5, 1.0f);
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
		EntityLivingBase owner = this.getOwner();
		if (owner == null) {
			return null;
		}
		
		return NostrumFairies.getFeyWrapper(owner);
	}

	@Override
	protected void initEntityAI() {
		int priority = 1;
		this.tasks.addTask(priority++, new EntityAIFlierDiveTask<EntityPersonalFairy>(this, 1.0, 20 * 5, 16, true) {
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
			public void attackTarget(EntityPersonalFairy entity, EntityLivingBase target) {
				super.attackTarget(entity, target);
				
				EntityLivingBase owner = getOwner();
				INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(owner);
				if (attr != null) {
					attr.addFairyXP(2);
				}
			}
		});
		
		this.tasks.addTask(priority++, new EntityAIOrbitEntityGeneric<EntityPersonalFairy>(this, null, 2, 20 * 5) {
			@Override
			public boolean shouldExecute() {
				if (getJob() != FairyJob.WARRIOR) {
					return false;
				}
				
				EntityLivingBase owner = getOwner();
				if (owner == null) {
					return false;
				}
				
				if (getAttackTarget() == null) {
					boolean weaponDrawn = true;
					if (owner instanceof EntityPlayer) {
						ItemStack held = ((EntityPlayer) owner).getHeldItemMainhand();
						weaponDrawn = false;
						if (held != null) {
							if (held.getItem() instanceof ItemSword || held.getItem() instanceof ItemBow) {
								weaponDrawn = true;
							} else if (held.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).containsKey(
									SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName())) {
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
			public boolean continueExecuting() {
				EntityLivingBase owner = getOwner();
				if (owner == null) {
					return false;
				}
				
				if (getAttackTarget() == null) {
					boolean weaponDrawn = true;
					if (owner instanceof EntityPlayer) {
						ItemStack held = ((EntityPlayer) owner).getHeldItemMainhand();
						weaponDrawn = false;
						if (held != null) {
							if (held.getItem() instanceof ItemSword || held.getItem() instanceof ItemBow) {
								weaponDrawn = true;
							} else if (held.getAttributeModifiers(EntityEquipmentSlot.MAINHAND).containsKey(
									SharedMonsterAttributes.ATTACK_DAMAGE.getAttributeUnlocalizedName())) {
								weaponDrawn = true;
							}
						}
					}
					if (!weaponDrawn) {
						return false;
					}
				}
				
				return super.continueExecuting();
			}
			
			@Override
			protected EntityLivingBase getOrbitTarget() {
				return getOwner();
			}
		});
		
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityPersonalFairy>(this, 20, 4, true, (fairy) -> {
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
		
		
		this.tasks.addTask(priority++, new EntitySpellAttackTask<EntityPersonalFairy>(this, 20, 4, true, (fairy) -> {
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
		});
		
		priority = 1;
        this.targetTasks.addTask(priority++, new EntityAIOwnerHurtByTargetGeneric<EntityPersonalFairy>(this) {
			@Override
			public boolean shouldExecute() {
				if (getJob() != FairyJob.WARRIOR) {
					return false;
				}
				
				return super.shouldExecute();
			}
		});
        this.targetTasks.addTask(priority++, new EntityAIOwnerHurtTargetGeneric<EntityPersonalFairy>(this) {
			@Override
			public boolean shouldExecute() {
				if (getJob() != FairyJob.WARRIOR) {
					return false;
				}
				
				return super.shouldExecute();
			}
		});
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[0]) {
			@Override
			public boolean shouldExecute() {
				if (getJob() != FairyJob.WARRIOR) {
					return false;
				}
				
				return super.shouldExecute();
			}
		});
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
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
		
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(armor);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(attack);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DATA_OWNER, Optional.absent());
		this.dataManager.register(DATA_JOB, FairyJob.WARRIOR);
		this.dataManager.register(DATA_ENERGY, 100f);
		this.dataManager.register(DATA_MAX_ENERGY, 100f);
		this.dataManager.register(DATA_PET_ACTION, PetAction.IDLING);
		this.dataManager.register(DATA_BUILDER_SPOT, Optional.absent());
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		if (getOwnerId() != null) {
			compound.setString(NBT_OWNER_ID, getOwnerId().toString());
		}
		
		compound.setString(NBT_JOB, this.getJob().name());
		compound.setFloat(NBT_ENERGY, getEnergy());
		compound.setFloat(NBT_MAX_ENERGY, this.getMaxEnergy());
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		if (compound.hasKey(NBT_OWNER_ID)) {
			try {
				UUID id = UUID.fromString(compound.getString(NBT_OWNER_ID));
				this.dataManager.set(DATA_OWNER, Optional.fromNullable(id));
			} catch (Exception e) {
				;
			}
		}
		
		this.setJob(FairyJob.valueOf(compound.getString(NBT_JOB).toUpperCase()));
		this.setMaxEnergy(compound.getFloat(NBT_MAX_ENERGY));
		this.setEnergy(compound.getFloat(NBT_ENERGY));
	}
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		// Do not save in world
		return false;
	}

	@Override
	protected void onCombatTick() {
		if (this.getAttackTarget() != null && this.isOnSameTeam(this.getAttackTarget())) {
			this.setAttackTarget(null);
			return;
		}
		
		if (this.getAttackTarget() != null && this.getAttackTarget().isDead) {
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
				worldObj.spawnParticle(EnumParticleTypes.CLOUD,
						pos.getX() + rand.nextFloat(),
						pos.getY() + .5,
						pos.getZ() + rand.nextFloat(),
						(rand.nextFloat() - .5f) * .4, 0, (rand.nextFloat() - .5f) * .4,
						new int[0]);
			}
			if (this.ticksExisted % 5 == 0 && rand.nextBoolean()) {
				// Building noises
				worldObj.playSound(posX, posY, posZ, SoundEvents.BLOCK_LADDER_STEP, SoundCategory.NEUTRAL, .4f, 2f, false);
			}
		}
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
	protected boolean shouldJoin(BlockPos pos, IBlockState state, HomeBlockTileEntity te) {
		return false;
	}

	@Override
	protected void onWanderTick() {
		if (this.getOwner() != null) {
			this.changeStatus(FairyGeneralStatus.IDLE);
		} else if (this.ticksExisted > 20) {
			worldObj.removeEntity(this);
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
	public boolean isSitting() {
		return false;
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
		return getOwner().getDistanceSq(pos) < (work ? workDistanceSq : wanderDistanceSq);
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
		if (castSpell != null) {
			castSpell.cast(this, 1f);
			
			EntityLivingBase owner = getOwner();
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
		if (this.isTamed()) {
			EntityLivingBase myOwner = this.getOwner();

			if (entityIn == myOwner) {
				return true;
			}

			if (myOwner != null) {
				
				if (entityIn instanceof IEntityOwnable) {
					if (((IEntityOwnable) entityIn).getOwner() == myOwner) {
						return true;
					}
				}
				
				return myOwner.isOnSameTeam(entityIn);
			}
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
}
