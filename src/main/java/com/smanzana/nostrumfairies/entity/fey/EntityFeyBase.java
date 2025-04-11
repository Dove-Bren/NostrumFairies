package com.smanzana.nostrumfairies.entity.fey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FeyHomeBlock;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.entity.navigation.PathNavigatorLogistics;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class EntityFeyBase extends AbstractGolem implements IFeyWorker, ILoreTagged {

	protected static final EntityDataAccessor<Optional<BlockPos>> HOME  = SynchedEntityData.<Optional<BlockPos>>defineId(EntityFeyBase.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
	protected static final EntityDataAccessor<String> NAME = SynchedEntityData.<String>defineId(EntityFeyBase.class, EntityDataSerializers.STRING);
	protected static final EntityDataAccessor<FairyGeneralStatus> STATUS  = SynchedEntityData.<FairyGeneralStatus>defineId(EntityFeyBase.class, FairyGeneralStatus.instance());
	protected static final EntityDataAccessor<String> ACTIVITY = SynchedEntityData.<String>defineId(EntityFeyBase.class, EntityDataSerializers.STRING);
	protected static final EntityDataAccessor<Float> HAPPINESS = SynchedEntityData.<Float>defineId(EntityFeyBase.class, EntityDataSerializers.FLOAT);
	protected static final EntityDataAccessor<Boolean> CURSED = SynchedEntityData.<Boolean>defineId(EntityFeyBase.class, EntityDataSerializers.BOOLEAN);
	
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
	protected int taskTickCount;
	
	/**
	 * Total time we've been idle for
	 */
	protected int idleTicks;
	
	private int idleChatTicks;
	
	/**
	 * Slowly updates pos used to figure out if we get stuck while on the job!
	 */
	private BlockPos lastTaskPos;
	private ILogisticsTask lastTask;
	private LogisticsSubTask lastSubTask;
	
	/**
	 * Tracks task we can't start and when we should try them again
	 */
	private Map<ILogisticsTask, Integer> taskRetryMap;
	
	
	public EntityFeyBase(EntityType<? extends EntityFeyBase> type, Level world) {
		this(type, world, 100, MAX_FAIRY_DISTANCE_SQ);
	}
	
	public EntityFeyBase(EntityType<? extends EntityFeyBase> type, Level world, double wanderDistanceSq, double workDistanceSq) {
		super(type, world);
		this.wanderDistanceSq = wanderDistanceSq;
		this.workDistanceSq = workDistanceSq;
		
		this.navigation = new PathNavigatorLogistics(this, world);
		
		idleChatTicks = -1;
		taskRetryMap = new HashMap<>();
	}
	
	@Override
	public FairyGeneralStatus getStatus() {
		return entityData.get(STATUS);
	}
	
	/**
	 * Attempt to transition to the new status type.
	 * Fairy implementations have the option of refusing a state change. This is merely a request.
	 * @param status
	 * @return true when the status change went thr ough
	 */
	protected boolean changeStatus(FairyGeneralStatus status) {
		if (level.isClientSide) {
			return true;
		}
		
		if (statusGuard) {
			return true;
		}

		boolean ret = false;
		statusGuard = true;
		
		if (onStatusChange(getStatus(), status)) {
			entityData.set(STATUS, status);
			ret = true;
		}
		
		statusGuard = false;
		return ret;
	}
	
	private boolean statusGuard = false;
	
	/**
	 * Called whenever the fairy's state is attempting to change.
	 * @param from Current state
	 * @param to State trying to transition to
	 * @return true to allow the change, or false to reject it
	 */
	protected abstract boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to);
	
	public float getHappiness() {
		return entityData.get(HAPPINESS);
	}
	
	protected void setHappiness(float happiness) {
		entityData.set(HAPPINESS, Math.max(0, Math.min(100f, happiness)));
	}
	
	protected void addHappiness(float diff) {
		float now = getHappiness();
		setHappiness(now + diff);
	}
	
	public boolean isCursed() {
		return entityData.get(CURSED);
	}
	
	public void setCursed(boolean cursed) {
		entityData.set(CURSED, cursed);
		if (cursed) {
			this.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 500, 1));
		}
	}
	
	@Override
	@Nullable
	public BlockPos getHome() {
		return this.entityData.get(HOME).orElse(null);
	}
	
	protected @Nullable HomeBlockTileEntity getHomeEnt(BlockPos pos) {
		if (!NostrumMagica.isBlockLoaded(level, pos)) {
			return null;
		}
		
		BlockState state = level.getBlockState(pos);
		if (state == null || !(state.getBlock() instanceof FeyHomeBlock)) {
			return null;
		}
		
		BlockPos center = ((FeyHomeBlock) state.getBlock()).getMasterPos(level, pos, state);
		BlockEntity te = level.getBlockEntity(center);
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
			home = home.immutable();
		}
		this.entityData.set(HOME, Optional.ofNullable(home));
		this.restrictTo(home, home == null ? -1 : (int) this.wanderDistanceSq);
		
		if (home != null) {
			ent = getHomeEnt();
			if (ent != null) {
				ent.addResident(this);
				if (this.getStatus() == FairyGeneralStatus.WANDERING) {
					this.changeStatus(FairyGeneralStatus.IDLE);
				}
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
		
		return home.distSqr(pos) < (work ? workDistanceSq : wanderDistanceSq);
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
	
	@Override
	public String getLogisticsID() {
		return "Fey-" + this.getType().getDescription().getString() + "-" + this.getId();
	}
	
	protected void forceSetTask(@Nullable ILogisticsTask task) {
		ILogisticsTask oldtask = this.getCurrentTask();
		this.currentTask = task;
		
		if (task != oldtask) {
			this.taskTickCount = 0;
		}
		this.onTaskChange(oldtask, task);
	}
	
	@Override
	public void dropTask(ILogisticsTask droppedTask) {
		if (currentTask == null) {
			NostrumFairies.logger.warn("Fey was asked to drop task that they didn't have anymore");
			return;
		}
		
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
	
	protected boolean canWorkTimeCheck() {
		return level.isDay();
	}
	
	/**
	 * Check whether this entity's needs (and home) are in line and work tasks can be picked up.
	 * @return
	 */
	protected boolean canWork() {
		this.verifyHome();
		if (this.getHappiness() < 10f) {
			return false; // TODO make this cause a revolt, not just no work
		}
		return (canWorkTimeCheck() && getHomeEnt() != null && getHomeEnt().canWork());
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
			this.addHappiness(random.nextFloat() * .1f); // Regain some happiness for finishing tasks
		}
	}
	
	protected void forfitTask() {
		if (this.currentTask != null) {
			if (this.getLogisticsNetwork() != null) {
				List<ILogisticsTask> tasks = new ArrayList<>(currentTask.unmerge());
				for (ILogisticsTask task : tasks) {
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
	protected abstract boolean shouldJoin(BlockPos pos, BlockState state, HomeBlockTileEntity te);
	
	/**
	 * Called every tick that a task is active.
	 * @param task
	 */
	protected abstract void onTaskTick(@Nullable ILogisticsTask task);
	
	protected abstract void onIdleTick();
	
	protected abstract void onCombatTick();
	
	protected abstract void onWanderTick();
	
	protected abstract void onRevoltTick();
	
	@OnlyIn(Dist.CLIENT)
	protected abstract void onCientTick();
	
	/**
	 * Called periodically while doing task ticks to see if we should see if any other tasks are out there that
	 * we could also pick up. Fairies should return false if they're far enough in their task that picking up more
	 * doesn't make sense. For example, if you ALREADY picked up an item in an item withdraw task.
	 * @return
	 */
	protected abstract boolean canMergeMoreJobs();
	
	@Override
	protected abstract void registerGoals();
	
	protected void teleportFromStuck() {
		this.teleportHome();
	}
	
	protected int getDefaultSwingAnimationDuration() {
		return 6;
	}
	
	/**
	 * Returns an integer indicating the end point of the swing animation, used by {@link #swingProgress} to provide a
	 * progress indicator. Takes dig speed enchantments into account.
	 * Note: Copied from vanilla where you can't override it :(
	 */
	protected int getArmSwingAnimationEnd() {
		return this.hasEffect(MobEffects.DIG_SPEED)
				? getDefaultSwingAnimationDuration() - (1 + this.getEffect(MobEffects.DIG_SPEED).getAmplifier())
				: (this.hasEffect(MobEffects.DIG_SLOWDOWN)
						? getDefaultSwingAnimationDuration() + (1 + this.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2
						: getDefaultSwingAnimationDuration());
	}

	@Override
	public void swing(InteractionHand hand) {
		//int unused; //Why not just call super?
		ItemStack stack = this.getItemInHand(hand);
		if (!stack.isEmpty()) {
			if (stack.getItem().onEntitySwing(stack, this)) {
				return;
			}
		}
		
		if (!this.swinging || this.swingTime >= this.getArmSwingAnimationEnd() / 2 || this.swingTime < 0) {
			this.swingTime = -1;
			this.swinging = true;
			this.swingingArm = hand;

			if (this.level instanceof ServerLevel) {
				((ServerLevel)this.level).getChunkSource().broadcast(this, new ClientboundAnimatePacket(this, hand == InteractionHand.MAIN_HAND ? 0 : 3));
			}
		}
	}
	
	protected void teleportHome() {
		BlockPos target = findEmptySpot(this.getHome(), false);
		if (this.randomTeleport(target.getX() + .5, target.getY() + .05, target.getZ() + .5, false)) {
			this.navigation.stop();
		}
	}
	
	@Override
	protected void updateSwingTime() {
		int i = this.getArmSwingAnimationEnd();

		if (this.swinging) {
			++this.swingTime;

			if (this.swingTime >= i) {
				this.swingTime = 0;
				this.swinging = false;
			}
		}else {
			this.swingTime = 0;
		}

		this.attackAnim = (float)this.swingTime / (float)i;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		this.updateSwingTime();
		
		if (level.isClientSide) {
			// Do some client-side tracking
			if (this.getStatus() == FairyGeneralStatus.IDLE) {
				idleTicks++;
				if (this.getIdleSound() != null) {
					if (level.isClientSide) {
						if (idleChatTicks == 0) {
							getIdleSound().play(NostrumFairies.proxy.getPlayer(), level, getX(), getY(), getZ());
							idleChatTicks = -1;
						}
						
						if (idleChatTicks == -1) {
							idleChatTicks = (random.nextInt(15) + 8) * 20; 
						}
						
						idleChatTicks--;
					}
				}
			} else {
				idleTicks = 0;
				idleChatTicks = -1;
			}
			onCientTick();
		}
		
		if (level.isClientSide || !this.isAlive()) {
			return;
		}
		
		if (this.isCursed()) {
			if (this.getEffect(MobEffects.WITHER) == null) {
				this.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 500, 1));
			}
		}
		
		// If we're in combat, ignore all the rest
		if (this.getTarget() != null) {
			if (!this.getTarget().isAlive()) {
				this.setTarget(null);
			} else {
				this.onCombatTick();
				return;
			}
		}
		
		// If we're supposed to have a home, verify it's still there and fix state.
		verifyHome();
		
		switch (getStatus()) {
		case IDLE:
			idleTicks++;
			adjustHappiness();
			if (this.tickCount % 60 == 0 && this.getHappiness() > 80f) {
				this.heal(1f);
			}
			onIdleTick();
			
			// Note: idle may decide to do task stuff on its own. We'll respect that.
			if (currentTask == null && canWork() && random.nextFloat() < .1f) {
				if (!searchForJobs()) {
					break;
				}
			}
			
			idleTicks = 0;
			changeStatus(FairyGeneralStatus.WORKING);
			// else fall through to start working as soon as task is picked up
			// TODO maybe both should search for tasks if the task is dropable?
		case WORKING:
			// Clean up rejected list
			if (tickCount % 5 == 0) {
				Iterator<Entry<ILogisticsTask, Integer>> it = taskRetryMap.entrySet().iterator();
				while (it.hasNext()) {
					Entry<ILogisticsTask, Integer> row = it.next();
					if (row.getValue() <= tickCount) {
						it.remove();
					}
				}
			}
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
					if (taskTickCount > 0 && taskTickCount % (20 * 30) == 0) {
						
						// Stuck check!
						// Same task?
						boolean reset = false;
						BlockPos pos = this.blockPosition();
						if (currentTask == lastTask && currentTask.getActiveSubtask() == lastSubTask) {
							pos = (lastTaskPos == null ? BlockPos.ZERO : pos.subtract(lastTaskPos));
							if (pos.getX() + pos.getY() + pos.getZ() < 3) {
								// Stuck! Teleport!
								teleportFromStuck();
								lastTaskPos = null;
								lastTask = null;
								lastSubTask = null;
								reset = true;
							}
						}
						
						if (!reset) {
							lastTaskPos = this.blockPosition();
							lastTask = currentTask;
							lastSubTask = currentTask.getActiveSubtask();
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
			if (this.tickCount % 100 == 0) {
				int radius = 3;
				BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
				for (int x = -radius; x <= radius; x++)
				for (int z = -radius; z <= radius; z++)
				for (int y = -radius; y <= radius; y++) {
					cursor.set(getX() + x, getY() + y, getZ() + z);
					if (!NostrumMagica.isBlockLoaded(level, cursor)) {
						continue;
					}
					
					BlockState state = level.getBlockState(cursor);
					if (state.getBlock() instanceof FeyHomeBlock) {
						FeyHomeBlock block = (FeyHomeBlock) state.getBlock();
						if (!block.isCenter(state)) {
							continue;
						}
						
						BlockEntity te = level.getBlockEntity(cursor);
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
	
	protected List<ILogisticsTask> getTaskList() {
		LogisticsNetwork network = this.getLogisticsNetwork();
		if (network != null) {
			return network.getTaskRegistry().findTasks(network, this, (task) -> {
				if (taskIsRejected(task)) {
					return false;
				}
				
				Level world = ILogisticsTask.GetSourceWorld(task);
				BlockPos pos = ILogisticsTask.GetSourcePosition(task);
				if (!DimensionUtils.SameDimension(world, this.level)) {
					return false;
				}
				
				if (!canReach(pos, true)) {
					return false;
				}
				
				return true;
			});
		}
		
		return null;
	}
	
	protected void claimTask(ILogisticsTask task) {
		LogisticsNetwork network = this.getLogisticsNetwork();
		network.getTaskRegistry().claimTask(task, this);
	}
	
	protected void rejectTask(ILogisticsTask task, int bonusPenalty) {
		final int penalty = bonusPenalty + (20 * 5) + random.nextInt(60);
		this.taskRetryMap.put(task, tickCount + penalty);
	}
	
	protected boolean taskIsRejected(ILogisticsTask task) {
		Integer time = taskRetryMap.get(task);
		if (time == null || time <= tickCount) {
			return false;
		}
		
		return true;
	}
	
	private boolean searchForJobs() {
		List<ILogisticsTask> list = getTaskList();
		if (list != null) {
			
			// Sort so nearest tasks are first in the list, with y weighing more than xz
			Collections.sort(list, (l, r) -> {
				
				if (l.equals(r)) {
					return 0;
				}
				
				BlockPos lPos = ILogisticsTask.GetStartPosition(l);
				BlockPos rPos = ILogisticsTask.GetStartPosition(r);
				BlockPos fairyPos = this.blockPosition();
				double lDist = Math.abs(lPos.getX() - fairyPos.getX()) + Math.abs(lPos.getZ() - fairyPos.getZ()) + 2 * Math.abs(lPos.getY() - fairyPos.getY());
				double rDist = Math.abs(rPos.getX() - fairyPos.getX()) + Math.abs(rPos.getZ() - fairyPos.getZ()) + 2 * Math.abs(rPos.getY() - fairyPos.getY());
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
									this.claimTask(task);
									if (foundTask == null) {
										foundTask = task;
									} else {
										// pair.task is another task we should merge into foundTask.
										// Note we also claim the original task.
										foundTask = foundTask.mergeIn(task);
									}
								}
							}
						} else {
							rejectTask(task, 0); // Remember that we rejected it and put it in cooldown
						}
					}
					
					//System.out.println(String.format("\t[%d] [%d] [%d] [%d]", time1, time2, time3, time4));
				}
				
				if (foundTask != null) {
					if (this.currentTask == null) {
						this.addHappiness(-random.nextFloat() * 1f);
					}
					forceSetTask(foundTask);
					return true;
				}
			}
		}
		//long end = System.currentTimeMillis();
		//System.out.println("Took " + (end - start) + "ms");
		
		return false;
	}
	
	protected void verifyHome() {
		if (this.getHome() != null && !NostrumMagica.isBlockLoaded(level, this.getHome())) {
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
	
	protected void adjustHappiness() {
		// If idling, adjust happiness.
		// TODO base chance of regaining happiness on some feature of homes
		if (tickCount % 20 == 0) {
			this.verifyHome();
			if (this.getHome() != null && random.nextFloat() < .5f) {
				this.addHappiness(random.nextFloat() * 1f);
			}
		}
	}
	
	protected abstract String getRandomName();
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		
		this.entityData.define(HOME, Optional.empty());
		this.entityData.define(NAME, getRandomName());
		this.entityData.define(STATUS, FairyGeneralStatus.WANDERING);
		this.entityData.define(ACTIVITY, "status.generic.working");
		this.entityData.define(HAPPINESS, 100f);
		this.entityData.define(CURSED, false);
	}
	
	private static final String NBT_HOME = "home";
	private static final String NBT_REVOLTING = "revolt";
	private static final String NBT_NAME = "default_name";
	private static final String NBT_HAPPINESS = "happiness";
	private static final String NBT_CURSED = "cursed";
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		BlockPos home = this.getHome();
		if (home != null) {
			compound.put(NBT_HOME, NbtUtils.writeBlockPos(home));
		}
		if (this.getStatus() == FairyGeneralStatus.REVOLTING) {
			compound.putBoolean(NBT_REVOLTING, true);
		}
		compound.putString(NBT_NAME, entityData.get(NAME));
		compound.putFloat(NBT_HAPPINESS, entityData.get(HAPPINESS));
		compound.putBoolean(NBT_CURSED, entityData.get(CURSED));
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		boolean hasHome = false;
		if (compound.contains(NBT_HOME)) {
			hasHome = true;
			BlockPos home = NbtUtils.readBlockPos(compound.getCompound(NBT_HOME));
			this.entityData.set(HOME, Optional.ofNullable(home));
			this.restrictTo(home, home == null ? -1 : (int) this.wanderDistanceSq);
			
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
		
		if (compound.contains(NBT_NAME, Tag.TAG_STRING)) {
			entityData.set(NAME, compound.getString(NBT_NAME));
		} // else default is a random one
		
		entityData.set(CURSED, compound.getBoolean(NBT_CURSED));
		if (compound.contains(NBT_HAPPINESS)) {
			entityData.set(HAPPINESS, compound.getFloat(NBT_HAPPINESS));
		}
	}
	
	@Override
	public void remove(Entity.RemovalReason reason) {
		forfitTask();
		HomeBlockTileEntity ent = getHomeEnt();
		if (ent != null) {
			ent.removeResident(this);
		}
		super.remove(reason);
	}
	
	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}
	
	@Override
	public Component getName() {
		if (this.hasCustomName()) {
			return this.getCustomName();
		}
		
		return new TextComponent(this.entityData.get(NAME));
	}
	
	@Override
	public boolean doHurtTarget(Entity entityIn) {
		// Copied from MonsterEntity
		
		float f = (float)this.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
		int i = 0;

		if (entityIn instanceof LivingEntity) {
			f += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity)entityIn).getMobType());
			i += EnchantmentHelper.getKnockbackBonus(this);
		}

		boolean flag = entityIn.hurt(DamageSource.mobAttack(this), f);

		if (flag) {
			if (i > 0 && entityIn instanceof LivingEntity) {
				((LivingEntity)entityIn).knockback((float)i * 0.5F, (double)Mth.sin(this.getYRot() * 0.017453292F), (double)(-Mth.cos(this.getYRot() * 0.017453292F)));
				this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1, 0.6));
			}

			int j = EnchantmentHelper.getFireAspect(this);

			if (j > 0) {
				entityIn.setSecondsOnFire(j * 4);
			}

			if (entityIn instanceof Player) {
				Player entityplayer = (Player)entityIn;
				ItemStack itemstack = this.getMainHandItem();
				ItemStack itemstack1 = entityplayer.isUsingItem() ? entityplayer.getUseItem() : ItemStack.EMPTY;

				if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem() instanceof AxeItem && itemstack1.getItem() == Items.SHIELD) {
					float f1 = 0.25F + (float)EnchantmentHelper.getBlockEfficiency(this) * 0.05F;

					if (this.random.nextFloat() < f1) {
						entityplayer.getCooldowns().addCooldown(Items.SHIELD, 100);
						this.level.broadcastEntityEvent(entityplayer, (byte)30);
					}
				}
			}

			this.doEnchantDamageEffects(this, entityIn);
		}

		return flag;
	}
	
	protected abstract String getUnlocPrefix();
	
	public abstract ResidentType getHomeType();
	
	@OnlyIn(Dist.CLIENT)
	public abstract String getSpecializationName();
	
	@OnlyIn(Dist.CLIENT)
	protected String getMoodPrefix() {
		return "fey"; // could be getUnlocPrefix
	}
	
	@OnlyIn(Dist.CLIENT)
	public  String getMoodSummary() {
		final float happiness = this.getHappiness();
		final String prefix = this.getMoodPrefix();
		final String unloc;
		if (happiness > 90f) {
			unloc = "info.mood." + prefix + ".great";
		} else if (happiness > 70f) {
			unloc = "info.mood." + prefix + ".good";
		} else if (happiness > 50f) {
			unloc = "info.mood." + prefix + ".average";
		} else if (happiness > 20f) {
			unloc = "info.mood." + prefix + ".bad";
		} else {
			unloc = "info.mood." + prefix + ".awful";
		}
		
		return unloc;
	}
	
	public String getActivitySummary() {
		return entityData.get(ACTIVITY);
	}
	
	protected void setActivitySummary(String unloc) {
		entityData.set(ACTIVITY, unloc);
	}
	
	protected final boolean isSolid(Level world, BlockPos pos, Direction direction) {
		return Block.canSupportCenter(world, pos, direction);
	}
	
	protected @Nullable BlockPos findEmptySpot(BlockPos targetPos, boolean allOrNothing) {
		if (!level.isEmptyBlock(targetPos)) {
			do {
				if (level.isEmptyBlock(targetPos.north())) {
					final boolean belowIsAir = level.isEmptyBlock(targetPos.north().below());
					if (!belowIsAir && Block.canSupportCenter(level, targetPos.north().below(), Direction.UP)) {
						targetPos = targetPos.north();
						break;
					} else if (belowIsAir && isSolid(level, targetPos.north().below().below(), Direction.UP)) {
						targetPos = targetPos.north().below();
						break;
					}
				}
				if (level.isEmptyBlock(targetPos.south())) {
					final boolean belowIsAir = level.isEmptyBlock(targetPos.south().below());
					if (!belowIsAir && isSolid(level, targetPos.south().below(), Direction.UP)) {
						targetPos = targetPos.south();
						break;
					} else if (belowIsAir && isSolid(level, targetPos.south().below().below(), Direction.UP)) {
						targetPos = targetPos.south().below();
						break;
					}
				}
				if (level.isEmptyBlock(targetPos.east())) {
					final boolean belowIsAir = level.isEmptyBlock(targetPos.east().below());
					if (!belowIsAir && isSolid(level, targetPos.east().below(), Direction.UP)) {
						targetPos = targetPos.east();
						break;
					} else if (belowIsAir && isSolid(level, targetPos.east().below().below(), Direction.UP)) {
						targetPos = targetPos.east().below();
						break;
					}
				}
				if (level.isEmptyBlock(targetPos.west())) {
					final boolean belowIsAir = level.isEmptyBlock(targetPos.west().below());
					if (!belowIsAir && isSolid(level, targetPos.west().below(), Direction.UP)) {
						targetPos = targetPos.west();
						break;
					} else if (belowIsAir && isSolid(level, targetPos.west().below().below(), Direction.UP)) {
						targetPos = targetPos.west().below();
						break;
					}
				}
				if (level.isEmptyBlock(targetPos.above()) && isSolid(level, targetPos, Direction.UP)) {
					targetPos = targetPos.above();
					break;
				}
				if (level.isEmptyBlock(targetPos.below()) && isSolid(level, targetPos.below().below(), Direction.UP)) {
					targetPos = targetPos.below();
					break;
				}
			} while (false);
		}
		
		if (allOrNothing) {
			if (!level.isEmptyBlock(targetPos)) {
				targetPos = null;
			}
		}
		
		return targetPos;
	}
	
	@Override
	protected void dropCustomDeathLoot(DamageSource source, int lootingModifier, boolean wasRecentlyHit) {
		if (isCursed() && !level.isClientSide) {
			this.spawnAtLocation(FeyResource.create(FeyResourceType.TEARS, 1 + lootingModifier), 0);
			
			if (wasRecentlyHit && random.nextInt(5) < (1 + lootingModifier)) {
				this.spawnAtLocation(FeyResource.create(FeyResourceType.ESSENCE, random.nextInt(2) + 1), 0);
			}
		}
	}
	
	public static boolean canUseSpecialization(ResidentType type, FeyStoneMaterial material) {
		switch (type) {
		case DWARF:
			return material == FeyStoneMaterial.GARNET || material == FeyStoneMaterial.EMERALD;
		case ELF:
			return material == FeyStoneMaterial.GARNET || material == FeyStoneMaterial.AQUAMARINE;
		case FAIRY:
			return false;
		case GNOME:
			return material == FeyStoneMaterial.EMERALD || material == FeyStoneMaterial.GARNET;
		}
		
		return false;
	}
	
	public abstract EntityFeyBase switchToSpecialization(FeyStoneMaterial material);
	
	public abstract FeyStoneMaterial getCurrentSpecialization();
	
	protected void copyFrom(EntityFeyBase other) {
		this.setUUID(other.getUUID());
		this.absMoveTo(other.getX(), other.getY(), other.getZ(), other.getYRot(), other.getXRot());
		this.setHappiness(other.getHappiness());
		this.entityData.set(NAME, other.getName().getString());
		HomeBlockTileEntity ent = other.getHomeEnt();
		if (ent != null) {
			BlockPos pos = other.getHome();
			ent.replaceResident(other, this);
			other.entityData.set(HOME, Optional.empty());
			this.entityData.set(HOME, Optional.of(pos));
			this.changeStatus(FairyGeneralStatus.IDLE);
		}
	}
	
	protected abstract @Nullable NostrumFairiesSounds getIdleSound();
	
	public final double getDistanceSq(BlockPos pos) {
		return this.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public final double getDistanceSqToCenter(BlockPos pos) {
		return this.distanceToSqr(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
	}
	
	protected boolean isAtMoveTarget(BlockPos target) {
		return this.getDistanceSq(target) <= 3;
	}
	
	protected boolean isAtMoveTarget(Entity ent) {
		return this.distanceToSqr(ent) <= 3;
	}
	
	protected @Nullable BlockPos movePos;
	protected @Nullable Entity moveEntity;
	
	protected void feyMoveToTask(ILogisticsTask task) {
		feyMoveToTask(task, false);
	}
	
	/**
	 * Try to move to the current subtask of the task using pathfinding.
	 * This just was copied into every fey type and was frustrating to fix bugs in so I moved it here.
	 * @param task
	 */
	protected void feyMoveToTask(ILogisticsTask task, boolean allOrNothingMovement) {
		final LogisticsSubTask sub = task.getActiveSubtask();
		if (sub == null || sub.getType() != LogisticsSubTask.Type.MOVE) {
			return;
		}
		
		if (this.navigation.isDone()) {
			// First time through?
			if ((movePos != null && isAtMoveTarget(movePos))
				|| (moveEntity != null && isAtMoveTarget(moveEntity))) {
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
				if (!this.getNavigation().moveTo(moveEntity,  1)) {
					this.getMoveControl().setWantedPosition(moveEntity.getX(), moveEntity.getY(), moveEntity.getZ(), 1.0f);
				}
			} else {
				movePos = findEmptySpot(movePos, allOrNothingMovement);
				
				// Is the block we shifted to where we are?
				if (!this.blockPosition().equals(movePos) && this.getDistanceSq(movePos) > 1) {
					// Note: tryMoveToXYZ always give slack of 1 which is pretty big!
					if (!this.getNavigation().moveTo(this.getNavigation().createPath(movePos, 0), 1.0f)) {
					//if (!this.getNavigator().tryMoveToXYZ(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5, 1.0f)) {
						this.getMoveControl().setWantedPosition(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f);
					}
				}
			}
		}
	}
	
	protected static boolean FeyWander(EntityFeyBase fey, BlockPos center, double minDist, double maxDist) {
		BlockPos targ = null;
		int attempts = 20;
		final Random rand = fey.random;
		do {
			double dist = minDist + (rand.nextDouble() * (maxDist - minDist));
			float angle = (float) (rand.nextDouble() * (2 * Math.PI));
			float tilt = (float) (rand.nextDouble() * (2 * Math.PI)) * .5f;
			
			targ = new BlockPos(new Vec3(
					center.getX() + (Math.cos(angle) * dist),
					center.getY() + (Math.cos(tilt) * dist),
					center.getZ() + (Math.sin(angle) * dist)));
			
			if (!fey.isNoGravity()) {
				while (targ.getY() > 0 && fey.level.isEmptyBlock(targ)) {
					targ = targ.below();
				}
				if (targ.getY() < 256) {
					targ = targ.above();
				}
			}
			
			// We've hit a non-air block. Make sure there's space above it
			BlockPos airBlock = null;
			for (int i = 0; i < Math.ceil(fey.getBbHeight()); i++) {
				if (airBlock == null) {
					airBlock = targ.above();
				} else {
					airBlock = airBlock.above();
				}
				
				if (!fey.level.isEmptyBlock(airBlock)) {
					targ = null;
					break;
				}
			}
		} while (targ == null && attempts-- > 0);
		
		if (targ == null) {
			targ = center.above();
		}
		if (!fey.getNavigation().moveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
			fey.getMoveControl().setWantedPosition(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
		}
		
		return true;
	}
	
	protected static boolean FeyWander(EntityFeyBase fey, BlockPos center, double maxDist) {
		return FeyWander(fey, center, maxDist * .4, maxDist);
	}
	
	protected static boolean FeyFollow(EntityFeyBase fey, LivingEntity target, double minDist, double maxDist) {
		return FeyWander(fey, target.blockPosition(), minDist, maxDist);
	}
	
	protected static boolean FeyFollow(EntityFeyBase fey, LivingEntity target, double maxDist) {
		return FeyFollow(fey, target, maxDist * .4, maxDist);
	}
	
	protected static boolean FeyFollowNearby(EntityFeyBase fey, Predicate<? super Entity> filter, boolean lazy, double maxSightDist, double minFollowDist, double maxFollowDist) {
		List<Entity> ents = fey.level.getEntities(fey,
				new AABB(fey.getX() - maxSightDist, fey.getY() - maxSightDist, fey.getZ() - maxSightDist, fey.getX() + maxSightDist, fey.getY() + maxSightDist, fey.getZ() + maxSightDist),
				filter);
		
		LivingEntity target = null;
		double minDist = 0;
		if (ents != null && !ents.isEmpty()) {
			// pick the closest
			for (Entity ent : ents) {
				if (!(ent instanceof LivingEntity)) {
					continue;
				}
				
				double dist = fey.distanceToSqr(ent);
				if (target == null || dist < minDist) {
					target = (LivingEntity) ent;
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
		public boolean test(Entity input) {
			if (input == null || !(input instanceof EntityFeyBase)) {
				return false;
			}
			
			EntityFeyBase fey = (EntityFeyBase) input;
			return fey.getStatus() == FairyGeneralStatus.IDLE || fey.getStatus() == FairyGeneralStatus.WORKING;
		}
		
	};
	
	protected static final Predicate<? super Entity> DOMESTIC_FEY_AND_PLAYER_FILTER = new Predicate<Entity>() {

		@Override
		public boolean test(Entity input) {
			if (input == null) {
				return false;
			}
			
			if (input instanceof EntityFeyBase) {
				EntityFeyBase fey = (EntityFeyBase) input;
				return fey.getStatus() == FairyGeneralStatus.IDLE || fey.getStatus() == FairyGeneralStatus.WORKING;
			}
			
			if (input instanceof Player) {
				return !((Player) input).isSpectator();
			}
			
			return false;
		}
		
	};
	
	protected static final AttributeSupplier.Builder BuildFeyAttributes() {
		return AbstractGolem.createMobAttributes()
			.add(Attributes.FOLLOW_RANGE, Math.sqrt(MAX_FAIRY_DISTANCE_SQ))
			.add(Attributes.ATTACK_DAMAGE)
		;
	}
}
