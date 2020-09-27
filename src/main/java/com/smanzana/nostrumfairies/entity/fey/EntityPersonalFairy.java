package com.smanzana.nostrumfairies.entity.fey;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
import com.smanzana.nostrumfairies.entity.IEntityListener;
import com.smanzana.nostrumfairies.entity.ITrackableEntity;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.entity.IEntityTameable;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class EntityPersonalFairy extends EntityFairy implements IEntityTameable, ITrackableEntity<EntityPersonalFairy> {
	
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
	
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	private List<IEntityListener<EntityPersonalFairy>> listeners;
	
	private EntityLivingBase ownerCache;
	private LogisticsNetwork networkOverride;
	private int idleTicks;
	
	public EntityPersonalFairy(World world) {
		super(world);
		this.height = .15f;
		this.width = .15f;
		this.workDistanceSq = 20 * 20;
		listeners = new LinkedList<>();
	}
	
	public void setNetwork(LogisticsNetwork network) {
		if (network != this.networkOverride) {
			this.forfitTask();
		}
		this.networkOverride = network;
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
	protected void onIdleTick() {
		int unused; // should teleport to owner if too far away. And when energy is too low, should return and go back in gael.
		EntityLivingBase owner = this.getOwner();
		
		if (owner == null || owner.isDead) {
			return;
		}
		
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		ItemStack heldItem = getHeldItem();
		if (heldItem != null) {
			// Move towards owner
			if (owner.getDistanceSqToEntity(this) > 2) {
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
			if (owner.getDistanceSqToEntity(this) > this.wanderDistanceSq) {
				
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
//		LogisticsSubTask sub = task.getActiveSubtask();
//		if (sub != null) {
//			switch (sub.getType()) {
//			case ATTACK:
//				break;
//			case BREAK:
//				// this is where we'd play some animation?
//				if (this.rand.nextBoolean()) {
//					task.markSubtaskComplete();
//				}
//				break;
//			case IDLE:
//				if (movePos == null || !this.moveHelper.isUpdating()) {
//					if (movePos == null) {
//						final BlockPos center = sub.getPos();
//						BlockPos targ = null;
//						int attempts = 20;
//						final double maxDistSq = 25;
//						do {
//							double dist = this.rand.nextDouble() * Math.sqrt(maxDistSq);
//							float angle = (float) (this.rand.nextDouble() * (2 * Math.PI));
//							float tilt = (float) (this.rand.nextDouble() * (2 * Math.PI)) * .5f;
//							
//							targ = new BlockPos(new Vec3d(
//									center.getX() + (Math.cos(angle) * dist),
//									center.getY() + (Math.cos(tilt) * dist),
//									center.getZ() + (Math.sin(angle) * dist)));
//							while (targ.getY() > 0 && worldObj.isAirBlock(targ)) {
//								targ = targ.down();
//							}
//							if (targ.getY() < 256) {
//								targ = targ.up();
//							}
//							
//							// We've hit a non-air block. Make sure there's space above it
//							BlockPos airBlock = null;
//							for (int i = 0; i < Math.ceil(this.height); i++) {
//								if (airBlock == null) {
//									airBlock = targ.up();
//								} else {
//									airBlock = airBlock.up();
//								}
//								
//								if (!worldObj.isAirBlock(airBlock)) {
//									targ = null;
//									break;
//								}
//							}
//						} while (targ == null && attempts > 0);
//						
//						if (targ == null) {
//							targ = center.up();
//						}
//						//if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
//							this.moveHelper.setMoveTo(targ.getX() + .5, targ.getY() + .5, targ.getZ() + .5, 1.0f);
//						//}
//						this.movePos = targ;
//					} else {
//						task.markSubtaskComplete();
//						// Cheat and see if we just finished idling
//						if (sub != task.getActiveSubtask()) {
//							this.movePos = null;
//						}
//					}
//				}
//				break;
//			case MOVE:
//				{
//					if (movePos == null || !this.moveHelper.isUpdating()) {
//						// First time through?
//						if ((movePos != null && this.getDistanceSqToCenter(movePos) < .5)
//							|| (moveEntity != null && this.getDistanceToEntity(moveEntity) < .5)) {
//							task.markSubtaskComplete();
//							movePos = null;
//							moveEntity = null;
//							return;
//						}
//						movePos = null;
//						moveEntity = null;
//						
//						movePos = sub.getPos();
//						if (movePos == null) {
//							moveEntity = sub.getEntity();
//							//if (!this.getNavigator().tryMoveToEntityLiving(moveEntity,  1)) {
//								this.moveHelper.setMoveTo(moveEntity.posX, moveEntity.posY, moveEntity.posZ, 1.0f);
//							//}
//						} else {
//							if (!worldObj.isAirBlock(movePos)) {
//								if (worldObj.isAirBlock(movePos.north())) {
//									movePos = movePos.north();
//								} else if (worldObj.isAirBlock(movePos.south())) {
//									movePos = movePos.south();
//								} else if (worldObj.isAirBlock(movePos.east())) {
//									movePos = movePos.east();
//								} else if (worldObj.isAirBlock(movePos.west())) {
//									movePos = movePos.west();
//								} else {
//									movePos = movePos.up();
//								}
//							}
//							//if (!this.getNavigator().tryMoveToXYZ(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5, 1.0f)) {
//								this.moveHelper.setMoveTo(movePos.getX() + .5, movePos.getY() + .5, movePos.getZ() + .5, 1.0f);
//							//}
//						}
//					}
////					else {
////						// Check if we're close to where we need to be
////						double distSq;
////						if (movePos == null) {
////							distSq = this.getDistanceSqToEntity(moveEntity);
////						} else {
////							distSq = this.getDistanceSq(movePos);
////						}
////						
////						if (distSq < .2) {
////							task.markSubtaskComplete();
////							this.navigator.clearPathEntity();
////						}
////					}
//					// FIXME this runs every tick. Save pos?
////					BlockPos pos = sub.getPos();
////					if (pos == null) {
////						EntityLivingBase entity = sub.getEntity();
////						
////					} else {
////						if (worldObj.isAirBlock(pos.north())) {
////							pos = pos.north();
////						} else if (worldObj.isAirBlock(pos.south())) {
////							pos = pos.south();
////						} else if (worldObj.isAirBlock(pos.east())) {
////							pos = pos.east();
////						} else if (worldObj.isAirBlock(pos.west())) {
////							pos = pos.west();
////						}
////						
////						// TODO FIXME I think this is constantly making new paths?
////						if (getPosition().distanceSq(pos) < .2) {
////							task.markSubtaskComplete();
////						} else if (!moveHelper.isUpdating()) {
////							if (!this.getNavigator().tryMoveToXYZ(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.0f)) {
////								this.moveHelper.setMoveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.0f);
////							}
////						}
////					}
//				}
//				break;
//			}
//		}
	}

	@Override
	protected void initEntityAI() {
		// TODO Auto-generated method stub
		// I guess we should wander and check if tehre's a home nearby and if so make it our home and stop wandering.
		// Or if we're revolting... just quit for this test one?
		// Or if we're working, dont use AI
		// Or if we're idle... wander?
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
		
		if (job == FairyJob.WARRIOR) {
			speed = .32;
			health = 12;
			armor = 2;
		} else if (job == FairyJob.LOGISTICS) {
			speed = .39;
			health = 6.0;
		}
		
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(armor);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DATA_OWNER, Optional.absent());
		this.dataManager.register(DATA_JOB, FairyJob.WARRIOR);
		this.dataManager.register(DATA_ENERGY, 100f);
		this.dataManager.register(DATA_MAX_ENERGY, 100f);
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
		; // No combat
		idleTicks = 0;
	}
	
	@Override
	protected void onCientTick() {
		;
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
}
