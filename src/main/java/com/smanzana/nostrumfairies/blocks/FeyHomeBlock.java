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

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ToolType;

/**
 * Houses fey.
 * Has all the bed and satisfaction and recruiting and job management stuff in it.
 * @author Skyler
 *
 */
public class FeyHomeBlock extends FeyContainerBlock {
	
	private static enum BlockFunction implements StringRepresentable {
		CENTER,
		TOP;

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.getSerializedName();
		}
	}
	
	//public static final PropertyEnum<ResidentType> TYPE = PropertyEnum.<ResidentType>create("type", ResidentType.class);
	public static final EnumProperty<BlockFunction> BLOCKFUNC = EnumProperty.<BlockFunction>create("func", BlockFunction.class);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	
	protected static final String ID_PREFIX = "home_block_";
	public static final String ID_DWARF = ID_PREFIX + "dwarf";
	public static final String ID_ELF = ID_PREFIX + "elf";
	public static final String ID_GNOME = ID_PREFIX + "gnome";
	public static final String ID_FAIRY = ID_PREFIX + "fairy";
	
	private final ResidentType type;
	
	public FeyHomeBlock(ResidentType type) {
		super(Block.Properties.of(Material.WOOD)
				.strength(4f, 100.0f)
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
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
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		if (state == null || !(state.getBlock() instanceof FeyHomeBlock)) {
			return InteractionResult.FAIL;
		}
		
		BlockPos center = ((FeyHomeBlock) state.getBlock()).getMasterPos(worldIn, pos, state);
		BlockEntity te = worldIn.getBlockEntity(center);
		if (te == null || !(te instanceof HomeBlockTileEntity)) {
			return InteractionResult.FAIL;
		}
		
		NostrumMagica.instance.proxy.openContainer(playerIn, HomeBlockGui.HomeBlockContainer.Make((HomeBlockTileEntity) te));
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	public boolean isCenter(BlockState state) {
		return state.getValue(BLOCKFUNC) == BlockFunction.CENTER;
	}
	
	public ResidentType getType(BlockState state) {
		return type;
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent != null && ent instanceof HomeBlockTileEntity) {
			HomeBlockTileEntity te = (HomeBlockTileEntity) ent;
			te.unlinkFromNetwork();
			
			// Slot inventory and upgrade inventories
			for (Container inv : new Container[] {te.getSlotInventory(), te.getUpgradeInventory()}) {
				for (int i = 0; i < inv.getContainerSize(); i++) {
					if (!inv.getItem(i).isEmpty()) {
						ItemEntity item = new ItemEntity(
								world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
								inv.removeItemNoUpdate(i));
						world.addFreshEntity(item);
					}
				}
			}
		}
		world.removeBlockEntity(pos);
		
		world.destroyBlock(getPaired(state, pos), true);
	}
	
	private BlockPos getPaired(BlockState state, BlockPos pos) {
		return pos.relative(state.getValue(BLOCKFUNC) == BlockFunction.CENTER ? Direction.UP : Direction.DOWN);
	}
	
	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState oldState) {
		this.destroy(world, pos, oldState);
		world.removeBlockEntity(pos);
		super.breakBlock(world, pos, oldState);
	}
	
	@Override
	public boolean canSurvive(BlockState stateIn, LevelReader worldIn, BlockPos pos) {
		
		for (BlockPos cursor : new BlockPos[] {pos, pos.above(), pos.above().above()}) {
			if (cursor.getY() > 255 || cursor.getY() <= 0) {
				return false;
			}
			
			BlockState state = worldIn.getBlockState(cursor);
			Block block = state.getBlock();
			if (!state.getMaterial().isReplaceable()
					&& !(block instanceof LeavesBlock)) {
				return false;
			}
			
			if (worldIn.getBlockEntity(cursor) != null) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
				.setValue(BLOCKFUNC, BlockFunction.CENTER)
				.setValue(FACING, context.getHorizontalDirection().getOpposite());
				
	}
	
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// This method hopefully is ONLY called when placed manually in the world.
		// Auto-create slave state
		Direction facing = state.getValue(FACING);
		this.spawn(worldIn, pos, type, facing);
		
//		worldIn.setBlockState(pos.up(), this.getDefaultState()
//				.with(BLOCKFUNC, BlockFunction.TOP));
	}
	
	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		if (isCenter(state)) {
			return new HomeBlockTileEntity(type);
		}
		
		return null;
	}
	
	public BlockPos getMasterPos(Level world, BlockPos pos, BlockState state) {
		if (state == null) {
			state = world.getBlockState(pos);
		}
		
		if (isCenter(state)) {
			return pos;
		}
		return pos.below();
	}
	
	/**
	 * Spawns a home block tree at the provided space. Skips the base block.
	 * @param world
	 * @param base
	 * @param type
	 */
	public void spawn(Level world, BlockPos base, ResidentType type, Direction direction) {
		//world.setBlockState(base, getDefaultState().with(BLOCKFUNC, BlockFunction.CENTER).with(FACING, direction));
		world.setBlockAndUpdate(base.above(), defaultBlockState().setValue(BLOCKFUNC, BlockFunction.TOP).setValue(FACING, direction)); // could be random
		world.setBlockAndUpdate(base.above().above(), Blocks.DARK_OAK_LOG.defaultBlockState());
		
		BlockState leaves = Blocks.DARK_OAK_LEAVES.defaultBlockState();
		BlockPos origin = base.above().above();
		for (BlockPos cursor : new BlockPos[] {
				// Layer 1
				origin.north().north(),
				origin.north().east(), origin.north(), origin.north().west(),
				origin.east().east(), origin.east(), origin.west(), origin.west().west(),
				origin.south().east(), origin.south(), origin.south().west(),
				origin.south().south(),
				
				// Layer 2
				origin.above().north().east(), origin.above().north(), origin.above().north().west(),
				origin.above().east().east(), origin.above().east(), origin.above(), origin.above().west(), origin.above().west().west(),
				origin.above().south().east(), origin.above().south(), origin.above().south().west(),
				
				// Layer 3
				origin.above().above().north(),
				origin.above().above().east(), origin.above().above(), origin.above().above().west(),
				origin.above().above().south(),
		}) {
			world.setBlockAndUpdate(cursor, leaves);
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
