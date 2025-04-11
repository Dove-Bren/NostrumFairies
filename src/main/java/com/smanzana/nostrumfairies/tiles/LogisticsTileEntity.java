package com.smanzana.nostrumfairies.tiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry.ILogisticsComponentFactory;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

public abstract class LogisticsTileEntity extends BlockEntity {

	private static final String NBT_NETWORK_COMP_UUID = "lognetcomp_uuid"; 
	
	protected static final List<ItemStack> emptyList = new ArrayList<>(1);
	protected LogisticsTileEntityComponent networkComponent;
	
	public LogisticsTileEntity(BlockEntityType<? extends LogisticsTileEntity> type) {
		super(type);
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		// We STORE the UUID of our network... but only so we can communicate it to the client.
		// We hook things back up on the server when we load by position.
		nbt = super.save(nbt);
		
		if (this.networkComponent != null) {
			nbt.putUUID(NBT_NETWORK_COMP_UUID, this.networkComponent.componentID);
		}
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundTag nbt) {
		super.load(state, nbt);
		// Load in network UUID, so clients can hook things back up to fake network devices
		if (nbt.hasUUID(NBT_NETWORK_COMP_UUID)) {
//			UUID oldID = networkID;
//			UUID newID = nbt.getUniqueId(NBT_NETWORK_UUID);
//			LogisticsNetwork network = NostrumFairies.instance.getLogisticsRegistry().findNetwork(newID);
//			
//			if (oldID != null) {
//				if (newID.equals(oldID)) {
//					// just a new instance to hook up. Nothing fancy.
//					// Don't call the event methods so they can do something fancy when we actually change networks
//					this.networkID = newID;
//					this.networkCache = network;
//				} else {
//					// We got shifted to a new network?
//					this.onLeaveNetwork();
//					this.onJoinNetwork(network);
//				}
//			} else {
//				// Hooking things up for the first time
//				this.onJoinNetwork(network);
//			}
			
			UUID compID = nbt.getUUID(NBT_NETWORK_COMP_UUID);
			LogisticsTileEntityComponent comp = LogisticsTileEntityComponent.find(compID);
			
			if (comp != null) {
				setNetworkComponent(comp);
			}
		}
	}
	
	protected abstract double getDefaultLinkRange();
	
	protected abstract double getDefaultLogisticsRange();
	
