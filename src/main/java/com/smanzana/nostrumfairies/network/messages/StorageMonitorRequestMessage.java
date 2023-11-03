package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has added a requested item to a storage monitor
 * @author Skyler
 *
 */
public class StorageMonitorRequestMessage {

	public static void handle(StorageMonitorRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			try {
				final World world = ctx.get().getSender().world;
				final TileEntity te = world.getTileEntity(message.pos);
				if (te != null && te instanceof StorageMonitorTileEntity) {
					StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) te;
					if (!message.delete) {
						monitor.addRequest(message.template);
					} else {
						monitor.removeRequest(message.template);
					}
				}
				
				// Cause an update to be sent back
				BlockState state = world.getBlockState(message.pos);
				world.notifyBlockUpdate(message.pos, state, state, 2);
			} catch (Exception e) {
				NostrumFairies.logger.error(e);
			}
		});
		
		ctx.get().setPacketHandled(true);
		
	}
	
	private final BlockPos pos;
	private final @Nonnull ItemStack template;
	private final boolean delete;
	
	public StorageMonitorRequestMessage(StorageMonitorTileEntity monitor, @Nonnull ItemStack template, boolean delete) {
		this(monitor.getPos(), template, delete);
	}
	
	protected StorageMonitorRequestMessage(BlockPos pos, @Nonnull ItemStack template, boolean delete) {
		this.pos = pos;
		this.template = template;
		this.delete = delete;
	}
	
	public static StorageMonitorRequestMessage decode(PacketBuffer buf) {
		return new StorageMonitorRequestMessage(
				buf.readBlockPos(),
				(buf.readBoolean() ? buf.readItemStack() : null),
				buf.readBoolean()
				);
	}

	public static void encode(StorageMonitorRequestMessage msg, PacketBuffer buf) {
		buf.writeBlockPos(msg.pos);
		if (msg.template != null) {
			buf.writeBoolean(true);
			buf.writeItemStack(msg.template);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeBoolean(msg.delete);
	}

}
