package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has changed mode or index and is sending such to the server
 * @author Skyler
 *
 */
public class TemplateWandUpdate {

	public static void handle(TemplateWandUpdate message, Supplier<NetworkEvent.Context> ctx) {
		
		ctx.get().enqueueWork(() -> {
			try {
				final ServerPlayer player = ctx.get().getSender();
				@Nonnull ItemStack wand = player.getMainHandItem();
				if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || 
						(message.type == WandUpdateType.SCROLL && TemplateWand.GetWandMode(wand) != WandMode.SPAWN)) {
					wand = player.getOffhandItem();
				}
				
				if (!wand.isEmpty() && wand.getItem() instanceof TemplateWand &&
						(message.type != WandUpdateType.SCROLL || TemplateWand.GetWandMode(wand) == WandMode.SPAWN)) {
					if (message.type == WandUpdateType.MODE) {
						TemplateWand.HandleModeChange(player, wand, message.val);
					} else {
						TemplateWand.HandleScroll(player, wand, message.val);
					}
				}
			} catch (Exception e) {
				NostrumFairies.logger.error(e);
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
	
	public static enum WandUpdateType {
		SCROLL,
		MODE,
	}
	
	private final WandUpdateType type;
	private final boolean val;

	public TemplateWandUpdate(WandUpdateType type, boolean val) {
		this.type = type;
		this.val = val;
	}
	
	public static TemplateWandUpdate decode(FriendlyByteBuf buf) {
		return new TemplateWandUpdate(
				buf.readEnum(WandUpdateType.class),
				buf.readBoolean()
				);
	}

	public static void encode(TemplateWandUpdate msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.type);
		buf.writeBoolean(msg.val);
	}

}
