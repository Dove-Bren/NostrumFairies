package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.container.OutputPanelGui;
import com.smanzana.nostrumfairies.tiles.OutputPanelTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class OutputLogisticsPanel extends FeyContainerBlock {
	
	private static final DirectionProperty FACING = DirectionProperty.create("facing");
	private static final double BB_MINOR = 1.0;
	private static final double BB_MAJOR = 2.0;
	private static final VoxelShape AABB_N = Block.makeCuboidShape(BB_MAJOR, BB_MAJOR, 0, 16 - BB_MAJOR, 16 - BB_MAJOR, BB_MINOR);
	private static final VoxelShape AABB_E = Block.makeCuboidShape(16 - BB_MINOR, BB_MAJOR, BB_MAJOR, 16, 16 - BB_MAJOR, 16 - BB_MAJOR);
	private static final VoxelShape AABB_S = Block.makeCuboidShape(BB_MAJOR, BB_MAJOR, 16 - BB_MINOR, 16 - BB_MAJOR, 16 - BB_MAJOR, 16);
	private static final VoxelShape AABB_W = Block.makeCuboidShape(0, BB_MAJOR, BB_MAJOR, BB_MINOR, 16 - BB_MAJOR, 16 - BB_MAJOR);
	private static final VoxelShape AABB_U = Block.makeCuboidShape(BB_MAJOR, 16 - BB_MINOR, BB_MAJOR, 16 - BB_MAJOR, 16, 16 - BB_MAJOR);
	private static final VoxelShape AABB_D = Block.makeCuboidShape(BB_MAJOR, 0, BB_MAJOR, 16 - BB_MAJOR, BB_MINOR, 16 - BB_MAJOR);
	public static final String ID = "logistics_output_panel";
	
	public OutputLogisticsPanel() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(3.0f, 1.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	protected static int metaFromFacing(Direction facing) {
		return facing.getIndex();
	}
	
	public Direction getFacing(BlockState state) {
		return state.get(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		// Want to point towards the block we clicked
		final World world = context.getWorld();
		final BlockPos pos = context.getPos();
		Direction facing = context.getFace().getOpposite();
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
				.with(FACING, facing);
	}
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(FACING)) {
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
	
	protected boolean canPlaceAt(IWorldReader worldIn, BlockPos pos, Direction side) {
		BlockState state = worldIn.getBlockState(pos.offset(side));
		if (state == null || !(state.getMaterial().blocksMovement())) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isValidPosition(BlockState stateIn, IWorldReader worldIn, BlockPos pos) {
		for (Direction side : Direction.values()) {
			if (canPlaceAt(worldIn, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos posFrom, boolean isMoving) {
		Direction face = state.get(FACING);
		if (!canPlaceAt(worldIn, pos, face)) {
			worldIn.removeBlock(pos, true);
		} else {
			TileEntity ent = worldIn.getTileEntity(pos);
			if (ent != null && ent instanceof OutputPanelTileEntity) {
				((OutputPanelTileEntity) ent).notifyNeighborChanged();
			}
		}
		
		super.neighborChanged(state, worldIn, posFrom, blockIn, posFrom, isMoving);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		OutputPanelTileEntity panel = (OutputPanelTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, OutputPanelGui.OutputPanelContainer.Make(panel));
		
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new OutputPanelTileEntity();
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
		if (ent == null || !(ent instanceof OutputPanelTileEntity))
			return;
		
		OutputPanelTileEntity table = (OutputPanelTileEntity) ent;
		table.unlinkFromNetwork();
	}
}
