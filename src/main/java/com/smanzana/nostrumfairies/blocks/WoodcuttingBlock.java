package com.smanzana.nostrumfairies.blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskChopTree;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.OreDict;

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
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WoodcuttingBlock extends BlockContainer {
	
	public static final String ID = "logistics_woodcutting_block";
	
	private static WoodcuttingBlock instance = null;
	public static WoodcuttingBlock instance() {
		if (instance == null)
			instance = new WoodcuttingBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(WoodcuttingBlockTileEntity.class, "logistics_woodcutting_block_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public WoodcuttingBlock() {
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
	
	public static class WoodcuttingBlockTileEntity extends LogisticsTileEntity implements ITickable{

		private int tickCount;
		private Map<BlockPos, LogisticsTaskChopTree> taskMap;
		private double radius;
		
		public WoodcuttingBlockTileEntity() {
			this(10);
		}
		
		public WoodcuttingBlockTileEntity(double blockRadius) {
			super();
			taskMap = new HashMap<>();
			this.radius = blockRadius;
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
		
		private void makeTask(BlockPos base) {
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			LogisticsTaskChopTree task = new LogisticsTaskChopTree(this.getNetworkComponent(), "Tree Chop Task", worldObj, base);
			this.taskMap.put(base, task);
			network.getTaskRegistry().register(task, null);
		}
		
		private void removeTask(BlockPos base) {
			LogisticsTaskChopTree task = taskMap.remove(base);
			if (task == null) {
				// wut
				return;
			}
			
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			network.getTaskRegistry().revoke(task);
		}
		
		private void scan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			// Look for trees nearby and record their base. Also mark off ones we already know about.
			Set<BlockPos> known = Sets.newHashSet(taskMap.keySet());
			List<BlockPos> trunks = new LinkedList<>();
			
			MutableBlockPos pos = new MutableBlockPos();
			BlockPos center = this.getPos();
			final int startY = (int) Math.max(-pos.getY(), Math.floor(-radius));
			final int endY = (int) Math.min(256 - pos.getY(), Math.ceil(radius));
			for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++)
			for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++)
			for (int y = startY; y < endY; y++) {
				
				pos.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
				if (!worldObj.isBlockLoaded(pos)) {
					break; // skip this whole column
				}
				
				if (isTree(worldObj, pos)) {
					// Record!
					// Don't make task cause we filter better later
					trunks.add(pos.toImmutable());
					
					// climb tree to avoid checking each piece above it
					do {
						pos.move(EnumFacing.UP);
						y++;
					} while (y < (endY - 1) && isTrunkMaterial(worldObj, pos));
				}
			}
			
			// For trunks we found, see if they are next to other trunks and ignore them if so!
			// This could see that two pos's are XZ 1 away and then climb trunks to see if they touch
			// but NAH
			if (!trunks.isEmpty()) {
				final int yRange = 4;
				BlockPos[] neighbors = new BlockPos[trunks.size() - 1];
				for (int i = 0; i < trunks.size(); i++) { // not enhanced since we will be modifying!
					// Find all other trunks that are XZ 1 away and within some Y range.
					BlockPos trunk = trunks.get(i);
					int neighborIdx = 0;
					
					// Gotta keep doing this until no new neighbors are found :(
					boolean foundOne;
					do {
						foundOne = false;
						for (int j = trunks.size() - 1; j > i; j--) { // backwards so we can remove and not worry
							BlockPos candidate = trunks.get(j);
							// If this pos is near the original or any neighbors, add it to neighbor and remove
							// Check trunk
							if (Math.abs(trunk.getY() - candidate.getY()) <= yRange
								&& Math.abs(trunk.getX() - candidate.getX()) <= 1
								&& Math.abs(trunk.getZ() - candidate.getZ()) <= 1) {
								// oops it's a neighbor
								neighbors[neighborIdx++] = candidate;
								trunks.remove(j);
								foundOne = true;
								continue;
							}
							
							// Repeat the above for all neighbors
							for (int n = 0; n < neighborIdx; n++) {
								BlockPos neighbor = neighbors[n];
								if (Math.abs(candidate.getY() - neighbor.getY()) <= yRange
										&& Math.abs(candidate.getX() - neighbor.getX()) <= 1
										&& Math.abs(candidate.getZ() - neighbor.getZ()) <= 1) {
										// oops it's a neighbor
										neighbors[neighborIdx++] = candidate;
										trunks.remove(j);
										foundOne = true;
										break;
									}
							}
						}
					} while (foundOne);
				}
				
				// New, condensed trunk list should be converted to tasks.
				// I think this all should work, since it should be deterministic...
				for (BlockPos base : trunks) {
					if (known.remove(base)) {
						; // We already knew about it, so don't create a new one
					} else {
						// Didn't know, so record!
						// Don't make task cause we filter better later
						makeTask(base);
					}
					
				}
			}
			
			// For any left in known, the tree is not there anymore! Remove!
			for (BlockPos base : known) {
				removeTask(base);
			}
			
			final long end = System.currentTimeMillis();
			if (end - startTime >= 5) {
				System.out.println("Took " + (end - startTime) + "ms to scan for trees!");
			}
		}
		
		@Override
		public void update() {
			if (this.worldObj.isRemote) {
				return;
			}
			
			this.tickCount++;
			if (this.tickCount % 100 == 0) {
				scan();
			}
		}
		
		private boolean inRange(BlockPos pos) {
			// Max distance with a square radius of X is X times sqrt(3).
			// sqrt(3) is ~1.7321
			
			// Idk if this is actually faster than 6 conditionals.
			return this.getDistanceSq(pos.getX(), pos.getY(), pos.getZ()) <=
						Math.pow(radius * 1.7321, 2);
		}
		
		@SubscribeEvent
		public void onGrow(SaplingGrowTreeEvent e) {
			 if (e.getWorld() == this.worldObj && inRange(e.getPos())) {
				 this.scan();
			 }
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			if (!worldIn.isRemote) {
				MinecraftForge.TERRAIN_GEN_BUS.register(this);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class WoodcuttingBlockRenderer extends TileEntityLogisticsRenderer<WoodcuttingBlockTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(WoodcuttingBlockTileEntity.class,
					new WoodcuttingBlockRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntity ent = new WoodcuttingBlockTileEntity();
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
		if (ent == null || !(ent instanceof WoodcuttingBlockTileEntity))
			return;
		
		WoodcuttingBlockTileEntity block = (WoodcuttingBlockTileEntity) ent;
		block.unlinkFromNetwork();
		MinecraftForge.TERRAIN_GEN_BUS.unregister(block);
	}
	
	public static boolean isLeafMaterial(IBlockState state) {
		
		// Easy fast check
		if (state.getBlock() == Blocks.LEAVES
				|| state.getBlock() == Blocks.LEAVES2) {
			return true;
		}
		
		return OreDict.blockMatchesOreDict(state, "treeLeaves", false);
	}
	
	public static boolean isLeafMaterial(World world, BlockPos pos) {
		return isLeafMaterial(world.getBlockState(pos));
	}
	
	public static boolean isTrunkMaterial(IBlockState state) {
		
		// Easy fast check
		if (state.getBlock() == Blocks.LOG
				|| state.getBlock() == Blocks.LOG2) {
			return true;
		}
		
		return OreDict.blockMatchesOreDict(state, "logWood", false);
	}
	
	public static boolean isTrunkMaterial(World world, BlockPos pos) {
		return isTrunkMaterial(world.getBlockState(pos));
	}
	
	public static boolean isTree(World world, BlockPos base) {
		// First, check if current block is even trunk material
		if (!isTrunkMaterial(world, base)) {
			return false;
		}
		
		// Then, check if there's also trunk material below. We only count the base.
		if (isTrunkMaterial(world, base.down())) {
			return false;
		}
		
		// A tree must be on the ground
		if (!world.isSideSolid(base.down(), EnumFacing.UP)) {
			return false;
		}
		
		// A tree must be topped with leaves
		MutableBlockPos pos = new MutableBlockPos(base);
		do {
			pos.move(EnumFacing.UP);
		} while ((pos.getY() < 255) && isTrunkMaterial(world, pos));
		
		if (!isLeafMaterial(world, pos)) {
			return false;
		}
		
		return true;
	}
}
