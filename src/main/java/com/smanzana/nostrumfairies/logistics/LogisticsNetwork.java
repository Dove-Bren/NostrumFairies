package com.smanzana.nostrumfairies.logistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry.ILogisticsComponentFactory;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskRegistry;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.Location;
import com.smanzana.nostrummagica.utils.ItemStacks;

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
	
	public static final class RequestedItemRecord {
		
		private ILogisticsTask task;
		
		private ItemDeepStack items;
		
		public RequestedItemRecord(ILogisticsTask task, ItemDeepStack item) {
			this.task = task;
			this.items = item;
		}
		
		public ILogisticsTask getOwningTask() {
			return task;
		}
		
		public ItemDeepStack getItem() {
			return items;
		}
	}
	
	public static final class IncomingItemRecord {
		private ILogisticsTask task;
		
		private ItemDeepStack items;
		
		public IncomingItemRecord(ILogisticsTask task, ItemDeepStack item) {
			this.task = task;
			this.items = item;
		}
		
		public ILogisticsTask getOwningTask() {
			return task;
		}
		
		public ItemDeepStack getItem() {
			return items;
		}
	}
	
	private static final class CachedItemList {
		public List<ItemDeepStack> rawItems;
		public List<ItemDeepStack> availableItems;
		public List<ItemDeepStack> grossItems;
		
		public CachedItemList(List<ItemDeepStack> raws, List<ItemDeepStack> available, List<ItemDeepStack> gross) {
			this.rawItems = raws;
			this.availableItems = available;
			this.grossItems = gross;
		}
	}
	
	public static enum ItemCacheType {
		RAW, // Items that are actually there in  the inventory right now
		NET, // Items minus items that are spoken for but plus items that are incoming
		GROSS, // Items that are there plus any that are incoming
	}
	
	private static final class ComponentGraphNode {
		public ILogisticsComponent component;
		public Set<ILogisticsComponent> neighbors;
		
		public ComponentGraphNode(ILogisticsComponent component) {
			this.component = component;
			this.neighbors = new HashSet<>();
		}
	}
	
	private static final String NBT_UUID = "uuid";
	private static final String NBT_COMPONENTS = "component";
	private static final String NBT_COMPONENT_KEY = "key";
	private static final String NBT_COMPONENT_VALUE = "value";
	private static final String NBT_BEACONS = "beacons";
	
	private UUID uuid;
	
	// Changes should be followed by a rebuildGraph() call
	protected Set<ILogisticsComponent> components;
	
	// Network graph
	protected Map<Location, ComponentGraphNode> componentGraph; // cached graphing of components set
	
	// Items
	protected boolean cacheDirty;
	protected List<ItemDeepStack> cachedCondensedItems; // List of itemstacks with over-stacked sizes
	protected Map<ILogisticsComponent, CachedItemList> cachedItemMap;
	protected Map<ILogisticsComponent, List<RequestedItemRecord>> activeItemRequests; // current items that are being taken from each component
	protected Map<ILogisticsComponent, List<IncomingItemRecord>> activeItemDeliveries; // current items that are soon to be added to the logistics network
	protected UUID cacheKey; // used by components to know whether things have changed since the last time THEY cached stuff
	
	// Tasks
	private LogisticsTaskRegistry taskRegistry;
	
	// Beacons
	protected Set<Location> extraBeacons;
	protected Set<Location> cacheBeaconSet; // Cached set of logistics component and beacon locations
	
	public LogisticsNetwork() {
		this(UUID.randomUUID(), true);
	}
	
	public LogisticsNetwork(UUID uuid, boolean register) {
		this.uuid = uuid;
		this.cachedCondensedItems = new LinkedList<>();
		this.components = new HashSet<>(); // components will load and re-attach
		this.componentGraph = new HashMap<>();
		this.activeItemRequests = new HashMap<>();
		this.activeItemDeliveries = new HashMap<>();
		this.cacheKey = UUID.randomUUID();
		this.taskRegistry = new LogisticsTaskRegistry();
		this.extraBeacons = new HashSet<>();
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
		
		list = new NBTTagList();
		
		for (Location beacon : extraBeacons) {
			list.appendTag(beacon.toNBT());
		}
		
		tag.setTag(NBT_BEACONS, list);
		
		
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
				// Avoid 'adding' them the regular way to avoid calling the onAdd, and constnatly rebuilding graph
				network.components.add(factory.construct(wrapper.getCompoundTag(NBT_COMPONENT_VALUE), network));
			}
		}
		
		list = tag.getTagList(NBT_BEACONS, NBT.TAG_COMPOUND);
		for (int i = list.tagCount() - 1; i >= 0; i--) {
			NBTTagCompound wrapper = list.getCompoundTagAt(i);
			network.addBeacon(Location.FromNBT(wrapper));
		}
		
		network.rebuildGraph();
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
	
	protected static Location getLocation(ILogisticsComponent component) {
		return new Location(component.getWorld(), component.getPosition());
	}
	
	private boolean canComponentLinkReach(ILogisticsComponent component, ILogisticsComponent other) {
		if (!component.getWorld().equals(other.getWorld())) {
			return false;
		}
		
		// If either distance reaches, call it a success
		final double dist = Math.max(component.getLogisticsLinkRange(), other.getLogisticsLinkRange());
		
		return (component.getPosition().distanceSq(other.getPosition()) < dist * dist);
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
			if (canComponentLinkReach(myComponent, component)) {
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
	
	public Collection<ILogisticsComponent> getAllComponents() {
		return this.components;
	}
	
	public @Nullable Collection<ILogisticsComponent> getConnectedComponents(Location location) {
		ComponentGraphNode node = componentGraph.get(location);
		return node == null ? null : componentGraph.get(location).neighbors; // give them null if not in the graph! MWAHAHA
	}
	
	public @Nullable Collection<ILogisticsComponent> getConnectedComponents(ILogisticsComponent component) {
		return getConnectedComponents(getLocation(component));
	}
	
	public @Nullable ILogisticsComponent getComponentAt(Location location) {
		ComponentGraphNode node = componentGraph.get(location);
		return node == null ? null : node.component;
	}
	
	public @Nullable ILogisticsComponent getComponentAt(World world, BlockPos pos) {
		return getComponentAt(new Location(world, pos));
	}
	
	/**
	 * Adds a component to the component list. Does not alert the component it's been added or trigger any effects.
	 * Appropriately dirties network caches and triggers a graph rebuild.
	 * Call this when you would add to the components set itself.
	 * @param component
	 */
	protected void addComponentInternal(ILogisticsComponent component) {
		this.components.add(component);
		dirty();
		rebuildGraph();
		NostrumFairies.instance.getLogisticsRegistry().markDirty();
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
		
		addComponentInternal(component);
		component.onJoinNetwork(this);
		
		return true;
	}
	
	/**
	 * Removed a component from this network.
	 * @param component
	 */
	public void removeComponent(ILogisticsComponent component) {
		if (components.remove(component)) {
			component.onLeaveNetwork();
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
			
			dirty();
			rebuildGraph();
			NostrumFairies.instance.getLogisticsRegistry().markDirty();
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
		
		NostrumFairies.instance.getLogisticsRegistry().markDirty();
		
		this.dirty();
		this.rebuildGraph();
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
	
	protected void clearComponents() {
		this.components.clear();
		this.dirty();
		this.rebuildGraph();
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
	
	private void addToCondensed(List<ItemDeepStack> items) {
		// Could make sure both lists are sorted by itemstack, and have iterators on both to make this merge fast.
		// Optimization oppertunity!
		for (ItemDeepStack stack : items) {
			boolean merged = false;
			for (ItemDeepStack condensed : cachedCondensedItems) {
				if (condensed.canMerge(stack)) {
					condensed.add(stack);
					merged = true;
					break;
				}
			}
			
			if (!merged) {
				cachedCondensedItems.add(stack);
			}
		}
	}
	
	private List<ItemDeepStack> makeAdjustedList(ILogisticsComponent component, List<ItemDeepStack> raws, boolean net) {
		List<RequestedItemRecord> requests = activeItemRequests.get(component);
		List<IncomingItemRecord> deliveries = activeItemDeliveries.get(component);
		List<ItemDeepStack> ret = new ArrayList<>(raws.size());
		for (ItemDeepStack raw : raws) {
			ret.add(raw.copy());
		}
		
		if (deliveries != null && !deliveries.isEmpty()) {
			for (IncomingItemRecord delivery : deliveries) {
				boolean found = false;
				Iterator<ItemDeepStack> it = ret.iterator();
				while (it.hasNext()) {
					ItemDeepStack deep = it.next();
					if (ItemStacks.stacksMatch(deep.getTemplate(), delivery.items.getTemplate())) {
						deep.add(delivery.items.getCount());
						found = true;
						break;
					}
				}
				
				if (!found) {
					// Should add it to the list
					ret.add(delivery.items.copy());
				}
			}
		}
		
		if (net) {
			if (requests != null && !requests.isEmpty()) {
				for (RequestedItemRecord request : requests) {
					Iterator<ItemDeepStack> it = ret.iterator();
					while (it.hasNext()) {
						ItemDeepStack deep = it.next();
						if (ItemStacks.stacksMatch(deep.getTemplate(), request.items.getTemplate())) {
							deep.add(-request.items.getCount());
							if (deep.getCount() <= 0) {
								it.remove();
							}
							break;
						}
					}
				}
			}
		}
		
		return ret;
	}
	
	private List<ItemDeepStack> makeAvailableList(ILogisticsComponent component, List<ItemDeepStack> raws) {
		return makeAdjustedList(component, raws, true);
	}
	
	private List<ItemDeepStack> makeGrossList(ILogisticsComponent component, List<ItemDeepStack> raws) {
		return makeAdjustedList(component, raws, false);
	}
	
	protected void refreshAvailableLists(ILogisticsComponent component) {
		// Corner case for if this network's first interaction is adding an incoming item
		if (cachedItemMap == null) {
			this.refresh();
		} else {
			CachedItemList cache = cachedItemMap.get(component);
			if (cache == null) {
				return;
			}
			
			// COULD make the func do both at the same time for effeciency
			cache.availableItems = makeAvailableList(component, cache.rawItems);
			cache.grossItems = makeGrossList(component, cache.rawItems); 
			refreshCacheKey();
		}
	}
	
	private CachedItemList makeItemListEntry(ILogisticsComponent component, List<ItemDeepStack> raws) {
		return new CachedItemList(raws, makeAvailableList(component, raws), makeGrossList(component, raws));
	}
	
	private void refreshCacheKey() {
		this.cacheKey = UUID.randomUUID();
	}
	
	protected void refresh() {
		if (!this.cacheDirty) {
			return;
		}
		
		this.cacheDirty = false;
		cachedItemMap = new HashMap<>();
		cachedCondensedItems = new LinkedList<>();
		
		for (ILogisticsComponent component : components) {
			List<ItemDeepStack> list = ItemDeepStack.toDeepList(component.getItems());
			list.removeIf((stack) -> {
				return stack == null || stack.getTemplate() == null;
			});
			Collections.sort(list, (stack1, stack2) -> {return stack1.getTemplate().getUnlocalizedName().compareTo(stack2.getTemplate().getUnlocalizedName());});
			addToCondensed(list);
			cachedItemMap.put(component, makeItemListEntry(component, list));
		}
		
		refreshCacheKey();
	}
	
	protected void rebuildGraph() {
		this.componentGraph.clear();
		this.cacheBeaconSet = null; // if components have changed, we need to redo beacons
		
		ILogisticsComponent[] arr = components.toArray(new ILogisticsComponent[0]);
		for (int j = 0; j < components.size(); j++) {
			ILogisticsComponent component = arr[j];
			Location compLocation = getLocation(component);
			ComponentGraphNode node = componentGraph.get(compLocation);
			if (node == null) {
				node = new ComponentGraphNode(component);
				componentGraph.put(compLocation, node);
			}
			
			for (int i = j + 1; i < components.size(); i++) {
				ILogisticsComponent other = arr[i];
				
				// Can we reach it?
				if (canComponentLinkReach(component, other)) {
					node.neighbors.add(other);
					
					// Add to their list, too
					Location otherLocation = getLocation(other);
					ComponentGraphNode otherNode = componentGraph.get(otherLocation);
					
					if (otherNode == null) {
						otherNode = new ComponentGraphNode(other);
						componentGraph.put(otherLocation, otherNode);
					}
					otherNode.neighbors.add(component);
				}
			}
		}
	}
	
	public List<ItemDeepStack> getAllCondensedNetworkItems() {
		refresh();
		return cachedCondensedItems;
	}
	
	/**
	 * Get a map from a logistics network component and the items it has.
	 * This version of this call does not do any filtering based on distance.
	 * @param rawContents if true, return the items actually in the component. If false, factors in items being
	 * 			pulled out of the logistics network and items being delivered.
	 * @return
	 */
	public Map<ILogisticsComponent, List<ItemDeepStack>> getNetworkItems(ItemCacheType type) {
		return getNetworkItems(null, null, 0.0, type);
	}
	
	/**
	 * Get a filtered set of all items in the network, mapped to the component that has them.
	 * This list is filtered to the world and max-distance requirements of the caller.
	 * This call either returns lists of items that are effectively in the component
	 * (actual - items being requested + items being delivered) OR lists of all the items
	 * actually in the component.
	 * World and pos are optional. If they are null, no distance checking will be done.
	 * If provided,components that are further than the provided maxDistance from the provided pos will be filtered out.
	 * <b>Note:</b> this map is sorted from least-distance to most-distance for convenience of selecting a component.
	 * @param world
	 * @param pos
	 * @param maxDistance
	 * @param rawContents if true, return the actaul contents of the component without adjusting for deliveries or item requests.
	 * @return
	 */
	public Map<ILogisticsComponent, List<ItemDeepStack>> getNetworkItems(@Nullable World world, @Nullable BlockPos pos, double maxDistance, ItemCacheType type) {
		refresh();
		
		Map<ILogisticsComponent, List<ItemDeepStack>> filteredMap;
		if (world == null || pos == null) {
			filteredMap = new HashMap<>();
		} else {
			filteredMap = new TreeMap<>((l, r) -> {
				double lDist = l.getPosition().distanceSq(pos);
				double rDist = r.getPosition().distanceSq(pos);
				return (lDist < rDist ? -1 : 1);
			});
		}
		
		final double maxDistSq = maxDistance * maxDistance;
		for (Entry<ILogisticsComponent, CachedItemList> entry : cachedItemMap.entrySet()) {
			ILogisticsComponent component = entry.getKey();
			if (world != null && !component.getWorld().equals(world)) {
				continue;
			}
			
			if (pos == null || component.getPosition().distanceSq(pos) < maxDistSq) {
				switch (type) {
				case GROSS:
					filteredMap.put(component, entry.getValue().grossItems);
					break;
				case NET:
					filteredMap.put(component, entry.getValue().availableItems);
					break;
				case RAW:
					filteredMap.put(component, entry.getValue().rawItems);
					break;
				}
				
			}
		}
		
		return filteredMap;
	}
	
	public @Nullable ILogisticsComponent getStorageForItem(World world, BlockPos pos, ItemStack stack, @Nullable Predicate<ILogisticsComponent> filter) {
		ILogisticsComponent nearest = null;
		double minDist = 0;
		
		for (ILogisticsComponent comp : components) {
			if (!comp.getWorld().equals(world)) {
				continue;
			}
			
			List<IncomingItemRecord> incomingRecords = activeItemDeliveries.get(comp);
			List<ItemDeepStack> allIncomingStacks = new ArrayList<>(incomingRecords == null ? 1 : incomingRecords.size());
			if (incomingRecords != null) {
				for (IncomingItemRecord record : incomingRecords) {
					allIncomingStacks.add(record.items);
				}
			}
			
			allIncomingStacks.add(new ItemDeepStack(stack));
			
			if (!comp.canAccept(allIncomingStacks)) {
				continue;
			}
			
			if (filter != null && !filter.apply(comp)) {
				continue;
			}
			
			final double dist = comp.getPosition().distanceSq(pos);
			if (nearest == null || dist < minDist) {
				minDist = dist;
				nearest = comp;
			}
		}
		
		return nearest;
	}
	
	public @Nullable ILogisticsComponent getStorageForItem(World world, BlockPos pos, ItemStack stack) {
		return getStorageForItem(world, pos, stack, null);
	}
	
	/**
	 * Notify the network that some number of an item are spoken for from the given component.
	 * Other requests should look elsewhere.
	 * @param component
	 * @param activeRequest
	 */
	public void addRequestedItem(ILogisticsComponent component, RequestedItemRecord request) {
		List<RequestedItemRecord> requests = activeItemRequests.get(component);
		if (requests == null) {
			requests = new LinkedList<>();
			activeItemRequests.put(component, requests);
		}
		
		requests.add(request);
		
		refreshAvailableLists(component);
	}
	
	public RequestedItemRecord addRequestedItem(ILogisticsComponent component, ILogisticsTask task, ItemDeepStack item) {
		RequestedItemRecord record = new RequestedItemRecord(task, item);
		
		addRequestedItem(component, record);
		
		return record;
	}
	
	public void removeRequestedItem(ILogisticsComponent component, RequestedItemRecord request) {
		List<RequestedItemRecord> requests = activeItemRequests.get(component);
		if (requests == null) {
			return;
		}
		
		requests.remove(request);
		refreshAvailableLists(component);
	}
	
	public void removeAllRequests(ILogisticsComponent component, ILogisticsTask task) {
		List<RequestedItemRecord> requests = activeItemRequests.get(component);
		if (requests == null) {
			return;
		}
		
		Iterator<RequestedItemRecord> it = requests.iterator();
		while (it.hasNext()) {
			if (it.next().task == task) {
				it.remove();
			}
		}
		refreshAvailableLists(component);
	}
	
	public @Nullable Collection<RequestedItemRecord> getItemRequests(ILogisticsComponent component) {
		return activeItemRequests.get(component);
	}
	
	public IncomingItemRecord addIncomingItem(ILogisticsComponent component, IncomingItemRecord record) {
		List<IncomingItemRecord> records = activeItemDeliveries.get(component);
		
		if (records == null) {
			records = new LinkedList<>();
			activeItemDeliveries.put(component, records);
		}
		
		records.add(record);
		refreshAvailableLists(component);
		return record;
	}
	
	public IncomingItemRecord addIncomingItem(ILogisticsComponent component, ILogisticsTask task, ItemDeepStack item) {
		return addIncomingItem(component, new IncomingItemRecord(task, item));
	}
	
	public void removeIncomingItem(ILogisticsComponent component, IncomingItemRecord request) {
		List<IncomingItemRecord> deliveries = activeItemDeliveries.get(component);
		if (deliveries == null) {
			return;
		}
		
		deliveries.remove(request);
		refreshAvailableLists(component);
	}
	
	public void removeAllIncomingItems(ILogisticsComponent component, ILogisticsTask task) {
		List<IncomingItemRecord> deliveries = activeItemDeliveries.get(component);
		if (deliveries == null) {
			return;
		}
		
		Iterator<IncomingItemRecord> it = deliveries.iterator();
		while (it.hasNext()) {
			if (it.next().task == task) {
				it.remove();
			}
		}
		refreshAvailableLists(component);
	}
	
	public @Nullable Collection<IncomingItemRecord> getItemDeliveries(ILogisticsComponent component) {
		return activeItemDeliveries.get(component);
	}
	
	/**
	 * Return a UUID that changes any time the item cache is refreshed.
	 * Network-dependent constructs can cache item stuff locally and use this key to see whether or not
	 * they need to bust their cache and recompute.
	 * @return
	 */
	public @Nullable UUID getCacheKey() {
		// if the cache is dirty, don't even return the old one. Make them query inventory to generate new stuff.
		return cacheDirty ? null : cacheKey;
	}
	
	public LogisticsTaskRegistry getTaskRegistry() {
		return taskRegistry;
	}
	
	public Set<Location> getAllBeacons() {
		if (this.cacheBeaconSet == null) {
			this.cacheBeaconSet = new HashSet<>(this.extraBeacons);
			for (ILogisticsComponent comp : components) {
				cacheBeaconSet.add(getLocation(comp));
			}
		}
		
		return Sets.newHashSet(cacheBeaconSet);
	}
	
	/**
	 * Get beacons that were added AS BEACONS and not as a component
	 * @return
	 */
	public Set<Location> getOnlyBeacons() {
		return Sets.newHashSet(extraBeacons);
	}
	
	public void clearBeacons() {
		this.cacheBeaconSet = null;
		this.extraBeacons.clear();
	}
	
	public void addBeacon(Location loc) {
		if (this.extraBeacons.add(loc)) {
			this.cacheBeaconSet = null;
		}
	}
	
	public void addBeacon(World world, BlockPos pos) {
		this.addBeacon(new Location(world, pos));
	}
	
	public void removeBeacon(Location loc) {
		if (this.extraBeacons.remove(loc)) {
			this.cacheBeaconSet = null;
		}
	}
	
	public void removeBeacon(World world, BlockPos pos) {
		this.removeBeacon(new Location(world, pos));
	}
}
