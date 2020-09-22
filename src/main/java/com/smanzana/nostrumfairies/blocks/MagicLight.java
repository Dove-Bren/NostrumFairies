package com.smanzana.nostrumfairies.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MagicLight extends Block {
	
	private static enum Brightness {
		BRIGHT,
		MEDIUM,
		DIM,
		UNLIT
	}
	
	protected static AxisAlignedBB LANTERN_AABB = new AxisAlignedBB(0.375D, 0.5D, 0.375D, 0.625D, 1D, 0.625D);
	
	public static final PropertyInteger Age = PropertyInteger.create("age", 0, 4);
	
	public static final String BrightID = "magic_light_bright";
	public static final String MediumID = "magic_light_medium";
	public static final String DimID = "magic_light_dim";
	public static final String UnlitID = "magic_light_unlit";
	
	private static MagicLight bright = null;
	private static MagicLight medium = null;
	private static MagicLight dim = null;
	private static MagicLight unlit = null;
	
	private final Brightness brightness;
	
	public static MagicLight Bright() {
		if (bright == null)
			bright = new MagicLight(Brightness.BRIGHT);
		
		return bright;
	}
	
	public static MagicLight Medium() {
		if (medium == null)
			medium = new MagicLight(Brightness.MEDIUM);
		
		return medium;
	}
	
	public static MagicLight Dim() {
		if (dim == null)
			dim = new MagicLight(Brightness.DIM);
		
		return dim;
	}
	
	public static MagicLight Unlit() {
		if (unlit == null)
			unlit = new MagicLight(Brightness.UNLIT);
		
		return unlit;
	}
	
	private MagicLight(Brightness brightness) {
		super(Material.CIRCUITS, MapColor.BLUE);
		this.setHardness(0.0f);
		this.setResistance(100.0f);
		this.setLightOpacity(0);
		this.setTickRandomly(true);
		this.brightness = brightness;
		
		switch (brightness) {
		case BRIGHT:
			this.setUnlocalizedName(BrightID);
			this.setLightLevel(0.875f);
			break;
		case MEDIUM:
			this.setUnlocalizedName(MediumID);
			this.setLightLevel(0.75f);
			break;
		case DIM:
			this.setUnlocalizedName(DimID);
			this.setLightLevel(0.5f);
			break;
		case UNLIT:
			this.setUnlocalizedName(UnlitID);
			this.setLightLevel(0f);
			break;
		}
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, Age);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(Age, Math.min(4, Math.max(0, meta)));
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return worldIn.getBlockState(pos.up()).isSideSolid(worldIn, pos.up(), EnumFacing.DOWN)
				&& worldIn.getBlockState(pos.up()).getMaterial() == Material.ROCK;
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(Age);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false; // could do a cool ping animation or something
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isFullyOpaque(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		//LANTERN_AABB = new AxisAlignedBB(0.375D, 0.5D, 0.375D, 0.625D, 1D, 0.625D)
		return LANTERN_AABB;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		super.randomTick(worldIn, pos, state, random);
		
		if (!canPlaceBlockAt(worldIn, pos)) {
			worldIn.setBlockToAir(pos);
			return;
		}
		
		if (this.brightness == Brightness.UNLIT) {
			return;
		}
		
		// Age
		int age = state.getValue(Age) + 1;
		if (age > 4) {
			switch (this.brightness) {
			case BRIGHT:
				worldIn.setBlockState(pos, MagicLight.Medium().getDefaultState());
				break;
			case MEDIUM:
				worldIn.setBlockState(pos, MagicLight.Dim().getDefaultState());
				break;
			case DIM:
				worldIn.setBlockState(pos, MagicLight.Unlit().getDefaultState());
				break;
			case UNLIT:
				break;
			}
			
		} else {
			worldIn.setBlockState(pos, this.getDefaultState().withProperty(Age, age));
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);
		
//		switch (brightness) {
//		case BRIGHT:
//			; // always
//			break;
//		case MEDIUM:
//			// 75% effects
//			if (rand.nextFloat() < .25f) {
//				return;
//			}
//			break;
//		case DIM:
//			// 50%
//			if (rand.nextFloat() < .5f) {
//				return;
//			}
//			break;
//		}
//		
//		double x = (double)pos.getX() + 0.25D + (rand.nextFloat() * .5f);
//		double y = (double)pos.getY() + 0.6D + (rand.nextFloat() * .2f);
//		double z = (double)pos.getZ() + 0.25D + (rand.nextFloat() * .5f);
//
//		worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
//		worldIn.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
	}
	
	public void refresh(World worldIn, BlockPos pos) {
		worldIn.setBlockState(pos, Bright().getDefaultState());
	}
	
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		if (!canPlaceBlockAt(worldIn, pos)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
	}
}
