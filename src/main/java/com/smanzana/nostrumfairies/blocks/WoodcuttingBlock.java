package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.IFeySign;
import com.smanzana.nostrumfairies.tiles.WoodcuttingBlockTileEntity;
import com.smanzana.nostrumfairies.utils.OreDict;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class WoodcuttingBlock extends BlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
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
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
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
				.withProperty(FACING, facingFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return metaFromFacing(state.getValue(FACING));
	}
	
	public Direction getFacing(BlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return this.getDefaultState()
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
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
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(FACING).getHorizontalIndex() % 2 == 0) {
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
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
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
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, EnumHand hand, Direction side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntity ent = new WoodcuttingBlockTileEntity();
		return ent;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(BlockState state) {
		return EnumBlockRenderType.MODEL;
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
