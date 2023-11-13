package com.smanzana.nostrumfairies.capabilities;

import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class AttributeProvider implements ICapabilitySerializable<INBT> {

	@CapabilityInject(INostrumFeyCapability.class)
	public static Capability<INostrumFeyCapability> CAPABILITY = null;
	
	private INostrumFeyCapability instance = CAPABILITY.getDefaultInstance();
	private Entity entity;
	
	public AttributeProvider(Entity object) {
		this.entity = object;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CAPABILITY) {
			if (entity instanceof LivingEntity)
				this.instance.provideEntity((LivingEntity) entity);
			return LazyOptional.of(() -> this.instance).cast();
		}
		
		return LazyOptional.empty();
	}

	@Override
	public INBT serializeNBT() {
		return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(INBT nbt) {
		CAPABILITY.getStorage().readNBT(CAPABILITY, instance, null, nbt);
	}

}
