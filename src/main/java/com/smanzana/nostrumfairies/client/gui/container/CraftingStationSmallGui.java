package com.smanzana.nostrumfairies.client.gui.container;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.CraftingBlockTileEntity;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationGui.CraftingStationContainer;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationGui.CraftingStationGuiContainer;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 3x3 regular sized crafting station
 * @author Skyler
 *
 */
public class CraftingStationSmallGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumFairies.MODID + ":textures/gui/container/craft_small_block.png");
	private static final int GUI_TOP_INV_HOFFSET = 49;
	private static final int GUI_TOP_INV_VOFFSET = 28;

	public static class CraftingStationSmallContainer extends CraftingStationContainer {
		
		public CraftingStationSmallContainer(IInventory playerInv, CraftingBlockTileEntity station) {
			super(playerInv, station);
		}
		
		protected int getCraftGridStartX() {
			return GUI_TOP_INV_HOFFSET;
		}
		
		protected int getCraftGridStartY() {
			return GUI_TOP_INV_VOFFSET;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class CraftingStationSmallGuiContainer extends CraftingStationGuiContainer {

		public CraftingStationSmallGuiContainer(CraftingStationContainer container) {
			super(container);
		}
		
		protected ResourceLocation getBackgroundTexture() {
			return TEXT;
		}
		
	}
	
}
