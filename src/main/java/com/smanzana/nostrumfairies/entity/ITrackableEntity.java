package com.smanzana.nostrumfairies.entity;

import net.minecraft.entity.LivingEntity;

public interface ITrackableEntity<T extends LivingEntity> {

	public void registerListener(IEntityListener<T> listener);
	
	public void removeListener(IEntityListener<T> listener);
	
}
