package com.smanzana.nostrumfairies.blocks;

import java.util.HashSet;
import java.util.Set;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.IFeySign;
import com.smanzana.nostrumfairies.tiles.MiningBlockTileEntity;
import com.smanzana.nostrumfairies.utils.OreDict;
import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.ManiOre;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.DirectionProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;

public class MiningBlock extends FeyContainerBlock {

	public static final DirectionProperty FACING = HorizontalBlock.FACING;
	public static final String ID = "logistics_mining_block";
	public static final int WORKER_REACH = 16;
	public static final int MAJOR_LEVEL_DIFF = 16;
	public static final int PLATFORM_WIDTH = 3;
	public static final int STAIRCASE_RADIUS = 4;
	public static final int SHAFT_DISTANCE = 4;
	
	private static MiningBlock instance = null;
	public static MiningBlock instance() {
		if (instance == null)
			instance = new MiningBlock();
		
		return instance;
	}
	
	public MiningBlock() {
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
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		return false;
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
		InitExtras();
		
		if (world.isAirBlock(pos)) {
			return false;
		}
		
		BlockState state = world.getBlockState(pos);
		if (state.getBlockHardness(world, pos) < 0) {
			// unbreakable
			return false;
		}
		
		// See if it's one of the known ores
		Block block = state.getBlock();
		if (block == Blocks.COAL_ORE
			|| block == Blocks.DIAMOND_ORE
			|| block == Blocks.EMERALD_ORE
			|| block == Blocks.GOLD_ORE
			|| block == Blocks.IRON_ORE
			|| block == Blocks.LAPIS_ORE
			|| block == Blocks.LIT_REDSTONE_ORE
			|| block == Blocks.QUARTZ_ORE
			|| block == Blocks.REDSTONE_ORE
			|| block == EssenceOre.instance()
			|| block == ManiOre.instance()
				) {
			return true;
		}
		
		// Check if it's in our extra list
		// First, try ore dictionary
		if (OreDict.blockMatchesAny(state, ExtraCachedArray, false)) {
			return true;
		}
		
		// Then try to see if any are registry names
		String registryName = block.getRegistryName().toString();
		for (String extra : ExtraOres) {
			if (extra.compareToIgnoreCase(registryName) == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	private static Set<String> ExtraOres = null;
	private static String[] ExtraCachedArray = null;
	
	private static void InitExtras() {
		ExtraOres = new HashSet<>();
		
		// Vanilla entries
		ExtraOres.add("oreGold");
		ExtraOres.add("oreIron");
		ExtraOres.add("oreLapis");
		ExtraOres.add("oreDiamond");
		ExtraOres.add("oreRedstone");
		ExtraOres.add("oreEmerald");
		ExtraOres.add("oreQuartz");
		ExtraOres.add("oreCoal");
		
		// Popular mod entries
		ExtraOres.add("oreCopper");
		ExtraOres.add("oreAluminum");
		ExtraOres.add("oreLead");
		ExtraOres.add("oreSteel");
		ExtraOres.add("oreTin");
		ExtraOres.add("oreBronze");
		
		// And, in a ditch effort, look through ore dictionary
		for (String entry : OreDictionary.getOreNames()) {
			if (entry.startsWith("ore")) {
				ExtraOres.add(entry);
			}
		}
		
		ExtraCachedArray = ExtraOres.toArray(new String[ExtraOres.size()]);
	}
	
	public static void AddOreName(String registryOrDictionary) {
		InitExtras();
		ExtraOres.add(registryOrDictionary);
		ExtraCachedArray = ExtraOres.toArray(new String[ExtraOres.size()]);
	}
}
