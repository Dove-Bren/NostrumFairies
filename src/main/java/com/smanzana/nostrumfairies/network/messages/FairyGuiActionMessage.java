package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client has made a client-side action in the fairy gui
 * @author Skyler
 *
 */
public class FairyGuiActionMessage {
	
	public static enum GuiAction {
		CHANGE_TARGET,
		CHANGE_PLACEMENT,
	}

	public static void handle(FairyGuiActionMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(ctx.get().getSender());
			FairyHolderInventory inv = (attr == null ? null : attr.getFairyInventory());
			
			if (inv != null && ctx.get().getSender().containerMenu instanceof FairyScreenGui.FairyScreenContainer) {
				FairyScreenGui.FairyScreenContainer container = (FairyScreenGui.FairyScreenContainer) ctx.get().getSender().containerMenu;
				container.handleAction(message.action, message.slot, message.selection);
			} else {
				NostrumFairies.logger.error("Got a Fairy screen gui action but no inventory present or gui isn't open");
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
	
	private final GuiAction action;
	private final int slot;
	private final int selection;
	
	public FairyGuiActionMessage(GuiAction action, int slot, int selection) {
		this.action = action;
		this.slot = slot;
		this.selection = selection;
	}
	
	public static FairyGuiActionMessage decode(FriendlyByteBuf buf) {
		GuiAction action = buf.readEnum(GuiAction.class);
		int slot = buf.readVarInt();
		int selection = buf.readVarInt();
		return new FairyGuiActionMessage(action, slot, selection);
	}

	public static void encode(FairyGuiActionMessage msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.action);
		buf.writeVarInt(msg.slot);
		buf.writeVarInt(msg.selection);
	}

}
