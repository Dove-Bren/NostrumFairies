package com.smanzana.nostrumfairies.client.gui;

import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest.StorageChestTileEntity;
import com.smanzana.nostrumfairies.client.gui.container.StorageChestGui;
import com.smanzana.nostrumfairies.client.gui.container.StorageChestGui.StorageChestGuiContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class NostrumFairyGui implements IGuiHandler {

	public static final int storageChestID = 0;
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		
		if (ID == storageChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof StorageChestTileEntity) {
				return new StorageChestGui.StorageChestContainer(
						player.inventory,
						(StorageChestTileEntity) ent); // should be tile inventory
			}
		}
		
		return null;
	}

	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		
		if (ID == storageChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof StorageChestTileEntity) {
				return new StorageChestGuiContainer(new StorageChestGui.StorageChestContainer(
						player.inventory,
						(StorageChestTileEntity) ent)); // should be tile inventory
			}
		}
		
		return null;
	}
	
}
