package com.smanzana.nostrumfairies.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class Location {

	private BlockPos pos;
	private DimensionType dimension;
	
	public Location(BlockPos pos, DimensionType dimension) {
		this.pos = pos;
		this.dimension = dimension;
	}
	
	public Location(World world, BlockPos pos) {
		this(pos, world.getDimension().getType());
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public DimensionType getDimension() {
		return dimension;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Location && ((Location) o).hashCode() == hashCode();
	}
	
	@Override
	public int hashCode() {
		return pos.hashCode() + 277 * dimension.getId();
	}
	
	private static final String NBT_DIM = "dim";
	private static final String NBT_POS = "pos";
	
	public CompoundNBT toNBT() {
		return toNBT(new CompoundNBT());
	}
	
	public CompoundNBT toNBT(CompoundNBT tag) {
		tag.putInt(NBT_DIM, dimension.getId());
		tag.put(NBT_POS, NBTUtil.writeBlockPos(pos));
		return tag;
	}
	
	public static Location FromNBT(CompoundNBT tag) {
		return new Location(
				NBTUtil.readBlockPos(tag.getCompound(NBT_POS)),
				DimensionType.getById(tag.getInt(NBT_DIM))
				);
	}
	
}
