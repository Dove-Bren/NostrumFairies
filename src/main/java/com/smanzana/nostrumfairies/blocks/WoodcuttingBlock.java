package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.tiles.WoodcuttingBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class WoodcuttingBlock extends FeyContainerBlock {

	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final String ID = "logistics_woodcutting_block";
	
	public WoodcuttingBlock() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(3.0f, 1.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	public Direction getFacing(BlockState state) {
		return state.get(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState()
				.with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (state.get(FACING).getHorizontalIndex() % 2 == 0) {
			return IFeySign.AABB_NS;
		} else {
			return IFeySign.AABB_EW;
		}
	}
	
	@Override
	public boolean isValidPosition(BlockState stateIn, IWorldReader worldIn, BlockPos pos) {
		if (!Block.hasSolidSide(worldIn.getBlockState(pos.down()), worldIn, pos.down(), Direction.UP)) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.DOWN && !isValidPosition(stateIn, world, pos)) {
			return null;
		}
		return stateIn;
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		return false;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		TileEntity ent = new WoodcuttingBlockTileEntity();
		return ent;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof WoodcuttingBlockTileEntity))
			return;
		
		WoodcuttingBlockTileEntity block = (WoodcuttingBlockTileEntity) ent;
		block.unlinkFromNetwork();
	}
	
	public static boolean isLeafMaterial(BlockState state) {
		return BlockTags.LEAVES.contains(state.getBlock());
	}
	
	public static boolean isLeafMaterial(World world, BlockPos pos) {
		return isLeafMaterial(world.getBlockState(pos));
	}
	
	public static boolean isTrunkMaterial(BlockState state) {
		return BlockTags.LOGS.contains(state.getBlock());
	}
	
	public static boolean isTrunkMaterial(World world, BlockPos pos) {
		return isTrunkMaterial(world.getBlockState(pos));
	}
	
	public static boolean isTree(World world, BlockPos base) {
		// First, check if current block is even trunk material
		if (!isTrunkMaterial(world, base)) {
			return false;
		}
		
		// Then, check if there's also trunk material below. We only count the base.
		if (isTrunkMaterial(world, base.down())) {
			return false;
		}
		
		// A tree must be on the ground
		if (!Block.hasSolidSide(world.getBlockState(base.down()), world, base.down(), Direction.UP)) {
			return false;
		}
		
		// A tree must be topped with leaves
		MutableBlockPos pos = new MutableBlockPos(base);
		do {
			pos.move(Direction.UP);
		} while ((pos.getY() < 255) && isTrunkMaterial(world, pos));
		
		if (!isLeafMaterial(world, pos)) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isBranch(World world, BlockPos base) {
		// First, check if current block is even trunk material
		if (!isTrunkMaterial(world, base)) {
			return false;
		}
		
		// Then, check if there's also trunk material below. We only count the base.
		if (isTrunkMaterial(world, base.down())) {
			return false;
		}
		
		// A branch is NOT on the ground
		if (Block.hasSolidSide(world.getBlockState(base.down()), world, base.down(), Direction.UP)) {
			return false;
		}
		
		// We count the northern, western-most connected log as the branch... so if any are below, north, or west,
		// don't count this as the branch
		for (BlockPos pos : new BlockPos[]{base.down(), base.north(), base.west()}) {
			if (isTrunkMaterial(world, pos)) {
				return false;
			}
		}
		
		return true;
	}
}
