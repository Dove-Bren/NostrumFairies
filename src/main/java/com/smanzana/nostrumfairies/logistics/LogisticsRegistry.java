package com.smanzana.nostrumfairies.logistics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Has a serialized master list of all current logistics networks.
 * @author Skyler
 *
 */
public class LogisticsRegistry extends WorldSavedData {
	
	public static final String DATA_NAME =  NostrumFairies.MODID + "_LogisticsNetwork";
	private static final String NBT_NETWORKS = "networks";
	
	private Set<LogisticsNetwork> networks; // persisted
	
	public LogisticsRegistry() {
		this(DATA_NAME);
	}
	
	public LogisticsRegistry(String name) {
		super(name);
		
		this.networks = new HashSet<>();
	}

	@Override
	public void read(CompoundNBT nbt) {
		ListNBT list = nbt.getList(NBT_NETWORKS, NBT.TAG_COMPOUND);
		for (int i = list.size() - 1; i >= 0; i--) {
			CompoundNBT compound = list.getCompound(i);
			this.networks.add(LogisticsNetwork.fromNBT(compound));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (compound == null) {
			compound = new CompoundNBT();
		}
		
		if (!this.networks.isEmpty()) {
			ListNBT list = new ListNBT();
			for (LogisticsNetwork network : this.networks) {
				list.add(network.toNBT());
			}
			compound.put(NBT_NETWORKS, list);
		}
		
		return compound;
	}
	
	public void addNetwork(LogisticsNetwork network) {
		this.networks.add(network);
		this.markDirty();
	}
	
	public void removeNetwork(LogisticsNetwork network) {
		this.networks.remove(network);
		this.markDirty();
	}
	
	public @Nullable LogisticsNetwork findNetwork(UUID id) {
		for (LogisticsNetwork network : this.networks) {
			if (network.getUUID().equals(id)) {
				return network;
			}
		}
		
		return null;
	}
	
	public @Nullable LogisticsNetwork findNetwork(ILogisticsComponent component) {
		for (LogisticsNetwork network : this.networks) {
			if (network.components.contains(component)) {
				return network;
			}
		}
		
		return null;
	}
	
	public @Nullable LogisticsNetwork findNetwork(World world, BlockPos pos) {
		for (LogisticsNetwork network : this.networks) {
			for (ILogisticsComponent comp : network.components) {
				if (comp.getWorld().equals(world) && pos.equals(comp.getPosition())) {
					return network;
				}
			}
		}
		
		return null;
	}
	
	public Collection<LogisticsNetwork> getNetworks() {
		return networks;
	}
	
	public @Nullable LogisticsNetwork getLogisticsNetworkFor(World world, BlockPos pos) {
		for (LogisticsNetwork network : this.networks) {
			if (network.getLogisticsFor(world, pos) != null) {
				return network;
			}
		}
		
		return null;
	}
	
	public void getLogisticsNetworksFor(World world, BlockPos pos, Collection<LogisticsNetwork> networks) {
		networks.clear();
		for (LogisticsNetwork network : this.networks) {
			if (network.getLogisticsFor(world, pos) != null) {
				networks.add(network);
			}
		}
	}
	
	/**
	 * Either adds the component to an existing network (if one is linkable) or creates a new one.
	 * When a component is placed down, they should call this function.
	 * @param component
	 */
	public void addNewComponent(ILogisticsComponent component) {
		LogisticsNetwork firstNetwork = null;
		
		Set<LogisticsNetwork> originalSet = Sets.newHashSet(this.networks);
		for (LogisticsNetwork network : originalSet) {
			if (firstNetwork == null) {
				if (network.addComponent(component)) {
					firstNetwork = network;
				}				
			} else {
				// We've already added ourselves to a network. If another network could attach,
				// combine the networks!
				if (network.canLinkReach(component)) {
					firstNetwork.mergeNetworkIn(network); // This may modify our list of networks!
				}
			}
		}
		
		if (firstNetwork != null) {
			// At least one network was found and joined.
			return;
		}
		
		// No networks found. Make a new one!
		firstNetwork = new LogisticsNetwork();
		firstNetwork.addComponent(component);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void injectNetwork(LogisticsNetwork network) {
		LogisticsNetwork existing = this.findNetwork(network.getUUID());
		if (existing != null) {
			this.networks.remove(existing);
			existing.dissolveNetwork();
		}
		
		this.networks.add(network);
	}

	@OnlyIn(Dist.CLIENT)
	public void clear() {
		for (LogisticsNetwork network : networks) {
			network.dissolveNetwork();
		}
		this.networks.clear();
	}
	
}
