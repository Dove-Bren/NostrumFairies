package com.smanzana.nostrumfairies.logistics.requesters;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrumfairies.entity.fairy.IFairyWorker;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.entity.EntityLivingBase;

/**
 * Wraps and manages making requests to have items picked up and put into the network
 * @author Skyler
 *
 */
public class LogisticsItemDepositRequester extends LogisticsItemTaskRequester<LogisticsItemDepositTask> {

	private List<LogisticsItemWithdrawTask> runningTasks;
	
	public LogisticsItemDepositRequester(LogisticsNetwork network, ILogisticsComponent component) {
		super(network, component);
		this.runningTasks = new LinkedList<>();
	}
	
	public LogisticsItemDepositRequester(LogisticsNetwork network, EntityLivingBase entityRequester) {
		super(network, entityRequester);
		this.runningTasks = new LinkedList<>();
	} 

	@Override
	public void onTaskDrop(ILogisticsTask task, IFairyWorker worker) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTaskAccept(ILogisticsTask task, IFairyWorker worker) {
		// TODO Auto-generated method stub
		
		// TODO is this good enough? Move an item out of 'current tasks' and put it into 'running tasks' ?
		// But probably actually need to see if the task is far enough that the item is picked up?
		// Basically, need to make sure that the fairy taking hte item out of the inventory _doesn't_ cause the
		// task that picked it up to disappear, but a player picking an item out of the inventory should
		
		//broke();
		// this is where we crash for now, since requests get pulled out from under things.
		// On one hand, I think this can be accomplished by ignoring tasks that already have the item
		// taken in this requester. But I want to step back and think about the 
		// network -> task registry -> fairy -> task -> subtask structure, since I so frequently have teo
		// think about what order things are happpening and wondering how 'done' tasks should get cleaned up.
		// Like probably fairies should get tasks and then run them until they are done and then let the registry know?
		// Or the registry should notice done tasks? I'm not sure, but creating a better downstream flow might help
		// improve the situation.
	}

	@Override
	protected LogisticsItemDepositTask makeTask(ILogisticsComponent component, ItemDeepStack item) {
		return new LogisticsItemDepositTask(component, "Item Deposit Request", item.splitStack(1));
	}

	@Override
	protected LogisticsItemDepositTask makeTask(EntityLivingBase entity, ItemDeepStack item) {
		return new LogisticsItemDepositTask(entity, "Item Deposit Request", item.splitStack(1));
	}
	
}
