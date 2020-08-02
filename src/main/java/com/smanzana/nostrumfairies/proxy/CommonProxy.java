package com.smanzana.nostrumfairies.proxy;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.BufferLogisticsChest;
import com.smanzana.nostrumfairies.blocks.GatheringBlock;
import com.smanzana.nostrumfairies.blocks.InputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.LogisticsPylon;
import com.smanzana.nostrumfairies.blocks.MagicLight;
import com.smanzana.nostrumfairies.blocks.MiningBlock;
import com.smanzana.nostrumfairies.blocks.OutputLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageLogisticsChest;
import com.smanzana.nostrumfairies.blocks.StorageMonitor;
import com.smanzana.nostrumfairies.blocks.WoodcuttingBlock;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityElf;
import com.smanzana.nostrumfairies.entity.fey.EntityFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityTestFairy;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.sound.NostrumFairiesSounds;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
	
	public void preinit() {
		NetworkHandler.getInstance();
		NostrumFairiesSounds.registerSounds();
    	
    	int entityID = 0;
    	EntityRegistry.registerModEntity(EntityTestFairy.class, "test_fairy",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityFairy.class, "fairy",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityDwarf.class, "dwarf",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);
    	EntityRegistry.registerModEntity(EntityElf.class, "elf",
    			entityID++,
    			NostrumFairies.instance,
    			128,
    			1,
    			false
    			);

    	registerItems();
    	registerBlocks();
	}
	
	public void init() {
    	NetworkRegistry.INSTANCE.registerGuiHandler(NostrumFairies.instance, new NostrumFairyGui());
	}
	
	public void postinit() {
		;
	}
    
    private void registerItems() {
    	;
    }
    
    private void registerBlocks() {
    	GameRegistry.register(StorageLogisticsChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, StorageLogisticsChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(StorageLogisticsChest.instance())).setRegistryName(StorageLogisticsChest.ID));
    	StorageLogisticsChest.init();
    	
    	GameRegistry.register(BufferLogisticsChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, BufferLogisticsChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(BufferLogisticsChest.instance())).setRegistryName(BufferLogisticsChest.ID));
    	BufferLogisticsChest.init();
    	
    	GameRegistry.register(OutputLogisticsChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, OutputLogisticsChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(OutputLogisticsChest.instance())).setRegistryName(OutputLogisticsChest.ID));
    	OutputLogisticsChest.init();
    	
    	GameRegistry.register(StorageMonitor.instance(),
    			new ResourceLocation(NostrumFairies.MODID, StorageMonitor.ID));
    	GameRegistry.register(
    			(new ItemBlock(StorageMonitor.instance())).setRegistryName(StorageMonitor.ID));
    	StorageMonitor.init();
    	
    	GameRegistry.register(InputLogisticsChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, InputLogisticsChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(InputLogisticsChest.instance())).setRegistryName(InputLogisticsChest.ID));
    	InputLogisticsChest.init();
    	
    	GameRegistry.register(GatheringBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, GatheringBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(GatheringBlock.instance())).setRegistryName(GatheringBlock.ID));
    	GatheringBlock.init();
    	
    	GameRegistry.register(LogisticsPylon.instance(),
    			new ResourceLocation(NostrumFairies.MODID, LogisticsPylon.ID));
    	GameRegistry.register(
    			(new ItemBlock(LogisticsPylon.instance())).setRegistryName(LogisticsPylon.ID));
    	LogisticsPylon.init();
    	
    	GameRegistry.register(WoodcuttingBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, WoodcuttingBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(WoodcuttingBlock.instance())).setRegistryName(WoodcuttingBlock.ID));
    	WoodcuttingBlock.init();
    	
    	GameRegistry.register(MiningBlock.instance(),
    			new ResourceLocation(NostrumFairies.MODID, MiningBlock.ID));
    	GameRegistry.register(
    			(new ItemBlock(MiningBlock.instance())).setRegistryName(MiningBlock.ID));
    	MiningBlock.init();
    	
    	GameRegistry.register(MagicLight.Bright(),
    			new ResourceLocation(NostrumFairies.MODID, MagicLight.BrightID));
    	GameRegistry.register(MagicLight.Medium(),
    			new ResourceLocation(NostrumFairies.MODID, MagicLight.MediumID));
    	GameRegistry.register(MagicLight.Dim(),
    			new ResourceLocation(NostrumFairies.MODID, MagicLight.DimID));
    }

	public EntityPlayer getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public boolean isServer() {
		return true;
	}
}
