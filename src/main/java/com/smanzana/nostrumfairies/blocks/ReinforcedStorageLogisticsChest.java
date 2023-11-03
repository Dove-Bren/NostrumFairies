package com.smanzana.nostrumfairies.blocks;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.ReinforcedChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedDiamondChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedGoldChestTileEntity;
import com.smanzana.nostrumfairies.tiles.ReinforcedIronChestTileEntity;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ReinforcedStorageLogisticsChest extends FeyContainerBlock {
	
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
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
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
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, BlockState state) {
		destroy(world, pos, state);
		super.breakBlock(world, pos, state);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof ReinforcedChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!@
		
		ReinforcedChestTileEntity table = (ReinforcedChestTileEntity) ent;
		for (int i = 0; i < table.getSizeInventory(); i++) {
			if (!table.getStackInSlot(i).isEmpty()) {
				ItemEntity item = new ItemEntity(
						world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
						table.removeStackFromSlot(i));
				world.spawnEntity(item);
			}
		}
		
		table.unlinkFromNetwork();
	}
}
