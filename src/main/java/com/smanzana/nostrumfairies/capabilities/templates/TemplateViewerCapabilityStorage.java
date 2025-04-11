package com.smanzana.nostrumfairies.capabilities.templates;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class TemplateViewerCapabilityStorage implements IStorage<ITemplateViewerCapability> {

	@Override
	public Tag writeNBT(Capability<ITemplateViewerCapability> capability, ITemplateViewerCapability instance, Direction side) {
		return new CompoundTag();
	}

	@Override
	public void readNBT(Capability<ITemplateViewerCapability> capability, ITemplateViewerCapability instance, Direction side, Tag nbt) {
		;
	}

}
