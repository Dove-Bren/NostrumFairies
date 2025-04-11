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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

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
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		
		tag.putUUID(NBT_ID, getUUID());
		ListTag list = new ListTag();
		for (ILogisticsComponent comp : components) {
			list.add(comp.toNBT());
		}
		tag.put(NBT_COMPONENTS, list);
		
		return tag;
	}
	
	public static LogisticsNetwork fromNBT(CompoundTag tag) {
		UUID id = tag.getUUID(NBT_ID);
		FakeLogisticsNetwork network = new FakeLogisticsNetwork(id);
		
		ListTag list = tag.getList(NBT_COMPONENTS, Tag.TAG_COMPOUND);
		for (int i = list.size() - 1; i >= 0; i--) {
			ILogisticsComponent comp = FakeLogisticsComponent.fromNBT(list.getCompound(i));
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
		private Level world;
		private BlockPos pos;
		private List<ItemStack> items;
		
		public FakeLogisticsComponent(ILogisticsComponent real) {
			this.logisticsRange = real.getLogisticRange();
			this.linkRange = real.getLogisticsLinkRange();
			this.world = real.getWorld();
			this.pos = real.getPosition();
			this.items = Lists.newArrayList(real.getItems());
		}
		
		private FakeLogisticsComponent(double logisticsRange, double linkRange, Level world, BlockPos pos, List<ItemStack> items) {
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
			
			BlockEntity te = this.world.getBlockEntity(this.pos);
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
			
			BlockEntity te = this.world.getBlockEntity(this.pos);
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
		public Level getWorld() {
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
		public CompoundTag toNBT() {
			CompoundTag tag = new CompoundTag();
			
			tag.putDouble(NBT_COMP_LOG_RANGE, logisticsRange);
			tag.putDouble(NBT_COMP_LINK_RANGE, linkRange);
			if (world != null) {
				tag.putString(NBT_COMP_DIM, world.dimension().location().toString());
			}
			tag.put(NBT_COMP_POS, NbtUtils.writeBlockPos(pos));
			
			// Note: since fake components don't need individual stack info and just typeXcount info, we _could_
			// send ItemDeepStacks here instead, and convert to regular itemstacks.
			// That'd mean the stacks on the client woulnd't match the server, but overal quantity would.
			
			ListTag list = new ListTag();
			for (ItemStack stack : this.items) {
				if (stack.isEmpty()) {
					continue;
				}
				list.add(stack.save(new CompoundTag()));
			}
			tag.put(NBT_COMP_ITEMS, list);
			
			return tag;
		}
		
		public static FakeLogisticsComponent fromNBT(CompoundTag tag) {
			double logisticsRange = tag.getDouble(NBT_COMP_LOG_RANGE);
			double linkRange = tag.getDouble(NBT_COMP_LINK_RANGE);
			ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString(NBT_COMP_DIM)));
			BlockPos pos = NbtUtils.readBlockPos(tag.getCompound(NBT_COMP_POS));
			Level world = null;
			
			ListTag list = tag.getList(NBT_COMP_ITEMS, Tag.TAG_COMPOUND);
			List<ItemStack> items = new ArrayList<>(list.size());
			for (int i = list.size() - 1; i >= 0; i--) {
				items.add(ItemStack.of(list.getCompound(i)));
			}
			
			world = NostrumFairies.getWorld(dim);
			
			return new FakeLogisticsComponent(logisticsRange, linkRange, world, pos, items);
		}
		
		public void overrideFromNBT(CompoundTag tag) {
			double logisticsRange = tag.getDouble(NBT_COMP_LOG_RANGE);
			double linkRange = tag.getDouble(NBT_COMP_LINK_RANGE);
			ResourceKey<Level> dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString(NBT_COMP_DIM)));
			BlockPos pos = NbtUtils.readBlockPos(tag.getCompound(NBT_COMP_POS));
			Level world = null;
			
			ListTag list = tag.getList(NBT_COMP_ITEMS, Tag.TAG_COMPOUND);
			List<ItemStack> items = new ArrayList<>(list.size());
			for (int i = list.size() - 1; i >= 0; i--) {
				items.add(ItemStack.of(list.getCompound(i)));
			}
			
			world = NostrumFairies.getWorld(dim);
			
			this.linkRange = linkRange;
			this.logisticsRange = logisticsRange;
			this.world = world;
			this.pos = pos;
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
