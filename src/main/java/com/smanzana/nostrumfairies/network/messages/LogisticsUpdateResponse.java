package com.smanzana.nostrumfairies.network.messages;

import java.util.Collection;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.logistics.FakeLogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server has processed a request for an update about all logistics networks and is
 * sending back the info.
 * @author Skyler
 *
 */
public class LogisticsUpdateResponse implements IMessage {

	public static class Handler implements IMessageHandler<LogisticsUpdateResponse, IMessage> {

		@Override
		public IMessage onMessage(LogisticsUpdateResponse message, MessageContext ctx) {

			Minecraft.getMinecraft().addScheduledTask(() -> {
				
				NostrumFairies.logger.info("Received logistics network refreshed data");
				
				// Clear out our list of networks, since this should be all networks
				NostrumFairies.instance.getLogisticsRegistry().clear();
				
				// Inject any network returned to us
				NBTTagList list = message.tag.getTagList(NBT_LIST, NBT.TAG_COMPOUND);
				for (int i = list.tagCount() - 1; i >= 0; i--) {
					NBTTagCompound nbt = list.getCompoundTagAt(i);
					LogisticsNetwork network = FakeLogisticsNetwork.fromNBT(nbt);
					
					NostrumFairies.instance.getLogisticsRegistry().injectNetwork(network);
				}
			});

			return null;
		}
		
	}

	private static final String NBT_LIST = "list";
	
	protected NBTTagCompound tag;
	
	public LogisticsUpdateResponse() {
		tag = new NBTTagCompound();
	}
	
	public LogisticsUpdateResponse(Collection<LogisticsNetwork> networks) {
		this();
		
		NBTTagList list = new NBTTagList();
		for (LogisticsNetwork network : networks) {
			list.appendTag(new FakeLogisticsNetwork(network).toNBT());
		}
		
		tag.setTag(NBT_LIST, list);
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
