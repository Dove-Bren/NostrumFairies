package com.smanzana.nostrumfairies.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MagicLight extends Block {
	
	public static enum Brightness {
		BRIGHT(14),
		MEDIUM(12),
		DIM(8),
		UNLIT(0);
		
		public final int lightLevel;
		
		private Brightness(int lightLevel) {
			this.lightLevel = lightLevel;
		}
	}
	
	protected static VoxelShape LANTERN_AABB = Block.makeCuboidShape(16 * 0.375D, 8, 16 * 0.375D, 16 * 0.625D, 16, 16 * 0.625D);
	
	public static final IntegerProperty Age = IntegerProperty.create("age", 0, 4);
	
	public static final String ID_BRIGHT = "magic_light_bright";
	public static final String ID_MEDIUM = "magic_light_medium";
	public static final String ID_DIM = "magic_light_dim";
	public static final String ID_UNLIT = "magic_light_unlit";
	
	private final Brightness brightness;
	
	public MagicLight(Brightness brightness) {
		super(Block.Properties.create(Material.IRON)
				.hardnessAndResistance(0f, 100f)
				.tickRandomly()
				.setLightLevel((s) -> brightness.lightLevel)
				.noDrops()
				);
		this.brightness = brightness;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(Age);
	}
	
	@Override
	public boolean isValidPosition(BlockState stateIn, IWorldReader worldIn, BlockPos pos) {
		if (!Block.hasEnoughSolidSide(worldIn, pos.up(), Direction.DOWN)
				|| worldIn.getBlockState(pos.up()).getMaterial() != Material.ROCK) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.UP && !isValidPosition(stateIn, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return stateIn;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		return ActionResultType.PASS; // could do a cool ping animation or something
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		//LANTERN_AABB = new AxisAlignedBB(0.375D, 0.5D, 0.375D, 0.625D, 1D, 0.625D)
		return LANTERN_AABB;
	}
	
	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		
		if (!isValidPosition(state, worldIn, pos)) {
			worldIn.removeBlock(pos, false);
			return;
		}
		
		if (this.brightness == Brightness.UNLIT) {
			return;
		}
		
		// Age
		int age = state.get(Age) + 1;
		if (age > 4) {
			switch (this.brightness) {
			case BRIGHT:
				worldIn.setBlockState(pos, FairyBlocks.magicLightMedium.getDefaultState());
				break;
			case MEDIUM:
				worldIn.setBlockState(pos, FairyBlocks.magicLightDim.getDefaultState());
				break;
			case DIM:
				worldIn.setBlockState(pos, FairyBlocks.magicLightUnlit.getDefaultState());
				break;
			case UNLIT:
				break;
			}
			
		} else {
			worldIn.setBlockState(pos, this.getDefaultState().with(Age, age));
		}
	}
	
	public void refresh(World worldIn, BlockPos pos) {
		worldIn.setBlockState(pos, FairyBlocks.magicLightBright.getDefaultState());
	}
}
