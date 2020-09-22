package com.smanzana.nostrumfairies.inventory;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.items.FairyGael;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants.NBT;

public class FairyHolderInventory implements IInventory {

	private static final int INV_SIZE = 27;
	
	private ItemStack[] slots;
	
	private boolean dirty;
	private NBTTagCompound nbtCache;
	
	public FairyHolderInventory() {
		slots = new ItemStack[INV_SIZE];
	}
	
	@Override
	public String getName() {
		return "Fairy Inventory";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(this.getName());
	}

	@Override
	public int getSizeInventory() {
		return INV_SIZE;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= INV_SIZE) {
			return null;
		}
		
		return slots[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		ItemStack inSlot = getStackInSlot(index);
		ItemStack removed = null;
		if (inSlot != null) {
			removed = inSlot.splitStack(count);
			if (inSlot.stackSize <= 0) {
				setInventorySlotContents(index, null);
			}
			
			this.markDirty();
		}
		
		return removed;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack inSlot = getStackInSlot(index);
		if (inSlot != null) {
			this.setInventorySlotContents(index, null);
			this.markDirty();
		}
		return inSlot;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index < 0 || index >= INV_SIZE) {
			return;
		}
		
		slots[index] = stack;
		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public void markDirty() {
		this.dirty = true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		;
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return stack == null || stack.getItem() instanceof FairyGael;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		slots = new ItemStack[INV_SIZE];
		this.markDirty();
	}
	
	public NBTTagCompound toNBT() {
		if (this.dirty || this.nbtCache == null) {
			this.nbtCache = new NBTTagCompound();
			
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < INV_SIZE; i++) {
				ItemStack inSlot = slots[i];
				NBTTagCompound tag = new NBTTagCompound();
				if (inSlot != null) {
					inSlot.writeToNBT(tag);
				}
				list.appendTag(tag);
			}
			nbtCache.setTag("contents", list);
			
			this.dirty = false;
		}
		
		return nbtCache;
	}
	
	public void readNBT(NBTTagCompound nbt) {
		this.dirty = false;
		this.nbtCache = nbt.copy();
		
		this.clear();
		NBTTagList list = nbt.getTagList("contents", NBT.TAG_COMPOUND);
		for (int i = 0; i < INV_SIZE && i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			@Nullable ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
			slots[i] = stack; // May be null :)
		}
	}

}
