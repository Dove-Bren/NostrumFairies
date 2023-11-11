package com.smanzana.nostrumfairies.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.container.AutoContainer;
import com.smanzana.nostrummagica.client.gui.container.AutoGuiContainer;
import com.smanzana.nostrummagica.utils.ContainerUtil.IAutoContainerInventory;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class LogicContainer extends AutoContainer {
	
	public LogicContainer(ContainerType<? extends LogicContainer> type, int windowId, @Nullable IAutoContainerInventory inventory) {
		super(type, windowId, inventory);
	}

	@Override
	public Slot addSlot(Slot slotIn) {
		return super.addSlot(slotIn);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static abstract class LogicGuiContainer<T extends LogicContainer> extends AutoGuiContainer<T> {

		public LogicGuiContainer(T inventorySlotsIn, PlayerInventory playerInv, ITextComponent name) {
			super(inventorySlotsIn, playerInv, name);
		}
	
		@Override
		public <W extends Widget> W addButton(W button) {
			return super.addButton(button);
		}
	}
	
}
