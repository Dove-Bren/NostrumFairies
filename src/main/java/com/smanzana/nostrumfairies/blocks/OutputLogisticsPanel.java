package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.tiles.OutputPanelTileEntity;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;

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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class OutputLogisticsPanel extends BlockContainer {
	
	private static final PropertyDirection FACING = PropertyDirection.create("facing");
	private static final double BB_MINOR = 1.0 / 16.0;
	private static final double BB_MAJOR = 2.0 / 16.0;
	private static final AxisAlignedBB AABB_N = new AxisAlignedBB(BB_MAJOR, BB_MAJOR, 0, 1 - BB_MAJOR, 1 - BB_MAJOR, BB_MINOR);
	private static final AxisAlignedBB AABB_E = new AxisAlignedBB(1 - BB_MINOR, BB_MAJOR, BB_MAJOR, 1, 1 - BB_MAJOR, 1 - BB_MAJOR);
	private static final AxisAlignedBB AABB_S = new AxisAlignedBB(BB_MAJOR, BB_MAJOR, 1 - BB_MINOR, 1 - BB_MAJOR, 1 - BB_MAJOR, 1);
	private static final AxisAlignedBB AABB_W = new AxisAlignedBB(0, BB_MAJOR, BB_MAJOR, BB_MINOR, 1 - BB_MAJOR, 1 - BB_MAJOR);
	private static final AxisAlignedBB AABB_U = new AxisAlignedBB(BB_MAJOR, 1 - BB_MINOR, BB_MAJOR, 1 - BB_MAJOR, 1, 1 - BB_MAJOR);
	private static final AxisAlignedBB AABB_D = new AxisAlignedBB(BB_MAJOR, 0, BB_MAJOR, 1 - BB_MAJOR, BB_MINOR, 1 - BB_MAJOR);
	public static final String ID = "logistics_output_panel";
	
	private static OutputLogisticsPanel instance = null;
	public static OutputLogisticsPanel instance() {
		if (instance == null)
			instance = new OutputLogisticsPanel();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(OutputPanelTileEntity.class, "logistics_output_panel_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public OutputLogisticsPanel() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	protected static int metaFromFacing(EnumFacing facing) {
		return facing.getIndex();
	}
	
	protected static EnumFacing facingFromMeta(int meta) {
		return EnumFacing.VALUES[meta % EnumFacing.VALUES.length];
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
		// Want to point towards the block we clicked
		facing = facing.getOpposite();
		if (!this.canPlaceAt(world, pos, facing) && facing.getIndex() > 1) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				facing = facing.rotateY();
				if (this.canPlaceAt(world, pos, facing)) {
					break;
				}
			}
		}
		
		return this.getDefaultState()
				.withProperty(FACING, facing);
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
		switch (state.getValue(FACING)) {
		case NORTH:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		case UP:
			return AABB_U;
		case DOWN:
		default:
			return AABB_D;
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		switch (blockState.getValue(FACING)) {
		case NORTH:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		case UP:
			return AABB_U;
		case DOWN:
		default:
			return AABB_D;
		}
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	protected boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing side) {
		IBlockState state = worldIn.getBlockState(pos.offset(side));
		if (state == null || !(state.getMaterial().blocksMovement())) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for (EnumFacing side : EnumFacing.VALUES) {
			if (canPlaceAt(worldIn, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos posFrom) {
		EnumFacing face = state.getValue(FACING);
		if (!canPlaceAt(worldIn, pos, face)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		} else {
			TileEntity ent = worldIn.getTileEntity(pos);
			if (ent != null && ent instanceof OutputPanelTileEntity) {
				((OutputPanelTileEntity) ent).notifyNeighborChanged();
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
		
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.outputPanelID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new OutputPanelTileEntity();
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
		if (ent == null || !(ent instanceof OutputPanelTileEntity))
			return;
		
		OutputPanelTileEntity table = (OutputPanelTileEntity) ent;
		table.unlinkFromNetwork();
	}
}
