package com.smanzana.nostrumfairies.capabilities;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityHandler {

	public static final ResourceLocation FEY_CAP_LOC = new ResourceLocation(NostrumFairies.MODID, "fey_capability");
	
	public CapabilityHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void attachEntity(AttachCapabilitiesEvent<Entity> event) {
		
		//if player. Or not. Should get config going. For now, if it's a player make it?
		//also need to catch death, etc
		if (event.getObject() instanceof Player) {
			//attach that shizz
			event.addCapability(FEY_CAP_LOC, new AttributeProvider(event.getObject()));
			
			if (event.getObject().level != null && event.getObject().level.isClientSide) {
				//NostrumFairies.proxy.requestCapabilityRefresh();
			}
		}
	}
	
	@SubscribeEvent
	public void onClone(PlayerEvent.Clone event) {
		//if (event.isWasDeath()) {
			INostrumFeyCapability cap = NostrumFairies.getFeyWrapper(event.getOriginal());
			event.getPlayer().getCapability(AttributeProvider.CAPABILITY, null)
				.orElse(null).readNBT(cap.toNBT());
		//}
		//if (!event.getPlayerEntity().world.isRemote)
		//	NostrumMagica.proxy.syncPlayer((ServerPlayerEntity) event.getPlayerEntity());
	}
	
//	@SubscribeEvent
//	public void attachItemstack(AttachCapabilitiesEvent<ItemStack> event) {
//		// If there were things to attach to, I'd do this and figure out if the stack was for that item and attach
//	}
}
