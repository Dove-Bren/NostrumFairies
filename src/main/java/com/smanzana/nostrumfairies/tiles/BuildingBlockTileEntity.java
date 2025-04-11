package com.smanzana.nostrumfairies.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.BuildingBlock;
import com.smanzana.nostrumfairies.blocks.IFeySign;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskBuildBlock;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BuildingBlockTileEntity extends LogisticsTileEntity implements TickableBlockEntity,  ILogisticsTaskListener, IFeySign {

	private static final String NBT_SLOT = "itemslot";
	
	private int tickCount;
	private Map<BlockPos, ILogisticsTask> taskMap;
	private double radius;
	private ItemStack slot = ItemStack.EMPTY;
	private int scanCounter;
	
	public BuildingBlockTileEntity() {
		this(16);
	}
	
	public BuildingBlockTileEntity(double blockRadius) {
		super(FairyTileEntities.BuildingBlockTileEntityType);
		taskMap = new HashMap<>();
		this.radius = blockRadius;
		
		MinecraftForge.EVENT_BUS.register(this);
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
	
	private void makePlaceTask(BlockPos base, BlockState missingState) {
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		if (!taskMap.containsKey(base)) {
			@Nonnull ItemStack item = TemplateBlock.GetRequiredItem(missingState);
			LogisticsTaskBuildBlock task = new LogisticsTaskBuildBlock(this.getNetworkComponent(), "Repair Task",
					item, missingState,
					level, base);
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
	
	private void scanBlueprint() {
		if (this.getNetwork() == null) {
			return;
		}
		
		TemplateBlueprint blueprint = TemplateScroll.GetTemplate(slot);
		if (blueprint == null) {
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		
		List<BlockPos> blocks = blueprint.spawn(level, worldPosition, Direction.NORTH);
		if (!blocks.isEmpty()) {
			for (BlockPos pos : blocks) {
				this.makePlaceTask(pos, TemplateBlock.GetTemplatedState(level, pos));
			}
		}
		
		final long end = System.currentTimeMillis();
		if (end - startTime >= 5) {
			System.out.println("Took " + (end - startTime) + "ms to scan for blueprint changes!");
		}
	}
	
	private void scan(int y) {
		if (this.getNetwork() == null) {
			return;
		}
		
		final BlockPos center = this.getBlockPos();
		
		if (center.getY() + y < 0 || center.getY() + y > 255) {
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		
		Set<BlockPos> known = Sets.newHashSet(taskMap.keySet());
		List<BlockPos> templateSpots = new LinkedList<>();
		
		final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++)
		for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++) {
			cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
			if (!NostrumMagica.isBlockLoaded(level, cursor)) {
				break; // skip this whole column
			}
			
			BlockState state = level.getBlockState(cursor);
			if (state != null && state.getBlock() instanceof TemplateBlock) {
				templateSpots.add(cursor.immutable());
			}
		}
		
		for (BlockPos base : templateSpots) {
			if (known.remove(base)) {
				; // We already knew about it, so don't create a new one
			} else {
				// Didn't know, so record!
				// Don't make task cause we filter better later
				makePlaceTask(base, TemplateBlock.GetTemplatedState(level, base));
			}
		}
		
		// For any left in known, the template spot is not there anymore! Remove!
		for (BlockPos base : known) {
			if (base.getY() - center.getY() == y) {
				removeTask(base);
			}
		}
		
		final long end = System.currentTimeMillis();
		if (end - startTime >= 5) {
			System.out.println("Took " + (end - startTime) + "ms to scan for lost templates!");
		}
	}
	
	@Override
	public void tick() {
		if (this.level.isClientSide) {
			return;
		}
		
		this.tickCount++;
		if (this.tickCount % (20 * 2) == 0) {
			// want to scan y -radius to radius, but want to start at 0, 1, -1, 2, -3...
			// Hits 0 twice and is <radius, not <= radius...
			final int idx = (int) (scanCounter++ % (radius * 2));
			final int y = (idx / 2) * (idx % 2 == 1 ? -1 : 1);
			//final int y = (int) ((scanCounter++ % (radius * 2)) - radius);
			
			scan(y);
		}
		if (!this.slot.isEmpty() && this.tickCount % 10 == 0) {
			scanBlueprint();
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
		if (task instanceof LogisticsTaskBuildBlock) {
			LogisticsTaskBuildBlock placeTask = (LogisticsTaskBuildBlock) task;
			BlockPos pos = placeTask.getTargetBlock();
			taskMap.remove(pos);
		}
	}
	
	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Detonate event) {
		if (event.isCanceled() || !event.getWorld().equals(level)) {
			return;
		}
		
		List<BlockPos> positions = new ArrayList<>();
		List<BlockState> states = new ArrayList<>();
		for (BlockPos loc : event.getAffectedBlocks()) {
			if (loc.equals(worldPosition)) {
				// We got blown up
				return;
			}
			
			if (Math.abs(worldPosition.getX() - loc.getX()) < radius
					|| Math.abs(worldPosition.getY() - loc.getY()) < radius
					|| Math.abs(worldPosition.getZ() - loc.getZ()) < radius) {
				positions.add(loc);
				states.add(level.getBlockState(loc));
			}
		}
		
		if (!positions.isEmpty()) {
			NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
				for (int i = 0; i < positions.size(); i++) {
					if (level.isEmptyBlock(positions.get(i))) {
						TemplateBlock.SetTemplate(level, positions.get(i), states.get(i));
					}
				}
				
				// Remove us from the listener
				return true;
			}, 1, 1);
		}
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		
		if (this.slot != null) {
			nbt.put(NBT_SLOT, slot.serializeNBT());
		}
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundTag nbt) {
		super.load(state, nbt);
		
		this.slot = ItemStack.of(nbt.getCompound(NBT_SLOT));
	}
	
	public Container getInventory() {
		BuildingBlockTileEntity self = this;
		Container inv = new SimpleContainer(1) {
			
			@Override
			public void setChanged() {
				if (!slot.isEmpty() && this.getItem(0).isEmpty() && !self.level.isClientSide) {
					System.out.println("Clearing item");
				}
				
				slot = this.getItem(0);
				super.setChanged();
				self.setChanged();
				BlockState state = self.level.getBlockState(worldPosition);
				self.level.sendBlockUpdated(worldPosition, state, state, 2);
			}
			
			@Override
			public boolean canPlaceItem(int index, ItemStack stack) {
				return index == 0 && (stack.isEmpty() || stack.getItem() instanceof TemplateScroll);
			}
		};
		
		inv.setItem(0, slot);
		return inv;
	}
	
	private static final ResourceLocation SIGN_ICON = new ResourceLocation(NostrumFairies.MODID, "textures/block/logistics_building_block_icon.png");

	@Override
	public ResourceLocation getSignIcon(IFeySign sign) {
		return SIGN_ICON;
	}
	
	@Override
	public Direction getSignFacing(IFeySign sign) {
		BlockState state = level.getBlockState(worldPosition);
		return state.getValue(BuildingBlock.FACING);
	}
	
	@Override
	public void setRemoved() {
		super.setRemoved();
	}
	
	public @Nonnull ItemStack getTemplateScroll() {
		return this.slot;
	}
}