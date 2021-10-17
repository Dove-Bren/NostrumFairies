package com.smanzana.nostrumfairies.serializers;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public enum FairyJob {
	WARRIOR,
	BUILDER,
	LOGISTICS;
	
	public final static class JobSerializer implements DataSerializer<FairyJob> {
		
		private JobSerializer() {
			;
		}
		
		@Override
		public void write(PacketBuffer buf, FairyJob value) {
			buf.writeEnumValue(value);
		}

		@Override
		public FairyJob read(PacketBuffer buf) throws IOException {
			return buf.readEnumValue(FairyJob.class);
		}

		@Override
		public DataParameter<FairyJob> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public FairyJob copyValue(FairyJob value) {
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