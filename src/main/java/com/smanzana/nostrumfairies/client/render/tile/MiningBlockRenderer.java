package com.smanzana.nostrumfairies.client.render.tile;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.tiles.MiningBlockTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class MiningBlockRenderer extends FeySignRenderer<MiningBlockTileEntity> {
	
	protected void renderCube(BlockPos origin, BlockPos target, float red, float green, float blue, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.pushMatrix();
		GlStateManager.translated(target.getX() - origin.getX(), target.getY() - origin.getY(), target.getZ() - origin.getZ());
		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		
		buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(0, 0, 1).color(red, green, blue, alpha).endVertex();
		buffer.pos(1, 0, 1).color(red, green, blue, alpha).endVertex();
		buffer.pos(1, 0, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
		
		tessellator.draw();
		
		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		
		buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(1, 1, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(1, 1, 1).color(red, green, blue, alpha).endVertex();
		buffer.pos(0, 1, 1).color(red, green, blue, alpha).endVertex();
		buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
		
		tessellator.draw();
		
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		
		buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
		buffer.pos(0, 0, 1).color(red, green, blue, alpha).endVertex();
			buffer.pos(0, 1, 1).color(red, green, blue, alpha).endVertex();
		buffer.pos(1, 0, 1).color(red, green, blue, alpha).endVertex();
			buffer.pos(1, 1, 1).color(red, green, blue, alpha).endVertex();
		buffer.pos(1, 0, 0).color(red, green, blue, alpha).endVertex();
			buffer.pos(1, 1, 0).color(red, green, blue, alpha).endVertex();
		
		tessellator.draw();
		GlStateManager.popMatrix();
	}
	
	@Override
	public void render(MiningBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		super.render(te, x, y, z, partialTicks, destroyStage);
		
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		
		// TODO make a capability and see if they can see logistics stuff / its turned on
		if (player != null && player.isSpectator() || player.isCreative()) { // REPLACE ME
			LogisticsNetwork network = te.getNetwork();
			if (network != null) {
				
				BlockPos origin = te.getPos();
				
				GlStateManager.pushMatrix();
				GlStateManager.translated(x, y, z);
				
				GlStateManager.lineWidth(3f);
				GlStateManager.disableLighting();
				GlStateManager.enableTexture();
				GlStateManager.disableTexture();
				GlStateManager.enableAlphaTest();
				GlStateManager.enableBlend();
				GlStateManager.disableAlphaTest();
				GlStateManager.disableBlend();
				GlStateManager.disableDepthTest();
				
				//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
				
				Set<BlockPos> locs = new HashSet<>();
				te.collectOreLocations(locs);
				for (BlockPos target : locs) {
					renderCube(origin, target, 1, 0, 0, 1);
				}
				
				locs.clear();
				te.collectRepairLocations(locs);
				for (BlockPos target : locs) {
					renderCube(origin, target, 0, 1, 0, 1);
				}
				
//				red = 0f;
//				blue = 0f;
//				green = 1f;
//				alpha = .7f;
//				
//				for (BlockPos target : te.taskMap.keySet()) {
//					
//					GlStateManager.pushMatrix();
//					GlStateManager.translate(target.getX() - origin.getX(), target.getY() - origin.getY(), target.getZ() - origin.getZ());
//					buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
//					
//					buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
//					buffer.pos(0, 0, 1).color(red, green, blue, alpha).endVertex();
//					buffer.pos(1, 0, 1).color(red, green, blue, alpha).endVertex();
//					buffer.pos(1, 0, 0).color(red, green, blue, alpha).endVertex();
//					buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
//					
//					tessellator.draw();
//					
//					buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
//					
//					buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
//					buffer.pos(1, 1, 0).color(red, green, blue, alpha).endVertex();
//					buffer.pos(1, 1, 1).color(red, green, blue, alpha).endVertex();
//					buffer.pos(0, 1, 1).color(red, green, blue, alpha).endVertex();
//					buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
//					
//					tessellator.draw();
//					
//					buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//					
//					buffer.pos(0, 0, 0).color(red, green, blue, alpha).endVertex();
//						buffer.pos(0, 1, 0).color(red, green, blue, alpha).endVertex();
//					buffer.pos(0, 0, 1).color(red, green, blue, alpha).endVertex();
//						buffer.pos(0, 1, 1).color(red, green, blue, alpha).endVertex();
//					buffer.pos(1, 0, 1).color(red, green, blue, alpha).endVertex();
//						buffer.pos(1, 1, 1).color(red, green, blue, alpha).endVertex();
//					buffer.pos(1, 0, 0).color(red, green, blue, alpha).endVertex();
//						buffer.pos(1, 1, 0).color(red, green, blue, alpha).endVertex();
//					
//					tessellator.draw();
//					GlStateManager.popMatrix();
//				}
				
				GlStateManager.enableDepthTest();
				GlStateManager.enableTexture();
				
//				GlStateManager.disableLighting();
//				GlStateManager.disableTexture2D();
//				GlStateManager.disableAlpha();
//				GlStateManager.disableBlend();
//				GlStateManager.disableDepth();
				
				GlStateManager.popMatrix();
				
//				for (ILogisticsComponent component : neighbors) {
//					BlockPos pos = component.getPosition();
//					GlStateManager.glLineWidth(2f);
//					GlStateManager.disableLighting();
//					GlStateManager.disableAlpha();
//					GlStateManager.disableBlend();
//					buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//					
//					buffer.pos(.5, 1.25, .5).color(1f, .2f, .4f, .8f).endVertex();
//					buffer.pos((pos.getX() - origin.getX()) + .5,
//							(pos.getY() - origin.getY()) + 1.25,
//							(pos.getZ() - origin.getZ()) + .5).color(1f, .2f, .4f, .8f).endVertex();
//					
//					tessellator.draw();
//				}
			}
		}
	}
}
