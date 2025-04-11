package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public enum ArmPoseElf {
	IDLE,
	WORKING,
	ATTACKING;
	
	public final static class PoseSerializer implements EntityDataSerializer<ArmPoseElf> {
		
		private PoseSerializer() {
			;
		}
		
		@Override
		public void write(FriendlyByteBuf buf, ArmPoseElf value) {
			buf.writeEnum(value);
		}

		@Override
		public ArmPoseElf read(FriendlyByteBuf buf) {
			return buf.readEnum(ArmPoseElf.class);
		}

		@Override
		public EntityDataAccessor<ArmPoseElf> createAccessor(int id) {
			return new EntityDataAccessor<>(id, this);
		}

		@Override
		public ArmPoseElf copy(ArmPoseElf value) {
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