package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.client.gui.container.CraftingStationSmallGui;
import com.smanzana.nostrumfairies.tiles.CraftingBlockGnomeTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class CraftingBlockGnome extends FeyContainerBlock {
	
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final String ID = "logistics_crafting_station_gnome";
	private static final VoxelShape SEL_AABB = Block.makeCuboidShape(0, 0, 0, 16, 8.8, 16);
	private static final VoxelShape COL_AABB = Block.makeCuboidShape(5.2, 0, 5.2, 10.8, 8, 10.8);
	
	public CraftingBlockGnome() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(4f, 1f)
				.sound(SoundType.WOOD)
				.harvestLevel(1)
				.harvestTool(ToolType.AXE)
				);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	protected static int metaFromFacing(Direction facing) {
		return facing.getHorizontalIndex();
	}
	
	public Direction getFacing(BlockState state) {
		return state.get(FACING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState()
				.with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SEL_AABB;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return COL_AABB;
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
		
		if (!worldIn.isRemote) {
			worldIn.notifyBlockUpdate(pos, state, state, 2);
		}
		
		CraftingBlockGnomeTileEntity craftBlock = (CraftingBlockGnomeTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(playerIn, CraftingStationSmallGui.CraftingStationSmallContainer.Make(craftBlock));
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent != null && ent instanceof CraftingBlockGnomeTileEntity) {
			((CraftingBlockGnomeTileEntity) ent).notifyNeighborChanged();
		}
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new CraftingBlockGnomeTileEntity();
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
		if (ent == null || !(ent instanceof CraftingBlockGnomeTileEntity))
			return;
		
		CraftingBlockGnomeTileEntity table = (CraftingBlockGnomeTileEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (!table.getStackInSlot(i).isEmpty()) {
				ItemEntity item = new ItemEntity(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.addEntity(item);
			}
		}
		
		table.unlinkFromNetwork();
	}
}
