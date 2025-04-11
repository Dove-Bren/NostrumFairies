package com.smanzana.nostrumfairies.serializers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public enum BattleStanceElfArcher {
	RANGED,
	MELEE;
	
	public final static class BattleStanceSerializer implements EntityDataSerializer<BattleStanceElfArcher> {
		
		private BattleStanceSerializer() {
			;
		}
		
		@Override
		public void write(FriendlyByteBuf buf, BattleStanceElfArcher value) {
			buf.writeEnum(value);
		}

		@Override
		public BattleStanceElfArcher read(FriendlyByteBuf buf) {
			return buf.readEnum(BattleStanceElfArcher.class);
		}

		@Override
		public EntityDataAccessor<BattleStanceElfArcher> createAccessor(int id) {
			return new EntityDataAccessor<>(id, this);
		}

		@Override
		public BattleStanceElfArcher copy(BattleStanceElfArcher value) {
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