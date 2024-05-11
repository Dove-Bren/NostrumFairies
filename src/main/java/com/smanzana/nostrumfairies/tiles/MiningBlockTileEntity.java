package com.smanzana.nostrumfairies.tiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.IFeySign;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.blocks.MiningBlock;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskMineBlock;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlaceBlock;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MiningBlockTileEntity extends LogisticsTileEntity implements ITickableTileEntity, ILogisticsTaskListener, IFeySign {

		private int tickCount;
		private Map<BlockPos, ILogisticsTask> taskMap;
		private LogisticsItemWithdrawRequester materialRequester;
		private int radius;
		
		// Persisted data
		private @Nonnull ItemStack buildingMaterial = ItemStack.EMPTY;
		private @Nonnull ItemStack torches = ItemStack.EMPTY;
		private Set<BlockPos> beacons; // Beacons we've placed (for re-placement on load) 
		
		private int chunkXOffset; // rediscovered each time
		private int chunkZOffset; // ^
		private int lowestLevel; // ^
		private int scanLevel; // reset to 0 on load but that should mean 1-time long scan
		private int nextPlatform; // ^
		private int scanProgress; // When we early-out of a scan, where to pick up next time
		private int platformRequests; // Number of platform blocks currently needed
		
		// Rendering variables
		protected Set<BlockPos> oreLocations;
		protected Set<BlockPos> repairLocations;
		
		public MiningBlockTileEntity() {
			this(32);
		}
		
		public MiningBlockTileEntity(int blockRadius) {
			super(FairyTileEntities.MiningBlockTileEntityType);
			this.radius = blockRadius;
			taskMap = new HashMap<>();
			oreLocations = new HashSet<>();
			repairLocations = new HashSet<>();
			beacons = new HashSet<>();
			
			lowestLevel = -1;
			scanLevel = 1;
		}
		
		@Override
		public double getDefaultLogisticsRange() {
			return radius;
		}

		@Override
		public double getDefaultLinkRange() {
			return 10;
		}

		@Override
		public boolean canAccept(List<ItemDeepStack> stacks) {
			return false;
		}
		
		@Override
		public void takeItem(ItemStack stack) {
			if (!buildingMaterial.isEmpty() && ItemStacks.stacksMatch(stack, this.buildingMaterial)) {
				this.buildingMaterial.shrink(stack.getCount());
				if (buildingMaterial.isEmpty()) {
					buildingMaterial = ItemStack.EMPTY;
				}
			} else if (!torches.isEmpty() && ItemStacks.stacksMatch(stack, this.torches)) {
				this.torches.shrink(stack.getCount());
				if (torches.isEmpty()) {
					torches = ItemStack.EMPTY;
				}
			}
			this.dirty();
		}
		
		@Override
		public void addItem(ItemStack stack) {
			// Less likely to be torches lol
			if (!torches.isEmpty() && ItemStacks.stacksMatch(stack, this.torches)) {
				torches.setCount(Math.min(torches.getMaxStackSize(), torches.getCount() + stack.getCount())); 
			} else if (!buildingMaterial.isEmpty() && ItemStacks.stacksMatch(stack, this.buildingMaterial)) {
				buildingMaterial.setCount(Math.min(buildingMaterial.getMaxStackSize(), buildingMaterial.getCount() + stack.getCount()));
			} else if (!buildingMaterial.isEmpty() && isMaterials(stack)) {
				this.buildingMaterial = stack;
			}
			this.dirty();
		}
		
		private void refreshRequester() {
			if (this.materialRequester == null) {
				materialRequester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent); // TODO make using buffer chests configurable!
				materialRequester.addChainListener(this);
			}
			materialRequester.updateRequestedItems(getItemRequests());
		}
		
		@Override
		protected void setNetworkComponent(LogisticsTileEntityComponent component) {
			super.setNetworkComponent(component);
			
			if (world != null && !world.isRemote) {
				if (component != null) {
					if (materialRequester != null) {
						materialRequester.setNetwork(component.getNetwork());
					}
					refreshRequester();
				}
			}
		}
		
		@Override
		public void onLeaveNetwork() {
			if (!world.isRemote) {
				if (materialRequester != null) {
					materialRequester.clearRequests();
					materialRequester.setNetwork(null);
				}
				
				if (this.getNetwork() != null) {
					for (ILogisticsTask task : this.taskMap.values()) {
						this.getNetwork().getTaskRegistry().revoke(task);
					}
					for (BlockPos beacon : beacons) {
						this.getNetwork().removeBeacon(world, beacon);
					}
				}
			}
			
			super.onLeaveNetwork();
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			if (!world.isRemote) {
				if (materialRequester != null) {
					materialRequester.setNetwork(network);
					refreshRequester();
				}
				
				// add tasks and beacons to this network
				for (BlockPos beacon : beacons) {
					this.getNetwork().addBeacon(world, beacon);
				}
				
				for (ILogisticsTask task : this.taskMap.values()) {
					this.getNetwork().getTaskRegistry().register(task, null);
				}
			}
			
			super.onJoinNetwork(network);
		}
		
		protected ItemStack getRepairStack() {
			return new ItemStack(Blocks.COBBLESTONE);
		}
		
		protected NonNullList<ItemStack> getItemRequests() {
			ItemStack base;
			LogisticsNetwork network = this.getNetwork();
			
			if (network == null) {
				return NonNullList.create();
			}
			
			if (this.platformRequests == 0) {
				return NonNullList.create();
			}
			
			int existing = 0;
			if (!buildingMaterial.isEmpty()) {
				base = this.buildingMaterial;
				existing = base.getCount();
			} else {
				// If we're auto-fetching, just take cobble
				base = getRepairStack();
			}
			
			NonNullList<ItemStack> list = NonNullList.create();
			int count = platformRequests - existing;
			while (count > 0) {
				ItemStack stack = base.copy();
				stack.setCount(Math.min(count, stack.getMaxStackSize()));
				count -= stack.getCount();
				list.add(stack);
			}
			
			return list;
		}
		
		private void addBeacon(BlockPos pos) {
			beacons.add(pos);
			LogisticsNetwork network = this.getNetwork();
			if (network != null) {
				network.addBeacon(this.world, pos);
			}
		}
		
		private void makeMineTask(BlockPos pos, @Nullable LogisticsTaskMineBlock[] prereqs) {
			makeMineTask(pos, null, prereqs);
		} 
		
		private void makeMineTask(BlockPos pos, BlockPos mineAt, @Nullable LogisticsTaskMineBlock[] prereqs) {
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			pos = pos.toImmutable();
			
			if (!taskMap.containsKey(pos)) {
				// Note: I'm using different constructors so that the mine task could change someday to do different things depending
				// on the constructor
				LogisticsTaskMineBlock task;
				if (mineAt == null) {
					task = new LogisticsTaskMineBlock(this.getNetworkComponent(), "Mining Task", world, pos, prereqs);
				} else {
					task = new LogisticsTaskMineBlock(this.getNetworkComponent(), "Mining Task", world, pos, mineAt.toImmutable(), prereqs);
				}
				this.taskMap.put(pos, task);
				network.getTaskRegistry().register(task, null);
				this.markDirty();
			}
		}
		
		private void makeRepairTask(BlockPos pos, @Nullable BlockPos standAt) {
			// Actually need two tasks: one to request the material, and one to go place it
			
			// so make some other list that is our item list and update requester.
			// and hook up to the logistics interface to expose those items to dwarves
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			pos = pos.toImmutable();
			if (!taskMap.containsKey(pos)) {
				// Request platform material
				platformRequests++;
				refreshRequester();
				
				// Request block placement
				LogisticsTaskPlaceBlock task;
				BlockState state = Blocks.COBBLESTONE.getDefaultState();
				if (!buildingMaterial.isEmpty()) {
					if (buildingMaterial.getItem() instanceof BlockItem) {
						// I don't trust the null entity in there...
						BlockItem itemBlock = (BlockItem) buildingMaterial.getItem();
						try {
//							// Note: saying raytrace was up one block on the bottom face, so the PLACE loc is the right spot
//							BlockRayTraceResult trace = new BlockRayTraceResult(new Vector3d(pos.up()).add(.5, 0, .5), Direction.DOWN, pos.up(), false);
//							BlockItemUseContext context = new BlockItemUseContext(
//									new ItemUseContext(world, null, Hand.MAIN_HAND, buildingMaterial, trace));
//							int meta = itemBlock.getMetadata(buildingMaterial.getMetadata());
//							state = itemBlock.getBlock().getStateForPlacement(world, pos, Direction.UP, 0, 0, 0, meta, null, Hand.MAIN_HAND);
							
							state = itemBlock.getBlock().getDefaultState();
						} catch (Exception e) {
							// fall back to default state
							state = itemBlock.getBlock().getDefaultState(); 
						}
					}
				}
				if (standAt == null) {
					task = new LogisticsTaskPlaceBlock(this.getNetworkComponent(), "Mine Repair Task",
							getRepairStack(), state,
							world, pos);
				} else {
					task = new LogisticsTaskPlaceBlock(this.getNetworkComponent(), "Mine Repair Task",
							getRepairStack(), state,
							world, pos, standAt.toImmutable());
				}
						
				
				this.taskMap.put(pos, task);
				network.getTaskRegistry().register(task, null);
				this.repairLocations.add(pos);
			}
			
			
		}
		
		private void removeTask(BlockPos base) {
			ILogisticsTask task = taskMap.remove(base);
			if (task == null) {
				// We call this freely from event handling. Ignore it.
				return;
			}
			
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			network.getTaskRegistry().revoke(task);
			oreLocations.remove(base);
			repairLocations.remove(base);
			this.dirty();
		}
		
		private int getYFromLevel(int level) {
			return pos.getY() - level;
		}
		
