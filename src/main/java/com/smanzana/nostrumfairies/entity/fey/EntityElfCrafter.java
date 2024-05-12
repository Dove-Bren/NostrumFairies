package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.blocks.CraftingBlockElf;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.serializers.ArmPoseElf;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityElfCrafter extends EntityElf {
	
	public static final String ID = "elf_crafter";
	
	public EntityElfCrafter(EntityType<? extends EntityElfCrafter> type, World world) {
		super(type, world);
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
			if (block == null || !(block.getBlock() instanceof CraftingBlockElf)) {
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
	protected void onIdleTick() {
		super.onIdleTick();
	}
	
	public static final AttributeModifierMap.MutableAttribute BuildCrafterAttributes() {
		return EntityElf.BuildAttributes()
				.createMutableAttribute(Attributes.MOVEMENT_SPEED, .24)
				.createMutableAttribute(Attributes.MAX_HEALTH, 4.0)
				.createMutableAttribute(Attributes.ATTACK_DAMAGE, 1.0)
			;
	}
	
	@Override
	protected void registerData() {
		super.registerData();
	}
	
	@Override
	protected void onCombatTick() {
		super.onCombatTick();
	}
	
	@Override
	protected int getDefaultSwingAnimationDuration() {
		return 30;
	}
	
	@Override
	protected void onCientTick() {
		if (this.ticksExisted % 10 == 0 && this.getElfPose() == ArmPoseElf.WORKING) {
			
			double angle = this.rotationYawHead + ((this.isLeftHanded() ? -1 : 1) * 22.5);
			double xdiff = Math.sin(angle / 180.0 * Math.PI) * .4;
			double zdiff = Math.cos(angle / 180.0 * Math.PI) * .4;
			
			double x = getPosX() - xdiff;
			double z = getPosZ() + zdiff;
			world.addParticle(ParticleTypes.SMOKE, x, getPosY() + 1.25, z, 0, .015, 0);
		}
	}
	
	@Override
	public String getSpecializationName() {
		return "Elven Crafter";
	}

	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return FeyStoneMaterial.GARNET;
	}
}
