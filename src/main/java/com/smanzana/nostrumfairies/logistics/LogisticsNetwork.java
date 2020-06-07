package com.smanzana.nostrumfairies.logistics;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry.ILogisticsComponentFactory;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * One logical logistics network, with all nodes and the summed availability of each.
 * @author Skyler
 */
public class LogisticsNetwork {
	
	private static final String NBT_UUID = "uuid";
	private static final String NBT_COMPONENTS = "component";
	private static final String NBT_COMPONENT_KEY = "key";
	private static final String NBT_COMPONENT_VALUE = "value";
	
	private UUID uuid;
	
	protected Set<ILogisticsComponent> components;
	
	protected boolean cacheDirty;
	protected List<ItemStack> cachedItems;
	protected List<ItemDeepStack> cachedCondensedItems; // List of itemstacks with over-stacked sizes
	protected Map<ILogisticsComponent, List<ItemStack>> cachedItemMap;
	
	public LogisticsNetwork() {
		this(UUID.randomUUID(), true);
	}
	
	public LogisticsNetwork(UUID uuid, boolean register) {
		this.uuid = uuid;
		this.cachedItems = new LinkedList<>();
		this.cachedCondensedItems = new LinkedList<>();
		this.components = new HashSet<>(); // components will load and re-attach
		cacheDirty = false;
		
		if (register) {
			NostrumFairies.instance.getLogisticsRegistry().addNetwork(this);
		}
	}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setUniqueId(NBT_UUID, uuid);
		NBTTagList list = new NBTTagList();
		
		for (ILogisticsComponent component : this.components) {
			NBTTagCompound subtag = new NBTTagCompound();
			subtag.setString(NBT_COMPONENT_KEY, component.getSerializationTag());
			subtag.setTag(NBT_COMPONENT_VALUE, component.toNBT());
			list.appendTag(subtag);
		}
		
		tag.setTag(NBT_COMPONENTS, list);
		