//		private int levelFromY(int y) {
//			return Math.max(0, pos.getY() - y);
//		}
		
		private int platformToY(int platform) {
			return getYFromLevel(platformToLevel(platform));
		}
		
		private int platformToLevel(int platform) {
			return platform * (MiningBlock.MAJOR_LEVEL_DIFF / 2);
		}
		
		/**
		 * Get the PLATFORM level (not the regular level) for the given Y
		 * @param y
		 * @return
		 */
		private int platformForY(int y) {
			// each platform is worth MAJOR_LEVEL_DIFF / 2
			// We want the one below it, but sometimes we can't
			final int lowest = getLowestLevel();
			if (lowest == 0) {
				return 0;
			}
			
			int platform = (int) Math.ceil((pos.getY() - y) / (double) (MiningBlock.MAJOR_LEVEL_DIFF / 2));
			int level = platformToLevel(platform); 
			
			// if platform is unavailable, do the next one up
			if (level > lowest) {
				platform -= 1;
			}
			
			return platform;
		}
		
		private boolean forEachOnLayer(int level, Function<MutableBlockPos, Boolean> func) {
			final int startX = this.pos.getX() + chunkXOffset - radius;
			final int endX = this.pos.getX() + chunkXOffset + radius;
			final int startZ = this.pos.getZ() + chunkZOffset - radius;
			final int endZ = this.pos.getZ() + chunkZOffset + radius;
			final int y = getYFromLevel(level);
			
			MutableBlockPos cursor = new MutableBlockPos();
			for (int x = startX; x <= endX; x++)
			for (int z = startZ; z <= endZ; z++) {
				cursor.setPos(x, y, z);
				if (!func.apply(cursor)) {
					return false;
				}
			}
			
			return true;
		}
		
		private boolean isIgnorableBlock(BlockPos pos) {
			BlockState state = world.getBlockState(pos);
			// We care if it's something that doesn't block movement and isn't a liquid source block
			if (state.getMaterial().blocksMovement()) {
				return false;
			}
			if (state.getMaterial().isLiquid()) {
				if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA) {
					return true;
				}
			}
			
			return true;
		}
		
		private boolean isEmpty(BlockPos pos) {
			return world.isAirBlock(pos)
					|| world.getBlockState(pos).getBlock() instanceof MagicLight
					|| world.getBlockState(pos).getBlock() instanceof TorchBlock
					|| isIgnorableBlock(pos);
		}
		
		/**
		 * Makes sure the provided location is a 1x3x1 space with a block underneath
		 * @param base
		 */
		private boolean clearBlock(BlockPos base, boolean tall, @Nullable BlockPos lastPos) {
			// should base be the block underneath or the first air block??
			
			base = base.toImmutable();
			boolean tasked = false;
			
			// check underneath
			if (!Block.func_220055_a(world, base.down(), Direction.UP)) {
				makeRepairTask(base.down(), lastPos);
				tasked = true;
			}
			
			// And above and sides (but only if there's liquid flowing in
			BlockPos[] around;
			if (lastPos == null) {
				around = new BlockPos[]{tall ? base.up().up().up() : base.up().up()};
			} else {
				// Figure out which ways are the sides
				if (lastPos.getX() == base.getX()) {
					if (tall) {
						around = new BlockPos[] {base.up().up().up(),
								base.east(), base.east().up(), base.east().up().up(),
								base.west(), base.west().up(), base.west().up().up()};
					} else {
						around = new BlockPos[] {base.up().up(),
								base.east(), base.east().up(), base.west(), base.west().up()};
					}
				} else {
					if (tall) {
						around = new BlockPos[] {base.up().up().up(),
								base.north(), base.north().up(), base.north().up().up(),
								base.south(), base.south().up(), base.south().up().up()};
					} else {
						around = new BlockPos[] {base.up().up().up(),
								base.north(), base.north().up(), base.south(), base.south().up()};
					}
				}
				
			}
			for (BlockPos aroundCurs : around) {
				if (world.getBlockState(aroundCurs).getMaterial().isLiquid()) {
					makeRepairTask(aroundCurs, lastPos);
				}
			}
			
			
			// check for 2 or 3 high air
			// Make a prereq from lastPos if it's there.
			// TODO could try and use above and below blocks as prereqs? Would that help?
			MutableBlockPos lastCursor = lastPos == null ? null : new MutableBlockPos(lastPos);
			BlockPos[] range = (tall ? new BlockPos[]{base, base.up(), base.up().up(), base.up().up()} : new BlockPos[]{base, base.up(), base.up()});
			for (BlockPos pos : range) {
				if (!isEmpty(pos)) {
					LogisticsTaskMineBlock[] prereq = null;
					if (lastCursor != null) {
						lastCursor.setY(pos.getY());
						ILogisticsTask otherTask = taskMap.get(lastCursor);
						prereq = (otherTask == null || !(otherTask instanceof LogisticsTaskMineBlock) ? null
								: new LogisticsTaskMineBlock[]{(LogisticsTaskMineBlock)otherTask});
					}
					makeMineTask(pos, prereq);
					tasked = true;
				}
			}
//			if (!isEmpty(base.up())) {
//				ILogisticsTask otherTask = (lastPos == null ? null : taskMap.get(lastCursor));
//				LogisticsTaskMineBlock[] prereq = (otherTask == null || !(otherTask instanceof LogisticsTaskMineBlock) ? null
//						: new LogisticsTaskMineBlock[]{(LogisticsTaskMineBlock)otherTask});
//				
//				makeMineTask(base.up(), prereq);
//				tasked = true;
//			}
//			if (tall) {
//				if (!isEmpty(base.up().up())) {
//					ILogisticsTask otherTask = (lastPos == null ? null : taskMap.get(lastCursor));
//					LogisticsTaskMineBlock[] prereq = (otherTask == null || !(otherTask instanceof LogisticsTaskMineBlock) ? null
//							: new LogisticsTaskMineBlock[]{(LogisticsTaskMineBlock)otherTask});
//					
//					makeMineTask(base.up().up(), prereq);
//					tasked = true;
//				}
//			}
			
			return tasked;
		}
		
		/**
		 * Create the shafts required to get to where we can mine the provided pos
		 * @param pos
		 */
		private void mineTo(BlockPos pos) {
			
			// Get platform level. Then move to the right X. Then mine up to the required Z.
			// If X isn't beyond the platform, make sure to adjust where we 'start' at the right spot.
			// Note X is rounded to 4 to spread shafts out
			MutableBlockPos cursor = new MutableBlockPos();
			final int effX = pos.getX() & ~3; // chop off bottom 2 bits, rounding down to nearest 4 
			int y = platformToY(platformForY(pos.getY()));
			int x;
			int z;
			int centerX = this.pos.getX();
			int centerZ = this.pos.getZ();
			boolean outside = true; // outside the platform ring
			boolean inside = false;
			if (effX >= centerX - MiningBlock.PLATFORM_WIDTH && effX <= centerX + MiningBlock.PLATFORM_WIDTH
					&& pos.getZ() >= centerZ - MiningBlock.PLATFORM_WIDTH && pos.getZ() <= centerZ + MiningBlock.PLATFORM_WIDTH) {
				inside = true;
				// inner staircase ring. Mine on perimeter of staircase
				x = (effX < centerX) ? centerX - (MiningBlock.PLATFORM_WIDTH + 1) : centerX + (MiningBlock.PLATFORM_WIDTH + 1);
				z = centerZ;
			} else if (effX <= centerX - (MiningBlock.PLATFORM_WIDTH + MiningBlock.STAIRCASE_RADIUS)) {
				x = centerX - (MiningBlock.PLATFORM_WIDTH + MiningBlock.STAIRCASE_RADIUS);
				z = centerZ;
			} else if (effX >= centerX + (MiningBlock.PLATFORM_WIDTH + MiningBlock.STAIRCASE_RADIUS)) {
				x = centerX + (MiningBlock.PLATFORM_WIDTH + MiningBlock.STAIRCASE_RADIUS);
				z = centerZ;
			} else if (pos.getZ() <= centerZ - (MiningBlock.PLATFORM_WIDTH + MiningBlock.STAIRCASE_RADIUS)) {
				x = effX;
				z = centerZ - (MiningBlock.PLATFORM_WIDTH + MiningBlock.STAIRCASE_RADIUS);
			} else if (pos.getZ() >= centerZ + (MiningBlock.PLATFORM_WIDTH + MiningBlock.STAIRCASE_RADIUS)) {
				x = effX;
				z = centerZ + (MiningBlock.PLATFORM_WIDTH + MiningBlock.STAIRCASE_RADIUS);
			} else {
				// inside the platform area??
				outside = false;
				x = effX;
				z = pos.getZ();
			}
			
			cursor.setPos(x, y, z);
			if (!inside) {
				
				// Set the start location (and create a beacon!)
				if (outside) {
					this.addBeacon(cursor.toImmutable());
				}
				
				// Keep track of last for platform building
				MutableBlockPos last = new MutableBlockPos(cursor);
				
				// First, get to the right x
				int spaces = 0;
				while (cursor.getX() != effX) {
					clearBlock(cursor, false, last);
					last.setPos(cursor);
					cursor.move(effX > cursor.getX() ? Direction.EAST : Direction.WEST);
					
					if (spaces++ % 16 == 0) {
						// Set a new beacon every once in a while
						this.addBeacon(cursor.toImmutable());
					}
				}
				
				// Then walk to Z
				while (cursor.getZ() != pos.getZ()) {
					clearBlock(cursor, false, last);
					last.setPos(cursor);
					cursor.move(pos.getZ() > cursor.getZ() ? Direction.SOUTH : Direction.NORTH);
					
					if (spaces++ % 16 == 0) {
						// Set a new beacon every once in a while
						this.addBeacon(cursor.toImmutable());
					}
				}
				
				// Clear last (or first) block, if there is one
				if (outside) {
					clearBlock(cursor, false, last);
				}
			}
			
			// Then queue up the actual mining task
			makeMineTask(pos, cursor, new LogisticsTaskMineBlock[]{(LogisticsTaskMineBlock) taskMap.get(cursor)});
			
			// And mark that we're going there in the first place
			oreLocations.add(pos.toImmutable());
			this.dirty();
		}
		
		/**
		 * Scan a layer and queue up any tasks to grab ores.
		 * If nothing worth mining is found, returns false. Otherwise, returns true.
		 * @param y
		 * @return
		 */
		private boolean scanLayer(int level) {
			boolean[] result = new boolean[]{false};
			int[] counter = new int[]{0};
			boolean[] earlyOut = new boolean[]{false};
			
			final long startTime = System.currentTimeMillis();
			forEachOnLayer(level, (pos) -> {
				// If we're resuming, skip to the relevant block
				if (scanProgress > counter[0]++) {
					return true;
				}
				
				// Check run time every so often and stop if we've run a while
				if (counter[0] % 256 == 0) {
					final long now = System.currentTimeMillis();
					if (now - startTime > 10) {
						earlyOut[0] = true;
						return false;
					}
				}
				
				// Stop after we have a bunch of tasks queued up
				if (taskMap.size() > 100) {
					earlyOut[0] = true;
					return false;
				}
				
				// Is this something we should mine?
				if (MiningBlock.IsOre(world, pos)) {
					mineTo(pos);
					result[0] = true;
				}
				
				return true;
			});
			
			if (earlyOut[0]) {
				result[0] = true; // Not finished with this layer
				this.scanProgress = counter[0];
			} else {
				this.scanProgress = 0;
			}
			
			return result[0];
		}
		
		private boolean makePlatform(int level, boolean upper) {
			// 17x17 ring with a doorway to the north (or south if !whole) that leads in
			
			final int bounds = MiningBlock.STAIRCASE_RADIUS + MiningBlock.PLATFORM_WIDTH;
			MutableBlockPos cursor = new MutableBlockPos();
			//MutableBlockPos last = null;
			boolean clear = true;
			final int y = getYFromLevel(level);
			
			for (int i = -bounds; i <= bounds; i++)
			for (int j = -bounds; j <= bounds; j++) {
				// east or west ( i == +-bounds)? we fill in all j for these i
				// north or south only clear top and bottom
				if ((i >= -bounds && i <= -bounds + MiningBlock.PLATFORM_WIDTH)
					|| (i >= bounds - MiningBlock.PLATFORM_WIDTH && i <= bounds)
					|| (j >= -bounds && j <= -bounds + MiningBlock.PLATFORM_WIDTH)
					|| (j >= bounds - MiningBlock.PLATFORM_WIDTH && j <= bounds)) {
					// Note: we burrow centered on the actual TE but then shift our scan and
					// mine area to chunk boundaries. So these x and z are not shifted.
					final int x = pos.getX() + i;
					final int z = pos.getZ() + j;
					
//					if (last == null) {
//						last = new MutableBlockPos(x, y, z);
//					} else {
//						last.setPos(cursor);
//					}
					cursor.setPos(x, y, z);
					if (clearBlock(cursor, false, null)) {
						clear = false;
					}
				} 
			}
			
			return !clear;
		}
		
		/**
		 * Scans and queues up task for the next stairway segment, starting at the level provided.
		 * The first block dug is actually on level+1.
		 * This should not be called if we can't get all the way to the next platform level.
		 * @param level
		 * @return
		 */
		private boolean makeStaircaseSegment(int level, boolean upper) {
			MutableBlockPos cursor = new MutableBlockPos();
			MutableBlockPos last = new MutableBlockPos();
			boolean clear = true;
			
			// 5x5 spiral staircase 'starting' to the north and descending 16 blocks in
			// one spiral.
			final int perimeter = MiningBlock.MAJOR_LEVEL_DIFF;
			// we always go one-half of a side to start
			final int edgeLength = perimeter / 4;
			final int startOffset = edgeLength / 2;
			
			final int startIdx = (upper ? 0 : MiningBlock.MAJOR_LEVEL_DIFF / 2);
			final int endIdx = (upper ? MiningBlock.MAJOR_LEVEL_DIFF / 2 : MiningBlock.MAJOR_LEVEL_DIFF);
			cursor.setPos(pos.getX(), getYFromLevel(level), pos.getZ() + (upper ? -startOffset : startOffset));
			for (int i = startIdx; i < endIdx; i++) {
				Direction side = i < startOffset ? Direction.NORTH
						: (i < startOffset + edgeLength ? Direction.EAST 
						: (i < startOffset + edgeLength + edgeLength ? Direction.SOUTH
						: (i < startOffset + edgeLength + edgeLength + edgeLength ? Direction.WEST
						: Direction.NORTH)));
				
				last.setPos(cursor);
				switch (side) {
				case NORTH:
					cursor.move(Direction.EAST);
					break;
				case EAST:
					cursor.move(Direction.SOUTH);
					break;
				case SOUTH:
					cursor.move(Direction.WEST);
					break;
				case WEST:
				case UP:
				case DOWN:
				default:
					cursor.move(Direction.NORTH);
					break;
				}
				cursor.move(Direction.DOWN);
				
				if (clearBlock(cursor, true, last)) {
					clear = false;
				}
			}
			
			// Finally, the door and beacon
			this.addBeacon(cursor.toImmutable());
			last.setPos(cursor);
			cursor.move(upper ? Direction.SOUTH : Direction.NORTH);
			if (clearBlock(cursor, false, last)) {
				clear = false;
			}
			
			last.setPos(cursor);
			cursor.move(upper ? Direction.SOUTH : Direction.NORTH);
			if (clearBlock(cursor, false, last)) {
				clear = false;
			}
			
			// Make special first-time door if needed?
			// Make this should make a top-level platform instead?
			// TODO
			
			return !clear;
		}
		
		/**
		 * Check and queue up tasks to dig the mine.
		 * If the mine is already dug and looks good, returns false.
		 * @return
		 */
		private boolean makeMine(int platformToMake, boolean repair) {
			// mine holes start to the north.
			// mine is a 5x5 spiral staircase all the way down, with platforms every (MAJOR_LEVEL_DIFF / 2) blocks.
			// note: this is a place where MAJOR_LEVEL_DIFF isn't dynamic.  It needs to be 16 to fit the 5x5.
			
			// Each platform is a 9x9 ring at the same y level connected to the north part of the spiral with a 1x3x1 doorway.
			
			// Each spot should be a 1x3x1 hole. Below it should be a solid block.
			
			// Repairs only take care of the staircase and the doorway.
			final int deepest = this.getLowestLevel();
			if (deepest == 0) {
				return false;
			}
			
			boolean dug = false;
			int level = platformToLevel(platformToMake);
			if (level < deepest) {
				boolean upper = (level % MiningBlock.MAJOR_LEVEL_DIFF == 0);
				if (makeStaircaseSegment(level, upper)) {
					dug = true;
				}
				if (!repair) {
					level += MiningBlock.MAJOR_LEVEL_DIFF / 2;
					if (makePlatform(level, upper)) {
						dug = true;
					}
				}
			}
			
			return dug;
		}
		
		/**
		 * Looks at platforms up to the provided one and queues up any repair tasks
		 * that are needed. Pesky gravel!
		 * @param deepestPlatform
		 * @return
		 */
		private boolean repairMine(int deepestPlatform) {
			boolean dug = false;
			for (int platform = 0; platform <= deepestPlatform; platform++) {
				dug = makeMine(platform, true) || dug;
			}
			return dug;
		}
		
		/**
		 * Get the lowest level the mine will go. Note this is level, not Y value.
		 * A return of 0 indicates that the mine cannot actually be built, as unbreakables
		 * are getting in the way.
		 * @return
		 */
		public int getLowestLevel() {
			// TODO this should also look in 5x5 (or 9x9 or 13x13?) block down to see if unmineables get in the way?
			
			if (lowestLevel == -1) {
				int y = this.pos.getY();
				lowestLevel = 0;
				while (y > MiningBlock.MAJOR_LEVEL_DIFF) {
					y -= MiningBlock.MAJOR_LEVEL_DIFF;
					// Check this layer for unbreakables/bedrock
					if (forEachOnLayer(y, (pos) -> {
						BlockState state = world.getBlockState(pos);
						return state.getBlockHardness(world, pos) >= 0;
					})) {
						lowestLevel += MiningBlock.MAJOR_LEVEL_DIFF;
					} else {
						break;
					}
				}
			}
			
			return lowestLevel;
		}
		
		public int getLowestReachableY() {
			int lowestLevel = getLowestLevel();
			if (lowestLevel != 0) {
				return Math.max(0, getYFromLevel(lowestLevel) - MiningBlock.WORKER_REACH);
			} else {
				return pos.getY();
			}
		}
		
		private void repairScan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			if (getLowestLevel() == 0) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			if (nextPlatform > 0) {
				repairMine(nextPlatform - 1);
			}
			
			// Also repair any mine shafts we have going (helps with gravel and changing terrain)
			for (BlockPos pos : this.oreLocations.toArray(new BlockPos[oreLocations.size()])) {
				mineTo(pos);
			}
			final long end = System.currentTimeMillis();
			if (end - startTime >= 5) {
				System.out.println("Took " + (end - startTime) + "ms to scan for repairs!");
			}
		}
		
		private void scan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			if (scanLevel > getLowestLevel() || getLowestLevel() == 0) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			boolean didWork = false;
			while (didWork == false) {
				// If scan level is behind platform, scan. Otherwise, make a platform.
				// Keep doing both if they run and find no work to do. Could limit this here. Once per minute seems long tho.
				if (nextPlatform == 0 || scanLevel > platformToLevel(nextPlatform)) {
					// Done scanning for this level.
					// However, we wait to dig the next layer until there aren't very many outstanding tasks.
					// This prevents the mine scanning from running away from the progress of the miners.
					if (this.taskMap.size() > 10) {
						break; // break the whole loop
					}
					
					didWork = makeMine(nextPlatform, false);
					nextPlatform++;
				}
				
				if (!didWork) {
					// If this is the first layer after a platform is made, wait for the platform to finish
					if (scanLevel == platformToLevel(nextPlatform - 1) + 1) {
						if (this.taskMap.size() > 0) {
							break;
						}
					}
					
					didWork = scanLayer(scanLevel);
					if (this.scanProgress == 0) { // if no 'pickup' location was set
						scanLevel++;
					}
				}
			}
			
			final long end = System.currentTimeMillis();
			if (end - startTime >= 15) {
				System.out.println("Took " + (end - startTime) + "ms to scan for ores!");
			}
		}
		
		private void cleanTasks() {
			// Clean out any finished tasks
			List<BlockPos> keys = Lists.newArrayList(taskMap.keySet());
			for (BlockPos pos : keys) {
				ILogisticsTask task = taskMap.get(pos);
				if (task != null && task.isComplete()) {
					removeTask(pos);
					oreLocations.remove(pos);
					repairLocations.remove(pos);
					this.dirty();
				}
			}
		}
		
		@Override
		public void tick() {
			if (this.world.isRemote) {
				return;
			}
			
			if (this.getNetwork() == null) {
				return;
			}
			
			if (this.tickCount == 0) {
				for (BlockPos pos : this.beacons) {
					this.getNetwork().addBeacon(world, pos);
				}
			}
			
			if (this.tickCount % 5 == 0) {
				cleanTasks();
			}
			if (this.tickCount % (20) == 0) {
				repairScan();
			}
			int period = (scanLevel >= getLowestLevel()) ? 180
					: (scanProgress > 0 && taskMap.size() < 100 ? 5 : 60);
			if (this.tickCount % (20 * period) == 0) {
				scan();
			}
			this.tickCount++;
		}
		
		@SubscribeEvent
		public void onBreak(BlockEvent.BreakEvent e) {
			if (e.isCanceled()) {
				return;
			}
			
			if (e.getWorld() == this.world) {
				removeTask(e.getPos());
			}
		}
		
		@Override
		public void setWorld(World worldIn) {
			super.setWorld(worldIn);
			if (!worldIn.isRemote) {
				MinecraftForge.EVENT_BUS.register(this);
			}
			
			if (this.networkComponent != null && !worldIn.isRemote && materialRequester == null) {
				refreshRequester();
			}
		}
		
		@Override
		public void setPos(BlockPos posIn) {
			super.setPos(posIn);
			
			chunkXOffset = -((posIn.getX() - radius) & 0xF); // lowest 16 values
			chunkZOffset = -((posIn.getZ() - radius) & 0xF);
		}
		
		public static final String NBT_ORES = "ores";
		public static final String NBT_REPAIRS = "repairs";
		public static final String NBT_BEACONS = "beacons";
		public static final String NBT_PLATFORMS = "platforms";
		public static final String NBT_TORCHES = "torches";
		
		@Override
		public CompoundNBT write(CompoundNBT nbt) {
			// We STORE the UUID of our network... but only so we can communicate it to the client.
			// We hook things back up on the server when we load by position.
			nbt = super.write(nbt);
			
			ListNBT list = new ListNBT();
			for (BlockPos pos : oreLocations) {
				list.add(NBTUtil.writeBlockPos(pos));
			}
			nbt.put(NBT_ORES, list);
			
			list = new ListNBT();
			for (BlockPos pos : repairLocations) {
				list.add(NBTUtil.writeBlockPos(pos));
			}
			nbt.put(NBT_REPAIRS, list);
			
			list = new ListNBT();
			for (BlockPos pos : beacons) {
				list.add(NBTUtil.writeBlockPos(pos));
			}
			nbt.put(NBT_BEACONS, list);
			
			if (!buildingMaterial.isEmpty()) {
				nbt.put(NBT_PLATFORMS, this.buildingMaterial.serializeNBT());
			}
			if (!torches.isEmpty()) {
				nbt.put(NBT_TORCHES, this.torches.serializeNBT());
			}
			
			list = new ListNBT();
			for (BlockPos pos : taskMap.keySet()) {
				list.add(NBTUtil.writeBlockPos(pos));
			}
			nbt.put("paths", list);
			
			return nbt;
		}
		
		@Override
		public void read(CompoundNBT nbt) {
			super.read(nbt);
			
			this.oreLocations.clear();
			this.repairLocations.clear();
			if (world != null && world.isRemote) {
				ListNBT list = nbt.getList(NBT_ORES, NBT.TAG_COMPOUND);
				for (int i = 0; i < list.size(); i++) {
					BlockPos pos = NBTUtil.readBlockPos(list.getCompound(i));
					oreLocations.add(pos);
				}
				list = nbt.getList(NBT_REPAIRS, NBT.TAG_COMPOUND);
				for (int i = 0; i < list.size(); i++) {
					BlockPos pos = NBTUtil.readBlockPos(list.getCompound(i));
					repairLocations.add(pos);
				}
				this.taskMap.clear();
				list = nbt.getList("paths", NBT.TAG_COMPOUND);
				for (int i = 0; i < list.size(); i++) {
					BlockPos pos = NBTUtil.readBlockPos(list.getCompound(i));
					taskMap.put(pos, null);
				}
			} else {
				ListNBT list = nbt.getList(NBT_BEACONS, NBT.TAG_COMPOUND);
				for (int i = 0; i < list.size(); i++) {
					BlockPos pos = NBTUtil.readBlockPos(list.getCompound(i));
					beacons.add(pos);
				}
			}
			
			this.buildingMaterial = ItemStack.EMPTY;
			if (nbt.contains(NBT_PLATFORMS)) {
				this.buildingMaterial = ItemStack.read(nbt.getCompound(NBT_PLATFORMS));
			}
			this.torches = ItemStack.EMPTY;
			if (nbt.contains(NBT_TORCHES)) {
				this.torches = ItemStack.read(nbt.getCompound(NBT_TORCHES));
			}
			
			if (this.world != null && this.world.isRemote) {
				StaticTESRRenderer.instance.update(world, pos, this);
			}
		}

		@Override
		public void onTaskDrop(ILogisticsTask task, IFeyWorker worker) {
			;
		}

		@Override
		public void onTaskAccept(ILogisticsTask task, IFeyWorker worker) {
			;
		}

		@Override
		public void onTaskComplete(ILogisticsTask task, IFeyWorker worker) {
			// If this was a material request, decrement our outstanding count
			if (task instanceof LogisticsTaskPlaceBlock) {
				this.platformRequests--;
			}
		}
		
		public @Nonnull ItemStack getMaterials() {
			return this.buildingMaterial;
		}
		
		public boolean isMaterials(@Nonnull ItemStack stack) {
			return stack.isEmpty() || stack.getItem() instanceof BlockItem;
		}
		
		public void setMaterials(@Nonnull ItemStack stack) {
			if (isMaterials(stack)) {
				this.buildingMaterial = stack;
				this.dirty();
			}
		}
		
		public @Nonnull ItemStack getTorches() {
			return this.torches;
		}
		
		public boolean isTorches(@Nonnull ItemStack stack) {
			return stack.isEmpty()
				|| (torches.getItem() instanceof BlockItem && ((BlockItem) torches.getItem()).getBlock() instanceof TorchBlock);
		}
		
		public void setTorches(@Nonnull ItemStack torches) {
			if (isTorches(torches)) {
				this.torches = torches;
				this.dirty();
			}
		}
		
		private void dirty() {
			//world.markBlockRangeForRenderUpdate(pos, pos);
			world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
			this.markDirty();
		}
		
		/**
		 * Check whether the provided task will be reachable after all other tasks the provided worker has for this
		 * mine have been completed
		 * @param task
		 * @param worker
		 * @return
		 */
		public boolean taskAccessibleWithTasks(LogisticsTaskMineBlock task, IFeyWorker worker) {
			// Will look at blocks next to this block and see if they have tasks, and if the task is
			// taken by this worker. Could care if it's taken by ANY worker, but that'd mean all workers
			// would be down in the mines waiting, and probably stomp eachother? I might need to experiment...
			BlockPos pos = task.getTargetBlock();
			if (!this.taskMap.containsKey(pos)) {
				return false;
			}
			
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return false;
			}
			
			// We do NOT look to see if there's air here. That would falsely say we could get to any
			// mine task that happens to have air next to it. We need to check pathing for those things.
			// Instead, we're trying to find blocks that are about to have the block next to them mined.
			// The other tasks had a pathing check, so we should be able to get to this one.
			/*
			if (world.isAirBlock(pos.north())) {
				pos = pos.north();
			} else if (world.isAirBlock(pos.south())) {
				pos = pos.south();
			} else if (world.isAirBlock(pos.east())) {
				pos = pos.east();
			} else if (world.isAirBlock(pos.west())) {
				pos = pos.west();
			} else if (world.isAirBlock(pos.up())) {
				pos = pos.up();
			} else {
				pos = pos.down();
			}
			
			if (world.isAirBlock(pos)) {
				// Already air?
				return true;
			}
			*/
			
			for (BlockPos other : new BlockPos[] {pos.up(), pos.north(), pos.south(), pos.east(), pos.west(), pos.down()}) {
				ILogisticsTask otherTask = taskMap.get(other);
				if (otherTask != null && network.getTaskRegistry().getCurrentWorker(otherTask) == worker) {
					return true;
				}
			}
			
			return false;
		}
		
		private static final ResourceLocation SIGN_ICON = new ResourceLocation(NostrumFairies.MODID, "textures/block/logistics_mining_block_icon.png");

		@Override
		public ResourceLocation getSignIcon(IFeySign sign) {
			return SIGN_ICON;
		}
		
		@Override
		public Direction getSignFacing(IFeySign sign) {
			BlockState state = world.getBlockState(pos);
			return state.get(MiningBlock.FACING);
		}
		
		@Override
		public void remove() {
			super.remove();
			if (world != null && world.isRemote) {
				StaticTESRRenderer.instance.update(world, pos, null);
			}
		}
		
		public void collectOreLocations(Set<BlockPos> locationSet) {
			locationSet.addAll(oreLocations);
		}
		
		public void collectRepairLocations(Set<BlockPos> locationSet) {
			locationSet.addAll(repairLocations);
		}
	}