	protected LogisticsTileEntityComponent makeNetworkComponent() {
		return new LogisticsTileEntityComponent(this, getDefaultLinkRange(), getDefaultLogisticsRange());
	}
	
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		this.networkComponent = component;
	}
	
    @Override
    public void clearCache() {
    	super.clearCache();
    	if (/*!world.isRemote && */ this.networkComponent == null) {
    		setNetworkComponent(makeNetworkComponent());
    		if (!level.isClientSide) {
    			NostrumFairies.instance.getLogisticsRegistry().addNewComponent(networkComponent);
    		}
		}
	}

    @Override
    public void setLevelAndPosition(Level worldIn, BlockPos pos) {
    	super.setLevelAndPosition(worldIn, pos);
    }

    @Override
    public void clearRemoved() {
    	super.clearRemoved();
    	if (level != null && this.networkComponent == null) {
    		setNetworkComponent(makeNetworkComponent());
    		if (!level.isClientSide) {
    			NostrumFairies.instance.getLogisticsRegistry().addNewComponent(networkComponent);
    		}
		}
    }
    
    public LogisticsTileEntityComponent getNetworkComponent() {
    	return this.networkComponent;
    }

	public @Nullable LogisticsNetwork getNetwork() {
		if (networkComponent != null) {
			return networkComponent.getNetwork();
		}
		
		return null;
	}
	
	public void unlinkFromNetwork() {
		LogisticsNetwork network = this.getNetwork();
		if (network != null) {
			network.removeComponent(networkComponent);
		}
	}
	
	///////////////////////////////////////////////////////////
	//  Duplicate component interface. Extend or implement these to change network interaction
	///////////////////////////////////////////////////////////
	
	public Collection<ItemStack> getItems() {
		return emptyList;
	}
	
	public boolean canAccept(List<ItemDeepStack> stacks) {
		return false;
	}
	
	public void onJoinNetwork(LogisticsNetwork network) {
		;
	}
	
	public void onLeaveNetwork() {
		;
	}
	
	public void takeItem(ItemStack stack) {
		;
	}
	
	public void addItem(ItemStack stack) {
		if (!level.isClientSide) {
			ItemEntity ent = new ItemEntity(level, worldPosition.getX() + .5, worldPosition.getY() + 1, worldPosition.getZ() + .5, stack);
			level.addFreshEntity(ent);
		}
	}
	
	public boolean isItemBuffer() {
		return false;
	}
	
	// The logistics component counterpart to the tile entity
	public static class LogisticsTileEntityComponent implements ILogisticsComponent {
		
		private static final String NBT_UUID = "uuid";
		private static final String NBT_DIM = "dim";
		private static final String NBT_POS = "pos";
		private static final String NBT_LINK_RANGE = "lrange";
		private static final String NBT_LOG_RANGE = "range";
		
		protected UUID componentID;
		private LogisticsNetwork network;
		private Level world;
		private BlockPos pos;
		private double linkRange;
		private double logisticsRange;
		private LogisticsTileEntity teCache;
		
		private LogisticsTileEntityComponent() {
			;
		}
		
		public LogisticsTileEntityComponent(LogisticsTileEntity tileEntity, double linkRange, double logisticsRange) {
			componentID = UUID.randomUUID();
			teCache = tileEntity;
			world = teCache.level;
			pos = teCache.getBlockPos();
			map.put(componentID, this);
			this.linkRange = linkRange;
			this.logisticsRange = logisticsRange;
		}
		
		public void setLinkRange(double range) {
			this.linkRange = range;
		}
		
		public void setLogisticsRange(double range) {
			this.logisticsRange = range;
		}
		
		private void refreshCache() {
			if (teCache == null) {
				teCache = (LogisticsTileEntity) world.getBlockEntity(pos);
			}
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			this.network = network;
			
			refreshCache();
			if (teCache != null) {
				teCache.onJoinNetwork(network);
			}
		}

		@Override
		public void onLeaveNetwork() {
			this.network = null;

			refreshCache();
			if (teCache != null) {
				teCache.onLeaveNetwork();
			}
		}

		@Override
		public BlockPos getPosition() {
			return this.pos;
		}
		
		@Override
		public Level getWorld() {
			return world;
		}

		@Override
		public Collection<ItemStack> getItems() {
			refreshCache();
			if (teCache != null) {
				return teCache.getItems();
			}
			return emptyList;
		}
		
		@Override
		public double getLogisticRange() {
			return logisticsRange;
		}

		@Override
		public double getLogisticsLinkRange() {
			return linkRange;
		}

		@Override
		public boolean canAccept(List<ItemDeepStack> stacks) {
			refreshCache();
			if (teCache != null) {
				return teCache.canAccept(stacks);
			}
			return false;
		}
		
		public LogisticsNetwork getNetwork() {
			return network;
		}
		
		public static final String LOGISTICS_TAG = "LogisticsTileEntityBaseAdapter"; 

		@Override
		public String getSerializationTag() {
			return "LogisticsTileEntityBaseAdapter";
		}

		@Override
		public CompoundTag toNBT() {
			CompoundTag tag = new CompoundTag();
			
			tag.putUUID(NBT_UUID, componentID);
			tag.put(NBT_POS, NbtUtils.writeBlockPos(pos));
			tag.putString(NBT_DIM, this.world.dimension().location().toString());
			tag.putDouble(NBT_LINK_RANGE, linkRange);
			tag.putDouble(NBT_LOG_RANGE, logisticsRange);
			
			return tag;
		}
		
		protected static LogisticsTileEntityComponent loadFromNBT(CompoundTag nbt, LogisticsNetwork network) {
			BlockPos pos = NbtUtils.readBlockPos(nbt.getCompound(NBT_POS));
			Level world = NostrumFairies.getWorld(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString(NBT_DIM))));
			UUID compID = nbt.getUUID(NBT_UUID);
			double linkRange = nbt.getDouble(NBT_LINK_RANGE);
			double logisticsRange = nbt.getDouble(NBT_LOG_RANGE);
			
			if (world == null) {
				throw new RuntimeException("Failed to find world for persisted TileEntity logistics component: "
						+ nbt.getString(NBT_DIM));
			}
			
			LogisticsTileEntityComponent comp = new LogisticsTileEntityComponent();
			comp.componentID = compID;
			comp.world = world;
			comp.pos = pos;
			comp.linkRange = linkRange;
			comp.logisticsRange = logisticsRange;
			
			map.put(compID, comp);
			
			comp.network = network;
			return comp;
		}
		
		public static class ComponentFactory implements ILogisticsComponentFactory<LogisticsTileEntityComponent> {
			@Override
			public LogisticsTileEntityComponent construct(CompoundTag nbt, LogisticsNetwork network) {
				return loadFromNBT(nbt, network);
			}
		}
		
		private static Map<UUID, LogisticsTileEntityComponent> map = new HashMap<>();
		
		public static LogisticsTileEntityComponent find(UUID componentID) {
			return map.get(componentID);
		}

		@Override
		public void takeItem(ItemStack stack) {
			refreshCache();
			if (teCache != null) {
				teCache.takeItem(stack);
			}
		}

		@Override
		public void addItem(ItemStack stack) {
			refreshCache();
			if (teCache != null) {
				teCache.addItem(stack);
			}
		}
		
		public @Nullable BlockEntity getTileEntity() {
			refreshCache();
			return teCache;
		}

		@Override
		public boolean isItemBuffer() {
			refreshCache();
			if (teCache != null) {
				return teCache.isItemBuffer();
			}
			
			return false;
		}

	}
	
}
