package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/*
 * Go to and then harvest a crop
 */
public class LogisticsTaskHarvest extends LogisticsTaskBase {
	
	private static enum Phase {
		IDLE,
		MOVING,
		HARVESTING,
		DONE,
	}
	
	private String displayName;
	private Level world;
	private BlockPos crop;
	private ILogisticsComponent owningComponent;
	
	private IItemCarrierFey fairy;
	private Phase phase;
	private LogisticsSubTask moveTask;
	private LogisticsSubTask workTask;
	
	private int animCount;
	
	private long lastCropCheck;
	private boolean lastCropResult;

	public LogisticsTaskHarvest(ILogisticsComponent owningComponent, String displayName, Level world, BlockPos pos) {
		this.displayName = displayName;
		this.world = world;
		this.crop = pos;
		this.owningComponent = owningComponent;
		phase = Phase.IDLE;
	}
	
	@Override
	public String getDisplayName() {
		return displayName + " (" + phase.name() + (animCount > 0 ? ("[" + animCount + "]") : "") + " - " + crop + ")";
	}

	@Override
	public boolean canDrop() {
		return false;
	}

	@Override
	public boolean canAccept(IFeyWorker worker) {
		LogisticsNetwork network = worker.getLogisticsNetwork();
		if (network == null) {
			return false;
		}
		
		if (!(worker instanceof IItemCarrierFey)) {
			return false;
		}
		
		lastCropCheck = 0;
		
		return true;
	}

	@Override
	public void onDrop(IFeyWorker worker) {
		releaseTasks();
		this.fairy = null;
		phase = Phase.IDLE;
	}

	@Override
	public void onAccept(IFeyWorker worker) {
		this.fairy = (IItemCarrierFey) worker;
		phase = Phase.MOVING;
		animCount = 0;
		tryTasks(worker);
	}
	
	@Override
	public void onRevoke() {
		;
	}

	@Override
	public boolean canMerge(ILogisticsTask other) {
		return false;
	}
	
	@Override
	public ILogisticsTask mergeIn(ILogisticsTask other) {
		return this;
	}

	@Override
	public @Nullable ILogisticsComponent getSourceComponent() {
		return owningComponent;
	}
	
	public @Nullable LivingEntity getSourceEntity() {
		return null;
	}

	@Override
	public Collection<ILogisticsTask> unmerge() {
		return Lists.newArrayList(this);
	}
	
	public boolean isActive() {
		return fairy != null;
	}
	
	public @Nullable IItemCarrierFey getCurrentWorker() {
		return fairy;
	}
	
	public BlockPos getCropPos() {
		return crop;
	}
	
	public Level getWorld() {
		return world;
	}
	
	private void releaseTasks() {
		moveTask = null;
		workTask = null;
	}
	
	private boolean tryTasks(IFeyWorker fairy) {
		releaseTasks();
		
		// Make move task
		moveTask = LogisticsSubTask.Move(crop);
		workTask = LogisticsSubTask.Break(crop);
				
		return true;
	}

	@Override
	public LogisticsSubTask getActiveSubtask() {
		switch (phase) {
		case IDLE:
			return null;
		case MOVING:
			return moveTask;
		case HARVESTING:
			return workTask;
		case DONE:
			return null;
		}
		
		return null;
	}
	
	@Override
	public void markSubtaskComplete() {
		switch (phase) {
		case IDLE:
			; // ?
			break;
		case MOVING:
		{
			phase = Phase.HARVESTING;
			float secs = NostrumFairies.random.nextInt(6) + 7;
			animCount = (int) Math.ceil(secs); // only .5 of a swing per second
			break;
		}
		case HARVESTING:
			// Moved to harvest. Spend time harvesting!
			if (animCount > 0) {
				animCount--;
			} else {
				phase = Phase.DONE;
				harvestCrop();
			}
			break;
		case DONE:
			break;
		}
	}
	
	@Override
	public boolean isComplete() {
		return phase == Phase.DONE;
	}
	
	private void harvestCrop() {
		world.destroyBlock(crop, true);
	}
	
	@Override
	public boolean isValid() {
		if (this.moveTask == null) {
			return false; // never will be...
		}
		
		if (this.phase != Phase.DONE) {
			if (lastCropCheck == 0 || this.world.getGameTime() - lastCropCheck > 100) {
				// Validate there's still a crop there
				lastCropResult = !world.isEmptyBlock(crop);
				lastCropCheck = world.getGameTime();
			}
			
			if (!lastCropResult) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public BlockPos getStartPosition() {
		return crop;
	}
}
