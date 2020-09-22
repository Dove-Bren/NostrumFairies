package com.smanzana.nostrumfairies.network.messages;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.INostrumFeyCapability;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is sending the most recent version of the fey capability to a client-side player
 * @author Skyler
 *
 */
public class CapabilitySyncMessage implements IMessage {

	public static class Handler implements IMessageHandler<CapabilitySyncMessage, IMessage> {

		@Override
		public IMessage onMessage(CapabilitySyncMessage message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(NostrumFairies.proxy.getPlayer());
				attr.readNBT(message.tag.getCompoundTag(NBT_ATTR));
			});
			
			return null;
		}
	}

	private static final String NBT_ATTR = "attr";
	
	protected NBTTagCompound tag;
	
	public CapabilitySyncMessage() {
		this(null);
	}
	
	public CapabilitySyncMessage(@Nullable INostrumFeyCapability attr) {
		tag = new NBTTagCompound();
		
		if (attr != null) {
			tag.setTag(NBT_ATTR, attr.toNBT());
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
