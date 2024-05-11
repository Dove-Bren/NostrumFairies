package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.tiles.MiningBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;

public class MiningBlock extends FeyContainerBlock {

	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final String ID = "logistics_mining_block";
	public static final int WORKER_REACH = 16;
	public static final int MAJOR_LEVEL_DIFF = 16;
	public static final int PLATFORM_WIDTH = 3;
	public static final int STAIRCASE_RADIUS = 4;
	public static final int SHAFT_DISTANCE = 4;
	
	public MiningBlock() {
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (state.get(FACING).getHorizontalIndex() % 2 == 0) {
			return IFeySign.AABB_NS;
		} else {
			return IFeySign.AABB_EW;
		}
	}
	
	@Override
	public boolean isValidPosition(BlockState stateIn, IWorldReader worldIn, BlockPos pos) {
		if (!Block.hasEnoughSolidSide(worldIn, pos.down(), Direction.UP)) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.DOWN && !isValidPosition(stateIn, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return stateIn;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		return ActionResultType.PASS;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		TileEntity ent = new MiningBlockTileEntity();
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
		if (ent == null || !(ent instanceof MiningBlockTileEntity))
			return;
		
		MiningBlockTileEntity block = (MiningBlockTileEntity) ent;
		block.unlinkFromNetwork();
		MinecraftForge.EVENT_BUS.unregister(block);
	}
	
	public static boolean IsOre(World world, BlockPos pos) {
		if (world.isAirBlock(pos)) {
			return false;
		}
		
		BlockState state = world.getBlockState(pos);
		if (state.getBlockHardness(world, pos) < 0) {
			// unbreakable
			return false;
		}
		
		// See if it's one of the known ores
		if (Tags.Blocks.ORES.contains(state.getBlock())) {
			return true;
		}
		
		return false;
	}
}
