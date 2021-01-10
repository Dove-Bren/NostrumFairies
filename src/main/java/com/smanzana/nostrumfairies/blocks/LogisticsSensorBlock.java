package com.smanzana.nostrumfairies.blocks;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.LogisticsLogicComponent.ILogicListener;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.LogisticsUpdateRequest;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.fml.common.registry.GameRegistry;

public class LogisticsSensorBlock extends BlockContainer
{
	private static final PropertyBool ACTIVE = PropertyBool.create("active");
	public static final String ID = "logistics_sensor";
	
	private static LogisticsSensorBlock instance = null;
	public static LogisticsSensorBlock instance() {
		if (instance == null)
			instance = new LogisticsSensorBlock();
		
		return instance;
	}
	
	public static void init() {
		GameRegistry.registerTileEntity(LogisticsSensorTileEntity.class, "logistics_sensor_te");
	}
	
	public LogisticsSensorBlock() {
		super(Material.WOOD, MapColor.WOOD);
		this.setUnlocalizedName(ID);
		this.setHardness(2.0f);
		this.setResistance(1.0f);
		this.setCreativeTab(NostrumFairies.creativeTab);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 0);
		this.setLightOpacity(2);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ACTIVE);
	}
	
	protected static int metaFromActive(boolean active) {
		return active ? 1 : 0;
	}
	
	protected static boolean activeFromMeta(int meta) {
		return meta == 0 ? false : true;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(ACTIVE, activeFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return metaFromActive(state.getValue(ACTIVE));
	}
	
	public boolean getActive(IBlockState state) {
		return state.getValue(ACTIVE);
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		return this.getDefaultState()
				.withProperty(ACTIVE, false);
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
		return Block.FULL_BLOCK_AABB;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return Block.FULL_BLOCK_AABB;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		IBlockState state = worldIn.getBlockState(pos.down());
		if (state == null || !(state.isSideSolid(worldIn, pos.down(), EnumFacing.UP))) {
			return false;
		}
		
		//return super.canPlaceBlockAt(worldIn, pos);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		if (!canPlaceBlockAt(worldIn, pos)) {
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
		
		// Kick off a request to refresh info.
		if (worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof LogisticsSensorTileEntity) {
				LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) te;
				LogisticsNetwork network = sensor.getNetwork();
				if (network != null) {
					NetworkHandler.getSyncChannel().sendToServer(new LogisticsUpdateRequest(network.getUUID()));
				}
			}
		}
		
		
		// Don't wait, though, and show the UI
		playerIn.openGui(NostrumFairies.instance,
				NostrumFairyGui.logisticsSensorID, worldIn,
				pos.getX(), pos.getY(), pos.getZ());
		
		return true;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new LogisticsSensorTileEntity();
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
		if (ent == null || !(ent instanceof LogisticsSensorTileEntity))
			return;
		
		LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) ent;
		final boolean activated = sensor.logicComp.isActivated();
		sensor.unlinkFromNetwork();
		
		if (activated) {
			for (EnumFacing side : EnumFacing.values()) {
				world.notifyNeighborsOfStateChange(pos.offset(side), this);
			}
		}
	}
	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		if (blockState.getValue(ACTIVE)) {
			return 15;
		}
		
		return 0;
	}

	public static class LogisticsSensorTileEntity extends LogisticsTileEntity implements ITickable, ILogicListener, ILogisticsLogicProvider {
		
		private boolean logicLastWorld;
		
		private static final String NBT_LOGIC_COMP = "logic";
		
		private final LogisticsLogicComponent logicComp;
		
		private boolean placed = false;
		
		public LogisticsSensorTileEntity() {
			super();
			logicComp = new LogisticsLogicComponent(true, this);
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			NBTTagCompound tag = new NBTTagCompound();
			logicComp.writeToNBT(tag);
			nbt.setTag(NBT_LOGIC_COMP, tag);
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			NBTTagCompound sub = nbt.getCompoundTag(NBT_LOGIC_COMP);
			if (sub != null) {
				logicComp.readFromNBT(sub);
			}
		}
		
		@Override
		protected void setNetworkComponent(LogisticsTileEntityComponent component) {
			super.setNetworkComponent(component);
			logicComp.setNetwork(component.getNetwork());
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			
			logicComp.setLocation(worldIn, pos);
		}
		
		@Override
		public void onLeaveNetwork() {
			super.onLeaveNetwork();
			logicComp.setNetwork(null);
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			//updateLogic();
			super.onJoinNetwork(network);
			logicComp.setNetwork(network);
		}
		
		@Override
		public void markDirty() {
			super.markDirty();
		}
		
		public void notifyNeighborChanged() {
			; // This used to bust logic cache. Needed?
		}
		
		@Override
		public void validate() {
			super.validate();
		}
		
		@Override
		public void update() {
			if (!placed || worldObj.getTotalWorldTime() % 5 == 0) {
				if (!worldObj.isRemote) {
					final boolean activated = this.logicComp.isActivated(); 

					if (!placed || logicLastWorld != activated) {
						// Make sure to update the world so that redstone will be updated
						//worldObj.notifyNeighborsOfStateChange(pos, worldObj.getBlockState(pos).getBlock());
						worldObj.setBlockState(pos, instance().getDefaultState().withProperty(ACTIVE, activated), 3);
						logicLastWorld = activated;
					}
				}
				
				placed = true;
			}
		}

		@Override
		protected double getDefaultLinkRange() {
			return 10;
		}

		@Override
		protected double getDefaultLogisticsRange() {
			return 5;
		}
		
		@Override
		public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
			return !(oldState.getBlock().equals(newState.getBlock()));
		}

		@Override
		public void onStateChange(boolean activated) {
			; // We handle this in a tick loop, which adds lag between redstone but also won't change blockstates
			// multiples times if item count jumps back and forth across a boundary in a single tick
		}

		@Override
		public void onDirty() {
			this.markDirty();
		}
		
		@Override
		public LogisticsLogicComponent getLogicComponent() {
			return this.logicComp;
		}
	}
}