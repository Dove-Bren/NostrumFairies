package com.smanzana.nostrumfairies.client.render.tile;

import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrumfairies.blocks.IFeySign;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESR;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.tiles.LogisticsTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class FeySignRenderer<T extends LogisticsTileEntity & IFeySign> extends TileEntityLogisticsRenderer<T> implements StaticTESR<T> {

	public static <T extends LogisticsTileEntity & IFeySign> void init(TileEntityType<T> clazz, Function<? super TileEntityRendererDispatcher, ? extends FeySignRenderer<T>> renderFactory) {
		StaticTESRRenderer.instance.registerRender(clazz, renderFactory);
	}
	
	private static final float ICON_SIZE = .2f;
	private static final float THICCNESS = .035f;
	private static final float HEIGHT = .5f - .035f;
	private static final Vector3f OFFSETS[] = new Vector3f[] {
			new Vector3f(.5f + (ICON_SIZE / 2),	HEIGHT, .5f + THICCNESS), // S
			new Vector3f(.5f - THICCNESS,		HEIGHT, .5f + (ICON_SIZE / 2)), // W
			new Vector3f(.5f - (ICON_SIZE / 2), HEIGHT, .5f - THICCNESS), // N
			new Vector3f(.5f + THICCNESS,		HEIGHT, .5f - (ICON_SIZE / 2)), // E
	};
	
	public FeySignRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	protected Vector3f getOffset(T te, Direction facing) {
		return OFFSETS[facing.getHorizontalIndex()];
	}
	
	@Override
	public VertexFormat getRenderFormat(T te) {
		return DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP;
	}
	
	@Override
	public void render(T te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		ResourceLocation signIcon = te.getSignIcon(te);
		final IVertexBuilder buffer = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(signIcon));
		
		//final int color = 0xFFFFFFFF;
		final Direction facing = te.getSignFacing(te);
		final Vector3f offset = getOffset(te, facing);
		//final Matrix4f transform = getTransform(te, facing);
		
		final float yaw = facing.getOpposite().getHorizontalAngle();
		
		int unused; // Definitely gonna need some help
		//rYZ += .5f;
		
		//f, f4, f1, f2, f3
		//float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ
		
		matrixStackIn.push();
		matrixStackIn.translate(offset.getX(), offset.getY(), offset.getZ());
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(yaw));
		RenderFuncs.renderSpaceQuad(matrixStackIn, buffer, ICON_SIZE, combinedLightIn, combinedOverlayIn, 1f, 1f, 1f, 1f);
		matrixStackIn.pop();
	}

	@Override
	public void render(T tileEntity, double x, double y, double z, BlockState state, World world,
			BufferBuilder buffer) {
		//renderTileEntityFast(tileEntity, x, y, z, 0, 0, 0, buffer);
	}
}
