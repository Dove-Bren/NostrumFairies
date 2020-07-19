package com.smanzana.nostrumfairies.logistics.requesters;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.entity.EntityLivingBase;

/**
 * Wraps requesting a dynamic list of items from the logistics network.
 * @author Skyler
 *
 */
public class LogisticsItemWithdrawRequester extends LogisticsItemTaskRequester<LogisticsItemWithdrawTask> {

	private boolean useBuffers;
	
	public LogisticsItemWithdrawRequester(LogisticsNetwork network, boolean useBuffers, ILogisticsComponent component) {
		super(network, component);
		this.useBuffers = useBuffers;
	}
	
	public LogisticsItemWithdrawRequester(LogisticsNetwork network, boolean useBuffers, EntityLivingBase entityRequester) {
		super(network, entityRequester);
		this.useBuffers = useBuffers;
	}
	
	@Override
	public LogisticsItemWithdrawTask makeTask(ILogisticsComponent comp, ItemDeepStack item) {
		return new LogisticsItemWithdrawTask(comp, "Item Request", item, useBuffers);
	}
	
	@Override
	public LogisticsItemWithdrawTask makeTask(EntityLivingBase entity, ItemDeepStack item) {
		return new LogisticsItemWithdrawTask(entity, "Item Request", item, useBuffers);
	}
	
	public void setUseBuffers(boolean useBuffers) {
		this.useBuffers = useBuffers;
		
		// Go and update any existing tasks
		for (LogisticsItemWithdrawTask task : this.getCurrentTasks()) {
			task.setUseBuffers(useBuffers);
		}
	}
	
	public void setNetwork(@Nullable LogisticsNetwork network) {
		this.network = network;
	}
}
