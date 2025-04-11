package com.smanzana.nostrumfairies.capabilities.fey;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class NostrumFeyCapabilityStorage implements IStorage<INostrumFeyCapability> {

	@Override
	public Tag writeNBT(Capability<INostrumFeyCapability> capability, INostrumFeyCapability instance, Direction side) {
		return instance.toNBT();
	}

	@Override
	public void readNBT(Capability<INostrumFeyCapability> capability, INostrumFeyCapability instance, Direction side, Tag nbt) {
		instance.readNBT((CompoundTag) nbt);
	}

}
