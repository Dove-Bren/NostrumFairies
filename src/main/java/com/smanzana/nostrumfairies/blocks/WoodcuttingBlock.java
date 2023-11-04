package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.WoodcuttingBlockTileEntity;
import com.smanzana.nostrumfairies.utils.OreDict;

import net.minecraft.block.Block;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.BlockRenderType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.common.MinecraftForge;

public class WoodcuttingBlock extends FeyContainerBlock {

	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final String ID = "logistics_woodcutting_block";
	
	private static WoodcuttingBlock instance = null;
	public static WoodcuttingBlock instance() {
		if (instance == null)
			instance = new WoodcuttingBlock();
		
		return instance;
	}
	
	public WoodcuttingBlock() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setLightOpacity(2);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	protected static int metaFromFacing(Direction facing) {
		return facing.getHorizontalIndex();
	}
	
	protected static Direction facingFromMeta(int meta) {
		return Direction.getHorizontal(meta);
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.with(FACING, facingFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return metaFromFacing(state.get(FACING));
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
	public boolean isFullBlock(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
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
	public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if (blockState.getValue(FACING).getHorizontalIndex() % 2 == 0) {
			return IFeySign.AABB_NS;
		} else {
			return IFeySign.AABB_EW;
		}
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean isValidPosition(BlockState stateIn, IWorldReader worldIn, BlockPos pos) {
		BlockState state = worldIn.getBlockState(pos.down());
		if (state == null || !(state.isSideSolid(worldIn, pos.down(), Direction.UP))) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos posFrom) {
		if (!canPlaceBlockAt(worldIn, pos)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		
		super.neighborChanged(state, worldIn, pos, blockIn, posFrom);
	}
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return true;
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
		MinecraftForge.TERRAIN_GEN_BUS.unregister(block);
	}
	
	public static boolean isLeafMaterial(BlockState state) {
		
		// Easy fast check
		if (state.getBlock() == Blocks.LEAVES
				|| state.getBlock() == Blocks.LEAVES2) {
			return true;
		}
		
		return OreDict.blockMatchesOreDict(state, "treeLeaves", false);
	}
	
	public static boolean isLeafMaterial(World world, BlockPos pos) {
		return isLeafMaterial(world.getBlockState(pos));
	}
	
	public static boolean isTrunkMaterial(BlockState state) {
		
		// Easy fast check
		if (state.getBlock() == Blocks.LOG
				|| state.getBlock() == Blocks.LOG2) {
			return true;
		}
		
		return OreDict.blockMatchesOreDict(state, "logWood", false);
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
		if (!world.isSideSolid(base.down(), Direction.UP)) {
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
		if (world.isSideSolid(base.down(), Direction.UP)) {
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
