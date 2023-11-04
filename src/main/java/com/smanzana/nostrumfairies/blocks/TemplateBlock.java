package com.smanzana.nostrumfairies.blocks;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.TemplateBlockTileEntity;
import com.smanzana.nostrummagica.utils.NonNullHashMap;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;

public class TemplateBlock extends FeyContainerBlock {
	
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
		
		// Try reflection
		if (required.isEmpty()) {
			try {
				// getSilkTouchDrop
				Method Block_CreateStackedBlock = ObfuscationReflectionHelper.findMethod(Block.class, "func_180643_i", ItemStack.class,
						BlockState.class);
				if (Block_CreateStackedBlock != null) {
					required = (ItemStack) Block_CreateStackedBlock.invoke(block, templatedState);
				}
			} catch (Exception e) {
				required = ItemStack.EMPTY;
			}
		}
		
		if (required.isEmpty()) {
			Item item = Item.getItemFromBlock(block);
			if (item != null) {
				if (!item.getHasSubtypes()) {
					return new ItemStack(item);
				} else if (item instanceof ItemBlock) {
					// Will not work all the time, but oh well. Better than nothing!
					return new ItemStack(block, 1, block.getMetaFromState(templatedState));
				} else {
					required = new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE);
				}
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
	
	public static IUnlistedProperty<BlockState> NESTED_STATE = new IUnlistedProperty<BlockState>() {

		@Override
		public String getName() {
			return "Template::NestedState";
		}

		@Override
		public boolean isValid(BlockState value) {
			return value != null;
		}

		@Override
		public Class<BlockState> getType() {
			return BlockState.class;
		}

		@Override
		public String valueToString(BlockState value) {
			return value.toString();
		}
		
	};
	
	public static final String ID = "template_block";
	
	public TemplateBlock() {
		super(Block.Properties.create(Material.PLANTS)
				.hardnessAndResistance(.2f)
				.doesNotBlockMovement()
				);
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(NESTED_STATE);
	}
	
	@Override
	public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
		IExtendedBlockState ext = (IExtendedBlockState) state;
		TemplateBlockTileEntity ent = GetEntity(world, pos);
		if (ent != null) {
			state = ent.getTemplateState();
			if (state != null) {
				ext = ext.with(NESTED_STATE, state.getBlock().getExtendedState(state, world, pos));
			}
		}
		
		return ext;
	}
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return false;
	}
	
	public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return Block.NULL_AABB;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return Block.FULL_BLOCK_AABB;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public boolean doesSideBlockRendering(BlockState state, IBlockAccess world, BlockPos pos, Direction face) {
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.SOLID;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	protected static @Nullable TemplateBlockTileEntity GetEntity(IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TemplateBlockTileEntity) {
			return (TemplateBlockTileEntity) te;
		}
		return null;
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onBlockHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
			BlockPos pos = event.getTarget().getBlockPos();
			BlockState hit = event.getPlayer().world.getBlockState(pos);
			if (hit != null && hit.getBlock() == this) {
				// Get block from Tile Entity
				TemplateBlockTileEntity ent = GetEntity(event.getPlayer().world, pos);
				BlockState blockState = ent.getTemplateState();
				if (blockState == null) {
					blockState = Blocks.STONE.getDefaultState();
				}
				
				RenderFuncs.RenderBlockOutline(event.getPlayer(), event.getPlayer().world, pos, hit, event.getPartialTicks());
				
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
		
		world.setBlockState(pos, instance().getDefaultState(), 3);
		if (world.captureBlockSnapshots) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), instance().getDefaultState(), 3);
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
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TemplateBlockTileEntity();
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, BlockState state) {
		super.breakBlock(worldIn, pos, state);
	}
	
}
