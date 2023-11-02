package com.smanzana.nostrumfairies.logistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.tiles.LogisticsTileEntity;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * A fake summary version of a logistics network.
 * These reflect real networks on the server, but in a simplified way on the client.
 * @author Skyler
 *
 */
public class FakeLogisticsNetwork extends LogisticsNetwork {

	public FakeLogisticsNetwork(LogisticsNetwork realNetwork) {
		super(realNetwork.getUUID(), false); // no registration
		
		for (ILogisticsComponent comp : realNetwork.components) {
			this.components.add(new FakeLogisticsComponent(comp));
		}
		this.dirty(); // changed components.
		this.rebuildGraph();
	}
	
	public FakeLogisticsNetwork(UUID id) {
		super(id, false);
	}
	
	public FakeLogisticsNetwork() {
		super();
	}
	
	@Override
	public void dissolveNetwork() {
		super.dissolveNetwork();
		
		// Clean up components, as new ones will be created by a replacement network if there is one.
//		for (ILogisticsComponent component : this.components) {
//			//component.onLeaveNetwork();
//		}
		
		components.clear();
		this.dirty();
		this.rebuildGraph();
	}
	
	private static final String NBT_ID = "uuid";
	private static final String NBT_COMPONENTS = "components";

	@Override
	public CompoundNBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		
		tag.setUniqueId(NBT_ID, getUUID());
		NBTTagList list = new NBTTagList();
		for (ILogisticsComponent comp : components) {
			list.appendTag(comp.toNBT());
		}
		tag.setTag(NBT_COMPONENTS, list);
		
