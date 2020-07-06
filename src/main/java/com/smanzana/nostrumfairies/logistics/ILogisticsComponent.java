package com.smanzana.nostrumfairies.logistics;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILogisticsComponent {

	/**
	 * Called when the network component is added to a network
	 * @param network
	 */
	public void onJoinNetwork(LogisticsNetwork network);
	
	/**
	 * Called when the component has been removed from a network.
	 * Note: This could be because the network is being merged into another or otherwise destroyed. This, of course,
	 * will be followed by on onJoinNetwork in that case.
	 */
	public void onLeaveNetwork();
	
	/**
	 * Return the range at which jobs can be serviced from this component.
	 * @return
	 */
	public double getLogisticRange();
	
	/**
	 * Return the range other network components can still be linked to from this component.
	 * @return
	 */
	public double getLogisticsLinkRange();
	
	/**
	 * Return the position of this component
	 * @return
	 */
	public BlockPos getPosition();
	
	/**
	 * Return which dimension this component is in
	 * @return
	 */
	public World getWorld();
	
	/**
	 * Return any items this component makes available to the network
	 * @return
	 */
	public Collection<ItemStack> getItems();
	
	/**
	 * Check and return whether this component can store the provided item stack.
	 * @param stack
	 * @return
	 */
	public boolean canAccept(ItemStack stack);
	
	// public float getEnergyProduction(); // and consumption? What drives all of this?
	
	/**
	 * Construct an NBTTagCompound representation of this logistics component.
	 * If this component is stored in a block, the block specifics should be omitted.
	 * Only enough to be deserialized to remain part of the network (and looked up later
	 * for linking) should be included.
	 * @return
	 */
	public NBTTagCompound toNBT();
	
	public String getSerializationTag();
	
	public void takeItem(ItemStack stack);
	
	public void addItem(ItemStack stack);
	
}
