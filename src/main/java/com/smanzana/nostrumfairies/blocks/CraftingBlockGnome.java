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

public class CraftingBlockGnome extends BlockContainer {
	
	public static final String ID = "logistics_crafting_station_gnome";
	
	private static CraftingBlockGnome instance = null;
	public static CraftingBlockGnome instance() {
		if (instance == null)
			instance = new CraftingBlockGnome();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(CraftingBlockGnomeTileEntity.class, "logistics_crafting_station_gnome_te");
	}
	
	public CraftingBlockGnome() {
		super(Material.CLOTH, MapColor.IRON);
		this.setUnlocalizedName(ID);
		this.setHardness(4.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.CLOTH);
		this.setHarvestLevel("pickaxe", 1);
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
				NostrumFairyGui.craftGnomeID, worldIn,
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
		if (ent != null && ent instanceof CraftingBlockGnomeTileEntity) {
			((CraftingBlockGnomeTileEntity) ent).notifyNeighborChanged();
		}
	}
	
	public static class CraftingBlockGnomeTileEntity extends CraftingBlockTileEntity {

		public CraftingBlockGnomeTileEntity() {
			super();
		}

		@Override
		public int getCraftGridDim() {
			return 2;
		}

		@Override
		protected boolean canCraftWith(ItemStack item) {
			return true;
		}

		@Override
		protected float getCraftBonus(ItemStack item) {
			if (item == null) {
				return 0f;
			}
			
			if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.DOWNGRADE
					&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
				return .2f;
			}
			
			Item itemBase = item.getItem();
			String unloc = itemBase.getUnlocalizedName().toLowerCase();
			if (unloc.contains("leaves")
					|| unloc.contains("leaf")
					|| unloc.contains("plant")
					|| unloc.contains("crop")
					|| unloc.contains("seed")
					|| unloc.contains("dirt")
					|| unloc.contains("water")
					|| unloc.contains("flower")) {
				return .3f;
			}
		
			return 0f;
		}
		
		@Override
		protected ItemStack generateOutput() {
			ItemStack out = super.generateOutput();
			if (out != null
					&& !out.getUnlocalizedName().toLowerCase().contains("block")
					&& !out.getUnlocalizedName().toLowerCase().contains("ingot")
					&& !out.getUnlocalizedName().toLowerCase().contains("nugget")) {
				if (FeyStone.instance().getFeySlot(this.getUpgrade()) == FeySlotType.UPGRADE
						&& FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
					int bonus = 0;
					int chances = 5;
					
					for (int i = 0; i < out.stackSize; i++) {
						if (NostrumFairies.random.nextInt(chances) == 0) {
							bonus++;
							chances += 2;
						}
					}
					
					out.stackSize = Math.min(out.getMaxStackSize(), out.stackSize + bonus);
				}
			}
			
			return out;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class CraftingBlockGnomeRenderer extends TileEntityLogisticsRenderer<CraftingBlockGnomeTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockGnomeTileEntity.class,
					new CraftingBlockGnomeRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new CraftingBlockGnomeTileEntity();
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
		if (ent == null || !(ent instanceof CraftingBlockGnomeTileEntity))
			return;
		
		CraftingBlockGnomeTileEntity table = (CraftingBlockGnomeTileEntity) ent;
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
