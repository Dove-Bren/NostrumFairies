package com.smanzana.nostrumfairies.blocks.tiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.blocks.GatheringBlock;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.ILogisticsTaskUniqueData;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPickupItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.items.ReagentBag;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GatheringBlockTileEntity extends LogisticsTileEntity implements ITickable, ILogisticsTaskListener, IFeySign {
	
	protected static final ILogisticsTaskUniqueData<EntityItem> GATHERING_ITEM = new ILogisticsTaskUniqueData<EntityItem>() { };

	private int tickCount;
	private Map<EntityItem, LogisticsTaskPickupItem> taskMap;
	private double radius;
	
	private AxisAlignedBB boxCache;
	private double radiusCache;
	
	public GatheringBlockTileEntity() {
		this(10);
	}
	
	public GatheringBlockTileEntity(double blockRadius) {
		super();
		taskMap = new HashMap<>();
		this.radius = blockRadius;
	}
	
	@Override
	public double getDefaultLogisticsRange() {
		return 10;
	}

	@Override
	public double getDefaultLinkRange() {
		return 10;
	}

	@Override
	public boolean canAccept(List<ItemDeepStack> stacks) {
		return false;
	}
	
	private void makeTask(EntityItem item) {
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		if (network.taskDataAdd(GATHERING_ITEM, item)) {
			LogisticsTaskPickupItem task = new LogisticsTaskPickupItem(this.getNetworkComponent(), "Item Pickup Task", item);
			this.taskMap.put(item, task);
			network.getTaskRegistry().register(task, this);
		}
	}
	
	private void removeTask(EntityItem item) {
		LogisticsTaskPickupItem task = taskMap.remove(item);
		if (task == null) {
			// wut
			return;
		}
		
		LogisticsNetwork network = this.getNetwork();
		if (network == null) {
			return;
		}
		
		network.getTaskRegistry().revoke(task);
		network.taskDataRemove(GATHERING_ITEM, item);
	}
	
	private void scan() {
		// Update BB cache if needed
		if (boxCache == null || radiusCache != radius) {
			boxCache = new AxisAlignedBB(this.pos).grow(radius);
			this.radiusCache = radius;
		}
		
		if (this.getNetwork() == null) {
			return;
		}
		
		// Check items on the ground nearby and create/destroy any tasks needed
		List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, boxCache);
		Set<EntityItem> known = Sets.newHashSet(taskMap.keySet());
		for (EntityItem item : items) {
			if (known.remove(item)) {
				// we knew about that item.
				continue;
			}
			
			// else this is an unknown item
			makeTask(item);
		}
		
		// For any left in known, the item is not there anymore! Remove!
		for (EntityItem item : known) {
			// Ignore any tasks that don't have entities anymore but that's because the worker
			// picked it up
			if (taskMap.get(item).hasTakenItems()) {
				continue;
			}
			
			removeTask(item);
		}
	}
	
	@Override
	public void update() {
		if (this.world.isRemote) {
			return;
		}
		
		this.tickCount++;
		if (this.tickCount % 20 == 0) {
			scan();
		}
	}
	
	private boolean inRange(EntityItem e) {
		// Max distance with a square radius of X is X times sqrt(3).
		// sqrt(3) is ~1.7321
		
		// Idk if this is actually faster than 6 conditionals.
		return (e.world == this.world &&
				this.getDistanceSq(e.posX, e.posY, e.posZ) <=
					Math.pow(radius * 1.7321, 2));
	}
	
	@SubscribeEvent
	public void onPickup(EntityItemPickupEvent e) {
		 if (inRange(e.getItem())) {
			 this.scan();
		 }
	}
	
	@SubscribeEvent
	public void onToss(ItemTossEvent e) {
		 if (inRange(e.getEntityItem())) {
			 this.scan();
		 }
	}
	
	@SubscribeEvent
	public void onExpire(ItemExpireEvent e) {
		 if (inRange(e.getEntityItem())) {
			 this.scan();
		 }
	}
	
	@Override
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		if (!worldIn.isRemote) {
			MinecraftForge.EVENT_BUS.register(this);
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
		if (task != null && task instanceof LogisticsTaskPickupItem) {
			LogisticsTaskPickupItem pickup = (LogisticsTaskPickupItem) task;
			this.removeTask(pickup.getEntityItem());
		}
	}
	
	private static final ItemStack SIGN_ICON = new ItemStack(ReagentBag.instance());

	@Override
	public ItemStack getSignIcon(IFeySign sign) {
		return SIGN_ICON;
	}
	
	@Override
	public EnumFacing getSignFacing(IFeySign sign) {
		IBlockState state = world.getBlockState(pos);
		return state.getValue(GatheringBlock.FACING);
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