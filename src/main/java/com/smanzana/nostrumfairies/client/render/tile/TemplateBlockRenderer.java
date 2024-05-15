package com.smanzana.nostrumfairies.client.render.tile;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESR;
import com.smanzana.nostrumfairies.tiles.TemplateBlockTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TemplateBlockRenderer extends TileEntityRenderer<TemplateBlockTileEntity> implements StaticTESR<TemplateBlockTileEntity> {

	protected static final Map<IBakedModel, Integer> RenderListCache = new HashMap<>();
	
	public TemplateBlockRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public VertexFormat getRenderFormat(TemplateBlockTileEntity te) {
		return DefaultVertexFormats.BLOCK;
	}
	
	public void render(TemplateBlockTileEntity te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		; // Nothing to render every frame; uses static batched renderer
	}

	@Override
	public void render(TemplateBlockTileEntity te, double x, double y, double z, BlockState stateIn, World world,
			IVertexBuilder buffer, final MatrixStack fakeStack, int combinedLightIn, int combinedOverlayIn) {
		final Minecraft mc = Minecraft.getInstance();
		final BlockState state = te.getTemplateState();
		
		// Need to force a different RenderType, so get model and render manually
		//RenderFuncs.RenderBlockState(state, stack, bufferIn, combinedLightIn, combinedOverlayIn);
		
		IBakedModel model = null;
		if (state != null) {
			model = mc.getBlockRendererDispatcher().getModelForState(state);
		}
		
		if (model == null || model == mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel()) {
			model = mc.getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState());
		}
		
		//MatrixStack stack, IVertexBuilder buffer, IBakedModel model, int combinedLight, int combinedOverlay, float red, float green, float blue, float alpha
		final float red = .6f;
		final float green = .6f;
		final float blue = .9f;
		final float alpha = .3f;
		RenderFuncs.RenderModel(fakeStack, buffer, model, combinedLightIn, combinedOverlayIn, red, green, blue, alpha);
	}
	
}
