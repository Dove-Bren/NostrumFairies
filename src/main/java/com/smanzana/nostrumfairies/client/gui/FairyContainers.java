package com.smanzana.nostrumfairies.client.gui;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.container.BufferChestGui;
import com.smanzana.nostrumfairies.client.gui.container.BuildingBlockGui;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationGui;
import com.smanzana.nostrumfairies.client.gui.container.CraftingStationSmallGui;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui;
import com.smanzana.nostrumfairies.client.gui.container.HomeBlockGui;
import com.smanzana.nostrumfairies.client.gui.container.InputChestGui;
import com.smanzana.nostrumfairies.client.gui.container.LogisticsSensorGui;
import com.smanzana.nostrumfairies.client.gui.container.OutputChestGui;
import com.smanzana.nostrumfairies.client.gui.container.OutputPanelGui;
import com.smanzana.nostrumfairies.client.gui.container.StorageChestGui;
import com.smanzana.nostrumfairies.client.gui.container.TemplateWandGui;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FairyContainers {

	@ObjectHolder(BufferChestGui.BufferChestContainer.ID) public static MenuType<BufferChestGui.BufferChestContainer> BufferChest;
	@ObjectHolder(BuildingBlockGui.BuildingBlockContainer.ID) public static MenuType<BuildingBlockGui.BuildingBlockContainer> BuildingBlock;
	@ObjectHolder(CraftingStationGui.CraftingStationContainer.ID) public static MenuType<CraftingStationGui.CraftingStationContainer> CraftingStation;
	@ObjectHolder(CraftingStationSmallGui.CraftingStationSmallContainer.ID) public static MenuType<CraftingStationSmallGui.CraftingStationSmallContainer> CraftingStationSmall;
	@ObjectHolder(FairyScreenGui.FairyScreenContainer.ID) public static MenuType<FairyScreenGui.FairyScreenContainer> FairyScreen;
	@ObjectHolder(HomeBlockGui.HomeBlockContainer.ID) public static MenuType<HomeBlockGui.HomeBlockContainer> HomeBlock;
	@ObjectHolder(InputChestGui.InputChestContainer.ID) public static MenuType<InputChestGui.InputChestContainer> InputChest;
	@ObjectHolder(LogisticsSensorGui.LogisticsSensorContainer.ID) public static MenuType<LogisticsSensorGui.LogisticsSensorContainer> LogisticsSensor;
	@ObjectHolder(OutputChestGui.OutputChestContainer.ID) public static MenuType<OutputChestGui.OutputChestContainer> OutputChest;
	@ObjectHolder(OutputPanelGui.OutputPanelContainer.ID) public static MenuType<OutputPanelGui.OutputPanelContainer> OutputPanel;
	@ObjectHolder(StorageChestGui.StorageChestContainer.ID) public static MenuType<StorageChestGui.StorageChestContainer> StorageChest;
	@ObjectHolder(TemplateWandGui.TemplateWandContainer.ID) public static MenuType<TemplateWandGui.TemplateWandContainer> TemplateWand;
	
	
	@SubscribeEvent
	public static void registerContainers(final RegistryEvent.Register<MenuType<?>> event) {
		final IForgeRegistry<MenuType<?>> registry = event.getRegistry();
		
		registry.register(IForgeMenuType.create(BufferChestGui.BufferChestContainer::FromNetwork).setRegistryName(BufferChestGui.BufferChestContainer.ID));
		registry.register(IForgeMenuType.create(BuildingBlockGui.BuildingBlockContainer::FromNetwork).setRegistryName(BuildingBlockGui.BuildingBlockContainer.ID));
		registry.register(IForgeMenuType.create(CraftingStationGui.CraftingStationContainer::FromNetwork).setRegistryName(CraftingStationGui.CraftingStationContainer.ID));
		registry.register(IForgeMenuType.create(CraftingStationSmallGui.CraftingStationSmallContainer::FromNetwork).setRegistryName(CraftingStationSmallGui.CraftingStationSmallContainer.ID));
		registry.register(IForgeMenuType.create(FairyScreenGui.FairyScreenContainer::FromNetwork).setRegistryName(FairyScreenGui.FairyScreenContainer.ID));
		registry.register(IForgeMenuType.create(HomeBlockGui.HomeBlockContainer::FromNetwork).setRegistryName(HomeBlockGui.HomeBlockContainer.ID));
		registry.register(IForgeMenuType.create(InputChestGui.InputChestContainer::FromNetwork).setRegistryName(InputChestGui.InputChestContainer.ID));
		registry.register(IForgeMenuType.create(LogisticsSensorGui.LogisticsSensorContainer::FromNetwork).setRegistryName(LogisticsSensorGui.LogisticsSensorContainer.ID));
		registry.register(IForgeMenuType.create(OutputChestGui.OutputChestContainer::FromNetwork).setRegistryName(OutputChestGui.OutputChestContainer.ID));
		registry.register(IForgeMenuType.create(OutputPanelGui.OutputPanelContainer::FromNetwork).setRegistryName(OutputPanelGui.OutputPanelContainer.ID));
		registry.register(IForgeMenuType.create(StorageChestGui.StorageChestContainer::FromNetwork).setRegistryName(StorageChestGui.StorageChestContainer.ID));
		registry.register(IForgeMenuType.create(TemplateWandGui.TemplateWandContainer::FromNetwork).setRegistryName(TemplateWandGui.TemplateWandContainer.ID));
	}
}
