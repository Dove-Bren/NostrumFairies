package com.smanzana.nostrumfairies.client.gui;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.tiles.BufferChestTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.BuildingBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.CraftingBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.HomeBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.InputChestTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.LogisticsSensorTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.OutputChestTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.OutputPanelTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.StorageChestTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.StorageMonitorTileEntity;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.client.gui.container.BufferChestGui;
import com.smanzana.nostrumfairies.client.gui.container.BufferChestGui.BufferChestGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.BuildingBlockGui;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationGui;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationSmallGui;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui;
import com.smanzana.nostrumfairies.client.gui.container.HomeBlockGui;
import com.smanzana.nostrumfairies.client.gui.container.InputChestGui;
import com.smanzana.nostrumfairies.client.gui.container.InputChestGui.InputChestGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.LogisticsSensorGui;
import com.smanzana.nostrumfairies.client.gui.container.OutputChestGui;
import com.smanzana.nostrumfairies.client.gui.container.OutputChestGui.OutputChestGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.OutputPanelGui;
import com.smanzana.nostrumfairies.client.gui.container.StorageChestGui;
import com.smanzana.nostrumfairies.client.gui.container.StorageChestGui.StorageChestGuiContainer;
import com.smanzana.nostrumfairies.client.gui.container.TemplateWandGui;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class NostrumFairyGui implements IGuiHandler {

	public static final int storageMonitorID = 0;
	public static final int storageChestID = 1;
	public static final int bufferChestID = 2;
	public static final int outputChestID = 3;
	public static final int inputChestID = 4;
	public static final int homeBlockID = 5;
	public static final int fairyGuiID = 6;
	public static final int templateWandGuiID = 7;
	public static final int buildBlockID = 8;
	public static final int craftDwarfID = 9;
	public static final int craftElfID = 10;
	public static final int craftGnomeID = 11;
	
	public static final int logisticsSensorID = 12;
	public static final int outputPanelID = 13;
	
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
		
		if (ID == inputChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof InputChestTileEntity) {
				return new InputChestGui.InputChestContainer(
						player.inventory,
						(InputChestTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == buildBlockID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof BuildingBlockTileEntity) {
				return new BuildingBlockGui.BuildingBlockContainer(
						player.inventory,
						(BuildingBlockTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == storageMonitorID) {
			; // nothing on server
		}
		
		if (ID == homeBlockID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof HomeBlockTileEntity) {
				return new HomeBlockGui.HomeBlockContainer(
						player.inventory,
						(HomeBlockTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == fairyGuiID) {
			INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
			if (attr != null && attr.isUnlocked()) {
				return new FairyScreenGui.FairyScreenContainer(player.inventory, attr.getFairyInventory(), attr);
			}
		}
		
		if (ID == templateWandGuiID) {
			// Find the wand
			ItemStack wand = player.getHeldItemMainhand();
			int pos = player.inventory.currentItem + 27;
			if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || TemplateWand.getModeOf(wand) != WandMode.SPAWN) {
				wand = player.getHeldItemOffhand();
				pos = 40;
			}
			if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || TemplateWand.getModeOf(wand) != WandMode.SPAWN) {
				return null; // Not actually in their hand
			}
			
			return new TemplateWandGui.BagContainer(player.inventory, TemplateWand.GetTemplateInventory(wand), pos);
		}
		
		if (ID == craftDwarfID || ID == craftElfID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof CraftingBlockTileEntity) {
				return new CraftingStationGui.CraftingStationContainer(
						player.inventory,
						(CraftingBlockTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == craftGnomeID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof CraftingBlockTileEntity) {
				return new CraftingStationSmallGui.CraftingStationSmallContainer(
						player.inventory,
						(CraftingBlockTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == logisticsSensorID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof LogisticsSensorTileEntity) {
				return new LogisticsSensorGui.LogisticsSensorContainer(
						player.inventory,
						(LogisticsSensorTileEntity) ent); // should be tile inventory
			}
		}
		
		if (ID == outputPanelID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof OutputPanelTileEntity) {
				return new OutputPanelGui.OutputPanelContainer(
						player.inventory,
						(OutputPanelTileEntity) ent); // should be tile inventory
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
		
		if (ID == inputChestID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof InputChestTileEntity) {
				return new InputChestGuiContainer(new InputChestGui.InputChestContainer(
						player.inventory,
						(InputChestTileEntity) ent)); // should be tile inventory
			}
		}
		
		if (ID == buildBlockID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof BuildingBlockTileEntity) {
				return new BuildingBlockGui.BuildingBlockGuiContainer(new BuildingBlockGui.BuildingBlockContainer(
						player.inventory,
						(BuildingBlockTileEntity) ent)); // should be tile inventory
			}
		}
		
		if (ID == storageMonitorID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof StorageMonitorTileEntity) {
				return new StorageMonitorScreen((StorageMonitorTileEntity) ent);
				// oh, here's the problem. The client network is unlinked, because we don't actually send
				// fake networks when we're integrated. But that means that the client copy of things
				// like tile entities are wrong.
			}
		}
		
		if (ID == homeBlockID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof HomeBlockTileEntity) {
				return new HomeBlockGui.HomeBlockGuiContainer(new HomeBlockGui.HomeBlockContainer(
						player.inventory,
						(HomeBlockTileEntity) ent)); // should be tile inventory
			}
		}
		
		if (ID == fairyGuiID) {
			INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
			if (attr != null && attr.isUnlocked()) {
				return new FairyScreenGui.FairyScreenGuiContainer(new FairyScreenGui.FairyScreenContainer(player.inventory, attr.getFairyInventory(), attr));
			}
		}
		
		if (ID == templateWandGuiID) {
			// Find the wand
			ItemStack wand = player.getHeldItemMainhand();
			int pos = player.inventory.currentItem + 27;
			if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || TemplateWand.getModeOf(wand) != WandMode.SPAWN) {
				wand = player.getHeldItemOffhand();
				pos = 40;
			}
			if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || TemplateWand.getModeOf(wand) != WandMode.SPAWN) {
				return null; // Not actually in their hand
			}
			
			return new TemplateWandGui.BagGui(new TemplateWandGui.BagContainer(player.inventory, TemplateWand.GetTemplateInventory(wand), pos));
		}
		
		if (ID == craftDwarfID || ID == craftElfID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof CraftingBlockTileEntity) {
				return new CraftingStationGui.CraftingStationGuiContainer(new CraftingStationGui.CraftingStationContainer(
						player.inventory,
						(CraftingBlockTileEntity) ent));
			}
		}
		
		if (ID == craftGnomeID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof CraftingBlockTileEntity) {
				return new CraftingStationSmallGui.CraftingStationSmallGuiContainer(new CraftingStationSmallGui.CraftingStationSmallContainer(
						player.inventory,
						(CraftingBlockTileEntity) ent));
			}
		}
		
		if (ID == logisticsSensorID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof LogisticsSensorTileEntity) {
				return new LogisticsSensorGui.LogisticsSensorGuiContainer(new LogisticsSensorGui.LogisticsSensorContainer(
						player.inventory,
						(LogisticsSensorTileEntity) ent)); // should be tile inventory
			}
		}
		
		if (ID == outputPanelID) {
			TileEntity ent = world.getTileEntity(new BlockPos(x, y, z));
			if (ent != null && ent instanceof OutputPanelTileEntity) {
				return new OutputPanelGui.OutputPanelGuiContainer(new OutputPanelGui.OutputPanelContainer(
						player.inventory,
						(OutputPanelTileEntity) ent)); // should be tile inventory
			}
		}
		
		return null;
	}
	
}
