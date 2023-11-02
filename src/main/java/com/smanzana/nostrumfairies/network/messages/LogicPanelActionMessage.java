package com.smanzana.nostrumfairies.network.messages;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.ILogisticsLogicProvider;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.LogicMode;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.LogicOp;

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
public class LogicPanelActionMessage implements IMessage {

	public static class Handler implements IMessageHandler<LogicPanelActionMessage, IMessage> {

		@Override
		public IMessage onMessage(LogicPanelActionMessage message, MessageContext ctx) {
			
			try {
				final Action type = Action.valueOf(message.tag.getString(NBT_TYPE));
				final BlockPos pos = BlockPos.fromLong(message.tag.getLong(NBT_POS));
				final World world = ctx.getServerHandler().player.world;
				
				// Implemented ILogisticsLogicProvider on your tile entity if it has a logic component that uses the logic panel gui
				ILogisticsLogicProvider te = (ILogisticsLogicProvider) world.getTileEntity(pos);
				
				ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
					final LogisticsLogicComponent comp = te.getLogicComponent();
					if (type == Action.TEMPLATE) {
						@Nonnull ItemStack stack = new ItemStack(message.tag.getCompoundTag(NBT_ITEM_VAL));
						comp.setLogicTemplate(stack);
					} else if (type == Action.OP) {
						LogicOp op = LogicOp.valueOf(message.tag.getString(NBT_INT_VAL));
						comp.setLogicOp(op);
					} else if (type == Action.MODE) {
						LogicMode mode = LogicMode.valueOf(message.tag.getString(NBT_INT_VAL));
						comp.setLogicMode(mode);
					} else {
						final int val = Integer.parseInt(message.tag.getString(NBT_INT_VAL));
						comp.setLogicCount(val);
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
	
	public static enum Action {
		TEMPLATE,
		OP,
		COUNT,
		MODE,
	}

	private static final String NBT_TYPE = "type";
	private static final String NBT_INT_VAL = "val";
	private static final String NBT_ITEM_VAL = "item";
	private static final String NBT_POS = "pos";
	
	protected NBTTagCompound tag;
	
	public LogicPanelActionMessage() {
		this(null, (String) null, null);
	}
	
	public LogicPanelActionMessage(ILogisticsLogicProvider ent, @Nonnull ItemStack template) {
		this(Action.TEMPLATE, template, ent.getPos());
	}
	
	public LogicPanelActionMessage(ILogisticsLogicProvider ent, LogicOp op) {
		this(Action.OP, op.name(), ent.getPos());
	}
	
	public LogicPanelActionMessage(ILogisticsLogicProvider ent, int val) {
		this(Action.COUNT, String.format("%d", val), ent.getPos());
	}

	public LogicPanelActionMessage(ILogisticsLogicProvider ent, LogicMode mode) {
		this(Action.MODE, mode.name(), ent.getPos());
	}
	
	protected LogicPanelActionMessage(Action type, String val, BlockPos pos) {
		tag = new NBTTagCompound();
		
		if (type != null) {
			tag.setString(NBT_TYPE, type.name());
			tag.setString(NBT_INT_VAL, val);
			tag.setLong(NBT_POS, pos.toLong());
		}
	}
	
	protected LogicPanelActionMessage(Action type, @Nonnull ItemStack template, BlockPos pos) {
		tag = new NBTTagCompound();
		
		if (type != null) {
			tag.setString(NBT_TYPE, type.name());
			if (!template.isEmpty()) {
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
