package com.smanzana.nostrumfairies.entity.fey;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPickupItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemStacks;
import com.smanzana.nostrumfairies.utils.Paths;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityGnome extends EntityFeyBase implements IItemCarrierFey {
	
	public static enum ArmPose {
		IDLE,
		WORKING,
		CARRYING;
		
		public final static class PoseSerializer implements DataSerializer<ArmPose> {
			
			private PoseSerializer() {
				DataSerializers.registerSerializer(this);
			}
			
			@Override
			public void write(PacketBuffer buf, ArmPose value) {
				buf.writeEnumValue(value);
			}

			@Override
			public ArmPose read(PacketBuffer buf) throws IOException {
				return buf.readEnumValue(ArmPose.class);
			}

			@Override
			public DataParameter<ArmPose> createKey(int id) {
				return new DataParameter<>(id, this);
			}
		}
		
		public static final PoseSerializer Serializer = new PoseSerializer();
	}
	
	protected static final DataParameter<ArmPose> POSE  = EntityDataManager.<ArmPose>createKey(EntityGnome.class, ArmPose.Serializer);
	private static final DataParameter<Optional<ItemStack>> DATA_HELD_ITEM = EntityDataManager.<Optional<ItemStack>>createKey(EntityGnome.class, DataSerializers.OPTIONAL_ITEM_STACK);

	private static final String NBT_ITEM = "helditem";
	
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityGnome(World world) {
		super(world);
		this.height = .6f;
		this.width = .3f;
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
	
	public @Nullable ItemStack getCarriedItem() {
		return this.dataManager.get(DATA_HELD_ITEM).orNull();
	}

	@Override
	public ItemStack[] getCarriedItems() {
		return new ItemStack[]{getCarriedItem()};
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		ItemStack heldItem = getCarriedItem();
		return heldItem == null ||
				(ItemStacks.stacksMatch(heldItem, stack) && heldItem.stackSize + stack.stackSize < heldItem.getMaxStackSize());
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
		if (heldItem == null) {
			heldItem = stack.copy();
		} else {
			// Just assume canAccept was called
			heldItem.stackSize += stack.stackSize; 
		}
		this.dataManager.set(DATA_HELD_ITEM, Optional.of(heldItem));
	}
	
	@Override
	public void removeItem(ItemStack stack) {
		ItemStack heldItem = getCarriedItem();
		if (heldItem != null) {
			if (ItemStacks.stacksMatch(stack, heldItem)) {
				heldItem.stackSize -= stack.stackSize;
				if (heldItem.stackSize <= 0) {
					heldItem = null;
				}
			}
		}
		this.dataManager.set(DATA_HELD_ITEM, Optional.fromNullable(heldItem));
	}
	
	protected boolean hasItems() {
		return getCarriedItem() != null;
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
	
	private @Nullable BlockPos findEmptySpot(BlockPos targetPos, boolean allOrNothing) {
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
				if (worldObj.isAirBlock(targetPos.up())) {
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

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskPlantItem) {
			LogisticsTaskPlantItem plant = (LogisticsTaskPlantItem) task;
			
			if (plant.getWorld() != this.worldObj) {
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
			
			return true;
		}
		
		return false;
	}
	
	private void dropItem() {
		EntityItem item = new EntityItem(this.worldObj, posX, posY, posZ, getCarriedItem());
		worldObj.spawnEntityInWorld(item);
		this.dataManager.set(DATA_HELD_ITEM, Optional.absent());
	}

	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		//return this.heldItem == null;
		return true;
	}

	@Override
	protected void onTaskChange(ILogisticsTask oldTask, ILogisticsTask newTask) {
//		if (oldTask != null && heldItem != null) {
//			// I guess drop our item
//			dropItem();
//		}
		// Task should have checked if we could hold what it needed, if it's item related.
		// Assuming it did, our current inventory is fine. We'll do that task, maybe use our
		// inventory, and then be idle with an item afterwards -- whicih will prompt
		// us to go return it.
	}
	
	@Override
	protected void onIdleTick() {
		this.setPose(ArmPose.IDLE);
		
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		if (hasItems()) {
			ItemStack held = getCarriedItem();
			
			LogisticsNetwork network = this.getLogisticsNetwork();
			if (network != null) {
				@Nullable ILogisticsComponent storage = network.getStorageForItem(worldObj, getPosition(), held);
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
				this.faceEntity(sub.getEntity(), 30, 180);
				break;
			case BREAK: {
				BlockPos pos = sub.getPos();
				double d0 = pos.getX() - this.posX;
		        double d2 = pos.getZ() - this.posZ;
				float desiredYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
				
				this.setPose(ArmPose.WORKING);
				this.rotationYaw = desiredYaw;
				if (this.isSwingInProgress) {
					// On the client, spawn some particles if we're using our wand
					// lel what if we sweat? xD
//					if (ticksExisted % 5 == 0 && getPose() == ArmPose.CHOPPING) {
//						worldObj.spawnParticle(EnumParticleTypes.DRAGON_BREATH,
//								posX, posY, posZ,
//								0, 0.3, 0,
//								new int[0]);
//					}
				} else {
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						setPose(ArmPose.IDLE);
						break;
					}
					this.swingArm(this.getActiveHand());
				}
				break;
			}
			case IDLE:
				if (this.hasItems()) {
					this.setPose(ArmPose.CARRYING);
				} else {
					this.setPose(ArmPose.IDLE);
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
					if (this.hasItems()) {
						this.setPose(ArmPose.CARRYING);
					} else {
						this.setPose(ArmPose.IDLE);
					}
					if (this.navigator.noPath()) {
						// First time through?
						if ((movePos != null && this.getDistanceSqToCenter(movePos) < 2)
							|| (moveEntity != null && this.getDistanceToEntity(moveEntity) < 2)) {
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
							movePos = findEmptySpot(movePos, false);
							
							// Is the block we shifted to where we are?
							if (!this.getPosition().equals(movePos) && this.getDistanceSqToCenter(movePos) > 1) {
								if (!this.getNavigator().tryMoveToXYZ(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f)) {
									this.moveHelper.setMoveTo(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f);
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
	protected void initEntityAI() {
		; // Could panic when they are attacked!
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.21D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		//this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		ItemStack held = getCarriedItem();
		if (held != null) {
			compound.setTag(NBT_ITEM, held.serializeNBT());
		}
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		if (compound.hasKey(NBT_ITEM, NBT.TAG_COMPOUND)) {
			dataManager.set(DATA_HELD_ITEM, Optional.fromNullable(ItemStack.loadItemStackFromNBT(compound.getCompoundTag(NBT_ITEM))));
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
	protected void entityInit() {
		super.entityInit();
		dataManager.register(POSE, ArmPose.IDLE);
		dataManager.register(DATA_HELD_ITEM, Optional.absent());
	}
	
	public ArmPose getPose() {
		return dataManager.get(POSE);
	}
	
	public void setPose(ArmPose pose) {
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
}
