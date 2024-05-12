package com.smanzana.nostrumfairies.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrumfairies.tiles.BuildingBlockTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class BuildingBlockRenderer extends FeySignRenderer<BuildingBlockTileEntity> {

	private static final float ICON_INNEROFFSETX = (2f / 16f);
	private static final float ICON_INNEROFFSETX2 = (1f / 16f);
	private static final float ICON_SIZE = .2f;
	private static final float THICCNESS = .035f;
	private static final float HEIGHT = .5f - .035f;
	private static final Vector3f ICON_OFFSETS[] = new Vector3f[] {
			new Vector3f(.5f - ICON_INNEROFFSETX + (ICON_SIZE / 2),	HEIGHT, .5f + THICCNESS), // S
			new Vector3f(.5f - THICCNESS,					HEIGHT, .5f - ICON_INNEROFFSETX + (ICON_SIZE / 2)), // W
			new Vector3f(.5f + ICON_INNEROFFSETX - (ICON_SIZE / 2),	HEIGHT, .5f - THICCNESS), // N
			new Vector3f(.5f + THICCNESS,					HEIGHT, .5f + ICON_INNEROFFSETX - (ICON_SIZE / 2)), // E
	};
	
	private static final Vector3f SCROLL_OFFSETS[] = new Vector3f[] {
			new Vector3f(.5f + ICON_INNEROFFSETX2 + (ICON_SIZE / 2),	HEIGHT - .2f, .5f- + THICCNESS), // S
			new Vector3f(.5f - THICCNESS,					HEIGHT - .2f, .5f + ICON_INNEROFFSETX2 + (ICON_SIZE / 2)), // W
			new Vector3f(.5f - ICON_INNEROFFSETX2 - (ICON_SIZE / 2),	HEIGHT - .2f, .5f - THICCNESS), // N
			new Vector3f(.5f + THICCNESS,					HEIGHT - .2f, .5f - ICON_INNEROFFSETX2 - (ICON_SIZE / 2)), // E
	};
	
	public BuildingBlockRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Vector3f getOffset(BuildingBlockTileEntity te, Direction facing) {
		return ICON_OFFSETS[facing.getHorizontalIndex()];
	}
	
	@Override
	public void render(BuildingBlockTileEntity te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// Use super to render sign icon
		super.render(te, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
		
		// Draw template on table, if present
		ItemStack template = te.getTemplateScroll();
		if (!template.isEmpty()) {
			final Direction facing = te.getSignFacing(te);
			final Vector3f offset = SCROLL_OFFSETS[facing.getHorizontalIndex()];

			matrixStackIn.push();
			matrixStackIn.scale(.5f, .5f, .5f);
			matrixStackIn.translate(offset.getX(), offset.getY(), offset.getZ());
			RenderFuncs.ItemRenderer(template, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
			matrixStackIn.pop();
		}
	}
}
