package com.smanzana.nostrumfairies.tiles;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class LogisticsChestTileEntity extends LogisticsTileEntity implements Container {

	private static final String NBT_INV = "inventory_contents";
	
	private NonNullList<ItemStack> slots;
	
	public LogisticsChestTileEntity(BlockEntityType<? extends LogisticsChestTileEntity> type) {
		super(type);
		slots = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
	}
	
	@Override
	public void setChanged() {
		LogisticsNetwork network = getNetwork();
		if (network != null && !this.level.isClientSide) {
			network.dirty();
		}
		super.setChanged();
	}

	@Override
	public @Nonnull ItemStack getItem(int index) {
		if (index < 0 || index >= getContainerSize())
			return ItemStack.EMPTY;
		
		return slots.get(index);
	}
	
	@Override
	public ItemStack removeItem(int index, int count) {
		if (index < 0 || index >= getContainerSize() || slots.get(index).isEmpty())
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
		
		this.setChanged();
		
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		if (index < 0 || index >= getContainerSize())
			return ItemStack.EMPTY;
		
		ItemStack stack = slots.get(index);
		slots.set(index, ItemStack.EMPTY);
		
		this.setChanged();
		return stack;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		setInventorySlotContentsDirty(index, stack);
		this.setChanged();
	}
	
	/**
	 * Sets the contents of the inventory <b>without setting the dirty flag</b>
	 * @param index
	 * @param stack
	 */
	protected void setInventorySlotContentsDirty(int index, ItemStack stack) {
		if (!canPlaceItem(index, stack))
			return;
		
		slots.set(index, stack);
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public boolean stillValid(Player player) {
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
	public void startOpen(Player player) {
	}

	@Override
	public void stopOpen(Player player) {
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		if (index < 0 || index >= getContainerSize())
			return false;
		
		return true;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < getContainerSize(); i++) {
			removeItemNoUpdate(i);
		}
	}
	
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		CompoundTag compound = new CompoundTag();
		
		for (int i = 0; i < getContainerSize(); i++) {
			if (getItem(i).isEmpty())
				continue;
			
			CompoundTag tag = new CompoundTag();
			compound.put(i + "", getItem(i).save(tag));
		}
		
		if (nbt == null)
			nbt = new CompoundTag();
		
		nbt.put(NBT_INV, compound);
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundTag nbt) {
		if (nbt == null || !nbt.contains(NBT_INV, NBT.TAG_COMPOUND))
			return;
		
		this.clearContent();
		CompoundTag items = nbt.getCompound(NBT_INV);
		for (String key : items.getAllKeys()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumFairies.logger.error("Failed reading LogisticsChest inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.of(items.getCompound(key));
			this.setItem(id, stack);
		}

		super.load(state, nbt);
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
			ItemEntity item = new ItemEntity(this.level, this.worldPosition.getX() + .5, this.worldPosition.getY() + 1, this.worldPosition.getZ() + .5, leftover);
			level.addFreshEntity(item);
		}
	}
}
