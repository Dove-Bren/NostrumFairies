package com.smanzana.nostrumfairies.capabilities;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.fey.NostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.templates.ITemplateViewerCapability;
import com.smanzana.nostrummagica.capabilities.AutoCapabilityProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityHandler {

	private static final ResourceLocation FEY_CAP_LOC = new ResourceLocation(NostrumFairies.MODID, "fey_capability");
	
	public static final Capability<INostrumFeyCapability> CAPABILITY_FEY = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<ITemplateViewerCapability> CAPABILITY_TEMPLATE_VIEWER = CapabilityManager.get(new CapabilityToken<>() {});
	
	public CapabilityHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void attachEntity(AttachCapabilitiesEvent<Entity> event) {
		
		//if player. Or not. Should get config going. For now, if it's a player make it?
		//also need to catch death, etc
		if (event.getObject() instanceof Player player) {
			//attach that shizz
			event.addCapability(FEY_CAP_LOC, new AutoCapabilityProvider<>(CAPABILITY_FEY, new NostrumFeyCapability(player)));
		}
	}
	
	@SubscribeEvent
	public void onClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			event.getOriginal().reviveCaps();
			
			INostrumFeyCapability cap = NostrumFairies.getFeyWrapper(event.getOriginal());
			event.getPlayer().getCapability(CAPABILITY_FEY, null)
				.orElse(null).deserializeNBT(cap.serializeNBT());
			
			event.getOriginal().invalidateCaps();
		}
		//if (!event.getPlayerEntity().world.isRemote)
		//	NostrumMagica.proxy.syncPlayer((ServerPlayerEntity) event.getPlayerEntity());
	}
	
//	@SubscribeEvent
//	public void attachItemstack(AttachCapabilitiesEvent<ItemStack> event) {
//		// If there were things to attach to, I'd do this and figure out if the stack was for that item and attach
//	}
}
