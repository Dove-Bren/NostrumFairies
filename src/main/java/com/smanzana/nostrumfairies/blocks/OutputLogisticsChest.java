package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.container.OutputChestGui;
import com.smanzana.nostrumfairies.tiles.OutputChestTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class OutputLogisticsChest extends FeyContainerBlock {
	
	public static final String ID = "logistics_output_chest";
	
	public OutputLogisticsChest() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(3.0f, 1.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		OutputChestTileEntity chest = (OutputChestTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, OutputChestGui.OutputChestContainer.Make(chest));
		
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new OutputChestTileEntity();
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
		if (ent == null || !(ent instanceof OutputChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!
		
		OutputChestTileEntity table = (OutputChestTileEntity) ent;
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
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		TileEntity ent = worldIn.getTileEntity(pos);
		if (ent != null && ent instanceof OutputChestTileEntity) {
			((OutputChestTileEntity) ent).notifyNeighborChanged();
		}
		
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}
}
