package com.smanzana.nostrumfairies.client.gui;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.templates.ITemplateViewerCapability;
import com.smanzana.nostrumfairies.capabilities.templates.TemplateViewerCapability;
import com.smanzana.nostrumfairies.client.render.FairyRenderTypes;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.world.blueprints.BlueprintBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends AbstractGui {

	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	protected boolean shouldDisplaySelection(PlayerEntity player) {
		for (ItemStack stack : player.getEquipmentAndArmor()) {
			if (stack.isEmpty() || !stack.getCapability(TemplateViewerCapability.CAPABILITY, null).isPresent()) {
				continue;
			}
			
			ITemplateViewerCapability cap = stack.getCapability(TemplateViewerCapability.CAPABILITY, null).orElse(null);
			if (cap.isEnabled()) {
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean shouldDisplayPreview(PlayerEntity player) {
		for (ItemStack stack : player.getHeldEquipment()) {
			if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
				continue;
			}
			
			if (TemplateWand.GetWandMode(stack) != WandMode.SPAWN) {
				continue;
			}
			
			return true;
		}
		
		return false;
	}
	
	@SubscribeEvent
	public void onRender(RenderWorldLastEvent event) {
		final Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
		final MatrixStack matrixStackIn = event.getMatrixStack();
		
		// Hook into static TESR renderer
		StaticTESRRenderer.instance.render(matrixStackIn, event.getProjectionMatrix(), mc, player, event.getPartialTicks());
		
		// Check for template viewer item and then display template selection
		if (shouldDisplaySelection(player)) {
			Pair<BlockPos, BlockPos> selection = attr.getTemplateSelection();
			BlockPos pos1 = selection.getLeft();
			BlockPos pos2 = selection.getRight();
			
			if (pos1 != null) {
				
				double minDist = player.getDistanceSq(pos1.getX(), pos1.getY(), pos1.getZ());
				if (minDist >= 5096 && pos2 != null) {
					minDist = player.getDistanceSq(pos2.getX(), pos2.getY(), pos2.getZ());
				}
				
				if (minDist < 5096) {
					
					final IRenderTypeBuffer.Impl bufferIn = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
					if (pos2 != null) {
						final IVertexBuilder buffer = bufferIn.getBuffer(FairyRenderTypes.TEMPLATE_SELECT_HIGHLIGHT);
						renderSelectionBox(
								matrixStackIn, buffer,
								new BlockPos(Math.min(pos1.getX(), pos2.getX()),
										Math.min(pos1.getY(), pos2.getY()),
										Math.min(pos1.getZ(), pos2.getZ())),
								
								new BlockPos(Math.max(pos1.getX(), pos2.getX()),
										Math.max(pos1.getY(), pos2.getY()),
										Math.max(pos1.getZ(), pos2.getZ())),
								
								event.getPartialTicks());
					}
					
					// Render pos1 anchor block special
					final IVertexBuilder buffer = bufferIn.getBuffer(FairyRenderTypes.TEMPLATE_SELECT_HIGHLIGHT_CULL);
					renderAnchorBlock(matrixStackIn, buffer, pos1, event.getPartialTicks());
					bufferIn.finish();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		final Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		final MatrixStack matrixStackIn = event.getMatrixStack();
		
		if (event.getType() == ElementType.CROSSHAIRS) {
			if (shouldDisplayPreview(player)) {
				String name = null;
				for (ItemStack held : player.getHeldEquipment()) {
					if (held.isEmpty() || !(held.getItem() instanceof TemplateWand)) {
						continue;
					}
					
					if (TemplateWand.GetWandMode(held) != WandMode.SPAWN) {
						continue;
					}
					
					ItemStack templateScroll = TemplateWand.GetSelectedTemplate(held);
					if (!templateScroll.isEmpty()) {
						name = templateScroll.getDisplayName().getString();
						break;
					}
				}
				
				if (name != null) {
					
					matrixStackIn.push();
					MainWindow res = event.getWindow();
					matrixStackIn.translate(
							((double) res.getScaledWidth() / 2),
							((double) res.getScaledHeight() / 2) + 10,
							0);
					renderCurrentIndex(matrixStackIn, name);
					matrixStackIn.pop();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onHighlight(DrawHighlightEvent.HighlightBlock event) {
		if (event.getTarget().getType() == RayTraceResult.Type.BLOCK) {
			Minecraft mc = Minecraft.getInstance();
			ClientPlayerEntity player = mc.player;
			final MatrixStack matrixStackIn = event.getMatrix();
			
			if (shouldDisplayPreview(player) && player.isSneaking()) {
				ItemStack templateScroll = ItemStack.EMPTY;
				for (ItemStack held : player.getHeldEquipment()) {
					if (held.isEmpty() || !(held.getItem() instanceof TemplateWand)) {
						continue;
					}
					
					if (TemplateWand.GetWandMode(held) != WandMode.SPAWN) {
						continue;
					}
					
					templateScroll = TemplateWand.GetSelectedTemplate(held);
					if (!templateScroll.isEmpty()) {
						TemplateBlueprint blueprint = TemplateScroll.GetTemplate(templateScroll);
						if (blueprint != cachedBlueprint) {
							cachedBlueprint = blueprint;
							cachedRenderDirty = true;
						}
						break;
					}
				}
				
				if (cachedBlueprint != null) {
					final IRenderTypeBuffer.Impl bufferIn = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
					BlockRayTraceResult blockResult = (BlockRayTraceResult) event.getTarget();
					Vector3d center = event.getTarget().getHitVec();
					BlockPos blockPos = blockResult.getPos().offset(blockResult.getFace());
					Direction face = Direction.getFacingFromVector((float) (center.x - player.getPosX()), 0f, (float) (center.z - player.getPosZ()));
					// apply original template rotation
					renderBlueprintPreview(matrixStackIn, bufferIn, blockPos, cachedBlueprint.getPreview(), face, event.getPartialTicks());
					bufferIn.finish();
				}
			}
		}
	}
	
	private TemplateBlueprint cachedBlueprint = null;
	private VertexBuffer cachedRenderList = new VertexBuffer(DefaultVertexFormats.BLOCK);
	private boolean cachedRenderDirty = true;
	
	private void renderAnchorBlock(MatrixStack matrixStackIn, IVertexBuilder buffer, BlockPos pos, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Vector3d playerPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();//player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vector3d offset = new Vector3d(pos.getX() - playerPos.x,
				pos.getY() - playerPos.y,
				pos.getZ() - playerPos.z);
		
		matrixStackIn.push();
		matrixStackIn.translate(offset.x + .5, offset.y + .5, offset.z + .5);
		matrixStackIn.scale(1.011f, 1.011f, 1.011f);
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, 0, OverlayTexture.NO_OVERLAY, .4f, .7f, 1f, .4f);
		matrixStackIn.pop();
	}
	
	private void renderSelectionBox(MatrixStack matrixStackIn, IVertexBuilder buffer, BlockPos min, BlockPos max, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		Vector3d playerPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();//player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vector3d offset = new Vector3d(min.getX() - playerPos.x,
				min.getY() - playerPos.y,
				min.getZ() - playerPos.z);
		
		matrixStackIn.push();
		
//		// Disable cull if inside
//		if (playerPos.x > min.getX() && playerPos.y > min.getY() && playerPos.z > min.getZ()
//				&& playerPos.x < max.getX() && playerPos.y < max.getY() && playerPos.z < max.getZ()) {
//			GlStateManager.disableCull();
//		}
		
		boolean good = true;
		if (!player.isCreative()) {
			// Check size
			final int size = (max.getX() - min.getX())
					* (max.getY() - min.getY())
					* (max.getZ() - min.getZ());
			
			if (size > TemplateWand.MAX_TEMPLATE_BLOCKS) {
				good = false;
			}
		}
		
		final float red, green, blue, alpha;
		if (good) { // If good
			red = .4f;
			green = .9f;
			blue = .4f;
			alpha = .3f;
		} else {
			red = .9f;
			green = .4f;
			blue = .4f;
			alpha = .3f;
		}
		
		// TODO apply partial tick offset to effects too! lol!
		
		final float widthX = (max.getX() - min.getX()) + 1;
		final float widthY = (max.getY() - min.getY()) + 1;
		final float widthZ = (max.getZ() - min.getZ()) + 1;
		matrixStackIn.translate(offset.x + (widthX / 2), offset.y + (widthY / 2), offset.z + (widthZ / 2));
		matrixStackIn.scale(widthX, widthY, widthZ);
		matrixStackIn.scale(1.0001f, 1.0001f, 1.0001f);
		RenderFuncs.drawUnitCube(matrixStackIn, buffer, 0, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
		matrixStackIn.pop();
	}
	
	private int unused; // This has moved to Magica. Remove and replce with using that.
	
	@SuppressWarnings("deprecation")
	private void renderBlueprintPreview(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, BlockPos center, BlueprintBlock[][][] preview, Direction rotation, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Vector3d playerPos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();//player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vector3d offset = new Vector3d(center.getX() - playerPos.x,
				center.getY() - playerPos.y,
				center.getZ() - playerPos.z);
		
		// Compile drawlist if not present
		if (cachedRenderDirty) {
			cachedRenderDirty = false;
			//cachedRenderList.reset(); Reset by doing new upload
			
			final int width = preview.length;
			final int height = preview[0].length;
			final int depth = preview[0][0].length;
			BufferBuilder buffer = new BufferBuilder(4096);
			
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			
			for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
			for (int z = 0; z < depth; z++) {
				final BlueprintBlock block = preview[x][y][z];
				if (block == null) {
					continue;
				}
				
				final int xOff = x - (width/2);
				final int yOff = y;
				final int zOff = z - (depth/2);
				
				BlockState state = block.getSpawnState(Direction.NORTH);
				
				if (state == null || state.getBlock() == Blocks.AIR) {
					continue;
				}
				
				IBakedModel model = null;
				if (state != null) {
					model = mc.getBlockRendererDispatcher().getModelForState(state);
				}
				
				if (model == null || model == mc.getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel()) {
					model = mc.getBlockRendererDispatcher().getModelForState(Blocks.STONE.getDefaultState());
				}
				
				final int fakeLight = 15728880;
				MatrixStack renderStack = new MatrixStack();
				renderStack.push();
				renderStack.translate(xOff, yOff, zOff);
				
				RenderFuncs.RenderModel(renderStack, buffer, model, fakeLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, .6f);
				
				renderStack.pop();
			}
			buffer.finishDrawing();
			cachedRenderList.upload(buffer); // Capture what we rendered
		}
		
		final float angle;
		final int rotX;
		final int rotZ;
		switch (rotation) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			angle = 0;
			rotX = 0;
			rotZ = 0;
			break;
		case EAST:
			angle = 270;
			rotX = 1;
			rotZ = 0;
			break;
		case SOUTH:
			angle = 180;
			rotX = 1;
			rotZ = 1;
			break;
		case WEST:
			angle = 90;
			rotX = 0;
			rotZ = 1;
			break;
		}
//		switch (rotation) {
//		case NORTH:
//		case UP:
//		case DOWN:
//		default:
//			angle = 180;
//			rotX = 1;
//			rotZ = 1;
//			break;
//		case EAST:
//			angle = 90;
//			rotX = 0;
//			rotZ = 1;
//			break;
//		case SOUTH:
//			angle = 0;
//			rotX = 0;
//			rotZ = 0;
//			break;
//		case WEST:
//			angle = 270;
//			rotX = 1;
//			rotZ = 0;
//			break;
//		}
		
		matrixStackIn.push();
		
//		GlStateManager.disableAlphaTest();
//		GlStateManager.disableBlend();
//		GlStateManager.disableTexture();
//		GlStateManager.disableLighting();
//		GlStateManager.enableAlphaTest();
//		GlStateManager.enableBlend();
//		GlStateManager.enableTexture();
//		GlStateManager.enableLighting();
//		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
//		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
		matrixStackIn.translate(offset.x + rotX, offset.y, offset.z + rotZ);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angle));
		
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		
		cachedRenderList.bindBuffer();
		DefaultVertexFormats.BLOCK.setupBufferState(0);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		cachedRenderList.draw(matrixStackIn.getLast().getMatrix(), GL11.GL_QUADS);
		RenderSystem.disableBlend();
		VertexBuffer.unbindBuffer();
        DefaultVertexFormats.BLOCK.clearBufferState();
		
		
		matrixStackIn.pop();
	}
	
	private void renderCurrentIndex(MatrixStack matrixStackIn, String name) {
		if (name == null) {
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		
		GlStateManager.disableBlend();
		matrixStackIn.push();
		
		drawCenteredString(matrixStackIn, mc.fontRenderer, name, 0, 0, 0xFFFFFFFF);
		
		matrixStackIn.pop();
		GlStateManager.enableBlend();
	}
}
