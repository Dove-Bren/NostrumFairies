package com.smanzana.nostrumfairies.blocks;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class LogisticsChestTileEntity extends LogisticsTileEntity implements IInventory {

	private static final String NBT_INV = "inventory_contents";
	
	private ItemStack slots[];
	
	public LogisticsChestTileEntity() {
		super();
		slots = new ItemStack[getSizeInventory()];
	}
	
	@Override
	public void markDirty() {
		LogisticsNetwork network = getNetwork();
		if (network != null) {
			network.dirty();
		}
		super.markDirty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return null;
		
		return slots[index];
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index < 0 || index >= getSizeInventory() || slots[index] == null)
			return null;
		
		ItemStack stack;
		if (slots[index].stackSize <= count) {
			stack = slots[index];
			slots[index] = null;
		} else {
			stack = slots[index].copy();
			stack.stackSize = count;
			slots[index].stackSize -= count;
		}
		
		this.markDirty();
		
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return null;
		
		ItemStack stack = slots[index];
		slots[index] = null;
		
		this.markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (!isItemValidForSlot(index, stack))
			return;
		
		slots[index] = stack;
		this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index < 0 || index >= getSizeInventory())
			return false;
		
		return true;
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
		for (int i = 0; i < getSizeInventory(); i++) {
			removeStackFromSlot(i);
		}
	}
	
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		NBTTagCompound compound = new NBTTagCompound();
		
		for (int i = 0; i < getSizeInventory(); i++) {
			if (getStackInSlot(i) == null)
				continue;
			
			NBTTagCompound tag = new NBTTagCompound();
			compound.setTag(i + "", getStackInSlot(i).writeToNBT(tag));
		}
		
		if (nbt == null)
			nbt = new NBTTagCompound();
		
		nbt.setTag(NBT_INV, compound);
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt == null || !nbt.hasKey(NBT_INV, NBT.TAG_COMPOUND))
			return;
		
		this.clear();
		NBTTagCompound items = nbt.getCompoundTag(NBT_INV);
		for (String key : items.getKeySet()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumFairies.logger.error("Failed reading LogisticsChest inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.loadItemStackFromNBT(items.getCompoundTag(key));
			this.setInventorySlotContents(id, stack);
		}

		super.readFromNBT(nbt);
	}
	
	@Override
	public Collection<ItemStack> getItems() {
		List<ItemStack> list = Lists.newArrayList(slots);
		list.removeIf((stack) -> {return stack == null;});
		return list;
	}
	
}
