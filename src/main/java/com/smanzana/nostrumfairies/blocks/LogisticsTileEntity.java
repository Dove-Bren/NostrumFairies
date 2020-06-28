package com.smanzana.nostrumfairies.blocks;

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
			setNetworkComponent(LogisticsTileEntityComponent.find(compID));
		}
		super.readFromNBT(nbt);
	}
	
	protected LogisticsTileEntityComponent makeNetworkComponent() {
		return new LogisticsTileEntityComponent(this);
	}
	
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		this.networkComponent = component;
	}
	
    @Override
    public void updateContainingBlockInfo() {
    	super.updateContainingBlockInfo();
    	if (!worldObj.isRemote && this.networkComponent == null) {
    		setNetworkComponent(makeNetworkComponent());
    		NostrumFairies.instance.getLogisticsRegistry().addNewComponent(networkComponent);
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
	
	protected void unlinkFromNetwork() {
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
	
	public abstract double getLogisticRange();

	public abstract double getLogisticsLinkRange();

	public boolean canAccept(ItemStack stack) {
		return false;
	}
	
	public void onJoinNetwork(LogisticsNetwork network) {
		;
	}
	
	public void onLeaveNetwork() {
		;
	}
	
	// The logistics component counterpart to the tile entity
	public static class LogisticsTileEntityComponent implements ILogisticsComponent {
		
		private static final String NBT_UUID = "uuid";
		private static final String NBT_DIM = "dim";
		private static final String NBT_POS = "pos";
		
		protected UUID componentID;
		private LogisticsNetwork network;
		private World world;
		private BlockPos pos;
		private LogisticsTileEntity teCache;
		
		private LogisticsTileEntityComponent() {
			;
		}
		
		public LogisticsTileEntityComponent(LogisticsTileEntity tileEntity) {
			componentID = UUID.randomUUID();
			teCache = tileEntity;
			world = teCache.worldObj;
			pos = teCache.getPos();
			map.put(componentID, this);
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
			refreshCache();
			if (teCache != null) {
				return teCache.getLogisticRange();
			}
			return 0;
		}

		@Override
		public double getLogisticsLinkRange() {
			refreshCache();
			if (teCache != null) {
				return teCache.getLogisticsLinkRange();
			}
			return 0;
		}

		@Override
		public boolean canAccept(ItemStack stack) {
			refreshCache();
			if (teCache != null) {
				return teCache.canAccept(stack);
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
			
			return tag;
		}
		
		protected static LogisticsTileEntityComponent loadFromNBT(NBTTagCompound nbt, LogisticsNetwork network) {
			BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_POS));
			World world = NostrumFairies.getWorld(nbt.getInteger(NBT_DIM));
			UUID compID = nbt.getUniqueId(NBT_UUID);
			
			if (world == null) {
				throw new RuntimeException("Failed to find world for persisted TileEntity logistics component: "
						+ nbt.getInteger(NBT_DIM));
			}
			
			LogisticsTileEntityComponent comp = new LogisticsTileEntityComponent();
			comp.componentID = compID;
			comp.world = world;
			comp.pos = pos;
			
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

	}
	
}
