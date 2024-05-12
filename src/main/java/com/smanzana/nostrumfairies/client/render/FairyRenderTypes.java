package com.smanzana.nostrumfairies.client.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrumaetheria.client.render.TileEntityWispBlockRenderer;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class FairyRenderTypes {

	public static final RenderType LOGISTICS_LINES;
	public static final RenderType BLOCK_HIGHLIGHT;
	public static final RenderType BLOCK_OUTLINE;
	
	private static final String Name(String suffix) {
		return "fairyrender_" + suffix;
	}
	
	static {
		
		final RenderState.TransparencyState TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderState.class, null, "field_228515_g_");
		final RenderState.CullState NO_CULL = new RenderState.CullState(false);
		//final RenderState.DepthTestState DEPTH_EQUAL = new RenderState.DepthTestState("==", GL11.GL_EQUAL);
		final RenderState.DepthTestState NO_DEPTH = new RenderState.DepthTestState("none", GL11.GL_ALWAYS);
		final RenderState.LightmapState NO_LIGHTING = new RenderState.LightmapState(false);
	    final RenderState.LightmapState LIGHTMAP_ENABLED = new RenderState.LightmapState(true);
//	    final RenderState.LineState LINE_2 = new RenderState.LineState(OptionalDouble.of(2));
	    final RenderState.LineState LINE_3 = new RenderState.LineState(OptionalDouble.of(3));
	    //@SuppressWarnings("deprecation")
		//final RenderState.TextureState BLOCK_SHEET = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, false);
//	    final RenderState.AlphaState HALF_ALPHA = new RenderState.AlphaState(.5f);
//	    final RenderState.ShadeModelState SHADE_ENABLED = new RenderState.ShadeModelState(true);
//	    final RenderState.WriteMaskState NO_DEPTH_WRITE = new RenderState.WriteMaskState(true, false);
		
		// Define render types
		RenderType.State glState;
				
		glState = RenderType.State.getBuilder()
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.cull(NO_CULL)
				.lightmap(LIGHTMAP_ENABLED)
				.line(LINE_3)
			.build(false);
		LOGISTICS_LINES = RenderType.makeType(Name("LogisticsLines"), DefaultVertexFormats.POSITION_COLOR_LIGHTMAP, GL11.GL_LINES, 64, glState);
		
		glState = RenderType.State.getBuilder()
				.texture(new RenderState.TextureState(TileEntityWispBlockRenderer.GEM_TEX_LOC, false, false))
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.depthTest(NO_DEPTH)
			.build(false);
		BLOCK_HIGHLIGHT = RenderType.makeType(Name("BlockHighlight"), DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 64, glState);

		glState = RenderType.State.getBuilder()
				.transparency(TRANSLUCENT_TRANSPARENCY)
				.lightmap(NO_LIGHTING)
				.line(LINE_3)
				.depthTest(NO_DEPTH)
			.build(false);
		BLOCK_OUTLINE = RenderType.makeType(Name("BlockHighlightOutline"), DefaultVertexFormats.POSITION_COLOR_LIGHTMAP, GL11.GL_LINES, 64, glState);
	}
	
}
