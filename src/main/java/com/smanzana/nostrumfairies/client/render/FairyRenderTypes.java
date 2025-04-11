package com.smanzana.nostrumfairies.client.render;

import java.util.OptionalDouble;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class FairyRenderTypes {

	public static final RenderType LOGISTICS_LINES;
	public static final RenderType MININGBLOCK_HIGHLIGHT;
	public static final RenderType MININGBLOCK_OUTLINE;
	public static final RenderType TEMPLATE_SELECT_HIGHLIGHT;
	public static final RenderType TEMPLATE_SELECT_HIGHLIGHT_CULL;
	
	private static final String Name(String suffix) {
		return "fairyrender_" + suffix;
	}
	
	static {
		
		final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = ObfuscationReflectionHelper.getPrivateValue(RenderStateShard.class, null, "TRANSLUCENT_TRANSPARENCY");
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
			.createCompositeState(false);
		LOGISTICS_LINES = RenderType.create(Name("LogisticsLines"), DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, GL11.GL_LINES, 64, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
			.createCompositeState(false);
		MININGBLOCK_HIGHLIGHT = RenderType.create(Name("BlockHighlight"), DefaultVertexFormat.POSITION_COLOR, GL11.GL_QUADS, 64, glState);
		TEMPLATE_SELECT_HIGHLIGHT_CULL = RenderType.create(Name("TemplateHighlightCull"), DefaultVertexFormat.POSITION_COLOR, GL11.GL_QUADS, 16, glState);
		
		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setDepthTestState(NO_DEPTH)
				.setCullState(NO_CULL) // Previously only was no-cull if inside box
			.createCompositeState(false);
		TEMPLATE_SELECT_HIGHLIGHT = RenderType.create(Name("TemplateHighlight"), DefaultVertexFormat.POSITION_COLOR, GL11.GL_QUADS, 16, glState);

		glState = RenderType.CompositeState.builder()
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(NO_LIGHTING)
				.setLineState(LINE_3)
				.setDepthTestState(NO_DEPTH)
			.createCompositeState(false);
		MININGBLOCK_OUTLINE = RenderType.create(Name("BlockHighlightOutline"), DefaultVertexFormat.POSITION_COLOR, GL11.GL_LINES, 64, glState);
	}
	
}
