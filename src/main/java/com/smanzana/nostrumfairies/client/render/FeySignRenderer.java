package com.smanzana.nostrumfairies.client.render;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.smanzana.nostrumfairies.blocks.IFeySign;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESR;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class FeySignRenderer<T extends TileEntity & IFeySign> extends TileEntitySpecialRenderer<T> implements StaticTESR<T> {

	public static <T extends TileEntity & IFeySign> void init(Class<T> clazz, FeySignRenderer<T> renderer) {
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
		new Matrix4f().rotate((float) (Math.PI * 1), new Vector3f(0, -1, 0)).scale(new Vector3f(ICON_SIZE, ICON_SIZE, .001f)), // S
		new Matrix4f().rotate((float) (Math.PI * 1.5), new Vector3f(0, -1, 0)).scale(new Vector3f(ICON_SIZE, ICON_SIZE, .001f)), // W
		new Matrix4f().rotate(0f, new Vector3f(0, -1, 0)).scale(new Vector3f(ICON_SIZE, ICON_SIZE, .001f)), // N
		new Matrix4f().rotate((float) (Math.PI * .5), new Vector3f(0, -1, 0)).scale(new Vector3f(ICON_SIZE, ICON_SIZE, .001f)), // E
	};
	
	public FeySignRenderer() {
		
	}
	
	@Override
	public void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
		Minecraft mc = Minecraft.getMinecraft();
		
		ItemStack icon = te.getSignIcon(te);
		IBakedModel model = null;
		if (icon != null) {
			model = mc.getRenderItem().getItemModelMesher().getItemModel(icon);
		}
		
		if (model == null || model == mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel()) {
			model = mc.getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState());
		}
		
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		
		final int color = 0xFFFFFFFF;
		final EnumFacing facing = te.getSignFacing(te);
		final Vector3f offset = OFFSETS[facing.getHorizontalIndex()];
		final Matrix4f transform = TRANSFORMS[facing.getHorizontalIndex()];
		
		RenderFuncs.RenderModelWithColor(model, color, buffer, offset, transform);
	}

	@Override
	public void render(T tileEntity, double x, double y, double z, IBlockState state, World world,
			VertexBuffer buffer) {
		renderTileEntityFast(tileEntity, x, y, z, 0, 0, buffer);
	}
}
