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
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {
	
	private KeyBinding bindingScroll;
	private KeyBinding bindingWandModeForward;
	private KeyBinding bindingWandModeBackward;

	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void initKeybinds() {
		bindingScroll = new KeyBinding("key.wandscroll.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingScroll);
		bindingWandModeForward = new KeyBinding("key.wandmode.forward.desc", GLFW.GLFW_KEY_RIGHT_BRACKET, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingWandModeForward);
		bindingWandModeBackward = new KeyBinding("key.wandmode.backward.desc", GLFW.GLFW_KEY_LEFT_BRACKET, "key.nostrumfairies.desc");
		ClientRegistry.registerKeyBinding(bindingWandModeBackward);
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public PlayerEntity getPlayer() {
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
	public void pushCapabilityRefresh(PlayerEntity player) {
		if (!player.world.isRemote) {
			super.pushCapabilityRefresh(player);
		}
		; // Nothing on client
	}
	
	@Override
	public void openStorageMonitor(World world, BlockPos pos) {
		if (world.isRemote())
		{
			StorageMonitorTileEntity monitor = (StorageMonitorTileEntity) world.getTileEntity(pos);
			Minecraft.getInstance().displayGuiScreen(new StorageMonitorScreen(monitor));
		}
	}
	
	@SubscribeEvent
	public void onMouse(MouseScrollEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		int wheel = event.getMouseY() < 0 ? -1 : event.getMouseY() > 0 ? 1 : 0;
		if (wheel != 0) {
			if (!NostrumFairies.getFeyWrapper(player)
					.builderFairyUnlocked()) {
				return;
			}
			
			if (bindingScroll.isKeyDown()) {
				ItemStack wand = player.getHeldItemMainhand();
				if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || TemplateWand.GetWandMode(wand) != WandMode.SPAWN) {
					wand = player.getHeldItemOffhand();
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
		PlayerEntity player = mc.player;
		final boolean forwardPressed = bindingWandModeForward.isPressed(); 
		if (forwardPressed || bindingWandModeBackward.isPressed()) {
			final INostrumMagic magic = NostrumMagica.getMagicWrapper(player);
			if (magic == null || !magic.getCompletedResearches().contains("logistics_construction") ) {
				return;
			}
			
			ItemStack wand = player.getHeldItemMainhand();
			if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand)) {
				wand = player.getHeldItemOffhand();
			}
			
			if (!wand.isEmpty() && wand.getItem() instanceof TemplateWand) {
				TemplateWand.HandleModeChange(player, wand, forwardPressed);
				return;
			}
		}
	}
	
}
