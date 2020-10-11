package com.smanzana.nostrumfairies.capabilities.fey;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class NostrumFeyCapabilityStorage implements IStorage<INostrumFeyCapability> {

	@Override
	public NBTBase writeNBT(Capability<INostrumFeyCapability> capability, INostrumFeyCapability instance, EnumFacing side) {
		return instance.toNBT();
	}

	@Override
	public void readNBT(Capability<INostrumFeyCapability> capability, INostrumFeyCapability instance, EnumFacing side, NBTBase nbt) {
		instance.readNBT((NBTTagCompound) nbt);
	}

}
