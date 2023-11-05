package com.smanzana.nostrumfairies.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;
import com.smanzana.nostrummagica.tiles.MimicBlockTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

public class TemplateBlockTileEntity extends MimicBlockTileEntity {
	
	private static final String NBT_STATE = "state";
	
	private BlockState state;
	
	public TemplateBlockTileEntity() {
		this(null);
	}
	
	public TemplateBlockTileEntity(BlockState state) {
		super(FairyTileEntities.TemplateBlockTileEntityType);
		setBlockState(state);
	}
	
//	@Override
//	public boolean hasFastRenderer() {
//		return true;
//	}
	
	public void setBlockState(BlockState state) {
		this.state = state;
		this.setDataBlock(state);
		this.updateBlock(); // Refresh model
		this.markDirty();
	}
	
	public @Nullable BlockState getTemplateState() {
		return state;
	}
	
	@Override
	protected @Nonnull BlockState refreshState() {
		// Don't detect any world blocks and just send ours
		return getTemplateState() == null ? Blocks.STONE.getDefaultState() : getTemplateState();
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		
		if (state != null) {
			compound.put(NBT_STATE, NBTUtil.writeBlockState(state));
		}
		
		return compound;
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		
		if (this.state != null) {
			this.setBlockState(null);
		}
		
		if (compound.contains(NBT_STATE)) {
			this.state = NBTUtil.readBlockState(compound.getCompound(NBT_STATE));
			if (this.world != null && this.world.isRemote) {
				StaticTESRRenderer.instance.update(world, pos, this);
			}
		}
	}
	
	protected void flush() {
		if (world != null && !world.isRemote) {
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		if (world != null && world.isRemote) {
			StaticTESRRenderer.instance.update(world, pos, null);
		}
	}
}