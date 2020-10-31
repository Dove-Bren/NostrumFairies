package com.smanzana.nostrumfairies.blocks;

import java.util.UUID;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
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
		sensor.unlinkFromNetwork();
		
		if (sensor.logicValidCache) {
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
		
//		TileEntity te = blockAccess.getTileEntity(pos);
//		if (te != null && te instanceof LogisticsSensorTileEntity) {
//			LogisticsSensorTileEntity sensor = (LogisticsSensorTileEntity) te;
//			if (sensor.checkConditions()) {
//				return 15;
//			} else {
//				return 0;
//			}
//		}
	}

	public static class LogisticsSensorTileEntity extends LogisticsTileEntity implements ITickable {
	
		private static final String NBT_LOGIC_ITEM = "logic_item";
		private static final String NBT_LOGIC_OP = "logic_op";
		private static final String NBT_LOGIC_COUNT = "logic_count";
		
		public static enum SensorLogicOp {
			LESS,
			EQUAL,
			MORE;
		}
		
		private SensorLogicOp op;
		private int count;
		private @Nullable ItemStack template;
		
		private UUID logicCacheID;
		private boolean logicValidCache;
		private boolean logicLastWorld;
		
		private boolean placed = false;
		
		public LogisticsSensorTileEntity() {
			super();
			op = SensorLogicOp.EQUAL;
		}
		
		public int getLogicCount() {
			return this.count;
		}
		
		public SensorLogicOp getLogicOp() {
			return this.op;
		}
		
		public @Nullable ItemStack getLogicTemplate() {
			return this.template;
		}
		
		public void setLogicOp(SensorLogicOp op) {
			this.op = op;
			logicCacheID = null;
			this.markDirty();
		}
		
		public void setLogicTemplate(@Nullable ItemStack stack) {
			this.template = stack != null ? stack.copy() : null;
			logicCacheID = null;
			this.markDirty();
		}
	
		public void setLogicCount(int val) {
			this.count = val;
			logicCacheID = null;
			this.markDirty();
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt = super.writeToNBT(nbt);
			
			nbt.setInteger(NBT_LOGIC_COUNT, count);
			nbt.setString(NBT_LOGIC_OP, op.name());
			if (template != null) {
				nbt.setTag(NBT_LOGIC_ITEM, template.serializeNBT());
			}
			
			return nbt;
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			try {
				this.op = SensorLogicOp.valueOf(nbt.getString(NBT_LOGIC_OP).toUpperCase());
			} catch (Exception e) {
				this.op = SensorLogicOp.EQUAL;
			}
			
			this.template = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag(NBT_LOGIC_ITEM));
			this.count = nbt.getInteger(NBT_LOGIC_COUNT);
			
			this.logicCacheID = null;
		}
		
		@Override
		protected void setNetworkComponent(LogisticsTileEntityComponent component) {
			super.setNetworkComponent(component);
		}
		
		@Override
		public void setWorldObj(World worldIn) {
			super.setWorldObj(worldIn);
			
			if (this.networkComponent != null && !worldIn.isRemote) {
				updateLogic();
			}
		}
		
		@Override
		public void onLeaveNetwork() {
			super.onLeaveNetwork();
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			updateLogic();
			super.onJoinNetwork(network);
		}
		
		@Override
		public void markDirty() {
			super.markDirty();
		}
		
		protected void updateLogic() {
			ItemStack req = this.getLogicTemplate();
			if (!placed || this.getNetwork() == null || req == null) {
				this.logicCacheID = null;
				this.logicValidCache = false;
				return;
			}
			
			if (this.logicCacheID == null || !logicCacheID.equals(getNetwork().getCacheKey())) {
				logicCacheID = getNetwork().getCacheKey();
				
				long available = getNetwork().getItemCount(req);
				
				switch(getLogicOp()) {
					case EQUAL:
					default:
						logicValidCache = (available == this.count);
						break;
					case LESS:
						logicValidCache = (available < this.count);
						break;
					case MORE:
						logicValidCache = (available > this.count);
						break;
				}
			}
		}
		
		protected boolean checkConditions() {
			if (worldObj == null || !placed) {
				return false;
			}
			
			updateLogic();
			return logicValidCache;
		}
		
		public void notifyNeighborChanged() {
			this.logicCacheID = null;
			this.updateLogic();
		}
		
		@Override
		public void validate() {
			super.validate();
		}
		
		@Override
		public void update() {
			if (!placed) {
				placed = true;
				if (!worldObj.isRemote) {
					this.logicCacheID = null;
					this.updateLogic();
					//worldObj.notifyNeighborsOfStateChange(pos, worldObj.getBlockState(pos).getBlock());
					worldObj.setBlockState(pos, instance().getDefaultState().withProperty(ACTIVE, this.logicValidCache), 3);
					logicLastWorld = logicValidCache;
				}
			}
			
			if (worldObj.getTotalWorldTime() % 5 == 0) {
				if (!worldObj.isRemote) {
					this.updateLogic();

					if (logicLastWorld != logicValidCache) {
						// Make sure to update the world so that redstone will be updated
						//worldObj.notifyNeighborsOfStateChange(pos, worldObj.getBlockState(pos).getBlock());
						worldObj.setBlockState(pos, instance().getDefaultState().withProperty(ACTIVE, this.logicValidCache), 3);
						logicLastWorld = logicValidCache;
					}
				}
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
	}
}