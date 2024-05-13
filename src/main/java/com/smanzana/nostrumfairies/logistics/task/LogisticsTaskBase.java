package com.smanzana.nostrumfairies.logistics.task;

public abstract class LogisticsTaskBase implements ILogisticsTask {
	
	public static int LastTaskID = 0;
	
	protected final int taskID;
	
	protected LogisticsTaskBase() {
		this(LastTaskID++);
	}
	
	protected LogisticsTaskBase(int taskID) {
		this.taskID = taskID;
	}
	
	@Override
	public int getTaskID() {
		return this.taskID;
	}

}
