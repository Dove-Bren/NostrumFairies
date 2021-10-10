package com.smanzana.nostrumfairies.client.render;

import java.util.ArrayList;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrumfairies.blocks.tiles.LogisticsTileEntity;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.potion.FeyVisibilityPotion;
import com.smanzana.nostrummagica.utils.Curves;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class TileEntityLogisticsRenderer<T extends LogisticsTileEntity> extends TileEntitySpecialRenderer<T> {

	public TileEntityLogisticsRenderer() {
		
	}
	
	@Override
	public void renderTileEntityAt(T te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage);
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		PotionEffect effect = player.getActivePotionEffect(FeyVisibilityPotion.instance());
		
		if (player != null && (player.isSpectator() || player.isCreative() || effect != null)) { // REPLACE ME
			LogisticsNetwork network = te.getNetwork();
			if (network != null) {
				Collection<ILogisticsComponent> neighbors = network.getConnectedComponents(te.getNetworkComponent());
				
				if (neighbors == null) {
					return;
				}
				
				neighbors = new ArrayList<>(neighbors);
				
				final int intervals = 60;
				final float alpha;
				if (effect == null) {
					alpha = 1f;
				} else {
					final int duration = effect.getDuration();
					if (duration < 20 * 5) {
						alpha = (float) duration / (float) (20 * 5);
					} else {
						alpha = 1f;
					}
				}
				
				Vec3d origin = new Vec3d(BlockPos.ORIGIN);
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();
				GlStateManager.pushMatrix();
				GlStateManager.translate(x + .5, y + 1.05, z + .5);
				//GlStateManager.disableColorMaterial();
				GlStateManager.enableTexture2D();
				GlStateManager.disableTexture2D();
				GlStateManager.enableLighting();
				GlStateManager.disableLighting();
				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.disableBlend();
				GlStateManager.disableAlpha();
				GlStateManager.enableBlend();
				GlStateManager.enableAlpha();
				GlStateManager.glLineWidth(3f);
				GlStateManager.enableDepth();
				GlStateManager.disableRescaleNormal();
				GL11.glDisable(GL11.GL_LINE_STIPPLE);
				GL11.glLineStipple(1, (short) 1);
				GlStateManager.color(1f, 1f, 1f, .9f);
				GlStateManager.color(1f, 1f, 1f, alpha);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
				
				for (ILogisticsComponent component : neighbors) {
					final Vec3d offset = new Vec3d(component.getPosition().toImmutable().subtract(te.getPos()));
					final Vec3d dist = offset.scale(.25);
					final Vec3d control1 = dist.add(dist.rotateYaw((float) (Math.PI * .5)));
					final Vec3d control2 = offset.subtract(dist).subtract(dist.rotateYaw((float) (Math.PI * .5)));
					buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
					for (int i = 0; i <= intervals; i++) {
						
						
//						buffer.pos(.5, 1.25, .5).color(1f, .2f, .4f, .8f).endVertex();
//						buffer.pos((pos.getX() - origin.getX()) + .5,
//								(pos.getY() - origin.getY()) + 1.25,
//								(pos.getZ() - origin.getZ()) + .5).color(1f, .2f, .4f, .8f).endVertex();
						
						float prog = (float) i / (float) intervals;
						Vec3d point = Curves.bezier(prog, origin, control1, control2, offset);
						
//						float dotAmt = Math.max(0f, 1f - (perI * Math.abs(dotI - (float) i)));
//						if (dotAmt == 0f) {
//							float pretendI = (prog > .5f ? i - intervals : i + intervals);
//							dotAmt = Math.max(0f, 1f - (perI * Math.abs(dotI - (float) pretendI)));
//						}
						
						buffer.pos(point.x, point.y, point.z)
								.color(0f, 1f, 0f, alpha).endVertex();
						
					}
					tessellator.draw();
				}
				
//				GlStateManager.rotate(rot, 0, 1f, 0);
//				
//				GlStateManager.scale(scale, scale, scale);
//				GlStateManager.enableBlend();
//				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//				GlStateManager.disableLighting();
//				GlStateManager.enableAlpha();
//				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
//				
//				Minecraft.getMinecraft().getRenderItem()
//					.renderItem(item, TransformType.GROUND);
				
				GlStateManager.popMatrix();
				GlStateManager.enableColorMaterial();
				GlStateManager.enableTexture2D();
			}
		}
	}
	
}
