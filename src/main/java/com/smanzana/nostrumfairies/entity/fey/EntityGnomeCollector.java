package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPickupItem;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityGnomeCollector extends EntityGnome {
	
	public EntityGnomeCollector(EntityType<? extends EntityGnomeCollector> type, World world) {
		super(type, world);
		this.workDistanceSq = 24 * 24;
	}

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskPickupItem) {
			LogisticsTaskPickupItem pickupTask = (LogisticsTaskPickupItem) task;
			
			// Check where the retrieval task wants us to go to pick up
			BlockPos pickup = pickupTask.getDestination();
			if (pickup != null && !this.canReach(pickup, true)) {
				return false;
			}
			
			// Check for pathing
			if (this.getDistanceSq(pickupTask.getItemEntity()) < .2) {
				return true;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
		//this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 18;
	}
	
	@Override
	protected void onCombatTick() {
		; // No combat
	}

	@Override
	public String getSpecializationName() {
		return "Broonie";
	}
	
	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return FeyStoneMaterial.EMERALD;
	}
}
