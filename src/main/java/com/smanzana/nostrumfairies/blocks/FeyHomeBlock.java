package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrumfairies.client.gui.container.HomeBlockGui;
import com.smanzana.nostrumfairies.entity.ResidentType;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

/**
 * Houses fey.
 * Has all the bed and satisfaction and recruiting and job management stuff in it.
 * @author Skyler
 *
 */
public class FeyHomeBlock extends FeyContainerBlock {
	
	private static enum BlockFunction implements IStringSerializable {
		CENTER,
		TOP;

		@Override
		public String getString() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.getString();
		}
	}
	
	//public static final PropertyEnum<ResidentType> TYPE = PropertyEnum.<ResidentType>create("type", ResidentType.class);
	public static final EnumProperty<BlockFunction> BLOCKFUNC = EnumProperty.<BlockFunction>create("func", BlockFunction.class);
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	
	protected static final String ID_PREFIX = "home_block_";
	public static final String ID_DWARF = ID_PREFIX + "dwarf";
	public static final String ID_ELF = ID_PREFIX + "elf";
	public static final String ID_GNOME = ID_PREFIX + "gnome";
	public static final String ID_FAIRY = ID_PREFIX + "fairy";
	
	private final ResidentType type;
	
	public FeyHomeBlock(ResidentType type) {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(4f, 100.0f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				);
		this.type = type;
	}
	
	public static FeyHomeBlock getBlock(ResidentType type) {
		switch (type) {
		case DWARF:
			return FairyBlocks.dwarfHome;
		case ELF:
			return FairyBlocks.elfHome;
		case FAIRY:
			return FairyBlocks.fairyHome;
		case GNOME:
			return FairyBlocks.gnomeHome;
		}
		
		return null;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(BLOCKFUNC, FACING);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		// non-center states don't do drops!
		if (!isCenter(state)) {
			return new ArrayList<>();
		}
		
		return super.getDrops(state, builder);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (state == null || !(state.getBlock() instanceof FeyHomeBlock)) {
			return ActionResultType.FAIL;
		}
		
		BlockPos center = ((FeyHomeBlock) state.getBlock()).getMasterPos(worldIn, pos, state);
		TileEntity te = worldIn.getTileEntity(center);
		if (te == null || !(te instanceof HomeBlockTileEntity)) {
			return ActionResultType.FAIL;
		}
		
		NostrumMagica.instance.proxy.openContainer(playerIn, HomeBlockGui.HomeBlockContainer.Make((HomeBlockTileEntity) te));
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	public boolean isCenter(BlockState state) {
		return state.get(BLOCKFUNC) == BlockFunction.CENTER;
	}
	
	public ResidentType getType(BlockState state) {
		return type;
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		TileEntity ent = world.getTileEntity(pos);
		if (ent != null && ent instanceof HomeBlockTileEntity) {
			HomeBlockTileEntity te = (HomeBlockTileEntity) ent;
			te.unlinkFromNetwork();
			
			// Slot inventory and upgrade inventories
			for (IInventory inv : new IInventory[] {te.getSlotInventory(), te.getUpgradeInventory()}) {
				for (int i = 0; i < inv.getSizeInventory(); i++) {
					if (!inv.getStackInSlot(i).isEmpty()) {
						ItemEntity item = new ItemEntity(
								world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
								inv.removeStackFromSlot(i));
						world.addEntity(item);
					}
				}
			}
		}
		world.removeTileEntity(pos);
		
		world.destroyBlock(getPaired(state, pos), true);
	}
	
	private BlockPos getPaired(BlockState state, BlockPos pos) {
		return pos.offset(state.get(BLOCKFUNC) == BlockFunction.CENTER ? Direction.UP : Direction.DOWN);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		this.destroy(world, pos, state);
		world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean isValidPosition(BlockState stateIn, IWorldReader worldIn, BlockPos pos) {
		
		for (BlockPos cursor : new BlockPos[] {pos, pos.up(), pos.up().up()}) {
			if (cursor.getY() > 255 || cursor.getY() <= 0) {
				return false;
			}
			
			BlockState state = worldIn.getBlockState(cursor);
			Block block = state.getBlock();
			if (!state.getMaterial().isReplaceable()
					&& !(block instanceof LeavesBlock)) {
				return false;
			}
			
			if (worldIn.getTileEntity(cursor) != null) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState()
				.with(BLOCKFUNC, BlockFunction.CENTER)
				.with(FACING, context.getPlacementHorizontalFacing().getOpposite());
				
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// This method hopefully is ONLY called when placed manually in the world.
		// Auto-create slave state
		Direction facing = state.get(FACING);
		this.spawn(worldIn, pos, type, facing);
		
//		worldIn.setBlockState(pos.up(), this.getDefaultState()
//				.with(BLOCKFUNC, BlockFunction.TOP));
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (isCenter(state)) {
			return new HomeBlockTileEntity(type);
		}
		
		return null;
	}
	
	public BlockPos getMasterPos(World world, BlockPos pos, BlockState state) {
		if (state == null) {
			state = world.getBlockState(pos);
		}
		
		if (isCenter(state)) {
			return pos;
		}
		return pos.down();
	}
	
	/**
	 * Spawns a home block tree at the provided space. Skips the base block.
	 * @param world
	 * @param base
	 * @param type
	 */
	public void spawn(World world, BlockPos base, ResidentType type, Direction direction) {
		//world.setBlockState(base, getDefaultState().with(BLOCKFUNC, BlockFunction.CENTER).with(FACING, direction));
		world.setBlockState(base.up(), getDefaultState().with(BLOCKFUNC, BlockFunction.TOP).with(FACING, direction)); // could be random
		world.setBlockState(base.up().up(), Blocks.DARK_OAK_LOG.getDefaultState());
		
		BlockState leaves = Blocks.DARK_OAK_LEAVES.getDefaultState();
		BlockPos origin = base.up().up();
		for (BlockPos cursor : new BlockPos[] {
				// Layer 1
				origin.north().north(),
				origin.north().east(), origin.north(), origin.north().west(),
				origin.east().east(), origin.east(), origin.west(), origin.west().west(),
				origin.south().east(), origin.south(), origin.south().west(),
				origin.south().south(),
				
				// Layer 2
				origin.up().north().east(), origin.up().north(), origin.up().north().west(),
				origin.up().east().east(), origin.up().east(), origin.up(), origin.up().west(), origin.up().west().west(),
				origin.up().south().east(), origin.up().south(), origin.up().south().west(),
				
				// Layer 3
				origin.up().up().north(),
				origin.up().up().east(), origin.up().up(), origin.up().up().west(),
				origin.up().up().south(),
		}) {
			world.setBlockState(cursor, leaves);
		}
	}
	
	public static boolean SpecializationMaterialAllowed(ResidentType type, FeyStoneMaterial material) {
		if (!material.existsForSlot(FeySlotType.SPECIALIZATION)) {
			return false;
		}
		
		return EntityFeyBase.canUseSpecialization(type, material);
	}
	
	public static FeyStoneMaterial[] GetSpecMaterials(ResidentType type) {
		ArrayList<FeyStoneMaterial> mats = new ArrayList<>();
		for (FeyStoneMaterial mat : FeyStoneMaterial.values()) {
			if (SpecializationMaterialAllowed(type, mat)) {
				mats.add(mat);
			}
		}
		
		return mats.toArray(new FeyStoneMaterial[Math.max(1, mats.size())]);
	}
}
