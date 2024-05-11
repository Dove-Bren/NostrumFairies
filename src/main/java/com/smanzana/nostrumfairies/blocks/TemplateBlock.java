package com.smanzana.nostrumfairies.blocks;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.TemplateBlockTileEntity;
import com.smanzana.nostrummagica.blocks.MimicBlock;
import com.smanzana.nostrummagica.utils.NonNullHashMap;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TemplateBlock extends MimicBlock {
	
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
		super(Block.Properties.create(Material.PLANTS)
				.hardnessAndResistance(.2f)
				.doesNotBlockMovement()
				.noDrops()
				);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		return new TemplateBlockTileEntity();
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return createNewTileEntity(world);
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.fullCube();
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
		return true;
	}
	
	@Override
	public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		return 2;
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	protected static @Nullable TemplateBlockTileEntity GetEntity(IBlockReader world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TemplateBlockTileEntity) {
			return (TemplateBlockTileEntity) te;
		}
		return null;
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onBlockHighlight(DrawHighlightEvent.HighlightBlock event) {
		if (event.getTarget().getType() == RayTraceResult.Type.BLOCK) {
			Entity renderEnt = event.getInfo().getRenderViewEntity();
			BlockPos pos = RayTrace.blockPosFromResult(event.getTarget());
			BlockState hit = renderEnt.world.getBlockState(pos);
			if (hit != null && hit.getBlock() == this && renderEnt instanceof PlayerEntity) {
				// Get block from Tile Entity
				TemplateBlockTileEntity ent = GetEntity(renderEnt.world, pos);
				BlockState blockState = ent.getTemplateState();
				if (blockState == null) {
					blockState = Blocks.STONE.getDefaultState();
				}
				
				int unused;
				//RenderFuncs.RenderBlockOutline((PlayerEntity)renderEnt, renderEnt.world, pos, hit, event.getPartialTicks());
				
				event.setCanceled(true);
				return;
			}
		}
	}
	
	public static void SetTemplate(World world, BlockPos pos, BlockState templatedState) {
		SetTemplate(world, pos, templatedState, false);
	}
	
	public static void SetTemplate(World world, BlockPos pos, BlockState templatedState, boolean force) {
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
		
		world.setBlockState(pos, FairyBlocks.templateBlock.getDefaultState(), 3);
		if (world.captureBlockSnapshots) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), FairyBlocks.templateBlock.getDefaultState(), 3);
		}
		world.setTileEntity(pos, new TemplateBlockTileEntity(templatedState));
	}
	
	public static @Nullable BlockState GetTemplatedState(World world, BlockPos pos) {
		TemplateBlockTileEntity ent = GetEntity(world, pos);
		if (ent != null) {
			return ent.getTemplateState();
		}
		
		return null;
	}

	@Override
	public BlockState getMimickedState(BlockState mimicBlockState, World world, BlockPos myPos) {
		return null; // Shouldn't be called
	}
	
//	@Override
//	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
//		return new TemplateBlockTileEntity();
//	}
//	
//	@Override
//	public void breakBlock(World worldIn, BlockPos pos, BlockState state) {
//		super.breakBlock(worldIn, pos, state);
//	}
	
}
