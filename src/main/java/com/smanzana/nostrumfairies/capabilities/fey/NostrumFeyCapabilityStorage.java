package com.smanzana.nostrumfairies.capabilities.fey;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class NostrumFeyCapabilityStorage implements IStorage<INostrumFeyCapability> {

	@Override
	public NBTBase writeNBT(Capability<INostrumFeyCapability> capability, INostrumFeyCapability instance, Direction side) {
		return instance.toNBT();
	}

	@Override
	public void readNBT(Capability<INostrumFeyCapability> capability, INostrumFeyCapability instance, Direction side, NBTBase nbt) {
		instance.readNBT((CompoundNBT) nbt);
	}

}
