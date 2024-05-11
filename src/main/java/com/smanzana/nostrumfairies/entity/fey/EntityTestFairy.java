package com.smanzana.nostrumfairies.entity.fey;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskMineBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemDeepStacks;
import com.smanzana.nostrumfairies.utils.Paths;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityTestFairy extends EntityFeyBase implements IItemCarrierFey {
	
	public static final String ID = "test_fairy";

	private static final String NBT_ITEMS = "helditems";
	private static final int INV_SIZE = 5;
	
	private Inventory inventory;
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityTestFairy(EntityType<? extends EntityTestFairy> type, World world) {
		super(type, world);
		this.workDistanceSq = 24 * 24;
		this.inventory = new Inventory(INV_SIZE);
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
	public NonNullList<ItemStack> getCarriedItems() {
		NonNullList<ItemStack> stacks = NonNullList.withSize(INV_SIZE, ItemStack.EMPTY);
		int idx = 0;
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				stacks.set(idx++, stack);
			}
		}
		return stacks;
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		return Inventories.canFit(inventory, stack);
	}
	
	@Override
	public boolean canAccept(ItemDeepStack stack) {
		return ItemDeepStacks.canFitAll(inventory, Lists.newArrayList(stack));
	}

	@Override
	public void addItem(ItemStack stack) {
		Inventories.addItem(inventory, stack);
	}
	
	@Override
	public void removeItem(ItemStack stack) {
		Inventories.remove(inventory, stack);
	}
	
	protected boolean hasItems() {
		for (int i = 0; i < INV_SIZE; i++) {
			if (!inventory.getStackInSlot(i).isEmpty()) {
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
	protected boolean canPerformTask(ILogisticsTask task) {
//		if (task instanceof LogisticsTaskChopTree) {
//			LogisticsTaskChopTree chop = (LogisticsTaskChopTree) task;
//			
//			if (chop.getWorld() != this.world) {
//				return false;
//			}
//			
//			// Check where the tree is
//			BlockPos pickup = chop.getTrunkPos();
//			if (pickup == null || !this.canReach(pickup, true)) {
//				return false;
//			}
//			
//			pickup = findEmptySpot(pickup, true);
//			if (null == pickup) {
//				return false;
//			}
//			
//			// Check for pathing
//			if (this.getDistanceSq(pickup) < .2) {
//				return true;
//			}
//			if (this.navigator.tryMoveToXYZ(pickup.getX(), pickup.getY(), pickup.getZ(), 1.0)) {
//				navigator.clearPath();
//				return true;
//			}
//		} else if (task instanceof LogisticsTaskMineBlock) {
//			LogisticsTaskMineBlock mine = (LogisticsTaskMineBlock) task;
//			
//			if (mine.getWorld() != this.world) {
//				return false;
//			}
//			
//			// Check where the block is
//			// EDIT mines have things go FAR down, so we ignore the distance check here
//			BlockPos target = mine.getTargetMineLoc();
//			if (target == null) {
//				return false;
//			}
//			
//			target = findEmptySpot(target, true);
//			if (target == null) {
//				return false;
//			}
//			
//			// Check for pathing
//			if (this.getDistanceSq(target) < .2) {
//				return true;
//			}
//			Path currentPath = navigator.getPath();
//			boolean success = navigator.tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0);
//			if (success) {
//				success = Paths.IsComplete(navigator.getPath(), target, 2);
//			}
//			if (currentPath == null) {
//				if (!success) {
//					navigator.setPath(currentPath, 1.0);
//				}
//			} else {
//				navigator.setPath(currentPath, 1.0);
//			}
//			if (success) {
//				return true;
//			} else if (this.getDistanceSq(target) < 1) {
//				// extra case for if the navigator refuses cause we're too close
//				return true;
//			}
//		} else if (task instanceof LogisticsTaskPlaceBlock) {
//			LogisticsTaskPlaceBlock place = (LogisticsTaskPlaceBlock) task;
//			
//			if (place.getWorld() != this.world) {
//				return false;
//			}
//			
//			// Check where the block is
//			// EDIT mines have things go FAR down, so we ignore the distance check here
//			BlockPos target = place.getTargetPlaceLoc();
//			if (target == null) {
//				return false;
//			}
//			
//			target = findEmptySpot(target, true);
//			if (target == null) {
//				return false;
//			}
//			
//			// Check for pathing
//			if (this.getDistanceSq(target) < .2) {
//				return true;
//			}
//			Path currentPath = navigator.getPath();
//			boolean success = navigator.tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0);
//			if (success) {
//				success = Paths.IsComplete(navigator.getPath(), target, 2);
//			}
//			if (currentPath == null) {
//				if (!success) {
//					navigator.setPath(currentPath, 1.0);
//				}
//			} else {
//				navigator.setPath(currentPath, 1.0);
//			}
//			if (success) {
//				return true;
//			} else if (this.getDistanceSq(target) < 1) {
//				// extra case for if the navigator refuses cause we're too close
//				return true;
//			}
//		} else
		if (task instanceof LogisticsTaskPlantItem) {
			LogisticsTaskPlantItem plant = (LogisticsTaskPlantItem) task;
			
			if (plant.getWorld() != this.world) {
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
			if (this.getDistanceSq(target.getX() + .5, target.getY(), target.getZ() + .5) < .2) {
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
			} else if (target.distanceSq(getPosition()) < 1) {
				// extra case for if the navigator refuses cause we're too close
				return true;
			}
		}
		
		return false;
	}
	
	private void dropItems() {
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack heldItem = inventory.getStackInSlot(i);
			if (heldItem.isEmpty()) {
				continue;
			}
			ItemEntity item = new ItemEntity(this.world, posX, posY, posZ, heldItem);
			world.addEntity(item);
		}
		inventory.clear();
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
	}
	
	@Override
	protected void onIdleTick() {
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		if (hasItems()) {
			ItemStack held = ItemStack.EMPTY;
			
			for (int i = 0; i < INV_SIZE; i++) {
				held = inventory.getStackInSlot(i);
				if (!held.isEmpty()) {
					break;
				}
			}
			
			if (!held.isEmpty()) {
				LogisticsNetwork network = this.getLogisticsNetwork();
				if (network != null) {
					@Nullable ILogisticsComponent storage = network.getStorageForItem(world, getPosition(), held);
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
					
					targ = new BlockPos(new Vector3d(
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
					for (int i = 0; i < Math.ceil(this.getHeight()); i++) {
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
				if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.getMoveHelper().setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
				}
				
			}
		}
	}

	@Override
	protected void onTaskTick(ILogisticsTask task) {
		
		// Mining dwarves should place down lights in the mines and refresh those around them
		if (task instanceof LogisticsTaskMineBlock && this.ticksExisted % 5 == 0) {
			if (!this.world.canBlockSeeSky(this.getPosition())) {
				// No light from the 'sky' which means we're underground
				// Refreseh magic lights around. Then see if it's too dark
				BlockState state;
				MutableBlockPos cursor = new MutableBlockPos();
				for (int x = -1; x <= 1; x++)
				for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++) {
					cursor.setPos(x, y, z);
					state = world.getBlockState(cursor);
					if (state != null && state.getBlock() instanceof MagicLight) {
						FairyBlocks.magicLightBright.refresh(world, cursor.toImmutable());
					}
				}
				
				if (this.world.getLightFor(LightType.BLOCK, this.getPosition()) < 8) {
					if (this.world.isAirBlock(this.getPosition().up().up())) {
						world.setBlockState(this.getPosition().up().up(), FairyBlocks.magicLightBright.getDefaultState());
					} else if (this.world.isAirBlock(this.getPosition().up())) {
						world.setBlockState(this.getPosition().up(), FairyBlocks.magicLightBright.getDefaultState());
					}
				}
			}
		}
		
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			switch (sub.getType()) {
			case ATTACK:
				this.faceEntity(sub.getEntity(), 30, 180);
				break;
			case BREAK:
				// this is where we'd play some animation?
				if (this.onGround) {
					BlockPos pos = sub.getPos();
					double d0 = pos.getX() - this.posX;
			        double d2 = pos.getZ() - this.posZ;
					float desiredYaw = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					
					this.rotationYaw = desiredYaw;
					
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
							
							targ = new BlockPos(new Vector3d(
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
							for (int i = 0; i < Math.ceil(this.getHeight()); i++) {
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
						if (!this.getNavigator().tryMoveToXYZ(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
							this.getMoveHelper().setMoveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
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
						if ((movePos != null && this.getDistanceSq(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5) < 1)
							|| (moveEntity != null && this.getDistanceSq(moveEntity) < 1)) {
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
								this.getMoveHelper().setMoveTo(moveEntity.posX, moveEntity.posY, moveEntity.posZ, 1.0f);
							}
						} else {
							movePos = findEmptySpot(movePos, false);
							
							// Is the block we shifted to where we are?
							if (!this.getPosition().equals(movePos) && movePos.distanceSq(getPosition()) > 1) {
								if (!this.getNavigator().tryMoveToXYZ(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f)) {
									this.getMoveHelper().setMoveTo(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f);
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
	protected void registerGoals() {
		// TODO Auto-generated method stub
		// I guess we should wander and check if tehre's a home nearby and if so make it our home and stop wandering.
		// Or if we're revolting... just quit for this test one?
		// Or if we're working, dont use AI
		// Or if we're idle... wander?
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.24D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0D);
		//this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	private ListNBT inventoryToNBT() {
		ListNBT list = new ListNBT();
		
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				list.add(stack.serializeNBT());
			}
		}
		
		return list;
	}
	
	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		
		compound.put(NBT_ITEMS, inventoryToNBT());
	}
	
	private void loadInventoryFromNBT(ListNBT list) {
		inventory.clear();
		
		for (int i = 0; i < list.size(); i++) {
			inventory.setInventorySlotContents(i, ItemStack.read(list.getCompound(i)));
		}
	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		
		loadInventoryFromNBT(compound.getList(NBT_ITEMS, NBT.TAG_COMPOUND));
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return !this.hasItems();
	}
	
	@Override
	protected void collideWithEntity(Entity entityIn) {
		if (this.getCurrentTask() != null && this.getCurrentTask() instanceof LogisticsTaskMineBlock
				&& entityIn instanceof IFeyWorker) {
			ILogisticsTask theirs = ((IFeyWorker) entityIn).getCurrentTask();
			if (theirs != null && theirs instanceof LogisticsTaskMineBlock) {
				return;
			}
		}
		
		super.collideWithEntity(entityIn);
	}

	@Override
	protected String getRandomName() {
		return "Test Fairy " + rand.nextInt();
	}
	
	@Override
	protected void onCombatTick() {
		; // No combat
	}

	@Override
	protected void onCientTick() {
		;
	}

	@Override
	public ResidentType getHomeType() {
		return ResidentType.FAIRY;
	}
	
	@Override
	public String getSpecializationName() {
		return "Test Fairy";
	}

	@Override
	protected String getUnlocPrefix() {
		return "testfairy";
	}
	
	@Override
	protected boolean shouldJoin(BlockPos pos, BlockState state, HomeBlockTileEntity te) {
		return rand.nextBoolean() && rand.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		if (this.navigator.noPath() && ticksExisted % 200 == 0 && rand.nextBoolean()) {
			// Go to a random place
			EntityFeyBase.FeyWander(this, this.getPosition(), Math.min(100, this.wanderDistanceSq));
		}
	}

	@Override
	protected void onRevoltTick() {
		// TODO Auto-generated method stub
		;
	}

	@Override
	public EntityFeyBase switchToSpecialization(FeyStoneMaterial material) {
		return this;
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return null;
	}

	@Override
	protected NostrumFairiesSounds getIdleSound() {
		// TODO Auto-generated method stub
		return null;
	}
}
