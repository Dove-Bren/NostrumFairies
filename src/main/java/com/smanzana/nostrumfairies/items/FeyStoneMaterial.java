package com.smanzana.nostrumfairies.items;

import com.smanzana.nostrumfairies.inventory.FeySlotType;

public enum FeyStoneMaterial {

	EMERALD(FeySlotType.SPECIALIZATION),
	GARNET(FeySlotType.SPECIALIZATION),
	AQUAMARINE(FeySlotType.SPECIALIZATION),
	RUBY(FeySlotType.UPGRADE, FeySlotType.DOWNGRADE),
	SAPPHIRE(FeySlotType.UPGRADE, FeySlotType.DOWNGRADE);
	
	private FeySlotType[] types;
	
	private FeyStoneMaterial(FeySlotType ... matchingTypes) {
		this.types = matchingTypes;
	}
	
	public boolean existsForSlot(FeySlotType slot) {
		for (FeySlotType type : types) {
			if (type == slot) {
				return true;
			}
		}
		
		return false;
	}
	
}
