package com.smanzana.nostrumfairies.client.render.tile;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.client.render.FairyRenderTypes;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.tiles.MiningBlockTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public class MiningBlockRenderer extends FeySignRenderer<MiningBlockTileEntity> {
	
	public MiningBlockRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	protected void renderCube(BlockPos offset, PoseStack matrixStackIn, VertexConsumer buffer, int combinedLightIn, int combinedOverlayIn,
			float red, float green, float blue, float alpha, boolean outline) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(offset.getX() + .5, offset.getY() + .5, offset.getZ() + .5);
		if (outline) {
			RenderFuncs.drawUnitCubeOutline(matrixStackIn, buffer, combinedLightIn, combinedOverlayIn, red, green, blue, alpha);
		} else {
			RenderFuncs.drawUnitCube(matrixStackIn, buffer, combinedLightIn, combinedOverlayIn, red, green, blue, alpha);
		}
		matrixStackIn.popPose();
	}
	
	@Override
	public void render(MiningBlockTileEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		super.render(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		
		// TODO make a capability and see if they can see logistics stuff / its turned on
		if (player != null && player.isSpectator() || player.isCreative()) { // REPLACE ME
			LogisticsNetwork network = te.getNetwork();
			if (network != null) {
				
				BlockPos origin = te.getBlockPos();
				List<BlockPos> oreLocs = new ArrayList<>();
				List<BlockPos> repairLocs = new ArrayList<>();
				te.collectOreLocations(oreLocs);
				te.collectRepairLocations(repairLocs);
				
				matrixStackIn.pushPose();
				
				// Getting multiple buffers (that aren't the standard ones) calls dispatch on the first and stops drawing.
				// So we have to break this up to render all of a type first, for efficiency. That means iterating the lists
				// twice but that's probably worth it to only issue two draw calls.
				
				// Render main cubes first
				final VertexConsumer cubeBuffer = bufferIn.getBuffer(FairyRenderTypes.MININGBLOCK_HIGHLIGHT);
				for (BlockPos target : oreLocs) {
					renderCube(target.subtract(origin), matrixStackIn, cubeBuffer, combinedLightIn, combinedOverlayIn, 1f, 0f, 0f, 1f, false);
				}
				
				for (BlockPos target : repairLocs) {
					renderCube(target.subtract(origin), matrixStackIn, cubeBuffer, combinedLightIn, combinedOverlayIn, 0f, 1f, 0f, 1f, false);
				}
				
				// Render outlines
				final VertexConsumer outlineBuffer = bufferIn.getBuffer(FairyRenderTypes.MININGBLOCK_OUTLINE);
				for (BlockPos target : oreLocs) {
					renderCube(target.subtract(origin), matrixStackIn, outlineBuffer, combinedLightIn, combinedOverlayIn, .3f, 0f, 0f, 1f, true);
				}
				
				for (BlockPos target : repairLocs) {
					renderCube(target.subtract(origin), matrixStackIn, outlineBuffer, combinedLightIn, combinedOverlayIn, 0f, .3f, 0f, 1f, true);
				}
				
				matrixStackIn.popPose();
			}
		}
	}
}
