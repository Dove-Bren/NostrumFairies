package com.smanzana.nostrumfairies.blocks;

import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

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
	
	protected static VoxelShape LANTERN_AABB = Block.box(16 * 0.375D, 8, 16 * 0.375D, 16 * 0.625D, 16, 16 * 0.625D);
	
	public static final IntegerProperty Age = IntegerProperty.create("age", 0, 4);
	
	public static final String ID_BRIGHT = "magic_light_bright";
	public static final String ID_MEDIUM = "magic_light_medium";
	public static final String ID_DIM = "magic_light_dim";
	public static final String ID_UNLIT = "magic_light_unlit";
	
	private final Brightness brightness;
	
	public MagicLight(Brightness brightness) {
		super(Block.Properties.of(Material.METAL)
				.strength(0f, 100f)
				.randomTicks()
				.lightLevel((s) -> brightness.lightLevel)
				.noDrops()
				.noOcclusion()
				);
		this.brightness = brightness;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(Age);
	}
	
	@Override
	public boolean canSurvive(BlockState stateIn, LevelReader worldIn, BlockPos pos) {
		if (!Block.canSupportCenter(worldIn, pos.above(), Direction.DOWN)
				|| worldIn.getBlockState(pos.above()).getMaterial() != Material.STONE) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.UP && !canSurvive(stateIn, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return stateIn;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		return InteractionResult.PASS; // could do a cool ping animation or something
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		//LANTERN_AABB = new AxisAlignedBB(0.375D, 0.5D, 0.375D, 0.625D, 1D, 0.625D)
		return LANTERN_AABB;
	}
	
	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		
		if (!canSurvive(state, worldIn, pos)) {
			worldIn.removeBlock(pos, false);
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
				worldIn.setBlockAndUpdate(pos, FairyBlocks.magicLightMedium.defaultBlockState());
				break;
			case MEDIUM:
				worldIn.setBlockAndUpdate(pos, FairyBlocks.magicLightDim.defaultBlockState());
				break;
			case DIM:
				worldIn.setBlockAndUpdate(pos, FairyBlocks.magicLightUnlit.defaultBlockState());
				break;
			case UNLIT:
				break;
			}
			
		} else {
			worldIn.setBlockAndUpdate(pos, this.defaultBlockState().setValue(Age, age));
		}
	}
	
	public void refresh(Level worldIn, BlockPos pos) {
		worldIn.setBlockAndUpdate(pos, FairyBlocks.magicLightBright.defaultBlockState());
	}
}
