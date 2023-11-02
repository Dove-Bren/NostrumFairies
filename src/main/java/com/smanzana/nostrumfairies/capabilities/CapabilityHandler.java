package com.smanzana.nostrumfairies.capabilities;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {

	public static final ResourceLocation FEY_CAP_LOC = new ResourceLocation(NostrumFairies.MODID, "fey_capability");
	
	public CapabilityHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void attachEntity(AttachCapabilitiesEvent<Entity> event) {
		
		//if player. Or not. Should get config going. For now, if it's a player make it?
		//also need to catch death, etc
		if (event.getObject() instanceof PlayerEntity) {
			//attach that shizz
			event.addCapability(FEY_CAP_LOC, new AttributeProvider(event.getObject()));
			
			if (event.getObject().world != null && event.getObject().world.isRemote) {
				NostrumFairies.proxy.requestCapabilityRefresh();
			}
		}
	}
	
	@SubscribeEvent
	public void onClone(PlayerEvent.Clone event) {
		//if (event.isWasDeath()) {
			INostrumFeyCapability cap = NostrumFairies.getFeyWrapper(event.getOriginal());
			event.getPlayerEntity().getCapability(AttributeProvider.CAPABILITY, null)
				.readNBT(cap.toNBT());
		//}
		//if (!event.getPlayerEntity().world.isRemote)
		//	NostrumMagica.proxy.syncPlayer((ServerPlayerEntity) event.getPlayerEntity());
	}
	
//	@SubscribeEvent
//	public void attachItemstack(AttachCapabilitiesEvent<ItemStack> event) {
//		// If there were things to attach to, I'd do this and figure out if the stack was for that item and attach
//	}
}
