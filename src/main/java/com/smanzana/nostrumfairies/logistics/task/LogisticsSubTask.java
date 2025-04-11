package com.smanzana.nostrumfairies.logistics.task;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;

public final class LogisticsSubTask {
	
	public static enum Type {
		MOVE,
		BREAK,
		ATTACK,
		IDLE,
	}
	
	private Type type;
	private BlockPos pos;
	private Entity entity;
	
	private LogisticsSubTask(Type type, BlockPos pos, Entity entity) {
		this.type = type;
		this.pos = pos;
		this.entity = entity;
	}
	
	public static LogisticsSubTask Move(BlockPos pos) {
		return new LogisticsSubTask(Type.MOVE, pos, null);
	}
	
	public static LogisticsSubTask Move(Entity entity) {
		return new LogisticsSubTask(Type.MOVE, null, entity);
	}
	
	public static LogisticsSubTask Break(BlockPos pos) {
		return new LogisticsSubTask(Type.BREAK, pos, null);
	}
	
	public static LogisticsSubTask Attack(LivingEntity entity) {
		return new LogisticsSubTask(Type.ATTACK, null, entity);
	}
	
	public static LogisticsSubTask Idle(BlockPos pos) {
		return new LogisticsSubTask(Type.IDLE, pos, null);
	}

	public Type getType() {
		return type;
	}

	public BlockPos getPos() {
		return pos;
	}

	public Entity getEntity() {
		return entity;
	}
	
	
}
