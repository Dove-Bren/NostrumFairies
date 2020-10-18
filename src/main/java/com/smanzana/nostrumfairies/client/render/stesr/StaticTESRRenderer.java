package com.smanzana.nostrumfairies.client.render.stesr;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.smanzana.nostrumfairies.utils.Location;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class StaticTESRRenderer {

	public static StaticTESRRenderer instance = new StaticTESRRenderer();
	
	// TODO what if I render whole chunks at a time like vanilla?
	// TODO this should really do a frustrum check of some sort
	
	private static final class RenderTarget {
		public TileEntity te;
		public int drawlist;
	}
	
	// Render thread exclusives
	private Map<Location, RenderTarget> cache;
	private Map<Class<? extends TileEntity>, StaticTESR<?>> renders;
	
	// Synced update collections
	private Map<Location, RenderTarget> updates; 
	private Boolean clear;
	
	private StaticTESRRenderer() {
		cache = new HashMap<>();
		renders = new HashMap<>();
		updates = new HashMap<>();
		clear = false;
	}
	
	public <T extends TileEntity> void registerRender(Class<T> entClass, StaticTESR<T> render) {
		renders.put(entClass, render);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends TileEntity> StaticTESR<T> getRender(TileEntity ent) {
		return (StaticTESR<T>) renders.get(ent.getClass());
	}
	
	public void render(Minecraft mc, EntityPlayerSP player, float partialTicks) {
		final boolean shouldClear;
		final Map<Location, RenderTarget> updatesCopy;
		
		synchronized(updates) {
			if (updates.isEmpty()) {
				updatesCopy = null;
			} else {
				updatesCopy = Maps.newHashMap(updates);
				updates.clear();
			}
		}
		
		synchronized(clear) {
			shouldClear = clear;
			clear = false;
		}
		
		if (shouldClear) {
			for (RenderTarget targ : cache.values()) {
				GLAllocation.deleteDisplayLists(targ.drawlist);
			}
			System.out.println("Clearing " + cache.size() + " cached renders");
			cache.clear();
		} else if (updatesCopy != null) {
			for (Location loc : updatesCopy.keySet()) {
				RenderTarget put = updatesCopy.get(loc);
				RenderTarget existing = cache.get(loc);
				
				if (existing != null && existing != put && existing.drawlist != -1) {
					GLAllocation.deleteDisplayLists(existing.drawlist);
				}
				
				if (put == null) {
					// Remove
					cache.remove(loc);
				} else {
					// Set
					StaticTESR<TileEntity> render = this.<TileEntity>getRender(put.te);
					put.drawlist = compile(put.te, render);

					cache.put(loc, put);
				}
			}
		}
		
		// Draw cached displays
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		for (RenderTarget targ : cache.values()) {
			drawTarget(targ, mc, player, partialTicks);
		}
	}
	
	private <T extends TileEntity> int compile(T te, StaticTESR<T> render) {
		int list = GLAllocation.generateDisplayLists(1);
		Tessellator tess = Tessellator.getInstance();
		VertexBuffer buffer = tess.getBuffer();
		
		
		GlStateManager.glNewList(list, GL11.GL_COMPILE);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		{
			BlockPos pos = te.getPos();
			World world = te.getWorld();
			IBlockState state = world.getBlockState(pos);
			render.render(te, pos.getX(), pos.getY(), pos.getZ(), state, world, buffer);
		}
		tess.draw();
		GlStateManager.glEndList();
		
		return list;
	}
	
	private void drawTarget(RenderTarget target, Minecraft mc, EntityPlayerSP player, float partialTicks) {
		final double eyeY = player.getEyeHeight();
		Vec3d playerPos = player.getPositionEyes(partialTicks).subtract(0, eyeY, 0);
		BlockPos pos = target.te.getPos();
		Vec3d offset = new Vec3d(pos.getX() - playerPos.xCoord,
				pos.getY() - playerPos.yCoord,
				pos.getZ() - playerPos.zCoord);
		
		//GlStateManager.enableLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
		GlStateManager.translate(offset.xCoord, offset.yCoord, offset.zCoord);
		GlStateManager.callList(target.drawlist);
		GlStateManager.popMatrix();
	}
	
	public void update(World world, BlockPos pos, @Nullable TileEntity te) {
		Location loc = new Location(world, pos.toImmutable());
		final RenderTarget targ;
		if (te == null) {
			targ = null;
		} else {
			targ = new RenderTarget();
			targ.te = te;
			targ.drawlist = -1;
			
		}
		
		// Put null to delete
		synchronized(updates) {
			updates.put(loc, targ);
		}
	}

	public void clear() {
		synchronized(clear) {
			clear = true;
		}
	}
}
