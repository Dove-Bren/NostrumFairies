package com.smanzana.nostrumfairies.client.render.tile;

import java.util.ArrayList;
import java.util.Collection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.smanzana.nostrumfairies.client.render.FairyRenderTypes;
import com.smanzana.nostrumfairies.effect.FeyEffects;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.tiles.LogisticsTileEntity;
import com.smanzana.nostrummagica.client.render.tile.BlockEntityRendererBase;
import com.smanzana.nostrummagica.util.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TileEntityLogisticsRenderer<T extends LogisticsTileEntity> extends BlockEntityRendererBase<T> {

	public TileEntityLogisticsRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	private void addVertex(PoseStack matrixStackIn, VertexConsumer buffer, int combinedLightIn, float red, float green, float blue, float alpha, Vec3 point, Vec3 next, boolean repeat) {
		// calculate normal
		final Vec3 diff = next.subtract(point);
		final double dist = diff.length();
		final float nx = (float) (diff.x / dist);
		final float ny = (float) (diff.y / dist);
		final float nz = (float) (diff.z / dist);
		final Matrix3f normal = matrixStackIn.last().normal();
		
		buffer.vertex(matrixStackIn.last().pose(), (float) point.x, (float) point.y, (float) point.z).color(red, green, blue, alpha).normal(normal, nx, ny, nz).endVertex();
		if (repeat) {
			this.addVertex(matrixStackIn, buffer, combinedLightIn, red, green, blue, alpha, point, next, false);
		}
	}
	
	protected void renderLine(PoseStack matrixStackIn, VertexConsumer buffer, int combinedLightIn, Vec3 offset,
			int intervals, float red, float green, float blue, float alpha) {
		final Vec3 dist = offset.scale(.25);
		final Vec3 control1 = dist.add(dist.yRot((float) (Math.PI * .5)));
		final Vec3 control2 = offset.subtract(dist).subtract(dist.yRot((float) (Math.PI * .5)));
		
		for (int i = 0; i <= intervals; i++) {
			float prog = (float) i / (float) intervals;
			Vec3 point = Curves.bezier(prog, Vec3.ZERO, control1, control2, offset);
			
			float progNext = (float) (i+1) / (float) intervals;
			Vec3 pointNext = Curves.bezier(progNext, Vec3.ZERO, control1, control2, offset);
			
			// We aren't rendering a strip, so need to repeat every 'last' point.
			// We can do this simply by just adding each point twice except the first and last one.
			final boolean repeat = (i != 0 && i != intervals);
			addVertex(matrixStackIn, buffer, combinedLightIn, red, green, blue, alpha, point, pointNext, repeat);
		}
	}
	
	@Override
	public void render(T te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		MobEffectInstance effect = player.getEffect(FeyEffects.feyVisibility);
		
		if (player != null && effect != null) { // REPLACE ME
			LogisticsNetwork network = te.getNetwork();
			if (network != null) {
				Collection<ILogisticsComponent> neighbors = network.getConnectedComponents(te.getNetworkComponent());
				
				if (neighbors == null) {
					return;
				}
				
				neighbors = new ArrayList<>(neighbors);
				
				final int intervals = 60;
				final float alpha;
				final int duration = effect.getDuration();
				if (duration < 20 * 5) {
					alpha = (float) duration / (float) (20 * 5);
				} else {
					alpha = 1f;
				}
				
				final VertexConsumer buffer = bufferIn.getBuffer(FairyRenderTypes.LOGISTICS_LINES);
				
				matrixStackIn.pushPose();
				matrixStackIn.translate(.5, 1.05, .5);
				for (ILogisticsComponent component : neighbors) {
					final Vec3 offset = Vec3.atLowerCornerOf(component.getPosition().immutable().subtract(te.getBlockPos()));
					this.renderLine(matrixStackIn, buffer, 15728880, offset, intervals, 1f, 1f, 1f, alpha);
				}
				
				matrixStackIn.popPose();
			}
		}
	}
	
}
