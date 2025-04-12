package com.smanzana.nostrumfairies.tiles;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FarmingBlock;
import com.smanzana.nostrumfairies.blocks.IFeySign;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskHarvest;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPlantItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class FarmingBlockTileEntity extends LogisticsTileEntity implements TickableBlockEntity,  ILogisticsTaskListener, IFeySign {

	private static final Map<Integer, ItemStack> SeedMap = new HashMap<>(); // int id of blockstate to itemstack seed
	
	private int tickCount;
	private final Map<BlockPos, ILogisticsTask> taskMap;
	private final Map<BlockPos, BlockState> seenStates;
	private double radius;
	
	public FarmingBlockTileEntity(BlockPos pos, BlockState state) {
		this(pos, state, 7);
	}
	
	public FarmingBlockTileEntity(BlockPos pos, BlockState state, double blockRadius) {
		super(FairyTileEntities.FarmingBlockTileEntityType, pos, state);
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
	
	private void rememberState(BlockPos pos, @Nullable BlockState state) {
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
			LogisticsTaskHarvest task = new LogisticsTaskHarvest(this.getNetworkComponent(), "Farm Harvest Task", level, base);
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
			LogisticsTaskPlantItem task = new LogisticsTaskPlantItem(this.networkComponent, "Plant Sapling", this.getSeed(level, base), level, base);
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
		
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos center = this.getBlockPos();
		final int startY = (int) Math.max(-pos.getY(), Math.floor(-radius));
		final int endY = (int) Math.min(256 - pos.getY(), Math.ceil(radius));
		for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++)
		for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++)
		for (int y = startY; y < endY; y++) {
			
			pos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
			if (!NostrumMagica.isBlockLoaded(level, pos)) {
				break; // skip this whole column
			}
			
			if (FarmingBlock.isGrownCrop(level, pos)) {
				grownCrops.add(pos.immutable());
			} else if (FarmingBlock.isPlantableSpot(level, pos.below(), this.getSeed(level, pos))) {
				emptySpots.add(pos.immutable());
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
				rememberState(base, level.getBlockState(base));
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
	public void tick() {
		if (this.level.isClientSide) {
			return;
		}
		
		this.tickCount++;
		if (this.tickCount % (20 * 10) == 0) {
			scan();
		}
	}
	
	protected static @Nonnull ItemStack ResolveSeed(@Nullable BlockState state) {
		ItemStack seeds = ItemStack.EMPTY;
		if (state != null) {
			seeds = SeedMap.get(Block.getId(state));
			if (seeds != null && !seeds.isEmpty()) {
				return seeds.copy();
			}
			if (seeds == null) {
				seeds = ItemStack.EMPTY;
			}
			
			// Try and figure out what the seed would be
			if (state.getBlock() instanceof CropBlock) {
				try {
					Method getSeed = ObfuscationReflectionHelper.findMethod(CropBlock.class, "getBaseSeedId", ItemLike.class); //getSeedItem
					seeds = new ItemStack(((ItemLike) getSeed.invoke((CropBlock) state.getBlock())).asItem());
				} catch (Exception e) {
					seeds = ItemStack.EMPTY;
				}
			}
			
			// Cache this lookup
			SeedMap.put(Block.getId(state), seeds.isEmpty() ? new ItemStack(Items.WHEAT_SEEDS) : seeds.copy());
		}
		
		return seeds;
	}
	
	// TODO make configurable!
	public @Nonnull ItemStack getSeed(Level world, BlockPos pos) {
		@Nullable BlockState state = seenStates.get(pos);
		
		// Try and figure out what the seed would be
		@Nonnull ItemStack seeds = ResolveSeed(state);
		
		// Attempt to use nearby seeds if we couldn't figure one out
		if (seeds.isEmpty()) {
			for (BlockPos nearby : new BlockPos[] {pos.north(), pos.south(), pos.east(), pos.west()}) {
				state = seenStates.get(nearby);
				if (state == null) {
					state = world.getBlockState(nearby);
				}
				
				seeds = ResolveSeed(state);
				if (!seeds.isEmpty()) {
					break;
				}
			}
		}
		
		if (seeds.isEmpty()) {
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
	
	private static final ResourceLocation SIGN_ICON = new ResourceLocation(NostrumFairies.MODID, "textures/block/logistics_farming_block_icon.png");

	@Override
	public ResourceLocation getSignIcon(IFeySign sign) {
		return SIGN_ICON;
	}
	
	@Override
	public Direction getSignFacing(IFeySign sign) {
		BlockState state = level.getBlockState(worldPosition);
		return state.getValue(FarmingBlock.FACING);
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
	}
	
	@Override
	public void setRemoved() {
		super.setRemoved();
	}
}