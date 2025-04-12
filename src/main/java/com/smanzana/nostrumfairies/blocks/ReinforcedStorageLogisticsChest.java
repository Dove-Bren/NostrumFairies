package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.tiles.ReinforcedChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedDiamondChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedGoldChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedIronChestTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class ReinforcedStorageLogisticsChest extends FeyContainerBlock {
	
	public static enum Type {
		IRON,
		GOLD,
		DIAMOND;
	}
	
	private static final String ID_PREFIX = "logistics_reinforced_chest_";
	public static final String ID_IRON = ID_PREFIX + "iron";
	public static final String ID_GOLD = ID_PREFIX + "gold";
	public static final String ID_DIAMOND = ID_PREFIX + "diamond";
	
	private final Type type;
	
	public ReinforcedStorageLogisticsChest(Type type) {
		super(Block.Properties.of(Material.WOOD)
				.strength(3.0f, 1.0f)
				.sound(SoundType.WOOD)
				);
		this.type = type;
	}
	
	public static ReinforcedStorageLogisticsChest getBlock(Type type) {
		switch (type) {
		case IRON:
			return FairyBlocks.reinforcedIronChest;
		case GOLD:
			return FairyBlocks.reinforcedGoldChest;
		case DIAMOND:
			return FairyBlocks.reinforcedDiamondChest;
		}
		
		return null;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		switch (this.type) {
		case IRON:
		default:
			return new ReinforcedIronChestTileEntity(pos, state);
		case GOLD:
			return new ReinforcedGoldChestTileEntity(pos, state);
		case DIAMOND:
			return new ReinforcedDiamondChestTileEntity(pos, state);
		}
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof ReinforcedChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!@
		
		ReinforcedChestTileEntity table = (ReinforcedChestTileEntity) ent;
		for (int i = 0; i < table.getContainerSize(); i++) {
			if (!table.getItem(i).isEmpty()) {
				ItemEntity item = new ItemEntity(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeItemNoUpdate(i));
				world.addFreshEntity(item);
			}
		}
		
		table.unlinkFromNetwork();
	}
}