		return tag;
	}
	
	public static LogisticsNetwork fromNBT(NBTTagCompound tag) {
		if (!tag.hasUniqueId(NBT_UUID)) {
			throw new RuntimeException("Missing UUID in LogisticsNetwork tag");
		}
		
		UUID id = tag.getUniqueId(NBT_UUID);
		LogisticsNetwork network = new LogisticsNetwork(id, false);
		
		NBTTagList list = tag.getTagList(NBT_COMPONENTS, NBT.TAG_COMPOUND);
		for (int i = list.tagCount() - 1; i >= 0; i--) {
			NBTTagCompound wrapper = list.getCompoundTagAt(i);
			String key = wrapper.getString(NBT_COMPONENT_KEY);
			ILogisticsComponentFactory<?> factory = NostrumFairies.logisticsComponentRegistry.lookupFactory(key);
			
			if (factory == null) {
				throw new RuntimeException("Failed to find factory for component type [" + key + "]! Data has been lost!");
			} else {
				// Avoid 'adding' them the regular way to avoid calling the onAdd.
				network.components.add(factory.construct(wrapper.getCompoundTag(NBT_COMPONENT_VALUE), network));
			}
		}
		
		network.dirty();
		return network;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof LogisticsNetwork && ((LogisticsNetwork) o).uuid.equals(this.uuid);
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode() + 97;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	/**
	 * Check if any of our components can reach the provided component
	 * @param component
	 */
	public boolean canLinkReach(ILogisticsComponent component) {
		if (components.isEmpty()) {
			return true; // First component
		}
		
		for (ILogisticsComponent myComponent : components) {
			
			if (!component.getWorld().equals(myComponent.getWorld())) {
				continue;
			}
			
			// If either distance reaches, call it a success
			final double dist = Math.max(component.getLogisticsLinkRange(), myComponent.getLogisticsLinkRange());
			
			if (component.getPosition().distanceSq(myComponent.getPosition()) < dist * dist) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Try to find a component that can service a provided location.
	 * If multiple can service the position, the closest is returned.
	 * @param world
	 * @param pos
	 * @return
	 */
	public @Nullable ILogisticsComponent getLogisticsFor(World world, BlockPos pos) {
		if (components.isEmpty()) {
			return null; // First component
		}
		
		ILogisticsComponent component = null;
		double minDist = 0;
		for (ILogisticsComponent myComponent : components) {
			
			if (!myComponent.getWorld().equals(world)) {
				continue;
			}
			
			final double dist = myComponent.getPosition().distanceSq(pos);
			if (dist > Math.pow(myComponent.getLogisticRange(), 2)) {
				// too far away
				continue;
			}
			
			// This component works. Better than what we have already?
			if (component == null || dist < minDist) {
				component = myComponent;
				minDist = dist;
			}
		}
		
		return component;
	}
	
	/**
	 * Check if any of our linked components can service out to the provided area.
	 * @param world
	 * @param pos
	 * @return
	 */
	public boolean canLogisticsReach(World world, BlockPos pos) {
		return getLogisticsFor(world, pos) != null;
	}
	
	/**
	 * Attempts to add the component to the network.
	 * This verifies whether the component can be in the network.
	 * @param component
	 * @return true if the network satisfied restrictions and was added to the network. Otherwise, false.
	 */
	public boolean addComponent(ILogisticsComponent component) {
		if (!canLinkReach(component)) {
			return false;
		}
		
		this.components.add(component);
		component.onJoinNetwork(this);
		dirty();
		
		return true;
	}
	
	/**
	 * Removed a component from this network.
	 * @param component
	 */
	public void removeComponent(ILogisticsComponent component) {
		if (this.components.remove(component)) {
			component.onLeaveNetwork();
			dirty();
			component = null;
			
			// Any time a component is removed, we have to re-eval the whole network in case we've been split into pieces.
			// My simple brute-force-ish algo for this it to loop over all nodes over and over, trying to add them together
			// to make a network. Any that can't be connected will make a new network as appropraite.
			if (!this.components.isEmpty()) {
				Set<ILogisticsComponent> remaining = this.components;
				this.components = new HashSet<>();
				
				boolean found;
				do {
					found = false;
					Iterator<ILogisticsComponent> it = remaining.iterator();
					while (it.hasNext()) {
						ILogisticsComponent comp = it.next();
						if (this.canLinkReach(comp)) {
							// not using addComponent to avoid having to remove and re-add to the network
							this.components.add(comp);
							it.remove();
							found = true;
						}
					}
				} while (found == true);
				
				if (!remaining.isEmpty()) {
					// Each still in the list couldn't connect to the existing network.
					// Remove, and then have them make new networks. Maybe one single other network, maybe multiple.
					for (ILogisticsComponent comp : remaining) {
						comp.onLeaveNetwork();
					}
					
					// Keep looping until all are in a network
					while (!remaining.isEmpty()) {
						LogisticsNetwork newNet = new LogisticsNetwork();
						do {
							found = false;
							Iterator<ILogisticsComponent> it = remaining.iterator();
							while (it.hasNext()) {
								ILogisticsComponent comp = it.next();
								if (newNet.addComponent(comp)) {
									it.remove();
									found = true;
								}
							}
						} while (found == true);
					}
				}
			}
			
			// Either there were no components left, or all components couldn't be added (not currently possible)
			if (this.components.isEmpty()) {
				this.removeNetwork();
				return;
			}
		}
	}
	
	/**
	 * Merges the providing network into the current one. After calling, the other network should not be used.
	 * @param otherNetwork
	 */
	public void mergeNetworkIn(LogisticsNetwork otherNetwork) {
		// Don't use removeComponent interface, as we don't want to constantly re-eval while moving over nodes. Instead,
		// trust the caller knows the two can be joined and add them all in.
		for (ILogisticsComponent comp : otherNetwork.components) {
			comp.onLeaveNetwork();
			this.components.add(comp);
			comp.onJoinNetwork(this);
		}
		
		otherNetwork.components.clear();
		otherNetwork.dirty();
		otherNetwork.removeNetwork();
		
		this.dirty();
	}
	
	/**
	 * Internal-ish function to shut down a network.
	 * This is indended for use by the registry itself, and not from network users.
	 * Components should use the registry to add themselves to any existing networks. There shouldn't
	 * be a need to call this.
	 */
	public void dissolveNetwork() {
		; // Do nothing for server networks.
	}
	
	private void removeNetwork() {
		NostrumFairies.instance.getLogisticsRegistry().removeNetwork(this);
	}
	
	/**
	 * Marks the network as dirty, meaning it should re-query components for what they can provide.
	 */
	public void dirty() {
		this.cacheDirty = true;
	}
	
	private void addToCondensed(List<ItemStack> items) {
		// Could make sure both lists are sorted by itemstack, and have iterators on both to make this merge fast.
		// Optimization oppertunity!
		for (ItemStack stack : items) {
			boolean merged = false;
			for (ItemDeepStack condensed : cachedCondensedItems) {
				if (condensed.canMerge(stack)) {
					condensed.add(stack);
					merged = true;
					break;
				}
			}
			
			if (!merged) {
				cachedCondensedItems.add(new ItemDeepStack(stack));
			}
		}
	}
	
	protected void refresh() {
		if (!this.cacheDirty) {
			return;
		}
		
		this.cacheDirty = false;
		cachedItems = new LinkedList<>();
		cachedItemMap = new HashMap<>();
		cachedCondensedItems = new LinkedList<>();
		
		for (ILogisticsComponent component : components) {
			List<ItemStack> list = Lists.newArrayList(component.getItems());
			list.removeIf((stack) -> {
				return stack == null;
			});
			Collections.sort(list, (stack1, stack2) -> {return stack1.getUnlocalizedName().compareTo(stack2.getUnlocalizedName());});
			cachedItems.addAll(list);
			addToCondensed(list);
			cachedItemMap.put(component, list);
		}
	}
	
	public List<ItemStack> getAvailableNetworkItems() {
		refresh();
		return cachedItems;
	}
	
	public List<ItemDeepStack> getCondensedNetworkItems() {
		refresh();
		return cachedCondensedItems;
	}
	
	public Map<ILogisticsComponent, List<ItemStack>> getNetworkItems() {
		return getNetworkItems(null, null, 0.0);
	}
	
	public Map<ILogisticsComponent, List<ItemStack>> getNetworkItems(@Nullable World world, @Nullable BlockPos pos, double maxDistance) {
		refresh();
		
		if (world == null || pos == null) {
			return cachedItemMap;
		}
		
		Map<ILogisticsComponent, List<ItemStack>> filteredMap = new HashMap<>();
		final double maxDistSq = maxDistance * maxDistance;
		for (Entry<ILogisticsComponent, List<ItemStack>> entry : cachedItemMap.entrySet()) {
			ILogisticsComponent component = entry.getKey();
			if (!component.getWorld().equals(world)) {
				continue;
			}
			
			if (component.getPosition().distanceSq(pos) < maxDistSq) {
				filteredMap.put(component, entry.getValue());
			}
		}
		
		return filteredMap;
	}
}
