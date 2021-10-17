package com.smanzana.nostrumfairies.serializers;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public enum ArmPoseDwarf {
	IDLE,
	MINING,
	ATTACKING;
	
	public final static class PoseSerializer implements DataSerializer<ArmPoseDwarf> {
		
		private PoseSerializer() {
			;
		}
		
		@Override
		public void write(PacketBuffer buf, ArmPoseDwarf value) {
			buf.writeEnumValue(value);
		}

		@Override
		public ArmPoseDwarf read(PacketBuffer buf) throws IOException {
			return buf.readEnumValue(ArmPoseDwarf.class);
		}

		@Override
		public DataParameter<ArmPoseDwarf> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public ArmPoseDwarf copyValue(ArmPoseDwarf value) {
			return value;
		}
	}
	
	private static PoseSerializer Serializer = null;
	public static PoseSerializer instance() {
		if (Serializer == null) {
			Serializer = new PoseSerializer();
		}
		return Serializer;
	}
}