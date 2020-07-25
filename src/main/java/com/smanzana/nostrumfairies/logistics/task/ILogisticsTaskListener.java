package com.smanzana.nostrumfairies.logistics.task;

import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;

public interface ILogisticsTaskListener {

	public void onTaskDrop(ILogisticsTask task, IFeyWorker worker);

	public void onTaskAccept(ILogisticsTask task, IFeyWorker worker);
	
	public void onTaskComplete(ILogisticsTask task, IFeyWorker worker);
	
}
