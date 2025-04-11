package com.smanzana.nostrumfairies.blocks;

import javax.annotation.Nonnull;

import net.minecraft.world.level.block.Block;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IFeySign {

	public static final double BB_MAJOR = .2125 * 16;
	public static final double BB_MINOR = .03 * 16;
	public static final VoxelShape AABB_NS = Block.box(8 - BB_MAJOR, 0, 8 - BB_MINOR, 8 + BB_MAJOR, 10.96, 8 + BB_MINOR);
	public static final VoxelShape AABB_EW = Block.box(8 - BB_MINOR, 0, 8 - BB_MAJOR, 8 + BB_MINOR, 10.96, 8 + BB_MAJOR);

	@OnlyIn(Dist.CLIENT)
	public @Nonnull ResourceLocation getSignIcon(IFeySign sign);
	
	public Direction getSignFacing(IFeySign sign);
	
}
