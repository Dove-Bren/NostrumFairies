package com.smanzana.nostrumfairies.blocks;

import javax.annotation.Nonnull;

import com.smanzana.nostrumfairies.tiles.FairyTileEntities;
import com.smanzana.nostrumfairies.tiles.FarmingBlockTileEntity;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;

public class FarmingBlock extends FeyContainerBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final String ID = "logistics_farming_block";
	
	public FarmingBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(3.0f, 1.0f)
				.sound(SoundType.WOOD)
				);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	public Direction getFacing(BlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
				.setValue(FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (state.getValue(FACING).get2DDataValue() % 2 == 0) {
			return IFeySign.AABB_NS;
		} else {
			return IFeySign.AABB_EW;
		}
	}
	
	@Override
	public boolean canSurvive(BlockState stateIn, LevelReader worldIn, BlockPos pos) {
		if (!Block.canSupportCenter(worldIn, pos.below(), Direction.UP)) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.DOWN && !canSurvive(stateIn, world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return stateIn;
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		return InteractionResult.PASS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FarmingBlockTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, FairyTileEntities.FarmingBlockTileEntityType);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof FarmingBlockTileEntity))
			return;
		
		FarmingBlockTileEntity block = (FarmingBlockTileEntity) ent;
		block.unlinkFromNetwork();
	}
	
	public static boolean isGrownCrop(Level world, BlockPos base) {
		if (world == null || base == null) {
			return false;
		}
		
		BlockState state = world.getBlockState(base);
		if (state == null) {
			return false;
		}
		
		if (!(state.getBlock() instanceof CropBlock)) {
			return false;
		}
		
		return ((CropBlock) state.getBlock()).isMaxAge(state);
	}
	
	public static boolean isPlantableSpot(Level world, BlockPos base, @Nonnull ItemStack seed) {
		if (world == null || base == null || seed.isEmpty()) {
			return false;
		}
		
		if (!world.isEmptyBlock(base.above())) {
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
