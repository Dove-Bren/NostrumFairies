package com.smanzana.nostrumfairies.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.client.render.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CraftingBlockElf extends BlockContainer {
	
	public static final String ID = "logistics_crafting_station_elf";
	
	private static CraftingBlockElf instance = null;
	public static CraftingBlockElf instance() {
		if (instance == null)
			instance = new CraftingBlockElf();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(CraftingBlockElfTileEntity.class, "logistics_crafting_station_elf_te");
	}
	
	public CraftingBlockElf() {
		super(Material.WOOD, MapColor.IRON);
		this.setUnlocalizedName(ID);
		this.setHardness(4.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 1);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if (!worldIn.isRemote) {
			worldIn.notifyBlockUpdate(pos, state, state, 2);
		}
		
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.craftElfID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent != null && ent instanceof CraftingBlockElfTileEntity) {
			((CraftingBlockElfTileEntity) ent).notifyNeighborChanged();
		}
	}
	
	public static class CraftingBlockElfTileEntity extends CraftingBlockTileEntity {

		public CraftingBlockElfTileEntity() {
			super();
		}

		@Override
		public int getCraftGridDim() {
			return 3;
		}

		@Override
		protected boolean canCraftWith(ItemStack item) {
			if (item == null) {
				return true;
			}
			
			if (this.getUpgrade() != null) {
				if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.DOWNGRADE
						&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
					return true;
				}
			}
			
			Item itemBase = item.getItem();
			String unloc = itemBase.getUnlocalizedName().toLowerCase();
			if (unloc.contains("ingot")
					|| unloc.contains("metal")
					|| unloc.contains("iron")
					|| unloc.contains("gold")) {
				return false;
			}
			
			return true;
		}
		
		@Override
		protected float getCraftBonus(ItemStack item) {
			if (item == null) {
				return 0f;
			}
			
			float buff = .1f;
			if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.DOWNGRADE
						&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
				buff = .025f;
			}
			
			Item itemBase = item.getItem();
			String unloc = itemBase.getUnlocalizedName().toLowerCase();
			if (unloc.contains("log")
					|| unloc.contains("plank")
					|| unloc.contains("wood")
					|| unloc.contains("stick")) {
				return buff;
			}
		
			return 0f;
		}
		
		@Override
		protected int getMaxWorkJobs() {
			final int base = super.getMaxWorkJobs();
			if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.UPGRADE
					&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
				return 2 * base;
			}
			return base;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class CraftingBlockElfRenderer extends TileEntityLogisticsRenderer<CraftingBlockElfTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockElfTileEntity.class,
					new CraftingBlockElfRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new CraftingBlockElfTileEntity();
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
		if (ent == null || !(ent instanceof CraftingBlockElfTileEntity))
			return;
		
		CraftingBlockElfTileEntity table = (CraftingBlockElfTileEntity) ent;
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
