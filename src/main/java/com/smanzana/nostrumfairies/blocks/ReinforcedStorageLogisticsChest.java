package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.tiles.ReinforcedChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedDiamondChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedGoldChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedIronChestTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

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
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(3.0f, 1.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
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
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		switch (this.type) {
		case IRON:
		default:
			return new ReinforcedIronChestTileEntity();
		case GOLD:
			return new ReinforcedGoldChestTileEntity();
		case DIAMOND:
			return new ReinforcedDiamondChestTileEntity();
		}
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof ReinforcedChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!@
		
		ReinforcedChestTileEntity table = (ReinforcedChestTileEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (!table.getStackInSlot(i).isEmpty()) {
				ItemEntity item = new ItemEntity(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.addEntity(item);
			}
		}
		
		table.unlinkFromNetwork();
	}
}
