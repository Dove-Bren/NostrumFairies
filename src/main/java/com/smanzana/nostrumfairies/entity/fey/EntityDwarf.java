package com.smanzana.nostrumfairies.entity.fey;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskBuildBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskMineBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlaceBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.serializers.ArmPoseDwarf;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrumfairies.serializers.ItemArraySerializer;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.tiles.MiningBlockTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemDeepStacks;
import com.smanzana.nostrumfairies.utils.Paths;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class EntityDwarf extends EntityFeyBase implements IItemCarrierFey {
	
	public static final String ID = "dwarf";

	protected static final EntityDataAccessor<ArmPoseDwarf> POSE  = SynchedEntityData.<ArmPoseDwarf>defineId(EntityDwarf.class, ArmPoseDwarf.instance());
	protected static final EntityDataAccessor<ItemStack[]> ITEMS = SynchedEntityData.<ItemStack[]>defineId(EntityDwarf.class, ItemArraySerializer.instance());
	
	private static final String NBT_ITEMS = "helditems";
	private static final int INV_SIZE = 5;
	
	public EntityDwarf(EntityType<? extends EntityDwarf> type, Level world) {
		super(type, world);

		this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
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
		return new Lore("Dwarves are a quiet, brooding fey who seem to have an affinity for mining.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore("Dwarves are a quiet, brooding fey who seem to have an affinity for mining.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}
	
	protected ItemStack[] getCarriedItemsRaw() {
		return entityData.get(ITEMS);
	}

	@Override
	public NonNullList<ItemStack> getCarriedItems() {
		return NonNullList.of(null, getCarriedItemsRaw());
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		return Inventories.canFit(getCarriedItemsRaw(), stack);
	}
	
	@Override
	public boolean canAccept(ItemDeepStack stack) {
		return ItemDeepStacks.canFitAll(getCarriedItemsRaw(), Lists.newArrayList(stack));
	}
	
	protected void updateItems(ItemStack items[]) {
		entityData.set(ITEMS, items);
		//dataManager.setDirty(ITEMS);
	}

	@Override
	public void addItem(ItemStack stack) {
		ItemStack[] oldItems = entityData.get(ITEMS);
		ItemStack[] items = Arrays.copyOf(oldItems, oldItems.length);
		Inventories.addItem(items, stack);
		updateItems(items);
	}
	
	@Override
	public void removeItem(ItemStack stack) {
		ItemStack[] oldItems = entityData.get(ITEMS);
		ItemStack[] items = Arrays.copyOf(oldItems, oldItems.length);
		Inventories.remove(items, stack);
		updateItems(items);
	}
	
	protected boolean hasItems() {
		for (int i = 0; i < INV_SIZE; i++) {
			if (!getCarriedItemsRaw()[i].isEmpty()) {
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
		
		switch (to) {
		case IDLE:
			setActivitySummary("status.dwarf.relax");
			break;
		case REVOLTING:
			setActivitySummary("status.dwarf.revolt");
			break;
		case WANDERING:
			setActivitySummary("status.dwarf.wander");
			break;
		case WORKING:
			; // Set by task
		}
		
		return true;
	}

	@Override
	public ResidentType getHomeType() {
		return ResidentType.DWARF;
	}
	
	@Nullable
	protected BlockPos findEmptySpot(BlockPos targetPos, boolean allOrNothing, boolean repair) {
		
		// repair tasks are going to add the block, so don't stand there! Stand above -- up to 2 blocks above
		// We also change the order we evaluate spots based on the same thing. we prefer above for repair, and prefer at or below
		// for non-repair
		
		if (repair || ((!level.isEmptyBlock(targetPos) && level.getBlockState(targetPos).getMaterial().blocksMotion() && level.getBlockState(targetPos).getMaterial() != Material.LAVA)
						|| !isSolid(level, targetPos.below(), Direction.UP))) {
			// could get enum facing from diffs in dir to start at the side closest!
			BlockPos[] initOffsets = {targetPos.north(), targetPos.east(), targetPos.south(), targetPos.west()};
			BlockPos[] offsets;
			if (repair) {
				// Prefer up, double up, same, then down
				offsets = new BlockPos[] {
					initOffsets[0].above(), initOffsets[1].above(), initOffsets[2].above(), initOffsets[3].above(),
					initOffsets[0].above().above(), initOffsets[1].above().above(), initOffsets[2].above().above(), initOffsets[3].above().above(),
					initOffsets[0], initOffsets[1], initOffsets[2], initOffsets[3],
					targetPos.below(), initOffsets[0].below(), initOffsets[1].below(), initOffsets[2].below(), initOffsets[3].below(),
				};
			} else {
				// Prefer same, below, above
				offsets = new BlockPos[] {
					initOffsets[0], initOffsets[1], initOffsets[2], initOffsets[3],
					initOffsets[0].below(), initOffsets[1].below(), initOffsets[2].below(), initOffsets[3].below(),
					targetPos.below(), targetPos.above(),
					initOffsets[0].above(), initOffsets[1].above(), initOffsets[2].above(), initOffsets[3].above(),
				};
			}
		
			// Check each candidate to see if we can stand there
			for (BlockPos pos : offsets) {
				if ((level.isEmptyBlock(pos) || level.getBlockState(pos).getMaterial() == Material.LAVA || !level.getBlockState(pos).getMaterial().blocksMotion())
						&& level.getBlockState(pos.below()).getMaterial().blocksMotion()) {
					targetPos = pos;
					break;
				}
			}
		}
		
//		
//		if ((repair) || (!world.isAirBlock(targetPos) || !world.isSideSolid(targetPos.down(), Direction.UP))) {
//			do {
//				if (world.isAirBlock(targetPos.north())) {
//					if (world.isSideSolid(targetPos.north().down(), Direction.UP)) {
//						targetPos = targetPos.north();
//						break;
//					} else if (world.isSideSolid(targetPos.north().down().down(), Direction.UP)) {
//						targetPos = targetPos.north().down();
//						break;
//					} else if (world.isSideSolid(targetPos.north(), Direction.UP)) {
//						targetPos = targetPos.north().up();
//						break;
//					} else if (repair && world.isSideSolid(targetPos.north().up(), Direction.UP)) {
//						targetPos = targetPos.north().up().up();
//						break;
//					}
//				}
//				if (world.isAirBlock(targetPos.south())) {
//					if (world.isSideSolid(targetPos.south().down(), Direction.UP)) {
//						targetPos = targetPos.south();
//						break;
//					} else if (world.isSideSolid(targetPos.south().down().down(), Direction.UP)) {
//						targetPos = targetPos.south().down();
//						break;
//					} else if (world.isSideSolid(targetPos.south(), Direction.UP)) {
//						targetPos = targetPos.south().up();
//						break;
//					} else if (repair && world.isSideSolid(targetPos.south().up(), Direction.UP)) {
//						targetPos = targetPos.south().up().up();
//						break;
//					}
//				}
//				if (world.isAirBlock(targetPos.east())) {
//					if (world.isSideSolid(targetPos.east().down(), Direction.UP)) {
//						targetPos = targetPos.east();
//						break;
//					} else if (world.isSideSolid(targetPos.east().down().down(), Direction.UP)) {
//						targetPos = targetPos.east().down();
//						break;
//					} else if (world.isSideSolid(targetPos.east(), Direction.UP)) {
//						targetPos = targetPos.east().up();
//						break;
//					} else if (repair && world.isSideSolid(targetPos.east().up(), Direction.UP)) {
//						targetPos = targetPos.east().up().up();
//						break;
//					}
//				}
//				if (world.isAirBlock(targetPos.west())) {
//					if (world.isSideSolid(targetPos.west().down(), Direction.UP)) {
//						targetPos = targetPos.west();
//						break;
//					} else if (world.isSideSolid(targetPos.west().down().down(), Direction.UP)) {
//						targetPos = targetPos.west().down();
//						break;
//					} else if (world.isSideSolid(targetPos.west(), Direction.UP)) {
//						targetPos = targetPos.west().up();
//						break;
//					} else if (repair && world.isSideSolid(targetPos.west().up(), Direction.UP)) {
//						targetPos = targetPos.west().up().up();
//						break;
//					}
//				}
//				if (!repair && world.isAirBlock(targetPos.up()) && world.isSideSolid(targetPos, Direction.UP)) {
//					targetPos = targetPos.up();
//					break;
//				}
//				if (world.isAirBlock(targetPos.down()) && world.isSideSolid(targetPos.down().down(), Direction.UP)) {
//					targetPos = targetPos.down();
//					break;
//				}
//			} while (false);
//		}
		
		if (allOrNothing) {
			if (!level.isEmptyBlock(targetPos)
					&& level.getBlockState(targetPos).getMaterial().blocksMotion()
					&& level.getBlockState(targetPos).getMaterial() != Material.LAVA) {
				targetPos = null;
			}
		}
		
		return targetPos;
	}

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskMineBlock) {
			LogisticsTaskMineBlock mine = (LogisticsTaskMineBlock) task;
			
			if (mine.getWorld() != this.level) {
				return false;
			}
			
			// Dwarves only perform tasks from their mine
//			if (this.getHome() == null || mine.getSourceComponent() == null ||
//					!this.getHome().equals(mine.getSourceComponent().getPosition())) {
//				return false;
//			}
			
			// Check where the block is
			// EDIT mines have things go FAR down, so we ignore the distance check here
			BlockPos target = mine.getTargetMineLoc();
			if (target == null) {
				//System.out.println("\t\t Exit C: " + (System.currentTimeMillis() - start));
				return false;
			}
			
			if (this.getCurrentTask() != null
					&& this.getCurrentTask() instanceof LogisticsTaskMineBlock) {
				
				// Try to stay around the other tasks
				if (((LogisticsTaskMineBlock)this.getCurrentTask()).getTargetMineLoc().distSqr(mine.getTargetMineLoc()) > 25) {
					return false;
				}
				
				// If we already have a mining task, we ask the mine to see if we'll be able to get to this task
				// with what we already have.
				// Otherwise we look for an empty spot and see if we can path.
				if (mine.getSourceComponent() != null) {
					BlockEntity te = level.getBlockEntity(mine.getSourceComponent().getPosition());
					if (te != null && te instanceof MiningBlockTileEntity) {
						return ((MiningBlockTileEntity) te).taskAccessibleWithTasks(mine, this);
					}
				}
			}
			
			// If this is a mine task that the mine set up a prereq, trust that that's been checked and
			// that pathfinding will work.
			if (mine.hasPrereqs()) {
				return true;
			} else {
				// Attempt to pathfind
				
				// Find a better block to stand, if we weren't told explicitely to stand there
				if (target == mine.getTargetBlock()) {
					target = findEmptySpot(target, true, false);
					if (target == null) {
						return false;
					}
				}
				
				// Check for pathing
				if (this.getDistanceSq(target) < .2) {
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
				if (success || this.getDistanceSq(target) < 1) {
					// extra case for if the navigator refuses cause we're too close
					return true;
				}
			}
		} else if (task instanceof LogisticsTaskPlaceBlock
				&& !(task instanceof LogisticsTaskPlantItem)
				&& !(task instanceof LogisticsTaskBuildBlock)) {
			LogisticsTaskPlaceBlock place = (LogisticsTaskPlaceBlock) task;
			
			if (place.getWorld() != this.level) {
				return false;
			}
			
//			// Dwarves only perform tasks from their mine
//			if (this.getHome() == null || place.getSourceComponent() == null ||
//					!this.getHome().equals(place.getSourceComponent().getPosition())) {
//				return false;
//			}
			
			// Check where the block is
			// EDIT mines have things go FAR down, so we ignore the distance check here
			BlockPos target = place.getTargetPlaceLoc();
			if (target == null) {
				return false;
			}
			
			// Find a better block to stand, if we weren't told explicitely to stand there
			if (target == place.getTargetBlock()) {
				target = findEmptySpot(target, true, true);
				if (target == null) {
					return false;
				}
			}
			
			// Check for pathing
			if (this.getDistanceSq(target) < .2) {
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
			} else if (this.getDistanceSq(target) < 1) {
				// extra case for if the navigator refuses cause we're too close
				return true;
			}
		}
		
		return false;
	}
	
	private void dropItems() {
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack heldItem = entityData.get(ITEMS)[i];
			if (heldItem.isEmpty()) {
				continue;
			}
			ItemEntity item = new ItemEntity(this.level, getX(), getY(), getZ(), heldItem);
			level.addFreshEntity(item);
		}
		updateItems(new ItemStack[INV_SIZE]);
	}

	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		return true;
	}

	@Override
	protected void onTaskChange(ILogisticsTask oldTask, ILogisticsTask newTask) {
		if (newTask == null) {
			this.setPose(ArmPoseDwarf.IDLE);
		} else {
		if (newTask instanceof LogisticsTaskMineBlock) {
				setActivitySummary("status.dwarf.work.mine");
			} else if (newTask instanceof LogisticsTaskBuildBlock) {
				setActivitySummary("status.dwarf.work.build");
			} else if (newTask instanceof LogisticsTaskPlaceBlock) {
				setActivitySummary("status.dwarf.work.repair");
			} else if (newTask instanceof LogisticsTaskWorkBlock) {
				setActivitySummary("status.dwarf.work.craft");
			}  else if (newTask instanceof LogisticsTaskDepositItem) {
				setActivitySummary("status.generic.return");
			} else {
				setActivitySummary("status.dwarf.work.generic");
			}
		}
	}
	
	@Override
	protected void onIdleTick() {
		this.setPose(ArmPoseDwarf.IDLE);
		// We could play some idle animation or something
		// For now, the only thing we care about is if we're idle but have an item. If so, make
		// a quick task to go and deposit it
		if (hasItems()) {
			ItemStack held = ItemStack.EMPTY;
			
			for (int i = 0; i < INV_SIZE; i++) {
				held = entityData.get(ITEMS)[i];
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
			if (home != null && (this.getDistanceSq(home) > 100 || this.tickCount % (20 * 10) == 0 && random.nextBoolean())) {
				
				// Go to a random place around our home
				final BlockPos center = home;
				BlockPos targ = null;
				int attempts = 20;
				final double maxDistSq = Math.min(100, this.wanderDistanceSq);
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
				for (int x = -3; x <= 3; x++)
				for (int y = -1; y <= 1; y++)
				for (int z = -3; z <= 3; z++) {
					cursor.set(getX() + x, getY() + y, getZ() + z);
					state = level.getBlockState(cursor);
					if (state != null && state.getBlock() instanceof MagicLight) {
						FairyBlocks.magicLightBright.refresh(level, cursor.immutable());
					}
				}
				
				if (this.level.getBrightness(LightLayer.BLOCK, this.blockPosition()) < 8) {
					if (!this.level.isEmptyBlock(this.blockPosition().above().above().above())
							&& this.level.isEmptyBlock(this.blockPosition().above().above())) {
						level.setBlockAndUpdate(this.blockPosition().above().above(), FairyBlocks.magicLightBright.defaultBlockState());
					} else if (!this.level.isEmptyBlock(this.blockPosition().above().above())
							&& this.level.isEmptyBlock(this.blockPosition().above())) {
						level.setBlockAndUpdate(this.blockPosition().above(), FairyBlocks.magicLightBright.defaultBlockState());
					}
				}
			}
		}
		
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			switch (sub.getType()) {
			case ATTACK:
				this.setPose(ArmPoseDwarf.ATTACKING);
				this.lookAt(sub.getEntity(), 30, 180);
				break;
			case BREAK:
				if (this.swinging) {
					;
				} else {
					this.setPose(ArmPoseDwarf.MINING);
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						this.setPose(ArmPoseDwarf.IDLE);
						break;
					}
					this.swing(getUsedItemHand());
					BlockPos pos = sub.getPos();
					double d0 = pos.getX() - this.getX();
			        double d2 = pos.getZ() - this.getZ();
					float desiredYaw = (float)(Mth.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
					
					this.setYRot(desiredYaw);
				}
//				// this is where we'd play some animation?
//				if (this.onGround) {
//					BlockPos pos = sub.getPos();
//					double d0 = pos.getX() - this.getPosX();
//			        double d2 = pos.getZ() - this.getPosZ();
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
				this.setPose(ArmPoseDwarf.IDLE);
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
					this.setPose(ArmPoseDwarf.IDLE);
					this.feyMoveToTask(task, (task instanceof LogisticsTaskPlaceBlock));
				}
				break;
			}
		}
	}

	@Override
	protected void registerGoals() {
		int priority = 1;
		this.goalSelector.addGoal(priority++, new FloatGoal(this) {
			@Override
			public boolean canUse() {
				// Ignore water when working
				if (EntityDwarf.this.getCurrentTask() != null) {
					return false;
				}
				return super.canUse();
			}
		});
		this.goalSelector.addGoal(priority++, new MeleeAttackGoal(this, 1.0, true)); // also gated on target, like 'combat tick' on fey mechs
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setAlertOthers(EntityDwarf.class));
		
		// Could hunt mobs
