package com.smanzana.nostrumfairies.serializers;

import java.util.Arrays;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public class ItemArraySerializer implements EntityDataSerializer<ItemStack[]> {

	private static ItemArraySerializer Serializer = null;
	public static ItemArraySerializer instance() {
		if (Serializer == null) {
			Serializer = new ItemArraySerializer();
		}
		return Serializer;
	}
	
	private ItemArraySerializer() {
		;
	}
	
	@Override
	public void write(FriendlyByteBuf buf, ItemStack[] value) {
		// write count, then each item
		buf.writeInt(value.length);
		for (ItemStack stack : value) {
			buf.writeItem(stack);
		}
	}

	@Override
	public ItemStack[] read(FriendlyByteBuf buf) {
		int count = buf.readInt();
		int size = Math.max(1, count);
		ItemStack array[] = new ItemStack[size];
		
		for (int i = 0; i < size; i++) {
			if (i < count) {
				array[i] = buf.readItem();
			} else {
				array[i] = ItemStack.EMPTY;
			}
		}
		
		return array;
	}

	@Override
	public EntityDataAccessor<ItemStack[]> createAccessor(int id) {
		return new EntityDataAccessor<>(id, this);
	}

	@Override
	public ItemStack[] copy(ItemStack[] value) {
		return Arrays.copyOf(value, value.length);
	}
	
}
