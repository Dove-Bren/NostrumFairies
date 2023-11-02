package com.smanzana.nostrumfairies.utils;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpawning {

	/** 
	 * Reads and creates (but does not spawn!) an entity.
	 * @param world
	 * @param tag
	 * @param pos
	 * @return
	 */
	public static @Nullable Entity readEntity(World world, CompoundNBT tag, Vec3d pos) {
		Entity ent;
		try {
			ent = EntityType.loadEntityUnchecked(tag, world).orElse(null);
			if (ent != null) {
				ent.setPosition(pos.x, pos.y, pos.z);
			}
		} catch (Exception e) {
			NostrumMagica.logger.error("Failed to spawn pet from snapshot: " + e.getMessage());
			e.printStackTrace();
			ent = null;
		}
		
		return ent;
	}
	
}
