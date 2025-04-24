package com.smanzana.nostrumfairies.network.messages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.FakeLogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server has processed a request for an update about all logistics networks and is
 * sending back the info.
 * @author Skyler
 *
 */
public class LogisticsUpdateResponse {

	public static void handle(LogisticsUpdateResponse message, Supplier<NetworkEvent.Context> ctx) {

		ctx.get().enqueueWork(() -> {
			
			NostrumFairies.logger.debug("Received logistics network refreshed data");
			
			// Clear out our list of networks, since this should be all networks
			NostrumFairies.instance.getLogisticsRegistry().clear();
			
			// Inject any network returned to us
			for (LogisticsNetwork network : message.networks) {
				NostrumFairies.instance.getLogisticsRegistry().injectNetwork(network);
			}
		});

		ctx.get().setPacketHandled(true);
	}

	private final @Nonnull List<LogisticsNetwork> networks;
	
	public LogisticsUpdateResponse(Collection<LogisticsNetwork> networks) {
		if (networks != null) {
			this.networks = new ArrayList<>(networks);
		} else {
			this.networks = new ArrayList<>();
		}
	}
	
	public static LogisticsUpdateResponse decode(FriendlyByteBuf buf) {
		final int count = buf.readVarInt();
		final List<LogisticsNetwork> networks = new ArrayList<>(count);
		
		for (int i = 0; i < count; i++) {
			LogisticsNetwork network = FakeLogisticsNetwork.fromNBT(buf.readNbt());
			networks.add(network);
		}
		
		return new LogisticsUpdateResponse(networks);
	}

	public static void encode(LogisticsUpdateResponse msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.networks.size());
		for (LogisticsNetwork network : msg.networks) {
			buf.writeNbt(new FakeLogisticsNetwork(network).toNBT());
		}
	}

}
