package com.smanzana.nostrumfairies.tiles;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.IFeySign;
import com.smanzana.nostrumfairies.blocks.WoodcuttingBlock;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.ILogisticsTaskUniqueData;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskChopTree;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WoodcuttingBlockTileEntity extends LogisticsTileEntity implements ITickableTileEntity,  ILogisticsTaskListener, IFeySign {
	
	protected static final ILogisticsTaskUniqueData<BlockPos> WOODCUTTING_POSITION = new ILogisticsTaskUniqueData<BlockPos>() { };

	private int tickCount;
	private Map<BlockPos, ILogisticsTask> taskMap;
	private double radius;
	
	public WoodcuttingBlockTileEntity() {
		this(16);
	}
	
	public WoodcuttingBlockTileEntity(double blockRadius) {
		super(FairyTileEntities.WoodcuttingBlockTileEntityType);
		taskMap = new HashMap<>();
		this.radius = blockRadius;
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
	
	private void makeChopTreeTask(BlockPos base) {
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		if (!taskMap.containsKey(base) && network.taskDataAdd(WOODCUTTING_POSITION, base)) {
			LogisticsTaskChopTree task = new LogisticsTaskChopTree(this.getNetworkComponent(), "Tree Chop Task", world, base);
			this.taskMap.put(base, task);
			network.getTaskRegistry().register(task, this);
		}
	}
	
	private void makeChopBranchTask(BlockPos base) {
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		if (!taskMap.containsKey(base) && network.taskDataAdd(WOODCUTTING_POSITION, base)) {
			// Find spot underneath on ground
			BlockPos target = base;
			while (!Block.hasEnoughSolidSide(world, target.down(), Direction.UP)) {
				target = target.down();
			}
			
			LogisticsTaskChopTree task = new LogisticsTaskChopTree(this.getNetworkComponent(), "Tree Chop Task", world, base, target);
			this.taskMap.put(base, task);
			network.getTaskRegistry().register(task, this);
		}
	}
	
	private void makePlantTask(BlockPos base) {
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		if (!taskMap.containsKey(base)) {
			LogisticsTaskPlantItem task = new LogisticsTaskPlantItem(this.networkComponent, "Plant Sapling", this.getSapling(), world, base);
			this.taskMap.put(base, task);
			network.getTaskRegistry().register(task, this);
		}
	}
	
	private void removeTask(BlockPos base) {
		ILogisticsTask task = taskMap.remove(base);
		if (task == null) {
			// wut
			return;
		}
		
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		network.getTaskRegistry().revoke(task);
		network.taskDataRemove(WOODCUTTING_POSITION, base);
	}
	
	private void scan() {
		if (this.getNetwork() == null) {
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		
		// Look for trees nearby and record their base. Also mark off ones we already know about.
		Set<BlockPos> known = Sets.newHashSet(taskMap.keySet());
		List<BlockPos> trunks = new LinkedList<>();
		List<BlockPos> branches = new LinkedList<>();
		
		// Remove any 'plant' tasks from the 'known' list
		Iterator<BlockPos> it = known.iterator();
		while (it.hasNext()) {
			BlockPos pos = it.next();
			ILogisticsTask task = taskMap.get(pos);
			if (task == null || task instanceof LogisticsTaskPlantItem) {
				it.remove();
			}
		}
		
		BlockPos.Mutable pos = new BlockPos.Mutable();
		BlockPos center = this.getPos();
		final int startY = (int) Math.max(-pos.getY(), Math.floor(-radius));
		final int endY = (int) Math.min(256 - pos.getY(), Math.ceil(radius));
		for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++)
		for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++)
		for (int y = startY; y < endY; y++) {
			
			pos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
			if (!NostrumMagica.isBlockLoaded(world, pos)) {
				break; // skip this whole column
			}
			
			if (WoodcuttingBlock.isTree(world, pos)) {
				// Record!
				// Don't make task cause we filter better later
				trunks.add(pos.toImmutable());
				
				// climb tree to avoid checking each piece above it
				do {
					pos.move(Direction.UP);
					y++;
				} while (y < (endY - 1) && WoodcuttingBlock.isTrunkMaterial(world, pos));
			} else if (WoodcuttingBlock.isBranch(world, pos)) {
				branches.add(pos.toImmutable());
			}
		}
		
		// For trunks we found, see if they are next to other trunks and ignore them if so!
		// This could see that two pos's are XZ 1 away and then climb trunks to see if they touch
		// but NAH
		if (!trunks.isEmpty()) {
			final int yRange = 4;
			BlockPos[] neighbors = new BlockPos[trunks.size() - 1];
			for (int i = 0; i < trunks.size(); i++) { // not enhanced since we will be modifying!
				// Find all other trunks that are XZ 1 away and within some Y range.
				BlockPos trunk = trunks.get(i);
				int neighborIdx = 0;
				
				// Gotta keep doing this until no new neighbors are found :(
				boolean foundOne;
				do {
					foundOne = false;
					for (int j = trunks.size() - 1; j > i; j--) { // backwards so we can remove and not worry
						BlockPos candidate = trunks.get(j);
						// If this pos is near the original or any neighbors, add it to neighbor and remove
						// Check trunk
						if (Math.abs(trunk.getY() - candidate.getY()) <= yRange
							&& Math.abs(trunk.getX() - candidate.getX()) <= 1
							&& Math.abs(trunk.getZ() - candidate.getZ()) <= 1) {
							// oops it's a neighbor
							neighbors[neighborIdx++] = candidate;
							trunks.remove(j);
							foundOne = true;
							continue;
						}
						
						// Repeat the above for all neighbors
						for (int n = 0; n < neighborIdx; n++) {
							BlockPos neighbor = neighbors[n];
							if (Math.abs(candidate.getY() - neighbor.getY()) <= yRange
									&& Math.abs(candidate.getX() - neighbor.getX()) <= 1
									&& Math.abs(candidate.getZ() - neighbor.getZ()) <= 1) {
									// oops it's a neighbor
									neighbors[neighborIdx++] = candidate;
									trunks.remove(j);
									foundOne = true;
									break;
								}
						}
					}
				} while (foundOne);
			}
			
			// New, condensed trunk list should be converted to tasks.
			// I think this all should work, since it should be deterministic...
			for (BlockPos base : trunks) {
				if (known.remove(base)) {
					; // We already knew about it, so don't create a new one
				} else {
					// Didn't know, so record!
					// Don't make task cause we filter better later
					makeChopTreeTask(base);
				}
				
			}
		}
		
		if (!branches.isEmpty()) {
			for (BlockPos base : branches) {
				if (known.remove(base)) {
					; // We already knew about it, so don't create a new one
				} else {
					// Didn't know, so record!
					// Don't make task cause we filter better later
					makeChopBranchTask(base);
				}
				
			}
		}
		
		// For any left in known, the tree is not there anymore! Remove!
		for (BlockPos base : known) {
			removeTask(base);
		}
		
		final long end = System.currentTimeMillis();
		if (end - startTime >= 5) {
			System.out.println("Took " + (end - startTime) + "ms to scan for trees!");
		}
	}
	
	@Override
	public void tick() {
		if (this.world.isRemote) {
			return;
		}
		
		this.tickCount++;
		if (this.tickCount % (20 * 10) == 0) {
			scan();
		}
	}
	
//		private boolean inRange(BlockPos pos) {
//			// Max distance with a square radius of X is X times sqrt(3).
//			// sqrt(3) is ~1.7321
//			
//			// Idk if this is actually faster than 6 conditionals.
//			return this.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <=
//						Math.pow(radius * 1.7321, 2);
//		}
//		
//		@SubscribeEvent
//		public void onGrow(SaplingGrowTreeEvent e) {
//			 if (e.getWorld() == this.world && inRange(e.getPos())) {
//				 this.scan();
//			 }
//		}
	
	@Override
	public void setWorldAndPos(World worldIn, BlockPos pos) {
		super.setWorldAndPos(worldIn, pos);
//			if (!worldIn.isRemote) {
//				MinecraftForge.TERRAIN_GEN_BUS.register(this);
//			}
	}
	
	// TODO make configurable!
	public ItemStack getSapling() {
		return new ItemStack(Items.OAK_SAPLING);
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
		// Cleanup used to be automatically handled by the requester. However, we want to
		// queue up replanting sometimes if the tree comes down. So do that here.
		if (task instanceof LogisticsTaskChopTree) {
			LogisticsTaskChopTree chopTask = (LogisticsTaskChopTree) task;
			BlockPos pos = chopTask.getTrunkPos();
			if (taskMap.remove(pos) != null) {
				makePlantTask(pos);
				LogisticsNetwork network = this.getNetwork();
				if (network != null) {
					network.taskDataRemove(WOODCUTTING_POSITION, pos);
				}
			}
		} else if (task instanceof LogisticsTaskPlantItem) {
			LogisticsTaskPlantItem plantTask = (LogisticsTaskPlantItem) task;
			BlockPos pos = plantTask.getTargetBlock();
			taskMap.remove(pos);
		}
	}
	
	private static final ResourceLocation SIGN_ICON = new ResourceLocation(NostrumFairies.MODID, "textures/block/logistics_woodcutting_block_icon.png");

	@Override
	public ResourceLocation getSignIcon(IFeySign sign) {
		return SIGN_ICON;
	}
	
	@Override
	public Direction getSignFacing(IFeySign sign) {
		BlockState state = world.getBlockState(pos);
		return state.get(WoodcuttingBlock.FACING);
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound) {
		super.read(state, compound);
	}
	
	@Override
	public void remove() {
		super.remove();
	}
}