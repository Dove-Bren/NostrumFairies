package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.entity.fey.EntityFeyBase;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.tiles.HomeBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Houses fey.
 * Has all the bed and satisfaction and recruiting and job management stuff in it.
 * @author Skyler
 *
 */
public class FeyHomeBlock extends Block implements ITileEntityProvider {
	
	public static enum ResidentType implements IStringSerializable {
		FAIRY,
		ELF,
		DWARF,
		GNOME;

		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.getName();
		}
	}
	
	private static enum BlockFunction implements IStringSerializable {
		CENTER,
		TOP;

		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		
		@Override
		public String toString() {
			return this.getName();
		}
	}
	
	//public static final PropertyEnum<ResidentType> TYPE = PropertyEnum.<ResidentType>create("type", ResidentType.class);
	public static final PropertyEnum<BlockFunction> BLOCKFUNC = PropertyEnum.<BlockFunction>create("func", BlockFunction.class);
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	protected static final String ID = "home_block";
	
	private static Map<ResidentType, FeyHomeBlock> instances = null;
	
	public static FeyHomeBlock instance(ResidentType type) {
		if (instances == null) {
			instances = new EnumMap<>(ResidentType.class);
			for (ResidentType t : ResidentType.values()) {
				instances.put(t, new FeyHomeBlock(t));
			}
		}
		
		return instances.get(type);
	}
	
	public static String ID(ResidentType type) {
		return ID + "_" + type.name().toLowerCase();
	}
	
	private final ResidentType type;
	
	private FeyHomeBlock(ResidentType type) {
		super(Material.WOOD, MapColor.BLUE);
		this.setUnlocalizedName(ID(type));
		this.setHardness(0.0f);
		this.setResistance(100.0f);
		this.setLightOpacity(0);
		this.setTickRandomly(true);
		this.setCreativeTab(NostrumFairies.creativeTab);
		
		this.type = type;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BLOCKFUNC, FACING);
	}
	
	private BlockFunction functionFromMeta(int meta) {
		int raw = (meta & 1);
		return BlockFunction.values()[raw];
	}
	
	private int metaFromFunc(BlockFunction func) {
		return (func.ordinal() & 1);
	}
	
	private Direction facingFromMeta(int meta) {
		int raw = (meta >> 1) & 0x3;
		return Direction.HORIZONTALS[raw];
	}
	
	private int metaFromFacing(Direction facing) {
		return facing.getHorizontalIndex() << 1;
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(BLOCKFUNC, functionFromMeta(meta))
				.withProperty(FACING, facingFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return metaFromFunc(state.getValue(BLOCKFUNC)) | metaFromFacing(state.getValue(FACING));
	}
	
	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return super.getItemDropped(state, rand, fortune);
		//return null;
    }
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (state == null || !(state.getBlock() instanceof FeyHomeBlock)) {
			return false;
		}
		
		BlockPos center = ((FeyHomeBlock) state.getBlock()).getMasterPos(worldIn, pos, state);
		TileEntity te = worldIn.getTileEntity(center);
		if (te == null || !(te instanceof HomeBlockTileEntity)) {
			return false;
		}
		
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.homeBlockID, worldIn,
				center.getX(), center.getY(), center.getZ());
		
		return true;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(BlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isFullBlock(BlockState state) {
		return true;
	}
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public void randomTick(World worldIn, BlockPos pos, BlockState state, Random random) {
		super.randomTick(worldIn, pos, state, random);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.randomDisplayTick(stateIn, worldIn, pos, rand);
	}
	
	@Override
	public boolean canSustainLeaves(BlockState state, IBlockAccess world, BlockPos pos) {
		return true;
	}
	
	public boolean isCenter(BlockState state) {
		return state.getValue(BLOCKFUNC) == BlockFunction.CENTER;
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
						EntityItem item = new EntityItem(
								world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
								inv.removeStackFromSlot(i));
						world.spawnEntity(item);
					}
				}
			}
		}
		world.removeTileEntity(pos);
		
		world.setBlockToAir(getPaired(state, pos));
	}
	
	private BlockPos getPaired(BlockState state, BlockPos pos) {
		return pos.offset(state.getValue(BLOCKFUNC) == BlockFunction.CENTER ? Direction.UP : Direction.DOWN);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		this.destroy(world, pos, state);
		world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		
		for (BlockPos cursor : new BlockPos[] {pos, pos.up(), pos.up().up()}) {
			if (cursor.getY() > 255 || cursor.getY() <= 0) {
				return false;
			}
			
			Block block = worldIn.getBlockState(cursor).getBlock();
			if (!block.isReplaceable(worldIn, cursor)
					&& !(block instanceof BlockLeaves)) {
				return false;
			}
			
			if (worldIn.getTileEntity(cursor) != null) {
				return false;
			}
		}
		
		return super.canPlaceBlockAt(worldIn, pos);
	}
	
	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return this.getDefaultState()
				.withProperty(BLOCKFUNC, BlockFunction.CENTER)
				.withProperty(FACING, placer.getHorizontalFacing().getOpposite());
				
	}
	
	@Override
	public int damageDropped(BlockState state) {
//		return metaFromType(state.getValue(TYPE));
		return 0;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		super.getSubBlocks(tab, list);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		// This method hopefully is ONLY called when placed manually in the world.
		// Auto-create slave state
		Direction facing = state.getValue(FACING);
		this.spawn(worldIn, pos, type, facing);
		
//		worldIn.setBlockState(pos.up(), this.getDefaultState()
//				.withProperty(BLOCKFUNC, BlockFunction.TOP));
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		BlockState state = this.getStateFromMeta(meta);
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
		//world.setBlockState(base, getDefaultState().withProperty(BLOCKFUNC, BlockFunction.CENTER).withProperty(FACING, direction));
		world.setBlockState(base.up(), getDefaultState().withProperty(BLOCKFUNC, BlockFunction.TOP).withProperty(FACING, direction)); // could be random
		world.setBlockState(base.up().up(), Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK));
		
		BlockState leaves = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK);
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
