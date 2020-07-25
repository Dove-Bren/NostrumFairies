package com.smanzana.nostrumfairies.entity.fairy;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPickupItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWithdrawItem;
import com.smanzana.nostrumfairies.utils.ItemStacks;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityTestFairy extends EntityFairyBase implements IItemCarrierFairy {

	private static final String NBT_ITEM = "helditem";
	
	private ItemStack heldItem;
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityTestFairy(World world) {
		super(world);
		this.height = .6f;
		this.workDistanceSq = 24 * 24;
	}

	@Override
	public String getLoreKey() {
		return "testfairy";
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
	public ItemStack[] getCarriedItems() {
		return new ItemStack[]{heldItem};
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		return heldItem == null ||
				(ItemStacks.stacksMatch(heldItem, stack) && heldItem.stackSize + stack.stackSize < heldItem.getMaxStackSize());
	}

	@Override
	public void addItem(ItemStack stack) {
		if (heldItem == null) {
			heldItem = stack.copy();
		} else {
			// Just assume canAccept was called
			heldItem.stackSize += stack.stackSize; 
		}
	}
	
	@Override
	public void removeItem(ItemStack stack) {
		if (heldItem != null) {
			if (ItemStacks.stacksMatch(stack, heldItem)) {
				heldItem.stackSize -= stack.stackSize;
				if (heldItem.stackSize <= 0) {
					heldItem = null;
				}
			}
		}
	}

	@Override
	protected boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to) {

		// We want to just drop our task if our status changes from WORKING
		if (from == FairyGeneralStatus.WORKING) {
			this.forfitTask();
		}
		
		return true;
	}

	@Override
	protected boolean isValidHome(BlockPos homePos) {
		TileEntity te = worldObj.getTileEntity(homePos);
		if (te == null || !(te instanceof StorageLogisticsChest.StorageChestTileEntity)) {
			return false;
		}
		
		return true;
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
				if (this.getDistanceSqToEntity(retrieve.getSourceEntity()) < .2) {
					return true;
				}
				
				if (this.navigator.tryMoveToEntityLiving(retrieve.getSourceEntity(), 1.0)) {
					navigator.clearPathEntity();
					return true;
				}
			} else {
				BlockPos pos = source.getPosition();
				
				if (!worldObj.isAirBlock(pos)) {
					if (worldObj.isAirBlock(pos.north())) {
						pos = pos.north();
					} else if (worldObj.isAirBlock(pos.south())) {
						pos = pos.south();
					} else if (worldObj.isAirBlock(pos.east())) {
						pos = pos.east();
					} else if (worldObj.isAirBlock(pos.west())) {
						pos = pos.west();
					} else {
						pos = pos.up();
					}
				}
				
				if (this.getDistanceSq(pos) < .2 || this.getPosition().equals(pos)) {
					return true;
				}
				
				if (this.navigator.tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0)) {
					navigator.clearPathEntity();
					return true;
				}
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
				if (this.getDistanceSqToEntity(deposit.getSourceEntity()) < .2) {
					return true;
				}
				if (this.navigator.tryMoveToEntityLiving(deposit.getSourceEntity(), 1.0)) {
					navigator.clearPathEntity();
					return true;
				}
			} else {
				BlockPos pos = source.getPosition();
				
				if (!worldObj.isAirBlock(pos)) {
					if (worldObj.isAirBlock(pos.north())) {
						pos = pos.north();
					} else if (worldObj.isAirBlock(pos.south())) {
						pos = pos.south();
					} else if (worldObj.isAirBlock(pos.east())) {
						pos = pos.east();
					} else if (worldObj.isAirBlock(pos.west())) {
						pos = pos.west();
					} else {
						pos = pos.up();
					}
				}
				
				if (this.getDistanceSq(pos) < .2 || this.getPosition().equals(pos)) {
					return true;
				}
				
				if (this.navigator.tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0)) {
					navigator.clearPathEntity();
					return true;
				}
			}
		} else if (task instanceof LogisticsTaskPickupItem) {
			LogisticsTaskPickupItem pickupTask = (LogisticsTaskPickupItem) task;
			
			// Check where the retrieval task wants us to go to pick up
			BlockPos pickup = pickupTask.getDestination();
			if (pickup != null && !this.canReach(pickup, true)) {
				return false;
			}
			
			// Check for pathing
			if (this.getDistanceSqToEntity(pickupTask.getEntityItem()) < .2) {
				return true;
			}
			if (this.navigator.tryMoveToEntityLiving(pickupTask.getEntityItem(), 1.0)) {
				navigator.clearPathEntity();
				return true;
			}
		}
		
		return false;
	}
	
	private void dropItem() {
		EntityItem item = new EntityItem(this.worldObj, posX, posY, posZ, heldItem);
		worldObj.spawnEntityInWorld(item);
		heldItem = null;
	}

	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		//return this.heldItem == null;
		return true;
	}

	@Override
	protected void onTaskChange(ILogisticsTask oldTask, ILogisticsTask newTask) {
		if (oldTask != null && heldItem != null) {
			// I guess drop our item
			dropItem();
		}
	}
	
	@Override
	protected void onIdleTick() {
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		if (heldItem != null) {
			LogisticsNetwork network = this.getLogisticsNetwork();
			if (network != null) {
				@Nullable ILogisticsComponent storage = network.getStorageForItem(worldObj, getPosition(), heldItem);
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
					while (targ.getY() > 0 && worldObj.isAirBlock(targ)) {
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
						
						if (!worldObj.isAirBlock(airBlock)) {
							targ = null;
							break;
						}
					}
				} while (targ == null && attempts > 0);
				
				if (targ == null) {
					targ = center.up();
				}
				if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.moveHelper.setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
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
				break;
			case BREAK:
				// this is where we'd play some animation?
				if (this.onGround) {
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						break;
					}
					this.jump();
				}
				break;
			case IDLE:
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
							while (targ.getY() > 0 && worldObj.isAirBlock(targ)) {
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
								
								if (!worldObj.isAirBlock(airBlock)) {
									targ = null;
									break;
								}
							}
						} while (targ == null && attempts > 0);
						
						if (targ == null) {
							targ = center.up();
						}
						if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
							this.moveHelper.setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
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
					if (this.navigator.noPath()) {
						// First time through?
						if ((movePos != null && this.getDistanceSqToCenter(movePos) < .5)
							|| (moveEntity != null && this.getDistanceToEntity(moveEntity) < .5)) {
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
								this.moveHelper.setMoveTo(moveEntity.posX, moveEntity.posY, moveEntity.posZ, 1.0f);
							}
						} else {
							if (!worldObj.isAirBlock(movePos)) {
								if (worldObj.isAirBlock(movePos.north())) {
									movePos = movePos.north();
								} else if (worldObj.isAirBlock(movePos.south())) {
									movePos = movePos.south();
								} else if (worldObj.isAirBlock(movePos.east())) {
									movePos = movePos.east();
								} else if (worldObj.isAirBlock(movePos.west())) {
									movePos = movePos.west();
								} else {
									movePos = movePos.up();
								}
							}
							if (!this.getNavigator().tryMoveToXYZ(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5, 1.0f)) {
								this.moveHelper.setMoveTo(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5, 1.0f);
							}
						}
					}
