package com.smanzana.nostrumfairies.client.model;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

public class TemplateBlockBakedModel implements IBakedModel {

	private final TextureAtlasSprite particle;
	
	public TemplateBlockBakedModel() {
		particle = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(NostrumMagica.MODID, "blocks/mimic_facade").toString());
	}
	
	protected IBlockState getNestedState(@Nullable IBlockState state) {
		if (state != null) {
			IExtendedBlockState ex = (IExtendedBlockState) state;
			IBlockState nestedState = ex.getValue(TemplateBlock.NESTED_STATE);
			
			while (nestedState instanceof IExtendedBlockState && nestedState.getBlock() instanceof TemplateBlock) {
				nestedState = ((IExtendedBlockState)nestedState).getValue(TemplateBlock.NESTED_STATE);
			}
			
			if (nestedState != null) {
				return nestedState;
			}
		}
		
		return null;
	}
	
	protected IBakedModel getModelToRender(@Nullable IBlockState nestedState) {
		IBakedModel missing = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
		IBakedModel nestedModel = null;
		
		if (nestedState != null) {
			nestedModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(nestedState);
		}
		
		return nestedModel == null ? missing : nestedModel;
	}
	
	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, EnumFacing side, long rand) {
		return new LinkedList<>();
//		IBlockState nested = getNestedState(state);
//		return getModelToRender(nested).getQuads(nested, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return particle;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}

}
