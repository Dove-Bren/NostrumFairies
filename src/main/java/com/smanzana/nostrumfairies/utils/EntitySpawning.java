package com.smanzana.nostrumfairies.utils;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class EntitySpawning {

	/** 
	 * Reads and creates (but does not spawn!) an entity.
	 * @param world
	 * @param tag
	 * @param pos
	 * @return
	 */
	public static @Nullable Entity readEntity(Level world, CompoundTag tag, Vec3 pos) {
		Entity ent;
		try {
			ent = EntityType.create(tag, world).orElse(null);
			if (ent != null) {
				ent.setPos(pos.x, pos.y, pos.z);
			}
		} catch (Exception e) {
			NostrumMagica.logger.error("Failed to spawn pet from snapshot: " + e.getMessage());
			e.printStackTrace();
			ent = null;
		}
		
		return ent;
	}
	
}
