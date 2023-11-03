package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.ILogisticsLogicProvider;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.LogicMode;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.LogicOp;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has made an action in a crafting station
 * @author Skyler
 *
 */
public class LogicPanelActionMessage {
	
	public static enum Action {
		TEMPLATE,
		OP,
		COUNT,
		MODE,
	}

	public static void handle(LogicPanelActionMessage message, Supplier<NetworkEvent.Context> ctx) {
		final World world = ctx.get().getSender().world;
		
		// Implemented ILogisticsLogicProvider on your tile entity if it has a logic component that uses the logic panel gui
		ILogisticsLogicProvider te = (ILogisticsLogicProvider) world.getTileEntity(message.pos);
		
		ctx.get().enqueueWork(() -> {
			try {
				final LogisticsLogicComponent comp = te.getLogicComponent();
				if (message.action == Action.TEMPLATE) {
					comp.setLogicTemplate(message.itemVal);
				} else if (message.action == Action.OP) {
					comp.setLogicOp(message.opVal);
				} else if (message.action == Action.MODE) {
					comp.setLogicMode(message.modeVal);
				} else {
					comp.setLogicCount(message.intVal);
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

	private final Action action;
	private final BlockPos pos;
	
	// One of these should be filled in, depending on the Action
	private final @Nullable LogicOp opVal;
	private final @Nullable LogicMode modeVal;
	private final @Nullable Integer intVal;
	private final @Nullable ItemStack itemVal;
	
	protected LogicPanelActionMessage(Action action, BlockPos pos,
			@Nullable LogicOp opVal,
			@Nullable LogicMode modeVal,
			@Nullable Integer intVal,
			@Nullable ItemStack itemVal) {
		
		this.action = action;
		this.pos = pos;
		
		this.opVal = opVal;
		this.modeVal = modeVal;
		this.intVal = intVal;
		this.itemVal = itemVal;
	}
	
	public LogicPanelActionMessage(ILogisticsLogicProvider ent, @Nonnull ItemStack template) {
		this(Action.TEMPLATE, ent.getPos(), null, null, null, template);
	}
	
	public LogicPanelActionMessage(ILogisticsLogicProvider ent, LogicOp op) {
		this(Action.OP, ent.getPos(), op, null, null, null);
	}
	
	public LogicPanelActionMessage(ILogisticsLogicProvider ent, int val) {
		this(Action.COUNT, ent.getPos(), null, null, val, null);
	}

	public LogicPanelActionMessage(ILogisticsLogicProvider ent, LogicMode mode) {
		this(Action.MODE, ent.getPos(), null, mode, null, null);
	}
	
	public static LogicPanelActionMessage decode(PacketBuffer buf) {
		final Action action = buf.readEnumValue(Action.class);
		final BlockPos pos = buf.readBlockPos();
		final @Nullable LogicOp opVal = buf.readBoolean() ? buf.readEnumValue(LogicOp.class) : null;
		final @Nullable LogicMode modeVal = buf.readBoolean() ? buf.readEnumValue(LogicMode.class) : null;
		final @Nullable Integer intVal = buf.readBoolean() ? buf.readVarInt() : null;
		final @Nullable ItemStack itemVal = buf.readBoolean() ? buf.readItemStack() : null;
		
		return new LogicPanelActionMessage(action, pos, opVal, modeVal, intVal, itemVal);
	}

	public static void encode(LogicPanelActionMessage msg, PacketBuffer buf) {
		buf.writeEnumValue(msg.action);
		buf.writeBlockPos(msg.pos);
		
		if (msg.opVal != null) {
			buf.writeBoolean(true);
			buf.writeEnumValue(msg.opVal);
		} else {
			buf.writeBoolean(false);
		}
		
		if (msg.modeVal != null) {
			buf.writeBoolean(true);
			buf.writeEnumValue(msg.modeVal);
		} else {
			buf.writeBoolean(false);
		}
		
		if (msg.intVal != null) {
			buf.writeBoolean(true);
			buf.writeVarInt(msg.intVal);
		} else {
			buf.writeBoolean(false);
		}
		
		if (msg.itemVal != null) {
			buf.writeBoolean(true);
			buf.writeItemStack(msg.itemVal);
		} else {
			buf.writeBoolean(false);
		}
	}

}
