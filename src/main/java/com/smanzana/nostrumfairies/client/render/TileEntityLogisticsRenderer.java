package com.smanzana.nostrumfairies.client.render;

import java.util.ArrayList;
import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrumfairies.blocks.LogisticsTileEntity;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
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
		
		// TODO make a capability and see if they can see logistics stuff / its turned on
		if (player != null) { // REPLACE ME
			LogisticsNetwork network = te.getNetwork();
			if (network != null) {
				Collection<ILogisticsComponent> neighbors = network.getConnectedComponents(te.getNetworkComponent());
				
				if (neighbors == null) {
					return;
				}
				
				neighbors = new ArrayList<>(neighbors);
				
				BlockPos origin = te.getPos();
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();
				GlStateManager.pushMatrix();
				GlStateManager.translate(x, y, z);
				
				for (ILogisticsComponent component : neighbors) {
					BlockPos pos = component.getPosition();
					GlStateManager.glLineWidth(2f);
					GlStateManager.disableLighting();
					GlStateManager.disableAlpha();
					GlStateManager.disableBlend();
					buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
					
					buffer.pos(.5, 1.25, .5).color(1f, .2f, .4f, .8f).endVertex();
					buffer.pos((pos.getX() - origin.getX()) + .5,
							(pos.getY() - origin.getY()) + 1.25,
							(pos.getZ() - origin.getZ()) + .5).color(1f, .2f, .4f, .8f).endVertex();
					
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
			}
		}
	}
	
}
