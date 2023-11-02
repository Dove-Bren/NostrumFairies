package com.smanzana.nostrumfairies.network.messages;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has changed mode or index and is sending such to the server
 * @author Skyler
 *
 */
public class TemplateWandUpdate implements IMessage {

	public static class Handler implements IMessageHandler<TemplateWandUpdate, IMessage> {

		@Override
		public IMessage onMessage(TemplateWandUpdate message, MessageContext ctx) {
			
			try {
				final WandUpdateType type = WandUpdateType.valueOf(message.tag.getString(NBT_TYPE));
				final boolean val = message.tag.getBoolean(NBT_VAL);
				
				ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
					@Nonnull ItemStack wand = ctx.getServerHandler().player.getHeldItemMainhand();
					if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || 
							(type == WandUpdateType.SCROLL && TemplateWand.getModeOf(wand) != WandMode.SPAWN)) {
						wand = ctx.getServerHandler().player.getHeldItemOffhand();
					}
					
					if (!wand.isEmpty() && wand.getItem() instanceof TemplateWand &&
							(type != WandUpdateType.SCROLL || TemplateWand.getModeOf(wand) == WandMode.SPAWN)) {
						if (type == WandUpdateType.MODE) {
							TemplateWand.HandleModeChange(ctx.getServerHandler().player, wand, val);
						} else {
							TemplateWand.HandleScroll(ctx.getServerHandler().player, wand, val);
						}
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
	
	public static enum WandUpdateType {
		SCROLL,
		MODE,
	}

	private static final String NBT_TYPE = "type";
	private static final String NBT_VAL = "val";
	
	protected CompoundNBT tag;
	
	public TemplateWandUpdate() {
		this(WandUpdateType.MODE, true);
	}
	
	public TemplateWandUpdate(WandUpdateType type, boolean val) {
		tag = new CompoundNBT();
		
		if (type != null) {
			tag.setString(NBT_TYPE, type.name());
			tag.setBoolean(NBT_VAL, val);
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
