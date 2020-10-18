package com.smanzana.nostrumfairies.entity.fey;

import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskBuildBlock;
import com.smanzana.nostrumfairies.utils.Paths;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityDwarfBuilder extends EntityDwarf {

	public EntityDwarfBuilder(World world) {
		super(world);
		
		this.height = 0.85f;
		this.workDistanceSq = 24 * 24;
	}
	
	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskBuildBlock) {
			// TODO require a specialization\
			LogisticsTaskBuildBlock build = (LogisticsTaskBuildBlock) task;
			
			if (build.getWorld() != this.worldObj) {
				return false;
			}
			
			// Check where the spot is
			BlockPos target = build.getTargetPlaceLoc();
			if (target == null || !this.canReach(target, true)) {
				return false;
			}
			
			// Find a better block to stand, if we weren't told explicitely to stand there
			if (target == build.getTargetBlock()) {
				target = findEmptySpot(target, true, true);
				if (target == null) {
					return false;
				}
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
	protected void initEntityAI() {
		super.initEntityAI();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.22D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(3.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(Math.sqrt(MAX_FAIRY_DISTANCE_SQ));
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
	public void onUpdate() {
		super.onUpdate();
		
//		if (worldObj.isRemote && isSwingInProgress) {
//			if (this.getPose() == ArmPose.MINING) {
//				// 20% into animation is the hit
//				if (this.swingProgressInt == Math.floor(this.getArmSwingAnimationEnd() * .2)) {
//					NostrumFairiesSounds.PICKAXE_HIT.play(NostrumFairies.proxy.getPlayer(), worldObj, posX, posY, posZ);
//				}
//			}
//		}
	}
	
	@Override
	public String getSpecializationName() {
		return "Builder Dwarf";
	}
	
	@Override
	public FeyStoneMaterial getCurrentSpecialization() {
		return FeyStoneMaterial.EMERALD;
	}
}
