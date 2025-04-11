package com.smanzana.nostrumfairies.client.render.tile;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESR;
import com.smanzana.nostrumfairies.tiles.TemplateBlockTileEntity;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TemplateBlockRenderer extends BlockEntityRenderer<TemplateBlockTileEntity> implements StaticTESR<TemplateBlockTileEntity> {

	protected static final Map<BakedModel, Integer> RenderListCache = new HashMap<>();
	
	public TemplateBlockRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public VertexFormat getRenderFormat(TemplateBlockTileEntity te) {
		return DefaultVertexFormat.BLOCK;
	}
	
	public void render(TemplateBlockTileEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		; // Nothing to render every frame; uses static batched renderer
	}

	@Override
	public void render(TemplateBlockTileEntity te, double x, double y, double z, BlockState stateIn, Level world,
			VertexConsumer buffer, final PoseStack fakeStack, int combinedLightIn, int combinedOverlayIn) {
		final Minecraft mc = Minecraft.getInstance();
		final BlockState state = te.getTemplateState();
		
		// Need to force a different RenderType, so get model and render manually
		//RenderFuncs.RenderBlockState(state, stack, bufferIn, combinedLightIn, combinedOverlayIn);
		
		BakedModel model = null;
		if (state != null) {
			model = mc.getBlockRenderer().getBlockModel(state);
		}
		
		if (model == null || model == mc.getBlockRenderer().getBlockModelShaper().getModelManager().getMissingModel()) {
			model = mc.getBlockRenderer().getBlockModel(Blocks.STONE.defaultBlockState());
		}
		
		//MatrixStack stack, IVertexBuilder buffer, IBakedModel model, int combinedLight, int combinedOverlay, float red, float green, float blue, float alpha
		final float red = .6f;
		final float green = .6f;
		final float blue = .9f;
		final float alpha = .3f;
		RenderFuncs.RenderModel(fakeStack, buffer, model, combinedLightIn, combinedOverlayIn, red, green, blue, alpha);
	}
	
}
