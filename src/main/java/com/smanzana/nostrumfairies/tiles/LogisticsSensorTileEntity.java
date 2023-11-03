package com.smanzana.nostrumfairies.tiles;

import com.smanzana.nostrumfairies.blocks.LogisticsSensorBlock;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.ILogicListener;

import net.minecraft.block.state.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LogisticsSensorTileEntity extends LogisticsTileEntity implements ITickable, ILogicListener, ILogisticsLogicProvider {
	
	private boolean logicLastWorld;
	
	private static final String NBT_LOGIC_COMP = "logic";
	
	private final LogisticsLogicComponent logicComp;
	
	private boolean placed = false;
	
	public LogisticsSensorTileEntity() {
		super();
		logicComp = new LogisticsLogicComponent(true, this);
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		CompoundNBT tag = new CompoundNBT();
		logicComp.writeToNBT(tag);
		nbt.put(NBT_LOGIC_COMP, tag);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt) {
		super.readFromNBT(nbt);
		
		CompoundNBT sub = nbt.getCompoundTag(NBT_LOGIC_COMP);
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
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		
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
		if (!placed || world.getGameTime() % 5 == 0) {
			if (!world.isRemote) {
				final boolean activated = this.logicComp.isActivated(); 

				if (!placed || logicLastWorld != activated) {
					// Make sure to update the world so that redstone will be updated
					//world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
					world.setBlockState(pos, LogisticsSensorBlock.getStateWithActive(activated), 3);
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
	public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newState) {
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