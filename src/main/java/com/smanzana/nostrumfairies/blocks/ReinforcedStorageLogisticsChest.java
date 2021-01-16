package com.smanzana.nostrumfairies.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrumfairies.utils.ItemDeepStacks;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ReinforcedStorageLogisticsChest extends BlockContainer {
	
	private static enum Type {
		IRON("iron"),
		GOLD("gold"),
		DIAMOND("diamond");
		
		private final String suffix;
		
		private Type(String suffix) {
			this.suffix = suffix;
		}
	}
	
	private static final String ID_PREFIX = "logistics_reinforced_chest";
	
	private static ReinforcedStorageLogisticsChest Iron = null;
	private static ReinforcedStorageLogisticsChest Gold = null;
	private static ReinforcedStorageLogisticsChest Diamond = null;
	
	public static ReinforcedStorageLogisticsChest Iron() {
		if (Iron == null)
			Iron = new ReinforcedStorageLogisticsChest(Type.IRON);
		
		return Iron;
	}
	
	public static ReinforcedStorageLogisticsChest Gold() {
		if (Gold == null)
			Gold = new ReinforcedStorageLogisticsChest(Type.GOLD);
		
		return Gold;
	}
	
	public static ReinforcedStorageLogisticsChest Diamond() {
		if (Diamond == null)
			Diamond = new ReinforcedStorageLogisticsChest(Type.DIAMOND);
		
		return Diamond;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(ReinforcedIronChestTileEntity.class, "logistics_reinforced_chest_iron_te");
		GameRegistry.registerTileEntity(ReinforcedGoldChestTileEntity.class, "logistics_reinforced_chest_gold_te");
		GameRegistry.registerTileEntity(ReinforcedDiamondChestTileEntity.class, "logistics_reinforced_chest_diamond_te");
	}
	
	private final Type type;
	
	public ReinforcedStorageLogisticsChest(Type type) {
		super(Material.WOOD, MapColor.WOOD);
		this.type = type;
		this.setUnlocalizedName(getID());
		this.setHardness(3.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
	}

	public String getID() {
		return ID_PREFIX + "_" + type.suffix;
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		switch (this.type) {
		case IRON:
		default:
			return new ReinforcedIronChestTileEntity();
		case GOLD:
			return new ReinforcedGoldChestTileEntity();
		case DIAMOND:
			return new ReinforcedDiamondChestTileEntity();
		}
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
		if (ent == null || !(ent instanceof ReinforcedChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!@
		
		ReinforcedChestTileEntity table = (ReinforcedChestTileEntity) ent;
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
	
	public static abstract class ReinforcedChestTileEntity extends LogisticsChestTileEntity {

		private String displayName;
		
		public ReinforcedChestTileEntity() {
			super();
			displayName = "Reinforced Storage Chest";
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
		public double getDefaultLogisticsRange() {
			return 20;
		}

		@Override
		public double getDefaultLinkRange() {
			return 10;
		}
		
		@Override
		public boolean canAccept(List<ItemDeepStack> stacks) {
			return ItemDeepStacks.canFitAll(this, stacks);
		}
	}
	
	public static class ReinforcedIronChestTileEntity extends ReinforcedChestTileEntity {
		
		public static final int INV_SIZE = 81;

		public ReinforcedIronChestTileEntity() {
			super();
		}
		
		@Override
		public int getSizeInventory() {
			return INV_SIZE;
		}
		
	}
	
	public static class ReinforcedGoldChestTileEntity extends ReinforcedChestTileEntity {
		
		public static final int INV_SIZE = 162;

		public ReinforcedGoldChestTileEntity() {
			super();
		}
		
		@Override
		public int getSizeInventory() {
			return INV_SIZE;
		}
		
	}
	
	public static class ReinforcedDiamondChestTileEntity extends ReinforcedChestTileEntity {
		
		public static final int INV_SIZE = 324;

		public ReinforcedDiamondChestTileEntity() {
			super();
		}
		
		@Override
		public int getSizeInventory() {
			return INV_SIZE;
		}
		
	}
}
