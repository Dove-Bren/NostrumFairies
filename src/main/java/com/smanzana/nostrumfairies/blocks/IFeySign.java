package com.smanzana.nostrumfairies.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IFeySign {

	public static final double BB_MAJOR = .2125 * 16;
	public static final double BB_MINOR = .03 * 16;
	public static final VoxelShape AABB_NS = Block.makeCuboidShape(8 - BB_MAJOR, 0, 8 - BB_MINOR, 8 + BB_MAJOR, 10.96, 8 + BB_MINOR);
	public static final VoxelShape AABB_EW = Block.makeCuboidShape(8 - BB_MINOR, 0, 8 - BB_MAJOR, 8 + BB_MINOR, 10.96, 8 + BB_MAJOR);

	@OnlyIn(Dist.CLIENT)
	public @Nonnull ResourceLocation getSignIcon(IFeySign sign);
	
	public Direction getSignFacing(IFeySign sign);
	
}
