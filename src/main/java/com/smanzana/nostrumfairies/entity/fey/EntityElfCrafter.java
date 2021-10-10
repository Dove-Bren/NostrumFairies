package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.blocks.CraftingBlockElf;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityElfCrafter extends EntityElf {
	
	public EntityElfCrafter(World world) {
		super(world);
		this.height = 0.90f;
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
			IBlockState block = world.getBlockState(target);
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
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.24D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
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
		if (this.ticksExisted % 10 == 0 && this.getPose() == ArmPose.WORKING) {
			
			double angle = this.rotationYawHead + ((this.isLeftHanded() ? -1 : 1) * 22.5);
			double xdiff = Math.sin(angle / 180.0 * Math.PI) * .4;
			double zdiff = Math.cos(angle / 180.0 * Math.PI) * .4;
			
			double x = posX - xdiff;
			double z = posZ + zdiff;
			world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, posY + 1.25, z, 0, .015, 0, new int[0]);
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
