package com.smanzana.nostrumfairies.inventory;

public enum FeySlotType {
	
	SPECIALIZATION("specialization"),
	UPGRADE("upgrade"),
	DOWNGRADE("downgrade"),
	EITHERGRADE("eithergrade");
	
	private final String id;
	
	private FeySlotType(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}
}
