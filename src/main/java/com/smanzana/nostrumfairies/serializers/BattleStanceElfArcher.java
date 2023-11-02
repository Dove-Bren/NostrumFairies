package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public enum BattleStanceElfArcher {
	RANGED,
	MELEE;
	
	public final static class BattleStanceSerializer implements IDataSerializer<BattleStanceElfArcher> {
		
		private BattleStanceSerializer() {
			;
		}
		
		@Override
		public void write(PacketBuffer buf, BattleStanceElfArcher value) {
			buf.writeEnumValue(value);
		}

		@Override
		public BattleStanceElfArcher read(PacketBuffer buf) {
			return buf.readEnumValue(BattleStanceElfArcher.class);
		}

		@Override
		public DataParameter<BattleStanceElfArcher> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public BattleStanceElfArcher copyValue(BattleStanceElfArcher value) {
			return value;
		}
	}
	
	private static BattleStanceSerializer Serializer = null;
	public static BattleStanceSerializer instance() {
		if (Serializer == null) {
			Serializer = new BattleStanceSerializer();
		}
		return Serializer;
	}
}