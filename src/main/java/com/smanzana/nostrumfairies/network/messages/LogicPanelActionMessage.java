package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.ILogisticsLogicProvider;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.LogicMode;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.LogicOp;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

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
		final Level world = ctx.get().getSender().level;
		
		// Implemented ILogisticsLogicProvider on your tile entity if it has a logic component that uses the logic panel gui
		ILogisticsLogicProvider te = (ILogisticsLogicProvider) world.getBlockEntity(message.pos);
		
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
				world.sendBlockUpdated(message.pos, state, state, 2);

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
		this(Action.TEMPLATE, ent.getBlockPos(), null, null, null, template);
	}
	
	public LogicPanelActionMessage(ILogisticsLogicProvider ent, LogicOp op) {
		this(Action.OP, ent.getBlockPos(), op, null, null, null);
	}
	
	public LogicPanelActionMessage(ILogisticsLogicProvider ent, int val) {
		this(Action.COUNT, ent.getBlockPos(), null, null, val, null);
	}

	public LogicPanelActionMessage(ILogisticsLogicProvider ent, LogicMode mode) {
		this(Action.MODE, ent.getBlockPos(), null, mode, null, null);
	}
	
	public static LogicPanelActionMessage decode(FriendlyByteBuf buf) {
		final Action action = buf.readEnum(Action.class);
		final BlockPos pos = buf.readBlockPos();
		final @Nullable LogicOp opVal = buf.readBoolean() ? buf.readEnum(LogicOp.class) : null;
		final @Nullable LogicMode modeVal = buf.readBoolean() ? buf.readEnum(LogicMode.class) : null;
		final @Nullable Integer intVal = buf.readBoolean() ? buf.readVarInt() : null;
		final @Nullable ItemStack itemVal = buf.readBoolean() ? buf.readItem() : null;
		
		return new LogicPanelActionMessage(action, pos, opVal, modeVal, intVal, itemVal);
	}

	public static void encode(LogicPanelActionMessage msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.action);
		buf.writeBlockPos(msg.pos);
		
		if (msg.opVal != null) {
			buf.writeBoolean(true);
			buf.writeEnum(msg.opVal);
		} else {
			buf.writeBoolean(false);
		}
		
		if (msg.modeVal != null) {
			buf.writeBoolean(true);
			buf.writeEnum(msg.modeVal);
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
			buf.writeItem(msg.itemVal);
		} else {
			buf.writeBoolean(false);
		}
	}

}
