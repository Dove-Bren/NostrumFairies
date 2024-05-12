package com.smanzana.nostrumfairies.proxy;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilitySyncMessage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommonProxy {
	
	public CommonProxy() {
		;
	}
	
	public PlayerEntity getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public boolean isServer() {
		return true;
	}
	
	public void requestCapabilityRefresh() {
		; // Nothing on server
	}
	
	public void pushCapabilityRefresh(PlayerEntity player) {
		INostrumFeyCapability feyAttr = NostrumFairies.getFeyWrapper(player);
		if (feyAttr != null) {
			NetworkHandler.sendTo(new CapabilitySyncMessage(feyAttr), (ServerPlayerEntity) player);
		}
	}
	
	public void openStorageMonitor(World world, BlockPos pos) {
		; // Nothing on server
	}
}
