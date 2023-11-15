package com.smanzana.nostrumfairies.client.render.tile;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrumfairies.blocks.IFeySign;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESR;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.tiles.LogisticsTileEntity;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class FeySignRenderer<T extends LogisticsTileEntity & IFeySign> extends TileEntityLogisticsRenderer<T> implements StaticTESR<T> {

	public static <T extends LogisticsTileEntity & IFeySign> void init(Class<T> clazz, FeySignRenderer<T> renderer) {
		StaticTESRRenderer.instance.registerRender(clazz, renderer);
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
	private static final Matrix4f TRANSFORMS[] = new Matrix4f[] {
		new Matrix4f(),
		new Matrix4f(),
		new Matrix4f(),
		new Matrix4f(),
		//new Matrix4f().rotate((float) (Math.PI * 1), new Vector3f(0, -1, 0)).scale(new Vector3f(ICON_SIZE, ICON_SIZE, .001f)), // S
		//new Matrix4f().rotate((float) (Math.PI * 1.5), new Vector3f(0, -1, 0)).scale(new Vector3f(ICON_SIZE, ICON_SIZE, .001f)), // W
		//new Matrix4f().rotate(0f, new Vector3f(0, -1, 0)).scale(new Vector3f(ICON_SIZE, ICON_SIZE, .001f)), // N
		//new Matrix4f().rotate((float) (Math.PI * .5), new Vector3f(0, -1, 0)).scale(new Vector3f(ICON_SIZE, ICON_SIZE, .001f)), // E
	};
	
	public FeySignRenderer() {
		
	}
	
	protected Vector3f getOffset(T te, Direction facing) {
		return OFFSETS[facing.getHorizontalIndex()];
	}
	
	protected Matrix4f getTransform(T te, Direction facing) {
		return TRANSFORMS[facing.getHorizontalIndex()];
	}
	
	@Override
	public VertexFormat getRenderFormat(T te) {
		return DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;
	}
	
	//@Override
	public void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, float partial, BufferBuilder buffer) {
		Minecraft mc = Minecraft.getInstance();
		
		ResourceLocation signIcon = te.getSignIcon(te);
		mc.getTextureManager().bindTexture(signIcon);
		
		//final int color = 0xFFFFFFFF;
		final Direction facing = te.getSignFacing(te);
		final Vector3f offset = getOffset(te, facing);
		//final Matrix4f transform = getTransform(te, facing);
		
		final float yaw = facing.getOpposite().getHorizontalAngle();
		final float pitch = 0;
		float rX = MathHelper.cos(yaw * ((float)Math.PI / 180F));
		float rYZ = MathHelper.sin(yaw * ((float)Math.PI / 180F));
		float rXY = -rYZ * MathHelper.sin(pitch * ((float)Math.PI / 180F));
		float rXZ = rX * MathHelper.sin(pitch * ((float)Math.PI / 180F));
		float rZ = MathHelper.cos(pitch * ((float)Math.PI / 180F));
		
		rYZ += .5f;
		
		//f, f4, f1, f2, f3
		//float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ
		
		GlStateManager.pushMatrix();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		RenderFuncs.renderSpaceQuad(buffer, offset.x, offset.y, offset.z,
				rX, rXZ, rZ, rYZ, rXY,
				ICON_SIZE, 1f, 1f, 1f, 1f);
		GlStateManager.popMatrix();
		
//		IBakedModel model = null;
//		if (!icon.isEmpty()) {
//			model = mc.getItemRenderer().getItemModelMesher().getItemModel(icon);
//		}
//		
//		if (model == null || model == mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel()) {
//			model = mc.getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState());
//		}
		
//		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//		
//		final int color = 0xFFFFFFFF;
//		final Direction facing = te.getSignFacing(te);
//		final Vector3f offset = getOffset(te, facing);
//		final Matrix4f transform = getTransform(te, facing);
//		
//		GlStateManager.pushMatrix();
//		GlStateManager.scalef(ICON_SIZE / 2, ICON_SIZE, .001f);
//		RenderFuncs.ItemRenderer(icon);
		//RenderFuncs.RenderModelWithColor(model, color, buffer, offset, transform);
//		GlStateManager.popMatrix();
	}

	@Override
	public void render(T tileEntity, double x, double y, double z, BlockState state, World world,
			BufferBuilder buffer) {
		//renderTileEntityFast(tileEntity, x, y, z, 0, 0, 0, buffer);
		int unused; // TODO: Remove this whole thing!
	}
}
