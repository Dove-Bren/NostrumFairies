package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.items.FairyItems;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;

/**
 * @author Skyler
 *
 */
public class FeyBush extends BushBlock {
	
	public static final String ID = "fey_bush";
	
	public FeyBush() {
		super(Block.Properties.of(Material.PLANT)
				.sound(SoundType.GRASS)
				.noCollission()
				);
	}
	
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }
	
	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
		boolean ret = super.canSustainPlant(state, world, pos, facing, plantable);
		
		return ret;
	}

//	@Override
//	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
//		return Lists.newArrayList(new ItemStack(this));
//	}

	public InteractionResult getEntityInteraction(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		if (target instanceof EntityFeyBase && !(target instanceof EntityPersonalFairy)) {
			EntityFeyBase fey = (EntityFeyBase) target;
			if (fey.getStatus() == FairyGeneralStatus.WANDERING) {
				if (!target.level.isClientSide) {
					target.spawnAtLocation(new ItemStack(FairyItems.feyTablet), .1f);
					((ServerLevel) target.level).sendParticles(ParticleTypes.HEART,
							target.getX(),
							target.getY(),	
							target.getZ(),
							10,
							.2,
							.25,
							.2,
							.1
							);
					NostrumMagicaSounds.AMBIENT_WOOSH2.play(target);
					stack.shrink(1);
				}
				
				return InteractionResult.SUCCESS;
			}
		}
		
		return InteractionResult.PASS;
	}
}
