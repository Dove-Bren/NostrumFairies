package com.smanzana.nostrumfairies.logistics.task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.smanzana.nostrumfairies.entity.fairy.EntityFairyBase;

import net.minecraft.util.math.BlockPos;

/**
 * Global registry of all active fairy task requesting blocks.
 * Even if the blocks aren't loaded, this presumably has all the blocks 
 * @author Skyler
 */
public class LogisticsTaskRegistry {
	
	private static final class RegistryItem {
		
		private ILogisticsTask task;
		private EntityFairyBase actor;
		
		public RegistryItem(ILogisticsTask task) {
			this.task = task;
			this.actor = null;
		}
		
		public boolean hasActor() {
			return actor != null;
		}
		
		public void setActor(EntityFairyBase actor) {
			this.actor = actor;
		}
	}
	
	public static final class FairyTaskPair {
		
		public final BlockPos pos;
		public final ILogisticsTask task;
		
		public FairyTaskPair(BlockPos pos, ILogisticsTask task) {
			this.pos = pos;
			this.task = task;
		}
	}
	
	private static LogisticsTaskRegistry instance = null;
	
	public static LogisticsTaskRegistry instance() {
		if (instance == null) {
			instance = new LogisticsTaskRegistry();
		}
		
		return instance;
	}
	
	// Map between dim ID and known requesters
	private Map<Integer, Map<BlockPos, RegistryItem>> registry;
	
	private LogisticsTaskRegistry() {
		registry = new HashMap<>();
	}
	
	public void clear() {
		registry.clear();
	}
	
	public void register(int dimension, BlockPos pos, ILogisticsTask task) {
		Map<BlockPos, RegistryItem> dimReg = registry.get(dimension);
		
		if (dimReg == null) {
			dimReg = new HashMap<>();
		}
		
		dimReg.put(pos, new RegistryItem(task));
	}
	
	public void revoke(int dimension, BlockPos pos) {
		Map<BlockPos, RegistryItem> dimReg = registry.get(dimension);
		
		if (dimReg == null) {
			return;
		}
		
		RegistryItem item = dimReg.remove(pos);
		if (item != null) {
			if (item.hasActor()) {
				item.actor.cancelTask();
			}
		}
	}
	
	public List<FairyTaskPair> findTasks(int dimension, BlockPos center, double maxDistanceSq, EntityFairyBase actor, @Nullable Predicate<ILogisticsTask> filter) {
		List<FairyTaskPair> list = new LinkedList<>();
		Map<BlockPos, RegistryItem> dimReg = registry.get(dimension);
		
		if (dimReg != null) {
			dimReg.forEach((pos, record) -> {
				if (record.hasActor()) {
					return;
				}
				
				if (pos.distanceSq(center) > maxDistanceSq) {
					return;
				}
				
				if (filter != null && !filter.apply(record.task)) {
					return;
				}
				
				if (!record.task.canAccept(actor)) {
					return;
				}
				
				list.add(new FairyTaskPair(pos, record.task));
			});
		}
		
		return list;
	}
	
	public void claimTask(int dimension, BlockPos pos, EntityFairyBase actor) {
		Map<BlockPos, RegistryItem> dimReg = registry.get(dimension);
		
		if (dimReg != null) {
			RegistryItem record = dimReg.get(pos);
			if (record != null) {
				record.setActor(actor);
				record.task.onAccept(actor); // May remove from registry
			}
		}
	}
	
	public void forfitTask(int dimension, BlockPos pos) {
		Map<BlockPos, RegistryItem> dimReg = registry.get(dimension);
		
		if (dimReg != null) {
			RegistryItem record = dimReg.get(pos);
			if (record != null) {
				final EntityFairyBase actor = record.actor;
				record.setActor(null);
				record.task.onDrop(actor); // May remove from registry
			}
		}
	}
}
