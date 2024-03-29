package com.smanzana.nostrumfairies.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

/**
 * A generic listener that wants updates about all the changes an entity experiences
 * @author Skyler
 *
 */
public interface IEntityListener<T extends LivingEntity> {

	public void onDeath(T entity);
	
	public void onDamage(T entity, DamageSource source, double damage);
	
	public void onHeal(T entity, double amount);
	
}
