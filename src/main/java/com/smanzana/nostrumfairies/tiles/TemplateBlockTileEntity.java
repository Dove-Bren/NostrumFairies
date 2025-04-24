package com.smanzana.nostrumfairies.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrummagica.tile.MimicBlockTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TemplateBlockTileEntity extends MimicBlockTileEntity {
	
	private static final String NBT_STATE = "state";
	
	private BlockState state;
	
	public TemplateBlockTileEntity(BlockPos pos, BlockState state) {
		this(pos, state, null);
	}
	
	public TemplateBlockTileEntity(BlockPos pos, BlockState state, BlockState mimicState) {
		super(FairyTileEntities.TemplateBlockTileEntityType, pos, state);
		setBlockState(mimicState);
	}
	
//	@Override
//	public boolean hasFastRenderer() {
//		return true;
//	}
	
	public void setBlockState(BlockState state) {
		this.state = state;
		this.setDataBlock(state);
		this.updateBlock(); // Refresh model
		this.setChanged();
	}
	
	public @Nullable BlockState getTemplateState() {
		return state;
	}
	
	@Override
	public void setLevel(Level worldIn) {
		super.setLevel(worldIn);
	}
	
	@Override
	protected @Nonnull BlockState refreshState() {
		// Don't detect any world blocks and just send ours
		return getTemplateState() == null ? Blocks.STONE.defaultBlockState() : getTemplateState();
	}
	
	// Notably, this makes sending chunked updates AND discrete updates send the whole TE instead of a trimmed version
	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithId();
	}
	
	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		
		if (state != null) {
			compound.put(NBT_STATE, NbtUtils.writeBlockState(state));
		}
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		if (this.state != null) {
			this.setBlockState(null);
		}
		
		if (compound.contains(NBT_STATE)) {
			this.state = NbtUtils.readBlockState(compound.getCompound(NBT_STATE));
			if (this.level != null && this.level.isClientSide) {
				StaticTESRRenderer.instance.update(level, worldPosition, this);
			}
		}
	}
	
	protected void flush() {
		if (level != null && !level.isClientSide) {
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2);
		}
	}
	
	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level != null && level.isClientSide) {
			StaticTESRRenderer.instance.update(level, worldPosition, null);
		}
	}
}