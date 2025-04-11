package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public enum ArmPoseGnome {
	IDLE,
	WORKING,
	CARRYING;
	
	public final static class PoseSerializer implements EntityDataSerializer<ArmPoseGnome> {
		
		private PoseSerializer() {
			;
		}
		
		@Override
		public void write(FriendlyByteBuf buf, ArmPoseGnome value) {
			buf.writeEnum(value);
		}

		@Override
		public ArmPoseGnome read(FriendlyByteBuf buf) {
			return buf.readEnum(ArmPoseGnome.class);
		}

		@Override
		public EntityDataAccessor<ArmPoseGnome> createAccessor(int id) {
			return new EntityDataAccessor<>(id, this);
		}

		@Override
		public ArmPoseGnome copy(ArmPoseGnome value) {
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