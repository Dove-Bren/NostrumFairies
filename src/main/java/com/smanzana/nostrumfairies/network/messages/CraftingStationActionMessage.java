package com.smanzana.nostrumfairies.network.messages;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.CraftingBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.CraftingBlockTileEntity.CraftingCriteriaMode;
import com.smanzana.nostrumfairies.blocks.CraftingBlockTileEntity.CraftingLogicOp;

import io.netty.buffer.ByteBuf;
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
public class CraftingStationActionMessage implements IMessage {

	public static class Handler implements IMessageHandler<CraftingStationActionMessage, IMessage> {

		@Override
		public IMessage onMessage(CraftingStationActionMessage message, MessageContext ctx) {
			
			try {
				final Action type = Action.valueOf(message.tag.getString(NBT_TYPE));
				final BlockPos pos = BlockPos.fromLong(message.tag.getLong(NBT_POS));
				final World world = ctx.getServerHandler().playerEntity.worldObj;
				
				CraftingBlockTileEntity te = (CraftingBlockTileEntity) world.getTileEntity(pos);
				
				ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(() -> {
					if (type == Action.MODE) {
						CraftingCriteriaMode mode = CraftingCriteriaMode.valueOf(message.tag.getString(NBT_VAL));
						te.setCriteriaMode(mode);
					} else if (type == Action.OP) {
						CraftingLogicOp op = CraftingLogicOp.valueOf(message.tag.getString(NBT_VAL));
						te.setCriteriaOp(op);
					} else {
						final int val = Integer.parseInt(message.tag.getString(NBT_VAL));
						te.setCriteriaCount(val);
					}
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
		MODE,
		OP,
		COUNT,
	}

	private static final String NBT_TYPE = "type";
	private static final String NBT_VAL = "val";
	private static final String NBT_POS = "pos";
	
	protected NBTTagCompound tag;
	
	public CraftingStationActionMessage() {
		this(null, null, null);
	}
	
	public CraftingStationActionMessage(CraftingBlockTileEntity ent, CraftingCriteriaMode mode) {
		this(Action.MODE, mode.name(), ent.getPos());
	}
	
	public CraftingStationActionMessage(CraftingBlockTileEntity ent, CraftingLogicOp op) {
		this(Action.OP, op.name(), ent.getPos());
	}
	
	public CraftingStationActionMessage(CraftingBlockTileEntity ent, int val) {
		this(Action.COUNT, String.format("%d", val), ent.getPos());
	}
	
	protected CraftingStationActionMessage(Action type, String val, BlockPos pos) {
		tag = new NBTTagCompound();
		
		if (type != null) {
			tag.setString(NBT_TYPE, type.name());
			tag.setString(NBT_VAL, val);
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
