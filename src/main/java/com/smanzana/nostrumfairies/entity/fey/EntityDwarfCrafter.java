package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.blocks.CraftingBlockDwarf;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityDwarfCrafter extends EntityDwarf {
	
	public static final String ID = "dwarf_crafter";

	public EntityDwarfCrafter(EntityType<? extends EntityDwarfCrafter> type, World world) {
		super(type, world);
		
		this.workDistanceSq = 24 * 24;
	}
	
	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskWorkBlock) {
			// TODO require a specialization
			LogisticsTaskWorkBlock work = (LogisticsTaskWorkBlock) task;
			
			if (work.getWorld() != this.world) {
				return false;
			}
			
			// Check where the spot is
			BlockPos target = work.getBlockPos();
			if (target == null || !this.canReach(target, true)) {
				return false;
			}
			
			// Dwarves only want to work at ones from dwarf blocks
			BlockState block = world.getBlockState(target);
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
			Path currentPath = navigator.getPath();
			boolean success = navigator.tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0);
			if (success) {
				success = Paths.IsComplete(navigator.getPath(), target, 2);
			}
			if (currentPath == null) {
				if (!success) {
					navigator.setPath(currentPath, 1.0);
				}
			} else {
				navigator.setPath(currentPath, 1.0);
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

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.18D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
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
		world.playSound(posX, posY, posZ, SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.NEUTRAL, .8f, 1.6f, false);
	}
}
