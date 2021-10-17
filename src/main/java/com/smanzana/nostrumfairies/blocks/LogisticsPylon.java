package com.smanzana.nostrumfairies.blocks;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.tiles.PylonTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class LogisticsPylon extends BlockContainer {
	
	public static final PropertyDirection FACING = PropertyDirection.create("facing", Lists.newArrayList(EnumFacing.UP, EnumFacing.DOWN));
	public static final String ID = "logistics_pylon";
	
	private static LogisticsPylon instance = null;
	public static LogisticsPylon instance() {
		if (instance == null)
			instance = new LogisticsPylon();
		
		return instance;
	}
	
	public LogisticsPylon() {
		super(Material.ROCK, MapColor.BLUE);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("pickaxe", 0);
		this.setLightOpacity(2);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	protected static int metaFromFacing(EnumFacing facing) {
		return facing == EnumFacing.UP ? 0 : 1;
	}
	
	protected static EnumFacing facingFromMeta(int meta) {
		return meta == 0 ? EnumFacing.UP : EnumFacing.DOWN;
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
				.withProperty(FACING, facing == EnumFacing.DOWN ? facing : EnumFacing.UP);
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
		return true;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
//		IBlockState state = worldIn.getBlockState(pos.up());
//		if (state == null || !(state.isSideSolid(worldIn, pos.down(), EnumFacing.DOWN))) {
//			return false;
//		}
		
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.DOWN) {
			IBlockState state = worldIn.getBlockState(pos.up());
			if (state == null || !state.isSideSolid(worldIn, pos.up(), EnumFacing.DOWN)) {
				return false;
			}
		}
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos posFrom) {
		if (state.getValue(FACING) == EnumFacing.DOWN) {
			IBlockState ceilState = worldIn.getBlockState(pos.up());
			if (ceilState == null || !ceilState.isSideSolid(worldIn, pos.up(), EnumFacing.DOWN)) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
			}
		}
		
		super.neighborChanged(state, worldIn, pos, blockIn, posFrom);
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false; // could do a cool ping animation or something
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new PylonTileEntity();
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
		if (ent == null || !(ent instanceof PylonTileEntity))
			return;
		
		PylonTileEntity monitor = (PylonTileEntity) ent;
		monitor.unlinkFromNetwork();
	}
}
