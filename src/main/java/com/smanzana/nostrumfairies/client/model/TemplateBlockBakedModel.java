package com.smanzana.nostrumfairies.client.model;

public class TemplateBlockBakedModel /*implements IBakedModel*/ {
	
	int unused; // Remove

//	private final TextureAtlasSprite particle;
//	
//	public TemplateBlockBakedModel() {
//		particle = Minecraft.getInstance().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(NostrumMagica.MODID, "blocks/mimic_facade").toString());
//	}
//	
//	protected BlockState getNestedState(@Nullable BlockState state) {
//		if (state != null) {
//			IExtendedBlockState ex = (IExtendedBlockState) state;
//			BlockState nestedState = ex.getValue(TemplateBlock.NESTED_STATE);
//			
//			while (nestedState instanceof IExtendedBlockState && nestedState.getBlock() instanceof TemplateBlock) {
//				nestedState = ((IExtendedBlockState)nestedState).getValue(TemplateBlock.NESTED_STATE);
//			}
//			
//			if (nestedState != null) {
//				return nestedState;
//			}
//		}
//		
//		return null;
//	}
//	
//	protected IBakedModel getModelToRender(@Nullable BlockState nestedState) {
//		IBakedModel missing = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
//		IBakedModel nestedModel = null;
//		
//		if (nestedState != null) {
//			nestedModel = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(nestedState);
//		}
//		
//		return nestedModel == null ? missing : nestedModel;
//	}
//	
//	@Override
//	public List<BakedQuad> getQuads(@Nullable BlockState state, Direction side, long rand) {
//		return new LinkedList<>();
////		BlockState nested = getNestedState(state);
////		return getModelToRender(nested).getQuads(nested, side, rand);
//	}
//
//	@Override
//	public boolean isAmbientOcclusion() {
//		return true;
//	}
//
//	@Override
//	public boolean isGui3d() {
//		return true;
//	}
//
//	@Override
//	public boolean isBuiltInRenderer() {
//		return false;
//	}
//
//	@Override
//	public TextureAtlasSprite getParticleTexture() {
//		return particle;
//	}
//
//	@Override
//	public ItemCameraTransforms getItemCameraTransforms() {
//		return ItemCameraTransforms.DEFAULT;
//	}
//
//	@Override
//	public ItemOverrideList getOverrides() {
//		return ItemOverrideList.NONE;
//	}

}
