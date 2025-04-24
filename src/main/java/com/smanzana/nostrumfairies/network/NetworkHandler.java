package com.smanzana.nostrumfairies.network;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.network.messages.CapabilityRequest;
import com.smanzana.nostrumfairies.network.messages.CapabilitySyncMessage;
import com.smanzana.nostrumfairies.network.messages.FairyGuiActionMessage;
import com.smanzana.nostrumfairies.network.messages.LogicPanelActionMessage;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateResponse;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateSingleResponse;
import com.smanzana.nostrumfairies.network.messages.StorageMonitorRequestMessage;
import com.smanzana.nostrumfairies.network.messages.TemplateWandUpdate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {

	private static SimpleChannel syncChannel;
	
	private static int discriminator = 10;
	
	private static final String CHANNEL_SYNC_NAME = "nostrumfairy_net"; // must be <20 chars
	private static final String PROTOCOL = "1";
	
	
	public static SimpleChannel getSyncChannel() {
		getInstance();
		return syncChannel;
	}
	
	private static NetworkHandler instance;
	
	public static NetworkHandler getInstance() {
		if (instance == null)
			instance = new NetworkHandler();
		
		return instance;
	}
	
	public NetworkHandler() {
		
		syncChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(NostrumFairies.MODID, CHANNEL_SYNC_NAME),
				() -> PROTOCOL,
				PROTOCOL::equals,
				PROTOCOL::equals
				);
		
		syncChannel.registerMessage(discriminator++, LogisticsUpdateRequest.class, LogisticsUpdateRequest::encode, LogisticsUpdateRequest::decode, LogisticsUpdateRequest::handle);
		syncChannel.registerMessage(discriminator++, LogisticsUpdateResponse.class, LogisticsUpdateResponse::encode, LogisticsUpdateResponse::decode, LogisticsUpdateResponse::handle);
		syncChannel.registerMessage(discriminator++, LogisticsUpdateSingleResponse.class, LogisticsUpdateSingleResponse::encode, LogisticsUpdateSingleResponse::decode, LogisticsUpdateSingleResponse::handle);
		syncChannel.registerMessage(discriminator++, CapabilitySyncMessage.class, CapabilitySyncMessage::encode, CapabilitySyncMessage::decode, CapabilitySyncMessage::handle);
		syncChannel.registerMessage(discriminator++, CapabilityRequest.class, CapabilityRequest::encode, CapabilityRequest::decode, CapabilityRequest::handle);
		syncChannel.registerMessage(discriminator++, FairyGuiActionMessage.class, FairyGuiActionMessage::encode, FairyGuiActionMessage::decode, FairyGuiActionMessage::handle);
		syncChannel.registerMessage(discriminator++, TemplateWandUpdate.class, TemplateWandUpdate::encode, TemplateWandUpdate::decode, TemplateWandUpdate::handle);
		syncChannel.registerMessage(discriminator++, LogicPanelActionMessage.class, LogicPanelActionMessage::encode, LogicPanelActionMessage::decode, LogicPanelActionMessage::handle);
		syncChannel.registerMessage(discriminator++, StorageMonitorRequestMessage.class, StorageMonitorRequestMessage::encode, StorageMonitorRequestMessage::decode, StorageMonitorRequestMessage::handle);
	}
	
	public static <T> void sendTo(T msg, ServerPlayer player) {
		NetworkHandler.syncChannel.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static <T> void sendToServer(T msg) {
		NetworkHandler.syncChannel.sendToServer(msg);
	}

	public static <T> void sendToAll(T msg) {
		NetworkHandler.syncChannel.send(PacketDistributor.ALL.noArg(), msg);
	}

	public static <T> void sendToDimension(T msg, ResourceKey<Level> dimension) {
		NetworkHandler.syncChannel.send(PacketDistributor.DIMENSION.with(() -> dimension), msg);
	}
	
	public static <T> void sendToAllAround(T msg, TargetPoint point) {
		NetworkHandler.syncChannel.send(PacketDistributor.NEAR.with(() -> point), msg);
	}

	public static <T> void sendToAllTracking(T msg, Entity ent) {
		NetworkHandler.syncChannel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> ent), msg);
	}
	
}
