package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public enum FairyJob {
	WARRIOR,
	BUILDER,
	LOGISTICS;
	
	public final static class JobSerializer implements EntityDataSerializer<FairyJob> {
		
		private JobSerializer() {
			;
		}
		
		@Override
		public void write(FriendlyByteBuf buf, FairyJob value) {
			buf.writeEnum(value);
		}

		@Override
		public FairyJob read(FriendlyByteBuf buf) {
			return buf.readEnum(FairyJob.class);
		}

		@Override
		public EntityDataAccessor<FairyJob> createAccessor(int id) {
			return new EntityDataAccessor<>(id, this);
		}

		@Override
		public FairyJob copy(FairyJob value) {
			return value;
		}
	}
	
	private static JobSerializer Serializer = null;
	public static JobSerializer instance() {
		if (Serializer == null) {
			Serializer = new JobSerializer();
		}
		return Serializer;
	}
}