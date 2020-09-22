package com.smanzana.nostrumfairies.capabilities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui;
import com.smanzana.nostrumfairies.entity.IEntityListener;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory;
import com.smanzana.nostrumfairies.items.FairyGael;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Default implementation of the INostrumFeyCapability interface
 * @author Skyler
 *
 */
public class NostrumFeyCapability implements INostrumFeyCapability {

	private static final String NBT_UNLOCKED = "unlocked";
	private static final String NBT_FAIRY_SLOTS = "fairy_slots";
	private static final String NBT_FAIRY_INVENTORY = "fairy_inventory";
	
	// Capability data
	private boolean isUnlocked;
	private int fairySlots;
	private FairyHolderInventory fairyInventory;
	
	// Operational transient data
	private EntityLivingBase owner;
	private List<FairyRecord> deployedFairies;
	private int disabledTicks;
	
	public NostrumFeyCapability() {
		this.fairyInventory = new FairyHolderInventory();
		this.deployedFairies = new LinkedList<>();
		
		// defaults...
		this.fairySlots = 1;
		this.isUnlocked = false;
	}
	
	@Override
	public boolean isUnlocked() {
		return isUnlocked;
	}

	@Override
	public void unlock() {
		isUnlocked = true;
	}

	@Override
	public int getFairySlots() {
		return fairySlots;
	}

	@Override
	public void addFairySlot() {
		fairySlots++;
	}

	@Override
	public void setFairySlots(int slots) {
		fairySlots = slots;
	}

	@Override
	public FairyHolderInventory getFairyInventory() {
		return fairyInventory;
	}

	@Override
	public void setFairyInventory(FairyHolderInventory inventory) {
		if (inventory != this.fairyInventory) {
			this.retractFairies();
			this.fairyInventory = inventory;
		}
	}

	@Override
	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setBoolean(NBT_UNLOCKED, isUnlocked);
		nbt.setInteger(NBT_FAIRY_SLOTS, fairySlots);
		writeFairies();
		nbt.setTag(NBT_FAIRY_INVENTORY, fairyInventory.toNBT());
		
		return nbt;
	}

	@Override
	public void readNBT(NBTTagCompound nbt) {
		clearFairies();
		this.isUnlocked = nbt.getBoolean(NBT_UNLOCKED);
		this.fairySlots = nbt.getInteger(NBT_FAIRY_SLOTS);
		this.fairyInventory.readNBT(nbt.getCompoundTag(NBT_FAIRY_INVENTORY));
	}

	@Override
	public void provideEntity(EntityLivingBase owner) {
		if (owner != this.owner) {
			if (this.owner == null) {
				MinecraftForge.EVENT_BUS.register(this);
			}
			this.owner = owner;
			if (owner == null) {
				MinecraftForge.EVENT_BUS.unregister(this);
			}
		}
	}
	
	@Override
	public void tick() {
		if (owner == null) {
			return;
		}
		
		if (owner.isDead) {
			retractFairies();
			MinecraftForge.EVENT_BUS.unregister(this);
			this.owner = null;
			return;
		}
		
		if (this.disabledTicks > 0) {
			this.disabledTicks--;
		}
		
		if (disabledTicks == 0 && owner instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) owner;
			if (player.openContainer instanceof FairyScreenGui.FairyScreenContainer) {
				this.retractFairies();
				disabledTicks = Math.max(2, disabledTicks);
			}
		}
		
		if (fairiesEnabled()) {
			// Check and see if fairies should come out?
			int i = -1;
			while (deployedFairies.size() < this.fairySlots && i < fairyInventory.getSizeInventory()) {
				i++;
				
				ItemStack gael = this.fairyInventory.getStackInSlot(i);
				if (gael == null) {
					continue;
				}
				
				if (FairyGael.isCracked(gael)) {
					continue;
				}
				
				boolean deployed = false;
				for (FairyRecord record : deployedFairies) {
					if (record.index == i) {
						deployed = true;
						break;
					}
				}
				if (deployed) {
					continue;
				}
				
				deployFairy(i, gael, owner.worldObj, owner.posX, owner.posY, owner.posZ);
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityTick(LivingUpdateEvent event) {
		if (event.getEntityLiving() == this.owner && !owner.worldObj.isRemote) {
			this.tick();
		}
	}
	
	protected void clearFairies() {
		for (FairyRecord record : this.deployedFairies) {
			record.fairy.worldObj.removeEntity(record.fairy);
		}
		this.deployedFairies.clear();
	}
	
	@Override
	public void retractFairies() {
		writeFairies();
		clearFairies();
	}
	
	public void deployFairy(int index, ItemStack gaelStack, World world, double x, double y, double z) {
		if (gaelStack != null && gaelStack.getItem() instanceof FairyGael) {
			EntityPersonalFairy fairy = FairyGael.spawnStoredEntity(gaelStack, world, x, y, z);
			if (fairy != null) {
				// TODO have FairyGael return a different type of fairy, like a PlayerFairy or whatever.
				// TODO have it set up the type so the fairy knows how to behave
				// TODO make those fairies not persist
				
				fairy.registerListener(new FairyListener());
				fairy.setOwner(owner);
				
				FairyRecord record = new FairyRecord(fairy, index);
				this.deployedFairies.add(record);
			}
		}
	}
	
	protected void writeFairies() {
		for (FairyRecord record : this.deployedFairies) {
			ItemStack gael = this.fairyInventory.getStackInSlot(record.index);
			if (gael == null) {
				NostrumFairies.logger.warn("Tries to save fairy, but housing slot was gael-less!");
				continue; // Fairy lost
			}
			FairyGael.setStoredEntity(gael, record.fairy);
			fairyInventory.setInventorySlotContents(record.index, gael);
		}
	}
	
	private boolean removeFairy(EntityPersonalFairy fairy) {
		Iterator<FairyRecord> it = deployedFairies.iterator();
		while (it.hasNext()) {
			FairyRecord record = it.next();
			if (record.fairy == fairy) {
				it.remove();
				ItemStack gael = fairyInventory.getStackInSlot(record.index);
				if (gael != null) {
					// Grab a snapshot of the fairy just before it died
					float health = fairy.getHealth();
					boolean dead = fairy.isDead;
					fairy.setHealth(1f);
					fairy.isDead = false;
					FairyGael.setStoredEntity(gael, fairy);
					FairyGael.crack(gael);
					fairyInventory.markDirty();
					fairy.setHealth(health);
					fairy.isDead = dead;
				}
				return true;
			}
		}
		
		return false;
	}

	private final class FairyListener implements IEntityListener<EntityPersonalFairy> {
		@Override
		public void onDeath(EntityPersonalFairy entity) {
			if (removeFairy(entity)) {
				; // fairy inventory already updated
			}
		}

		@Override
		public void onDamage(EntityPersonalFairy entity, DamageSource source, double damage) {
			;
		}

		@Override
		public void onHeal(EntityPersonalFairy entity, double amount) {
			;
		}
	}
	
	private static final class FairyRecord {
		public final EntityPersonalFairy fairy;
		public final int index;
		
		public FairyRecord(EntityPersonalFairy fairy, int index) {
			this.fairy = fairy;
			this.index = index;
		}
	}

	@Override
	public void disableFairies(int ticks) {
		if (this.disabledTicks == 0) {
			this.retractFairies();
			this.disabledTicks = ticks;
		}
	}

	@Override
	public void enableFairies() {
		this.disabledTicks = 0;
	}
	
	@Override
	public boolean fairiesEnabled() {
		return this.disabledTicks == 0 && isUnlocked;
	}
}
