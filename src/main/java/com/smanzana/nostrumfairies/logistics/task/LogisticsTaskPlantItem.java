package com.smanzana.nostrumfairies.logistics.task;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.IPlantable;

/*
 * Travel to a location and plant a seed or sapling there
 */
public class LogisticsTaskPlantItem extends LogisticsTaskPlaceBlock {
	
	private @Nonnull ItemStack seed;
	
	protected LogisticsTaskPlantItem(@Nullable ILogisticsComponent owningComponent, @Nullable LivingEntity entity,
			String displayName, ItemStack plantable, Level world, BlockPos pos, BlockPos placeAt) {
		super(owningComponent, entity, displayName, plantable, getPlantable(plantable).getPlant(world, pos.below()), world, pos, placeAt);
		this.seed = plantable;
		
		Validate.notNull(plantable);
	}

	public LogisticsTaskPlantItem(ILogisticsComponent owningComponent, String displayName,
			ItemStack plantable, Level world, BlockPos pos) {
		this(owningComponent, displayName, plantable, world, pos, pos);
	}
	
	public LogisticsTaskPlantItem(ILogisticsComponent owningComponent, String displayName,
			ItemStack plantable, Level world, BlockPos pos, BlockPos placeAt) {
		this(owningComponent, null, displayName, plantable, world, pos, placeAt);
	}
	
	public LogisticsTaskPlantItem(LivingEntity entity, String displayName,
			ItemStack plantable, Level world, BlockPos pos) {
		this(entity, displayName, plantable, world, pos, pos);
	}
	
	public LogisticsTaskPlantItem(LivingEntity entity, String displayName,
			ItemStack plantable, Level world, BlockPos pos, BlockPos placeAt) {
		this(null, entity, displayName, plantable, world, pos, placeAt);
	}
	
	protected static IPlantable getPlantable(ItemStack seed) {
		if (seed.getItem() instanceof IPlantable) {
			return (IPlantable) seed.getItem();
		} else if (seed.getItem() instanceof BlockItem && ((BlockItem) seed.getItem()).getBlock() instanceof IPlantable) {
			return (IPlantable) ((BlockItem) seed.getItem()).getBlock();
		}
		
		throw new RuntimeException("Given a seed that is not plantable");
	}
	
	@Override
	protected boolean isSpotValid(Level world, BlockPos pos) {
		// We also check if the ground below can be planted on still
		if (!super.isSpotValid(world, pos)) {
			return false;
		}
		
		BlockState soil = world.getBlockState(pos.below());
		if (!soil.getBlock().canSustainPlant(soil, world, pos.below(), Direction.UP, getPlantable(seed))) {
			return false;
		}
		
		return true;
	}
	
}
