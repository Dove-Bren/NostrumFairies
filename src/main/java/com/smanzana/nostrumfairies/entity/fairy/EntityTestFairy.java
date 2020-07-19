package com.smanzana.nostrumfairies.entity.fairy;

import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemDepositTask;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsSubTask.Type;
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
	public void removeItem(ItemStack stack) {
		if (heldItem != null) {
			if (ItemStacks.stacksMatch(stack, heldItem)) {
				heldItem.stackSize -= stack.stackSize;
				if (heldItem.stackSize <= 0) {
					heldItem = null;
				}
			}
		}
	}

	@Override
	protected boolean onStatusChange(FairyGeneralStatus from, FairyGeneralStatus to) {

		// We want to just drop our task if our status changes from WORKING
		if (from == FairyGeneralStatus.WORKING) {
			this.forfitTask();
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
		if (task instanceof LogisticsItemWithdrawTask) {
			boolean hasItems = true;
			LogisticsItemWithdrawTask retrieve = (LogisticsItemWithdrawTask) task;
//			Map<ILogisticsComponent, List<ItemDeepStack>> items = this.getLogisticsNetwork().getNetworkItems(false);
//			
//			for (List<ItemDeepStack> stacks : items.values()) {
//				for (ItemDeepStack deep : stacks) {
//					if (ItemStacks.stacksMatch(deep.getTemplate(), retrieve.getAttachedItem().getTemplate())) {
//						hasItems = true;
//						break;
//					}
//				}
//				
//				if (hasItems) {
//					break;
//				}
//			}
			
			if (hasItems) {
				// Check for pathing
				ILogisticsComponent source = retrieve.getSourceComponent();
				if (source == null) {
					// entity
					if (this.navigator.tryMoveToEntityLiving(retrieve.getSourceEntity(), 1.0)) {
						navigator.clearPathEntity();
						return true;
					}
				} else {
					BlockPos pos = source.getPosition();
					if (this.navigator.tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0)) {
						navigator.clearPathEntity();
						return true;
					}
				}
				
			}
		} else if (task instanceof LogisticsItemDepositTask) {
			LogisticsItemDepositTask deposit = (LogisticsItemDepositTask) task;
			
			// Check for pathing
			ILogisticsComponent source = deposit.getSourceComponent();
			if (source == null) {
				// entity
				if (this.navigator.tryMoveToEntityLiving(deposit.getSourceEntity(), 1.0)) {
					navigator.clearPathEntity();
					return true;
				}
			} else {
				BlockPos pos = source.getPosition();
				if (this.navigator.tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 1.0)) {
					navigator.clearPathEntity();
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	protected boolean shouldPerformTask(ILogisticsTask task) {
		if (heldItem != null) {
			EntityItem item = new EntityItem(this.worldObj, posX, posY, posZ, heldItem);
			worldObj.spawnEntityInWorld(item);
			heldItem = null;
		}
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
		LogisticsSubTask sub = task.getActiveSubtask();
		if (sub != null) {
			if (sub.getType() == Type.MOVE) {
				BlockPos pos = sub.getPos();
				if (worldObj.isAirBlock(pos.north())) {
					pos = pos.north();
				} else if (worldObj.isAirBlock(pos.south())) {
					pos = pos.south();
				} else if (worldObj.isAirBlock(pos.east())) {
					pos = pos.east();
				} else if (worldObj.isAirBlock(pos.west())) {
					pos = pos.west();
				}
				
				if (getPosition().distanceSq(pos) < .2) {
					task.markSubtaskComplete();
				} else if (!moveHelper.isUpdating()) {
					if (!this.getNavigator().tryMoveToXYZ(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.0f)) {
						this.moveHelper.setMoveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.0f);
					}
				}
			} else if (sub.getType() == Type.BREAK) {
				// this is where we'd play some animation?
				if (this.onGround) {
					this.jump();
					task.markSubtaskComplete();
				}
			}
		}
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
