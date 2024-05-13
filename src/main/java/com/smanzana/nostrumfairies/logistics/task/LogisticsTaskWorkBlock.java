package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/*
 * Go to a block and then 'work' it until something signals it's done.
 */
public abstract class LogisticsTaskWorkBlock extends LogisticsTaskBase {
	
	private static enum Phase {
		IDLE,
		MOVING,
		WORKING,
		DONE,
	}
	
	private String displayName;
	private World world;
	private BlockPos block;
	private ILogisticsComponent owningComponent;
	
	private IItemCarrierFey fairy;
	private Phase phase;
	private LogisticsSubTask moveTask;
	private LogisticsSubTask workTask;
	
	private boolean done;
	
	public LogisticsTaskWorkBlock(ILogisticsComponent owningComponent, String displayName, World world, BlockPos pos) {
		this.displayName = displayName;
		this.world = world;
		this.block = pos;
		this.owningComponent = owningComponent;
		phase = Phase.IDLE;
	}
	
	@Override
	public String getDisplayName() {
		return displayName + " (" + phase.name() + " - " + block + ")";
	}

	@Override
	public boolean canDrop() {
		return true;
	}

	@Override
	public boolean canAccept(IFeyWorker worker) {
		LogisticsNetwork network = worker.getLogisticsNetwork();
		if (network == null) {
			return false;
		}
		
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
		tryTasks(worker);
		done = false;
	}
	
	@Override
	public void onRevoke() {
		done = false;
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
	
	public BlockPos getBlockPos() {
		return block;
	}
	
	public World getWorld() {
		return world;
	}
	
	private void releaseTasks() {
		moveTask = null;
		workTask = null;
	}
	
	private boolean tryTasks(IFeyWorker fairy) {
		releaseTasks();
		
		// Make move task
		moveTask = LogisticsSubTask.Move(block);
		workTask = LogisticsSubTask.Break(block);
				
		return true;
	}

	@Override
	public LogisticsSubTask getActiveSubtask() {
		switch (phase) {
		case IDLE:
			return null;
		case MOVING:
			return moveTask;
		case WORKING:
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
			phase = Phase.WORKING;
			break;
		}
		case WORKING:
			// Interacting. Keep calling work command until told to stop
			this.workBlock();
			if (this.done) {
				phase = Phase.DONE;
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
	
	@Override
	public boolean isValid() {
		if (this.moveTask == null) {
			return false; // never will be...
		}
		
		// could check if there's space to put it, and after some time, drop it on the floor.
		// requester should cancel if the item is taken away before the worker gets there.
		
		return true;
	}
	
	@Override
	public BlockPos getStartPosition() {
		return block;
	}
	
	public void markComplete() {
		this.done = true;
	}
	
	protected abstract void workBlock();
}
