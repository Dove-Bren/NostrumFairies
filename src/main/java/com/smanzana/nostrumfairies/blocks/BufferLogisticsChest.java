package com.smanzana.nostrumfairies.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.client.render.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.logistics.LogisticsComponentRegistry.ILogisticsComponentFactory;
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

public class BufferLogisticsChest extends BlockContainer {
	
	public static final String ID = "logistics_buffer_chest";
	
	private static BufferLogisticsChest instance = null;
	public static BufferLogisticsChest instance() {
		if (instance == null)
			instance = new BufferLogisticsChest();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(BufferChestTileEntity.class, "logistics_buffer_chest_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public BufferLogisticsChest() {
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
				NostrumFairyGui.bufferChestID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class BufferChestTileEntity extends LogisticsChestTileEntity {

		private static final int SLOTS = 9;
		private static final String NBT_TEMPLATES = "templates";
		private static final String NBT_TEMPLATE_INDEX = "index";
		private static final String NBT_TEMPLATE_ITEM = "item";
		
		private String displayName;
		private ItemStack[] templates;
		
		public BufferChestTileEntity() {
			super();
			displayName = "Buffer Chest";
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
		public double getLogisticRange() {
			return 10;
		}

		@Override
		public double getLogisticsLinkRange() {
			return 10;
		}
		
		@Override
		public boolean canAccept(ItemStack stack) {
			return ItemStacks.canFit(this, stack);
		}
		
		public void setTemplate(int index, @Nullable ItemStack template) {
			if (index < 0 || index >=  SLOTS) {
				return;
			}
			
			System.out.println("Setting template");
			
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
		public NBTTagCompound toNBT() {
			NBTTagCompound tag =  this.baseToNBT();
			
			// Save templates
			NBTTagList templates = new NBTTagList();
			for (int i = 0; i < SLOTS; i++) {
				ItemStack stack = this.getStackInSlot(i);
				if (stack == null) {
					continue;
				}
				
				NBTTagCompound template = new NBTTagCompound();
				
				template.setInteger(NBT_TEMPLATE_INDEX, i);
				template.setTag(NBT_TEMPLATE_ITEM, stack.writeToNBT(new NBTTagCompound()));
				
				templates.appendTag(template);
			}
			tag.setTag(NBT_TEMPLATES, templates);
			
			return tag;
		}

		public static final String LOGISTICS_TAG = "logcomp_bufferchest"; 

		@Override
		public String getSerializationTag() {
			return LOGISTICS_TAG;
		}
		
		public static class BufferChestTEFactory implements ILogisticsComponentFactory<BufferChestTileEntity> {

			@Override
			public BufferChestTileEntity construct(NBTTagCompound nbt, LogisticsNetwork network) {
				BufferChestTileEntity ent = (BufferChestTileEntity) LogisticsChestTileEntity.loadFromNBT(nbt, network);
				
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
					
					ent.templates[index] = stack;
				}
				
				return ent; 
			}
			
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class BufferChestRenderer extends TileEntityLogisticsRenderer<BufferChestTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(BufferChestTileEntity.class,
					new BufferChestRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new BufferChestTileEntity();
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
		if (ent == null || !(ent instanceof BufferChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!
		
		BufferChestTileEntity table = (BufferChestTileEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (table.getStackInSlot(i) != null) {
				EntityItem item = new EntityItem(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.spawnEntityInWorld(item);
			}
		}
		
		if (table.getNetwork() != null) {
			table.getNetwork().removeComponent(table);
		}
	}
}
