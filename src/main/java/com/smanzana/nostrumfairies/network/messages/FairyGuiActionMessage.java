package com.smanzana.nostrumfairies.network.messages;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has made a client-side action in the fairy gui
 * @author Skyler
 *
 */
public class FairyGuiActionMessage implements IMessage {
	
	public static enum GuiAction {
		CHANGE_TARGET,
		CHANGE_PLACEMENT,
	}

	public static class Handler implements IMessageHandler<FairyGuiActionMessage, IMessage> {

		@Override
		public CapabilitySyncMessage onMessage(FairyGuiActionMessage message, MessageContext ctx) {
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(() -> {
				INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(ctx.getServerHandler().playerEntity);
				FairyHolderInventory inv = (attr == null ? null : attr.getFairyInventory());
				
				if (inv != null && ctx.getServerHandler().playerEntity.openContainer instanceof FairyScreenGui.FairyScreenContainer) {
					FairyScreenGui.FairyScreenContainer container = (FairyScreenGui.FairyScreenContainer) ctx.getServerHandler().playerEntity.openContainer;
					container.handleAction(message.action, message.slot, message.selection);
				} else {
					NostrumFairies.logger.error("Got a Fairy screen gui action but no inventory present or gui isn't open");
				}
			});
			
			// This is dumb. Because of network thread, this interface has to return null and instead send
			// packet manually in scheduled task.
			return null;
		}
	}
	
	private GuiAction action;
	private int slot;
	private int selection;
	
	public FairyGuiActionMessage() {
		this(GuiAction.CHANGE_TARGET, 0, 0);
	}

	public FairyGuiActionMessage(GuiAction action, int slot, int selection) {
		this.action = action;
		this.slot = slot;
		this.selection = selection;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.action = GuiAction.values()[buf.readInt() % GuiAction.values().length];
		this.slot = buf.readInt();
		this.selection = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(action.ordinal());
		buf.writeInt(this.slot);
		buf.writeInt(this.selection);
	}

}
