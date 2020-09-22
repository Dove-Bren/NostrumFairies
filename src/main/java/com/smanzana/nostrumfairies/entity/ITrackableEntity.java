package com.smanzana.nostrumfairies.entity;

import net.minecraft.entity.EntityLivingBase;

public interface ITrackableEntity<T extends EntityLivingBase> {

	public void registerListener(IEntityListener<T> listener);
	
	public void removeListener(IEntityListener<T> listener);
	
}
