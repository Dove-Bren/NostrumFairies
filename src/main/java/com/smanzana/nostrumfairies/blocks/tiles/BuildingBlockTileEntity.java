package com.smanzana.nostrumfairies.blocks.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.blocks.BuildingBlock;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskBuildBlock;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BuildingBlockTileEntity extends LogisticsTileEntity implements ITickable,  ILogisticsTaskListener, IFeySign {

	private static final String NBT_SLOT = "itemslot";
	
	private int tickCount;
	private Map<BlockPos, ILogisticsTask> taskMap;
	private double radius;
	private ItemStack slot;
	private int scanCounter;
	
	public BuildingBlockTileEntity() {
		this(16);
	}
	
	public BuildingBlockTileEntity(double blockRadius) {
		super();
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
	
	private void makePlaceTask(BlockPos base, IBlockState missingState) {
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		if (!taskMap.containsKey(base)) {
			ItemStack item = TemplateBlock.GetRequiredItem(missingState);
			LogisticsTaskBuildBlock task = new LogisticsTaskBuildBlock(this.getNetworkComponent(), "Repair Task",
					item, missingState,
					world, base);
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
		
		List<BlockPos> blocks = blueprint.spawn(world, pos, EnumFacing.NORTH);
		if (!blocks.isEmpty()) {
			for (BlockPos pos : blocks) {
				this.makePlaceTask(pos, TemplateBlock.GetTemplatedState(world, pos));
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
		
		final BlockPos center = this.getPos();
		
		if (center.getY() + y < 0 || center.getY() + y > 255) {
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		
		Set<BlockPos> known = Sets.newHashSet(taskMap.keySet());
		List<BlockPos> templateSpots = new LinkedList<>();
		
		final MutableBlockPos cursor = new MutableBlockPos();
		for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++)
		for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++) {
			cursor.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
			if (!world.isBlockLoaded(cursor)) {
				break; // skip this whole column
			}
			
			IBlockState state = world.getBlockState(cursor);
			if (state != null && state.getBlock() instanceof TemplateBlock) {
				templateSpots.add(cursor.toImmutable());
			}
		}
		
		for (BlockPos base : templateSpots) {
			if (known.remove(base)) {
				; // We already knew about it, so don't create a new one
			} else {
				// Didn't know, so record!
				// Don't make task cause we filter better later
				makePlaceTask(base, TemplateBlock.GetTemplatedState(world, base));
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
	public void update() {
		if (this.world.isRemote) {
			return;
		}
		
		this.tickCount++;
		if (this.tickCount % (20 * 2) == 0) {
			final int y = (int) ((scanCounter++ % (radius * 2)) - radius);
			scan(y);
		}
		if (this.slot != null && this.tickCount % 10 == 0) {
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
		if (event.isCanceled() || !event.getWorld().equals(world)) {
			return;
		}
		
		List<BlockPos> positions = new ArrayList<>();
		List<IBlockState> states = new ArrayList<>();
		for (BlockPos loc : event.getAffectedBlocks()) {
			if (loc.equals(pos)) {
				// We got blown up
				return;
			}
			
			if (Math.abs(pos.getX() - loc.getX()) < radius
					|| Math.abs(pos.getY() - loc.getY()) < radius
					|| Math.abs(pos.getZ() - loc.getZ()) < radius) {
				positions.add(loc);
				states.add(world.getBlockState(loc));
			}
		}
		
		if (!positions.isEmpty()) {
			NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
				for (int i = 0; i < positions.size(); i++) {
					if (world.isAirBlock(positions.get(i))) {
						TemplateBlock.SetTemplate(world, positions.get(i), states.get(i));
					}
				}
				
				// Remove us from the listener
				return true;
			}, 1, 1);
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		if (this.slot != null) {
			nbt.setTag(NBT_SLOT, slot.serializeNBT());
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		this.slot = new ItemStack(nbt.getCompoundTag(NBT_SLOT));
		
		if (this.world != null && this.world.isRemote) {
			StaticTESRRenderer.instance.update(world, pos, this);
		}
	}
	
	public IInventory getInventory() {
		BuildingBlockTileEntity self = this;
		IInventory inv = new InventoryBasic("BuildingBlockInv", false, 1) {
			
			@Override
			public void markDirty() {
				if (slot != null && this.getStackInSlot(0) == null && !self.world.isRemote) {
					System.out.println("Clearing item");
				}
				
				slot = this.getStackInSlot(0);
				super.markDirty();
				self.markDirty();
				IBlockState state = self.world.getBlockState(pos);
				self.world.notifyBlockUpdate(pos, state, state, 2);
			}
			
			@Override
			public boolean isItemValidForSlot(int index, ItemStack stack) {
				return index == 0 && (stack == null || stack.getItem() instanceof TemplateScroll);
			}
		};
		
		inv.setInventorySlotContents(0, slot);
		return inv;
	}
	
	private static final ItemStack SIGN_ICON = new ItemStack(Blocks.BRICK_BLOCK);

	@Override
	public ItemStack getSignIcon(IFeySign sign) {
		return SIGN_ICON;
	}
	
	@Override
	public EnumFacing getSignFacing(IFeySign sign) {
		IBlockState state = world.getBlockState(pos);
		return state.getValue(BuildingBlock.FACING);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if (world != null && world.isRemote) {
			StaticTESRRenderer.instance.update(world, pos, null);
		}
	}
	
	public @Nullable ItemStack getTemplateScroll() {
		return this.slot;
	}
}