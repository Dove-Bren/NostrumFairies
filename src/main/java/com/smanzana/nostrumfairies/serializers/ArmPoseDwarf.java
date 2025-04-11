package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public enum ArmPoseDwarf {
	IDLE,
	MINING,
	ATTACKING;
	
	public final static class PoseSerializer implements EntityDataSerializer<ArmPoseDwarf> {
		
		private PoseSerializer() {
			;
		}
		
		@Override
		public void write(FriendlyByteBuf buf, ArmPoseDwarf value) {
			buf.writeEnum(value);
		}

		@Override
		public ArmPoseDwarf read(FriendlyByteBuf buf) {
			return buf.readEnum(ArmPoseDwarf.class);
		}

		@Override
		public EntityDataAccessor<ArmPoseDwarf> createAccessor(int id) {
			return new EntityDataAccessor<>(id, this);
		}

		@Override
		public ArmPoseDwarf copy(ArmPoseDwarf value) {
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