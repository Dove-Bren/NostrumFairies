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

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = NostrumFairies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(NostrumFairies.MODID)
public class FairyContainers {

	@ObjectHolder(BufferChestGui.BufferChestContainer.ID) public static ContainerType<BufferChestGui.BufferChestContainer> BufferChest;
	@ObjectHolder(BuildingBlockGui.BuildingBlockContainer.ID) public static ContainerType<BuildingBlockGui.BuildingBlockContainer> BuildingBlock;
	@ObjectHolder(CraftingStationGui.CraftingStationContainer.ID) public static ContainerType<CraftingStationGui.CraftingStationContainer> CraftingStation;
	@ObjectHolder(CraftingStationSmallGui.CraftingStationSmallContainer.ID) public static ContainerType<CraftingStationSmallGui.CraftingStationSmallContainer> CraftingStationSmall;
	@ObjectHolder(FairyScreenGui.FairyScreenContainer.ID) public static ContainerType<FairyScreenGui.FairyScreenContainer> FairyScreen;
	@ObjectHolder(HomeBlockGui.HomeBlockContainer.ID) public static ContainerType<HomeBlockGui.HomeBlockContainer> HomeBlock;
	@ObjectHolder(InputChestGui.InputChestContainer.ID) public static ContainerType<InputChestGui.InputChestContainer> InputChest;
	@ObjectHolder(LogisticsSensorGui.LogisticsSensorContainer.ID) public static ContainerType<LogisticsSensorGui.LogisticsSensorContainer> LogisticsSensor;
	@ObjectHolder(OutputChestGui.OutputChestContainer.ID) public static ContainerType<OutputChestGui.OutputChestContainer> OutputChest;
	@ObjectHolder(OutputPanelGui.OutputPanelContainer.ID) public static ContainerType<OutputPanelGui.OutputPanelContainer> OutputPanel;
	@ObjectHolder(StorageChestGui.StorageChestContainer.ID) public static ContainerType<StorageChestGui.StorageChestContainer> StorageChest;
	@ObjectHolder(TemplateWandGui.TemplateWandContainer.ID) public static ContainerType<TemplateWandGui.TemplateWandContainer> TemplateWand;
	
	
	@SubscribeEvent
	public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
		final IForgeRegistry<ContainerType<?>> registry = event.getRegistry();
		
		registry.register(IForgeContainerType.create(BufferChestGui.BufferChestContainer::FromNetwork).setRegistryName(BufferChestGui.BufferChestContainer.ID));
		registry.register(IForgeContainerType.create(BuildingBlockGui.BuildingBlockContainer::FromNetwork).setRegistryName(BuildingBlockGui.BuildingBlockContainer.ID));
		registry.register(IForgeContainerType.create(CraftingStationGui.CraftingStationContainer::FromNetwork).setRegistryName(CraftingStationGui.CraftingStationContainer.ID));
		registry.register(IForgeContainerType.create(CraftingStationSmallGui.CraftingStationSmallContainer::FromNetwork).setRegistryName(CraftingStationSmallGui.CraftingStationSmallContainer.ID));
		registry.register(IForgeContainerType.create(FairyScreenGui.FairyScreenContainer::FromNetwork).setRegistryName(FairyScreenGui.FairyScreenContainer.ID));
		registry.register(IForgeContainerType.create(HomeBlockGui.HomeBlockContainer::FromNetwork).setRegistryName(HomeBlockGui.HomeBlockContainer.ID));
		registry.register(IForgeContainerType.create(InputChestGui.InputChestContainer::FromNetwork).setRegistryName(InputChestGui.InputChestContainer.ID));
		registry.register(IForgeContainerType.create(LogisticsSensorGui.LogisticsSensorContainer::FromNetwork).setRegistryName(LogisticsSensorGui.LogisticsSensorContainer.ID));
		registry.register(IForgeContainerType.create(OutputChestGui.OutputChestContainer::FromNetwork).setRegistryName(OutputChestGui.OutputChestContainer.ID));
		registry.register(IForgeContainerType.create(OutputPanelGui.OutputPanelContainer::FromNetwork).setRegistryName(OutputPanelGui.OutputPanelContainer.ID));
		registry.register(IForgeContainerType.create(StorageChestGui.StorageChestContainer::FromNetwork).setRegistryName(StorageChestGui.StorageChestContainer.ID));
		registry.register(IForgeContainerType.create(TemplateWandGui.TemplateWandContainer::FromNetwork).setRegistryName(TemplateWandGui.TemplateWandContainer.ID));
	}
	
	@SubscribeEvent
	public static void registerContainerScreens(FMLClientSetupEvent event) {
		ScreenManager.registerFactory(BufferChest, BufferChestGui.BufferChestGuiContainer::new);
		ScreenManager.registerFactory(BuildingBlock, BuildingBlockGui.BuildingBlockGuiContainer::new);
		ScreenManager.registerFactory(CraftingStation, CraftingStationGui.CraftingStationGuiContainer::new);
		ScreenManager.registerFactory(CraftingStationSmall, CraftingStationSmallGui.CraftingStationSmallGuiContainer::new);
		ScreenManager.registerFactory(FairyScreen, FairyScreenGui.FairyScreenGuiContainer::new);
		ScreenManager.registerFactory(HomeBlock, HomeBlockGui.HomeBlockGuiContainer::new);
		ScreenManager.registerFactory(InputChest, InputChestGui.InputChestGuiContainer::new);
		ScreenManager.registerFactory(LogisticsSensor, LogisticsSensorGui.LogisticsSensorGuiContainer::new);
		ScreenManager.registerFactory(OutputChest, OutputChestGui.OutputChestGuiContainer::new);
		ScreenManager.registerFactory(OutputPanel, OutputPanelGui.OutputPanelGuiContainer::new);
		ScreenManager.registerFactory(StorageChest, StorageChestGui.StorageChestGuiContainer::new);
		ScreenManager.registerFactory(TemplateWand, TemplateWandGui.TemplateWandGuiContainer::new);
	}
}
