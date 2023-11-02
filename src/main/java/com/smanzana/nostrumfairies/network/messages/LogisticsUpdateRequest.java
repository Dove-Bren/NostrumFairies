package com.smanzana.nostrumfairies.network.messages;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.network.NetworkHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has requested an update about the logistics network.
 * This may be info about a single network, or a full copy of all networks
 * @author Skyler
 *
 */
public class LogisticsUpdateRequest implements IMessage {

	public static class Handler implements IMessageHandler<LogisticsUpdateRequest, IMessage> {

		@Override
		public IMessage onMessage(LogisticsUpdateRequest message, MessageContext ctx) {
			
			// We actually ignore this message if we're running integrated, as the client will already have
			// correct information in the singleton registry
			if (!ctx.getServerHandler().player.getServer().isDedicatedServer()) {
				return null;
			}
			
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
				IMessage response;
				
				// Is this about a single network, or all of them?
				if (message.tag.hasUniqueId(NBT_NETWORK_ID)) {
					// single network! look it up!
					UUID uuid = message.tag.getUniqueId(NBT_NETWORK_ID);
					response = new LogisticsUpdateSingleResponse(uuid, NostrumFairies.instance.getLogisticsRegistry().findNetwork(
							uuid));
				} else {
					// All networks!
					response = new LogisticsUpdateResponse(NostrumFairies.instance.getLogisticsRegistry().getNetworks());
				}
				
				NetworkHandler.getSyncChannel().sendTo(response,
						ctx.getServerHandler().player);
			});
			
			// This is dumb. Because of network thread, this interface has to return null and instead send
			// packet manually in scheduled task.
			return null;
		}
	}

	private static final String NBT_NETWORK_ID = "id";
	
	protected CompoundNBT tag;
	
	public LogisticsUpdateRequest() {
		this(null);
	}
	
	public LogisticsUpdateRequest(@Nullable UUID id) {
		tag = new CompoundNBT();
		
		if (id != null) {
			tag.setUniqueId(NBT_NETWORK_ID, id);
		}
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}

}
