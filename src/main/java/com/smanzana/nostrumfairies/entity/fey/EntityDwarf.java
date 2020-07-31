package com.smanzana.nostrumfairies.entity.fey;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.blocks.MiningBlock;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskMineBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlaceBlock;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemStacks;
import com.smanzana.nostrumfairies.utils.Paths;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityDwarf extends EntityFeyBase implements IItemCarrierFey {

	public static enum ArmPose {
		IDLE,
		MINING,
		ATTACKING;
		
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
	
	protected static final DataParameter<ArmPose> POSE  = EntityDataManager.<ArmPose>createKey(EntityFeyBase.class, ArmPose.Serializer);
	private static final String NBT_ITEMS = "helditems";
	private static final int INV_SIZE = 5;
	
	private InventoryBasic inventory;
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityDwarf(World world) {
		super(world);
		this.height = .95f;
		this.workDistanceSq = 24 * 24;
		this.inventory = new InventoryBasic("Dwarf Inv", false, INV_SIZE);
	}
	
	@Override
	public String getLoreKey() {
		return "dwarf";
	}

	@Override
	public String getLoreDisplayName() {
		return "dwarf";
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
		ItemStack[] stacks = new ItemStack[INV_SIZE];
		int idx = 0;
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null) {
				stacks[idx++] = stack;
			}
		}
		return stacks;
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		return ItemStacks.canFit(inventory, stack);
	}
	
	@Override
	public boolean canAccept(ItemDeepStack stack) {
		return ItemStacks.canFitAll(inventory, Lists.newArrayList(stack));
	}

	@Override
	public void addItem(ItemStack stack) {
		ItemStacks.addItem(inventory, stack);
	}
	
	@Override
	public void removeItem(ItemStack stack) {
		ItemStacks.remove(inventory, stack);
	}
	
	protected boolean hasItems() {
		for (int i = 0; i < INV_SIZE; i++) {
			if (inventory.getStackInSlot(i) != null) {
				return true;
			}
		}
		
		return false;
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
		if (te == null || !(te instanceof MiningBlock.MiningBlockTileEntity)) {
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
		if (task instanceof LogisticsTaskMineBlock) {
			long start = System.currentTimeMillis();
			LogisticsTaskMineBlock mine = (LogisticsTaskMineBlock) task;
			
			if (mine.getWorld() != this.worldObj) {
				//System.out.println("\t\t Exit A: " + (System.currentTimeMillis() - start));
				return false;
			}
			
			// Dwarves only perform tasks from their mine
			if (this.getHome() == null || mine.getSourceComponent() == null ||
					!this.getHome().equals(mine.getSourceComponent().getPosition())) {
				//System.out.println("\t\t Exit B: " + (System.currentTimeMillis() - start));
				return false;
			}
			
			//System.out.println("\t\tdelta: " + (System.currentTimeMillis() - start));
			
			// Check where the block is
			// EDIT mines have things go FAR down, so we ignore the distance check here
			BlockPos target = mine.getTargetMineLoc();
			if (target == null) {
				//System.out.println("\t\t Exit C: " + (System.currentTimeMillis() - start));
				return false;
			}
			
			//System.out.println("\t\tdelta: " + (System.currentTimeMillis() - start));
			
			if (this.getCurrentTask() != null
					&& this.getCurrentTask() instanceof LogisticsTaskMineBlock) {
				
				//System.out.println("\t\t--- delta: " + (System.currentTimeMillis() - start));
				
				// Try to stay around the other tasks
				if (((LogisticsTaskMineBlock)this.getCurrentTask()).getTargetMineLoc().distanceSq(mine.getTargetMineLoc()) > 25) {
					//System.out.println("\t\t Exit D: " + (System.currentTimeMillis() - start));
					return false;
				}
				
				//System.out.println("\t\t*** delta: " + (System.currentTimeMillis() - start));
				
				// If we already have a mining task, we ask the mine to see if we'll be able to get to this task
				// with what we already have.
				// Otherwise we look for an empty spot and see if we can path.
				if (mine.getSourceComponent() != null) {
					TileEntity te = worldObj.getTileEntity(mine.getSourceComponent().getPosition());
					if (te != null && te instanceof MiningBlock.MiningBlockTileEntity) {
						//System.out.println("\t\t Exit E: " + (System.currentTimeMillis() - start));
						return ((MiningBlock.MiningBlockTileEntity) te).taskAccessibleWithTasks(mine, this);
					}
					
					//return taskAccessibleWithTasks
				}
				//System.out.println("\t\tdelta: " + (System.currentTimeMillis() - start));
			}
			
			// else
			
			target = findEmptySpot(target, true);
			if (target == null) {
				//System.out.println("\t\t Exit F: " + (System.currentTimeMillis() - start));
				return false;
			}
			
			// Check for pathing
			if (this.getDistanceSq(target) < .2) {
				//System.out.println("\t\t Exit G: " + (System.currentTimeMillis() - start));
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
			if (success || this.getDistanceSq(target) < 1) {
				// extra case for if the navigator refuses cause we're too close
				//System.out.println("\t\t Exit : " + (System.currentTimeMillis() - start));
				return true;
			}
			//System.out.println("\t\t Exit Z: " + (System.currentTimeMillis() - start));
		} else if (task instanceof LogisticsTaskPlaceBlock) {
			LogisticsTaskPlaceBlock place = (LogisticsTaskPlaceBlock) task;
			
			if (place.getWorld() != this.worldObj) {
				return false;
			}
			
			// Dwarves only perform tasks from their mine
			if (this.getHome() == null || place.getSourceComponent() == null ||
					!this.getHome().equals(place.getSourceComponent().getPosition())) {
				return false;
			}
			
			// Check where the block is
			// EDIT mines have things go FAR down, so we ignore the distance check here
			BlockPos target = place.getTargetPlaceLoc();
			if (target == null) {
				return false;
			}
			
			target = findEmptySpot(target, true);
			if (target == null) {
				return false;
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
		}
		
		return false;
	}
	
	private void dropItems() {
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack heldItem = inventory.getStackInSlot(i);
			if (heldItem == null) {
				continue;
			}
			EntityItem item = new EntityItem(this.worldObj, posX, posY, posZ, heldItem);
			worldObj.spawnEntityInWorld(item);
		}
		inventory.clear();
	}

	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		return true;
	}

	@Override
	protected void onTaskChange(ILogisticsTask oldTask, ILogisticsTask newTask) {
		if (newTask == null) {
			this.setPose(ArmPose.IDLE);
		}
	}
	
	@Override
	protected void onIdleTick() {
		this.setPose(ArmPose.IDLE);
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		if (hasItems()) {
			ItemStack held = null;
			
			for (int i = 0; i < INV_SIZE; i++) {
				held = inventory.getStackInSlot(i);
				if (held != null) {
					break;
				}
			}
			
			if (held != null) {
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
			}
			
			// no return means we couldn't set up a task to drop it
			dropItems();
			
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
		
		// Mining dwarves should place down lights in the mines and refresh those around them
		if (task instanceof LogisticsTaskMineBlock && this.ticksExisted % 5 == 0) {
			if (!this.worldObj.canBlockSeeSky(this.getPosition())) {
				// No light from the 'sky' which means we're underground
				// Refreseh magic lights around. Then see if it's too dark
				IBlockState state;
				MutableBlockPos cursor = new MutableBlockPos();
				for (int x = -1; x <= 1; x++)
				for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++) {
					cursor.setPos(x, y, z);
					state = worldObj.getBlockState(cursor);
					if (state != null && state.getBlock() instanceof MagicLight) {
						MagicLight.Bright().refresh(worldObj, cursor.toImmutable());
					}
				}
				
				if (this.worldObj.getLightFor(EnumSkyBlock.BLOCK, this.getPosition()) < 8) {
					if (this.worldObj.isAirBlock(this.getPosition().up().up())) {
						worldObj.setBlockState(this.getPosition().up().up(), MagicLight.Bright().getDefaultState());
					} else if (this.worldObj.isAirBlock(this.getPosition().up())) {
						worldObj.setBlockState(this.getPosition().up(), MagicLight.Bright().getDefaultState());
					}
				}
			}
		}
		
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			switch (sub.getType()) {
			case ATTACK:
				this.setPose(ArmPose.ATTACKING);
				this.faceEntity(sub.getEntity(), 30, 180);
				break;
			case BREAK:
				if (this.isSwingInProgress) {
					;
				} else {
					this.setPose(ArmPose.MINING);
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						this.setPose(ArmPose.IDLE);
						break;
					}
					this.swingArm(getActiveHand());
					BlockPos pos = sub.getPos();
					double d0 = pos.getX() - this.posX;
			        double d2 = pos.getZ() - this.posZ;
					float desiredYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					
					this.rotationYaw = desiredYaw;
				}
//				// this is where we'd play some animation?
//				if (this.onGround) {
//					BlockPos pos = sub.getPos();
//					double d0 = pos.getX() - this.posX;
//			        double d2 = pos.getZ() - this.posZ;
//					float desiredYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
//					
//					this.rotationYaw = desiredYaw;
//					
//					task.markSubtaskComplete();
//					if (task.getActiveSubtask() != sub) {
//						break;
//					}
//					this.jump();
//				}
				break;
			case IDLE:
				this.setPose(ArmPose.IDLE);
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
					this.setPose(ArmPose.IDLE);
					if (this.navigator.noPath()) {
						// First time through?
						if ((movePos != null && this.getDistanceSqToCenter(movePos) < 1)
							|| (moveEntity != null && this.getDistanceToEntity(moveEntity) < 1)) {
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
		int priority = 1;
		this.tasks.addTask(priority++, new EntityAIAttackMelee(this, 1.0, true)); // also gated on target, like 'combat tick' on fey mechs
		
		priority = 1;
		this.targetTasks.addTask(priority++, new EntityAIHurtByTarget(this, true, new Class[0]));
		
		// Could hunt mobs
//		this.targetTasks.addTask(priority++, new EntityAINearestAttackableTarget<EntityMob>(this, EntityMob.class, 10, true, false, (mob) -> {
//			return (mob instanceof IEntityTameable ? !((IEntityTameable) mob).isTamed()
//					: true);
//		}));
		
		// TODO Auto-generated method stub
		// I guess we should wander and check if tehre's a home nearby and if so make it our home and stop wandering.
		// Or if we're revolting... just quit for this test one?
		// Or if we're working, dont use AI
		// Or if we're idle... wander?
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	private NBTTagList inventoryToNBT() {
		NBTTagList list = new NBTTagList();
		
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null) {
				list.appendTag(stack.serializeNBT());
			}
		}
		
		return list;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		compound.setTag(NBT_ITEMS, inventoryToNBT());
	}
	
	private void loadInventoryFromNBT(NBTTagList list) {
		inventory.clear();
		
		for (int i = 0; i < list.tagCount(); i++) {
			inventory.setInventorySlotContents(i, ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
		}
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		loadInventoryFromNBT(compound.getTagList(NBT_ITEMS, NBT.TAG_COMPOUND));
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return !this.hasItems();
	}
	
	@Override
	protected void collideWithEntity(Entity entityIn) {
		if (entityIn instanceof IFeyWorker) {
			IFeyWorker other = (IFeyWorker) entityIn;
			if ((this.getCurrentTask() != null)
				|| (other.getCurrentTask() != null)) {
					return;
			}
		}
		
		super.collideWithEntity(entityIn);
	}
	
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		
		// Dwarves are 40:60 lefthanded
		if (this.rand.nextFloat() < .4f) {
			this.setLeftHanded(true);
		}
		
		return livingdata;
	}
	
	private String getRandomFirstName() {
		final String[] names = new String[] {"Griliggs",
				"Magnir",
				"Hjalmor",
				"Hjulkum",
				"Ragdren",
				"Raggran",
				"Gerdor",
				"Karmar",
				"Murrik",
				"Dulrigg",
				"Harron",
				"Kramkyl",
				"Grennur",
				"Kharthrun",
				"Grildal",
				"Baerrus",
				"Morgron",
				"Torkohm",
				"Bandus",
				"Amnik",};
		return names[this.rand.nextInt(names.length)];
	}
	
	private String getRandomLastName() {
		final String[] names = new String[] {"Griliggs",
				"Nightbelly",
				"Warshield",
				"Gravelblade",
				"Thunderforged",
				"Emberbranch",
				"Opalbasher",
				"Deeptank",
				"Oreview",
				"Earthbrew",
				"Whitchest",
				"Stronggranite",
				"Honorarm",
				"Pebblechest",
				"Thunderback",
				"Fierycoat",
				"Dragonstone",
				"Dragonmantle",
				"Twilightmail",
				"Amberchest",
				"Hillgranite"};
		return names[this.rand.nextInt(names.length)];
	}

	@Override
	protected String getRandomName() {
		return getRandomFirstName() + " " + getRandomLastName();
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(POSE, ArmPose.IDLE);
	}
	
	public ArmPose getPose() {
		return dataManager.get(POSE);
	}
	
	public void setPose(ArmPose pose) {
		this.dataManager.set(POSE, pose);
	}

	@Override
	protected void onCombatTick() {
		this.setPose(ArmPose.ATTACKING);
	}
}
