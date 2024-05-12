package com.smanzana.nostrumfairies.logistics.task;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/*
 * Travel to a block and mine it. If the block drops anything, return it to the surface.
 */
public class LogisticsTaskMineBlock implements ILogisticsTask {
	
	private static enum Phase {
		IDLE,
		MOVING,
		MINING,
		RETURNING,
		DONE,
	}
	
	private String displayName;
	private World world;
	private BlockPos block;
	private BlockPos mineAt;
	private ILogisticsComponent owningComponent;
	private @Nullable LogisticsTaskMineBlock[] prereqs; // Pre-req tasks if any. Useful for mines which create paths
														// themselves, eliminating the need for pathfinding checks
	
	private @Nullable List<ILogisticsTask> mergedTasks;
	private @Nullable LogisticsTaskMineBlock compositeTask; // Task we were merged into
	
	private IItemCarrierFey fairy;
	private Phase phase;
	private LogisticsSubTask moveTask;
	private LogisticsSubTask workTask;
	private LogisticsSubTask returnTask;
	
	private int animCount;
	private long lastOreCheck;
	private boolean lastOreResult;

	public LogisticsTaskMineBlock(ILogisticsComponent owningComponent, String displayName,
			World world, BlockPos pos, @Nullable LogisticsTaskMineBlock[] prereqs) {
		this(owningComponent, displayName, world, pos, pos, prereqs);
	}
	
	public LogisticsTaskMineBlock(ILogisticsComponent owningComponent, String displayName,
			World world, BlockPos pos, BlockPos mineAt, @Nullable LogisticsTaskMineBlock[] prereqs) {
		this.displayName = displayName;
		this.block = pos;
		this.mineAt = mineAt;
		this.world = world;
		this.owningComponent = owningComponent;
		this.prereqs = (prereqs == null ? new LogisticsTaskMineBlock[0] : prereqs);
		phase = Phase.IDLE;
	}
	
	private static LogisticsTaskMineBlock makeComposite(LogisticsTaskMineBlock left, LogisticsTaskMineBlock right) {
		LogisticsTaskMineBlock composite;
		// Just take left's pos for now
		composite = new LogisticsTaskMineBlock(left.owningComponent, left.displayName, left.world, left.block, left.mineAt, left.prereqs);
		
		// pull registry stuff
		composite.fairy = left.fairy;
		
		composite.mergedTasks = new LinkedList<>();
		composite.mergedTasks.add(left);
		composite.mergedTasks.add(right);
		
		composite.phase = left.phase;
		composite.animCount = left.animCount;
		composite.tryTasks(left.fairy);
		
		left.compositeTask = composite;
		right.compositeTask = composite;
		return composite;
	}
	
	public boolean hasPrereqs() {
		return prereqs.length > 0;
	}

	@Override
	public String getDisplayName() {
		return displayName + " (" + (this.mergedTasks == null ? block : "Multiple blocks") + " - " + phase.name() + ")";
	}

	@Override
	public boolean canDrop() {
		return false;
	}

	@Override
	public boolean canAccept(IFeyWorker worker) {
		if (!(worker instanceof IItemCarrierFey)) {
			return false;
		}
		
		LogisticsNetwork network = worker.getLogisticsNetwork();
		if (network == null) {
			return false;
		}
		
		for (LogisticsTaskMineBlock prereq : prereqs) {
			boolean found = false;
			if (prereq == null) {
				continue;
			}
			
			if ((prereq.isComplete() || prereq.phase == Phase.RETURNING) || (prereq.isActive() && prereq.fairy == worker)) {
				found = true;
				break;
			}
			if (!found) {
				return false;
			}
		}
		
		if (!world.isAreaLoaded(block, 1)) {
			return false;
		}
		
		if (!isValid()) {
			return false;
		}
		
		 return true;
	}

	@Override
	public void onDrop(IFeyWorker worker) {
		// If part of a composite, let it know that this subtask has been dropped
		if (this.compositeTask != null) {
			this.compositeTask.dropMerged(this);
		}
		
		releaseTasks();
		
		this.fairy = null;
		phase = Phase.IDLE;
	}
	
	@Override
	public void onRevoke() {
		// Only need cleanup if we were being worked, which would call onDrop.
	}

	@Override
	public void onAccept(IFeyWorker worker) {
		this.fairy = (IItemCarrierFey) worker;
		phase = Phase.MOVING;
		animCount = 0;
		tryTasks(worker);
		
		lastOreCheck = 0; //reset so 'isValid' runs fully the first time
	}

