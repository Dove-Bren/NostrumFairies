package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWithdrawItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class OutputLogisticsPanel extends BlockContainer {
	
	private static final PropertyDirection FACING = PropertyDirection.create("facing");
	private static final double BB_MINOR = 1.0 / 16.0;
	private static final double BB_MAJOR = 2.0 / 16.0;
	private static final AxisAlignedBB AABB_N = new AxisAlignedBB(BB_MAJOR, BB_MAJOR, 0, 1 - BB_MAJOR, 1 - BB_MAJOR, BB_MINOR);
	private static final AxisAlignedBB AABB_E = new AxisAlignedBB(1 - BB_MINOR, BB_MAJOR, BB_MAJOR, 1, 1 - BB_MAJOR, 1 - BB_MAJOR);
	private static final AxisAlignedBB AABB_S = new AxisAlignedBB(BB_MAJOR, BB_MAJOR, 1 - BB_MINOR, 1 - BB_MAJOR, 1 - BB_MAJOR, 1);
	private static final AxisAlignedBB AABB_W = new AxisAlignedBB(0, BB_MAJOR, BB_MAJOR, BB_MINOR, 1 - BB_MAJOR, 1 - BB_MAJOR);
	private static final AxisAlignedBB AABB_U = new AxisAlignedBB(BB_MAJOR, 1 - BB_MINOR, BB_MAJOR, 1 - BB_MAJOR, 1, 1 - BB_MAJOR);
	private static final AxisAlignedBB AABB_D = new AxisAlignedBB(BB_MAJOR, 0, BB_MAJOR, 1 - BB_MAJOR, BB_MINOR, 1 - BB_MAJOR);
	public static final String ID = "logistics_output_panel";
	
	private static OutputLogisticsPanel instance = null;
	public static OutputLogisticsPanel instance() {
		if (instance == null)
			instance = new OutputLogisticsPanel();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(OutputPanelTileEntity.class, "logistics_output_panel_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public OutputLogisticsPanel() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	protected static int metaFromFacing(EnumFacing facing) {
		return facing.getIndex();
	}
	
	protected static EnumFacing facingFromMeta(int meta) {
		return EnumFacing.VALUES[meta % EnumFacing.VALUES.length];
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(FACING, facingFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return metaFromFacing(state.getValue(FACING));
	}
	
	public EnumFacing getFacing(IBlockState state) {
		return state.getValue(FACING);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		// Want to point towards the block we clicked
		facing = facing.getOpposite();
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
				.withProperty(FACING, facing);
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
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
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		switch (blockState.getValue(FACING)) {
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
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }
	
	protected boolean canPlaceAt(World worldIn, BlockPos pos, EnumFacing side) {
		IBlockState state = worldIn.getBlockState(pos.offset(side));
		if (state == null || !(state.getMaterial().blocksMovement())) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		for (EnumFacing side : EnumFacing.VALUES) {
			if (canPlaceAt(worldIn, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		EnumFacing face = state.getValue(FACING);
		if (!canPlaceAt(worldIn, pos, face)) {
			this.dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
		}
		
		super.neighborChanged(state, worldIn, pos, blockIn);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.outputPanelID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class OutputPanelTileEntity extends LogisticsTileEntity implements ITickable {

		private static final int SLOTS = 3;
		private static final String NBT_TEMPLATES = "templates";
		private static final String NBT_TEMPLATE_INDEX = "index";
		private static final String NBT_TEMPLATE_ITEM = "item";
		
		private ItemStack[] templates;
		private LogisticsItemWithdrawRequester requester;
		private int ticksExisted; // Not persisted
		
		public OutputPanelTileEntity() {
			super();
			templates = new ItemStack[SLOTS];
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

		@Override
		public Collection<ItemStack> getItems() {
			// Output chests don't offer their items to the network
			return LogisticsTileEntity.emptyList;
		}
		
		public void setTemplate(int index, @Nullable ItemStack template) {
			if (index < 0 || index >=  SLOTS) {
				return;
			}
			
			ItemStack temp = template == null ? null : template.copy();
			templates[index] = temp;
			this.markDirty();
		}
		
		public @Nullable ItemStack getTemplate(int index) {
			if (index < 0 || index >=  SLOTS) {
				return null;
			}
			
			return templates[index];
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			// Save templates
			NBTTagList templates = new NBTTagList();
			for (int i = 0; i < SLOTS; i++) {
				ItemStack stack = this.getTemplate(i);
				if (stack == null) {
					continue;
				}
				
				NBTTagCompound template = new NBTTagCompound();
				
				template.setInteger(NBT_TEMPLATE_INDEX, i);
				template.setTag(NBT_TEMPLATE_ITEM, stack.writeToNBT(new NBTTagCompound()));
				
				templates.appendTag(template);
			}
			nbt.setTag(NBT_TEMPLATES, templates);
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			templates = new ItemStack[SLOTS];
			
			// Reload templates
			NBTTagList list = nbt.getTagList(NBT_TEMPLATES, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound template = list.getCompoundTagAt(i);
				int index = template.getInteger(NBT_TEMPLATE_INDEX);
				
				if (index < 0 || index > SLOTS) {
					NostrumFairies.logger.error("Found serialized template with invalid index! " + index + " outside of " + SLOTS);
					continue;
				}
				
				ItemStack stack = ItemStack.loadItemStackFromNBT(template.getCompoundTag(NBT_TEMPLATE_ITEM));
				
				templates[index] = stack;
			}
			
			// Do super afterwards so taht we have templates already
			super.readFromNBT(nbt);
		}
		
		protected LogisticsItemWithdrawRequester makeRequester(LogisticsNetwork network, LogisticsTileEntityComponent networkComponent) {
			return new LogisticsItemWithdrawRequester(network, true, networkComponent) {
				@Override
				protected List<LogisticsTaskWithdrawItem> filterActiveRequests(final List<LogisticsTaskWithdrawItem> taskList) {
//					List<LogisticsTaskWithdrawItem> list = new ArrayList<>(taskList.size());
//					
//					for (LogisticsTaskWithdrawItem task : taskList) {
//						if (!task.isComplete()) { // Filter out completed tasks
//							list.add(task); 
//						}
//					}
//					
//					return list;
					return super.filterActiveRequests(taskList);
				}
			}; // TODO make using buffer chests configurable!;
		}
		
		@Override
		protected void setNetworkComponent(LogisticsTileEntityComponent component) {
			super.setNetworkComponent(component);
			
			if (worldObj != null && !worldObj.isRemote && requester == null) {
				requester = makeRequester(this.networkComponent.getNetwork(), this.networkComponent);
				requester.updateRequestedItems(getItemRequests());
			}
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			
			if (this.networkComponent != null && !worldIn.isRemote && requester == null) {
				requester = makeRequester(this.networkComponent.getNetwork(), this.networkComponent);
				//requester.updateRequestedItems(getItemRequests());
			}
		}
		
		@Override
		public void onLeaveNetwork() {
			if (!worldObj.isRemote && requester != null) {
				requester.clearRequests();
				requester.setNetwork(null);
			}
			
			super.onLeaveNetwork();
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			if (!worldObj.isRemote && requester != null) {
				requester.setNetwork(network);
				requester.updateRequestedItems(getItemRequests());
			}
			
			super.onJoinNetwork(network);
		}
		
		private @Nullable IItemHandler getLinkedInventoryHandler() {
			final EnumFacing direction = getFacing();
			final BlockPos linkPos = pos.offset(direction);
			final TileEntity te = worldObj.getTileEntity(linkPos);
			if (te != null) {
				if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
					@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
					return handler;
				}
			}
			
			return null;
		}
		
		private @Nullable IInventory getLinkedInventory() {
			final EnumFacing direction = getFacing();
			final BlockPos linkPos = pos.offset(direction);
			final TileEntity te = worldObj.getTileEntity(linkPos);
			if (te != null) {
				if (te instanceof IInventory) {
					return (IInventory) te;
				}
			}
			
			return null;
		}
		
		private final List<ItemDeepStack> linkedItemList = new ArrayList<>();
		
		/**
		 * Get the items in the linked inventory, if any
		 * @return
		 */
		private List<ItemDeepStack> getLinkedItems() {
			linkedItemList.clear();
			final @Nullable IItemHandler handler = getLinkedInventoryHandler();
			if (handler != null) {
				return ItemDeepStack.toDeepList(linkedItemList, () -> {
					return new Iterator<ItemStack>() {
						
						private int i = 0;
						
						@Override
						public boolean hasNext() {
							return i < handler.getSlots();
						}

						@Override
						public ItemStack next() {
							return handler.getStackInSlot(i++);
						}
					};
				});
			} else {
				final @Nullable IInventory inv = getLinkedInventory();
				if (inv != null) {
					return ItemDeepStack.toDeepList(linkedItemList, inv);
				}
			}
			
			return linkedItemList;
		}
		
		private List<ItemStack> getItemRequests() {
			final List<ItemStack> requests = new LinkedList<>();
			final List<ItemDeepStack> available = getLinkedItems();
			
			for (int i = 0; i < templates.length; i++) {
				if (templates[i] == null) {
					continue;
				}
				int wanted = templates[i].stackSize;
				
				// Find ItemDeepStack for this template
				ItemDeepStack found = null;
				for (ItemDeepStack stack : available) {
					if (stack.canMerge(templates[i])) {
						found = stack;
						break;
					}
				}
				
				if (found != null) {
					final int wantedCount = wanted;
					wanted -= found.getCount();
					found.add(-wantedCount);
				}
				
				if (wanted > 0) {
					ItemStack req = templates[i].copy();
					req.stackSize = wanted;
					requests.add(req);
				}
			}
			
			return requests;
		}
		
		@Override
		public void takeItem(ItemStack stack) {
			// If there's an option, take from slots that don't have templates first
			super.takeItem(stack);
			// TODO
		}
		
		@Override
		public void addItem(ItemStack stack) {
			// Add items to the linked inventory
			final @Nullable IItemHandler handler = getLinkedInventoryHandler();
			ItemStack remaining = stack.copy();
			if (handler != null) {
				remaining = Inventories.addItem(handler, remaining);
			} else {
				final @Nullable IInventory inv = getLinkedInventory();
				if (inv != null) {
					remaining = Inventories.addItem(inv, remaining);
				}
			}
			
			// Any leftover?
			if (remaining != null && remaining.stackSize > 0) {
				EntityItem item = new EntityItem(this.worldObj, this.pos.getX() + .5, this.pos.getY() + 1, this.pos.getZ() + .5, remaining);
				worldObj.spawnEntityInWorld(item);
			}
		}
		
		@Override
		public void markDirty() {
			super.markDirty();
			tickRequester();
		}
		
		public EnumFacing getFacing() {
			if (worldObj != null) {
				IBlockState state = worldObj.getBlockState(pos);
				try {
					return state.getValue(FACING);
				} catch (Exception e) {
					;
				}
			}
			
			return EnumFacing.NORTH;
		}
		
		protected void tickRequester() {
			if (worldObj != null && !worldObj.isRemote && requester != null) {
				requester.updateRequestedItems(getItemRequests());
			}
		}
		
		@Override
		public void update() {
			ticksExisted++;
			if (this.ticksExisted % 8 == 0) {
				tickRequester();
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new OutputPanelTileEntity();
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
		if (ent == null || !(ent instanceof OutputPanelTileEntity))
			return;
		
		OutputPanelTileEntity table = (OutputPanelTileEntity) ent;
		table.unlinkFromNetwork();
	}
}
