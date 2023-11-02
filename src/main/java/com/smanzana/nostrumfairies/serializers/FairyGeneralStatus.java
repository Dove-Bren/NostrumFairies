package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public enum FairyGeneralStatus {
	WANDERING, // Not attached to a home, and therefore incapable of working
	IDLE, // Able to work, but with no work to do
	WORKING, // Working
	REVOLTING; // Refusing to work
	
	public final static class FairyStatusSerializer implements IDataSerializer<FairyGeneralStatus> {
		
		private FairyStatusSerializer() {
			;
		}
		
		@Override
		public void write(PacketBuffer buf, FairyGeneralStatus value) {
			buf.writeEnumValue(value);
		}

		@Override
		public FairyGeneralStatus read(PacketBuffer buf) {
			return buf.readEnumValue(FairyGeneralStatus.class);
		}

		@Override
		public DataParameter<FairyGeneralStatus> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public FairyGeneralStatus copyValue(FairyGeneralStatus value) {
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