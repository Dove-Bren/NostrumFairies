package com.smanzana.nostrumfairies.entity.fey;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrumfairies.blocks.LogisticsTileEntity;
import com.smanzana.nostrumfairies.entity.navigation.PathNavigatorLogistics;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrummagica.loretag.ILoreTagged;

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
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityFeyBase extends EntityGolem implements IFeyWorker, ILoreTagged {

	protected static final DataParameter<Optional<BlockPos>> HOME  = EntityDataManager.<Optional<BlockPos>>createKey(EntityFeyBase.class, DataSerializers.OPTIONAL_BLOCK_POS);
	protected static final DataParameter<String> NAME = EntityDataManager.<String>createKey(EntityFeyBase.class, DataSerializers.STRING);
	
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
	
	/**
	 * Current task being performed.
	 */
	private @Nullable ILogisticsTask currentTask;
	
	/**
	 * Number of ticks the current task has been being run
	 */
	private int taskTickCount;
	
	
	public EntityFeyBase(World world) {
		this(world, 100, MAX_FAIRY_DISTANCE_SQ);
	}
	
	public EntityFeyBase(World world, double wanderDistanceSq, double workDistanceSq) {
		super(world);
		generalStatus = FairyGeneralStatus.WANDERING;
		this.wanderDistanceSq = wanderDistanceSq;
		this.workDistanceSq = workDistanceSq;
		
		this.navigator = new PathNavigatorLogistics(this, world);
	}
	
	@Override
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
	
	@Override
	@Nullable
	public BlockPos getHome() {
		return this.dataManager.get(HOME).orNull();
	}
	
	/**
	 * Set the block the fairy will consider its home, and try to stay close to.
	 * @return
	 */
	public void setHome(@Nullable BlockPos home) {
		this.dataManager.set(HOME, Optional.fromNullable(home));
		this.setHomePosAndDistance(home, (int) this.wanderDistanceSq);
	}
	
	/**
	 * Called to check that the provided home position is still a valid home position
	 * @param homePos
	 * @return
	 */
	protected abstract boolean isValidHome(BlockPos homePos);
	
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
		BlockPos home = getHome();
		if (home == null) {
			return null;
		}
		
		TileEntity te = worldObj.getTileEntity(home);
		if (te != null && te instanceof LogisticsTileEntity) {
			return ((LogisticsTileEntity) te).getNetwork();
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
	
	protected void finishTask() {
		// This _could_ immediately look for a new task.
		// For now, I think 1 frame delay between having a new job is okay.
		// That'll also mean that code that calls this and then looks at the task after will get NULL instead
		// of possibly the same task or possibly a new one, which might make debugging easier.
		if (this.currentTask != null) {
			for (ILogisticsTask task : this.currentTask.unmerge()) {
				this.getLogisticsNetwork().getTaskRegistry().completeTask(task);
			}
			forceSetTask(null);
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
	 * Called every tick that a task is active.
	 * @param task
	 */
	protected abstract void onTaskTick(@Nullable ILogisticsTask task);
	
	protected abstract void onIdleTick();
	
	protected abstract void onCombatTick();
	
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
		
		// TODO communiate status with DAtaParamater
		if (worldObj.isRemote || this.isDead) {
			return;
		}
		
		// TODO remove. Testing code!
		{
			if (getHome() == null) {
				if (isValidHome(getPosition().add(0,-1,0))) {
					setHome(getPosition().add(0,-1,0));
					changeStatus(FairyGeneralStatus.IDLE);
				}
			}
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
		
		switch (generalStatus) {
		case IDLE:
			onIdleTick();
			
			// Note: idle may decide to do task stuff on its own. We'll respect that.
			if (currentTask == null) {
				if (!searchForJobs()) {
					break;
				}
			}
			
			changeStatus(FairyGeneralStatus.WORKING);
			// else fall through to start working as soon as task is picked up
			// TODO maybe both should search for tasks if the task is dropable?
		case WORKING:
			if (currentTask != null) {
				if (currentTask.isComplete()) {
					finishTask();
				} else {
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
						this.onTaskTick(currentTask);
					}
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
		case WANDERING:
			// Implementation should say what these do
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
				BlockPos lPos = ILogisticsTask.GetSourcePosition(l);
				World lWorld = ILogisticsTask.GetSourceWorld(l);
				BlockPos rPos = ILogisticsTask.GetSourcePosition(r);
				World rWorld = ILogisticsTask.GetSourceWorld(r);
				
				if (!lWorld.equals(this.worldObj)) {
					return 1;
				}
				if (!rWorld.equals(this.worldObj)) {
					return -1;
				}
				
				BlockPos fairyPos = this.getPosition();
				double lDist = lPos.distanceSq(fairyPos);
				double rDist = rPos.distanceSq(fairyPos);
				return lDist < rDist ? -1 : 1;
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
		BlockPos home = this.getHome();
		if (home != null) {
			if (!this.isValidHome(home)) {
				this.setHome(null);
				home = null; // Home is no longer valid
				
				if (generalStatus != FairyGeneralStatus.WANDERING) {
					// Don't have a home, and we expected to. Homeless!
					this.changeStatus(FairyGeneralStatus.WANDERING);
				}
			}
		}
	}
	
	protected abstract String getRandomName();
	
	@Override
	protected void entityInit() {
		super.entityInit();
		
		this.dataManager.register(HOME, Optional.absent());
		this.dataManager.register(NAME, getRandomName());
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
		
		if (compound.hasKey(NBT_NAME, NBT.TAG_STRING)) {
			dataManager.set(NAME, compound.getString(NBT_NAME));
		} // else default is a random one
	}
	
	@Override
	public void setDead() {
		forfitTask();
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
}
