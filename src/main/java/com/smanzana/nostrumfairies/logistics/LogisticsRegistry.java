package com.smanzana.nostrumfairies.logistics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Has a serialized master list of all current logistics networks.
 * @author Skyler
 *
 */
public class LogisticsRegistry extends SavedData {
	
	public static final String DATA_NAME =  NostrumFairies.MODID + "_LogisticsNetwork";
	private static final String NBT_NETWORKS = "networks";
	
	private Set<LogisticsNetwork> networks; // persisted
	
	public LogisticsRegistry() {
		super();
		
		this.networks = new HashSet<>();
	}

	public void load(CompoundTag nbt) {
		ListTag list = nbt.getList(NBT_NETWORKS, Tag.TAG_COMPOUND);
		for (int i = list.size() - 1; i >= 0; i--) {
			CompoundTag compound = list.getCompound(i);
			this.networks.add(LogisticsNetwork.fromNBT(compound));
		}
	}
	
	public static LogisticsRegistry Load(CompoundTag nbt) {
		LogisticsRegistry reg = new LogisticsRegistry();
		reg.load(nbt);
		return reg;
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		if (compound == null) {
			compound = new CompoundTag();
		}
		
		if (!this.networks.isEmpty()) {
			ListTag list = new ListTag();
			for (LogisticsNetwork network : this.networks) {
				list.add(network.toNBT());
			}
			compound.put(NBT_NETWORKS, list);
		}
		
		return compound;
	}
	
	public void addNetwork(LogisticsNetwork network) {
		this.networks.add(network);
		this.setDirty();
	}
	
	public void removeNetwork(LogisticsNetwork network) {
		this.networks.remove(network);
		this.setDirty();
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
	
	public @Nullable LogisticsNetwork findNetwork(Level world, BlockPos pos) {
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
	
	public @Nullable LogisticsNetwork getLogisticsNetworkFor(Level world, BlockPos pos) {
		for (LogisticsNetwork network : this.networks) {
			if (network.getLogisticsFor(world, pos) != null) {
				return network;
			}
		}
		
		return null;
	}
	
	public void getLogisticsNetworksFor(Level world, BlockPos pos, Collection<LogisticsNetwork> networks) {
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
