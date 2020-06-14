package com.smanzana.nostrumfairies.entity.fairy;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrummagica.loretag.ILoreTagged;

import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class EntityFairyBase extends EntityGolem implements ILoreTagged {

	public static enum FairyGeneralStatus {
		WANDERING, // Not attached to a home, and therefore incapable of working
		IDLE, // Able to work, but with no work to do
		WORKING, // Working
		REVOLTING, // Refusing to work
	}
	
	protected static final double MAX_FAIRY_DISTANCE_SQ = 144;
	protected static final DataParameter<Optional<BlockPos>> HOME  = EntityDataManager.<Optional<BlockPos>>createKey(EntityFairyBase.class, DataSerializers.OPTIONAL_BLOCK_POS);
	
	/**
	 * Current general status of the worker.
	 */
	private FairyGeneralStatus generalStatus;
	
	/**
	 * Maximum amount of distance (squared) a fairy will freely wander away from its home.
	 */
	protected double wanderDistanceSq;
	
	/**
	 * Maximum amount of distance (squared) a fairy can be away from home while performing a job.
	 */
	protected double workDistanceSq;
	
	private ILogisticsTask currentTask;
	
	public EntityFairyBase(World world) {
		this(world, MAX_FAIRY_DISTANCE_SQ, MAX_FAIRY_DISTANCE_SQ);
	}
	
	public EntityFairyBase(World world, double wanderDistanceSq, double workDistanceSq) {
		super(world);
		generalStatus = FairyGeneralStatus.WANDERING;
		this.wanderDistanceSq = wanderDistanceSq;
		this.workDistanceSq = workDistanceSq;
	}
	
	/**
	 * Get current fairy worker status
	 * @return
	 */
	public FairyGeneralStatus getStatus() {
		return this.generalStatus;
	}
	
	/**
	 * Attempt to transition to the new status type.
	 * Fairy implementations have the option of refusing a state change. This is merely a request.
	 * @param status
	 * @return true when the status change went thr ough
	 */
	protected boolean changeStatus(FairyGeneralStatus status) {
		if (onStatusChange(getStatus(), status)) {
			this.generalStatus = status;
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
	
	/**
	 * Get the fairy's home block.
	 * @return
	 */
	@Nullable
	public BlockPos getHome() {
		return this.dataManager.get(HOME).orNull();
	}
	
	/**
	 * Set the block the fairy will consider its home, and try to stay close to.
	 * @return
	 */
	public void setHome(@Nullable BlockPos home) {
		this.dataManager.set(HOME, Optional.of(home));
		this.setHomePosAndDistance(home, (int) this.wanderDistanceSq);
	}
	
	/**
	 * Called to check that the provided home position is still a valid home position
	 * @param homePos
	 * @return
	 */
	protected abstract boolean isValidHome(BlockPos homePos);
	
	/**
	 * Return the current task a fairy is working on.
	 * It's expected that a status of 'WORKING' means this will return something,
	 * while calling while IDLE would not.
	 * @return The current task if there is one, or null.
	 */
	public @Nullable ILogisticsTask getCurrentTask() {
		return currentTask;
	}
	
	protected void forceSetTask(@Nullable ILogisticsTask task) {
		ILogisticsTask oldtask = this.getCurrentTask();
		this.currentTask = task;
		this.onTaskChange(oldtask, task);
		
	}
	
	public void cancelTask() {
		forceSetTask(null);
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
	
	protected void finishTask() {
		// This _could_ immediately look for a new task.
		// For now, I think 1 frame delay between having a new job is okay.
		// That'll also mean that code that calls this and then looks at the task after will get NULL instead
		// of possibly the same task or possibly a new one, which might make debugging easier.
		forceSetTask(null);
	}
	
	/**
	 * Called when the current task changes. This includes when the task is reset and there is no new task.
	 * @param oldTask The task that was just given up. 
	 * @param newTask The new task.
	 */
	protected abstract void onTaskChange(@Nullable ILogisticsTask oldTask, @Nullable ILogisticsTask newTask);
	
	/**
	 * Called every tick that a task is active.
	 * @param task
	 */
	protected abstract void onTaskTick(@Nullable ILogisticsTask task);
	
	@Override
	protected abstract void initEntityAI();
	
	@Override
	protected abstract void applyEntityAttributes();
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		// If we're supposed to have a home, verify it's still there and fix state.
		verifyHome();
		
		switch (generalStatus) {
		case IDLE:
			if (!searchForJobs()) {
				break;
			}
			// else fall through to start working as soon as task is picked up
		case WORKING:
			if (currentTask != null) {
				this.onTaskTick(currentTask);
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
		case WANDERING:
			// Implementation should say what these do
			break;
		}
	}
	
	private boolean searchForJobs() {
		
		// TODO
		return false;
	}
	
	private void verifyHome() {
		if (generalStatus != FairyGeneralStatus.WANDERING) {
			BlockPos home = this.getHome();
			if (home != null) {
				if (!this.isValidHome(home)) {
					this.setHome(null);
					home = null; // Home is no longer valid
				}
			}
			
			if (home == null) {
				// Don't have a home, and we expected to. Homeless!
				this.changeStatus(FairyGeneralStatus.WANDERING);
			}
		}
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(HOME, Optional.absent());
	}
	
	private static final String NBT_HOME = "home";
	private static final String NBT_REVOLTING = "revolt";
	
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
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		boolean hasHome = false;
		if (compound.hasKey(NBT_HOME)) {
			hasHome = true;
			this.setHome(BlockPos.fromLong(compound.getLong(NBT_HOME)));
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
	}
	
	
}
