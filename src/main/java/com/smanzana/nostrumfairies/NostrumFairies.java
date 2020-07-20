package com.smanzana.nostrumfairies;

import java.util.Random;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrumfairies.blocks.LogisticsTileEntity;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry;
import com.smanzana.nostrumfairies.logistics.LogisticsRegistry;
import com.smanzana.nostrumfairies.proxy.CommonProxy;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = NostrumFairies.MODID, version = NostrumFairies.VERSION,
	dependencies="required-after:" + NostrumMagica.MODID + "@[" + NostrumMagica.VERSION + ",)")
public class NostrumFairies {

	public static final String MODID = "nostrumfairies";
    public static final String VERSION = "1.0";
	
    public static NostrumFairies instance;
    @SidedProxy(clientSide="com.smanzana.nostrumfairies.proxy.ClientProxy", serverSide="com.smanzana.nostrumfairies.proxy.CommonProxy")
    public static CommonProxy proxy;
    public static Logger logger = LogManager.getLogger(MODID);
    public static CreativeTabs creativeTab;
    public static LogisticsComponentRegistry logisticsComponentRegistry;
    public static Random random = new Random();
    
    private LogisticsRegistry logisticsRegistry; // use getter below
    private boolean logisticsRegistryInitRecurseGuard;
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        
        registerLogisticsComponents();
    }
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
    	instance = this;
    	logisticsRegistryInitRecurseGuard = false;
    	
//	    if (Loader.isModLoaded("Baubles")) {
//	    	baubles.enable();
//	    }
    	
    	logisticsComponentRegistry = new LogisticsComponentRegistry();
    	
    	NostrumFairies.creativeTab = new CreativeTabs(MODID){
	    	@Override
	        @SideOnly(Side.CLIENT)
	        public Item getTabIconItem(){
	    		return Items.ELYTRA;
	        }
	    };
	    
    	proxy.preinit();
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    	proxy.postinit();
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    @EventHandler
    public void startup(FMLServerStartingEvent event) {
    	//event.registerServerCommand(new CommandGotoDungeon());
    }
    
    @EventHandler
    public void onServerShutdown(FMLServerStoppedEvent event) {
    	;
    }
    
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
    	if (!event.getWorld().isRemote) {
    		initLogisticsRegistry(event.getWorld());
    	}
    }
    
    public LogisticsRegistry getLogisticsRegistry() {
    	if (proxy.isServer() && this.logisticsRegistry == null) {
    		throw new RuntimeException("Accessing logistics registry before it's been loaded!");
    	} else if (this.logisticsRegistry == null) {
    		//client wants a registry, but doesn't appear to be integrated
    		this.logisticsRegistry = new LogisticsRegistry();
    	}
    	
    	return logisticsRegistry;
    }
    
    private void initLogisticsRegistry(World world) {
    	if (logisticsRegistryInitRecurseGuard) {
    		throw new RuntimeException("Recursed into logistics registry init code while initting registry");
    	}
    	logisticsRegistry = null;
    	logisticsRegistryInitRecurseGuard = true;
    	
    	logisticsRegistry = (LogisticsRegistry) world.getMapStorage().getOrLoadData(
    			LogisticsRegistry.class, LogisticsRegistry.DATA_NAME);
		
		if (logisticsRegistry == null) { // still
			logisticsRegistry = new LogisticsRegistry();
			world.getMapStorage().setData(LogisticsRegistry.DATA_NAME, logisticsRegistry);
		}
		
		logisticsRegistryInitRecurseGuard = false;
    }
    
    private void registerLogisticsComponents() {
//    	logisticsComponentRegistry.registerComponentType(StorageLogisticsChest.StorageChestTileEntity.LOGISTICS_TAG,
//    			new StorageLogisticsChest.StorageChestTileEntity.StorageChestTEFactory());
//    	logisticsComponentRegistry.registerComponentType(BufferLogisticsChest.BufferChestTileEntity.LOGISTICS_TAG,
//    			new BufferLogisticsChest.BufferChestTileEntity.BufferChestTEFactory());
//    	logisticsComponentRegistry.registerComponentType(OutputLogisticsChest.OutputChestTileEntity.LOGISTICS_TAG,
//    			new OutputLogisticsChest.OutputChestTileEntity.OutputChestTEFactory());
//    	logisticsComponentRegistry.registerComponentType(StorageMonitor.StorageMonitorTileEntity.LOGISTICS_TAG,
//    			new StorageMonitor.StorageMonitorTileEntity.StorageMonitorTEFactory());
    	logisticsComponentRegistry.registerComponentType(LogisticsTileEntity.LogisticsTileEntityComponent.LOGISTICS_TAG,
    			new LogisticsTileEntity.LogisticsTileEntityComponent.ComponentFactory());
    }
    
    public static @Nullable World getWorld(int dimension) {
		for (World world : DimensionManager.getWorlds()) {
			if (world.provider.getDimension() == dimension) {
				return world;
			}
		}
    	
    	return null;
    }
}