	@Override
	public boolean canMerge(ILogisticsTask other) {
		if (this.phase == Phase.RETURNING || this.phase == Phase.DONE) {
			return false;
		}
		
		// Don't grab more tasks if you've already mined at least one of the children.
		// This should ease up on gravel disasters by the first dwarf in the list.
		if (this.mergedTasks != null) {
			for (ILogisticsTask task : mergedTasks) {
				LogisticsTaskMineBlock subtask = (LogisticsTaskMineBlock) task;
				if (subtask.phase == Phase.RETURNING || subtask.phase == Phase.DONE) {
					return false;
				}
			}
		}
		
		if (other instanceof LogisticsTaskMineBlock) {
			LogisticsTaskMineBlock otherTask = (LogisticsTaskMineBlock) other;
			
			// Are these requests from the same place?
			if (owningComponent != otherTask.owningComponent) {
				return false;
			}
			
			// We limit mining tasks to stacks of 8 to avoid having to try and guess how many items come out.
			if (this.mergedTasks != null && this.mergedTasks.size() >= 8) {
				return false;
			}
			
			// I was going to check if it was the same type of block to limit the number of TYPES of items,
			// but I'm just gonna give dwarfs some good inventory sizes
			
			return true;
		}
		
		return false;
	}
	
	private void dropMerged(LogisticsTaskMineBlock otherTask) {
		this.mergedTasks.remove(otherTask);
	}
	
	private void mergeToComposite(LogisticsTaskMineBlock otherTask) {
		mergedTasks.add(otherTask);
		otherTask.compositeTask = this;
	}

