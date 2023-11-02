package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.container.AutoContainer;
import com.smanzana.nostrummagica.client.gui.container.AutoGuiContainer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LogicContainer extends AutoContainer {
	
	public LogicContainer(@Nullable IInventory inventory) {
		super(inventory);
	}

	public Slot addSlotToContainer(Slot slotIn) {
		return super.addSlotToContainer(slotIn);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static abstract class LogicGuiContainer extends AutoGuiContainer {

		public LogicGuiContainer(Container inventorySlotsIn) {
			super(inventorySlotsIn);
		}
	
		public <T extends GuiButton> T addButton(T button) {
			return super.addButton(button);
		}
	}
	
}
