package com.smanzana.nostrumfairies.entity;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;

public class ItemArraySerializer implements DataSerializer<ItemStack[]> {

	public static ItemArraySerializer Serializer = null;
	public static void Init() {
		Serializer = new ItemArraySerializer();
	}
	
	private ItemArraySerializer() {
		DataSerializers.registerSerializer(this);
	}
	
	@Override
	public void write(PacketBuffer buf, ItemStack[] value) {
		// write count, then each item
		buf.writeInt(value.length);
		for (ItemStack stack : value) {
			buf.writeItemStack(stack);
		}
	}

	@Override
	public ItemStack[] read(PacketBuffer buf) throws IOException {
		int count = buf.readInt();
		ItemStack array[] = new ItemStack[Math.max(1, count)];
		
		for (int i = 0; i < count; i++) {
			array[i] = buf.readItemStack();
		}
		
		return array;
	}

	@Override
	public DataParameter<ItemStack[]> createKey(int id) {
		return new DataParameter<>(id, this);
	}

	@Override
	public ItemStack[] copyValue(ItemStack[] value) {
		return Arrays.copyOf(value, value.length);
	}
	
}
