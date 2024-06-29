package com.smanzana.nostrumfairies;

import java.util.Random;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.smanzana.nostrumfairies.capabilities.AttributeProvider;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.items.FeySoulStone;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.LogisticsRegistry;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.proxy.ClientProxy;
import com.smanzana.nostrumfairies.proxy.CommonProxy;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
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
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod(NostrumFairies.MODID)
public class NostrumFairies {

	public static final String MODID = "nostrumfairies";
    public static final String VERSION = "1.16.5-1.3.0";
	
    public static NostrumFairies instance;
    public static CommonProxy proxy;
    public static Logger logger = LogManager.getLogger(MODID);
    public static ItemGroup creativeTab;
    public static Random random = new Random();
	public static LogisticsComponentRegistry logisticsComponentRegistry;
    
    private LogisticsRegistry logisticsRegistry; // use getter below
    private boolean logisticsRegistryInitRecurseGuard;
    
    public NostrumFairies() {
    	instance = this;
    	
    	proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    	logisticsRegistryInitRecurseGuard = false;
    	
    	NostrumFairies.creativeTab = new ItemGroup(MODID){
	    	@Override
	        @OnlyIn(Dist.CLIENT)
			public ItemStack createIcon() {
	    		return FeySoulStone.create(FeySoulStone.SoulStoneType.GEM);
	        }
	    };
	    
	    MinecraftForge.EVENT_BUS.register(this); // for onWorldLoad
	    logisticsComponentRegistry = new LogisticsComponentRegistry();
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
    	
    	logisticsRegistry = (LogisticsRegistry) world.getServer().getWorld(World.OVERWORLD).getSavedData().getOrCreate(
    			LogisticsRegistry::new, LogisticsRegistry.DATA_NAME);
		
//		if (logisticsRegistry == null) { // still
//			logisticsRegistry = new LogisticsRegistry();
//			world.getMapStorage().setData(LogisticsRegistry.DATA_NAME, logisticsRegistry);
//		}
		
		logisticsRegistryInitRecurseGuard = false;
    }
    
    public static @Nullable World getWorld(RegistryKey<World> dimension) {
    	PlayerEntity p = proxy.getPlayer();
    	if (p != null && p.world.isRemote()) {
    		if (DimensionUtils.InDimension(p, dimension)) {
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
    
    static boolean LogisticsLogging = false;
    
    public static void LogLogistics(@Nullable LogisticsNetwork network, @Nullable ILogisticsTask task, @Nullable IFeyWorker worker, String msg) {
    	if (LogisticsLogging) {
    		StringBuilder buffer = new StringBuilder();
    		if (network != null) {
    			buffer.append('[');
    			buffer.append(network.getUUID().toString().substring(28));
    			buffer.append(']');
    			buffer.append(' ');
    		}
    		
    		if (task != null) {
    			buffer.append("<[");
    			buffer.append(task.getTaskID());
    			buffer.append(']');
    			buffer.append(task.getDisplayName());
    			buffer.append("> ");
    		}
    		
    		if (worker != null) {
    			buffer.append('{');
    			buffer.append(worker.getLogisticsID());
    			buffer.append("} ");
    		}
    		
    		buffer.append(msg);
    		logger.debug(buffer.toString());
    	}
    }
}