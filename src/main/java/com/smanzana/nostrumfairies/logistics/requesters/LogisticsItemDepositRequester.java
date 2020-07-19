package com.smanzana.nostrumfairies.logistics.requesters;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.entity.EntityLivingBase;

/**
 * Wraps and manages making requests to have items picked up and put into the network
 * @author Skyler
 *
 */
public class LogisticsItemDepositRequester extends LogisticsItemTaskRequester<LogisticsItemDepositTask> {

	public LogisticsItemDepositRequester(LogisticsNetwork network, ILogisticsComponent component) {
		super(network, component);
	}
	
	public LogisticsItemDepositRequester(LogisticsNetwork network, EntityLivingBase entityRequester) {
		super(network, entityRequester);
	} 

	@Override
	protected List<LogisticsItemDepositTask> filterActiveRequests(List<LogisticsItemDepositTask> taskList) {
		// One DIFFERENT approach to 'hiding' tasks that have picked up the item this way would be to make this override
		// setting the item list... and pass a map down to the base class that represents the diff - the items that
		// have been picked up?
		
		List<LogisticsItemDepositTask> ret = new ArrayList<>(taskList.size());
		for (LogisticsItemDepositTask task : taskList) {
			if (!task.hasTakenItems()) {
				ret.add(task);
			}
		}
		// java 8 could do this better...
		return ret;
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
