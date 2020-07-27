package com.smanzana.nostrumfairies.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskMineBlock;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.OreDict;
import com.smanzana.nostrummagica.blocks.EssenceOre;
import com.smanzana.nostrummagica.blocks.ManiOre;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class MiningBlock extends BlockContainer {
	
	public static final String ID = "logistics_mining_block";
	public static final int WORKER_REACH = 16;
	public static final int MAJOR_LEVEL_DIFF = 16;
	
	private static MiningBlock instance = null;
	public static MiningBlock instance() {
		if (instance == null)
			instance = new MiningBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(MiningBlockTileEntity.class, "logistics_mining_block_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public MiningBlock() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	public static class MiningBlockTileEntity extends LogisticsTileEntity implements ITickable{

		private int tickCount;
		private Map<BlockPos, LogisticsTaskMineBlock> taskMap;
		private LogisticsItemWithdrawRequester materialRequester; // TODO initialize! lol
		private int radius;
		
		private ItemStack buildingMaterial;
		// TODO persist this!!!
		
		private int chunkXOffset; // rediscovered each time
		private int chunkZOffset; // ^
		private int lowestLevel; // ^
		private int scanLevel; // reset to 0 on load but that should mean 1-time long scan
		private int nextPlatform; // ^
		
		public MiningBlockTileEntity() {
			this(32);
		}
		
		public MiningBlockTileEntity(int blockRadius) {
			super();
			taskMap = new HashMap<>();
			this.radius = blockRadius;
			
			lowestLevel = -1;
			scanLevel = 1;
		}
		
		@Override
		public double getDefaultLogisticsRange() {
			return radius;
		}

		@Override
		public double getDefaultLinkRange() {
			return 10;
		}

		@Override
		public boolean canAccept(List<ItemDeepStack> stacks) {
			return false;
		}
		
		private void makeMineTask(BlockPos pos) {
			// TESTING
			//worldObj.setBlockToAir(pos);
			
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			if (!taskMap.containsKey(pos)) {
				LogisticsTaskMineBlock task = new LogisticsTaskMineBlock(this.getNetworkComponent(), "Mining Task", worldObj, pos);
				this.taskMap.put(pos, task);
				network.getTaskRegistry().register(task, null);
			}
		}
		
		private void makeRepairTask(BlockPos pos) {
			// Actually need two tasks: one to request the material, and one to go place it
			
			// so make some other list that is our item list and update requester.
			// and hook up to the logistics interface to expose those items to dwarves
			
			// Putting on hold until the digging part is done
//			broke() {
//				/*
//				 * Need to make a repair task (including waiting) to place missing blocks.
//				 * And need to hook up requester.
//				 * And then need to make platform creation code (makeMine())
//				 * And then shaft creation code.
//				 */
//			}
			worldObj.setBlockState(pos, Blocks.LOG.getDefaultState());
		}
		
		private void removeTask(BlockPos base) {
			LogisticsTaskMineBlock task = taskMap.remove(base);
			if (task == null) {
				// We call this freely from event handling. Ignore it.
				return;
			}
			
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			network.getTaskRegistry().revoke(task);
		}
		
		private int getYFromLevel(int level) {
			return pos.getY() - level;
		}
		
		private int levelFromY(int y) {
			return Math.max(0, pos.getY() - y);
		}
		
		private int platformToY(int platform) {
			return getYFromLevel(platformToLevel(platform));
		}
		
		private int platformToLevel(int platform) {
			return platform * (MAJOR_LEVEL_DIFF / 2);
		}
		
		/**
		 * Get the PLATFORM level (not the regular level) for the given Y
		 * @param y
		 * @return
		 */
		private int platformForY(int y) {
			// each platform is worth MAJOR_LEVEL_DIFF / 2
			// We want the one below it, but sometimes we can't
			final int lowest = getLowestLevel();
			if (lowest == 0) {
				return 0;
			}
			
			int platform = (int) Math.ceil((pos.getY() - y) / (double) (MAJOR_LEVEL_DIFF / 2));
			int level = platformToLevel(platform); 
			
			// if platform is unavailable, do the next one up
			if (level > lowest) {
				platform -= 1;
			}
			
			return platform;
		}
		
		private boolean forEachOnLayer(int level, Function<MutableBlockPos, Boolean> func) {
			final int startX = this.pos.getX() + chunkXOffset - radius;
			final int endX = this.pos.getX() + chunkXOffset + radius;
			final int startZ = this.pos.getZ() + chunkZOffset - radius;
			final int endZ = this.pos.getZ() + chunkZOffset + radius;
			final int y = getYFromLevel(level);
			
			MutableBlockPos cursor = new MutableBlockPos();
			for (int x = startX; x <= endX; x++)
			for (int z = startZ; z <= endZ; z++) {
				cursor.setPos(x, y, z);
				if (!func.apply(cursor)) {
					return false;
				}
			}
			
			return true;
		}
		
		/**
		 * Makes sure the provided location is a 1x3x1 space with a block underneath
		 * @param base
		 */
		private boolean clearBlock(BlockPos base, boolean tall) {
			// should base be the block underneath or the first air block??
			
			base = base.toImmutable();
			boolean tasked = false;
			
			// check underneath
			if (!worldObj.isSideSolid(base.down(), EnumFacing.UP)) {
				makeRepairTask(base.down());
				tasked = true;
			}
			
			// check for 3-high air
			if (!worldObj.isAirBlock(base)) {
				makeMineTask(base);
				tasked = true;
			}
			if (!worldObj.isAirBlock(base.up())) {
				makeMineTask(base.up());
				tasked = true;
			}
			if (tall) {
				if (!worldObj.isAirBlock(base.up().up())) {
					makeMineTask(base.up().up());
					tasked = true;
				}
			}
			
			return tasked;
		}
		
		/**
		 * Create the shafts required to get to where we can mine the provided pos
		 * @param pos
		 */
		private void mineTo(BlockPos pos) {
			
		}
		
		/**
		 * Scan a layer and queue up any tasks to grab ores.
		 * If nothing worth mining is found, returns false. Otherwise, returns true.
		 * @param y
		 * @return
		 */
		private boolean scanLayer(int level) {
			boolean[] result = new boolean[]{false};
			forEachOnLayer(getYFromLevel(level), (pos) -> {
				// Is this something we should mine?
				if (IsOre(worldObj, pos)) {
					mineTo(pos);
					result[0] = true;
				}
				
				return true;
			});
			
			// TODO testing! do not leave this in!!!!!!!!!
			return false;
			//return result[0];
		}
		
		private boolean makePlatform(int level, boolean upper) {
			// 17x17 ring with a doorway to the north (or south if !whole) that leads in
			final int width = 3;
			final int innerRadius = 4;
			final int bounds = innerRadius + width;
			MutableBlockPos cursor = new MutableBlockPos();
			boolean clear = true;
			final int y = getYFromLevel(level);
			
			for (int i = -bounds; i <= bounds; i++)
			for (int j = -bounds; j <= bounds; j++) {
				// east or west ( i == +-bounds)? we fill in all j for these i
				// north or south only clear top and bottom
				if ((i >= -bounds && i <= -bounds + width)
					|| (i >= bounds - width && i <= bounds)
					|| (j >= -bounds && j <= -bounds + width)
					|| (j >= bounds - width && j <= bounds)) {
					// Note: we burrow centered on the actual TE but then shift our scan and
					// mine area to chunk boundaries. So these x and z are not shifted.
					final int x = pos.getX() + i;
					final int z = pos.getZ() + j;
					
					cursor.setPos(x, y, z);
					if (clearBlock(cursor, false)) {
						clear = false;
					}
				} 
			}
			
			// Finally, the door
			cursor.setPos(0 + pos.getX(), y, pos.getZ() + (upper ? (innerRadius - 1) : -(innerRadius - 1)));
			if (clearBlock(cursor, false)) {
				clear = false;
			}
			
			// Make staircase in front of door into a beacon
			cursor.setPos(0 + pos.getX(), y, pos.getZ() + (upper ? (innerRadius - 2) : -(innerRadius - 2)));
			this.getNetwork().addBeacon(worldObj, cursor.toImmutable());
			
			return !clear;
		}
		
		/**
		 * Scans and queues up task for the next stairway segment, starting at the level provided.
		 * The first block dug is actually on level+1.
		 * This should not be called if we can't get all the way to the next platform level.
		 * @param level
		 * @return
		 */
		private boolean makeStaircaseSegment(int level, boolean upper) {
			MutableBlockPos cursor = new MutableBlockPos();
			boolean clear = true;
			
			// 5x5 spiral staircase 'starting' to the north and descending 16 blocks in
			// one spiral.
			final int perimeter = MAJOR_LEVEL_DIFF;
			// we always go one-half of a side to start
			final int edgeLength = perimeter / 4;
			final int startOffset = edgeLength / 2;
			
			final int startIdx = (upper ? 0 : MAJOR_LEVEL_DIFF / 2);
			final int endIdx = (upper ? MAJOR_LEVEL_DIFF / 2 : MAJOR_LEVEL_DIFF);
			cursor.setPos(pos.getX(), getYFromLevel(level), pos.getZ() + (upper ? -startOffset : startOffset));
			for (int i = startIdx; i < endIdx; i++) {
				EnumFacing side = i < startOffset ? EnumFacing.NORTH
						: (i < startOffset + edgeLength ? EnumFacing.EAST 
						: (i < startOffset + edgeLength + edgeLength ? EnumFacing.SOUTH
						: (i < startOffset + edgeLength + edgeLength + edgeLength ? EnumFacing.WEST
						: EnumFacing.NORTH)));
				switch (side) {
				case NORTH:
					cursor.move(EnumFacing.EAST);
					break;
				case EAST:
					cursor.move(EnumFacing.SOUTH);
					break;
				case SOUTH:
					cursor.move(EnumFacing.WEST);
					break;
				case WEST:
				case UP:
				case DOWN:
				default:
					cursor.move(EnumFacing.NORTH);
					break;
				}
				cursor.move(EnumFacing.DOWN);
				
				if (clearBlock(cursor, true)) {
					clear = false;
				}
			}
			
			// Make special first-time door if needed?
			// Make this should make a top-level platform instead?
			// TODO
			
			return !clear;
		}
		
		/**
		 * Check and queue up tasks to dig the mine.
		 * If the mine is already dug and looks good, returns false.
		 * @return
		 */
		private boolean makeMine(int platformToMake) {
			// mine holes start to the north.
			// mine is a 5x5 spiral staircase all the way down, with platforms every (MAJOR_LEVEL_DIFF / 2) blocks.
			// note: this is a place where MAJOR_LEVEL_DIFF isn't dynamic.  It needs to be 16 to fit the 5x5.
			
			// Each platform is a 9x9 ring at the same y level connected to the north part of the spiral with a 1x3x1 doorway.
			
			// Each spot should be a 1x3x1 hole. Below it should be a solid block.
			final int deepest = this.getLowestLevel();
			if (deepest == 0) {
				return false;
			}
			
			boolean dug = false;
			int level = platformToLevel(platformToMake);
			if (level < deepest) {
				boolean upper = (level % MAJOR_LEVEL_DIFF == 0);
				if (makeStaircaseSegment(level, upper)) {
					dug = true;
				}
				level += MAJOR_LEVEL_DIFF / 2;
				if (makePlatform(level, upper)) {
					dug = true;
				}
			}
			
			return dug;
		}
		
		/**
		 * Looks at platforms up to the provided one and queues up any repair tasks
		 * that are needed. Pesky gravel!
		 * @param deepestPlatform
		 * @return
		 */
		private boolean repairMine(int deepestPlatform) {
			boolean dug = false;
			for (int platform = 0; platform <= deepestPlatform; platform++) {
				dug = makeMine(platform) || dug;
			}
			return dug;
		}
		
		/**
		 * Get the lowest level the mine will go. Note this is level, not Y value.
		 * A return of 0 indicates that the mine cannot actually be built, as unbreakables
		 * are getting in the way.
		 * @return
		 */
		public int getLowestLevel() {
			// TODO this should also look in 5x5 (or 9x9 or 13x13?) block down to see if unmineables get in the way?
			
			if (lowestLevel == -1) {
				int y = this.pos.getY();
				lowestLevel = 0;
				while (y > MAJOR_LEVEL_DIFF) {
					y -= MAJOR_LEVEL_DIFF;
					// Check this layer for unbreakables/bedrock
					if (forEachOnLayer(y, (pos) -> {
						IBlockState state = worldObj.getBlockState(pos);
						return state.getBlockHardness(worldObj, pos) >= 0;
					})) {
						lowestLevel += MAJOR_LEVEL_DIFF;
					} else {
						break;
					}
				}
			}
			
			return lowestLevel;
		}
		
		public int getLowestReachableY() {
			int lowestLevel = getLowestLevel();
			if (lowestLevel != 0) {
				return Math.max(0, getYFromLevel(lowestLevel) - WORKER_REACH);
			} else {
				return pos.getY();
			}
		}
		
		private void repairScan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			if (getLowestLevel() == 0) {
				return;
			}
			final long startTime = System.currentTimeMillis();
			if (nextPlatform > 0) {
				repairMine(nextPlatform - 1);
			}
			final long end = System.currentTimeMillis();
			if (end - startTime >= 5) {
				System.out.println("Took " + (end - startTime) + "ms to scan for repairs!");
			}
		}
		
		private void scan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			if (scanLevel > getLowestLevel() || getLowestLevel() == 0) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			boolean didWork = false;
			while (didWork == false) {
				// If scan level is behind platform, scan. Otherwise, make a platform.
				// Keep doing both if they run and find no work to do. Could limit this here. Once per minute seems long tho.
				if (nextPlatform == 0 || scanLevel > platformToLevel(nextPlatform)) {
					didWork = makeMine(nextPlatform);
					nextPlatform++;
				}
				
				if (!didWork) {
					didWork = scanLayer(scanLevel++);
				}
			}
			
			final long end = System.currentTimeMillis();
			if (end - startTime >= 5) {
				System.out.println("Took " + (end - startTime) + "ms to scan for ores!");
			}
		}
		
		@Override
		public void update() {
			if (this.worldObj.isRemote) {
				return;
			}
			
			int period = (scanLevel < getLowestLevel()) ? 60 : 180;
			if (this.tickCount % (20) == 0) {
				repairScan();
			}
			if (this.tickCount % (20 * period) == 0) {
				scan();
			}
			this.tickCount++;
		}
		
		@SubscribeEvent
		public void onBreak(BlockEvent.BreakEvent e) {
			if (e.isCanceled()) {
				return;
			}
			
			if (e.getWorld() == this.worldObj) {
				removeTask(e.getPos());
			}
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			if (!worldIn.isRemote) {
				MinecraftForge.EVENT_BUS.register(this);
			}
		}
		
		@Override
		public void setPos(BlockPos posIn) {
			super.setPos(posIn);
			
			chunkXOffset = -((posIn.getX() - radius) & 0xF); // lowest 16 values
			chunkZOffset = -((posIn.getZ() - radius) & 0xF);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class MiningBlockRenderer extends TileEntityLogisticsRenderer<MiningBlockTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(MiningBlockTileEntity.class,
					new MiningBlockRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntity ent = new MiningBlockTileEntity();
		return ent;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof MiningBlockTileEntity))
			return;
		
		MiningBlockTileEntity block = (MiningBlockTileEntity) ent;
		block.unlinkFromNetwork();
		MinecraftForge.EVENT_BUS.unregister(block);
	}
	
	public static boolean IsOre(World world, BlockPos pos) {
		InitExtras();
		
		if (world.isAirBlock(pos)) {
			return false;
		}
		
		IBlockState state = world.getBlockState(pos);
		if (state.getBlockHardness(world, pos) < 0) {
			// unbreakable
			return false;
		}
		
		// See if it's one of the known ores
		Block block = state.getBlock();
		if (block == Blocks.COAL_ORE
			|| block == Blocks.DIAMOND_ORE
			|| block == Blocks.EMERALD_ORE
			|| block == Blocks.GOLD_ORE
			|| block == Blocks.IRON_ORE
			|| block == Blocks.LAPIS_ORE
			|| block == Blocks.LIT_REDSTONE_ORE
			|| block == Blocks.QUARTZ_ORE
			|| block == Blocks.REDSTONE_ORE
			|| block == EssenceOre.instance()
			|| block == ManiOre.instance()
				) {
			return true;
		}
		
		// Check if it's in our extra list
		// First, try ore dictionary
		if (OreDict.blockMatchesAny(state, ExtraCachedArray, false)) {
			return true;
		}
		
		// Then try to see if any are registry names
		String registryName = block.getRegistryName().toString();
		for (String extra : ExtraOres) {
			if (extra.compareToIgnoreCase(registryName) == 0) {
				return true;
			}
		}
		
		return true;
	}
	
	private static Set<String> ExtraOres = null;
	private static String[] ExtraCachedArray = null;
	
	private static void InitExtras() {
		ExtraOres = new HashSet<>();
		
		// Vanilla entries
		ExtraOres.add("oreGold");
		ExtraOres.add("oreIron");
		ExtraOres.add("oreLapis");
		ExtraOres.add("oreDiamond");
		ExtraOres.add("oreRedstone");
		ExtraOres.add("oreEmerald");
		ExtraOres.add("oreQuartz");
		ExtraOres.add("oreCoal");
		
		// Popular mod entries
		ExtraOres.add("oreCopper");
		ExtraOres.add("oreAluminum");
		ExtraOres.add("oreLead");
		ExtraOres.add("oreSteel");
		ExtraOres.add("oreTin");
		ExtraOres.add("oreBronze");
		
		// And, in a ditch effort, look through ore dictionary
		for (String entry : OreDictionary.getOreNames()) {
			if (entry.startsWith("ore")) {
				ExtraOres.add(entry);
			}
		}
		
		ExtraCachedArray = ExtraOres.toArray(new String[ExtraOres.size()]);
	}
	
	public static void AddOreName(String registryOrDictionary) {
		InitExtras();
		ExtraOres.add(registryOrDictionary);
		ExtraCachedArray = ExtraOres.toArray(new String[ExtraOres.size()]);
	}
}
