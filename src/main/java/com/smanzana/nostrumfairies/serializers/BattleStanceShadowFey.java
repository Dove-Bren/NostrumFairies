package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public enum BattleStanceShadowFey {
	RANGED,
	MELEE,
	IDLE;
	
	public final static class BattleStanceSerializer implements EntityDataSerializer<BattleStanceShadowFey> {
		
		private BattleStanceSerializer() {
			;
		}
		
		@Override
		public void write(FriendlyByteBuf buf, BattleStanceShadowFey value) {
			buf.writeEnum(value);
		}

		@Override
		public BattleStanceShadowFey read(FriendlyByteBuf buf) {
			return buf.readEnum(BattleStanceShadowFey.class);
		}

		@Override
		public EntityDataAccessor<BattleStanceShadowFey> createAccessor(int id) {
			return new EntityDataAccessor<>(id, this);
		}

		@Override
		public BattleStanceShadowFey copy(BattleStanceShadowFey value) {
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