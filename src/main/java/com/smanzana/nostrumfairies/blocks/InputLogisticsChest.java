package com.smanzana.nostrumfairies.blocks;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.client.render.TileEntityLogisticsRenderer;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemDepositRequester;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

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
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InputLogisticsChest extends BlockContainer {
	
	public static final String ID = "logistics_input_chest";
	
	private static InputLogisticsChest instance = null;
	public static InputLogisticsChest instance() {
		if (instance == null)
			instance = new InputLogisticsChest();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(InputChestTileEntity.class, "logistics_input_chest_te");
//		GameRegistry.addShapedRecipe(new ItemStack(instance()),
//				"WPW", "WCW", "WWW",
//				'W', new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE),
//				'P', new ItemStack(Items.PAPER, 1, OreDictionary.WILDCARD_VALUE),
//				'C', NostrumResourceItem.getItem(ResourceType.CRYSTAL_LARGE, 1));
	}
	
	public InputLogisticsChest() {
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
				NostrumFairyGui.inputChestID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	public static class InputChestTileEntity extends LogisticsChestTileEntity {

		private static final int SLOTS = 27;
		
		private String displayName;
		private LogisticsItemDepositRequester requester;
		
		public InputChestTileEntity() {
			super();
			displayName = "Input Chest";
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
		public boolean canAccept(List<ItemDeepStack> stacks) {
			return false;
		}

		@Override
		public Collection<ItemStack> getItems() {
			// Input chests don't offer their items to the network
			return LogisticsTileEntity.emptyList;
		}
		
		@Override
		protected void setNetworkComponent(LogisticsTileEntityComponent component) {
			super.setNetworkComponent(component);
			
			if (worldObj != null && !worldObj.isRemote && requester == null) {
				requester = new LogisticsItemDepositRequester(this.networkComponent.getNetwork(), this.networkComponent); // TODO make using buffer chests configurable!
				requester.updateRequestedItems(getItemRequests());
			}
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			
			if (this.networkComponent != null && !worldIn.isRemote && requester == null) {
				requester = new LogisticsItemDepositRequester(this.networkComponent.getNetwork(), this.networkComponent);
				requester.updateRequestedItems(getItemRequests());
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
		
		private List<ItemStack> getItemRequests() {
			List<ItemStack> requests = new LinkedList<>();
			
			for (int i = 0; i < SLOTS; i++) {
				requests.add(this.getStackInSlot(i));
			}
			
			return requests;
		}
		
		@Override
		public void markDirty() {
			super.markDirty();
			if (worldObj != null && !worldObj.isRemote && requester != null) {
				requester.updateRequestedItems(getItemRequests());
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class InputChestRenderer extends TileEntityLogisticsRenderer<InputChestTileEntity> {
		
		public static void init() {
			ClientRegistry.bindTileEntitySpecialRenderer(InputChestTileEntity.class,
					new InputChestRenderer());
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new InputChestTileEntity();
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
		if (ent == null || !(ent instanceof InputChestTileEntity))
			return;
		
		// TODO! This is missing some items sometimmes!
		
		InputChestTileEntity table = (InputChestTileEntity) ent;
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
