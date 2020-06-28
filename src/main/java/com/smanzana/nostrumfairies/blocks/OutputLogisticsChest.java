package com.smanzana.nostrumfairies.blocks;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.client.render.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.logistics.LogisticsItemRequester;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.utils.ItemStacks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OutputLogisticsChest extends BlockContainer {
	
	public static final String ID = "logistics_output_chest";
	
	private static OutputLogisticsChest instance = null;
	public static OutputLogisticsChest instance() {
		if (instance == null)
			instance = new OutputLogisticsChest();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(OutputChestTileEntity.class, "logistics_output_chest_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public OutputLogisticsChest() {
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
				NostrumFairyGui.outputChestID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class OutputChestTileEntity extends LogisticsChestTileEntity {

		private static final int SLOTS = 3;
		private static final String NBT_TEMPLATES = "templates";
		private static final String NBT_TEMPLATE_INDEX = "index";
		private static final String NBT_TEMPLATE_ITEM = "item";
		
		private String displayName;
		private ItemStack[] templates;
		private LogisticsItemRequester requester;
		
		public OutputChestTileEntity() {
			super();
			displayName = "Output Chest";
			templates = new ItemStack[SLOTS];
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
		public double getDefaultLogisticsRange() {
			return 10;
		}

		@Override
		public double getDefaultLinkRange() {
			return 10;
		}
		
		@Override
		public boolean canAccept(ItemStack stack) {
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
			
			// Update requester
			if (!worldObj.isRemote) {
				requester.updateRequestedItems(Lists.newArrayList(templates));
			}
			
			this.markDirty();
		}
		
		public @Nullable ItemStack getTemplate(int index) {
			if (index < 0 || index >=  SLOTS) {
				return null;
			}
			
			return templates[index];
		}
		
		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {
			if (!super.isItemValidForSlot(index, stack)) {
				return false;
			}
			
			ItemStack template = getTemplate(index);
			if (template != null) {
				return ItemStacks.stacksMatch(template, stack);
			}
			
			return true;
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
		
		@Override
		protected void setNetworkComponent(LogisticsTileEntityComponent component) {
			super.setNetworkComponent(component);
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			
			if (!worldIn.isRemote && requester == null) {
				requester = new LogisticsItemRequester(this.networkComponent);
				requester.updateRequestedItems(Lists.newArrayList(templates));
			}
		}
		
		@Override
		public void onLeaveNetwork() {
			if (!worldObj.isRemote && requester != null) {
				requester.clearRequests();
			}
			
			super.onLeaveNetwork();
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			if (!worldObj.isRemote && requester != null) {
				requester.updateRequestedItems(Lists.newArrayList(templates));
			}
			
			super.onJoinNetwork(network);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class OutputChestRenderer extends TileEntityLogisticsRenderer<OutputChestTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(OutputChestTileEntity.class,
					new OutputChestRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new OutputChestTileEntity();
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
		if (ent == null || !(ent instanceof OutputChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!
		
		OutputChestTileEntity table = (OutputChestTileEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (table.getStackInSlot(i) != null) {
				EntityItem item = new EntityItem(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.spawnEntityInWorld(item);
			}
		}
		
		table.unlinkFromNetwork();
	}
}
