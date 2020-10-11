package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.client.render.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.items.TemplateScroll;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskBuildBlock;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BuildingBlock extends BlockContainer {
	
	public static final String ID = "logistics_building_block";
	
	private static BuildingBlock instance = null;
	public static BuildingBlock instance() {
		if (instance == null)
			instance = new BuildingBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(BuildingBlockTileEntity.class, "logistics_building_block_te");
	}
	
	public BuildingBlock() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("pickaxe", 0);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		playerIn.openGui(NostrumFairies.MODID, NostrumFairyGui.buildBlockID, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}
	
	public static class BuildingBlockTileEntity extends LogisticsTileEntity implements ITickable,  ILogisticsTaskListener {

		private static final String NBT_SLOT = "itemslot";
		
		private int tickCount;
		private Map<BlockPos, ILogisticsTask> taskMap;
		private double radius;
		private ItemStack slot;
		
		public BuildingBlockTileEntity() {
			this(16);
		}
		
		public BuildingBlockTileEntity(double blockRadius) {
			super();
			taskMap = new HashMap<>();
			this.radius = blockRadius;
			
			MinecraftForge.EVENT_BUS.register(this);
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
		
		private void makePlaceTask(BlockPos base, IBlockState missingState) {
			LogisticsNetwork network = this.getNetwork();
			if (network == null) {
				return;
			}
			
			if (!taskMap.containsKey(base)) {
				ItemStack item = TemplateBlock.GetRequiredItem(missingState);
				LogisticsTaskBuildBlock task = new LogisticsTaskBuildBlock(this.getNetworkComponent(), "Repair Task",
						item, missingState,
						worldObj, base);
				this.taskMap.put(base, task);
				network.getTaskRegistry().register(task, this);
			}
		}
		
		private void removeTask(BlockPos base) {
			ILogisticsTask task = taskMap.remove(base);
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
		
		private void scanBlueprint() {
			if (this.getNetwork() == null) {
				return;
			}
			
			TemplateBlueprint blueprint = TemplateScroll.GetTemplate(slot);
			if (blueprint == null) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			List<BlockPos> blocks = blueprint.spawn(worldObj, pos, EnumFacing.NORTH);
			if (!blocks.isEmpty()) {
				for (BlockPos pos : blocks) {
					this.makePlaceTask(pos, TemplateBlock.GetTemplatedState(worldObj, pos));
				}
			}
			
			final long end = System.currentTimeMillis();
			if (end - startTime >= 5) {
				System.out.println("Took " + (end - startTime) + "ms to scan for blueprint changes!");
			}
		}
		
		private void scan() {
			if (this.getNetwork() == null) {
				return;
			}
			
			final long startTime = System.currentTimeMillis();
			
			Set<BlockPos> known = Sets.newHashSet(taskMap.keySet());
			List<BlockPos> templateSpots = new LinkedList<>();
			
			MutableBlockPos cursor = new MutableBlockPos();
			BlockPos center = this.getPos();
			final int startY = (int) Math.max(-cursor.getY(), Math.floor(-radius));
			final int endY = (int) Math.min(256 - cursor.getY(), Math.ceil(radius));
			for (int x = (int) Math.floor(-radius); x <= Math.ceil(radius); x++)
			for (int z = (int) Math.floor(-radius); z <= Math.ceil(radius); z++)
			for (int y = startY; y < endY; y++) {
				
				cursor.setPos(center.getX() + x, center.getY() + y, center.getZ() + z);
				if (!worldObj.isBlockLoaded(cursor)) {
					break; // skip this whole column
				}
				
				IBlockState state = worldObj.getBlockState(cursor);
				if (state != null && state.getBlock() instanceof TemplateBlock) {
					templateSpots.add(cursor.toImmutable());
				}
			}
			
			for (BlockPos base : templateSpots) {
				if (known.remove(base)) {
					; // We already knew about it, so don't create a new one
				} else {
					// Didn't know, so record!
					// Don't make task cause we filter better later
					makePlaceTask(base, TemplateBlock.GetTemplatedState(worldObj, base));
				}
			}
			
			// For any left in known, the template spot is not there anymore! Remove!
			for (BlockPos base : known) {
				removeTask(base);
			}
			
			final long end = System.currentTimeMillis();
			if (end - startTime >= 5) {
				System.out.println("Took " + (end - startTime) + "ms to scan for lost templates!");
			}
		}
		
		@Override
		public void update() {
			if (this.worldObj.isRemote) {
				return;
			}
			
			this.tickCount++;
			if (this.tickCount % (20 * 10) == 0) {
				scan();
			}
			if (this.slot != null && this.tickCount % 10 == 0) {
				scanBlueprint();
			}
		}
		
		@Override
		public void onTaskDrop(ILogisticsTask task, IFeyWorker worker) {
			; 
		}

		@Override
		public void onTaskAccept(ILogisticsTask task, IFeyWorker worker) {
			;
		}

		@Override
		public void onTaskComplete(ILogisticsTask task, IFeyWorker worker) {
			if (task instanceof LogisticsTaskBuildBlock) {
				LogisticsTaskBuildBlock placeTask = (LogisticsTaskBuildBlock) task;
				BlockPos pos = placeTask.getTargetBlock();
				taskMap.remove(pos);
			}
		}
		
		@SubscribeEvent
		public void onExplosion(ExplosionEvent.Detonate event) {
			if (event.isCanceled() || !event.getWorld().equals(worldObj)) {
				return;
			}
			
			List<BlockPos> positions = new ArrayList<>();
			List<IBlockState> states = new ArrayList<>();
			for (BlockPos loc : event.getAffectedBlocks()) {
				if (Math.abs(pos.getX() - loc.getX()) < radius
						|| Math.abs(pos.getY() - loc.getY()) < radius
						|| Math.abs(pos.getZ() - loc.getZ()) < radius) {
					positions.add(loc);
					states.add(worldObj.getBlockState(loc));
				}
			}
			
			if (!positions.isEmpty()) {
				NostrumMagica.playerListener.registerTimer((type, entity, data) -> {
					for (int i = 0; i < positions.size(); i++) {
						if (worldObj.isAirBlock(positions.get(i))) {
							TemplateBlock.SetTemplate(worldObj, positions.get(i), states.get(i));
						}
					}
					
					// Remove us from the listener
					return true;
				}, 1, 1);
			}
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			if (this.slot != null) {
				nbt.setTag(NBT_SLOT, slot.serializeNBT());
			}
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			this.slot = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag(NBT_SLOT));
		}
		
		public IInventory getInventory() {
			BuildingBlockTileEntity self = this;
			IInventory inv = new InventoryBasic("BuildingBlockInv", false, 1) {
				
				@Override
				public void markDirty() {
					if (slot != null && this.getStackInSlot(0) == null && !self.worldObj.isRemote) {
						System.out.println("Clearing item");
					}
					
					slot = this.getStackInSlot(0);
					super.markDirty();
					self.markDirty();
				}
				
				@Override
				public boolean isItemValidForSlot(int index, ItemStack stack) {
					return index == 0 && (stack == null || stack.getItem() instanceof TemplateScroll);
				}
			};
			
			inv.setInventorySlotContents(0, slot);
			return inv;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class BuildingBlockRenderer extends TileEntityLogisticsRenderer<BuildingBlockTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(BuildingBlockTileEntity.class,
					new BuildingBlockRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntity ent = new BuildingBlockTileEntity();
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
		if (ent == null || !(ent instanceof BuildingBlockTileEntity))
			return;
		
		BuildingBlockTileEntity block = (BuildingBlockTileEntity) ent;
		block.unlinkFromNetwork();
		MinecraftForge.EVENT_BUS.unregister(block);
	}
	
	public static boolean isGrownCrop(World world, BlockPos base) {
		if (world == null || base == null) {
			return false;
		}
		
		IBlockState state = world.getBlockState(base);
		if (state == null) {
			return false;
		}
		
		if (!(state.getBlock() instanceof BlockCrops)) {
			return false;
		}
		
		return ((BlockCrops) state.getBlock()).isMaxAge(state);
	}
	
	public static boolean isPlantableSpot(World world, BlockPos base, ItemStack seed) {
		if (world == null || base == null || seed == null) {
			return false;
		}
		
		if (!world.isAirBlock(base.up())) {
			return false;
		}
		
		IPlantable plantable = null;
		if (seed.getItem() instanceof IPlantable) {
			plantable = (IPlantable) seed.getItem();
		} else if (seed.getItem() instanceof ItemBlock && ((ItemBlock) seed.getItem()).getBlock() instanceof IPlantable) {
			plantable = (IPlantable) ((ItemBlock) seed.getItem()).getBlock();
		}
		
		if (plantable == null) {
			return false;
		}
		
		IBlockState state = world.getBlockState(base);
		return state.getBlock().canSustainPlant(state, world, base, EnumFacing.UP, plantable);
	}
}
