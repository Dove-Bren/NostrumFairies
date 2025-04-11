package com.smanzana.nostrumfairies.blocks;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.TemplateBlockTileEntity;
import com.smanzana.nostrummagica.util.NonNullHashMap;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TemplateBlock extends Block {
	
	/**
	 * Attempts to deduce what item is needed to create the provided blockstate.
	 * Mostly this means hoping an ItemBlock is registered.
	 * Mods that don't have ItemBlocks for their blocks but still have items can register items.
	 * @param templatedState
	 * @return
	 */
	public static final @Nonnull ItemStack GetRequiredItem(BlockState templatedState) {
		if (templatedState == null) {
			return ItemStack.EMPTY;
		}
		
		if (StateItemOverrides.containsKey(templatedState)) {
			// Note: Always returning if key is there so that things can register 'empty' to say that
			// a block can't be build from template
			return StateItemOverrides.get(templatedState);
		}
		
		ItemStack required = ItemStack.EMPTY;
		if (StateItemCache.containsKey(templatedState)) {
			StateItemCache.get(templatedState);
		}
		Block block = templatedState.getBlock();
		
//		// Try reflection
//		if (required.isEmpty()) {
//			try {
//				// getSilkTouchDrop
//				Method Block_CreateStackedBlock = ObfuscationReflectionHelper.findMethod(Block.class, "func_180643_i", ItemStack.class,
//						BlockState.class);
//				if (Block_CreateStackedBlock != null) {
//					required = (ItemStack) Block_CreateStackedBlock.invoke(block, templatedState);
//				}
//			} catch (Exception e) {
//				required = ItemStack.EMPTY;
//			}
//		}
		
		if (required.isEmpty()) {
			Item item = block.asItem();
			if (item != null) {
				return new ItemStack(item);
			}
		}
		
		if (!required.isEmpty()) {
			// Cache it!
			StateItemCache.put(templatedState, required);
		}
		
		return required;
	}
	
	private static Map<BlockState, ItemStack> StateItemOverrides = new NonNullHashMap<>();
	private static Map<BlockState, ItemStack> StateItemCache = new NonNullHashMap<>();
	
	public static final void RegisterItemForBlock(BlockState state, @Nonnull ItemStack stack) {
		Validate.notNull(stack);
		StateItemOverrides.put(state, stack.copy());
	}
	
	public static void RegisterBaseOverrides() {
		// Vanilla
//		ItemStack stack = new ItemStack(Blocks.LEAVES);
//		for (BlockState leafState : Blocks.LEAVES.getBlockState().getValidStates()) {
//			if (leafState.getBlock() instanceof BlockOldLeaf) {
//				BlockOldLeaf leaf = (BlockOldLeaf) leafState.getBlock();
//				BlockPlanks.EnumType type = leafState.getValue(BlockOldLeaf.VARIANT);
//				RegisterItemForBlock()
//			}
//			
//		}
	}
	
	public static final String ID = "template_block";
	
	public TemplateBlock() {
		super(Block.Properties.of(Material.PLANT)
				.strength(.2f)
				.noCollission()
				.noDrops()
				.noOcclusion()
				);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return new TemplateBlockTileEntity();
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
//	@Override
//	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
//		builder.add(NESTED_STATE);
//	}
	
//	@Override
//	public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
//		IExtendedBlockState ext = (IExtendedBlockState) state;
//		TemplateBlockTileEntity ent = GetEntity(world, pos);
//		if (ent != null) {
//			state = ent.getTemplateState();
//			if (state != null) {
//				ext = ext.with(NESTED_STATE, state.getBlock().getExtendedState(state, world, pos));
//			}
//		}
//		
//		return ext;
//	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
		return true;
	}
	
	@Override
	public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
		return 2;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}
	
	protected static @Nullable TemplateBlockTileEntity GetEntity(BlockGetter world, BlockPos pos) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te != null && te instanceof TemplateBlockTileEntity) {
			return (TemplateBlockTileEntity) te;
		}
		return null;
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onBlockHighlight(DrawHighlightEvent.HighlightBlock event) {
		if (event.getTarget().getType() == HitResult.Type.BLOCK) {
			Entity renderEnt = event.getInfo().getEntity();
			BlockPos pos = RayTrace.blockPosFromResult(event.getTarget());
			BlockState hit = renderEnt.level.getBlockState(pos);
			if (hit != null && hit.getBlock() == this && renderEnt instanceof Player) {
				// Get block from Tile Entity
				TemplateBlockTileEntity ent = GetEntity(renderEnt.level, pos);
				BlockState blockState = ent.getTemplateState();
				if (blockState == null) {
					blockState = Blocks.STONE.defaultBlockState();
				}
				
				int unused;
				//RenderFuncs.RenderBlockOutline((PlayerEntity)renderEnt, renderEnt.world, pos, hit, event.getPartialTicks());
				
				event.setCanceled(true);
				return;
			}
		}
	}
	
	public static void SetTemplate(Level world, BlockPos pos, BlockState templatedState) {
		SetTemplate(world, pos, templatedState, false);
	}
	
	public static void SetTemplate(Level world, BlockPos pos, BlockState templatedState, boolean force) {
		if (templatedState == null) {
			NostrumFairies.logger.warn("Attempted to set null template at " + pos);
			return;
		}
		
		if (!force) {
			// Make sure there's an item that matches this as a pseudo way of making sure it's allowed
			@Nonnull ItemStack material = GetRequiredItem(templatedState);
			if (material.isEmpty()) {
				return;
			}
		}
		
		world.setBlock(pos, FairyBlocks.templateBlock.defaultBlockState(), 3);
		if (world.captureBlockSnapshots) {
			world.sendBlockUpdated(pos, world.getBlockState(pos), FairyBlocks.templateBlock.defaultBlockState(), 3);
		}
		world.setBlockEntity(pos, new TemplateBlockTileEntity(templatedState));
	}
	
	public static @Nullable BlockState GetTemplatedState(Level world, BlockPos pos) {
		TemplateBlockTileEntity ent = GetEntity(world, pos);
		if (ent != null) {
			return ent.getTemplateState();
		}
		
		return null;
	}
}
