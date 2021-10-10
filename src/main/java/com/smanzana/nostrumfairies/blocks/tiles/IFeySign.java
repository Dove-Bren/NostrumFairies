package com.smanzana.nostrumfairies.blocks.tiles;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

public interface IFeySign {

	public static final double BB_MAJOR = .2125;
	public static final double BB_MINOR = .03;
	public static final AxisAlignedBB AABB_NS = new AxisAlignedBB(.5 - BB_MAJOR, 0, .5 - BB_MINOR, .5 + BB_MAJOR, .685, .5 + BB_MINOR);
	public static final AxisAlignedBB AABB_EW = new AxisAlignedBB(.5 - BB_MINOR, 0, .5 - BB_MAJOR, .5 + BB_MINOR, .685, .5 + BB_MAJOR);

	public @Nonnull ItemStack getSignIcon(IFeySign sign);
	
	public EnumFacing getSignFacing(IFeySign sign);
	
}
