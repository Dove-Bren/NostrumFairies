package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.container.LogisticsSensorGui;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.tiles.FairyTileEntities;
import com.smanzana.nostrumfairies.tiles.LogisticsSensorTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LogisticsSensorBlock extends FeyContainerBlock
{
	private static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final String ID = "logistics_sensor";
	
	public LogisticsSensorBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.0f, 1.0f)
				.sound(SoundType.WOOD)
				.noOcclusion()
				);
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}
	
	public boolean getActive(BlockState state) {
		return state.getValue(ACTIVE);
	}
	
	public BlockState getStateWithActive(boolean active) {
		return defaultBlockState().setValue(ACTIVE, active);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
				.setValue(ACTIVE, false);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.block();
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
		
		// Kick off a request to refresh info.
		if (worldIn.isClientSide) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te != null && te instanceof LogisticsSensorTileEntity) {
				LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) te;
				LogisticsNetwork network = sensor.getNetwork();
				if (network != null) {
					NetworkHandler.sendToServer(new LogisticsUpdateRequest(network.getUUID()));
				}
			}
		}
		
		
		// Don't wait, though, and show the UI
		LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.Proxy.openContainer(playerIn, LogisticsSensorGui.LogisticsSensorContainer.Make(sensor));
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LogisticsSensorTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, FairyTileEntities.LogisticsSensorTileEntityType);
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
		if (ent == null || !(ent instanceof LogisticsSensorTileEntity))
			return;
		
		LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) ent;
		final boolean activated = sensor.getLogicComponent().isActivated();
		sensor.unlinkFromNetwork();
		
		if (activated) {
			for (Direction side : Direction.values()) {
				world.updateNeighborsAt(pos.relative(side), this);
			}
		}
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}
	
	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (blockState.getValue(ACTIVE)) {
			return 15;
		}
		
		return 0;
	}
}