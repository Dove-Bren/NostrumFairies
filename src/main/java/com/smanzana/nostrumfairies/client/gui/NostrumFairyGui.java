package com.smanzana.nostrumfairies.client.gui;

import com.smanzana.nostrumfairies.blocks.TestChest.TestChestTileEntity;
import com.smanzana.nostrumfairies.client.gui.container.TestChestGui;
import com.smanzana.nostrumfairies.client.gui.container.TestChestGui.TestChestGuiContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class NostrumFairyGui implements IGuiHandler {

	public static final int testChestID = 0;
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		
		if (ID == testChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof TestChestTileEntity) {
				return new TestChestGui.TestChestContainer(
						player.inventory,
						(TestChestTileEntity) ent); // should be tile inventory
			}
		}
		
		return null;
	}

	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		
		if (ID == testChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof TestChestTileEntity) {
				return new TestChestGuiContainer(new TestChestGui.TestChestContainer(
						player.inventory,
						(TestChestTileEntity) ent)); // should be tile inventory
			}
		}
		
		return null;
	}
	
}
