package com.smanzana.nostrumfairies;

import java.util.Random;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrumfairies.capabilities.AttributeProvider;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.items.FeySoulStone.SoulStoneType;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry;
import com.smanzana.nostrumfairies.logistics.LogisticsRegistry;
import com.smanzana.nostrumfairies.proxy.ClientProxy;
import com.smanzana.nostrumfairies.proxy.CommonProxy;
import com.smanzana.nostrumfairies.tiles.LogisticsTileEntity;
import com.smanzana.nostrummagica.research.NostrumResearch.NostrumResearchTab;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod(NostrumFairies.MODID)
public class NostrumFairies {

	public static final String MODID = "nostrumfairies";
    public static final String VERSION = "1.14.4-1.2.0";
	
    public static NostrumFairies instance;
    public static CommonProxy proxy;
    public static Logger logger = LogManager.getLogger(MODID);
    public static ItemGroup creativeTab;
    public static LogisticsComponentRegistry logisticsComponentRegistry;
    public static Random random = new Random();
    public static NostrumResearchTab researchTab;
    
    private LogisticsRegistry logisticsRegistry; // use getter below
    private boolean logisticsRegistryInitRecurseGuard;
    
    public NostrumFairies() {
    	instance = this;
    	
    	proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    	logisticsRegistryInitRecurseGuard = false;
    	
    	logisticsComponentRegistry = new LogisticsComponentRegistry();
    	
    	NostrumFairies.creativeTab = new ItemGroup(MODID){
	    	@Override
	        @OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
	    		return FeySoulStone.create(FeySoulStone.SoulStoneType.GEM);
	        }
	    };
	    
	    FMLJavaModLoadingContext.get().getModEventBus().register(this);
	    
    	proxy.preinit();
    }
    
    @SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent event) {
    	researchTab = new NostrumResearchTab("fey", FeySoulStone.create(SoulStoneType.GEM));
    	
    	proxy.init();
        registerLogisticsComponents();
        
        proxy.postinit();
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    
    @SubscribeEvent
    public void startup(FMLServerStartingEvent event) {
    	//event.registerServerCommand(new CommandGotoDungeon());
    }
    
    @SubscribeEvent
    public void onServerShutdown(FMLServerStoppedEvent event) {
    	;
    }
    
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
    	if (!event.getWorld().isRemote()) {
    		initLogisticsRegistry((ServerWorld) event.getWorld());
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
    
    private void initLogisticsRegistry(ServerWorld world) {
    	if (logisticsRegistryInitRecurseGuard) {
    		throw new RuntimeException("Recursed into logistics registry init code while initting registry");
    	}
    	logisticsRegistry = null;
    	logisticsRegistryInitRecurseGuard = true;
    	
    	logisticsRegistry = (LogisticsRegistry) world.getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(
    			LogisticsRegistry::new, LogisticsRegistry.DATA_NAME);
		
//		if (logisticsRegistry == null) { // still
//			logisticsRegistry = new LogisticsRegistry();
//			world.getMapStorage().setData(LogisticsRegistry.DATA_NAME, logisticsRegistry);
//		}
		
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
    
    public static @Nullable World getWorld(RegistryKey<World> dimension) {
    	PlayerEntity p = proxy.getPlayer();
    	if (p != null && p.world.isRemote()) {
    		if (p.world.getDimension().getType() == dimension) {
				return p.world;
			}
    		return null;
    	}
    	
    	return ServerLifecycleHooks.getCurrentServer().getWorld(dimension);
    }
    
    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent e) {
    	if (e.isCanceled()) {
    		return;
    	}
    	
    	if (e.getEntity() instanceof MonsterEntity) {
    		MonsterEntity mob = (MonsterEntity) e.getEntity();
    		if (e.getEntity() instanceof MonsterEntity) {
	    		mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<EntityFeyBase>(mob, EntityFeyBase.class, true));
    		}
    	}
    }
    
    public static @Nullable INostrumFeyCapability getFeyWrapper(Entity e) {
    	if (e == null)
    		return null;
    	
    	return e.getCapability(AttributeProvider.CAPABILITY, null).orElse(null);
    }
    
//    private static int potionID = 85;
//	
//    public static int registerPotion(Potion potion, ResourceLocation loc) {
//    	while (Potion.getPotionById(potionID) != null)
//    		potionID++;
//    	Potion.REGISTRY.register(potionID, loc, potion);
//    	return potionID;
//    }
}