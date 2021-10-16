package com.smanzana.nostrumfairies.client.render;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import com.smanzana.nostrumfairies.blocks.tiles.TemplateBlockTileEntity;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESR;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TemplateBlockRenderer extends TileEntitySpecialRenderer<TemplateBlockTileEntity> implements StaticTESR<TemplateBlockTileEntity> {

	protected static final Map<IBakedModel, Integer> RenderListCache = new HashMap<>();
	
	public static void init() {
//		ClientRegistry.bindTileEntitySpecialRenderer(TemplateBlockTileEntity.class,
//				new TemplateBlockRenderer());
		
		StaticTESRRenderer.instance.registerRender(TemplateBlockTileEntity.class, new TemplateBlockRenderer());
	}
	
	public TemplateBlockRenderer() {
		
	}
	
//	protected static int Cache(IBakedModel model) {
//		Integer existing = RenderListCache.get(model); 
//		if (existing != null) {
//			return existing;
//		}
//		
//		existing = GLAllocation.generateDisplayLists(1);
//		GlStateManager.glNewList(existing, GL11.GL_COMPILE);
//		
//		final int color = 0x66AAAADD;
//		RenderFuncs.RenderModelWithColor(model, color);
//		
//		GlStateManager.glEndList();
//		RenderListCache.put(model, existing);
//		System.out.println("New cache entry");
//		return existing;
//	}
	
	@Override
	public void renderTileEntityFast(TemplateBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		Minecraft mc = Minecraft.getMinecraft();
		
		IBlockState state = te.getTemplateState();
		IBakedModel model = null;
		if (state != null) {
			model = mc.getBlockRendererDispatcher().getModelForState(state);
		}
		
		if (model == null || model == mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel()) {
			model = mc.getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState());
		}
		
		//final int drawlist = Cache(model);
		
		//GlStateManager.pushMatrix();
		//GlStateManager.translate(x, y, z);
		
		//GlStateManager.color(0f, 0f, 0f, .3f);
//		GlStateManager.disableAlpha();
//		GlStateManager.disableBlend();
//		GlStateManager.disableTexture2D();
//		GlStateManager.disableLighting();
//		GlStateManager.enableAlpha();
//		GlStateManager.enableBlend();
//		GlStateManager.enableTexture2D();
//		GlStateManager.enableLighting();
//		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
//		GlStateManager.pushAttrib();
//		GlStateManager.callList(drawlist);
//		GlStateManager.popAttrib();
		
		final int color = 0x66AAAADD;
		RenderFuncs.RenderModelWithColor(model, color, buffer, new Vector3f((float) 0, (float) 0, (float) 0));
		
		//GlStateManager.popMatrix();
	}

	@Override
	public void render(TemplateBlockTileEntity tileEntity, double x, double y, double z, IBlockState state, World world,
			BufferBuilder buffer) {
		renderTileEntityFast(tileEntity, x, y, z, 0, 0, 0, buffer);
	}
	
}
