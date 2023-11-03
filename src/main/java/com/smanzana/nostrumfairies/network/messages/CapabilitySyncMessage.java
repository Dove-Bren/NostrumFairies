package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is sending the most recent version of the fey capability to a client-side player
 * @author Skyler
 *
 */
public class CapabilitySyncMessage {

	public static void handle(CapabilitySyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(NostrumFairies.proxy.getPlayer());
			attr.readNBT(message.capData);
		});
		
		ctx.get().setPacketHandled(true);
	}

	private final @Nonnull CompoundNBT capData;
	
	public CapabilitySyncMessage(@Nonnull CompoundNBT attrData) {
		this.capData = attrData;
	}
	
	public CapabilitySyncMessage(@Nullable INostrumFeyCapability attr) {
		this(attr == null ? new CompoundNBT() : attr.toNBT());
	}
	
	public static CapabilitySyncMessage decode(PacketBuffer buf) {
		@Nonnull CompoundNBT data = buf.readCompoundTag(); 
		return new CapabilitySyncMessage(data);
	}
	
	public static void encode(CapabilitySyncMessage msg, PacketBuffer buf) {
		buf.writeCompoundTag(msg.capData);
	}

}