	@Override
	public ILogisticsTask mergeIn(ILogisticsTask other) {
		// If already a composite, just add. Otherwise, make a composite!
		if (this.mergedTasks == null) {
			return makeComposite(this, (LogisticsTaskMineBlock) other);
		} //else
		
		this.mergeToComposite((LogisticsTaskMineBlock) other);
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
		if (mergedTasks == null) {
			return Lists.newArrayList(this);
		}
		
		return Lists.newArrayList(mergedTasks);
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public BlockPos getTargetBlock() {
		return block;
	}
	
	public BlockPos getTargetMineLoc() {
		return mineAt;
	}
	
	public boolean isActive() {
		return fairy != null;
	}
	
	public @Nullable IItemCarrierFey getCurrentWorker() {
		return fairy;
	}
	
	private void releaseTasks() {
		workTask = null;
		moveTask = null;
		returnTask = null;
	}
	
	private boolean tryTasks(IFeyWorker fairy) {
		releaseTasks();
		
		// composites only have a returnTask
		this.returnTask = LogisticsSubTask.Move(owningComponent.getPosition());
		
		if (mergedTasks == null) {
			moveTask = LogisticsSubTask.Move(mineAt);
			workTask = LogisticsSubTask.Break(block);
			
			// Figure out hardness for anim count
			// TODO different tools for dwarves?
			BlockState state = world.getBlockState(block);
			float secs = (state.getBlockHardness(world, block)) * 5;
			
			if (state.getMaterial().isLiquid()) {
				secs = .5f;
			}
			
			// could put tool stuff here
			
			animCount = (int) Math.ceil(secs * 1); // 1 break completes per second
		}
		
		return true;
	}
	
	private @Nullable LogisticsTaskMineBlock getFirstUnfinishedChild() {
		if (this.mergedTasks == null) {
			return null;
		}
		
		for (ILogisticsTask subtask : this.mergedTasks) {
			LogisticsTaskMineBlock task = (LogisticsTaskMineBlock) subtask;
			if (task.phase == Phase.MOVING || task.phase == Phase.MINING) {
				return task;
			}
		}
		
		return null;
	}

	@Override
	public LogisticsSubTask getActiveSubtask() {
		// Composite defer to subtasks for MOVING and MINING
		
		@Nullable LogisticsTaskMineBlock subtask = getFirstUnfinishedChild();
		switch (phase) {
		case IDLE:
			return null;
		case MOVING:
			if (this.mergedTasks != null) {
				if (subtask == null) {
					// Error case. Something broke our block while we were moving maybe?
					markSubtaskComplete();
					return null;
				} else {
					return subtask.getActiveSubtask();
				}
			} else {
				return moveTask;
			}
		case MINING:
			if (this.mergedTasks != null) {
				if (subtask == null) {
					// Error case. Something broke our block while we were moving maybe?
					markSubtaskComplete();
					return null;
				} else {
					return subtask.getActiveSubtask();
				}
			} else {
				return workTask;
			}
		case RETURNING:
			return returnTask;
		case DONE:
			return null;
		}
		
		return null;
	}
	
	@Override
	public void markSubtaskComplete() {
		// If composite, find the child that this actually is talking about if we're in a work phase
		@Nullable LogisticsTaskMineBlock subtask = getFirstUnfinishedChild();
		
		switch (phase) {
		case IDLE:
			; // ?
			break;
		case MOVING:
			if (this.mergedTasks != null) {
				if (subtask == null) {
					// somehow ended up with no MINING phase.
					phase = Phase.RETURNING;
				} else {
					// set our phase for better printing, and update the child task
					phase = Phase.MINING;
					subtask.markSubtaskComplete();
				}
			} else {
				phase = Phase.MINING;
			}
			break;
		case MINING:
			// If composite, echo down as always. If there's another subtask afterwards,
			// go back to 'moving' state
			if (this.mergedTasks != null) {
				if (subtask == null) {
					// this state is substate was mining but didn't actually go to mining?
					phase = Phase.RETURNING;
				} else {
					// note: marking subtask complete here first
					LogisticsTaskMineBlock lastSubtask = subtask;
					subtask.markSubtaskComplete();
					
					subtask = getFirstUnfinishedChild();
					if (subtask == null) {
						phase = Phase.RETURNING; // no more undone children!
					} else if (subtask == lastSubtask) {
						// same subtask. Animation continues
					} else {
						phase = Phase.MOVING;
					}
					
				}
			} else {
				if (animCount > 0) {
					animCount--;
				} else {
					phase = Phase.RETURNING;
					mineBlock();
				}
			}
			
			break;
		case RETURNING:
			dropItems();
			phase = Phase.DONE;
			
			// Update subtasks, if any, too
			if (mergedTasks != null) {
				for (ILogisticsTask task : mergedTasks) {
					((LogisticsTaskMineBlock) task).phase = Phase.DONE;
				}
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
	
	private void mineBlock() {
		BlockState state = world.getBlockState(block);
		
		NonNullList<ItemStack> drops = NonNullList.create();
		if (state.getBlock() instanceof FallingBlock) {
			// Walk and DESTROY ALL GRAVEL that's up
			BlockPos.Mutable cursor = new BlockPos.Mutable().setPos(block);
			do {
				drops.addAll(Block.getDrops(state, (ServerWorld) world, cursor, world.getTileEntity(cursor)));
				world.destroyBlock(cursor, false);
				
				cursor.move(Direction.UP);
				state = world.getBlockState(cursor);
			} while (cursor.getY() < 256 && state.getBlock() instanceof FallingBlock);
		} else {
			drops.addAll(Block.getDrops(state, (ServerWorld) world, block, world.getTileEntity(block)));
			world.destroyBlock(block, false);
		}
		
		// Try to add them all to the dwarf
		for (ItemStack drop : drops) {
			if (fairy.canAccept(drop)) {
				fairy.addItem(drop);
			} else {
				// drop on the floor
				world.addEntity(new ItemEntity(world, block.getX() + .5, block.getY() + .5, block.getZ() + .5, drop));
			}
		}
	}
	
	private void dropItems() {
		IItemCarrierFey worker = fairy; // capture before making changes!
		// Just instruct the dwarf to drop all that they have :)
		
		NonNullList<ItemStack> heldItems = worker.getCarriedItems();
		NonNullList<ItemStack> copies = NonNullList.create();
		double x;
		double y;
		double z;
		if (fairy instanceof LivingEntity) {
			x = ((LivingEntity) fairy).getPosX();
			y = ((LivingEntity) fairy).getPosY();
			z = ((LivingEntity) fairy).getPosZ();
		} else {
			BlockPos pos = owningComponent.getPosition();
			x = pos.getX() + .5;
			y = pos.getY() + 1.5;
			z = pos.getZ() + .5;
		}
		
		for (int i = 0; i < heldItems.size(); i++) {
			if (heldItems.get(i).isEmpty()) {
				continue;
			}
			copies.add(heldItems.get(i).copy());
		}
		
		for (ItemStack stack : copies) {
			if (stack.isEmpty()) {
				continue;
			}
			fairy.removeItem(stack);
			world.addEntity(new ItemEntity(world, x, y, z, stack));
		}
	}
	
	public boolean minedBlock() {
		return this.phase == Phase.RETURNING || this.phase == Phase.DONE;
	}

	@Override
	public boolean isValid() {
		if (this.mergedTasks != null) {
			// If this task was a merged one but all things have been pulled out, no longer valid!
			if (this.mergedTasks.isEmpty()) {
				return false;
			}
			
			// Otherwise, check children (assuming we're moving/mining)
			if (this.phase == Phase.IDLE || this.phase == Phase.MOVING || phase == Phase.MINING) {
				for (ILogisticsTask child : this.mergedTasks) {
					// could walk backwards, and drop children that no longer are valid.
					// Gonna just be greedy and fail the whole thing here which will mean things like
					// players 'helping' will thrash the AI.
					if (!child.isValid()) {
						return false;
					}
				}
			}
		} else {
			if (this.phase == Phase.IDLE || this.phase == Phase.MOVING || phase == Phase.MINING) {
				// Make sure block is still there
				if (lastOreCheck == 0 || this.world.getGameTime() - lastOreCheck > 100) {
					lastOreResult = !world.isAirBlock(block) && world.getBlockState(block).getBlockHardness(world, block) >= 0;
					lastOreCheck = world.getGameTime();
				}
				
				if (!lastOreResult) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public BlockPos getStartPosition() {
		return mineAt;
	}
}
