package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.blocks.CraftingBlockDwarf;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class EntityDwarfCrafter extends EntityDwarf {
	
	public static final String ID = "dwarf_crafter";

	public EntityDwarfCrafter(EntityType<? extends EntityDwarfCrafter> type, Level world) {
		super(type, world);
	}
	
	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskWorkBlock) {
			// TODO require a specialization
			LogisticsTaskWorkBlock work = (LogisticsTaskWorkBlock) task;
			
			if (work.getWorld() != this.level) {
				return false;
			}
			
			// Check where the spot is
			BlockPos target = work.getBlockPos();
			if (target == null || !this.canReach(target, true)) {
				return false;
			}
			
			// Dwarves only want to work at ones from dwarf blocks
			BlockState block = level.getBlockState(target);
			if (block == null || !(block.getBlock() instanceof CraftingBlockDwarf)) {
				return false;
			}
			
			// Find a better block to stand, if we weren't told explicitely to stand there
			target = findEmptySpot(target, true, true);
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

	@Override
	protected void registerGoals() {
		super.registerGoals();
	}

	public static final AttributeSupplier.Builder BuildCrafterAttributes() {
		return EntityDwarf.BuildAttributes()
				.add(Attributes.MOVEMENT_SPEED, .18)
				.add(Attributes.MAX_HEALTH, 18)
				.add(Attributes.ARMOR, 4)
			;
	}
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 8;
	}
	
	@Override
	protected void onCientTick() {
		;
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	@Override
	public String getSpecializationName() {
		return "Smithing Dwarf";
	}
	
	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return FeyStoneMaterial.GARNET;
	}
	
	@Override
	protected void playWorkSound() {
		level.playLocalSound(getX(), getY(), getZ(), SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, .8f, 1.6f, false);
	}
}
