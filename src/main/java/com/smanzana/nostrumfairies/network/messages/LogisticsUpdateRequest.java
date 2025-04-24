package com.smanzana.nostrumfairies.network.messages;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.network.NetworkHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has requested an update about the logistics network.
 * This may be info about a single network, or a full copy of all networks
 * @author Skyler
 *
 */
public class LogisticsUpdateRequest {

	public static void handle(LogisticsUpdateRequest message, Supplier<NetworkEvent.Context> ctx) {
		
		// We actually ignore this message if we're running integrated, as the client will already have
		// correct information in the singleton registry
		if (!ctx.get().getSender().getServer().isDedicatedServer()) {
			return;
		}
		
		ctx.get().enqueueWork(() -> {
			Object response;
			
			// Is this about a single network, or all of them?
			if (message.id != null) {
				// single network! look it up!
				response = new LogisticsUpdateSingleResponse(message.id, NostrumFairies.instance.getLogisticsRegistry().findNetwork(
						message.id));
			} else {
				// All networks!
				response = new LogisticsUpdateResponse(NostrumFairies.instance.getLogisticsRegistry().getNetworks());
			}
			
			NetworkHandler.sendTo(response, ctx.get().getSender());
		});
		
		ctx.get().setPacketHandled(true);
	}

	private final @Nullable UUID id;
	
	public LogisticsUpdateRequest(@Nullable UUID id) {
		this.id = id;
	}
	
	public static LogisticsUpdateRequest decode(FriendlyByteBuf buf) {
		return new LogisticsUpdateRequest(
				buf.readBoolean() ? buf.readUUID() : null
				);
	}

	public static void encode(LogisticsUpdateRequest msg, FriendlyByteBuf buf) {
		if (msg.id != null) {
			buf.writeBoolean(true);
			buf.writeUUID(msg.id);
		} else {
			buf.writeBoolean(false);
		}
	}

}
