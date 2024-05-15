package com.smanzana.nostrumfairies.client.render.stesr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.utils.Location;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
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
		public VertexBuffer drawlist;
	}
	
	// Render thread exclusives
	private Map<Location, RenderTarget> cache;
	private Map<TileEntityType<? extends TileEntity>, StaticTESR<?>> renders;
	
	// Synced update collections
	private Map<Location, RenderTarget> updates; 
	private Boolean clear;
	
	private StaticTESRRenderer() {
		cache = new HashMap<>();
		renders = new HashMap<>();
		updates = new HashMap<>();
		clear = false;
	}
	
	public <T extends TileEntity> void registerRender(TileEntityType<T> entClass, Function<? super TileEntityRendererDispatcher, ? extends StaticTESR<T>> renderFactory) {
		renders.put(entClass, renderFactory.apply(TileEntityRendererDispatcher.instance));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends TileEntity> StaticTESR<T> getRender(T ent) {
		return (StaticTESR<T>) renders.get(ent.getType());
	}
	
	@SuppressWarnings("deprecation")
	public void render(MatrixStack matrixStackIn, Matrix4f projectionMatrix, Minecraft mc, ClientPlayerEntity player, float partialTicks) {
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
				// I think this auto-closes these...
				if (targ.drawlist != null) {
					targ.drawlist.close();
				}
			}
			System.out.println("Clearing " + cache.size() + " cached renders");
			cache.clear();
		}
		if (updatesCopy != null) {
			for (Location loc : updatesCopy.keySet()) {
				RenderTarget put = updatesCopy.get(loc);
				RenderTarget existing = cache.get(loc);
				final @Nullable VertexBuffer buffer;
				
				if (existing != null && existing.drawlist != null) {
					buffer = existing.drawlist;
				} else {
					buffer = null; // create a new one
				}
				
				if (put == null) {
					// Remove
					if (buffer != null) {
						buffer.close();
					}
					cache.remove(loc);
				} else {
					// Set
					StaticTESR<TileEntity> render = this.getRender(put.te);
					put.drawlist = compile(put.te, render, buffer);

					cache.put(loc, put);
				}
			}
		}
		
		// Draw cached displays
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		final ActiveRenderInfo renderInfo = mc.gameRenderer.getActiveRenderInfo();
		final Vector3d camera = renderInfo.getProjectedView();
		final ClippingHelper clippinghelper;
		// This is what WorldRenderer does. Wish it was passed in to us!
		clippinghelper = new ClippingHelper(matrixStackIn.getLast().getMatrix(), projectionMatrix);
		clippinghelper.setCameraPosition(camera.getX(), camera.getY(), camera.getZ());
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		for (RenderTarget targ : cache.values()) {
			AxisAlignedBB bb;
			// getRenderBoundingBox returns an infinite BB if block collision size is 0...
			if (targ.te.getBlockState().getCollisionShape(targ.te.getWorld(), targ.te.getPos()) == VoxelShapes.empty()) {
				bb = VoxelShapes.fullCube().getBoundingBox().offset(targ.te.getPos());
			} else {
				bb = targ.te.getRenderBoundingBox();
			}
			
			
			if (targ.te.getPos().distanceSq(camera.getX(), camera.getY(), camera.getZ(), true) < 10000
					&& clippinghelper.isBoundingBoxInFrustum(bb)) {
				drawTarget(targ, matrixStackIn, renderInfo, player, partialTicks);
			}
		}
		VertexBuffer.unbindBuffer();
        DefaultVertexFormats.BLOCK.clearBufferState();
		RenderSystem.disableBlend();
	}
	
	private <T extends TileEntity> VertexBuffer compile(T te, StaticTESR<T> render, @Nullable VertexBuffer bufferIn) {
		final int combinedLightIn = WorldRenderer.getCombinedLight(te.getWorld(), te.getPos());
		final int combinedOverlayIn = OverlayTexture.NO_OVERLAY;
		final MatrixStack fakeStack = new MatrixStack(); // identity
		
		if (bufferIn == null) {
			bufferIn = new VertexBuffer(render.getRenderFormat(te));
		}
		
		BufferBuilder builder = new BufferBuilder(4096);
		builder.begin(GL11.GL_QUADS, render.getRenderFormat(te));
		{
			BlockPos pos = te.getPos();
			World world = te.getWorld();
			BlockState state = world.getBlockState(pos);
			render.render(te, pos.getX(), pos.getY(), pos.getZ(), state, world, builder, fakeStack, combinedLightIn, combinedOverlayIn);
		}
		builder.finishDrawing();
		
		// Copy built buffers into the vertex buffer
		bufferIn.upload(builder);
		return bufferIn;
	}
	
	private void drawTarget(RenderTarget target, MatrixStack matrixStackIn, ActiveRenderInfo info, ClientPlayerEntity player, float partialTicks) {
		Vector3d playerPos = info.getProjectedView();
		BlockPos pos = target.te.getPos();
		Vector3d offset = new Vector3d(pos.getX() - playerPos.x,
				pos.getY() - playerPos.y,
				pos.getZ() - playerPos.z);
		
		matrixStackIn.push();
		matrixStackIn.translate(offset.x, offset.y, offset.z);
		target.drawlist.bindBuffer();
		DefaultVertexFormats.BLOCK.setupBufferState(0);
		target.drawlist.draw(matrixStackIn.getLast().getMatrix(), GL11.GL_QUADS);
		matrixStackIn.pop();
	}
	
	private Set<TileEntityType<?>> missingTypesSeen = new HashSet<>(); 
	public void update(World world, BlockPos pos, @Nullable TileEntity te) {
		if (te != null && this.getRender(te) == null) {
			if (missingTypesSeen.add(te.getType())) {
				NostrumFairies.logger.error("No static TEST render registered for " + te.getType());
			}
		}
		
		Location loc = new Location(world, pos.toImmutable());
		final RenderTarget targ;
		if (te == null) {
			targ = null;
		} else {
			targ = new RenderTarget();
			targ.te = te;
			targ.drawlist = null;
			
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
