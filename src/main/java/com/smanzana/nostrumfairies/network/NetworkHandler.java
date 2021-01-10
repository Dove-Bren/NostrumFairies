package com.smanzana.nostrumfairies.network;

import com.smanzana.nostrumfairies.network.messages.CapabilityRequest;
import com.smanzana.nostrumfairies.network.messages.CapabilitySyncMessage;
import com.smanzana.nostrumfairies.network.messages.FairyGuiActionMessage;
import com.smanzana.nostrumfairies.network.messages.LogicPanelActionMessage;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateResponse;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateSingleResponse;
import com.smanzana.nostrumfairies.network.messages.TemplateWandUpdate;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

	private static SimpleNetworkWrapper syncChannel;
	
	private static int discriminator = 10;
	
	private static final String CHANNEL_SYNC_NAME = "nostrumfairy_net"; // must be <20 chars
	
	
	public static SimpleNetworkWrapper getSyncChannel() {
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
		
		syncChannel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_SYNC_NAME);
		
		syncChannel.registerMessage(LogisticsUpdateRequest.Handler.class, LogisticsUpdateRequest.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(LogisticsUpdateResponse.Handler.class, LogisticsUpdateResponse.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(LogisticsUpdateSingleResponse.Handler.class, LogisticsUpdateSingleResponse.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(CapabilitySyncMessage.Handler.class, CapabilitySyncMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(CapabilityRequest.Handler.class, CapabilityRequest.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(FairyGuiActionMessage.Handler.class, FairyGuiActionMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(TemplateWandUpdate.Handler.class, TemplateWandUpdate.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(LogicPanelActionMessage.Handler.class, LogicPanelActionMessage.class, discriminator++, Side.SERVER);
	}
	
}
