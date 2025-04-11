package com.smanzana.nostrumfairies.proxy;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilitySyncMessage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class CommonProxy {
	
	public CommonProxy() {
		;
	}
	
	public Player getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public boolean isServer() {
		return true;
	}
	
	public void requestCapabilityRefresh() {
		; // Nothing on server
	}
	
	public void pushCapabilityRefresh(Player player) {
		INostrumFeyCapability feyAttr = NostrumFairies.getFeyWrapper(player);
		if (feyAttr != null) {
			NetworkHandler.sendTo(new CapabilitySyncMessage(feyAttr), (ServerPlayer) player);
		}
	}
	
	public void openStorageMonitor(Level world, BlockPos pos) {
		; // Nothing on server
	}
}
