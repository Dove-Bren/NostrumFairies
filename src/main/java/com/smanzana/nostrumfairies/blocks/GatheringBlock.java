package com.smanzana.nostrumfairies.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.render.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskPickupItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GatheringBlock extends BlockContainer {
	
	public static final String ID = "logistics_gathering_block";
	
	private static GatheringBlock instance = null;
	public static GatheringBlock instance() {
		if (instance == null)
			instance = new GatheringBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(GatheringBlockTileEntity.class, "logistics_gathering_block_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public GatheringBlock() {
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
	
	public static class GatheringBlockTileEntity extends LogisticsTileEntity implements ITickable{

		private int tickCount;
		private Map<EntityItem, LogisticsTaskPickupItem> taskMap;
		private double radius;
		
		private AxisAlignedBB boxCache;
		private double radiusCache;
		
		public GatheringBlockTileEntity() {
			this(10);
		}
		
		public GatheringBlockTileEntity(double blockRadius) {
			super();
			taskMap = new HashMap<>();
			this.radius = blockRadius;
		}
		
		@Override
		public double getDefaultLogisticsRange() {
			return 10;
		}

		@Override
		public double getDefaultLinkRange() {
			return 10;
		}

		@Override
		public boolean canAccept(List<ItemDeepStack> stacks) {
			return false;
		}
		
		private void makeTask(EntityItem item) {
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			LogisticsTaskPickupItem task = new LogisticsTaskPickupItem(this.getNetworkComponent(), "Item Pickup Task", item);
			this.taskMap.put(item, task);
			network.getTaskRegistry().register(task, null);
		}
		
		private void removeTask(EntityItem item) {
			LogisticsTaskPickupItem task = taskMap.remove(item);
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
			// Update BB cache if needed
			if (boxCache == null || radiusCache != radius) {
				boxCache = new AxisAlignedBB(this.pos).expandXyz(radius);
				this.radiusCache = radius;
			}
			
			// Check items on the ground nearby and create/destroy any tasks needed
			List<EntityItem> items = this.worldObj.getEntitiesWithinAABB(EntityItem.class, boxCache);
			Set<EntityItem> known = Sets.newHashSet(taskMap.keySet());
			for (EntityItem item : items) {
				if (known.remove(item)) {
					// we knew about that item.
					continue;
				}
				
				// else this is an unknown item
				makeTask(item);
			}
			
			// For any left in known, the item is not there anymore! Remove!
			for (EntityItem item : known) {
				// Ignore any tasks that don't have entities anymore but that's because the worker
				// picked it up
				if (taskMap.get(item).hasTakenItems()) {
					continue;
				}
				
				removeTask(item);
			}
		}
		
		@Override
		public void update() {
			if (this.worldObj.isRemote) {
				return;
			}
			
			this.tickCount++;
			if (this.tickCount % 20 == 0) {
				scan();
			}
		}
		
		private boolean inRange(EntityItem e) {
			// Max distance with a square radius of X is X times sqrt(3).
			// sqrt(3) is ~1.7321
			
			// Idk if this is actually faster than 6 conditionals.
			return (e.worldObj == this.worldObj &&
					this.getDistanceSq(e.posX, e.posY, e.posZ) <=
						Math.pow(radius * 1.7321, 2));
		}
		
		@SubscribeEvent
		public void onPickup(EntityItemPickupEvent e) {
			 if (inRange(e.getItem())) {
				 this.scan();
			 }
		}
		
		@SubscribeEvent
		public void onToss(ItemTossEvent e) {
			 if (inRange(e.getEntityItem())) {
				 this.scan();
			 }
		}
		
		@SubscribeEvent
		public void onExpire(ItemExpireEvent e) {
			 if (inRange(e.getEntityItem())) {
				 this.scan();
			 }
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			if (!worldIn.isRemote) {
				MinecraftForge.EVENT_BUS.register(this);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class GatheringBlockRenderer extends TileEntityLogisticsRenderer<GatheringBlockTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(GatheringBlockTileEntity.class,
					new GatheringBlockRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntity ent = new GatheringBlockTileEntity();
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
		if (ent == null || !(ent instanceof GatheringBlockTileEntity))
			return;
		
		GatheringBlockTileEntity block = (GatheringBlockTileEntity) ent;
		block.unlinkFromNetwork();
		MinecraftForge.EVENT_BUS.unregister(block);
	}
}
