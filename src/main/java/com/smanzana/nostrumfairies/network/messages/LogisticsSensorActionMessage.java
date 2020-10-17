package com.smanzana.nostrumfairies.network.messages;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.LogisticsSensorBlock.LogisticsSensorTileEntity;
import com.smanzana.nostrumfairies.blocks.LogisticsSensorBlock.LogisticsSensorTileEntity.SensorLogicOp;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has made an action in a crafting station
 * @author Skyler
 *
 */
public class LogisticsSensorActionMessage implements IMessage {

	public static class Handler implements IMessageHandler<LogisticsSensorActionMessage, IMessage> {

		@Override
		public IMessage onMessage(LogisticsSensorActionMessage message, MessageContext ctx) {
			
			try {
				final Action type = Action.valueOf(message.tag.getString(NBT_TYPE));
				final BlockPos pos = BlockPos.fromLong(message.tag.getLong(NBT_POS));
				final World world = ctx.getServerHandler().playerEntity.worldObj;
				
				LogisticsSensorTileEntity te = (LogisticsSensorTileEntity) world.getTileEntity(pos);
				
				ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(() -> {
					if (type == Action.TEMPLATE) {
						@Nullable ItemStack stack = ItemStack.loadItemStackFromNBT(message.tag.getCompoundTag(NBT_ITEM_VAL));
						te.setLogicTemplate(stack);
					} else if (type == Action.OP) {
						SensorLogicOp op = SensorLogicOp.valueOf(message.tag.getString(NBT_INT_VAL));
						te.setLogicOp(op);
					} else {
						final int val = Integer.parseInt(message.tag.getString(NBT_INT_VAL));
						te.setLogicCount(val);
					}
					
					// Cause an update to be sent back
					IBlockState state = te.getWorld().getBlockState(te.getPos());
					te.getWorld().notifyBlockUpdate(te.getPos(), state, state, 2);
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
	
	public static enum Action {
		TEMPLATE,
		OP,
		COUNT,
	}

	private static final String NBT_TYPE = "type";
	private static final String NBT_INT_VAL = "val";
	private static final String NBT_ITEM_VAL = "item";
	private static final String NBT_POS = "pos";
	
	protected NBTTagCompound tag;
	
	public LogisticsSensorActionMessage() {
		this(null, (String) null, null);
	}
	
	public LogisticsSensorActionMessage(LogisticsSensorTileEntity ent, @Nullable ItemStack template) {
		this(Action.TEMPLATE, template, ent.getPos());
	}
	
	public LogisticsSensorActionMessage(LogisticsSensorTileEntity ent, SensorLogicOp op) {
		this(Action.OP, op.name(), ent.getPos());
	}
	
	public LogisticsSensorActionMessage(LogisticsSensorTileEntity ent, int val) {
		this(Action.COUNT, String.format("%d", val), ent.getPos());
	}
	
	protected LogisticsSensorActionMessage(Action type, String val, BlockPos pos) {
		tag = new NBTTagCompound();
		
		if (type != null) {
			tag.setString(NBT_TYPE, type.name());
			tag.setString(NBT_INT_VAL, val);
			tag.setLong(NBT_POS, pos.toLong());
		}
	}
	
	protected LogisticsSensorActionMessage(Action type, @Nullable ItemStack template, BlockPos pos) {
		tag = new NBTTagCompound();
		
		if (type != null) {
			tag.setString(NBT_TYPE, type.name());
			if (template != null) {
				tag.setTag(NBT_ITEM_VAL, template.serializeNBT());
			}
			tag.setLong(NBT_POS, pos.toLong());
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
