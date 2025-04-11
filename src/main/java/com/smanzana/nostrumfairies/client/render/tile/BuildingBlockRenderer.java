package com.smanzana.nostrumfairies.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrumfairies.tiles.BuildingBlockTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import com.mojang.math.Vector3f;

public class BuildingBlockRenderer extends FeySignRenderer<BuildingBlockTileEntity> {

	private static final float ICON_INNEROFFSETX2 = (1f / 16f);
	private static final float ICON_SIZE = .2f;
	private static final float THICCNESS = .035f;
	private static final float HEIGHT = .5f - .035f;
	
	private static final Vector3f SCROLL_OFFSETS[] = new Vector3f[] {
			new Vector3f(.5f + ICON_INNEROFFSETX2 + (ICON_SIZE / 2),	HEIGHT - .2f, .5f- + THICCNESS), // S
			new Vector3f(.5f - THICCNESS,					HEIGHT - .2f, .5f + ICON_INNEROFFSETX2 + (ICON_SIZE / 2)), // W
			new Vector3f(.5f - ICON_INNEROFFSETX2 - (ICON_SIZE / 2),	HEIGHT - .2f, .5f - THICCNESS), // N
			new Vector3f(.5f + THICCNESS,					HEIGHT - .2f, .5f - ICON_INNEROFFSETX2 - (ICON_SIZE / 2)), // E
	};
	
	public BuildingBlockRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void render(BuildingBlockTileEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// Use super to render sign icon
		super.render(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		// Draw template on table, if present
		ItemStack template = te.getTemplateScroll();
		if (!template.isEmpty()) {
			final Direction facing = te.getSignFacing(te);
			final Vector3f offset = SCROLL_OFFSETS[facing.get2DDataValue()];

			matrixStackIn.pushPose();
			matrixStackIn.translate(offset.x(), offset.y(), offset.z());
			matrixStackIn.scale(.5f, .5f, .5f);
			RenderFuncs.RenderWorldItem(template, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
			matrixStackIn.popPose();
		}
	}
}
