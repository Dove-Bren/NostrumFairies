package com.smanzana.nostrumfairies.blocks;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class LogisticsChestTileEntity extends TileEntity implements ILogisticsChest {

	private static final String NBT_INV = "inventory_contents";
	
	private ItemStack slots[];
	private LogisticsNetwork network;
	
	public LogisticsChestTileEntity() {
		slots = new ItemStack[getSizeInventory()];
	}
	
	@Override
	public void markDirty() {
		if (network != null) {
			this.network.dirty();
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
		super.readFromNBT(nbt);
		
		if (nbt == null || !nbt.hasKey(NBT_INV, NBT.TAG_COMPOUND))
			return;
		this.clear();
		NBTTagCompound items = nbt.getCompoundTag(NBT_INV);
		for (String key : items.getKeySet()) {
			int id;
			try {
				id = Integer.parseInt(key);
			} catch (NumberFormatException e) {
				NostrumFairies.logger.error("Failed reading TestChest inventory slot: " + key);
				continue;
			}
			
			ItemStack stack = ItemStack.loadItemStackFromNBT(items.getCompoundTag(key));
			this.setInventorySlotContents(id, stack);
		}
	}
	
    @Override
    public void updateContainingBlockInfo() {
    	super.updateContainingBlockInfo();
    	if (!worldObj.isRemote && this.network == null) {
			System.out.println("Setting tile entity");
			NostrumFairies.instance.getLogisticsRegistry().addNewComponent(this);
		}
    }

	@Override
	public void onJoinNetwork(LogisticsNetwork network) {
		this.network = network;
	}

	@Override
	public void onLeaveNetwork() {
		this.network = null;
	}

	@Override
	public BlockPos getPosition() {
		return this.pos;
	}

	@Override
	public Collection<ItemStack> getItems() {
		List<ItemStack> list = Lists.newArrayList(slots);
		list.removeIf((stack) -> {return stack == null;});;
		return list;
	}
	
	private static final String NBT_LOG_POS = "pos";
	private static final String NBT_LOG_DIM = "dim";

	protected NBTTagCompound baseToNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.setLong(NBT_LOG_POS, this.pos.toLong());
		tag.setInteger(NBT_LOG_DIM, this.worldObj.provider.getDimension());
		
		return tag;
	}
	
	protected static LogisticsChestTileEntity loadFromNBT(NBTTagCompound nbt, LogisticsNetwork network) {
		// We store the TE position. Hook back up!
		BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_LOG_POS));
		World world = NostrumFairies.getWorld(nbt.getInteger(NBT_LOG_DIM));
		
		if (world == null) {
			throw new RuntimeException("Failed to find world for persisted TileEntity logistics component: "
					+ nbt.getInteger(NBT_LOG_DIM));
		}
		
		TileEntity te = world.getTileEntity(pos);
		
		if (te == null) {
			throw new RuntimeException("Failed to lookup tile entity at persisted location: "
					+ pos);
		}
		
		LogisticsChestTileEntity chest = (LogisticsChestTileEntity) te;
		chest.network = network;
		return chest;
	}
	
	public @Nullable LogisticsNetwork getNetwork() {
		return network;
	}
	
}
