package com.smanzana.nostrumfairies.client.gui;

import com.smanzana.nostrumfairies.blocks.BufferLogisticsChest.BufferChestTileEntity;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsChest.OutputChestTileEntity;
import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest.StorageChestTileEntity;
import com.smanzana.nostrumfairies.blocks.StorageMonitor.StorageMonitorTileEntity;
import com.smanzana.nostrumfairies.client.gui.container.BufferChestGui;
import com.smanzana.nostrumfairies.client.gui.container.BufferChestGui.BufferChestGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.OutputChestGui;
import com.smanzana.nostrumfairies.client.gui.container.OutputChestGui.OutputChestGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.StorageChestGui;
import com.smanzana.nostrumfairies.client.gui.container.StorageChestGui.StorageChestGuiContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class NostrumFairyGui implements IGuiHandler {

	public static final int storageMonitorID = 0;
	public static final int storageChestID = 1;
	public static final int bufferChestID = 2;
	public static final int outputChestID = 3;
	
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
		
		if (ID == bufferChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof BufferChestTileEntity) {
				return new BufferChestGui.BufferChestContainer(
						player.inventory,
						(BufferChestTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == outputChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof OutputChestTileEntity) {
				return new OutputChestGui.OutputChestContainer(
						player.inventory,
						(OutputChestTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == storageMonitorID) {
			; // nothing on server
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
		
		if (ID == bufferChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof BufferChestTileEntity) {
				return new BufferChestGuiContainer(new BufferChestGui.BufferChestContainer(
						player.inventory,
						(BufferChestTileEntity) ent)); // should be tile inventory
			}
		}
		
		if (ID == outputChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof OutputChestTileEntity) {
				return new OutputChestGuiContainer(new OutputChestGui.OutputChestContainer(
						player.inventory,
						(OutputChestTileEntity) ent)); // should be tile inventory
			}
		}
		
		if (ID == storageMonitorID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof StorageMonitorTileEntity) {
				return new StorageMonitorScreen(((StorageMonitorTileEntity) ent).getNetwork());
				// oh, here's the problem. The client network is unlinked, because we don't actually send
				// fake networks when we're integrated. But that means that the client copy of things
				// like tile entities are wrong.
			}
		}
		
		return null;
	}
	
}
