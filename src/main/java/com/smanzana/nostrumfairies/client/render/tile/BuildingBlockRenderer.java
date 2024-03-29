package com.smanzana.nostrumfairies.client.render.tile;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.tiles.BuildingBlockTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

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
	
	@Override
	protected Vector3f getOffset(BuildingBlockTileEntity te, Direction facing) {
		return ICON_OFFSETS[facing.getHorizontalIndex()];
	}
	
	@Override
	public void renderTileEntityFast(BuildingBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		// Use super to render sign icon
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partial, buffer);
		
		// Draw template on table, if present
		ItemStack template = te.getTemplateScroll();
		if (!template.isEmpty()) {
			Minecraft mc = Minecraft.getInstance();
			IBakedModel model = null;
			model = mc.getItemRenderer().getItemModelMesher().getItemModel(template);
			
			if (model == null || model == mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel()) {
				model = mc.getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState());
			}
			
			mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			
			final int color = 0xFFFFFFFF;
			final Direction facing = te.getSignFacing(te);
			final Vector3f offset = SCROLL_OFFSETS[facing.getHorizontalIndex()];
			final Matrix4f transform = new Matrix4f(getTransform(te, facing))
					//.scale(new Vector3f(.5f, .5f, .5f));
					//.rotate(90f, new Vector3f(1f, 0, 0));
					;

			GlStateManager.pushMatrix();
			GlStateManager.scalef(.5f, .5f, .5f);
			RenderFuncs.RenderModelWithColor(model, color, buffer, offset, transform);
			GlStateManager.popMatrix();
		}
	}
}
