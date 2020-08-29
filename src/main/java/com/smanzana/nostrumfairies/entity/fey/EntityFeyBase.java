package com.smanzana.nostrumfairies.entity.fey;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.entity.navigation.PathNavigatorLogistics;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrummagica.loretag.ILoreTagged;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityFeyBase extends EntityGolem implements IFeyWorker, ILoreTagged {

	protected static final DataParameter<Optional<BlockPos>> HOME  = EntityDataManager.<Optional<BlockPos>>createKey(EntityFeyBase.class, DataSerializers.OPTIONAL_BLOCK_POS);
	protected static final DataParameter<String> NAME = EntityDataManager.<String>createKey(EntityFeyBase.class, DataSerializers.STRING);
	protected static final DataParameter<FairyGeneralStatus> STATUS  = EntityDataManager.<FairyGeneralStatus>createKey(EntityFeyBase.class, FairyGeneralStatus.Serializer);
	protected static final DataParameter<String> ACTIVITY = EntityDataManager.<String>createKey(EntityFeyBase.class, DataSerializers.STRING);
	
	/**
	 * Maximum amount of distance (squared) a fairy will freely wander away from its home.
	 */
	protected double wanderDistanceSq;
	
	/**
	 * Maximum amount of distance (squared) a fairy can be away from home while performing a job.
	 */
	protected double workDistanceSq;
	
	/**
	 * Current task being performed.
	 */
	private @Nullable ILogisticsTask currentTask;
	
	/**
	 * Number of ticks the current task has been being run
	 */
	private int taskTickCount;
	
	/**
	 * Slowly updates pos used to figure out if we get stuck while on the job!
	 */
	private BlockPos lastTaskPos;
	
	
	public EntityFeyBase(World world) {
		this(world, 100, MAX_FAIRY_DISTANCE_SQ);
	}
	
	public EntityFeyBase(World world, double wanderDistanceSq, double workDistanceSq) {
		super(world);
		this.wanderDistanceSq = wanderDistanceSq;
		this.workDistanceSq = workDistanceSq;
		
		this.navigator = new PathNavigatorLogistics(this, world);
	}
	
	@Override
	public FairyGeneralStatus getStatus() {
		return dataManager.get(STATUS);
	}
	
	/**
	 * Attempt to transition to the new status type.
	 * Fairy implementations have the option of refusing a state change. This is merely a request.
	 * @param status
	 * @return true when the status change went thr ough
	 */
	protected boolean changeStatus(FairyGeneralStatus status) {
		if (worldObj.isRemote) {
			return true;
		}
		
		if (onStatusChange(getStatus(), status)) {
			dataManager.set(STATUS, status);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Called whenever the fairy's state is attempting to change.
	 * @param from Current state
	 * @param to State trying to transition to
	 * @return true to allow the change, or false to reject it
	 */
	protected abstract boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to);
	
	@Override
	@Nullable
	public BlockPos getHome() {
		return this.dataManager.get(HOME).orNull();
	}
	
	protected @Nullable HomeBlockTileEntity getHomeEnt(BlockPos pos) {
		if (!worldObj.isBlockLoaded(pos)) {
			return null;
		}
		
		IBlockState state = worldObj.getBlockState(pos);
		if (state == null || !(state.getBlock() instanceof FeyHomeBlock)) {
			return null;
		}
		
		BlockPos center = ((FeyHomeBlock) state.getBlock()).getMasterPos(worldObj, pos, state);
		TileEntity te = worldObj.getTileEntity(center);
		if (te == null || !(te instanceof HomeBlockTileEntity)) {
			return null;
		}
		
		return (HomeBlockTileEntity) te;
	}
	
	protected @Nullable HomeBlockTileEntity getHomeEnt() {
		BlockPos homePos = this.getHome();
		if (homePos == null) {
			return null;
		}
		
		return getHomeEnt(homePos);
	}
	
	/**
	 * Set the block the fairy will consider its home, and try to stay close to.
	 * @return
	 */
	public void setHome(@Nullable BlockPos home) {
		HomeBlockTileEntity ent = getHomeEnt();
		if (ent != null) {
			ent.removeResident(this);
		}
		
		if (home != null) {
			home = home.toImmutable();
		}
		this.dataManager.set(HOME, Optional.fromNullable(home));
		this.setHomePosAndDistance(home, home == null ? -1 : (int) this.wanderDistanceSq);
		
		if (home != null) {
			ent = getHomeEnt();
			if (ent != null) {
				ent.addResident(this);
			}
		}
	}
	
	/**
	 * Called to check that the provided home position is still a valid home position
	 * @param homePos
	 * @return
	 */
	protected boolean isValidHome(BlockPos homePos) {
		HomeBlockTileEntity ent = getHomeEnt(homePos);
		if (ent != null) {
			return ent.canAccept(this);
		}
		
		return false;
	}
	
	/**
	 * Checks whether or not the provided location is within this worker's reach
	 * @param work
	 * @return
	 */
	public boolean canReach(BlockPos pos, boolean work) {
		BlockPos home = this.getHome();
		if (home == null) {
			return false;
		}
		
		return home.distanceSq(pos) < (work ? workDistanceSq : wanderDistanceSq);
	}
	
	/**
	 * Returns the logistics network this fairy is part of.
	 * The default implementation uses the home position and insists that a tile entity exists there
	 * that is a logistics component.
	 * Subclasses can override this to do whatever they want.
	 * @return
	 */
	public @Nullable LogisticsNetwork getLogisticsNetwork() {
		verifyHome();
		HomeBlockTileEntity ent = getHomeEnt();
		if (ent != null) {
			return ent.getNetwork();
		}
		
		 return null;
	}
	
	@Override
	public @Nullable ILogisticsTask getCurrentTask() {
		return currentTask;
	}
	
	protected void forceSetTask(@Nullable ILogisticsTask task) {
		ILogisticsTask oldtask = this.getCurrentTask();
		this.currentTask = task;
		this.taskTickCount = 0;
		this.onTaskChange(oldtask, task);
	}
	
	@Override
	public void dropTask(ILogisticsTask droppedTask) {
		// Our 'currentTask' may be composite, so dissolve, drop the dropped task out of the list, and then recombine.
		Collection<ILogisticsTask> tasks = this.currentTask.unmerge();
		Iterator<ILogisticsTask> iter = tasks.iterator();
		while (iter.hasNext()) {
			if (iter.next() == droppedTask) {
				iter.remove();
				break;
			}
		}
		
		if (tasks.isEmpty()) {	
			forceSetTask(null);
		} else {
			// all tasks remaining in tasks list are still claimed by us. Merge back up.
			ILogisticsTask newTask = null;
			for (ILogisticsTask task : tasks) {
				if (newTask == null) {
					newTask = task;
				} else {
					newTask = newTask.mergeIn(task);
				}
			}
			this.forceSetTask(newTask);
		}
	}
	
	/**
	 * Checks whether this fairy is _Capable_ of performing the provided task.
	 * This checks compatibility, not availability. So even if a fairy is currently performing
	 * some other task, it should return true here.
	 * @param task
	 * @return
	 */
	protected abstract boolean canPerformTask(ILogisticsTask task);
	
	/**
	 * Check whether a fairy should start performing the provided task.
	 * Usually, this is only called when the fairy is idle and looking for work.
	 * Callers may wish to verify their state before accepting a new task to avoid
	 * inventory problems, etc.
	 * @param task
	 * @return
	 */
	protected abstract boolean shouldPerformTask(ILogisticsTask task);
	
	/**
	 * Check whether this entity's needs (and home) are in line and work tasks can be picked up.
	 * @return
	 */
	protected boolean canWork() {
		this.verifyHome();
		return (getHomeEnt() != null && getHomeEnt().canWork());
	}
	
	protected float getGrowthForTask(ILogisticsTask task) {
		return .1f;
	}
	
	protected void finishTask() {
		// This _could_ immediately look for a new task.
		// For now, I think 1 frame delay between having a new job is okay.
		// That'll also mean that code that calls this and then looks at the task after will get NULL instead
		// of possibly the same task or possibly a new one, which might make debugging easier.
		if (this.currentTask != null) {
			float amt = 0;
			for (ILogisticsTask task : this.currentTask.unmerge()) {
				this.getLogisticsNetwork().getTaskRegistry().completeTask(task);
				amt += getGrowthForTask(task);
			}
			forceSetTask(null);
			HomeBlockTileEntity home = this.getHomeEnt();
			if (home != null) {
				home.addGrowth(amt); // TODO multiply by happiness?
			}
		}
	}
	
	protected void forfitTask() {
		if (this.currentTask != null) {
			if (this.getLogisticsNetwork() != null) {
				for (ILogisticsTask task : this.currentTask.unmerge()) {
					this.getLogisticsNetwork().getTaskRegistry().forfitTask(task);
				}
			}
			forceSetTask(null);
		}
	}
	
	/**
	 * Called when the current task changes. This includes when the task is reset and there is no new task.
	 * Note: This will be called whenever one task that we have taken is dropped, and we're creating a new composite.
	 * @param oldTask The task that was just given up. 
	 * @param newTask The new task.
	 */
	protected abstract void onTaskChange(@Nullable ILogisticsTask oldTask, @Nullable ILogisticsTask newTask);
	
	/**
	 * Called while wandering for each prospective housing location.
	 * The home block has already been checked for availability and for basic type compatibility.
	 * @param pos
	 * @param state
	 * @param te
	 * @return
	 */
	protected abstract boolean shouldJoin(BlockPos pos, IBlockState state, HomeBlockTileEntity te);
	
	/**
	 * Called every tick that a task is active.
	 * @param task
	 */
	protected abstract void onTaskTick(@Nullable ILogisticsTask task);
	
	protected abstract void onIdleTick();
	
	protected abstract void onCombatTick();
	
	protected abstract void onWanderTick();
	
	protected abstract void onRevoltTick();
	
	@SideOnly(Side.CLIENT)
	protected abstract void onCientTick();
	
	/**
	 * Called periodically while doing task ticks to see if we should see if any other tasks are out there that
	 * we could also pick up. Fairies should return false if they're far enough in their task that picking up more
	 * doesn't make sense. For example, if you ALREADY picked up an item in an item withdraw task.
	 * @return
	 */
	protected abstract boolean canMergeMoreJobs();
	
	@Override
	protected abstract void initEntityAI();
	
	protected int getDefaultSwingAnimationDuration() {
		return 6;
	}
	
	/**
	 * Returns an integer indicating the end point of the swing animation, used by {@link #swingProgress} to provide a
	 * progress indicator. Takes dig speed enchantments into account.
	 * Note: Copied from vanilla where you can't override it :(
	 */
	protected int getArmSwingAnimationEnd() {
		return this.isPotionActive(MobEffects.HASTE)
				? getDefaultSwingAnimationDuration() - (1 + this.getActivePotionEffect(MobEffects.HASTE).getAmplifier())
				: (this.isPotionActive(MobEffects.MINING_FATIGUE)
						? getDefaultSwingAnimationDuration() + (1 + this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2
						: getDefaultSwingAnimationDuration());
	}

	@Override
	public void swingArm(EnumHand hand) {
		ItemStack stack = this.getHeldItem(hand);
		if (stack != null && stack.getItem() != null) {
			if (stack.getItem().onEntitySwing(this, stack)) {
				return;
			}
		}
		
		if (!this.isSwingInProgress || this.swingProgressInt >= this.getArmSwingAnimationEnd() / 2 || this.swingProgressInt < 0) {
			this.swingProgressInt = -1;
			this.isSwingInProgress = true;
			this.swingingHand = hand;

			if (this.worldObj instanceof WorldServer) {
				((WorldServer)this.worldObj).getEntityTracker().sendToAllTrackingEntity(this, new SPacketAnimation(this, hand == EnumHand.MAIN_HAND ? 0 : 3));
			}
		}
	}
	
	protected void teleportHome() {
		BlockPos target = findEmptySpot(this.getHome(), false);
		this.attemptTeleport(target.getX() + .5, target.getY() + .05, target.getZ() + .5);
	}
	
	@Override
	protected void updateArmSwingProgress() {
		int i = this.getArmSwingAnimationEnd();

		if (this.isSwingInProgress) {
			++this.swingProgressInt;

			if (this.swingProgressInt >= i) {
				this.swingProgressInt = 0;
				this.isSwingInProgress = false;
			}
		}else {
			this.swingProgressInt = 0;
		}

		this.swingProgress = (float)this.swingProgressInt / (float)i;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		this.updateArmSwingProgress();
		
		if (worldObj.isRemote) {
			onCientTick();
		}
		
		if (worldObj.isRemote || this.isDead) {
			return;
		}
		
		// If we're in combat, ignore all the rest
		if (this.getAttackTarget() != null) {
			if (this.getAttackTarget().isDead) {
				this.setAttackTarget(null);;
			} else {
				this.onCombatTick();
				return;
			}
		}
		
		// If we're supposed to have a home, verify it's still there and fix state.
		verifyHome();
		
		switch (getStatus()) {
		case IDLE:
			onIdleTick();
			
			// Note: idle may decide to do task stuff on its own. We'll respect that.
			if (currentTask == null && canWork()) {
				if (!searchForJobs()) {
					break;
				}
			}
			
			changeStatus(FairyGeneralStatus.WORKING);
			// else fall through to start working as soon as task is picked up
			// TODO maybe both should search for tasks if the task is dropable?
		case WORKING:
			if (currentTask != null) {
				this.taskTickCount++;
				
				if (this.taskTickCount % 5 == 0) {
					// Make sure task is still okay
					if (!this.currentTask.isValid()) {
						forfitTask();
					} else if (this.canMergeMoreJobs()) {
						this.searchForJobs();
					}
				}
				
				if (currentTask != null) {
					if (taskTickCount > 0 && taskTickCount % (20 * 60) == 0) {
						BlockPos pos = this.getPosition();
						if (pos.equals(lastTaskPos)) {
							// Stuck! Teleport!
							teleportHome();
							lastTaskPos = null;
						} else {
							lastTaskPos = pos;
						}
					}
					this.onTaskTick(currentTask);
				}
				
				if (currentTask != null && currentTask.isComplete()) {
					finishTask();
				}
			}
			
			// This isn't an else. That means it catches where we didn't have a task,
			// as well as when our task was just switched to be empty
			if (currentTask == null) {
				// Like in finishTask, this _could_ immediately look for a replacement. Switching to idle for
				// better debugging -- and so that we switch into 'working' mode every time we get a new task.
				this.changeStatus(FairyGeneralStatus.IDLE);
			}
			break;
		case REVOLTING:
			onRevoltTick();
			break;
		case WANDERING:
			// Look for nearby homes
			if (this.ticksExisted % 100 == 0) {
				int radius = 3;
				MutableBlockPos cursor = new MutableBlockPos();
				for (int x = -radius; x <= radius; x++)
				for (int z = -radius; z <= radius; z++)
				for (int y = -radius; y <= radius; y++) {
					cursor.setPos(posX + x, posY + y, posZ + z);
					if (!worldObj.isBlockLoaded(cursor)) {
						continue;
					}
					
					IBlockState state = worldObj.getBlockState(cursor);
					if (state.getBlock() instanceof FeyHomeBlock) {
						FeyHomeBlock block = (FeyHomeBlock) state.getBlock();
						if (!block.isCenter(state)) {
							continue;
						}
						
						TileEntity te = worldObj.getTileEntity(cursor);
						if (te == null || !(te instanceof HomeBlockTileEntity)) {
							continue;
						}
						
						HomeBlockTileEntity ent = (HomeBlockTileEntity) te;
						if (ent.canAccept(this) && isValidHome(cursor) && this.shouldJoin(cursor, state, ent)) {
							if (this.changeStatus(FairyGeneralStatus.IDLE)) {
								this.setHome(cursor);
							}
							break;
						}
					}
				}
			}
			
			if (this.getStatus() == FairyGeneralStatus.WANDERING) {
				// still wandering
				onWanderTick();
			}
			
			break;
		}
	}
	
	private boolean searchForJobs() {
		LogisticsNetwork network = this.getLogisticsNetwork();
		//long start = System.currentTimeMillis();
		if (network != null) {
			
			List<ILogisticsTask> list = network.getTaskRegistry().findTasks(network, this, (task) -> {
				World world = ILogisticsTask.GetSourceWorld(task);
				BlockPos pos = ILogisticsTask.GetSourcePosition(task);
				if (world.provider.getDimension() != this.dimension) {
					return false;
				}
				
				if (!canReach(pos, true)) {
					return false;
				}
				
				return true;
			});
			
			// Sort so nearest tasks are first in the list
			Collections.sort(list, (l, r) -> {
				
				if (l.equals(r)) {
					return 0;
				}
				
				BlockPos lPos = ILogisticsTask.GetSourcePosition(l);
				BlockPos rPos = ILogisticsTask.GetSourcePosition(r);
				BlockPos fairyPos = this.getPosition();
				double lDist = lPos.distanceSq(fairyPos);
				double rDist = rPos.distanceSq(fairyPos);
				return lDist < rDist ? -1 : lDist == rDist ? 0 : 1;
			});
			
			if (list != null && !list.isEmpty()) {
				// Could sort somehow.
				
				ILogisticsTask foundTask = this.currentTask; // may be null
				for (ILogisticsTask task : list) {
//					if (canPerformTask(task)
//							&& task.canAccept(this)
//							&& shouldPerformTask(task)
//							&& (foundTask == null || foundTask.canMerge(task))) {
//						network.getTaskRegistry().claimTask(task, this);
//						if (foundTask == null) {
//							foundTask = task;
//						} else {
//							// pair.task is another task we should merge into foundTask.
//							// Note we also claim the original task.
//							foundTask = foundTask.mergeIn(task);
//						}
//					}
					
					boolean success;
//					long time1 = 0;
//					long time2 = 0;
//					long time3 = 0;
//					long time4 = 0;
//					long startInner;

					//startInner = System.currentTimeMillis();
					success = (foundTask == null || foundTask.canMerge(task));
					//time4 += System.currentTimeMillis() - startInner;
					if (success) {
						//startInner = System.currentTimeMillis();
						success = canPerformTask(task);
						//time1 += System.currentTimeMillis() - startInner;
						if (success) {
							//startInner = System.currentTimeMillis();
							success = task.canAccept(this);
							//time2 += System.currentTimeMillis() - startInner;
							if (success) {
								//startInner = System.currentTimeMillis();
								success = shouldPerformTask(task);
								//time3 += System.currentTimeMillis() - startInner;
								if (success) {
									network.getTaskRegistry().claimTask(task, this);
									if (foundTask == null) {
										foundTask = task;
									} else {
										// pair.task is another task we should merge into foundTask.
										// Note we also claim the original task.
										foundTask = foundTask.mergeIn(task);
									}
								}
							}
						}
					}
					
					//System.out.println(String.format("\t[%d] [%d] [%d] [%d]", time1, time2, time3, time4));
				}
				
				if (foundTask != null) {
					forceSetTask(foundTask);
					return true;
				}
			}
		}
		//long end = System.currentTimeMillis();
		//System.out.println("Took " + (end - start) + "ms");
		
		return false;
	}
	
	private void verifyHome() {
		if (this.getHome() != null && !worldObj.isBlockLoaded(this.getHome())) {
			// Can't actually verify, so just pretend it's fine.
			return;
		}
		
		HomeBlockTileEntity ent = getHomeEnt();
		if (ent == null || !ent.isResident(this)) {
			if (ent != null) {
				ent.removeResident(this);
			}
			
			this.setHome(null);
			
			if (getStatus() != FairyGeneralStatus.WANDERING) {
				// Don't have a home, and we expected to. Homeless!
				this.changeStatus(FairyGeneralStatus.WANDERING);
			}
		}
	}
	
	protected abstract String getRandomName();
	
	@Override
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(HOME, Optional.absent());
		this.dataManager.register(NAME, getRandomName());
		this.dataManager.register(STATUS, FairyGeneralStatus.WANDERING);
		this.dataManager.register(ACTIVITY, "status.generic.working");
	}
	
	private static final String NBT_HOME = "home";
	private static final String NBT_REVOLTING = "revolt";
	private static final String NBT_NAME = "default_name";
	
	@Override
	public boolean writeToNBTOptional(NBTTagCompound compound) {
		return super.writeToNBTOptional(compound);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		BlockPos home = this.getHome();
		if (home != null) {
			compound.setLong(NBT_HOME, home.toLong());
		}
		if (this.getStatus() == FairyGeneralStatus.REVOLTING) {
			compound.setBoolean(NBT_REVOLTING, true);
		}
		compound.setString(NBT_NAME, dataManager.get(NAME));
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		boolean hasHome = false;
		if (compound.hasKey(NBT_HOME)) {
			hasHome = true;
			BlockPos home =  BlockPos.fromLong(compound.getLong(NBT_HOME));
			this.dataManager.set(HOME, Optional.fromNullable(home));
			this.setHomePosAndDistance(home, home == null ? -1 : (int) this.wanderDistanceSq);
			
			// Don't use the helper func since that also tries to make sure there's a TE and what not, and the TE may not
			// have loaded yet.
			//this.setHome(BlockPos.fromLong(compound.getLong(NBT_HOME)));
		}
		
		if (compound.getBoolean(NBT_REVOLTING)) {
			this.changeStatus(FairyGeneralStatus.REVOLTING);
		} else if (hasHome) {
			// Not wandering if we have a home. May have had a task before, but we'll
			// re-grab it later
			this.changeStatus(FairyGeneralStatus.IDLE);
		} else {
			this.changeStatus(FairyGeneralStatus.WANDERING);
		}
		
		if (compound.hasKey(NBT_NAME, NBT.TAG_STRING)) {
			dataManager.set(NAME, compound.getString(NBT_NAME));
		} // else default is a random one
	}
	
	@Override
	public void setDead() {
		forfitTask();
		HomeBlockTileEntity ent = getHomeEnt();
		if (ent != null) {
			ent.removeResident(this);
		}
		super.setDead();
	}
	
	@Override
	protected boolean canDespawn() {
		return false;
	}
	
	@Override
	public String getName() {
		if (this.hasCustomName()) {
			return this.getCustomNameTag();
		}
		
		return this.dataManager.get(NAME);
	}
	
	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		// Copied from EntityMob
		
		float f = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		int i = 0;

		if (entityIn instanceof EntityLivingBase) {
			f += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)entityIn).getCreatureAttribute());
			i += EnchantmentHelper.getKnockbackModifier(this);
		}

		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

		if (flag) {
			if (i > 0 && entityIn instanceof EntityLivingBase) {
				((EntityLivingBase)entityIn).knockBack(this, (float)i * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
				this.motionX *= 0.6D;
				this.motionZ *= 0.6D;
			}

			int j = EnchantmentHelper.getFireAspectModifier(this);

			if (j > 0) {
				entityIn.setFire(j * 4);
			}

			if (entityIn instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer)entityIn;
				ItemStack itemstack = this.getHeldItemMainhand();
				ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : null;

				if (itemstack != null && itemstack1 != null && itemstack.getItem() instanceof ItemAxe && itemstack1.getItem() == Items.SHIELD) {
					float f1 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

					if (this.rand.nextFloat() < f1) {
						entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
						this.worldObj.setEntityState(entityplayer, (byte)30);
					}
				}
			}

			this.applyEnchantments(this, entityIn);
		}

		return flag;
	}
	
	public abstract FeyHomeBlock.ResidentType getHomeType();
	
	@SideOnly(Side.CLIENT)
	public abstract String getSpecializationName();
	
	@SideOnly(Side.CLIENT)
	public abstract String getMoodSummary();
	
	public String getActivitySummary() {
		return dataManager.get(ACTIVITY);
	}
	
	protected void setActivitySummary(String unloc) {
		dataManager.set(ACTIVITY, unloc);
	}
	
	protected @Nullable BlockPos findEmptySpot(BlockPos targetPos, boolean allOrNothing) {
		if (!worldObj.isAirBlock(targetPos)) {
			do {
				if (worldObj.isAirBlock(targetPos.north())) {
					if (worldObj.isSideSolid(targetPos.north().down(), EnumFacing.UP)) {
						targetPos = targetPos.north();
						break;
					} else if (worldObj.isSideSolid(targetPos.north().down().down(), EnumFacing.UP)) {
						targetPos = targetPos.north().down();
						break;
					}
				}
				if (worldObj.isAirBlock(targetPos.south())) {
					if (worldObj.isSideSolid(targetPos.south().down(), EnumFacing.UP)) {
						targetPos = targetPos.south();
						break;
					} else if (worldObj.isSideSolid(targetPos.south().down().down(), EnumFacing.UP)) {
						targetPos = targetPos.south().down();
						break;
					}
				}
				if (worldObj.isAirBlock(targetPos.east())) {
					if (worldObj.isSideSolid(targetPos.east().down(), EnumFacing.UP)) {
						targetPos = targetPos.east();
						break;
					} else if (worldObj.isSideSolid(targetPos.east().down().down(), EnumFacing.UP)) {
						targetPos = targetPos.east().down();
						break;
					}
				}
				if (worldObj.isAirBlock(targetPos.west())) {
					if (worldObj.isSideSolid(targetPos.west().down(), EnumFacing.UP)) {
						targetPos = targetPos.west();
						break;
					} else if (worldObj.isSideSolid(targetPos.west().down().down(), EnumFacing.UP)) {
						targetPos = targetPos.west().down();
						break;
					}
				}
				if (worldObj.isAirBlock(targetPos.up()) && worldObj.isSideSolid(targetPos, EnumFacing.UP)) {
					targetPos = targetPos.up();
					break;
				}
				if (worldObj.isAirBlock(targetPos.down()) && worldObj.isSideSolid(targetPos.down().down(), EnumFacing.UP)) {
					targetPos = targetPos.down();
					break;
				}
			} while (false);
		}
		
		if (allOrNothing) {
			if (!worldObj.isAirBlock(targetPos)) {
				targetPos = null;
			}
		}
		
		return targetPos;
	}
	
	protected static boolean FeyWander(EntityFeyBase fey, BlockPos center, double minDist, double maxDist) {
		BlockPos targ = null;
		int attempts = 20;
		final Random rand = fey.rand;
		do {
			double dist = minDist + (rand.nextDouble() * (maxDist - minDist));
			float angle = (float) (rand.nextDouble() * (2 * Math.PI));
			float tilt = (float) (rand.nextDouble() * (2 * Math.PI)) * .5f;
			
			targ = new BlockPos(new Vec3d(
					center.getX() + (Math.cos(angle) * dist),
					center.getY() + (Math.cos(tilt) * dist),
					center.getZ() + (Math.sin(angle) * dist)));
			while (targ.getY() > 0 && fey.worldObj.isAirBlock(targ)) {
				targ = targ.down();
			}
			if (targ.getY() < 256) {
				targ = targ.up();
			}
			
			// We've hit a non-air block. Make sure there's space above it
			BlockPos airBlock = null;
			for (int i = 0; i < Math.ceil(fey.height); i++) {
				if (airBlock == null) {
					airBlock = targ.up();
				} else {
					airBlock = airBlock.up();
				}
				
				if (!fey.worldObj.isAirBlock(airBlock)) {
					targ = null;
					break;
				}
			}
		} while (targ == null && attempts > 0);
		
		if (targ == null) {
			targ = center.up();
		}
		if (!fey.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
			fey.moveHelper.setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
		}
		
		return true;
	}
	
	protected static boolean FeyWander(EntityFeyBase fey, BlockPos center, double maxDist) {
		return FeyWander(fey, center, maxDist * .4, maxDist);
	}
	
	protected static boolean FeyFollow(EntityFeyBase fey, EntityLivingBase target, double minDist, double maxDist) {
		return FeyWander(fey, target.getPosition(), minDist, maxDist);
	}
	
	protected static boolean FeyFollow(EntityFeyBase fey, EntityLivingBase target, double maxDist) {
		return FeyFollow(fey, target, maxDist * .4, maxDist);
	}
	
	protected static boolean FeyFollowNearby(EntityFeyBase fey, Predicate<? super Entity> filter, boolean lazy, double maxSightDist, double minFollowDist, double maxFollowDist) {
		List<Entity> ents = fey.worldObj.getEntitiesInAABBexcluding(fey,
				new AxisAlignedBB(fey.posX - maxSightDist, fey.posY - maxSightDist, fey.posZ - maxSightDist, fey.posX + maxSightDist, fey.posY + maxSightDist, fey.posZ + maxSightDist),
				filter);
		
		EntityLivingBase target = null;
		double minDist = 0;
		if (ents != null && !ents.isEmpty()) {
			// pick the closest
			for (Entity ent : ents) {
				if (!(ent instanceof EntityLivingBase)) {
					continue;
				}
				
				double dist = fey.getDistanceSqToEntity(ent);
				if (target == null || dist < minDist) {
					target = (EntityLivingBase) ent;
					minDist = dist;
				}
			}
		}
		
		if (target != null && (lazy || minDist > minFollowDist)) {
			return FeyFollow(fey, target, minFollowDist, maxFollowDist);
		}
		
		return false;
	}
	
	protected static boolean FeyLazyFollowNearby(EntityFeyBase fey, Predicate<? super Entity> filter, double maxSightDist, double minFollowDist, double maxFollowDist) {
		return FeyFollowNearby(fey, filter, true, maxSightDist, minFollowDist, maxFollowDist);
	}
	
	protected static boolean FeyActiveFollowNearby(EntityFeyBase fey, Predicate<? super Entity> filter, double maxSightDist, double minFollowDist, double maxFollowDist) {
		return FeyFollowNearby(fey, filter, false, maxSightDist, minFollowDist, maxFollowDist);
	}
	
	protected static final Predicate<? super Entity> DOMESTIC_FEY_FILTER = new Predicate<Entity>() {

		@Override
		public boolean apply(Entity input) {
			if (input == null || !(input instanceof EntityFeyBase)) {
				return false;
			}
			
			EntityFeyBase fey = (EntityFeyBase) input;
			return fey.getStatus() == FairyGeneralStatus.IDLE || fey.getStatus() == FairyGeneralStatus.WORKING;
		}
		
	};
	
	protected static final Predicate<? super Entity> DOMESTIC_FEY_AND_PLAYER_FILTER = new Predicate<Entity>() {

		@Override
		public boolean apply(Entity input) {
			if (input == null) {
				return false;
			}
			
			if (input instanceof EntityFeyBase) {
				EntityFeyBase fey = (EntityFeyBase) input;
				return fey.getStatus() == FairyGeneralStatus.IDLE || fey.getStatus() == FairyGeneralStatus.WORKING;
			}
			
			if (input instanceof EntityPlayer) {
				return !((EntityPlayer) input).isSpectator();
			}
			
			return false;
		}
		
	};
}
