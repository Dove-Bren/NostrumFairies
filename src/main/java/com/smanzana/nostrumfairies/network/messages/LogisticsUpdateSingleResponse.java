package com.smanzana.nostrumfairies.network.messages;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.FakeLogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server has processed a request for an update about one logistics network and is
 * sending back the info.
 * @author Skyler
 *
 */
public class LogisticsUpdateSingleResponse implements IMessage {

	public static class Handler implements IMessageHandler<LogisticsUpdateSingleResponse, IMessage> {

		@Override
		public IMessage onMessage(LogisticsUpdateSingleResponse message, MessageContext ctx) {

			Minecraft.getMinecraft().addScheduledTask(() -> {
				UUID id = message.tag.getUniqueId(NBT_UUID);
				
				// If data is present, inject and update.
				// Otherwise, remove existing if it's there.
				
				if (message.tag.hasKey(NBT_DATA)) {
					LogisticsNetwork network = FakeLogisticsNetwork.fromNBT(message.tag.getCompoundTag(NBT_DATA));
					NostrumFairies.instance.getLogisticsRegistry().injectNetwork(network);
				} else {
					// Remove it!
					LogisticsNetwork network = NostrumFairies.instance.getLogisticsRegistry().findNetwork(id);
					if (network != null) {
						NostrumFairies.instance.getLogisticsRegistry().removeNetwork(network);
					}
				}
			});

			return null;
		}
		
	}

	private static final String NBT_DATA = "data";
	private static final String NBT_UUID = "uuid";
	
	protected CompoundNBT tag;
	
	public LogisticsUpdateSingleResponse() {
		tag = new CompoundNBT();
	}
	
	public LogisticsUpdateSingleResponse(UUID id, @Nullable LogisticsNetwork network) {
		this();
		
		tag.setUniqueId(NBT_UUID, id);
		
		if (network != null) {
			tag.setTag(NBT_DATA, new FakeLogisticsNetwork(network).toNBT());
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
