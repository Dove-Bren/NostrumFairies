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

public class CraftingBlockDwarf extends BlockContainer {
	
	public static final String ID = "logistics_crafting_station_dwarf";
	
	private static CraftingBlockDwarf instance = null;
	public static CraftingBlockDwarf instance() {
		if (instance == null)
			instance = new CraftingBlockDwarf();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(CraftingBlockDwarfTileEntity.class, "logistics_crafting_station_dwarf_te");
	}
	
	public CraftingBlockDwarf() {
		super(Material.IRON, MapColor.IRON);
		this.setUnlocalizedName(ID);
		this.setHardness(4.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.ANVIL);
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
				NostrumFairyGui.craftDwarfID, worldIn,
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
		if (ent != null && ent instanceof CraftingBlockDwarfTileEntity) {
			((CraftingBlockDwarfTileEntity) ent).notifyNeighborChanged();
		}
	}
	
	public static class CraftingBlockDwarfTileEntity extends CraftingBlockTileEntity {

		public CraftingBlockDwarfTileEntity() {
			super();
		}

		@Override
		public int getCraftGridDim() {
			return 3;
		}
		
		protected boolean isGoodMaterialName(String unloc) {
			return unloc.contains("ingot")
					|| unloc.contains("metal")
					|| unloc.contains("iron")
					|| unloc.contains("gold")
					|| unloc.contains("gear")
					|| unloc.contains("bronze")
					|| unloc.contains("copper")
					|| unloc.contains("tin")
					|| unloc.contains("aluminum")
					|| unloc.contains("titanium")
					|| unloc.contains("rod")
					|| unloc.contains("stone")
					|| unloc.contains("rock")
					|| unloc.contains("machine")
					|| unloc.contains("part")
					|| unloc.contains("cast");
		}

		@Override
		protected boolean canCraftWith(ItemStack item) {
			if (item == null) {
				return true;
			}
			
			boolean strict = false;
			if (FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
				FeySlotType slot = FeyStone.instance().getFeySlot(this.getUpgrade()); 
				if (slot == FeySlotType.DOWNGRADE) {
					return true;
				} else if (slot == FeySlotType.UPGRADE) {
					strict = true;
				}
			}
		
			Item itemBase = item.getItem();
			String unloc = itemBase.getUnlocalizedName();
			
			if (strict) {
				// HAS to be a friendly material
				if (isGoodMaterialName(unloc)) {
					return true;
				}
				return false;
			}
			
			// if not strict, just can't be a bad material
			if (unloc.contains("log")
					|| unloc.contains("plank")
					|| unloc.contains("wood")
					|| unloc.contains("stick")) {
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
			if (FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.SAPPHIRE) {
				FeySlotType slot = FeyStone.instance().getFeySlot(this.getUpgrade());
				if (slot == FeySlotType.DOWNGRADE) {
					buff = .025f; // but no disallowed item types
				} else if (slot == FeySlotType.UPGRADE) {
					buff = .35f;
				}
			}
			
			Item itemBase = item.getItem();
			String unloc = itemBase.getUnlocalizedName();
			if (isGoodMaterialName(unloc)) {
				return buff;
			}
		
			return 0f;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class CraftingBlockDwarfRenderer extends TileEntityLogisticsRenderer<CraftingBlockDwarfTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(CraftingBlockDwarfTileEntity.class,
					new CraftingBlockDwarfRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new CraftingBlockDwarfTileEntity();
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
		if (ent == null || !(ent instanceof CraftingBlockDwarfTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!
		
		CraftingBlockDwarfTileEntity table = (CraftingBlockDwarfTileEntity) ent;
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
