package com.smanzana.nostrumfairies.capabilities.templates;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TemplateViewerCapabilityStorage implements IStorage<ITemplateViewerCapability> {

	@Override
	public NBTBase writeNBT(Capability<ITemplateViewerCapability> capability, ITemplateViewerCapability instance, EnumFacing side) {
		return new NBTTagCompound();
	}

	@Override
	public void readNBT(Capability<ITemplateViewerCapability> capability, ITemplateViewerCapability instance, EnumFacing side, NBTBase nbt) {
		;
	}

}
