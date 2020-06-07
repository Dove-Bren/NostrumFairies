package com.smanzana.nostrumfairies.logistics;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Has the mapping between component key and factory
 * @author Skyler
 *
 */
public class LogisticsComponentRegistry  {
	
	public static interface ILogisticsComponentFactory<T extends ILogisticsComponent> {
		
		public T construct(NBTTagCompound nbt, LogisticsNetwork network);
		
	}
	
	private Map<String, ILogisticsComponentFactory<?>> registeredFactories; // runtime pre-init setup
	
	public LogisticsComponentRegistry() {
		this.registeredFactories = new HashMap<>();
	}

	public void registerComponentType(String key, ILogisticsComponentFactory<?> factory) {
		this.registeredFactories.put(key, factory);
	}
	
	public @Nullable ILogisticsComponentFactory<?> lookupFactory(String key) {
		return this.registeredFactories.get(key);
	}
}
