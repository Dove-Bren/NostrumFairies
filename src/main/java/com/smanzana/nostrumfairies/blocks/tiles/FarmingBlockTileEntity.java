package com.smanzana.nostrumfairies.blocks.tiles;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.blocks.FarmingBlock;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskHarvest;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class FarmingBlockTileEntity extends LogisticsTileEntity implements ITickable,  ILogisticsTaskListener, IFeySign {

	private static final Map<Integer, ItemStack> SeedMap = new HashMap<>(); // int id of blockstate to itemstack seed
	
	private int tickCount;
	private final Map<BlockPos, ILogisticsTask> taskMap;
	private final Map<BlockPos, IBlockState> seenStates;
	private double radius;
	
	public FarmingBlockTileEntity() {
		this(7);
	}
	
	public FarmingBlockTileEntity(double blockRadius) {
		super();
		taskMap = new HashMap<>();
		seenStates = new HashMap<>();
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
	
	private void rememberState(BlockPos pos, @Nullable IBlockState state) {
		if (state == null) {
			seenStates.remove(pos);
		} else {
			seenStates.put(pos, state);
		}
	}
	
	private void makeHarvestTask(BlockPos base) {
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		if (!taskMap.containsKey(base)) {
			LogisticsTaskHarvest task = new LogisticsTaskHarvest(this.getNetworkComponent(), "Farm Harvest Task", world, base);
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
			LogisticsTaskPlantItem task = new LogisticsTaskPlantItem(this.networkComponent, "Plant Sapling", this.getSeed(world, base), world, base);
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
	}
	
	private void scan() {
		if (this.getNetwork() == null) {
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		
		Set<BlockPos> known = Sets.newHashSet(taskMap.keySet());
		List<BlockPos> emptySpots = new LinkedList<>();
		List<BlockPos> grownCrops = new LinkedList<>();
		
		MutableBlockPos pos = new MutableBlockPos();
		BlockPos center = this.getPos();
		final int startY = (int) Math.max(-pos.getY(), Math.floor(-radius));
		final int endY = (int) Math.min(256 - pos.getY(), Math.ceil(radius));
		for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++)
		for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++)
		for (int y = startY; y < endY; y++) {
			
			pos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
			if (!world.isBlockLoaded(pos)) {
				break; // skip this whole column
			}
			
			if (FarmingBlock.isGrownCrop(world, pos)) {
				grownCrops.add(pos.toImmutable());
			} else if (FarmingBlock.isPlantableSpot(world, pos.down(), this.getSeed(world, pos))) {
				emptySpots.add(pos.toImmutable());
			}
		}
		
		for (BlockPos base : grownCrops) {
			if (known.contains(base) && taskMap.get(base) instanceof LogisticsTaskHarvest) {
				known.remove(base); // We already knew about it, so don't create a new one
			} else {
				// Didn't know, so record!
				// Don't make task cause we filter better later
				makeHarvestTask(base);
				
				// Also remember what state we saw here
				rememberState(base, world.getBlockState(base));
			}
		}
		
		// Repeat for plant tasks
		for (BlockPos base : emptySpots) {
			if (known.contains(base) && taskMap.get(base) instanceof LogisticsTaskPlantItem) {
				known.remove(base); // We already knew about it, so don't create a new one
			} else {
				// Didn't know, so record!
				// Don't make task cause we filter better later
				makePlantTask(base);
			}
		}
		
		// For any left in known, the crop/spot is not there anymore! Remove!
		for (BlockPos base : known) {
			removeTask(base);
		}
		
		final long end = System.currentTimeMillis();
		if (end - startTime >= 5) {
			System.out.println("Took " + (end - startTime) + "ms to scan for crops!");
		}
	}
	
	@Override
	public void update() {
		if (this.world.isRemote) {
			return;
		}
		
		this.tickCount++;
		if (this.tickCount % (20 * 10) == 0) {
			scan();
		}
	}
	
	protected static @Nullable ItemStack ResolveSeed(@Nullable IBlockState state) {
		ItemStack seeds = null;
		if (state != null) {
			seeds = SeedMap.get(Block.getStateId(state));
			if (seeds != null) {
				return seeds.copy();
			}
			
			// Try and figure out what the seed would be
			if (state.getBlock() instanceof BlockCrops) {
				try {
					Method getSeed = ReflectionHelper.findMethod(BlockCrops.class, (BlockCrops) state.getBlock(), new String[] {"getSeed", "func_149866_i"});
					seeds = new ItemStack((Item) getSeed.invoke((BlockCrops) state.getBlock()));
				} catch (Exception e) {
					seeds = null;
				}
			}
			
			// Cache this lookup
			SeedMap.put(Block.getStateId(state), seeds == null ? new ItemStack(Items.WHEAT_SEEDS) : seeds.copy());
		}
		
		return seeds;
	}
	
	// TODO make configurable!
	public ItemStack getSeed(World world, BlockPos pos) {
		@Nullable IBlockState state = seenStates.get(pos);
		
		// Try and figure out what the seed would be
		@Nullable ItemStack seeds = ResolveSeed(state);
		
		// Attempt to use nearby seeds if we couldn't figure one out
		if (seeds == null) {
			for (BlockPos nearby : new BlockPos[] {pos.north(), pos.south(), pos.east(), pos.west()}) {
				state = seenStates.get(nearby);
				if (state == null) {
					state = world.getBlockState(nearby);
				}
				
				seeds = ResolveSeed(state);
				if (seeds != null) {
					break;
				}
			}
		}
		
		if (seeds == null) {
			// Could look for available seeds
			seeds = new ItemStack(Items.WHEAT_SEEDS);
		}
		
		return seeds;
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
		if (task instanceof LogisticsTaskHarvest) {
			LogisticsTaskHarvest harvesetTask = (LogisticsTaskHarvest) task;
			BlockPos pos = harvesetTask.getCropPos();
			taskMap.remove(pos);
		} else if (task instanceof LogisticsTaskPlantItem) {
			LogisticsTaskPlantItem plantTask = (LogisticsTaskPlantItem) task;
			BlockPos pos = plantTask.getTargetBlock();
			taskMap.remove(pos);
		}
	}
	
	private static final ItemStack SIGN_ICON = new ItemStack(Items.IRON_HOE);

	@Override
	public ItemStack getSignIcon(IFeySign sign) {
		return SIGN_ICON;
	}
	
	@Override
	public EnumFacing getSignFacing(IFeySign sign) {
		IBlockState state = world.getBlockState(pos);
		return state.getValue(FarmingBlock.FACING);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		if (this.world != null && this.world.isRemote) {
			StaticTESRRenderer.instance.update(world, pos, this);
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if (world != null && world.isRemote) {
			StaticTESRRenderer.instance.update(world, pos, null);
		}
	}
}