package com.smanzana.nostrumfairies.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;

/**
 * Specialized list for ItemDeepStacks.
 * List is always sorted.
 * Internally, a linked list.
 * @author Skyler
 *
 */
public class ItemDeepStackList implements Collection<ItemDeepStack> {

	private final TreeMap<ItemStack, Long> storage;
	
	public ItemDeepStackList() {
		storage = new TreeMap<>((stack1, stack2) -> {
			if (stack1.getItem() == stack2.getItem()) {
				if (stack1.getItem().getHasSubtypes()) {
					return stack1.getMetadata() - stack2.getMetadata();
				} else {
					return 0;
				}
			}
			
			return stack1.getUnlocalizedName().compareTo(stack2.getUnlocalizedName());
		});
	}
	
	public ItemDeepStackList(Iterable<ItemDeepStack> input) {
		this();
		addAll(input);
	}
	
	@Override
	public boolean add(ItemDeepStack e) {
		System.out.println("adding " + e.getTemplate() + " X " + e.getCount());
		// Check if the itemstack template is already in map. If so, add to it. Otherwise, add it.
		Long existing = storage.get(e.getTemplate());
		final long amt;
		if (existing == null || existing <= 0) {
			// New item!
			amt = e.getCount();
		} else {
			amt = existing + e.getCount();
		}
		storage.put(e.getTemplate(), amt);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends ItemDeepStack> c) {
		for (ItemDeepStack stack : c) {
			add(stack);
		}
		return true;
	}
	
	public boolean addAll(Iterable<? extends ItemDeepStack> c) {
		Iterator<? extends ItemDeepStack> it = c.iterator();
		while (it.hasNext()) {
			add(it.next());
		}
		return true;
	}

	@Override
	public void clear() {
		storage.clear();
	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof ItemDeepStack) {
			return storage.containsKey(((ItemDeepStack) o).getTemplate());
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object stack : c) {
			if (!contains(stack)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return storage.isEmpty();
	}

	@Override
	public Iterator<ItemDeepStack> iterator() {
		return new Iterator<ItemDeepStack>() {
			
			private final Iterator<ItemStack> internalIt = storage.keySet().iterator();

			@Override
			public boolean hasNext() {
				return internalIt.hasNext();
			}

			@Override
			public ItemDeepStack next() {
				ItemStack key = internalIt.next();
				if (!key.isEmpty()) {
					return new ItemDeepStack(key, storage.get(key));
				}
				
				return null;
			}
			
		};
	}

	@Override
	public boolean remove(Object o) {
		if (o instanceof ItemDeepStack) {
			ItemDeepStack stack = (ItemDeepStack) o;
			if (stack.getCount() <= 0) {
				return false;
			}
			
			Long count = storage.get(stack.getTemplate());
			if (count == null) {
				count = 0L;
			}
			
			if (count <= stack.getCount()) {
				storage.remove(stack.getTemplate());
			} else {
				count -= stack.getCount();
				storage.put(stack.getTemplate(), count);
			}
			
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Object stack : c) {
			if (remove(stack)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (c.isEmpty()) {
			this.clear();
			return true;
		}
		
		boolean changed = false;
		@SuppressWarnings("unchecked")
		Collection<? extends ItemDeepStack> input = (Collection<? extends ItemDeepStack>) c;
		
		Iterator<ItemStack> it = storage.keySet().iterator();
		while (it.hasNext()) {
			@Nonnull ItemStack key = it.next();
			boolean found = false;
			for (ItemDeepStack allowed : input) {
				if (allowed.canMerge(key)) {
					found = true;
					break;
				}
			}
			if (!found) {
				it.remove();
				changed = true;
			}
		}
		
		return changed;
	}

	@Override
	public int size() {
		return storage.size();
	}

	@Override
	public Object[] toArray() {
		List<ItemDeepStack> cheat = Lists.newArrayList(this);
		return cheat.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		List<ItemDeepStack> cheat = Lists.newArrayList(this);
		return cheat.toArray(a);
	}
	
	public ItemDeepStack get(int index) {
		// Sort of like a linked list lol
		Iterator<ItemStack> it = storage.navigableKeySet().iterator();
		ItemStack key = ItemStack.EMPTY;
		for (int i = 0; i <= index; i++) {
			key = it.next();
		}
		
		return new ItemDeepStack(key, storage.get(key));
	}
	
}
