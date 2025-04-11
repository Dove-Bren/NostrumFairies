package com.smanzana.nostrumfairies.proxy;

import org.lwjgl.glfw.GLFW;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.StorageMonitorScreen;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.CapabilityRequest;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.tiles.StorageMonitorTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
	
	private KeyMapping bindingScroll;
	private KeyMapping bindingWandModeForward;
	private KeyMapping bindingWandModeBackward;

	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void initKeybinds() {
		bindingScroll = new KeyMapping("key.wandscroll.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingScroll);
		bindingWandModeForward = new KeyMapping("key.wandmode.forward.desc", GLFW.GLFW_KEY_RIGHT_BRACKET, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingWandModeForward);
		bindingWandModeBackward = new KeyMapping("key.wandmode.backward.desc", GLFW.GLFW_KEY_LEFT_BRACKET, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingWandModeBackward);
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public Player getPlayer() {
		final Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	@SubscribeEvent
	public void onClientConnect(EntityJoinWorldEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		if (event.getEntity() == mc.player) {
			// Every time we join a world, request a copy of its networks
			
			NostrumFairies.logger.info("Requested automatic logistics network refresh");
			NetworkHandler.sendToServer(new LogisticsUpdateRequest(null));
			NostrumFairies.proxy.requestCapabilityRefresh();
			StaticTESRRenderer.instance.clear();
		}
	}
	
	@Override
	public void requestCapabilityRefresh() {
		NetworkHandler.sendToServer(new CapabilityRequest());
	}
	
	@Override
	public void pushCapabilityRefresh(Player player) {
		if (!player.level.isClientSide) {
			super.pushCapabilityRefresh(player);
		}
		; // Nothing on client
	}
	
	@Override
	public void openStorageMonitor(Level world, BlockPos pos) {
		if (world.isClientSide())
		{
			StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) world.getBlockEntity(pos);
			Minecraft.getInstance().setScreen(new StorageMonitorScreen(monitor));
		}
	}
	
	@SubscribeEvent
	public void onMouse(MouseScrollEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		int wheel = event.getMouseY() < 0 ? -1 : event.getMouseY() > 0 ? 1 : 0;
		if (wheel != 0) {
			if (!NostrumFairies.getFeyWrapper(player)
					.builderFairyUnlocked()) {
				return;
			}
			
			if (bindingScroll.isDown()) {
				ItemStack wand = player.getMainHandItem();
				if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || TemplateWand.GetWandMode(wand) != WandMode.SPAWN) {
					wand = player.getOffhandItem();
				}
				
				if (!wand.isEmpty() && wand.getItem() instanceof TemplateWand && TemplateWand.GetWandMode(wand) == WandMode.SPAWN) {
					TemplateWand.HandleScroll(player, wand, wheel > 0);
					event.setCanceled(true);
					return;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onKey(KeyInputEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		final boolean forwardPressed = bindingWandModeForward.consumeClick(); 
		if (forwardPressed || bindingWandModeBackward.consumeClick()) {
			final INostrumMagic magic = NostrumMagica.getMagicWrapper(player);
			if (magic == null || !magic.getCompletedResearches().contains("logistics_construction") ) {
				return;
			}
			
			ItemStack wand = player.getMainHandItem();
			if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand)) {
				wand = player.getOffhandItem();
			}
			
			if (!wand.isEmpty() && wand.getItem() instanceof TemplateWand) {
				TemplateWand.HandleModeChange(player, wand, forwardPressed);
				return;
			}
		}
	}
	
}
