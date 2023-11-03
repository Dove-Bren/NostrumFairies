package com.smanzana.nostrumfairies.entity;

import net.minecraft.util.IStringSerializable;

public enum ResidentType implements IStringSerializable {

	FAIRY,
	ELF,
	DWARF,
	GNOME;

	@Override
	public String getName() {
		return this.name().toLowerCase();
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
}
