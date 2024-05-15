package com.smanzana.nostrumfairies.client.render.stesr;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * A heavily-cached version of a TESR.
 * The idea is that it's cached like regular block rendering, with the main difference being
 * that the actual rendering is a freeform 'render' call. All the rendering is compiled and done in a quick pass that isn't
 * recomputed unless an update has been signalled for the block.
 * 
 * To use properly, created a Static TESR that implements render. Then register it to the TE class in
 * the StaticTESRRenderer. Finally, issue calls to 'update' the StaticTESRRenderer any time your TESR should
 * be re-rendered.
 * @author Skyler
 *
 * @param <T>
 */
public interface StaticTESR<T extends TileEntity> {

	/**
	 * Render the tile entity renderer.
	 * Note that this is intended to be SUPER optimized. A buffer has been initialized and drawing started.
	 * This only updates when block updates happen.
	 * Also note that, while the TE position has been provided, no translation is needed and is done for you.
	 * @param tileEntity
	 * @param x
	 * @param y
	 * @param z
	 * @param state
	 * @param world
	 * @param buffer the buffer to draw into
	 * @param fakeStack an identit matrixStack that shouldn't actually be modified, for ease of use of drawing functions. You should draw in relation to
	 * a stack that's set up for your TESR and not do any stack modifications.
	 */
	public void render(T tileEntity, double x, double y, double z, BlockState state, World world, IVertexBuilder buffer, final MatrixStack fakeStack,
			int combinedLightIn, int combinedOverlayIn);
	
	/**
	 * Return the vertex format (the order the rendering data is going to be added) that this static TESR wants to use.
	 * Some good options are DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP if using RenderFunc's drawSpaceQuad, or
	 * .Item/.Block if rendering item or block models.
	 * @param tileEntity
	 * @return
	 */
	public VertexFormat getRenderFormat(T tileEntity);
	
}
