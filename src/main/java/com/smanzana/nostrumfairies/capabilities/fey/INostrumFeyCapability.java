package com.smanzana.nostrumfairies.capabilities.fey;

import org.apache.commons.lang3.tuple.Pair;

import com.smanzana.nostrumfairies.inventory.FairyHolderInventory;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface INostrumFeyCapability extends INBTSerializable<CompoundTag> {

	// Unlock
	public boolean isUnlocked();
	public void unlock();
	
	// Enable/Disable
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
	
	// Fairy Slots/progression
	public int getFairySlots();
	public void addFairySlot();
	public void setFairySlots(int slots);
	public int getFairyXP();
	public int getFairyMaxXP();
	public int getFairyLevel();
	public void addFairyXP(int xp);
	
	public FairyHolderInventory getFairyInventory();
	public void setFairyInventory(FairyHolderInventory inventory);
	
	public boolean attackFairyUnlocked();
	public boolean builderFairyUnlocked();
	public boolean logisticsFairyUnlocked();
//	public void unlockAttackFairy();
//	public void unlockBuilderFairy();
//	public void unlockLogisticsFairy();
	
	public void retractFairies();
	public void deactivateFairies(int ticks);
	public void reactivateFairies();
	public boolean fairiesDeactivated();
	
	// Templating
	public Pair<BlockPos, BlockPos> getTemplateSelection();
	public void clearTemplateSelection();
	public void addTemplateSelection(BlockPos pos);
	
	// Building
	public void addBuildSpot(BlockPos pos);
	
	// Operation
	public void tick();
}
