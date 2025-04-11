package com.smanzana.nostrumfairies.entity;

import net.minecraft.util.StringRepresentable;

public enum ResidentType implements StringRepresentable {

	FAIRY,
	ELF,
	DWARF,
	GNOME;

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase();
	}
	
	@Override
	public String toString() {
		return this.getSerializedName();
	}
	
}
