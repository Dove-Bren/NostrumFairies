package com.smanzana.nostrumfairies.capabilities.fey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Wraps up build tasks and how to delegate them.
 * @author Skyler
 *
 */
public class BuildTaskPlanner {

	private Map<BlockPos, EntityPersonalFairy> work;
	private IInventory inventory;
	private World world;
	
	public BuildTaskPlanner() {
		work = new HashMap<>();
	}
	
	public void setInventory(IInventory inv) {
		this.inventory = inv;
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public List<EntityPersonalFairy> clear() {
		List<EntityPersonalFairy> jobless = new LinkedList<>();
		for (EntityPersonalFairy worker : work.values()) {
			if (worker != null) {
				jobless.add(worker);
			}
		}
		work.clear();
		
		return jobless;
	}
	
	public List<EntityPersonalFairy> resetTaskList(Set<BlockPos> tasks) {
		List<EntityPersonalFairy> jobless = new LinkedList<>();
		Set<BlockPos> existing = new HashSet<>(work.keySet());
		tasks = new HashSet<>(tasks);
		
		for (BlockPos exist : existing) {
			if (tasks.remove(exist)) {
				; // Was in both sets
			} else {
				// work no longer available
				EntityPersonalFairy worker = work.remove(exist);
				if (worker != null) {
					jobless.add(worker);
				}
			}
		}
		
		// Anything left in tasks are new tasks
		for (BlockPos task : tasks) {
			work.put(task.toImmutable(), null);
		}
		
		return jobless;
	}

	public void add(BlockPos buildSpot) {
		// Not evne gonna check
		if (!work.containsKey(buildSpot)) {
			work.put(buildSpot, null);
		}
	}
	
	public EntityPersonalFairy removeTask(BlockPos pos) {
		return work.remove(pos);
	}
	
	public void finishTask(EntityPersonalFairy worker, BlockPos pos) {
		if (worker != work.get(pos)) {
			throw new RuntimeException("One worker tried to finish another's task!");
		}
		
		work.remove(pos);
		IBlockState state = TemplateBlock.GetTemplatedState(world, pos);
		world.setBlockState(pos, state);
		worker.removeItem(worker.getCarriedItems().get(0));
	}
	
	public void cleanList() {
		Iterator<Entry<BlockPos, EntityPersonalFairy>> it = work.entrySet().iterator();
		while (it.hasNext()) {
			Entry<BlockPos, EntityPersonalFairy> entry = it.next();
			EntityPersonalFairy worker = entry.getValue();
			BlockPos pos = entry.getKey();
			
			if (!NostrumMagica.isBlockLoaded(world, pos)) {
				if (worker != null && !worker.isDead) {
					worker.cancelBuildTask();
				}
				
				it.remove();
				continue;
			}
			
			IBlockState state = world.getBlockState(pos);
			if (state == null || !(state.getBlock() instanceof TemplateBlock)) {
				if (worker != null && !worker.isDead) {
					worker.cancelBuildTask();
				}
				
				it.remove();
				continue;
			}
			
			IBlockState template = TemplateBlock.GetTemplatedState(world, pos);
			if (template == null) {
				if (worker != null && !worker.isDead) {
					worker.cancelBuildTask();
				}
				
				it.remove();
				continue;
			}
			
			if (worker != null && worker.isDead) {
				entry.setValue(null);
				continue;
			}
		}
	}
	
	protected boolean isClaimable(IInventory inv, World world, BlockPos pos) {
		if (this.inventory == null) {
			return false;
		}
		
		if (!NostrumMagica.isBlockLoaded(world, pos)) {
			return false;
		}
		
		ItemStack needed = TemplateBlock.GetRequiredItem(TemplateBlock.GetTemplatedState(world, pos));
		if (needed == null) {
			return false;
		}
		
		return Inventories.contains(inv, needed);
	}
	
	public @Nullable BlockPos claimTask(EntityPersonalFairy worker) {
		// Careful; I'm lazy and call here with a null worker to check if one exists
		for (BlockPos task : work.keySet()) {
			if (work.get(task) == null && isClaimable(inventory,world, task)) {
				work.put(task, worker);
				return task;
			}
		}
		
		return null;
	}
	
	public boolean hasUnclaimedTasks() {
		return claimTask(null) != null;
	}
}
