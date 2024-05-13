package com.smanzana.nostrumfairies.client.render.tile;

import java.util.ArrayList;
import java.util.Collection;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrumfairies.client.render.FairyRenderTypes;
import com.smanzana.nostrumfairies.effect.FeyEffects;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.tiles.LogisticsTileEntity;
import com.smanzana.nostrummagica.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TileEntityLogisticsRenderer<T extends LogisticsTileEntity> extends TileEntityRenderer<T> {

	public TileEntityLogisticsRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	private void addVertex(MatrixStack matrixStackIn, IVertexBuilder buffer, int combinedLightIn, float red, float green, float blue, float alpha, Vector3d point, boolean repeat) {
		buffer.pos(matrixStackIn.getLast().getMatrix(), (float) point.x, (float) point.y, (float) point.z).color(red, green, blue, alpha).lightmap(combinedLightIn).endVertex();
		if (repeat) {
			this.addVertex(matrixStackIn, buffer, combinedLightIn, red, green, blue, alpha, point, false);
		}
	}
	
	protected void renderLine(MatrixStack matrixStackIn, IVertexBuilder buffer, int combinedLightIn, Vector3d offset,
			int intervals, float red, float green, float blue, float alpha) {
		final Vector3d dist = offset.scale(.25);
		final Vector3d control1 = dist.add(dist.rotateYaw((float) (Math.PI * .5)));
		final Vector3d control2 = offset.subtract(dist).subtract(dist.rotateYaw((float) (Math.PI * .5)));
		
		for (int i = 0; i <= intervals; i++) {
			float prog = (float) i / (float) intervals;
			Vector3d point = Curves.bezier(prog, Vector3d.ZERO, control1, control2, offset);
			
			// We aren't rendering a strip, so need to repeat every 'last' point.
			// We can do this simply by just adding each point twice except the first and last one.
			final boolean repeat = (i != 0 && i != intervals);
			addVertex(matrixStackIn, buffer, combinedLightIn, red, green, blue, alpha, point, repeat);
		}
	}
	
	@Override
	public void render(T te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		EffectInstance effect = player.getActivePotionEffect(FeyEffects.feyVisibility);
		
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
				
				final IVertexBuilder buffer = bufferIn.getBuffer(FairyRenderTypes.LOGISTICS_LINES);
				
				matrixStackIn.push();
				matrixStackIn.translate(.5, 1.05, .5);
				for (ILogisticsComponent component : neighbors) {
					final Vector3d offset = Vector3d.copyCentered(component.getPosition().toImmutable().subtract(te.getPos()));
					this.renderLine(matrixStackIn, buffer, 15728880, offset, intervals, 1f, 1f, 1f, alpha);
				}
				
				matrixStackIn.pop();
			}
		}
	}
	
}
