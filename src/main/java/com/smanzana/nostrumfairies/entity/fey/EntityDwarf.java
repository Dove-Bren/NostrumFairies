package com.smanzana.nostrumfairies.entity.fey;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.blocks.MiningBlock;
import com.smanzana.nostrumfairies.entity.navigation.PathFinderPublic;
import com.smanzana.nostrumfairies.entity.navigation.PathNavigatorLogistics;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskMineBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlaceBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemStacks;
import com.smanzana.nostrumfairies.utils.Paths;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
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
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
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
		this.isImmuneToFire = true;
		
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
					
					private PathNodeType getPathNodeTypeRaw(IBlockAccess access, int x, int y, int z) {
						BlockPos blockpos = new BlockPos(x, y, z);
						IBlockState iblockstate = access.getBlockState(blockpos);
						Block block = iblockstate.getBlock();
						Material material = iblockstate.getMaterial();
						return (material == Material.AIR || material == Material.LAVA) ? PathNodeType.OPEN : (block != Blocks.TRAPDOOR && block != Blocks.IRON_TRAPDOOR && block != Blocks.WATERLILY ? (block == Blocks.FIRE ? PathNodeType.DAMAGE_FIRE : (block == Blocks.CACTUS ? PathNodeType.DAMAGE_CACTUS : (block instanceof BlockDoor && material == Material.WOOD && !((Boolean)iblockstate.getValue(BlockDoor.OPEN)).booleanValue() ? PathNodeType.DOOR_WOOD_CLOSED : (block instanceof BlockDoor && material == Material.IRON && !((Boolean)iblockstate.getValue(BlockDoor.OPEN)).booleanValue() ? PathNodeType.DOOR_IRON_CLOSED : (block instanceof BlockDoor && ((Boolean)iblockstate.getValue(BlockDoor.OPEN)).booleanValue() ? PathNodeType.DOOR_OPEN : (block instanceof BlockRailBase ? PathNodeType.RAIL : (!(block instanceof BlockFence) && !(block instanceof BlockWall) && (!(block instanceof BlockFenceGate) || ((Boolean)iblockstate.getValue(BlockFenceGate.OPEN)).booleanValue()) ? (material == Material.WATER ? PathNodeType.WATER : (material == Material.LAVA ? PathNodeType.LAVA : (block.isPassable(access, blockpos) ? PathNodeType.OPEN : PathNodeType.BLOCKED))) : PathNodeType.FENCE))))))) : PathNodeType.TRAPDOOR);
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
	
	private @Nullable BlockPos findEmptySpot(BlockPos targetPos, boolean allOrNothing, boolean repair) {
		
		// repair tasks are going to add the block, so don't stand there! Stand above -- up to 2 blocks above
		// We also change the order we evaluate spots based on the same thing. we prefer above for repair, and prefer at or below
		// for non-repair
		
		if (repair || ((!worldObj.isAirBlock(targetPos) && worldObj.getBlockState(targetPos).getMaterial().blocksMovement() && worldObj.getBlockState(targetPos).getMaterial() != Material.LAVA)
						|| !worldObj.isSideSolid(targetPos.down(), EnumFacing.UP))) {
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
				if ((worldObj.isAirBlock(pos) || worldObj.getBlockState(pos).getMaterial() == Material.LAVA || !worldObj.getBlockState(pos).getMaterial().blocksMovement())
						&& worldObj.isSideSolid(pos.down(), EnumFacing.UP)) {
					targetPos = pos;
					break;
				}
			}
		}
		
//		
//		if ((repair) || (!worldObj.isAirBlock(targetPos) || !worldObj.isSideSolid(targetPos.down(), EnumFacing.UP))) {
//			do {
//				if (worldObj.isAirBlock(targetPos.north())) {
//					if (worldObj.isSideSolid(targetPos.north().down(), EnumFacing.UP)) {
//						targetPos = targetPos.north();
//						break;
//					} else if (worldObj.isSideSolid(targetPos.north().down().down(), EnumFacing.UP)) {
//						targetPos = targetPos.north().down();
//						break;
//					} else if (worldObj.isSideSolid(targetPos.north(), EnumFacing.UP)) {
//						targetPos = targetPos.north().up();
//						break;
//					} else if (repair && worldObj.isSideSolid(targetPos.north().up(), EnumFacing.UP)) {
//						targetPos = targetPos.north().up().up();
//						break;
//					}
//				}
//				if (worldObj.isAirBlock(targetPos.south())) {
//					if (worldObj.isSideSolid(targetPos.south().down(), EnumFacing.UP)) {
//						targetPos = targetPos.south();
//						break;
//					} else if (worldObj.isSideSolid(targetPos.south().down().down(), EnumFacing.UP)) {
//						targetPos = targetPos.south().down();
//						break;
//					} else if (worldObj.isSideSolid(targetPos.south(), EnumFacing.UP)) {
//						targetPos = targetPos.south().up();
//						break;
//					} else if (repair && worldObj.isSideSolid(targetPos.south().up(), EnumFacing.UP)) {
//						targetPos = targetPos.south().up().up();
//						break;
//					}
//				}
//				if (worldObj.isAirBlock(targetPos.east())) {
//					if (worldObj.isSideSolid(targetPos.east().down(), EnumFacing.UP)) {
//						targetPos = targetPos.east();
//						break;
//					} else if (worldObj.isSideSolid(targetPos.east().down().down(), EnumFacing.UP)) {
//						targetPos = targetPos.east().down();
//						break;
//					} else if (worldObj.isSideSolid(targetPos.east(), EnumFacing.UP)) {
//						targetPos = targetPos.east().up();
//						break;
//					} else if (repair && worldObj.isSideSolid(targetPos.east().up(), EnumFacing.UP)) {
//						targetPos = targetPos.east().up().up();
//						break;
//					}
//				}
//				if (worldObj.isAirBlock(targetPos.west())) {
//					if (worldObj.isSideSolid(targetPos.west().down(), EnumFacing.UP)) {
//						targetPos = targetPos.west();
//						break;
//					} else if (worldObj.isSideSolid(targetPos.west().down().down(), EnumFacing.UP)) {
//						targetPos = targetPos.west().down();
//						break;
//					} else if (worldObj.isSideSolid(targetPos.west(), EnumFacing.UP)) {
//						targetPos = targetPos.west().up();
//						break;
//					} else if (repair && worldObj.isSideSolid(targetPos.west().up(), EnumFacing.UP)) {
//						targetPos = targetPos.west().up().up();
//						break;
//					}
//				}
//				if (!repair && worldObj.isAirBlock(targetPos.up()) && worldObj.isSideSolid(targetPos, EnumFacing.UP)) {
//					targetPos = targetPos.up();
//					break;
//				}
//				if (worldObj.isAirBlock(targetPos.down()) && worldObj.isSideSolid(targetPos.down().down(), EnumFacing.UP)) {
//					targetPos = targetPos.down();
//					break;
//				}
//			} while (false);
//		}
		
		if (allOrNothing) {
			if (!worldObj.isAirBlock(targetPos)
					&& worldObj.getBlockState(targetPos).getMaterial().blocksMovement()
					&& worldObj.getBlockState(targetPos).getMaterial() != Material.LAVA) {
				targetPos = null;
			}
		}
		
		return targetPos;
	}

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskMineBlock) {
			LogisticsTaskMineBlock mine = (LogisticsTaskMineBlock) task;
			
			if (mine.getWorld() != this.worldObj) {
				return false;
			}
			
			// Dwarves only perform tasks from their mine
			if (this.getHome() == null || mine.getSourceComponent() == null ||
					!this.getHome().equals(mine.getSourceComponent().getPosition())) {
				return false;
			}
			
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
					TileEntity te = worldObj.getTileEntity(mine.getSourceComponent().getPosition());
					if (te != null && te instanceof MiningBlock.MiningBlockTileEntity) {
						return ((MiningBlock.MiningBlockTileEntity) te).taskAccessibleWithTasks(mine, this);
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
		} else if (task instanceof LogisticsTaskPlaceBlock && !(task instanceof LogisticsTaskPlantItem)) {
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
							movePos = findEmptySpot(movePos, false, (task instanceof LogisticsTaskPlaceBlock));
							
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
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 18;
	}
	
	@Override
	protected void onCientTick() {
		;
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (worldObj.isRemote && isSwingInProgress) {
			if (this.getPose() == ArmPose.MINING) {
				// 20% into animation is the hit
				if (this.swingProgressInt == Math.floor(this.getArmSwingAnimationEnd() * .2)) {
					NostrumFairiesSounds.PICKAXE_HIT.play(NostrumFairies.proxy.getPlayer(), worldObj, posX, posY, posZ);
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
}
