package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.blocks.WoodcuttingBlock;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

/*
 * Go to and then chop down a tree. Does not pick up the items.
 */
public class LogisticsTaskChopTree implements ILogisticsTask {
	
	private static enum Phase {
		IDLE,
		MOVING,
		CHOPPING,
		DONE,
	}
	
	private String displayName;
	private World world;
	private BlockPos trunk;
	private BlockPos chopAt;
	private ILogisticsComponent owningComponent;
	
	private IItemCarrierFey fairy;
	private Phase phase;
	private LogisticsSubTask moveTask;
	private LogisticsSubTask workTask;
	
	private int animCount;
	
	private long lastTreeCheck;
	private boolean lastTreeResult;

	public LogisticsTaskChopTree(ILogisticsComponent owningComponent, String displayName, World world, BlockPos pos, BlockPos chopAt) {
		this.displayName = displayName;
		this.world = world;
		this.trunk = pos;
		this.chopAt = chopAt;
		this.owningComponent = owningComponent;
		phase = Phase.IDLE;
	}
	
	public LogisticsTaskChopTree(ILogisticsComponent owningComponent, String displayName, World world, BlockPos pos) {
		this(owningComponent, displayName, world, pos, pos);
	}
	
	@Override
	public String getDisplayName() {
		return displayName + " (" + phase.name() + (animCount > 0 ? ("[" + animCount + "]") : "") + " - " + trunk + ")";
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
		
		lastTreeCheck = 0;
		
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
	
	public @Nullable EntityLivingBase getSourceEntity() {
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
	
	public BlockPos getTrunkPos() {
		return trunk;
	}
	
	public BlockPos getChopLocation() {
		return chopAt;
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
		moveTask = LogisticsSubTask.Move(chopAt);
		workTask = LogisticsSubTask.Break(trunk);
				
		return true;
	}

	@Override
	public LogisticsSubTask getActiveSubtask() {
		switch (phase) {
		case IDLE:
			return null;
		case MOVING:
			return moveTask;
		case CHOPPING:
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
			phase = Phase.CHOPPING;
			float secs = getTreeHeight() * 4;
			animCount = (int) Math.ceil(secs * .5); // only .5 of a swing per second
			break;
		}
		case CHOPPING:
			// Moved to chop. Spend time chopping!
			if (animCount > 0) { // TODO base on size of tree?
				animCount--;
			} else {
				phase = Phase.DONE;
				breakTree();
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
	
	private int getTreeHeight() {
		int count = 0;
		MutableBlockPos pos = new MutableBlockPos(trunk);
		
		while (WoodcuttingBlock.isTrunkMaterial(world, pos)) {
			count++;
			pos.move(EnumFacing.UP);
		}
		
		return count;
	}
	
	private void breakTreeInternal(Set<BlockPos> visitted, BlockPos pos) {
		if (visitted.contains(pos)) {
			return;
		}
		
		boolean isTrunk = WoodcuttingBlock.isTrunkMaterial(world, pos);
		//if (isTrunk || WoodcuttingBlock.isLeafMaterial(world, pos)) { // ends up chopping nearby trees to easily
		if (isTrunk) {
			if (isTrunk) {
				world.destroyBlock(pos, true);
			}
			
			visitted.add(pos);
			
			breakTreeInternal(visitted, pos.east());
			breakTreeInternal(visitted, pos.west());
			breakTreeInternal(visitted, pos.north());
			breakTreeInternal(visitted, pos.south());
			
			// corners
			breakTreeInternal(visitted, pos.north().east());
			breakTreeInternal(visitted, pos.north().west());
			breakTreeInternal(visitted, pos.south().east());
			breakTreeInternal(visitted, pos.south().west());
			
			BlockPos up = pos.up();
			
			breakTreeInternal(visitted, up);
			
			breakTreeInternal(visitted, up.east());
			breakTreeInternal(visitted, up.west());
			breakTreeInternal(visitted, up.north());
			breakTreeInternal(visitted, up.south());
			
			// corners
			breakTreeInternal(visitted, up.north().east());
			breakTreeInternal(visitted, up.north().west());
			breakTreeInternal(visitted, up.south().east());
			breakTreeInternal(visitted, up.south().west());
		}
		// else die here ;-;
	}
	
	private void breakTree() {
		// This should be kept somewhat in sync with the merging code in the woodcutting block's task creation
		// As of writing, we go horizontally up to 1 block (but over and over if there's wood) and up forever.
		Set<BlockPos> visitted = new HashSet<>();
		breakTreeInternal(visitted, trunk);
	}
	
	@Override
	public boolean isValid() {
		if (this.moveTask == null) {
			return false; // never will be...
		}
		
		if (this.phase != Phase.DONE) {
			if (lastTreeCheck == 0 || this.world.getTotalWorldTime() - lastTreeCheck > 100) {
				// Validate there's still a tree there
				lastTreeResult = WoodcuttingBlock.isTrunkMaterial(world, trunk);
				lastTreeCheck = world.getTotalWorldTime();
			}
			
			if (!lastTreeResult) {
				return false;
			}
		}
		
		// could check if there's space to put it, and after some time, drop it on the floor.
		// requester should cancel if the item is taken away before the worker gets there.
		
		return true;
	}
	
	@Override
	public BlockPos getStartPosition() {
		return trunk;
	}
}
