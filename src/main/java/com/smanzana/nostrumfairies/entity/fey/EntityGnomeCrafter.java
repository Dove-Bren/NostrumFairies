package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.blocks.CraftingBlockGnome;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class EntityGnomeCrafter extends EntityGnome {
	
	public static final String ID = "gnome_crafter";
	
	public EntityGnomeCrafter(EntityType<? extends EntityGnomeCrafter> type, Level world) {
		super(type, world);
		this.workDistanceSq = 24 * 24;
	}

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskWorkBlock) {
			LogisticsTaskWorkBlock work = (LogisticsTaskWorkBlock) task;
			
			if (work.getWorld() != this.level) {
				return false;
			}
			
			// Check where the spot is
			BlockPos target = work.getBlockPos();
			if (target == null || !this.canReach(target, true)) {
				return false;
			}
			
			BlockState block = level.getBlockState(target);
			if (block == null || !(block.getBlock() instanceof CraftingBlockGnome)) {
				return false;
			}
			
			// Find a better block to stand, if we weren't told explicitely to stand there
			target = findEmptySpot(target, true);
			if (target == null) {
				return false;
			}
			
			// Check for pathing
			if (this.getDistanceSq(target) < .2) {
				return true;
			}
			Path currentPath = navigation.getPath();
			boolean success = navigation.moveTo(target.getX(), target.getY(), target.getZ(), 1.0);
			if (success) {
				success = Paths.IsComplete(navigation.getPath(), target, 2);
			}
			if (currentPath == null) {
				if (!success) {
					navigation.moveTo(currentPath, 1.0);
				}
			} else {
				navigation.moveTo(currentPath, 1.0);
			}
			if (success) {
				return true;
			} else if (this.getDistanceSq(target) < 1) {
				// extra case for if the navigator refuses cause we're too close
				return true;
			}
			
		}
		
		return false;
	}
	
	public static final AttributeSupplier.Builder BuildCrafterAttributes() {
		return EntityGnome.BuildAttributes()
				.add(Attributes.MOVEMENT_SPEED, .20)
				.add(Attributes.MAX_HEALTH, 4.0)
			;
	}
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 64;
	}
	
	@Override
	protected void onCombatTick() {
		; // No combat
	}

	@Override
	public String getSpecializationName() {
		return "Crafting Gnome";
	}
	
	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return FeyStoneMaterial.GARNET;
	}
}
