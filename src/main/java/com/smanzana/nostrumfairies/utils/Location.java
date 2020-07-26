package com.smanzana.nostrumfairies.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Location {

	private BlockPos pos;
	private int dimension;
	
	public Location(BlockPos pos, int dimension) {
		this.pos = pos;
		this.dimension = dimension;
	}
	
	public Location(World world, BlockPos pos) {
		this(pos, world.provider.getDimension());
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public int getDimension() {
		return dimension;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Location && ((Location) o).hashCode() == hashCode();
	}
	
	@Override
	public int hashCode() {
		return pos.hashCode() + 277 * dimension;
	}
	
}