		return tag;
	}
	
	public static LogisticsNetwork fromNBT(CompoundNBT tag) {
		UUID id = tag.getUniqueId(NBT_ID);
		FakeLogisticsNetwork network = new FakeLogisticsNetwork(id);
		
		NBTTagList list = tag.getTagList(NBT_COMPONENTS, NBT.TAG_COMPOUND);
		for (int i = list.tagCount() - 1; i >= 0; i--) {
			ILogisticsComponent comp = FakeLogisticsComponent.fromNBT(list.getCompoundTagAt(i));
			network.components.add(comp);
			comp.onJoinNetwork(network);
		}
		
		network.dirty();
		network.rebuildGraph();
		
		return network;
	}

	@Override
	public boolean addComponent(ILogisticsComponent component) {
		// Fake! Do nothing!
		return true;
	}

	@Override
	public void removeComponent(ILogisticsComponent component) {
		// Fake! do nothing!
		;
	}

	@Override
	public void mergeNetworkIn(LogisticsNetwork otherNetwork) {
		// Fake! do nothing!
		;
	}
	
	public static class FakeLogisticsComponent implements ILogisticsComponent {

		private FakeLogisticsNetwork network;
		private double logisticsRange;
		private double linkRange;
		private World world;
		private BlockPos pos;
		private List<ItemStack> items;
		
		public FakeLogisticsComponent(ILogisticsComponent real) {
			this.logisticsRange = real.getLogisticRange();
			this.linkRange = real.getLogisticsLinkRange();
			this.world = real.getWorld();
			this.pos = real.getPosition();
			this.items = Lists.newArrayList(real.getItems());
		}
		
		private FakeLogisticsComponent(double logisticsRange, double linkRange, World world, BlockPos pos, List<ItemStack> items) {
			this.logisticsRange = logisticsRange;
			this.linkRange = linkRange;
			this.world = world;
			this.pos = pos;
			this.items = items;
		}
		
		@Override
		public void onJoinNetwork(LogisticsNetwork network) {
			// Straight cast here. If it's not, a fake network component
			// is being added to a real network!
			this.network = (FakeLogisticsNetwork) network;
			
			TileEntity te = this.world.getTileEntity(this.pos);
			if (te != null && te instanceof LogisticsTileEntity) {
				LogisticsTileEntity ent = (LogisticsTileEntity) te;
				if (ent.getNetworkComponent() != null) {
					ent.getNetworkComponent().onJoinNetwork(network);
				}
			}
		}

		@Override
		public void onLeaveNetwork() {
			this.network = null;
			
			TileEntity te = this.world.getTileEntity(this.pos);
			if (te != null && te instanceof LogisticsTileEntity) {
				LogisticsTileEntity ent = (LogisticsTileEntity) te;
				if (ent.getNetworkComponent() != null) {
					ent.getNetworkComponent().onLeaveNetwork();
				}
			}
		}

		@Override
		public double getLogisticRange() {
			return this.logisticsRange;
		}

		@Override
		public double getLogisticsLinkRange() {
			return this.linkRange;
		}

		@Override
		public BlockPos getPosition() {
			return this.pos;
		}

		@Override
		public World getWorld() {
			return this.world;
		}

		@Override
		public Collection<ItemStack> getItems() {
			return this.items;
		}
		
		@Override
		public boolean canAccept(List<ItemDeepStack> stacks) {
			return false;
		}

		@Override
		public String getSerializationTag() {
			return "_FakeLogisticsComponent";
		}
		
		private static final String NBT_COMP_LOG_RANGE = "logistic_range";
		private static final String NBT_COMP_LINK_RANGE = "link_range";
		private static final String NBT_COMP_DIM = "dimension";
		private static final String NBT_COMP_POS = "pos";
		private static final String NBT_COMP_ITEMS = "items";

		@Override
		public CompoundNBT toNBT() {
			CompoundNBT tag = new CompoundNBT();
			
			tag.setDouble(NBT_COMP_LOG_RANGE, logisticsRange);
			tag.setDouble(NBT_COMP_LINK_RANGE, linkRange);
			if (world != null) {
				tag.putInt(NBT_COMP_DIM, world.provider.getDimension());
			}
			tag.setLong(NBT_COMP_POS, pos.toLong());
			
			// Note: since fake components don't need individual stack info and just typeXcount info, we _could_
			// send ItemDeepStacks here instead, and convert to regular itemstacks.
			// That'd mean the stacks on the client woulnd't match the server, but overal quantity would.
			
			NBTTagList list = new NBTTagList();
			for (ItemStack stack : this.items) {
				if (stack.isEmpty()) {
					continue;
				}
				list.appendTag(stack.writeToNBT(new CompoundNBT()));
			}
			tag.setTag(NBT_COMP_ITEMS, list);
			
			return tag;
		}
		
		public static FakeLogisticsComponent fromNBT(CompoundNBT tag) {
			double logisticsRange = tag.getDouble(NBT_COMP_LOG_RANGE);
			double linkRange = tag.getDouble(NBT_COMP_LINK_RANGE);
			int dim = tag.getInt(NBT_COMP_DIM);
			long pos = tag.getLong(NBT_COMP_POS);
			World world = null;
			
			NBTTagList list = tag.getTagList(NBT_COMP_ITEMS, NBT.TAG_COMPOUND);
			List<ItemStack> items = new ArrayList<>(list.tagCount());
			for (int i = list.tagCount() - 1; i >= 0; i--) {
				items.add(new ItemStack(list.getCompoundTagAt(i)));
			}
			
			world = NostrumFairies.getWorld(dim);
			
			return new FakeLogisticsComponent(logisticsRange, linkRange, world, BlockPos.fromLong(pos), items);
		}
		
		public void overrideFromNBT(CompoundNBT tag) {
			double logisticsRange = tag.getDouble(NBT_COMP_LOG_RANGE);
			double linkRange = tag.getDouble(NBT_COMP_LINK_RANGE);
			int dim = tag.getInt(NBT_COMP_DIM);
			long pos = tag.getLong(NBT_COMP_POS);
			World world = null;
			
			NBTTagList list = tag.getTagList(NBT_COMP_ITEMS, NBT.TAG_COMPOUND);
			List<ItemStack> items = new ArrayList<>(list.tagCount());
			for (int i = list.tagCount() - 1; i >= 0; i--) {
				items.add(new ItemStack(list.getCompoundTagAt(i)));
			}
			
			world = NostrumFairies.getWorld(dim);
			
			this.linkRange = linkRange;
			this.logisticsRange = logisticsRange;
			this.world = world;
			this.pos = BlockPos.fromLong(pos);
			this.items = items;
		}
		
		public @Nullable LogisticsNetwork getNetwork() {
			return network;
		}

		@Override
		public void takeItem(ItemStack stack) {
			;
		}

		@Override
		public void addItem(ItemStack stack) {
			;
		}

		@Override
		public boolean isItemBuffer() {
			return false;
		}
	}
	
}
