package com.smanzana.nostrumfairies.blocks;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.tiles.FarmingBlockTileEntity;
import com.smanzana.nostrumfairies.blocks.tiles.IFeySign;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;

public class FarmingBlock extends BlockContainer {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final String ID = "logistics_farming_block";
	
	private static FarmingBlock instance = null;
	public static FarmingBlock instance() {
		if (instance == null)
			instance = new FarmingBlock();
		
		return instance;
	}
	
	public FarmingBlock() {
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
	
	protected static int metaFromFacing(EnumFacing facing) {
		return facing.getHorizontalIndex();
	}
	
	protected static EnumFacing facingFromMeta(int meta) {
		return EnumFacing.getHorizontal(meta);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, facingFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return metaFromFacing(state.getValue(FACING));
	}
	
	public EnumFacing getFacing(IBlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState()
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(FACING).getHorizontalIndex() % 2 == 0) {
			return IFeySign.AABB_NS;
		} else {
			return IFeySign.AABB_EW;
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
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
		IBlockState state = worldIn.getBlockState(pos.down());
		if (state == null || !(state.isSideSolid(worldIn, pos.down(), EnumFacing.UP))) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos posFrom) {
		if (!canPlaceBlockAt(worldIn, pos)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		
		super.neighborChanged(state, worldIn, pos, blockIn, posFrom);
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntity ent = new FarmingBlockTileEntity();
		return ent;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof FarmingBlockTileEntity))
			return;
		
		FarmingBlockTileEntity block = (FarmingBlockTileEntity) ent;
		block.unlinkFromNetwork();
		MinecraftForge.TERRAIN_GEN_BUS.unregister(block);
	}
	
	public static boolean isGrownCrop(World world, BlockPos base) {
		if (world == null || base == null) {
			return false;
		}
		
		IBlockState state = world.getBlockState(base);
		if (state == null) {
			return false;
		}
		
		if (!(state.getBlock() instanceof BlockCrops)) {
			return false;
		}
		
		return ((BlockCrops) state.getBlock()).isMaxAge(state);
	}
	
	public static boolean isPlantableSpot(World world, BlockPos base, @Nonnull ItemStack seed) {
		if (world == null || base == null || seed.isEmpty()) {
			return false;
		}
		
		if (!world.isAirBlock(base.up())) {
			return false;
		}
		
		IPlantable plantable = null;
		if (seed.getItem() instanceof IPlantable) {
			plantable = (IPlantable) seed.getItem();
		} else if (seed.getItem() instanceof ItemBlock && ((ItemBlock) seed.getItem()).getBlock() instanceof IPlantable) {
			plantable = (IPlantable) ((ItemBlock) seed.getItem()).getBlock();
		}
		
		if (plantable == null) {
			return false;
		}
		
		IBlockState state = world.getBlockState(base);
		return state.getBlock().canSustainPlant(state, world, base, EnumFacing.UP, plantable);
	}
}
