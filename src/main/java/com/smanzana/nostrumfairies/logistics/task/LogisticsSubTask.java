package com.smanzana.nostrumfairies.logistics.task;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;

public final class LogisticsSubTask {
	
	public static enum Type {
		MOVE,
		BREAK,
		ATTACK,
	}
	
	private Type type;
	private BlockPos pos;
	private EntityLivingBase entity;
	
	private LogisticsSubTask(Type type, BlockPos pos, EntityLivingBase entity) {
		this.type = type;
		this.pos = pos;
		this.entity = entity;
	}
	
	public static LogisticsSubTask Move(BlockPos pos) {
		return new LogisticsSubTask(Type.MOVE, pos, null);
	}
	
	public static LogisticsSubTask Break(BlockPos pos) {
		return new LogisticsSubTask(Type.BREAK, pos, null);
	}
	
	public static LogisticsSubTask Attack(EntityLivingBase entity) {
		return new LogisticsSubTask(Type.ATTACK, null, entity);
	}

	public Type getType() {
		return type;
	}

	public BlockPos getPos() {
		return pos;
	}

	public EntityLivingBase getEntity() {
		return entity;
	}
	
	
}
