package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public enum FairyGeneralStatus {
	WANDERING, // Not attached to a home, and therefore incapable of working
	IDLE, // Able to work, but with no work to do
	WORKING, // Working
	REVOLTING; // Refusing to work
	
	public final static class FairyStatusSerializer implements EntityDataSerializer<FairyGeneralStatus> {
		
		private FairyStatusSerializer() {
			;
		}
		
		@Override
		public void write(FriendlyByteBuf buf, FairyGeneralStatus value) {
			buf.writeEnum(value);
		}

		@Override
		public FairyGeneralStatus read(FriendlyByteBuf buf) {
			return buf.readEnum(FairyGeneralStatus.class);
		}

		@Override
		public EntityDataAccessor<FairyGeneralStatus> createAccessor(int id) {
			return new EntityDataAccessor<>(id, this);
		}

		@Override
		public FairyGeneralStatus copy(FairyGeneralStatus value) {
			return value;
		}
	}
	
	private static FairyGeneralStatus.FairyStatusSerializer INSTANCE = null;
	public static FairyStatusSerializer instance() {
		if (INSTANCE == null) {
			INSTANCE = new FairyStatusSerializer();
		}
		return INSTANCE;
	}
}