package com.smanzana.nostrumfairies.tiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.GatheringBlock;
import com.smanzana.nostrumfairies.blocks.IFeySign;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork.ILogisticsTaskUniqueData;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPickupItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GatheringBlockTileEntity extends LogisticsTileEntity implements ITickableTileEntity, ILogisticsTaskListener, IFeySign {
	
	protected static final ILogisticsTaskUniqueData<ItemEntity> GATHERING_ITEM = new ILogisticsTaskUniqueData<ItemEntity>() { };

	private int tickCount;
	private Map<ItemEntity, LogisticsTaskPickupItem> taskMap;
	private double radius;
	
	private AxisAlignedBB boxCache;
	private double radiusCache;
	
	public GatheringBlockTileEntity() {
		this(10);
	}
	
	public GatheringBlockTileEntity(double blockRadius) {
		super(FairyTileEntities.GatheringBlockTileEntityType);
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
	
	private void makeTask(ItemEntity item) {
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
	
	private void removeTask(ItemEntity item) {
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
		List<ItemEntity> items = this.world.getEntitiesWithinAABB(ItemEntity.class, boxCache);
		Set<ItemEntity> known = Sets.newHashSet(taskMap.keySet());
		for (ItemEntity item : items) {
			if (known.remove(item)) {
				// we knew about that item.
				continue;
			}
			
			// else this is an unknown item
			makeTask(item);
		}
		
		// For any left in known, the item is not there anymore! Remove!
		for (ItemEntity item : known) {
			// Ignore any tasks that don't have entities anymore but that's because the worker
			// picked it up
			if (taskMap.get(item).hasTakenItems()) {
				continue;
			}
			
			removeTask(item);
		}
	}
	
	@Override
	public void tick() {
		if (this.world.isRemote) {
			return;
		}
		
		this.tickCount++;
		if (this.tickCount % 20 == 0) {
			scan();
		}
	}
	
	private boolean inRange(ItemEntity e) {
		// Max distance with a square radius of X is X times sqrt(3).
		// sqrt(3) is ~1.7321
		
		// Idk if this is actually faster than 6 conditionals.
		return (e.world == this.world &&
				this.getDistanceSq(e.posX, e.posY, e.posZ) <=
					Math.pow(radius * 1.7321, 2));
	}
	
	@SubscribeEvent
	public void onPickup(ItemPickupEvent e) {
		 if (inRange(e.getOriginalEntity())) {
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
			this.removeTask(pickup.getItemEntity());
		}
	}
	
	private static final ResourceLocation SIGN_ICON = new ResourceLocation(NostrumFairies.MODID, "textures/block/logistics_gathering_block_icon.png");

	@Override
	public ResourceLocation getSignIcon(IFeySign sign) {
		return SIGN_ICON;
	}
	
	@Override
	public Direction getSignFacing(IFeySign sign) {
		BlockState state = world.getBlockState(pos);
		return state.get(GatheringBlock.FACING);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		
		if (this.world != null && this.world.isRemote) {
			StaticTESRRenderer.instance.update(world, pos, this);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		if (world != null && world.isRemote) {
			StaticTESRRenderer.instance.update(world, pos, null);
		}
	}
}