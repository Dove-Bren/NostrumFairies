package com.smanzana.nostrumfairies.client.render.stesr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.utils.Location;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StaticTESRRenderer {

	public static StaticTESRRenderer instance = new StaticTESRRenderer();
	
	// TODO what if I render whole chunks at a time like vanilla?
	// TODO this should really do a frustrum check of some sort
	
	private static final class RenderTarget {
		public BlockEntity te;
		public VertexBuffer drawlist;
		public VertexFormat format;
		public RenderType renderType;
	}
	
	// Render thread exclusives
	private Map<Location, RenderTarget> cache;
	private Map<BlockEntityType<? extends BlockEntity>, StaticTESR<?>> renders;
	
	// Synced update collections
	private Map<Location, RenderTarget> updates; 
	private AtomicBoolean clear;
	
	private StaticTESRRenderer() {
		cache = new HashMap<>();
		renders = new HashMap<>();
		updates = new HashMap<>();
		clear = new AtomicBoolean(false);
	}
	
	public <T extends BlockEntity> void registerRender(BlockEntityType<T> entClass, Supplier<? extends StaticTESR<T>> renderFactory) {
		renders.put(entClass, renderFactory.get());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> StaticTESR<T> getRender(T ent) {
		return (StaticTESR<T>) renders.get(ent.getType());
	}
	
	@SuppressWarnings("deprecation")
	public void render(PoseStack matrixStackIn, Matrix4f projectionMatrix, Minecraft mc, LocalPlayer player, float partialTicks) {
		final boolean shouldClear;
		final Map<Location, RenderTarget> updatesCopy;
		
		int unused;
		if (mc != null) {
			return;
		}
		
		synchronized(updates) {
			if (updates.isEmpty()) {
				updatesCopy = null;
			} else {
				updatesCopy = Maps.newHashMap(updates);
				updates.clear();
			}
		}
		
		shouldClear = clear.getAndSet(false);
		
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
					StaticTESR<BlockEntity> render = this.getRender(put.te);
					put.drawlist = compile(put.te, render, buffer);
					put.format = render.getRenderFormat(put.te);
					put.renderType = render.getRenderType(put.te);

					cache.put(loc, put);
				}
			}
		}
		
		// Draw cached displays
		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
		final Camera renderInfo = mc.gameRenderer.getMainCamera();
		final Vec3 camera = renderInfo.getPosition();
		final Frustum clippinghelper;
		// This is what WorldRenderer does. Wish it was passed in to us!
		clippinghelper = new Frustum(matrixStackIn.last().pose(), projectionMatrix);
		clippinghelper.prepare(camera.x(), camera.y(), camera.z());
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		for (RenderTarget targ : cache.values()) {
			AABB bb;
			// getRenderBoundingBox returns an infinite BB if block collision size is 0...
			if (targ.te.getBlockState().getCollisionShape(targ.te.getLevel(), targ.te.getBlockPos()) == Shapes.empty()) {
				bb = Shapes.block().bounds().move(targ.te.getBlockPos());
			} else {
				bb = targ.te.getRenderBoundingBox();
			}
			
			
			if (targ.te.getBlockPos().distToCenterSqr(camera.x(), camera.y(), camera.z()) < 10000
					&& clippinghelper.isVisible(bb)) {
				drawTarget(targ, matrixStackIn, renderInfo, player, partialTicks);
			}
		}
		VertexBuffer.unbind();
        DefaultVertexFormat.BLOCK.clearBufferState();
		RenderSystem.disableBlend();
	}
	
	private <T extends BlockEntity> VertexBuffer compile(T te, StaticTESR<T> render, @Nullable VertexBuffer bufferIn) {
		final int combinedLightIn = LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos());
		final int combinedOverlayIn = OverlayTexture.NO_OVERLAY;
		final PoseStack fakeStack = new PoseStack(); // identity
		
		if (bufferIn == null) {
			bufferIn = new VertexBuffer(); // render.getRenderFormat(te)
		}
		
		BufferBuilder builder = new BufferBuilder(4096);
		builder.begin(VertexFormat.Mode.QUADS, render.getRenderFormat(te));
		{
			BlockPos pos = te.getBlockPos();
			Level world = te.getLevel();
			BlockState state = world.getBlockState(pos);
			render.render(te, pos.getX(), pos.getY(), pos.getZ(), state, world, builder, fakeStack, combinedLightIn, combinedOverlayIn);
		}
		builder.end();
		
		// Copy built buffers into the vertex buffer
		bufferIn.upload(builder);
		return bufferIn;
	}
	
	private void drawTarget(RenderTarget target, PoseStack matrixStackIn, Camera info, LocalPlayer player, float partialTicks) {
		
		
		Vec3 playerPos = info.getPosition();
		BlockPos pos = target.te.getBlockPos();
		Vec3 offset = new Vec3(pos.getX() - playerPos.x,
				pos.getY() - playerPos.y,
				pos.getZ() - playerPos.z);
		
		final Matrix4f unknownMatrix = new Matrix4f();
		unknownMatrix.setIdentity();
		
		matrixStackIn.pushPose();
		matrixStackIn.translate(offset.x, offset.y, offset.z);
		//target.drawlist.bind();
		//target.format.setupBufferState();
		target.renderType.setupRenderState();
		
		ShaderInstance shaderinstance = RenderSystem.getShader();
		shaderinstance.apply();
		
		RenderSystem.backupProjectionMatrix();
		RenderSystem.getProjectionMatrix().multiply(matrixStackIn.last().pose());
		RenderSystem.applyModelViewMatrix();
		
		//target.drawlist.drawWithShader(matrixStackIn.last().pose(), unknownMatrix, GameRenderer.getRendertypeTranslucentMovingBlockShader());
		target.drawlist.drawChunkLayer();
		//target.drawlist.draw();
		
		RenderSystem.restoreProjectionMatrix();
		RenderSystem.applyModelViewMatrix();
		
		target.renderType.clearRenderState();
		matrixStackIn.popPose();
	}
	
	private Set<BlockEntityType<?>> missingTypesSeen = new HashSet<>(); 
	public void update(Level world, BlockPos pos, @Nullable BlockEntity te) {
		if (te != null && this.getRender(te) == null) {
			if (missingTypesSeen.add(te.getType())) {
				NostrumFairies.logger.error("No static TEST render registered for " + te.getType());
			}
		}
		
		Location loc = new Location(world, pos.immutable());
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
		clear.set(true);
	}
}
