package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.container.AutoContainer;
import com.smanzana.nostrummagica.client.gui.container.AutoGuiContainer;
import com.smanzana.nostrummagica.util.ContainerUtil.IAutoContainerInventory;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class LogicContainer extends AutoContainer {
	
	public LogicContainer(MenuType<? extends LogicContainer> type, int windowId, @Nullable IAutoContainerInventory inventory) {
		super(type, windowId, inventory);
	}

	@Override
	public Slot addSlot(Slot slotIn) {
		return super.addSlot(slotIn);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static abstract class LogicGuiContainer<T extends LogicContainer> extends AutoGuiContainer<T> {

		public LogicGuiContainer(T inventorySlotsIn, Inventory playerInv, Component name) {
			super(inventorySlotsIn, playerInv, name);
		}
	
		@Override
		public <W extends AbstractWidget> W addButton(W button) {
			return super.addButton(button);
		}
	}
	
}
