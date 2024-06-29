package com.smanzana.nostrumfairies.tiles;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class LogisticsChestTileEntity extends LogisticsTileEntity implements IInventory {

	private static final String NBT_INV = "inventory_contents";
	
	private NonNullList<ItemStack> slots;
	
	public LogisticsChestTileEntity(TileEntityType<? extends LogisticsChestTileEntity> type) {
		super(type);
		slots = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
	}
	
	@Override
	public void markDirty() {
		LogisticsNetwork network = getNetwork();
		if (network != null && !this.world.isRemote) {
			network.dirty();
		}
		super.markDirty();
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		return slots.get(index);
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index < 0 || index >= getSizeInventory() || slots.get(index).isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack stack;
		if (slots.get(index).getCount() <= count) {
			stack = slots.get(index);
			slots.set(index, ItemStack.EMPTY);
		} else {
			stack = slots.get(index).copy();
			stack.setCount(count);
			slots.get(index).shrink(count);
		}
		
		this.markDirty();
		
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (index < 0 || index >= getSizeInventory())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots.get(index);
		slots.set(index, ItemStack.EMPTY);
		
		this.markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		setInventorySlotContentsDirty(index, stack);
		this.markDirty();
	}
	
	/**
	 * Sets the contents of the inventory <b>without setting the dirty flag</b>
	 * @param index
	 * @param stack
	 */
	protected void setInventorySlotContentsDirty(int index, ItemStack stack) {
		if (!isItemValidForSlot(index, stack))
			return;
		
		slots.set(index, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		for (ItemStack stack : slots) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void openInventory(PlayerEntity player) {
	}

	@Override
	public void closeInventory(PlayerEntity player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (index < 0 || index >= getSizeInventory())
			return false;
		
		return true;
	}

	@Override
	public void clear() {
		for (int i = 0; i < getSizeInventory(); i++) {
			removeStackFromSlot(i);
		}
	}
	
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		CompoundNBT compound = new CompoundNBT();
		
		for (int i = 0; i < getSizeInventory(); i++) {
			if (getStackInSlot(i).isEmpty())
				continue;
			
			CompoundNBT tag = new CompoundNBT();
			compound.put(i + "", getStackInSlot(i).write(tag));
		}
		
		if (nbt == null)
			nbt = new CompoundNBT();
		
		nbt.put(NBT_INV, compound);
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		if (nbt == null || !nbt.contains(NBT_INV, NBT.TAG_COMPOUND))
			return;
		
		this.clear();
		CompoundNBT items = nbt.getCompound(NBT_INV);
		for (String key : items.keySet()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumFairies.logger.error("Failed reading LogisticsChest inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.read(items.getCompound(key));
			this.setInventorySlotContents(id, stack);
		}

		super.read(state, nbt);
	}
	
	@Override
	public Collection<ItemStack> getItems() {
		// Can this have empties in it? If so, just return slots...?
		List<ItemStack> list = Lists.newArrayList(slots);
		list.removeIf((stack) -> {return stack.isEmpty();});
		return list;
	}
	
	@Override
	public void takeItem(ItemStack stack) {
		super.takeItem(stack);
		Inventories.remove(this, stack);
	}
	
	@Override
	public void addItem(ItemStack stack) {
		//super.addItem(stack);
		ItemStack leftover = Inventories.addItem(this, stack);
		if (!leftover.isEmpty()) {
			ItemEntity item = new ItemEntity(this.world, this.pos.getX() + .5, this.pos.getY() + 1, this.pos.getZ() + .5, leftover);
			world.addEntity(item);
		}
	}
}
