package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPickupItem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class EntityGnomeCollector extends EntityGnome {
	
	public static final String ID = "gnome_collector";
	
	public EntityGnomeCollector(EntityType<? extends EntityGnomeCollector> type, Level world) {
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
			if (this.distanceToSqr(pickupTask.getItemEntity()) < .2) {
				return true;
			}
			
			return true;
		}
		
		return false;
	}
	
	public static final AttributeSupplier.Builder BuildCollectorAttributes() {
		return EntityGnome.BuildAttributes()
				.add(Attributes.MOVEMENT_SPEED, .20)
				.add(Attributes.MAX_HEALTH, 4.0)
			;
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
