package com.smanzana.nostrumfairies.network.messages;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.FakeLogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Server has processed a request for an update about one logistics network and is
 * sending back the info.
 * @author Skyler
 *
 */
public class LogisticsUpdateSingleResponse {

	public static void handle(LogisticsUpdateSingleResponse message, Supplier<NetworkEvent.Context> ctx) {

		ctx.get().enqueueWork(() -> {
			// If data is present, inject and update.
			// Otherwise, remove existing if it's there.
			if (message.network != null) {
				NostrumFairies.instance.getLogisticsRegistry().injectNetwork(message.network);
			} else {
				// Remove it!
				LogisticsNetwork network = NostrumFairies.instance.getLogisticsRegistry().findNetwork(message.id);
				if (network != null) {
					NostrumFairies.instance.getLogisticsRegistry().removeNetwork(network);
				}
			}
		});

		ctx.get().setPacketHandled(true);
	}
	
	private final UUID id;
	private final @Nullable LogisticsNetwork network;

	public LogisticsUpdateSingleResponse(UUID id, @Nullable LogisticsNetwork network) {
		this.id = id;
		this.network = network;
	}
	
	public static LogisticsUpdateSingleResponse decode(FriendlyByteBuf buf) {
		final UUID id = buf.readUUID();
		final LogisticsNetwork network = buf.readBoolean() ? FakeLogisticsNetwork.fromNBT(buf.readNbt()) : null;
		
		return new LogisticsUpdateSingleResponse(id, network);
	}

	public static void encode(LogisticsUpdateSingleResponse msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.id);
		
		if (msg.network != null) {
			buf.writeBoolean(true);
			buf.writeNbt(new FakeLogisticsNetwork(msg.network).toNBT());
		} else {
			buf.writeBoolean(false);
		}
	}

}
