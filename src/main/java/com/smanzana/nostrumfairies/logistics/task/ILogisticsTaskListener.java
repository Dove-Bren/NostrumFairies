package com.smanzana.nostrumfairies.logistics.task;

import com.smanzana.nostrumfairies.entity.fairy.IFairyWorker;

public interface ILogisticsTaskListener {

	public void onTaskDrop(ILogisticsTask task, IFairyWorker worker);

	public void onTaskAccept(ILogisticsTask task, IFairyWorker worker);
	
	public void onTaskComplete(ILogisticsTask task, IFairyWorker worker);
	
}
