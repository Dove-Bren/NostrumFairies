package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public enum ArmPoseGnome {
	IDLE,
	WORKING,
	CARRYING;
	
	public final static class PoseSerializer implements IDataSerializer<ArmPoseGnome> {
		
		private PoseSerializer() {
			;
		}
		
		@Override
		public void write(PacketBuffer buf, ArmPoseGnome value) {
			buf.writeEnumValue(value);
		}

		@Override
		public ArmPoseGnome read(PacketBuffer buf) {
			return buf.readEnumValue(ArmPoseGnome.class);
		}

		@Override
		public DataParameter<ArmPoseGnome> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public ArmPoseGnome copyValue(ArmPoseGnome value) {
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