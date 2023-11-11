package com.smanzana.nostrumfairies.client.gui;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.templates.ITemplateViewerCapability;
import com.smanzana.nostrumfairies.capabilities.templates.TemplateViewerCapability;
import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.items.TemplateWand;
import com.smanzana.nostrumfairies.items.TemplateWand.WandMode;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrummagica.utils.RenderFuncs;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.BlueprintBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayRenderer extends AbstractGui {

	public OverlayRenderer() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Pre event) {
//		ClientPlayerEntity player = Minecraft.getInstance().player;
//		ScaledResolution scaledRes = event.getResolution();
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Post event) {
		Minecraft.getInstance().getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
		
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
		
		// Hook into static TESR renderer
		StaticTESRRenderer.instance.render(mc, player, event.getPartialTicks());
		GlStateManager.color4f(1f, 1f, 0f, 1f);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
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
					if (pos2 != null) {
						renderSelectionBox(
								new BlockPos(Math.min(pos1.getX(), pos2.getX()),
										Math.min(pos1.getY(), pos2.getY()),
										Math.min(pos1.getZ(), pos2.getZ())),
								
								new BlockPos(Math.max(pos1.getX(), pos2.getX()),
										Math.max(pos1.getY(), pos2.getY()),
										Math.max(pos1.getZ(), pos2.getZ())),
								
								event.getPartialTicks());
					}
					
					// Render pos1 anchor block special
					renderAnchorBlock(pos1, event.getPartialTicks());
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
		final Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		
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
						name = templateScroll.getDisplayName().getFormattedText();
						break;
					}
				}
				
				if (name != null) {
					
					GlStateManager.pushMatrix();
					MainWindow res = event.getWindow();
					GlStateManager.translated(
							((double) res.getScaledWidth() / 2),
							((double) res.getScaledHeight() / 2) + 10,
							0);
					renderCurrentIndex(name);
					GlStateManager.popMatrix();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget().getType() == RayTraceResult.Type.BLOCK) {
			Minecraft mc = Minecraft.getInstance();
			ClientPlayerEntity player = mc.player;
			
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
							cachedRenderList = -1;
						}
						break;
					}
				}
				
				if (cachedBlueprint != null) {
					BlockRayTraceResult blockResult = (BlockRayTraceResult) event.getTarget();
					Vec3d center = event.getTarget().getHitVec();
					BlockPos blockPos = blockResult.getPos().offset(blockResult.getFace());
					Direction face = Direction.getFacingFromVector((float) (center.x - player.posX), 0f, (float) (center.z - player.posZ));
					// apply original template rotation
					renderBlueprintPreview(blockPos, cachedBlueprint.getPreview(), face, event.getPartialTicks());
				}
			}
		}
	}
	
	private TemplateBlueprint cachedBlueprint = null;
	private int cachedRenderList = -1;
	
	private void renderAnchorBlock(BlockPos pos, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		Vec3d playerPos = player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vec3d offset = new Vec3d(pos.getX() - playerPos.x,
				pos.getY() - playerPos.y,
				pos.getZ() - playerPos.z);
		
		GlStateManager.pushMatrix();
		
		GlStateManager.disableAlphaTest();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.disableDepthTest();
		GlStateManager.color4f(.4f, .7f, 1f, .4f);
		
		GlStateManager.translated(offset.x, offset.y, offset.z);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		
		buffer.pos(0, 1, 0).endVertex();
		buffer.pos(0, 1, 1).endVertex();
		buffer.pos(1, 1, 1).endVertex();
		buffer.pos(1, 1, 0).endVertex();
		
		buffer.pos(0, 0, 0).endVertex();
		buffer.pos(1, 0, 0).endVertex();
		buffer.pos(1, 0, 1).endVertex();
		buffer.pos(0, 0, 1).endVertex();
		
		buffer.pos(0, 1, 0).endVertex();
		buffer.pos(1, 1, 0).endVertex();
		buffer.pos(1, 0, 0).endVertex();
		buffer.pos(0, 0, 0).endVertex();
		
		buffer.pos(0, 1, 1).endVertex();
		buffer.pos(0, 0, 1).endVertex();
		buffer.pos(1, 0, 1).endVertex();
		buffer.pos(1, 1, 1).endVertex();
		
		buffer.pos(0, 0, 0).endVertex();
		buffer.pos(0, 0, 1).endVertex();
		buffer.pos(0, 1, 1).endVertex();
		buffer.pos(0, 1, 0).endVertex();
		
		buffer.pos(1, 0, 0).endVertex();
		buffer.pos(1, 1, 0).endVertex();
		buffer.pos(1, 1, 1).endVertex();
		buffer.pos(1, 0, 1).endVertex();
		
		tessellator.draw();
		
		GlStateManager.popMatrix();
		GlStateManager.enableTexture();
		GlStateManager.enableDepthTest();
	}
	
	private void renderSelectionBox(BlockPos min, BlockPos max, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		Vec3d playerPos = player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vec3d offset = new Vec3d(min.getX() - playerPos.x,
				min.getY() - playerPos.y,
				min.getZ() - playerPos.z);
		
		GlStateManager.pushMatrix();
		
		GlStateManager.disableAlphaTest();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.disableDepthTest();
		
		// Disable cull if inside
		if (playerPos.x > min.getX() && playerPos.y > min.getY() && playerPos.z > min.getZ()
				&& playerPos.x < max.getX() && playerPos.y < max.getY() && playerPos.z < max.getZ()) {
			GlStateManager.disableCull();
		}
		
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
		
		if (good) { // If good
			GlStateManager.color4f(.4f, .9f, .4f, .3f);
		} else {
			GlStateManager.color4f(.9f, .4f, .4f, .3f);
		}
		
		// TODO apply partial tick offset to effects too! lol!
		
		GlStateManager.translated(offset.x, offset.y, offset.z);
		GlStateManager.scaled((max.getX() - min.getX()) + 1,
				(max.getY() - min.getY()) + 1,
				(max.getZ() - min.getZ()) + 1);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		
		buffer.pos(0, 1, 0).endVertex();
		buffer.pos(0, 1, 1).endVertex();
		buffer.pos(1, 1, 1).endVertex();
		buffer.pos(1, 1, 0).endVertex();
		
		buffer.pos(0, 0, 0).endVertex();
		buffer.pos(1, 0, 0).endVertex();
		buffer.pos(1, 0, 1).endVertex();
		buffer.pos(0, 0, 1).endVertex();
		
		buffer.pos(0, 1, 0).endVertex();
		buffer.pos(1, 1, 0).endVertex();
		buffer.pos(1, 0, 0).endVertex();
		buffer.pos(0, 0, 0).endVertex();
		
		buffer.pos(0, 1, 1).endVertex();
		buffer.pos(0, 0, 1).endVertex();
		buffer.pos(1, 0, 1).endVertex();
		buffer.pos(1, 1, 1).endVertex();
		
		buffer.pos(0, 0, 0).endVertex();
		buffer.pos(0, 0, 1).endVertex();
		buffer.pos(0, 1, 1).endVertex();
		buffer.pos(0, 1, 0).endVertex();
		
		buffer.pos(1, 0, 0).endVertex();
		buffer.pos(1, 1, 0).endVertex();
		buffer.pos(1, 1, 1).endVertex();
		buffer.pos(1, 0, 1).endVertex();
		
		tessellator.draw();
		
		GlStateManager.popMatrix();
		GlStateManager.enableTexture();
		GlStateManager.enableDepthTest();
		GlStateManager.enableCull();
	}
	
	private void renderBlueprintPreview(BlockPos center, BlueprintBlock[][][] preview, Direction rotation, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		Vec3d playerPos = player.getEyePosition(partialTicks).subtract(0, player.getEyeHeight(), 0);
		Vec3d offset = new Vec3d(center.getX() - playerPos.x,
				center.getY() - playerPos.y,
				center.getZ() - playerPos.z);
		
		// Compile drawlist if not present
		if (cachedRenderList == -1) {
			cachedRenderList = GLAllocation.generateDisplayLists(1);
			GlStateManager.newList(cachedRenderList, GL11.GL_COMPILE);
			
			mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			
			for (int x = 0; x < 5; x++)
			for (int y = 0; y < 2; y++)
			for (int z = 0; z < 5; z++) {
				final BlueprintBlock block = preview[x][y][z];
				if (block == null) {
					continue;
				}
				
				final int xOff = x - 2;
				final int yOff = y;
				final int zOff = z - 2;
				
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
				
				GlStateManager.pushMatrix();
				GlStateManager.translated(xOff, yOff, zOff);
				
				RenderFuncs.RenderModelWithColor(model, 0xFFFFFFFF);
				
				GlStateManager.popMatrix();
			}

			GlStateManager.endList();
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
		
		GlStateManager.pushMatrix();
		
		GlStateManager.disableAlphaTest();
		GlStateManager.disableBlend();
		GlStateManager.disableTexture();
		GlStateManager.disableLighting();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableBlend();
		GlStateManager.enableTexture();
		GlStateManager.enableLighting();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		
		GlStateManager.translated(offset.x + rotX, offset.y, offset.z + rotZ);
		GlStateManager.rotatef(angle, 0, 1, 0);
		
		mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		
		GlStateManager.pushTextureAttributes();
		GlStateManager.callList(cachedRenderList);
		GlStateManager.popAttributes();
		
		GlStateManager.popMatrix();
		GlStateManager.disableTexture();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepthTest();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}
	
	private void renderCurrentIndex(String name) {
		if (name == null) {
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		
		GlStateManager.disableBlend();
		GlStateManager.pushMatrix();
		
		this.drawCenteredString(mc.fontRenderer, name, 0, 0, 0xFFFFFFFF);
		
		GlStateManager.popMatrix();
		GlStateManager.enableBlend();
		GlStateManager.color4f(1f, 1f, 1f, 1f);
	}
}