//		this.targetSelector.addGoal(priority++, new NearestAttackableTargetGoal<MonsterEntity>(this, MonsterEntity.class, 10, true, false, (mob) -> {
//			return (mob instanceof IEntityTameable ? !((IEntityTameable) mob).isTamed()
//					: true);
//		}));
		
		// TODO Auto-generated method stub
		// I guess we should wander and check if tehre's a home nearby and if so make it our home and stop wandering.
		// Or if we're revolting... just quit for this test one?
		// Or if we're working, dont use AI
		// Or if we're idle... wander?
	}

	public static final AttributeSupplier.Builder BuildAttributes() {
		return EntityFeyBase.BuildFeyAttributes()
				.add(Attributes.MOVEMENT_SPEED, .2)
				.add(Attributes.MAX_HEALTH, 24)
				.add(Attributes.ATTACK_DAMAGE, 3.0)
				.add(Attributes.ARMOR, 2.0)
			;
	}
	
	private ListTag inventoryToNBT() {
		ListTag list = new ListTag();
		
		ItemStack items[] = entityData.get(ITEMS);
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack stack = items[i];
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
		ItemStack items[] = new ItemStack[INV_SIZE];
		
		for (int i = 0; i < INV_SIZE; i++) {
			if (i < list.size()) {
				items[i] = ItemStack.of(list.getCompound(i));
			} else {
				items[i] = ItemStack.EMPTY;
			}
		}
		
		updateItems(items);
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		
		loadInventoryFromNBT(compound.getList(NBT_ITEMS, Tag.TAG_COMPOUND));
	}

	@Override
	protected boolean canMergeMoreJobs() {
		return !this.hasItems();
	}
	
	@Override
	protected void doPush(Entity entityIn) {
		if (entityIn instanceof IFeyWorker) {
			IFeyWorker other = (IFeyWorker) entityIn;
			if ((this.getCurrentTask() != null)
				|| (other.getCurrentTask() != null)) {
					return;
			}
		}
		
		super.doPush(entityIn);
	}
	
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata, @Nullable CompoundTag tag) {
		livingdata = super.finalizeSpawn(world, difficulty, reason, livingdata, tag);
		
		// Dwarves are 40:60 lefthanded
		if (this.random.nextFloat() < .4f) {
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
		return names[this.random.nextInt(names.length)];
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
		return names[this.random.nextInt(names.length)];
	}

	@Override
	protected String getRandomName() {
		return getRandomFirstName() + " " + getRandomLastName();
	}
	
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(POSE, ArmPoseDwarf.IDLE);
		ItemStack[] arr = new ItemStack[INV_SIZE];
		Arrays.fill(arr, ItemStack.EMPTY);
		entityData.define(ITEMS, arr);
	}
	
	public ArmPoseDwarf getDwarfPose() {
		return entityData.get(POSE);
	}
	
	public void setPose(ArmPoseDwarf pose) {
		this.entityData.set(POSE, pose);
	}

	@Override
	protected void onCombatTick() {
		this.setPose(ArmPoseDwarf.ATTACKING);
	}
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 18;
	}
	
	@Override
	protected void onCientTick() {
		;
	}
	
	protected void playWorkSound() {
		NostrumFairiesSounds.PICKAXE_HIT.play(NostrumFairies.proxy.getPlayer(), level, getX(), getY(), getZ());
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (level.isClientSide && swinging) {
			if (this.getDwarfPose() == ArmPoseDwarf.MINING) {
				// 20% into animation is the hit
				if (this.swingTime == Math.floor(this.getArmSwingAnimationEnd() * .2)) {
					playWorkSound();
				}
			}
		}
	}
	
	@Override
	public boolean isPushedByFluid() {
		return false;
	}
	
	protected float getWaterSlowDown() {
		//return super.getWaterSlowDown();
		return .7f;
	}
	
	@Override
	public boolean isInWater() {
		return false;//super.isInWater();
	}
	
	@Override
	public boolean isInLava() {
		return false;
	}
	
	@Override
	public boolean canBreatheUnderwater() {
		return false;
	}
	
	@Override
	protected int decreaseAirSupply(int air) {
		if (this.tickCount % 3 == 0) {
			return super.decreaseAirSupply(air);
		}
		return air;
	}

	@Override
	public String getSpecializationName() {
		return "Mining Dwarf";
	}
	
	@Override
	protected String getUnlocPrefix() {
		return "dwarf";
	}

	@Override
	protected boolean shouldJoin(BlockPos pos, BlockState state, HomeBlockTileEntity te) {
		return random.nextBoolean() && random.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		if (this.navigation.isDone() && tickCount % 100 == 0 && random.nextBoolean()) {
			if (!EntityFeyBase.FeyLazyFollowNearby(this, EntityFeyBase.DOMESTIC_FEY_AND_PLAYER_FILTER, 15, 3, 6)) {
				// Go to a random place
				EntityFeyBase.FeyWander(this, this.blockPosition(), Math.min(10, Math.sqrt(this.wanderDistanceSq)));
			}
		}

		if (this.getTarget() == null) {
			this.setPose(ArmPoseDwarf.IDLE);
		}
	}

	@Override
	protected void onRevoltTick() {
		// TODO Auto-generated method stub
		;
	}
	
	@Override
	protected float getGrowthForTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskMineBlock) {
			return 0.8f;
		}
		if (task instanceof LogisticsTaskBuildBlock) {
			return .5f;
		}
		if (task instanceof LogisticsTaskWorkBlock) { // Crafting
			return 1.2f;
		}
		if (task instanceof LogisticsTaskPlaceBlock) {
			return 0.65f;
		}
		
		return 0f;
	}
	
	@Override
	protected void teleportFromStuck() {
		if (this.getCurrentTask() != null && this.getCurrentTask() instanceof LogisticsTaskMineBlock) {
			BlockPos target = findEmptySpot(((LogisticsTaskMineBlock) this.getCurrentTask()).getTargetMineLoc(), false);
			this.randomTeleport(target.getX() + .5, target.getY() + .05, target.getZ() + .5, false);
		} else {
			super.teleportFromStuck();
		}
		
	}

	@Override
	public EntityFeyBase switchToSpecialization(FeyStoneMaterial material) {
		if (level.isClientSide) {
			return this;
		}
		
		EntityFeyBase replacement = null;
		if (material != this.getCurrentSpecialization()) {
			if (material == FeyStoneMaterial.GARNET) {
				// Crafting
				replacement = new EntityDwarfCrafter(FairyEntities.DwarfCrafter, level);
			} else if (material == FeyStoneMaterial.EMERALD) {
				// Builder
				replacement = new EntityDwarfBuilder(FairyEntities.DwarfBuilder, level);
			} else {
				replacement = new EntityDwarf(FairyEntities.Dwarf, level);
			}
		}
		
		if (replacement != null) {
			// Kill this entity and add the other one
			replacement.copyFrom(this);
			((ServerLevel) level).removeEntity(this);
			level.addFreshEntity(replacement);
		}
		
		return replacement == null ? this : replacement;
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return NostrumFairiesSounds.DWARF_HURT.getEvent();
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return NostrumFairiesSounds.DWARF_DIE.getEvent();
	}
	
	@Override
	protected @Nullable NostrumFairiesSounds getIdleSound() {
		return null;
	}
}
