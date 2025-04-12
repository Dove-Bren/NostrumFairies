package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.container.CraftingStationGui;
import com.smanzana.nostrumfairies.tiles.CraftingBlockElfTileEntity;
import com.smanzana.nostrumfairies.tiles.FairyTileEntities;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

public class CraftingBlockElf extends FeyContainerBlock {
	
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final String ID = "logistics_crafting_station_elf";
	private static final VoxelShape AABB = Block.box(0, 0, 0, 16, 8, 16);
	
	public CraftingBlockElf() {
		super(Block.Properties.of(Material.WOOD)
				.strength(4f, 1f)
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
		return AABB;
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
		
		if (!worldIn.isClientSide) {
			worldIn.sendBlockUpdated(pos, state, state, 2);
		}
		
		CraftingBlockElfTileEntity craftBlock = (CraftingBlockElfTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, CraftingStationGui.CraftingStationContainer.Make(craftBlock));
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent != null && ent instanceof CraftingBlockElfTileEntity) {
			((CraftingBlockElfTileEntity) ent).notifyNeighborChanged();
		}
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CraftingBlockElfTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, FairyTileEntities.CraftingBlockElfTileEntityType);
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
		if (ent == null || !(ent instanceof CraftingBlockElfTileEntity))
			return;
		
		CraftingBlockElfTileEntity table = (CraftingBlockElfTileEntity) ent;
		for (int i = 0; i < table.getContainerSize(); i++) {
			if (!table.getItem(i).isEmpty()) {
				ItemEntity item = new ItemEntity(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeItemNoUpdate(i));
				world.addFreshEntity(item);
			}
		}
		
		table.unlinkFromNetwork();
	}
}
