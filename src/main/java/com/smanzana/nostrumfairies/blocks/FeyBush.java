package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.items.FeyResource;
import com.smanzana.nostrumfairies.items.FeyResource.FeyResourceType;
import com.smanzana.nostrumfairies.serializers.FairyGeneralStatus;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;

/**
 * @author Skyler
 *
 */
public class FeyBush extends BushBlock {
	
	public static final String ID = "fey_bush";
	
	public FeyBush() {
		super(Block.Properties.create(Material.PLANTS)
				.sound(SoundType.PLANT)
				.doesNotBlockMovement()
				);
	}
	
	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext context) {
        return false;
    }
	
	@Override
	public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction facing, IPlantable plantable) {
		boolean ret = super.canSustainPlant(state, world, pos, facing, plantable);
		
		return ret;
	}

//	@Override
//	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
//		return Lists.newArrayList(new ItemStack(this));
//	}

	public ActionResultType getEntityInteraction(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		if (target instanceof EntityFeyBase && !(target instanceof EntityPersonalFairy)) {
			EntityFeyBase fey = (EntityFeyBase) target;
			if (fey.getStatus() == FairyGeneralStatus.WANDERING) {
				if (!target.world.isRemote) {
					target.entityDropItem(FeyResource.create(FeyResourceType.TABLET, 1), .1f);
					((ServerWorld) target.world).spawnParticle(ParticleTypes.HEART,
							target.getPosX(),
							target.getPosY(),	
							target.getPosZ(),
							10,
							.2,
							.25,
							.2,
							.1
							);
					NostrumMagicaSounds.AMBIENT_WOOSH2.play(target);
					stack.shrink(1);
				}
				
				return ActionResultType.SUCCESS;
			}
		}
		
		return ActionResultType.PASS;
	}
}
