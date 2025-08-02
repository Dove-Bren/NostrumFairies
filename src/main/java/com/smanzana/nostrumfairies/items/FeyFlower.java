package com.smanzana.nostrumfairies.items;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.entity.FairyEntities;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityElfArcher;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrummagica.item.api.LoreItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class FeyFlower extends LoreItem {

	public static final String ID = "fey_flower";
	
	public FeyFlower(Item.Properties props) {
		super(props);
	}
	
	protected void spawnFey(ServerLevel worldIn, BlockPos at) {
		final EntityFeyBase fey;
		switch (NostrumFairies.random.nextInt(5)) {
		case 0:
			fey = new EntityFairy(FairyEntities.Fairy, worldIn);
			break;
		case 1:
			fey = new EntityDwarf(FairyEntities.Dwarf, worldIn);
			break;
		case 2:
			fey = new EntityGnome(FairyEntities.Gnome, worldIn);
			break;
		case 3:
			fey = new EntityElf(FairyEntities.Elf, worldIn);
			break;
		case 4:
		default:
			fey = new EntityElfArcher(FairyEntities.ElfArcher, worldIn);
			break;
		}
		fey.setPos(at.getX() + .5, at.getY(), at.getZ() + .5);
		fey.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(fey.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, null);
		
		worldIn.addFreshEntity(fey);
		
		((ServerLevel) worldIn).sendParticles(ParticleTypes.END_ROD,
				at.getX() + .5,
				at.getY() + .25,
				at.getZ() + .5,
				100,
				.25,
				.4,
				.25,
				.05);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Player playerIn = context.getPlayer();
		final InteractionHand hand = context.getHand();
		final Level worldIn = context.getLevel();
		final ItemStack stack = playerIn.getItemInHand(hand);
		final BlockPos pos = context.getClickedPos();
		
		if (!worldIn.isClientSide) {
			spawnFey((ServerLevel) worldIn, pos.above());
			int count = NostrumFairies.random.nextInt(3) + 1;
			BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
			for (int i = 0; i < count; i++) {
				cursor.set(pos.above()).move(Direction.Plane.HORIZONTAL.getRandomDirection(NostrumFairies.random), NostrumFairies.random.nextInt(2) + 1);
				for (int j = 0; j < 5; j++) {
					if (worldIn.isEmptyBlock(cursor)) {
						if (FairyBlocks.feyBush.canSurvive(FairyBlocks.feyBush.defaultBlockState(), worldIn, cursor)) {
							// Found a spot!
							worldIn.setBlockAndUpdate(cursor.immutable(), FairyBlocks.feyBush.defaultBlockState());
							((ServerLevel) worldIn).sendParticles(ParticleTypes.HAPPY_VILLAGER,
									cursor.getX() + .5,
									cursor.getY() + .25,
									cursor.getZ() + .5,
									20,
									.25,
									1,
									.25,
									0);
							break;
						} else {
							cursor.move(Direction.DOWN);
						}
					} else {
						cursor.move(Direction.UP);
					}
				}
				
			}
		}
		
		stack.shrink(1);
		return InteractionResult.SUCCESS;
	}
}
