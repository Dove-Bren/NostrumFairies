package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.container.LogisticsSensorGui;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;
import com.smanzana.nostrumfairies.tiles.LogisticsSensorTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
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
import net.minecraftforge.common.ToolType;

public class LogisticsSensorBlock extends FeyContainerBlock
{
	private static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final String ID = "logistics_sensor";
	
	public LogisticsSensorBlock() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(2.0f, 1.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.notSolid()
				);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}
	
	public boolean getActive(BlockState state) {
		return state.get(ACTIVE);
	}
	
	public BlockState getStateWithActive(boolean active) {
		return getDefaultState().with(ACTIVE, active);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState()
				.with(ACTIVE, false);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.fullCube();
	}
	
	@Override
	public boolean isValidPosition(BlockState stateIn, IWorldReader worldIn, BlockPos pos) {
		if (!Block.hasEnoughSolidSide(worldIn, pos.down(), Direction.UP)) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.DOWN && !isValidPosition(stateIn, world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return stateIn;
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		
		// Kick off a request to refresh info.
		if (worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof LogisticsSensorTileEntity) {
				LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) te;
				LogisticsNetwork network = sensor.getNetwork();
				if (network != null) {
					NetworkHandler.sendToServer(new LogisticsUpdateRequest(network.getUUID()));
				}
			}
		}
		
		
		// Don't wait, though, and show the UI
		LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, LogisticsSensorGui.LogisticsSensorContainer.Make(sensor));
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LogisticsSensorTileEntity();
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
		if (ent == null || !(ent instanceof LogisticsSensorTileEntity))
			return;
		
		LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) ent;
		final boolean activated = sensor.getLogicComponent().isActivated();
		sensor.unlinkFromNetwork();
		
		if (activated) {
			for (Direction side : Direction.values()) {
				world.notifyNeighborsOfStateChange(pos.offset(side), this);
			}
		}
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}
	
	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (blockState.get(ACTIVE)) {
			return 15;
		}
		
		return 0;
	}
}