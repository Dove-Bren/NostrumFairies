package com.smanzana.nostrumfairies.tiles;

import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.ILogicListener;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LogisticsSensorTileEntity extends LogisticsTileEntity implements ITickableTileEntity, ILogicListener, ILogisticsLogicProvider {
	
	private boolean logicLastWorld;
	
	private static final String NBT_LOGIC_COMP = "logic";
	
	private final LogisticsLogicComponent logicComp;
	
	private boolean placed = false;
	
	public LogisticsSensorTileEntity() {
		super(FairyTileEntities.LogisticsSensorTileEntityType);
		logicComp = new LogisticsLogicComponent(true, this);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		CompoundNBT tag = new CompoundNBT();
		logicComp.write(tag);
		nbt.put(NBT_LOGIC_COMP, tag);
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		CompoundNBT sub = nbt.getCompound(NBT_LOGIC_COMP);
		if (sub != null) {
			logicComp.read(sub);
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
	public void tick() {
		if (!placed || world.getGameTime() % 5 == 0) {
			if (!world.isRemote) {
				final boolean activated = this.logicComp.isActivated(); 

				if (!placed || logicLastWorld != activated) {
					// Make sure to update the world so that redstone will be updated
					//world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
					world.setBlockState(pos, FairyBlocks.logisticsSensor.getStateWithActive(activated), 3);
					logicLastWorld = activated;
					
					// Copied from comparator
					{
						for (Direction direction : Direction.values()) {
							BlockPos blockpos = pos.offset(direction);
							if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(world, pos, world.getBlockState(pos), java.util.EnumSet.of(direction), false).isCanceled())
								return;
							world.neighborChanged(blockpos, this.getBlockState().getBlock(), pos);
							world.notifyNeighborsOfStateExcept(blockpos, this.getBlockState().getBlock(), direction.getOpposite());
						}
					}
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