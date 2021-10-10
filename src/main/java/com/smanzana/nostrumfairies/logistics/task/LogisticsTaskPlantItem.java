package com.smanzana.nostrumfairies.logistics.task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

/*
 * Travel to a location and plant a seed or sapling there
 */
public class LogisticsTaskPlantItem extends LogisticsTaskPlaceBlock {
	
	private @Nonnull ItemStack seed;
	
	protected LogisticsTaskPlantItem(@Nullable ILogisticsComponent owningComponent, @Nullable EntityLivingBase entity,
			String displayName, ItemStack plantable, World world, BlockPos pos, BlockPos placeAt) {
		super(owningComponent, entity, displayName, plantable, getPlantable(plantable).getPlant(world, pos.down()), world, pos, placeAt);
		this.seed = plantable;
		
		Validate.notNull(plantable);
	}

	public LogisticsTaskPlantItem(ILogisticsComponent owningComponent, String displayName,
			ItemStack plantable, World world, BlockPos pos) {
		this(owningComponent, displayName, plantable, world, pos, pos);
	}
	
	public LogisticsTaskPlantItem(ILogisticsComponent owningComponent, String displayName,
			ItemStack plantable, World world, BlockPos pos, BlockPos placeAt) {
		this(owningComponent, null, displayName, plantable, world, pos, placeAt);
	}
	
	public LogisticsTaskPlantItem(EntityLivingBase entity, String displayName,
			ItemStack plantable, World world, BlockPos pos) {
		this(entity, displayName, plantable, world, pos, pos);
	}
	
	public LogisticsTaskPlantItem(EntityLivingBase entity, String displayName,
			ItemStack plantable, World world, BlockPos pos, BlockPos placeAt) {
		this(null, entity, displayName, plantable, world, pos, placeAt);
	}
	
	protected static IPlantable getPlantable(ItemStack seed) {
		if (seed.getItem() instanceof IPlantable) {
			return (IPlantable) seed.getItem();
		} else if (seed.getItem() instanceof ItemBlock && ((ItemBlock) seed.getItem()).getBlock() instanceof IPlantable) {
			return (IPlantable) ((ItemBlock) seed.getItem()).getBlock();
		}
		
		throw new RuntimeException("Given a seed that is not plantable");
	}
	
	@Override
	protected boolean isSpotValid(World world, BlockPos pos) {
		// We also check if the ground below can be planted on still
		if (!super.isSpotValid(world, pos)) {
			return false;
		}
		
		IBlockState soil = world.getBlockState(pos.down());
		if (!soil.getBlock().canSustainPlant(soil, world, pos.down(), EnumFacing.UP, getPlantable(seed))) {
			return false;
		}
		
		return true;
	}
	
}
