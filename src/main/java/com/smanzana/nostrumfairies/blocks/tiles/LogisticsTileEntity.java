package com.smanzana.nostrumfairies.blocks.tiles;

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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class LogisticsTileEntity extends TileEntity {

	private static final String NBT_NETWORK_COMP_UUID = "lognetcomp_uuid"; 
	
	protected static final List<ItemStack> emptyList = new ArrayList<>(1);
	protected LogisticsTileEntityComponent networkComponent;
	
	public LogisticsTileEntity() {
		super();
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		// We STORE the UUID of our network... but only so we can communicate it to the client.
		// We hook things back up on the server when we load by position.
		nbt = super.writeToNBT(nbt);
		
		if (this.networkComponent != null) {
			nbt.setUniqueId(NBT_NETWORK_COMP_UUID, this.networkComponent.componentID);
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		// Load in network UUID, so clients can hook things back up to fake network devices
		if (nbt.hasUniqueId(NBT_NETWORK_COMP_UUID)) {
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
			
			UUID compID = nbt.getUniqueId(NBT_NETWORK_COMP_UUID);
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
    public void updateContainingBlockInfo() {
    	super.updateContainingBlockInfo();
    	if (/*!world.isRemote && */ this.networkComponent == null) {
    		setNetworkComponent(makeNetworkComponent());
    		if (!world.isRemote) {
    			NostrumFairies.instance.getLogisticsRegistry().addNewComponent(networkComponent);
    		}
		}
	}

    @Override
    public void setWorldObj(World worldIn) {
    	super.setWorldObj(worldIn);
    	// Sometimes the position isn't set yet >:(
//    	if (/*!world.isRemote && */ this.networkComponent == null) {
//    		setNetworkComponent(makeNetworkComponent());
//    		if (!world.isRemote) {
//    			NostrumFairies.instance.getLogisticsRegistry().addNewComponent(networkComponent);
//    		}
//		}
    }

    @Override
    public void validate() {
    	super.validate();
    	if (world != null && this.networkComponent == null) {
    		setNetworkComponent(makeNetworkComponent());
    		if (!world.isRemote) {
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
		if (!world.isRemote) {
			EntityItem ent = new EntityItem(world, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, stack);
			world.spawnEntity(ent);
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
		private World world;
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
			world = teCache.world;
			pos = teCache.getPos();
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
				teCache = (LogisticsTileEntity) world.getTileEntity(pos);
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
		public World getWorld() {
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
		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			
			tag.setUniqueId(NBT_UUID, componentID);
			tag.setLong(NBT_POS, this.pos.toLong());
			tag.setInteger(NBT_DIM, this.world.provider.getDimension());
			tag.setDouble(NBT_LINK_RANGE, linkRange);
			tag.setDouble(NBT_LOG_RANGE, logisticsRange);
			
			return tag;
		}
		
		protected static LogisticsTileEntityComponent loadFromNBT(NBTTagCompound nbt, LogisticsNetwork network) {
			BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_POS));
			World world = NostrumFairies.getWorld(nbt.getInteger(NBT_DIM));
			UUID compID = nbt.getUniqueId(NBT_UUID);
			double linkRange = nbt.getDouble(NBT_LINK_RANGE);
			double logisticsRange = nbt.getDouble(NBT_LOG_RANGE);
			
			if (world == null) {
				throw new RuntimeException("Failed to find world for persisted TileEntity logistics component: "
						+ nbt.getInteger(NBT_DIM));
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
			public LogisticsTileEntityComponent construct(NBTTagCompound nbt, LogisticsNetwork network) {
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
		
		public @Nullable TileEntity getTileEntity() {
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
