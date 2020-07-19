package com.smanzana.nostrumfairies.logistics.task;

import com.smanzana.nostrumfairies.utils.ItemDeepStack;

public interface ILogisticsItemTask extends ILogisticsTask {
	
	public ItemDeepStack getAttachedItem();

}
