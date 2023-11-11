package com.smanzana.nostrumfairies.entity.fey;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.entity.navigation.PathFinderPublic;
import com.smanzana.nostrumfairies.entity.navigation.PathNavigatorLogistics;
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
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class EntityDwarf extends EntityFeyBase implements IItemCarrierFey {

	protected static final DataParameter<ArmPoseDwarf> POSE  = EntityDataManager.<ArmPoseDwarf>createKey(EntityDwarf.class, ArmPoseDwarf.instance());
	protected static final DataParameter<ItemStack[]> ITEMS = EntityDataManager.<ItemStack[]>createKey(EntityDwarf.class, ItemArraySerializer.instance());
	
	private static final String NBT_ITEMS = "helditems";
	private static final int INV_SIZE = 5;
	
	protected @Nullable BlockPos movePos;
	protected @Nullable Entity moveEntity;
	
	public EntityDwarf(EntityType<? extends EntityDwarf> type, World world) {
		super(type, world);
		this.workDistanceSq = 24 * 24;
		
		this.navigator = new PathNavigatorLogistics(this, world) {
			@Override
			protected PathFinder getPathFinder() {
				this.nodeProcessor = new WalkNodeProcessor(){
					// Naturally, copied from vanilla since there isn't a good way to override
					@Override
					public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z) {
						PathNodeType pathnodetype = this.getPathNodeTypeRaw(blockaccessIn, x, y, z);
	
						if (pathnodetype == PathNodeType.OPEN && y >= 1)
						{
							Block block = blockaccessIn.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
							PathNodeType pathnodetype1 = this.getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);
							pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
	
							if (pathnodetype1 == PathNodeType.DAMAGE_FIRE || block == Blocks.MAGMA)
							{
								pathnodetype = PathNodeType.DAMAGE_FIRE;
							}
	
							if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS)
							{
								pathnodetype = PathNodeType.DAMAGE_CACTUS;
							}
						}
	
						BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();
	
						if (pathnodetype == PathNodeType.WALKABLE)
						{
							for (int j = -1; j <= 1; ++j)
							{
								for (int i = -1; i <= 1; ++i)
								{
									if (j != 0 || i != 0)
									{
										Block block1 = blockaccessIn.getBlockState(blockpos$pooledmutableblockpos.setPos(j + x, y, i + z)).getBlock();
	
										if (block1 == Blocks.CACTUS)
										{
											pathnodetype = PathNodeType.DANGER_CACTUS;
										}
										else if (block1 == Blocks.FIRE)
										{
											pathnodetype = PathNodeType.DANGER_FIRE;
										}
									}
								}
							}
						}
	
						blockpos$pooledmutableblockpos.release();
						return pathnodetype;
					}
					
					@Override
					protected PathNodeType getPathNodeTypeRaw(IBlockAccess access, int x, int y, int z) {
						BlockPos blockpos = new BlockPos(x, y, z);
						BlockState iblockstate = access.getBlockState(blockpos);
						Block block = iblockstate.getBlock();
						Material material = iblockstate.getMaterial();
						return (material == Material.AIR || material == Material.LAVA) ? PathNodeType.OPEN : (block != Blocks.TRAPDOOR && block != Blocks.IRON_TRAPDOOR && block != Blocks.WATERLILY ? (block == Blocks.FIRE ? PathNodeType.DAMAGE_FIRE : (block == Blocks.CACTUS ? PathNodeType.DAMAGE_CACTUS : (block instanceof BlockDoor && material == Material.WOOD && !((Boolean)iblockstate.get(BlockDoor.OPEN)).booleanValue() ? PathNodeType.DOOR_WOOD_CLOSED : (block instanceof BlockDoor && material == Material.IRON && !((Boolean)iblockstate.get(BlockDoor.OPEN)).booleanValue() ? PathNodeType.DOOR_IRON_CLOSED : (block instanceof BlockDoor && ((Boolean)iblockstate.get(BlockDoor.OPEN)).booleanValue() ? PathNodeType.DOOR_OPEN : (block instanceof BlockRailBase ? PathNodeType.RAIL : (!(block instanceof BlockFence) && !(block instanceof BlockWall) && (!(block instanceof BlockFenceGate) || ((Boolean)iblockstate.get(BlockFenceGate.OPEN)).booleanValue()) ? (material == Material.WATER ? PathNodeType.WATER : (material == Material.LAVA ? PathNodeType.LAVA : (block.isPassable(access, blockpos) ? PathNodeType.OPEN : PathNodeType.BLOCKED))) : PathNodeType.FENCE))))))) : PathNodeType.TRAPDOOR);
					}
				};
				this.nodeProcessor.setCanEnterDoors(true);
				this.nodeProcessor.setCanSwim(true);
				this.pathFinder = new PathFinderPublic(this.nodeProcessor);
				return new PathFinder(this.nodeProcessor);
			}
		};
		
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
	
	protected ItemStack[] getCarriedItemsRaw() {
		return dataManager.get(ITEMS);
	}

	@Override
	public NonNullList<ItemStack> getCarriedItems() {
		return NonNullList.from(null, getCarriedItemsRaw());
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
		dataManager.set(ITEMS, items);
		//dataManager.setDirty(ITEMS);
	}

	@Override
	public void addItem(ItemStack stack) {
		ItemStack[] items = dataManager.get(ITEMS);
		Inventories.addItem(items, stack);
		updateItems(items);
	}
	
	@Override
	public void removeItem(ItemStack stack) {
		ItemStack[] items = dataManager.get(ITEMS);
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
		
		if (repair || ((!world.isAirBlock(targetPos) && world.getBlockState(targetPos).getMaterial().blocksMovement() && world.getBlockState(targetPos).getMaterial() != Material.LAVA)
						|| !isSolid(world, targetPos.down(), Direction.UP))) {
			// could get enum facing from diffs in dir to start at the side closest!
			BlockPos[] initOffsets = {targetPos.north(), targetPos.east(), targetPos.south(), targetPos.west()};
			BlockPos[] offsets;
			if (repair) {
				// Prefer up, double up, same, then down
				offsets = new BlockPos[] {
					initOffsets[0].up(), initOffsets[1].up(), initOffsets[2].up(), initOffsets[3].up(),
					initOffsets[0].up().up(), initOffsets[1].up().up(), initOffsets[2].up().up(), initOffsets[3].up().up(),
					initOffsets[0], initOffsets[1], initOffsets[2], initOffsets[3],
					targetPos.down(), initOffsets[0].down(), initOffsets[1].down(), initOffsets[2].down(), initOffsets[3].down(),
				};
			} else {
				// Prefer same, below, above
				offsets = new BlockPos[] {
					initOffsets[0], initOffsets[1], initOffsets[2], initOffsets[3],
					initOffsets[0].down(), initOffsets[1].down(), initOffsets[2].down(), initOffsets[3].down(),
					targetPos.down(), targetPos.up(),
					initOffsets[0].up(), initOffsets[1].up(), initOffsets[2].up(), initOffsets[3].up(),
				};
			}
		
			// Check each candidate to see if we can stand there
			for (BlockPos pos : offsets) {
				if ((world.isAirBlock(pos) || world.getBlockState(pos).getMaterial() == Material.LAVA || !world.getBlockState(pos).getMaterial().blocksMovement())
						&& world.getBlockState(pos.down()).getMaterial().blocksMovement()) {
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
			if (!world.isAirBlock(targetPos)
					&& world.getBlockState(targetPos).getMaterial().blocksMovement()
					&& world.getBlockState(targetPos).getMaterial() != Material.LAVA) {
				targetPos = null;
			}
		}
		
		return targetPos;
	}

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskMineBlock) {
			LogisticsTaskMineBlock mine = (LogisticsTaskMineBlock) task;
			
			if (mine.getWorld() != this.world) {
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
				if (((LogisticsTaskMineBlock)this.getCurrentTask()).getTargetMineLoc().distanceSq(mine.getTargetMineLoc()) > 25) {
					return false;
				}
				
				// If we already have a mining task, we ask the mine to see if we'll be able to get to this task
				// with what we already have.
				// Otherwise we look for an empty spot and see if we can path.
				if (mine.getSourceComponent() != null) {
					TileEntity te = world.getTileEntity(mine.getSourceComponent().getPosition());
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
					return true;
				}
			}
		} else if (task instanceof LogisticsTaskPlaceBlock
				&& !(task instanceof LogisticsTaskPlantItem)
				&& !(task instanceof LogisticsTaskBuildBlock)) {
			LogisticsTaskPlaceBlock place = (LogisticsTaskPlaceBlock) task;
			
			if (place.getWorld() != this.world) {
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
			ItemStack heldItem = dataManager.get(ITEMS)[i];
			if (heldItem.isEmpty()) {
				continue;
			}
			ItemEntity item = new ItemEntity(this.world, posX, posY, posZ, heldItem);
			world.addEntity(item);
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
				held = dataManager.get(ITEMS)[i];
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
			if (home != null && (this.getDistanceSq(home) > 100 || this.ticksExisted % (20 * 10) == 0 && rand.nextBoolean())) {
				
				// Go to a random place around our home
				final BlockPos center = home;
				BlockPos targ = null;
				int attempts = 20;
				final double maxDistSq = Math.min(100, this.wanderDistanceSq);
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
				for (int x = -3; x <= 3; x++)
				for (int y = -1; y <= 1; y++)
				for (int z = -3; z <= 3; z++) {
					cursor.setPos(posX + x, posY + y, posZ + z);
					state = world.getBlockState(cursor);
					if (state != null && state.getBlock() instanceof MagicLight) {
						FairyBlocks.magicLightBright.refresh(world, cursor.toImmutable());
					}
				}
				
				if (this.world.getLightFor(LightType.BLOCK, this.getPosition()) < 8) {
					if (!this.world.isAirBlock(this.getPosition().up().up().up())
							&& this.world.isAirBlock(this.getPosition().up().up())) {
						world.setBlockState(this.getPosition().up().up(), FairyBlocks.magicLightBright.getDefaultState());
					} else if (!this.world.isAirBlock(this.getPosition().up().up())
							&& this.world.isAirBlock(this.getPosition().up())) {
						world.setBlockState(this.getPosition().up(), FairyBlocks.magicLightBright.getDefaultState());
					}
				}
			}
		}
		
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			switch (sub.getType()) {
			case ATTACK:
				this.setPose(ArmPoseDwarf.ATTACKING);
				this.faceEntity(sub.getEntity(), 30, 180);
				break;
			case BREAK:
				if (this.isSwingInProgress) {
					;
				} else {
					this.setPose(ArmPoseDwarf.MINING);
					task.markSubtaskComplete();
					if (task.getActiveSubtask() != sub) {
						this.setPose(ArmPoseDwarf.IDLE);
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
				this.setPose(ArmPoseDwarf.IDLE);
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
					this.setPose(ArmPoseDwarf.IDLE);
					if (this.navigator.noPath()) {
						// First time through?
						if ((movePos != null && this.getDistanceSqToCenter(movePos) < 1)
							|| (moveEntity != null && this.getDistance(moveEntity) < 1)) {
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
							movePos = findEmptySpot(movePos, false, (task instanceof LogisticsTaskPlaceBlock));
							
							// Is the block we shifted to where we are?
							if (!this.getPosition().equals(movePos) && this.getDistanceSqToCenter(movePos) > 1) {
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
		int priority = 1;
		this.goalSelector.addGoal(priority++, new SwimGoal(this) {
			@Override
			public boolean shouldExecute() {
				// Ignore water when working
				if (EntityDwarf.this.getCurrentTask() != null) {
					return false;
				}
				return super.shouldExecute();
			}
		});
		this.goalSelector.addGoal(priority++, new MeleeAttackGoal(this, 1.0, true)); // also gated on target, like 'combat tick' on fey mechs
		
		priority = 1;
		this.targetSelector.addGoal(priority++, new HurtByTargetGoal(this).setCallsForHelp(EntityDwarf.class));
		
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

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0D);
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	private ListNBT inventoryToNBT() {
		ListNBT list = new ListNBT();
		
		ItemStack items[] = dataManager.get(ITEMS);
		for (int i = 0; i < INV_SIZE; i++) {
			ItemStack stack = items[i];
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
		ItemStack items[] = new ItemStack[INV_SIZE];
		
		for (int i = 0; i < INV_SIZE; i++) {
			if (i < list.size()) {
				items[i] = ItemStack.read(list.getCompound(i));
			} else {
				items[i] = ItemStack.EMPTY;
			}
		}
		
		updateItems(items);
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
	public ILivingEntityData onInitialSpawn(IWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData livingdata, @Nullable CompoundNBT tag) {
		livingdata = super.onInitialSpawn(world, difficulty, reason, livingdata, tag);
		
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
	protected void registerData() {
		super.registerData();
		dataManager.register(POSE, ArmPoseDwarf.IDLE);
		ItemStack[] arr = new ItemStack[INV_SIZE];
		Arrays.fill(arr, ItemStack.EMPTY);
		dataManager.register(ITEMS, arr);
	}
	
	public ArmPoseDwarf getDwarfPose() {
		return dataManager.get(POSE);
	}
	
	public void setPose(ArmPoseDwarf pose) {
		this.dataManager.set(POSE, pose);
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
		NostrumFairiesSounds.PICKAXE_HIT.play(NostrumFairies.proxy.getPlayer(), world, posX, posY, posZ);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (world.isRemote && isSwingInProgress) {
			if (this.getDwarfPose() == ArmPoseDwarf.MINING) {
				// 20% into animation is the hit
				if (this.swingProgressInt == Math.floor(this.getArmSwingAnimationEnd() * .2)) {
					playWorkSound();
				}
			}
		}
	}
	
	@Override
	public boolean isPushedByWater() {
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
		if (this.ticksExisted % 3 == 0) {
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
		return rand.nextBoolean() && rand.nextBoolean();
	}

	@Override
	protected void onWanderTick() {
		// Wander around
		if (this.navigator.noPath() && ticksExisted % 100 == 0 && rand.nextBoolean()) {
			if (!EntityFeyBase.FeyLazyFollowNearby(this, EntityFeyBase.DOMESTIC_FEY_AND_PLAYER_FILTER, 15, 3, 6)) {
				// Go to a random place
				EntityFeyBase.FeyWander(this, this.getPosition(), Math.min(10, Math.sqrt(this.wanderDistanceSq)));
			}
		}

		if (this.getAttackTarget() == null) {
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
			this.attemptTeleport(target.getX() + .5, target.getY() + .05, target.getZ() + .5, false);
		} else {
			super.teleportFromStuck();
		}
		
	}

	@Override
	public EntityFeyBase switchToSpecialization(FeyStoneMaterial material) {
		if (world.isRemote) {
			return this;
		}
		
		EntityFeyBase replacement = null;
		if (material != this.getCurrentSpecialization()) {
			if (material == FeyStoneMaterial.GARNET) {
				// Crafting
				replacement = new EntityDwarfCrafter(FairyEntities.DwarfCrafter, world);
			} else if (material == FeyStoneMaterial.EMERALD) {
				// Builder
				replacement = new EntityDwarfBuilder(FairyEntities.DwarfBuilder, world);
			} else {
				replacement = new EntityDwarf(FairyEntities.Dwarf, world);
			}
		}
		
		if (replacement != null) {
			// Kill this entity and add the other one
			replacement.copyFrom(this);
			this.remove();
			//world.removeEntityDangerously(this);
			world.addEntity(replacement);
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
