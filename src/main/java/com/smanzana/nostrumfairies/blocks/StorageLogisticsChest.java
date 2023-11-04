package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.tiles.StorageChestTileEntity;

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

public class StorageLogisticsChest extends FeyContainerBlock {
	
	public static final String ID = "logistics_storage_chest";
	
	public StorageLogisticsChest() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(3.0f, 1.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.storageChestID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new StorageChestTileEntity();
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
		if (ent == null || !(ent instanceof StorageChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!@
		
		StorageChestTileEntity table = (StorageChestTileEntity) ent;
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
