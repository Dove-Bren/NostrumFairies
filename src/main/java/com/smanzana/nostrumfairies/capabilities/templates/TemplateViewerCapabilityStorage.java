package com.smanzana.nostrumfairies.capabilities.templates;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TemplateViewerCapabilityStorage implements IStorage<ITemplateViewerCapability> {

	@Override
	public NBTBase writeNBT(Capability<ITemplateViewerCapability> capability, ITemplateViewerCapability instance, Direction side) {
		return new CompoundNBT();
	}

	@Override
	public void readNBT(Capability<ITemplateViewerCapability> capability, ITemplateViewerCapability instance, Direction side, NBTBase nbt) {
		;
	}

}
