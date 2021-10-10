package com.smanzana.nostrumfairies.network.messages;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.tiles.StorageMonitorTileEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has added a requested item to a storage monitor
 * @author Skyler
 *
 */
public class StorageMonitorRequestMessage implements IMessage {

	public static class Handler implements IMessageHandler<StorageMonitorRequestMessage, IMessage> {

		@Override
		public IMessage onMessage(StorageMonitorRequestMessage message, MessageContext ctx) {
			
			try {
				final BlockPos pos = BlockPos.fromLong(message.tag.getLong(NBT_POS));
				final World world = ctx.getServerHandler().player.world;
				final @Nonnull ItemStack request = new ItemStack(message.tag.getCompoundTag(NBT_REQ));
				final boolean delete = message.tag.getBoolean(NBT_DEL);
				
				ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
					final TileEntity te = world.getTileEntity(pos);
					if (te != null && te instanceof StorageMonitorTileEntity) {
						StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) te;
						if (!delete) {
							monitor.addRequest(request);
						} else {
							monitor.removeRequest(request);
						}
					}
					
					// Cause an update to be sent back
					IBlockState state = world.getBlockState(pos);
					world.notifyBlockUpdate(pos, state, state, 2);
				});
				
				
				// This is dumb. Because of network thread, this interface has to return null and instead send
				// packet manually in scheduled task.
				return null;
			} catch (Exception e) {
				NostrumFairies.logger.error(e);
			}
			
			return null;
		}
	}
	
	private static final String NBT_POS = "pos";
	private static final String NBT_REQ = "request";
	private static final String NBT_DEL = "delete";
	
	protected NBTTagCompound tag;
	
	public StorageMonitorRequestMessage() {
		this(null, null, false);
	}
	
	public StorageMonitorRequestMessage(StorageMonitorTileEntity monitor, @Nonnull ItemStack template, boolean delete) {
		tag = new NBTTagCompound();
		
		if (!template.isEmpty()) {
			tag.setLong(NBT_POS, monitor.getPos().toLong());
			tag.setTag(NBT_REQ, template.serializeNBT());
			tag.setBoolean(NBT_DEL, delete);
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
