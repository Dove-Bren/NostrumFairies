package com.smanzana.nostrumfairies.client.gui.container;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.FairyContainers;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationGui.CraftingStationContainer;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationGui.CraftingStationGuiContainer;
import com.smanzana.nostrumfairies.tiles.CraftingBlockTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		
		public static final String ID = "crafting_station_small";
		
		public CraftingStationSmallContainer(int windowId, Inventory playerInv, CraftingBlockTileEntity station) {
			super(FairyContainers.CraftingStationSmall, windowId, playerInv, station);
		}
		
		public static CraftingStationSmallContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
			return new CraftingStationSmallContainer(windowId, playerInv, ContainerUtil.GetPackedTE(buf));
		}
		
		public static IPackedContainerProvider Make(CraftingBlockTileEntity hopper) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new CraftingStationSmallContainer(windowId, playerInv, hopper);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, hopper);
			});
		}
		
		protected int getCraftGridStartX() {
			return GUI_TOP_INV_HOFFSET;
		}
		
		protected int getCraftGridStartY() {
			return GUI_TOP_INV_VOFFSET;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class CraftingStationSmallGuiContainer extends CraftingStationGuiContainer {

		public CraftingStationSmallGuiContainer(CraftingStationContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
		}
		
		protected ResourceLocation getBackgroundTexture() {
			return TEXT;
		}
		
	}
	
}
