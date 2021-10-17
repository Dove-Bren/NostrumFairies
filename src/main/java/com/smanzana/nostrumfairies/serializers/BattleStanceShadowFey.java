package com.smanzana.nostrumfairies.serializers;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;

public enum BattleStanceShadowFey {
	RANGED,
	MELEE,
	IDLE;
	
	public final static class BattleStanceSerializer implements DataSerializer<BattleStanceShadowFey> {
		
		private BattleStanceSerializer() {
			;
		}
		
		@Override
		public void write(PacketBuffer buf, BattleStanceShadowFey value) {
			buf.writeEnumValue(value);
		}

		@Override
		public BattleStanceShadowFey read(PacketBuffer buf) throws IOException {
			return buf.readEnumValue(BattleStanceShadowFey.class);
		}

		@Override
		public DataParameter<BattleStanceShadowFey> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public BattleStanceShadowFey copyValue(BattleStanceShadowFey value) {
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