package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.network.NetworkHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client has requested an update about the their fey capability
 * @author Skyler
 *
 */
public class CapabilityRequest {

	public static void handle(CapabilityRequest message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			CapabilitySyncMessage response = new CapabilitySyncMessage(NostrumFairies.getFeyWrapper(ctx.get().getSender()));
			NetworkHandler.sendTo(response, ctx.get().getSender());
		});
		
		ctx.get().setPacketHandled(true);
	}

	public CapabilityRequest() {
		;
	}
	
	public static CapabilityRequest decode(FriendlyByteBuf buf) {
		return new CapabilityRequest();
	}

	public static void encode(CapabilityRequest msg, FriendlyByteBuf buf) {
		;
	}

}
