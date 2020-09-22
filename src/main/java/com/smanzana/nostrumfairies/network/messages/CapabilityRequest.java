package com.smanzana.nostrumfairies.network.messages;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.network.NetworkHandler;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has requested an update about the their fey capability
 * @author Skyler
 *
 */
public class CapabilityRequest implements IMessage {

	public static class Handler implements IMessageHandler<CapabilityRequest, CapabilitySyncMessage> {

		@Override
		public CapabilitySyncMessage onMessage(CapabilityRequest message, MessageContext ctx) {
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(() -> {
				CapabilitySyncMessage response = new CapabilitySyncMessage(NostrumFairies.getFeyWrapper(ctx.getServerHandler().playerEntity));
				NetworkHandler.getSyncChannel().sendTo(response,
						ctx.getServerHandler().playerEntity);
			});
			
			// This is dumb. Because of network thread, this interface has to return null and instead send
			// packet manually in scheduled task.
			return null;
		}
	}

	public CapabilityRequest() {
		;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		;
	}

}
