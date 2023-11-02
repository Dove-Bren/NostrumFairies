package com.smanzana.nostrumfairies.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.client.render.stesr.StaticTESRRenderer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants.NBT;

public class TemplateBlockTileEntity extends TileEntity {
	
	private IBlockState state;
	
	public TemplateBlockTileEntity() {
		this(null);
	}
	
	public TemplateBlockTileEntity(IBlockState state) {
		this.state = state;
	}
	
	@Override
	public boolean hasFastRenderer() {
		return true;
	}
	
	public void setBlockState(IBlockState state) {
		this.state = state;
		this.markDirty();
	}
	
	public @Nullable IBlockState getTemplateState() {
		return state;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	private static final String NBT_STATE = "state";
	
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		
		if (state != null) {
			compound.setInteger(NBT_STATE, Block.getStateId(state));
		}
		
		return compound;
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		if (this.state != null) {
			this.setBlockState(null);
		}
		
		if (compound.hasKey(NBT_STATE, NBT.TAG_INT)) {
			this.state = Block.getStateById(compound.getInteger(NBT_STATE));
			if (this.world != null && this.world.isRemote) {
				StaticTESRRenderer.instance.update(world, pos, this);
			}
		}
	}
	
	protected void flush() {
		if (world != null && !world.isRemote) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if (world != null && world.isRemote) {
			StaticTESRRenderer.instance.update(world, pos, null);
		}
	}
}