//					else {
//						// Check if we're close to where we need to be
//						double distSq;
//						if (movePos == null) {
//							distSq = this.getDistanceSqToEntity(moveEntity);
//						} else {
//							distSq = this.getDistanceSq(movePos);
//						}
//						
//						if (distSq < .2) {
//							task.markSubtaskComplete();
//							this.navigator.clearPathEntity();
//						}
//					}
					// FIXME this runs every tick. Save pos?
//					BlockPos pos = sub.getPos();
//					if (pos == null) {
//						EntityLivingBase entity = sub.getEntity();
//						
//					} else {
//						if (worldObj.isAirBlock(pos.north())) {
//							pos = pos.north();
//						} else if (worldObj.isAirBlock(pos.south())) {
//							pos = pos.south();
//						} else if (worldObj.isAirBlock(pos.east())) {
//							pos = pos.east();
//						} else if (worldObj.isAirBlock(pos.west())) {
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
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.24D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0D);
		//this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		if (heldItem != null) {
			compound.setTag(NBT_ITEM, heldItem.writeToNBT(new NBTTagCompound()));
		}
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		if (compound.hasKey(NBT_ITEM)) {
			heldItem = ItemStack.loadItemStackFromNBT(compound.getCompoundTag(NBT_ITEM));
		}
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return this.heldItem == null;
	}
}
