package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public enum ArmPoseElf {
	IDLE,
	WORKING,
	ATTACKING;
	
	public final static class PoseSerializer implements IDataSerializer<ArmPoseElf> {
		
		private PoseSerializer() {
			;
		}
		
		@Override
		public void write(PacketBuffer buf, ArmPoseElf value) {
			buf.writeEnumValue(value);
		}

		@Override
		public ArmPoseElf read(PacketBuffer buf) {
			return buf.readEnumValue(ArmPoseElf.class);
		}

		@Override
		public DataParameter<ArmPoseElf> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public ArmPoseElf copyValue(ArmPoseElf value) {
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