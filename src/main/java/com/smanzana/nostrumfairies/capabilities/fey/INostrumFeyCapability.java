package com.smanzana.nostrumfairies.capabilities.fey;

import org.apache.commons.lang3.tuple.Pair;

import com.smanzana.nostrumfairies.inventory.FairyHolderInventory;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface INostrumFeyCapability {

	// Unlock
	public boolean isUnlocked();
	public void unlock();
	
	// Fairy Slots/progression
	public int getFairySlots();
	public void addFairySlot();
	public void setFairySlots(int slots);
	
	public FairyHolderInventory getFairyInventory();
	public void setFairyInventory(FairyHolderInventory inventory);
	
	public boolean attackFairyUnlocked();
	public boolean builderFairyUnlocked();
	public boolean logisticsFairyUnlocked();
//	public void unlockAttackFairy();
//	public void unlockBuilderFairy();
//	public void unlockLogisticsFairy();
	
	public void retractFairies();
	public void disableFairies(int ticks);
	public void enableFairies();
	public boolean fairiesEnabled();
	
	// Templating
	public Pair<BlockPos, BlockPos> getTemplateSelection();
	public void clearTemplateSelection();
	public void addTemplateSelection(BlockPos pos);
	
	// Building
	public void addBuildSpot(BlockPos pos);
	
	// Operation
	public void tick();
	public void provideEntity(EntityLivingBase owner);
	
	
	// Serialization
	public NBTTagCompound toNBT();
	public void readNBT(NBTTagCompound nbt);
}
