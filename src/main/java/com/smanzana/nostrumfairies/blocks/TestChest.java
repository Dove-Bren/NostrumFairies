package com.smanzana.nostrumfairies.blocks;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry.ILogisticsComponentFactory;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TestChest extends BlockContainer {
	
	public static final String ID = "test_chest";
	
	private static TestChest instance = null;
	public static TestChest instance() {
		if (instance == null)
			instance = new TestChest();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(TestChestTileEntity.class, "test_chest_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public TestChest() {
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
		
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.testChestID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class TestChestTileEntity extends TileEntity implements IInventory, ILogisticsComponent {

		private static final String NBT_INV = "testchest";
		
		/**
		 * Inventory:
		 *   0-26 - chest contents
		 */
		
		private static final int SLOTS = 27;
		
		private String displayName;
		private ItemStack slots[];
		private LogisticsNetwork network;
		
		public TestChestTileEntity() {
			displayName = "Test Chest";
			slots = new ItemStack[getSizeInventory()];
		}
		
		@Override
		public String getName() {
			return displayName;
		}

		@Override
		public boolean hasCustomName() {
			return false;
		}
		
		@Override
		public int getSizeInventory() {
			return SLOTS;
		}

		@Override
		public ItemStack getStackInSlot(int index) {
			if (index < 0 || index >= getSizeInventory())
				return null;
			
			return slots[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {
			if (index < 0 || index >= getSizeInventory() || slots[index] == null)
				return null;
			
			ItemStack stack;
			if (slots[index].stackSize <= count) {
				stack = slots[index];
				slots[index] = null;
			} else {
				stack = slots[index].copy();
				stack.stackSize = count;
				slots[index].stackSize -= count;
			}
			
			if (network != null) {
				this.network.dirty();
			}
			this.markDirty();
			
			return stack;
		}

		@Override
		public ItemStack removeStackFromSlot(int index) {
			if (index < 0 || index >= getSizeInventory())
				return null;
			
			ItemStack stack = slots[index];
			slots[index] = null;
			
			if (network != null) {
				this.network.dirty();
			}
			this.markDirty();
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			if (!isItemValidForSlot(index, stack))
				return;
			
			slots[index] = stack;
			if (network != null) {
				this.network.dirty();
			}
			this.markDirty();
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return true;
		}

		@Override
		public void openInventory(EntityPlayer player) {
		}

		@Override
		public void closeInventory(EntityPlayer player) {
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			if (index < 0 || index >= getSizeInventory())
				return false;
			
			return true;
		}

		@Override
		public int getField(int id) {
			return 0;
		}

		@Override
		public void setField(int id, int value) {
			
		}

		@Override
		public int getFieldCount() {
			return 0;
		}

		@Override
		public void clear() {
			for (int i = 0; i < getSizeInventory(); i++)
				removeStackFromSlot(i);
		}
		
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			NBTTagCompound compound = new NBTTagCompound();
			
			for (int i = 0; i < getSizeInventory(); i++) {
				if (getStackInSlot(i) == null)
					continue;
				
				NBTTagCompound tag = new NBTTagCompound();
				compound.setTag(i + "", getStackInSlot(i).writeToNBT(tag));
			}
			
			if (nbt == null)
				nbt = new NBTTagCompound();
			
			nbt.setTag(NBT_INV, compound);
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			if (nbt == null || !nbt.hasKey(NBT_INV, NBT.TAG_COMPOUND))
				return;
			this.clear();
			NBTTagCompound items = nbt.getCompoundTag(NBT_INV);
			for (String key : items.getKeySet()) {
				int id;
				try {
					id = Integer.parseInt(key);
				} catch (NumberFormatException e) {
					NostrumFairies.logger.error("Failed reading TestChest inventory slot: " + key);
					continue;
				}
				
				ItemStack stack = ItemStack.loadItemStackFromNBT(items.getCompoundTag(key));
				this.setInventorySlotContents(id, stack);
			}
		}
		
		/**
	     * Try to add the item to the invntory.
	     * Return what won't fit.
	     */
	    public ItemStack addItem(@Nullable ItemStack stack) {
	    	if (stack == null) {
	    		return null;
	    	}
	    	
	    	ItemStack itemstack = stack.copy();

	    	for (int i = 0; i < this.getSizeInventory(); ++i) {
	            ItemStack itemstack1 = this.getStackInSlot(i);

	            if (itemstack1 == null) {
	                this.setInventorySlotContents(i, itemstack);
	                if (network != null) {
	    				this.network.dirty();
	    			}
	                this.markDirty();
	                return null;
	            }
	            
	            if (itemstack.getItem() == itemstack1.getItem()
	            		&& itemstack.getMetadata() == itemstack1.getMetadata()
	            		&& Objects.equal(itemstack.getTagCompound(), itemstack1.getTagCompound())) {
	            	// stacks appear to match. Deduct stack size
	            	int room = itemstack1.getMaxStackSize() - itemstack1.stackSize;
	            	if (room > itemstack.stackSize) {
	            		itemstack1.stackSize += itemstack.stackSize;
	            		if (network != null) {
	        				this.network.dirty();
	        			}
	            		this.markDirty();
	            		return null;
	            	} else if (room > 0) {
	            		if (network != null) {
	        				this.network.dirty();
	        			}
	            		this.markDirty();
	            		itemstack.stackSize -= room;
	            		itemstack1.stackSize += room;
	            	}
	            }
	        }

	        return itemstack;
	    }
	    
	    @Override
	    public void updateContainingBlockInfo() {
	    	super.updateContainingBlockInfo();
	    	if (!worldObj.isRemote && this.network == null) {
				System.out.println("Setting tile entity");
				NostrumFairies.instance.getLogisticsRegistry().addNewComponent(this);
			}
	    }

		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			this.network = network;
		}

		@Override
		public void onLeaveNetwork() {
			this.network = null;
		}

		@Override
		public double getLogisticRange() {
			return 20.0;
		}

		@Override
		public double getLogisticsLinkRange() {
			return 10.0;
		}

		@Override
		public BlockPos getPosition() {
			return this.pos;
		}

		@Override
		public Collection<ItemStack> getItems() {
			return Lists.newArrayList(slots);
		}
		
		private static final String NBT_LOG_POS = "pos";
		private static final String NBT_LOG_DIM = "dim";
		private static final String NBT_LOG_NETWORK = "network";

		@Override
		public NBTTagCompound toNBT() {
			NBTTagCompound tag = new NBTTagCompound();
			
			tag.setLong(NBT_LOG_POS, this.pos.toLong());
			tag.setInteger(NBT_LOG_DIM, this.worldObj.provider.getDimension());
			
			return tag;
		}
		
		public static final String LOGISTICS_TAG = "logcomp_testchest"; 

		@Override
		public String getSerializationTag() {
			return LOGISTICS_TAG;
		}
		
		public static class TestChestTEFactory implements ILogisticsComponentFactory<TestChestTileEntity> {

			@Override
			public TestChestTileEntity construct(NBTTagCompound nbt, LogisticsNetwork network) {
				// We store the TE position. Hook back up!
				BlockPos pos = BlockPos.fromLong(nbt.getLong(NBT_LOG_POS));
				World world = NostrumFairies.getWorld(nbt.getInteger(NBT_LOG_DIM));
				
				if (world == null) {
					throw new RuntimeException("Failed to find world for persisted TileEntity logistics component: "
							+ nbt.getInteger(NBT_LOG_DIM));
				}
				
				TileEntity te = world.getTileEntity(pos);
				
				if (te == null) {
					throw new RuntimeException("Failed to lookup tile entity at persisted location: "
							+ pos);
				}
				
				TestChestTileEntity chest = (TestChestTileEntity) te;
				chest.network = network;
				return chest;
			}
			
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TestChestTileEntity();
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
		if (ent == null || !(ent instanceof TestChestTileEntity))
			return;
		
		TestChestTileEntity table = (TestChestTileEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (table.getStackInSlot(i) != null) {
				EntityItem item = new EntityItem(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.spawnEntityInWorld(item);
			}
		}
		
		if (table.network != null) {
			table.network.removeComponent(table);
		}
	}
}
