package com.smanzana.nostrumfairies.entity.fairy;

import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskRegistry;
import com.smanzana.nostrumfairies.utils.ItemStacks;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityTestFairy extends EntityFairyBase implements IItemCarrierFairy {

	private static final String NBT_ITEM = "helditem";
	
	private ItemStack heldItem;
	
	public EntityTestFairy(World world) {
		super(world);
		this.height = .6f;
		System.out.println("Creating fairy");
	}

	@Override
	public String getLoreKey() {
		return "testfairy";
	}

	@Override
	public String getLoreDisplayName() {
		return "testfairy";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore("test lore for test fairy lol");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore("test lore for test fairy lol");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ENTITY;
	}

	@Override
	public ItemStack[] getCarriedItems() {
		return new ItemStack[]{heldItem};
	}

	@Override
	public boolean canAccept(ItemStack stack) {
		return heldItem == null ||
				(ItemStacks.stacksMatch(heldItem, stack) && heldItem.stackSize + stack.stackSize < heldItem.getMaxStackSize());
	}

	@Override
	public void addItem(ItemStack stack) {
		if (heldItem == null) {
			heldItem = stack.copy();
		} else {
			// Just assume canAccept was called
			heldItem.stackSize += stack.stackSize; 
		}
	}

	@Override
	protected boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to) {

		// We want to just drop our task if our status changes from WORKING
		if (from == FairyGeneralStatus.WORKING) {
			if (this.getCurrentTask() != null) {
				LogisticsTaskRegistry.instance().forfitTask(this.getCurrentTask());
				this.forceSetTask(null);
			}
		}
		
		return true;
	}

	@Override
	protected boolean isValidHome(BlockPos homePos) {
		TileEntity te = worldObj.getTileEntity(homePos);
		if (te == null || !(te instanceof StorageLogisticsChest.StorageChestTileEntity)) {
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean canPerformTask(ILogisticsTask task) {
		return true;
	}

	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		return this.heldItem == null;
	}

	@Override
	protected void onTaskChange(ILogisticsTask oldTask, ILogisticsTask newTask) {
		if (oldTask != null && heldItem != null) {
			// I guess drop our item
			EntityItem item = new EntityItem(this.worldObj, posX, posY, posZ, heldItem);
			worldObj.spawnEntityInWorld(item);
			heldItem = null;
		}
	}

	@Override
	protected void onTaskTick(ILogisticsTask task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initEntityAI() {
		// TODO Auto-generated method stub
		// I guess we should wander and check if tehre's a home nearby and if so make it our home and stop wandering.
		// Or if we're revolting... just quit for this test one?
		// Or if we're working, dont use AI
		// Or if we're idle... wander?
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.24D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(2.0D);
		//this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		
		if (heldItem != null) {
			compound.setTag(NBT_ITEM, heldItem.writeToNBT(new NBTTagCompound()));
		}
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		
		if (compound.hasKey(NBT_ITEM)) {
			heldItem = ItemStack.loadItemStackFromNBT(compound.getCompoundTag(NBT_ITEM));
		}
	}
}
