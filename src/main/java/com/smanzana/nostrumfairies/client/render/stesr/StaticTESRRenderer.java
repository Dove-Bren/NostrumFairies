package com.smanzana.nostrumfairies.client.render.stesr;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.utils.Location;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
	
	public void render(Minecraft mc, ClientPlayerEntity player, float partialTicks) {
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
				int unused;//GLAllocation.deleteDisplayLists(targ.drawlist);
			}
			System.out.println("Clearing " + cache.size() + " cached renders");
			cache.clear();
		}
		if (updatesCopy != null) {
			for (Location loc : updatesCopy.keySet()) {
				RenderTarget put = updatesCopy.get(loc);
				RenderTarget existing = cache.get(loc);
				
				if (existing != null && existing != put && existing.drawlist != -1) {
					int unused;//GLAllocation.deleteDisplayLists(existing.drawlist);
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
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		for (RenderTarget targ : cache.values()) {
			drawTarget(targ, mc, player, partialTicks);
		}
	}
	
	private <T extends TileEntity> int compile(T te, StaticTESR<T> render) {
		int unused;//
//		final VertexFormat format = render.getRenderFormat(te);
//		int list = GLAllocation.generateDisplayLists(1);
//		Tessellator tess = Tessellator.getInstance();
//		BufferBuilder buffer = tess.getBuffer();
//		
//		WorldRenderer a;
//		GlStateManager.newList(list, GL11.GL_COMPILE);
//		buffer.begin(GL11.GL_QUADS, format);
//		{
//			BlockPos pos = te.getPos();
//			World world = te.getWorld();
//			BlockState state = world.getBlockState(pos);
//			render.render(te, pos.getX(), pos.getY(), pos.getZ(), state, world, buffer);
//		}
//		tess.draw();
//		GlStateManager.endList();
//		
//		return list;
		return 0;
	}
	
	private void drawTarget(RenderTarget target, Minecraft mc, ClientPlayerEntity player, float partialTicks) {
		Vector3d playerPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();//player.getEyePosition(partialTicks).subtract(0, eyeY, 0);
		BlockPos pos = target.te.getPos();
		Vector3d offset = new Vector3d(pos.getX() - playerPos.x,
				pos.getY() - playerPos.y,
				pos.getZ() - playerPos.z);
		int unused;//
//		matrixStackIn.push();
//		matrixStackIn.translate(offset.x, offset.y, offset.z);
//		GlStateManager.callList(target.drawlist);
//		matrixStackIn.pop();
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
