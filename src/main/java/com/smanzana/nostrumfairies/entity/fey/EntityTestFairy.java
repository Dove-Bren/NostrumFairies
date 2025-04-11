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
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityTestFairy extends EntityFeyBase implements IItemCarrierFey {
	
	public static final String ID = "test_fairy";

	private static final String NBT_ITEMS = "helditems";
	private static final int INV_SIZE = 5;
	
	private SimpleContainer inventory;
	private @Nullable BlockPos movePos;
	private @Nullable Entity moveEntity;
	
	public EntityTestFairy(EntityType<? extends EntityTestFairy> type, Level world) {
		super(type, world);
		this.workDistanceSq = 24 * 24;
		this.inventory = new SimpleContainer(INV_SIZE);
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
			ItemStack stack = inventory.getItem(i);
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
			if (!inventory.getItem(i).isEmpty()) {
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
			
			if (plant.getWorld() != this.level) {
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
			if (this.distanceToSqr(target.getX() + .5, target.getY(), target.getZ() + .5) < .2) {
				return true;
			}
			Path currentPath = navigation.getPath();
			boolean success = navigation.moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
			if (success) {
				success = Paths.IsComplete(navigation.getPath(), target, 2);
			}
			if (currentPath == null) {
				if (!success) {
					navigation.moveTo(currentPath, 1.0);
				}
			} else {
				navigation.moveTo(currentPath, 1.0);
			}
			if (success) {
				return true;
			} else if (target.distSqr(blockPosition()) < 1) {
				// extra case for if the navigator refuses cause we're too close
				return true;
			}
		}
		
		return false;
	}
	
	private void dropItems() {
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack heldItem = inventory.getItem(i);
			if (heldItem.isEmpty()) {
				continue;
			}
			ItemEntity item = new ItemEntity(this.level, getX(), getY(), getZ(), heldItem);
			level.addFreshEntity(item);
		}
		inventory.clearContent();
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
				held = inventory.getItem(i);
				if (!held.isEmpty()) {
					break;
				}
			}
			
			if (!held.isEmpty()) {
				LogisticsNetwork network = this.getLogisticsNetwork();
				if (network != null) {
					@Nullable ILogisticsComponent storage = network.getStorageForItem(level, blockPosition(), held);
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
		if (this.navigation.isDone()) {
			BlockPos home = this.getHome();
			if (home != null && !this.canReach(this.blockPosition(), false)) {
				
				// Go to a random place around our home
				final BlockPos center = home;
				BlockPos targ = null;
				int attempts = 20;
				final double maxDistSq = Math.min(25, this.wanderDistanceSq);
				do {
					double dist = this.random.nextDouble() * Math.sqrt(maxDistSq);
					float angle = (float) (this.random.nextDouble() * (2 * Math.PI));
					float tilt = (float) (this.random.nextDouble() * (2 * Math.PI)) * .5f;
					
					targ = new BlockPos(new Vec3(
							center.getX() + (Math.cos(angle) * dist),
							center.getY() + (Math.cos(tilt) * dist),
							center.getZ() + (Math.sin(angle) * dist)));
					while (targ.getY() > 0 && level.isEmptyBlock(targ)) {
						targ = targ.below();
					}
					if (targ.getY() < 256) {
						targ = targ.above();
					}
					
					// We've hit a non-air block. Make sure there's space above it
					BlockPos airBlock = null;
					for (int i = 0; i < Math.ceil(this.getBbHeight()); i++) {
						if (airBlock == null) {
							airBlock = targ.above();
						} else {
							airBlock = airBlock.above();
						}
						
						if (!level.isEmptyBlock(airBlock)) {
							targ = null;
							break;
						}
					}
				} while (targ == null && attempts > 0);
				
				if (targ == null) {
					targ = center.above();
				}
				if (!this.getNavigation().moveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
					this.getMoveControl().setWantedPosition(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
				}
				
			}
		}
	}

	@Override
	protected void onTaskTick(ILogisticsTask task) {
		
		// Mining dwarves should place down lights in the mines and refresh those around them
		if (task instanceof LogisticsTaskMineBlock && this.tickCount % 5 == 0) {
			if (!this.level.canSeeSkyFromBelowWater(this.blockPosition())) {
				// No light from the 'sky' which means we're underground
				// Refreseh magic lights around. Then see if it's too dark
				BlockState state;
				BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
				for (int x = -1; x <= 1; x++)
				for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++) {
					cursor.set(x, y, z);
					state = level.getBlockState(cursor);
					if (state != null && state.getBlock() instanceof MagicLight) {
						FairyBlocks.magicLightBright.refresh(level, cursor.immutable());
					}
				}
				
				if (this.level.getBrightness(LightLayer.BLOCK, this.blockPosition()) < 8) {
					if (this.level.isEmptyBlock(this.blockPosition().above().above())) {
						level.setBlockAndUpdate(this.blockPosition().above().above(), FairyBlocks.magicLightBright.defaultBlockState());
					} else if (this.level.isEmptyBlock(this.blockPosition().above())) {
						level.setBlockAndUpdate(this.blockPosition().above(), FairyBlocks.magicLightBright.defaultBlockState());
					}
				}
			}
		}
		
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			switch (sub.getType()) {
			case ATTACK:
				this.lookAt(sub.getEntity(), 30, 180);
				break;
			case BREAK:
				// this is where we'd play some animation?
				if (this.onGround) {
					BlockPos pos = sub.getPos();
					double d0 = pos.getX() - this.getX();
			        double d2 = pos.getZ() - this.getZ();
					float desiredYaw = (float)(Mth.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					
					this.yRot = desiredYaw;
					
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						break;
					}
					this.jumpFromGround();
				}
				break;
			case IDLE:
				if (this.navigation.isDone()) {
					if (movePos == null) {
						final BlockPos center = sub.getPos();
						BlockPos targ = null;
						int attempts = 20;
						final double maxDistSq = 25;
						do {
							double dist = this.random.nextDouble() * Math.sqrt(maxDistSq);
							float angle = (float) (this.random.nextDouble() * (2 * Math.PI));
							float tilt = (float) (this.random.nextDouble() * (2 * Math.PI)) * .5f;
							
							targ = new BlockPos(new Vec3(
									center.getX() + (Math.cos(angle) * dist),
									center.getY() + (Math.cos(tilt) * dist),
									center.getZ() + (Math.sin(angle) * dist)));
							while (targ.getY() > 0 && level.isEmptyBlock(targ)) {
								targ = targ.below();
							}
							if (targ.getY() < 256) {
								targ = targ.above();
							}
							
							// We've hit a non-air block. Make sure there's space above it
							BlockPos airBlock = null;
							for (int i = 0; i < Math.ceil(this.getBbHeight()); i++) {
								if (airBlock == null) {
									airBlock = targ.above();
								} else {
									airBlock = airBlock.above();
								}
								
								if (!level.isEmptyBlock(airBlock)) {
									targ = null;
									break;
								}
							}
						} while (targ == null && attempts > 0);
						
						if (targ == null) {
							targ = center.above();
						}
						if (!this.getNavigation().moveTo(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f)) {
							this.getMoveControl().setWantedPosition(targ.getX() + .5, targ.getY(), targ.getZ() + .5, 1.0f);
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
					if (this.navigation.isDone()) {
						// First time through?
						if ((movePos != null && this.distanceToSqr(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5) < 1)
							|| (moveEntity != null && this.distanceToSqr(moveEntity) < 1)) {
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
							movePos = findEmptySpot(movePos, false);
							
							// Is the block we shifted to where we are?
							if (!this.blockPosition().equals(movePos) && movePos.distSqr(blockPosition()) > 1) {
								if (!this.getNavigation().moveTo(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f)) {
									this.getMoveControl().setWantedPosition(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f);
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

	public static final AttributeSupplier.Builder BuildAttributes() {
		return EntityFeyBase.BuildFeyAttributes()
				.add(Attributes.MOVEMENT_SPEED, .24)
				.add(Attributes.MAX_HEALTH, 2.0)
			;
	}
	
	private ListTag inventoryToNBT() {
		ListTag list = new ListTag();
		
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty()) {
				list.add(stack.serializeNBT());
			}
		}
		
		return list;
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		
		compound.put(NBT_ITEMS, inventoryToNBT());
	}
	
	private void loadInventoryFromNBT(ListTag list) {
		inventory.clearContent();
		
		for (int i = 0; i < list.size(); i++) {
			inventory.setItem(i, ItemStack.of(list.getCompound(i)));
		}
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		loadInventoryFromNBT(compound.getList(NBT_ITEMS, NBT.TAG_COMPOUND));
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return !this.hasItems();
	}
	
	@Override
	protected void doPush(Entity entityIn) {
		if (this.getCurrentTask() != null && this.getCurrentTask() instanceof LogisticsTaskMineBlock
				&& entityIn instanceof IFeyWorker) {
			ILogisticsTask theirs = ((IFeyWorker) entityIn).getCurrentTask();
			if (theirs != null && theirs instanceof LogisticsTaskMineBlock) {
				return;
			}
		}
		
		super.doPush(entityIn);
	}

	@Override
	protected String getRandomName() {
		return "Test Fairy " + random.nextInt();
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
		return random.nextBoolean() && random.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		if (this.navigation.isDone() && tickCount % 200 == 0 && random.nextBoolean()) {
			// Go to a random place
			EntityFeyBase.FeyWander(this, this.blockPosition(), Math.min(100, this.wanderDistanceSq));
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
