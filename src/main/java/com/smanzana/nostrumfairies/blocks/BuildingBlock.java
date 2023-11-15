package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.container.BuildingBlockGui;
import com.smanzana.nostrumfairies.tiles.BuildingBlockTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
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
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;

public class BuildingBlock extends FeyContainerBlock {

	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final String ID = "logistics_building_block";
	private static double BB_MAJOR = 16 * .345;
	private static double BB_MINOR = 16 * .03;
	private static final VoxelShape AABB_NS = Block.makeCuboidShape(8 - BB_MAJOR, 0, 8 - BB_MINOR, 8 + BB_MAJOR, 16 * .685, 8 + BB_MINOR);
	private static final VoxelShape AABB_EW = Block.makeCuboidShape(8 - BB_MINOR, 0, 8 - BB_MAJOR, 8 + BB_MINOR, 16 * .685, 8 + BB_MAJOR);
	
	public BuildingBlock() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(3f, 1f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.PICKAXE)
				.doesNotBlockMovement()
				);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return 2; // How much light out of 16 I think to take away
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
			return AABB_NS;
		} else {
			return AABB_EW;
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
			return Blocks.AIR.getDefaultState();
		}
		return stateIn;
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		BuildingBlockTileEntity buildBlock = (BuildingBlockTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, BuildingBlockGui.BuildingBlockContainer.Make(buildBlock));
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		TileEntity ent = new BuildingBlockTileEntity();
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
		if (ent == null || !(ent instanceof BuildingBlockTileEntity))
			return;
		
		BuildingBlockTileEntity block = (BuildingBlockTileEntity) ent;
		block.unlinkFromNetwork();
		if (!block.getTemplateScroll().isEmpty()) {
			ItemEntity item = new ItemEntity(
					world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
					block.getTemplateScroll());
			world.addEntity(item);
		}
		MinecraftForge.EVENT_BUS.unregister(block);
	}
	
	public static boolean isGrownCrop(World world, BlockPos base) {
		if (world == null || base == null) {
			return false;
		}
		
		BlockState state = world.getBlockState(base);
		if (state == null) {
			return false;
		}
		
		if (!(state.getBlock() instanceof CropsBlock)) {
			return false;
		}
		
		return ((CropsBlock) state.getBlock()).isMaxAge(state);
	}
	
	public static boolean isPlantableSpot(World world, BlockPos base, ItemStack seed) {
		if (world == null || base == null || seed.isEmpty()) {
			return false;
		}
		
		if (!world.isAirBlock(base.up())) {
			return false;
		}
		
		IPlantable plantable = null;
		if (seed.getItem() instanceof IPlantable) {
			plantable = (IPlantable) seed.getItem();
		} else if (seed.getItem() instanceof BlockItem && ((BlockItem) seed.getItem()).getBlock() instanceof IPlantable) {
			plantable = (IPlantable) ((BlockItem) seed.getItem()).getBlock();
		}
		
		if (plantable == null) {
			return false;
		}
		
		BlockState state = world.getBlockState(base);
		return state.getBlock().canSustainPlant(state, world, base, Direction.UP, plantable);
	}
}
