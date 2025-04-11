package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client has added a requested item to a storage monitor
 * @author Skyler
 *
 */
public class StorageMonitorRequestMessage {

	public static void handle(StorageMonitorRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			try {
				final Level world = ctx.get().getSender().level;
				final BlockEntity te = world.getBlockEntity(message.pos);
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
				world.sendBlockUpdated(message.pos, state, state, 2);
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
		this(monitor.getBlockPos(), template, delete);
	}
	
	protected StorageMonitorRequestMessage(BlockPos pos, @Nonnull ItemStack template, boolean delete) {
		this.pos = pos;
		this.template = template;
		this.delete = delete;
	}
	
	public static StorageMonitorRequestMessage decode(FriendlyByteBuf buf) {
		return new StorageMonitorRequestMessage(
				buf.readBlockPos(),
				(buf.readBoolean() ? buf.readItem() : null),
				buf.readBoolean()
				);
	}

	public static void encode(StorageMonitorRequestMessage msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
		if (msg.template != null) {
			buf.writeBoolean(true);
			buf.writeItem(msg.template);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeBoolean(msg.delete);
	}

}
