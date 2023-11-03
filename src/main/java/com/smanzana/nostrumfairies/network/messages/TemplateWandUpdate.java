package com.smanzana.nostrumfairies.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has changed mode or index and is sending such to the server
 * @author Skyler
 *
 */
public class TemplateWandUpdate {

	public static void handle(TemplateWandUpdate message, Supplier<NetworkEvent.Context> ctx) {
		
		ctx.get().enqueueWork(() -> {
			try {
				final ServerPlayerEntity player = ctx.get().getSender();
				@Nonnull ItemStack wand = player.getHeldItemMainhand();
				if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || 
						(message.type == WandUpdateType.SCROLL && TemplateWand.GetWandMode(wand) != WandMode.SPAWN)) {
					wand = player.getHeldItemOffhand();
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
	
	public static TemplateWandUpdate decode(PacketBuffer buf) {
		return new TemplateWandUpdate(
				buf.readEnumValue(WandUpdateType.class),
				buf.readBoolean()
				);
	}

	public static void encode(TemplateWandUpdate msg, PacketBuffer buf) {
		buf.writeEnumValue(msg.type);
		buf.writeBoolean(msg.val);
	}

}
