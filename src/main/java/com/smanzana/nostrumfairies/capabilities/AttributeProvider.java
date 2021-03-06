package com.smanzana.nostrumfairies.capabilities;

import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class AttributeProvider implements ICapabilitySerializable<NBTBase> {

	@CapabilityInject(INostrumFeyCapability.class)
	public static Capability<INostrumFeyCapability> CAPABILITY = null;
	
	private INostrumFeyCapability instance = CAPABILITY.getDefaultInstance();
	private Entity entity;
	
	public AttributeProvider(Entity object) {
		this.entity = object;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CAPABILITY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CAPABILITY) {
			if (entity instanceof EntityLivingBase)
				this.instance.provideEntity((EntityLivingBase) entity);
			return (T) this.instance;
		}
		
		return null;
	}

	@Override
	public NBTBase serializeNBT() {
		return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		CAPABILITY.getStorage().readNBT(CAPABILITY, instance, null, nbt);
	}

}
