package com.smanzana.nostrumfairies.tiles;

import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.ILogicListener;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class LogisticsSensorTileEntity extends LogisticsTileEntity implements TickableBlockEntity, ILogicListener, ILogisticsLogicProvider {
	
	private boolean logicLastWorld;
	
	private static final String NBT_LOGIC_COMP = "logic";
	
	private final LogisticsLogicComponent logicComp;
	
	private boolean placed = false;
	
	public LogisticsSensorTileEntity() {
		super(FairyTileEntities.LogisticsSensorTileEntityType);
		logicComp = new LogisticsLogicComponent(true, this);
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt) {
		nbt = super.save(nbt);
		
		CompoundTag tag = new CompoundTag();
		logicComp.write(tag);
		nbt.put(NBT_LOGIC_COMP, tag);
		
		return nbt;
	}
	
	@Override
	public void load(BlockState state, CompoundTag nbt) {
		super.load(state, nbt);
		
		CompoundTag sub = nbt.getCompound(NBT_LOGIC_COMP);
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
	public void setLevelAndPosition(Level worldIn, BlockPos pos) {
		super.setLevelAndPosition(worldIn, pos);
		
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
	public void setChanged() {
		super.setChanged();
	}
	
	public void notifyNeighborChanged() {
		; // This used to bust logic cache. Needed?
	}
	
	@Override
	public void clearRemoved() {
		super.clearRemoved();
	}
	
	@Override
	public void tick() {
		if (!placed || level.getGameTime() % 5 == 0) {
			if (!level.isClientSide) {
				final boolean activated = this.logicComp.isActivated(); 

				if (!placed || logicLastWorld != activated) {
					// Make sure to update the world so that redstone will be updated
					//world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock());
					level.setBlock(worldPosition, FairyBlocks.logisticsSensor.getStateWithActive(activated), 3);
					logicLastWorld = activated;
					
					// Copied from comparator
					{
						for (Direction direction : Direction.values()) {
							BlockPos blockpos = worldPosition.relative(direction);
							if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(level, worldPosition, level.getBlockState(worldPosition), java.util.EnumSet.of(direction), false).isCanceled())
								return;
							level.neighborChanged(blockpos, this.getBlockState().getBlock(), worldPosition);
							level.updateNeighborsAtExceptFromFacing(blockpos, this.getBlockState().getBlock(), direction.getOpposite());
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
		this.setChanged();
	}
	
	@Override
	public LogisticsLogicComponent getLogicComponent() {
		return this.logicComp;
	}
}