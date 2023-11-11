package com.smanzana.nostrumfairies.entity.fey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.blocks.FeyHomeBlock.ResidentType;
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
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityFairy extends EntityFeyBase implements IItemCarrierFey {

	private static final String NBT_ITEM = "helditem";
	private static final DataParameter<ItemStack> DATA_HELD_ITEM = EntityDataManager.<ItemStack>createKey(EntityFairy.class, IDataSerializers.ITEM_STACK);
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityFairy(World world) {
		super(world);
		this.height = .25f;
		this.width = .25f;
		this.workDistanceSq = 100 * 100;
		this.noClip = true;

		this.moveHelper = new FairyFlyMoveHelper(this);
	}

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
	
	protected @Nonnull ItemStack getHeldItem() {
		return this.dataManager.get(DATA_HELD_ITEM);
	}

	@Override
	public NonNullList<ItemStack> getCarriedItems() {
		return NonNullList.from(null, getHeldItem());
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
		this.dataManager.set(DATA_HELD_ITEM, heldItem);
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
		this.dataManager.set(DATA_HELD_ITEM, heldItem);
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
				if (this.getDistanceSq(retrieve.getSourceEntity()) < .2) {
					return true;
				}
				
				return true;
			} else {
				BlockPos pos = source.getPosition();
				
				if (!world.isAirBlock(pos)) {
					if (world.isAirBlock(pos.north())) {
						pos = pos.north();
					} else if (world.isAirBlock(pos.south())) {
						pos = pos.south();
					} else if (world.isAirBlock(pos.east())) {
						pos = pos.east();
					} else if (world.isAirBlock(pos.west())) {
						pos = pos.west();
					} else {
						pos = pos.up();
					}
				}
				
				if (this.getDistanceSq(pos) < .2 || this.getPosition().equals(pos)) {
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
				if (this.getDistanceSq(deposit.getSourceEntity()) < .2) {
					return true;
				}
				
				return true;
			} else {
				BlockPos pos = source.getPosition();
				
				if (!world.isAirBlock(pos)) {
					if (world.isAirBlock(pos.north())) {
						pos = pos.north();
					} else if (world.isAirBlock(pos.south())) {
						pos = pos.south();
					} else if (world.isAirBlock(pos.east())) {
						pos = pos.east();
					} else if (world.isAirBlock(pos.west())) {
						pos = pos.west();
					} else {
						pos = pos.up();
					}
				}
				
				if (this.getDistanceSq(pos) < .2 || this.getPosition().equals(pos)) {
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
		ItemEntity item = new ItemEntity(this.world, posX, posY, posZ, getHeldItem());
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
				@Nullable ILogisticsComponent storage = network.getStorageForItem(world, getPosition(), heldItem);
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
		if (!this.moveHelper.isUpdating()) {
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
					for (int i = 0; i < Math.ceil(this.height); i++) {
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
					this.moveHelper.setMoveTo(targ.getX() + .5, targ.getY() + .5, targ.getZ() + .5, 1.0f);
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
				if (this.rand.nextBoolean()) {
					task.markSubtaskComplete();
				}
				break;
			case IDLE:
				if (movePos == null || !this.moveHelper.isUpdating()) {
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
							for (int i = 0; i < Math.ceil(this.height); i++) {
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
							this.moveHelper.setMoveTo(targ.getX() + .5, targ.getY() + .5, targ.getZ() + .5, 1.0f);
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
					if (movePos == null || !this.moveHelper.isUpdating()) {
						// First time through?
						if ((movePos != null && this.getDistanceSqToCenter(movePos) < .5)
							|| (moveEntity != null && this.getDistance(moveEntity) < .5)) {
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
							//if (!this.getNavigator().tryMoveToMobEntity(moveEntity,  1)) {
								this.moveHelper.setMoveTo(moveEntity.posX, moveEntity.posY, moveEntity.posZ, 1.0f);
							//}
						} else {
							if (!world.isAirBlock(movePos)) {
								if (world.isAirBlock(movePos.north())) {
									movePos = movePos.north();
								} else if (world.isAirBlock(movePos.south())) {
									movePos = movePos.south();
								} else if (world.isAirBlock(movePos.east())) {
									movePos = movePos.east();
								} else if (world.isAirBlock(movePos.west())) {
									movePos = movePos.west();
								} else {
									movePos = movePos.up();
								}
							}
							//if (!this.getNavigator().tryMoveToXYZ(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5, 1.0f)) {
								this.moveHelper.setMoveTo(movePos.getX() + .5, movePos.getY() + .5, movePos.getZ() + .5, 1.0f);
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
//						} else if (!moveHelper.isUpdating()) {
//							if (!this.getNavigator().tryMoveToXYZ(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.0f)) {
//								this.moveHelper.setMoveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.0f);
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

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
		//this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DATA_HELD_ITEM, ItemStack.EMPTY);
	}
	
	@Override
	public boolean hasNoGravity() {
		return true;
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {
		;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
		return distance < 6400;
	}
	
	@Override
	public void writeEntityToNBT(CompoundNBT compound) {
		super.writeEntityToNBT(compound);
		
		if (!getHeldItem().isEmpty()) {
			compound.put(NBT_ITEM, getHeldItem().writeToNBT(new CompoundNBT()));
		}
	}
	
	@Override
	public void readEntityFromNBT(CompoundNBT compound) {
		super.readEntityFromNBT(compound);
		
		if (compound.hasKey(NBT_ITEM)) {
			this.dataManager.set(DATA_HELD_ITEM, new ItemStack(compound.getCompoundTag(NBT_ITEM)));
		}
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return this.getHeldItem().isEmpty();
	}
	
	static class FairyFlyMoveHelper extends EntityMoveHelper {
		private final MobEntity parentEntity;
		private double lastDist;
		private int courseChangeCooldown;

		public FairyFlyMoveHelper(MobEntity entity) {
			super(entity);
			this.parentEntity = entity;
		}

		public void onUpdateMoveHelper() {
			if (this.action == EntityMoveHelper.Action.MOVE_TO) {
				double d0 = this.posX - this.parentEntity.posX;
				double d1 = this.posY - this.parentEntity.posY;
				double d2 = this.posZ - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				d3 = (double)MathHelper.sqrt(d3);
				
				if (Math.abs(d3) < .25) {
					lastDist = 0.0D;
					this.parentEntity.motionX = 0;
					this.parentEntity.motionY = 0;
					this.parentEntity.motionZ = 0;
					this.action = EntityMoveHelper.Action.WAIT;
					return;
				} else if (lastDist != 0.0D && Math.abs(lastDist - d3) < 0.05) {
					courseChangeCooldown--;
				} else {
					courseChangeCooldown = this.parentEntity.getRNG().nextInt(5) + 10;
				}

				if (courseChangeCooldown <= 0) {
					lastDist = 0.0D;
					this.action = EntityMoveHelper.Action.WAIT;
				} else {
					float speed = (float) this.parentEntity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
					//speed *= 3f;
					this.parentEntity.motionX = (d0 / d3) * speed;
					this.parentEntity.motionY = (d1 / d3) * speed;
					this.parentEntity.motionZ = (d2 / d3) * speed;
					
					lastDist = d3;
					
					float f9 = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
				}
			} else if (this.action == EntityMoveHelper.Action.STRAFE) {
				this.entity.setMoveStrafing(moveStrafe);
				this.entity.setMoveForward(moveForward);
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
		return names[rand.nextInt(names.length)];
	}

	@Override
	protected void onCombatTick() {
		; // No combat
	}
	
	@Override
	protected void onCientTick() {
		int color = 0x40CCFFDD;
		NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
				1, posX, posY + height/2f, posZ, 0, 40, 0,
				new Vec3d(rand.nextFloat() * .025 - .0125, rand.nextFloat() * .025 - .0125, rand.nextFloat() * .025 - .0125), null
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
		return rand.nextBoolean() && rand.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		//System.out.println(this.getPosition());
		if (this.navigator.noPath() && ticksExisted % 50 == 0 && rand.nextBoolean() && rand.nextBoolean()) {
			if (!EntityFeyBase.FeyLazyFollowNearby(this, EntityFeyBase.DOMESTIC_FEY_AND_PLAYER_FILTER, 20, 2, 5)) {
				// Go to a random place
				EntityFeyBase.FeyWander(this, this.getPosition(), Math.min(10, Math.sqrt(this.wanderDistanceSq)));
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
		if (world.isRemote) {
			return null;
		}
		
		EntityPersonalFairy replacement = new EntityPersonalFairy(world);
		
		// Kill this entity and add the other one
		if (this.getHome() != null) {
			this.setHome(null);
		}
		replacement.copyFrom(this);
		world.removeEntityDangerously(this);
		world.addEntity(replacement);
		
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
}
