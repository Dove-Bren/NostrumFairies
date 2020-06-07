package com.smanzana.nostrumfairies.proxy;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.TestChest;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.network.NetworkHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
	
	public void preinit() {
		NetworkHandler.getInstance();
		
    	
//    	int entityID = 0;
//    	EntityRegistry.registerModEntity(EntitySpellProjectile.class, "spell_projectile",
//    			entityID++,
//    			NostrumFairies.instance,
//    			64,
//    			1,
//    			true
//    			);

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
    	GameRegistry.register(TestChest.instance(),
    			new ResourceLocation(NostrumFairies.MODID, TestChest.ID));
    	GameRegistry.register(
    			(new ItemBlock(TestChest.instance())).setRegistryName(TestChest.ID));
    	TestChest.init();
    	
    }

	public EntityPlayer getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public boolean isServer() {
		return true;
	}
}
