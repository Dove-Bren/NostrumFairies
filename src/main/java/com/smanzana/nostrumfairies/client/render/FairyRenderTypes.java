package com.smanzana.nostrumfairies.client.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class FairyRenderTypes extends RenderType {

	public FairyRenderTypes(String string, VertexFormat vertexFormat, Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
		super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
		throw new UnsupportedOperationException("Should not be instantiated");
	}

	public static final RenderType LOGISTICS_LINES;
	public static final RenderType MININGBLOCK_HIGHLIGHT;
	public static final RenderType MININGBLOCK_OUTLINE;
	public static final RenderType TEMPLATE_SELECT_HIGHLIGHT;
	public static final RenderType TEMPLATE_SELECT_HIGHLIGHT_CULL;
	
	private static final String Name(String suffix) {
		return "fairyrender_" + suffix;
	}
	
	static {
		
		final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
		//final RenderState.DepthTestState DEPTH_EQUAL = new RenderState.DepthTestState("==", GL11.GL_EQUAL);
		final RenderStateShard.DepthTestStateShard NO_DEPTH = new RenderStateShard.DepthTestStateShard("none", GL11.GL_ALWAYS);
		final RenderStateShard.LightmapStateShard NO_LIGHTING = new RenderStateShard.LightmapStateShard(false);
	    //final RenderState.LightmapState LIGHTMAP_ENABLED = new RenderState.LightmapState(true);
//	    final RenderState.LineState LINE_2 = new RenderState.LineState(OptionalDouble.of(2));
	    final RenderStateShard.LineStateShard LINE_3 = new RenderStateShard.LineStateShard(OptionalDouble.of(3));
	    //@SuppressWarnings("deprecation")
		//final RenderState.TextureState BLOCK_SHEET = new RenderState.TextureState(AtlasTexture.LOCATION_BLOCKS_TEXTURE, false, false);
//	    final RenderState.AlphaState HALF_ALPHA = new RenderState.AlphaState(.5f);
//	    final RenderState.ShadeModelState SHADE_ENABLED = new RenderState.ShadeModelState(true);
//	    final RenderState.WriteMaskState NO_DEPTH_WRITE = new RenderState.WriteMaskState(true, false);
		
		// Define render types
		RenderType.CompositeState glState;
				
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(NO_LIGHTING)
				.setLineState(LINE_3)
				.setShaderState(RENDERTYPE_LINES_SHADER)
			.createCompositeState(false);
		LOGISTICS_LINES = RenderType.create(Name("LogisticsLines"), DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 64, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
			.createCompositeState(false);
		MININGBLOCK_HIGHLIGHT = RenderType.create(Name("BlockHighlight"), DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 64, false, false, glState);
		TEMPLATE_SELECT_HIGHLIGHT_CULL = RenderType.create(Name("TemplateHighlightCull"), DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 16, false, false, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
				.setCullState(NO_CULL) // Previously only was no-cull if inside box
			.createCompositeState(false);
		TEMPLATE_SELECT_HIGHLIGHT = RenderType.create(Name("TemplateHighlight"), DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 16, false, false, glState);

		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLineState(LINE_3)
				.setDepthTestState(NO_DEPTH)
				.setShaderState(RENDERTYPE_LINES_SHADER)
			.createCompositeState(false);
		MININGBLOCK_OUTLINE = RenderType.create(Name("BlockHighlightOutline"), DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 64, false, false, glState);
		
	}
	
}
