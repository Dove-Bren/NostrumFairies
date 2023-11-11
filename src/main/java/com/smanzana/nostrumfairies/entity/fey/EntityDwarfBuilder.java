package com.smanzana.nostrumfairies.entity.fey;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskBuildBlock;
import com.smanzana.nostrumfairies.serializers.ArmPoseDwarf;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class EntityDwarfBuilder extends EntityDwarf {
	
	protected static Map<World, Map<BlockPos, BlockPos>> BuildSpots = new HashMap<>();
	
	protected static void AddBuildSpot(World world, BlockPos pos, BlockPos spot) {
		Map<BlockPos, BlockPos> map = BuildSpots.get(world);
		if (map == null) {
			map = new HashMap<>();
			BuildSpots.put(world, map);
		}
		
		map.put(pos, spot);
	}
	
	protected static @Nullable BlockPos GetBuildSpot(World world, BlockPos pos) {
		Map<BlockPos, BlockPos> map = BuildSpots.get(world);
		if (map == null) {
			return null;
		}
		
		return map.get(pos);
	}
	
	protected static void ClearBuildSpot(World world, BlockPos pos) {
		Map<BlockPos, BlockPos> map = BuildSpots.get(world);
		if (map == null) {
			return;
		}
		
		map.remove(pos);
	}

	public EntityDwarfBuilder(EntityType<? extends EntityDwarfBuilder> type, World world) {
		super(type, world);
		
		this.workDistanceSq = 24 * 24;
	}
	
	protected @Nullable BlockPos findBuildSpot(BlockPos buildCell) {
		BlockPos buildPos = GetBuildSpot(world, buildCell);
		if (buildPos != null) {
			return buildPos;
		}
		
		buildPos = findEmptySpot(buildCell, false, true);
		
		// Is the block we shifted to where we are?
		if (this.getPosition().equals(buildPos) || this.getDistanceSqToCenter(buildPos) <= 1) {
			AddBuildSpot(world, buildCell, buildPos);
			return buildPos; // do nothing. Next loop will call it 'success'
		}
		if (this.getNavigator().tryMoveToXYZ(buildPos.getX(), buildPos.getY(), buildPos.getZ(), 1.0f)) {
			AddBuildSpot(world, buildCell, buildPos);
			return buildPos; // Sweet.
		}
		
		// Otherwise try and find a place underneat the build spot to walk to instead
		MutableBlockPos cursor = new MutableBlockPos();
		final int yIncr = 1 * (buildPos.getY() > posY ? -1 : 1);
		
		boolean lastSolid = true;
		for (int x = -1; x <= 1; x++)
		for (int z = -1; z <= 1; z++) {
			cursor.setPos(buildCell.getX() + x, buildCell.getY() + yIncr, buildCell.getZ() + z);
			
			for (int i = 0; i < 15; i++) {
				if (cursor.getY() <= 0) {
					break;
				}
				if (x == 0 && z == 0 && i == 0) {
					continue;
				}
				if (!lastSolid) {
					// Last block was breathable. Are we suddenly in solid ground?
					lastSolid = world.getBlockState(cursor).getMaterial().blocksMovement();
					if (lastSolid) {
						// Can we path there?
						//System.out.println("Solid: " + cursor);
						BlockPos candidate = cursor.toImmutable().up();
						if (this.getPosition().equals(candidate)
								|| this.getDistanceSqToCenter(candidate) <= 1
								|| this.getNavigator().tryMoveToXYZ(candidate.getX(), candidate.getY(), candidate.getZ(), 1.0f)) {
							AddBuildSpot(world, buildCell, candidate);
							return candidate; // Yay
						}
					}
				} else {
					// Last block was solid so we can't stand there. Keep travelling down looking for air
					lastSolid = world.getBlockState(cursor).getMaterial().blocksMovement();
				}
				cursor.setY(cursor.getY() + yIncr);
			}
		}
		
		return null;
	}
	
	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		if (task instanceof LogisticsTaskBuildBlock) {
			// TODO require a specialization\
			LogisticsTaskBuildBlock build = (LogisticsTaskBuildBlock) task;
			
			if (build.getWorld() != this.world) {
				return false;
			}
			
			// Check where the spot is
			BlockPos target = build.getTargetPlaceLoc();
			if (target == null || !this.canReach(target, true)) {
				return false;
			}
			
			// Find a better block to stand, if we weren't told explicitely to stand there
			if (target == build.getTargetBlock()) {
				target = findBuildSpot(target);
				if (target == null) {
					return false;
				}
			}
			
			return true;
			
//			// Check for pathing
//			if (this.getDistanceSq(target) < 1) {
//				// extra case for if the navigator refuses cause we're too close
//				return true;
//			}
//			
//			//Path currentPath = navigator.getPath();
//			boolean success = navigator.tryMoveToXYZ(target.getX(), target.getY(), target.getZ(), 1.0);
//			if (success) {
//				success = Paths.IsComplete(navigator.getPath(), target, 2);
//				navigator.clearPath();
//			}
			
			//if (!success) {
				// Builders just teleport
//				this.setPosition(target.getX() + .5, target.getY(), target.getZ() + .5);
				
			//}
			
//			if (currentPath == null) {
//				if (!success) {
//					navigator.setPath(currentPath, 1.0);
//				}
//			} else {
//				navigator.setPath(currentPath, 1.0);
//			}
//			if (success) {
//				return true;
//			} else 
		}
		
		return false;
	}
	
	@Override
	protected void onTaskTick(ILogisticsTask task) {
		
		// Builder dwarves have special movement options...
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null && sub.getType() == LogisticsSubTask.Type.MOVE && task instanceof LogisticsTaskBuildBlock) {
			this.setPose(ArmPoseDwarf.IDLE);
			if (this.navigator.noPath()) {
				// First time through?
				if ((movePos != null && this.getDistanceSqToCenter(movePos) < 1)
					|| (moveEntity != null && this.getDistance(moveEntity) < 1)) {
					task.markSubtaskComplete();
					if (movePos != null) {
						ClearBuildSpot(world, movePos);
					}
					movePos = null;
					moveEntity = null;
					return;
				}
				movePos = null;
				moveEntity = null;
				
				movePos = sub.getPos();
				if (movePos == null) {
					moveEntity = sub.getEntity();
					if (!this.getNavigator().tryMoveToEntityLiving(moveEntity,  1)) {
						this.getMoveHelper().setMoveTo(moveEntity.posX, moveEntity.posY, moveEntity.posZ, 1.0f);
					}
				} else {
					BlockPos betterSpot = findBuildSpot(movePos);
					// If a blockpos is returned, may have already set path
					if (betterSpot != null) {
						movePos = betterSpot;
					}
					
					if (this.getNavigator().noPath()) {
						if (!navigator.tryMoveToXYZ(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0)) {
							//this.getMoveHelper().setMoveTo(movePos.getX(), movePos.getY(), movePos.getZ(), 1.0f);
							this.setPosition(movePos.getX() + .5, movePos.getY(), movePos.getZ() + .5);
						}
					}
				}
			}
		} else {
			super.onTaskTick(task);
		}
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.22D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(3.0D);
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
		
//		if (world.isRemote && isSwingInProgress) {
//			if (this.getPose() == ArmPose.MINING) {
//				// 20% into animation is the hit
//				if (this.swingProgressInt == Math.floor(this.getArmSwingAnimationEnd() * .2)) {
//					NostrumFairiesSounds.PICKAXE_HIT.play(NostrumFairies.proxy.getPlayer(), world, posX, posY, posZ);
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
