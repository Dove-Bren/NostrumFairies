package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class LogisticsTileEntity extends TileEntity implements ILogisticsComponent {

	private static final String NBT_NETWORK_UUID = "lognet_uuid"; 
	
	protected static final List<ItemStack> emptyList = new ArrayList<>(1);
	protected LogisticsNetwork network;
	protected UUID networkID;
	
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
		
		if (this.network != null) {
			nbt.setUniqueId(NBT_NETWORK_UUID, this.network.getUUID());
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		// Load in network UUID, so clients can hook things back up to fake network devices
		if (nbt.hasUniqueId(NBT_NETWORK_UUID)) {
			networkID = nbt.getUniqueId(NBT_NETWORK_UUID);
			LogisticsNetwork network = NostrumFairies.instance.getLogisticsRegistry().findNetwork(networkID);
			
			if (this.network != null) {
				if (networkID.equals(this.network.getUUID())) {
					// just a new instance to hook up. Nothing fancy.
					// Don't call the event methods so they can do something fancy when we actually change networks
					this.network = network;
				} else {
					// We got shifted to a new network?
					this.onLeaveNetwork();
					this.onJoinNetwork(network);
				}
			} else {
				// Hooking things up for the first time
				this.onJoinNetwork(network);
			}
		}
		super.readFromNBT(nbt);
	}
	
    @Override
    public void updateContainingBlockInfo() {
    	super.updateContainingBlockInfo();
    	if (!worldObj.isRemote && this.network == null) {
			NostrumFairies.instance.getLogisticsRegistry().addNewComponent(this);
		}
    }

	@Override
	public void onJoinNetwork(LogisticsNetwork network) {
		this.network = network;
		this.markDirty();
	}

	@Override
	public void onLeaveNetwork() {
		this.network = null;
		this.markDirty();
		// Might need to look for a new hookup here?
	}

	@Override
	public BlockPos getPosition() {
		return this.pos;
	}

	@Override
	public Collection<ItemStack> getItems() {
		return emptyList;
	}
	
	private static final String NBT_LOG_POS = "pos";
	private static final String NBT_LOG_DIM = "dim";

	protected NBTTagCompound baseToNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.setLong(NBT_LOG_POS, this.pos.toLong());
		tag.setInteger(NBT_LOG_DIM, this.worldObj.provider.getDimension());
		
		return tag;
	}
	
	protected static LogisticsTileEntity loadFromNBT(NBTTagCompound nbt, LogisticsNetwork network) {
		// We store the TE position. Hook back up!
		BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_LOG_POS));
		World world = NostrumFairies.getWorld(nbt.getInteger(NBT_LOG_DIM));
		
		if (world == null) {
			throw new RuntimeException("Failed to find world for persisted TileEntity logistics component: "
					+ nbt.getInteger(NBT_LOG_DIM));
		}
		
		TileEntity te = world.getTileEntity(pos);
		
		if (te == null) {
			throw new RuntimeException("Failed to lookup tile entity at persisted location: "
					+ pos);
		}
		
		LogisticsTileEntity chest = (LogisticsTileEntity) te;
		chest.network = network;
		return chest;
	}
	
	public @Nullable LogisticsNetwork getNetwork() {
		return network;
	}
	
}
