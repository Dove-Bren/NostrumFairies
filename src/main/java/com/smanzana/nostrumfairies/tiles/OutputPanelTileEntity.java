package com.smanzana.nostrumfairies.tiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.FairyBlocks;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWithdrawItem;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.ILogicListener;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.util.ContainerUtil.IAutoContainerInventory;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class OutputPanelTileEntity extends LogisticsTileEntity implements ITickableTileEntity, ILogisticsLogicProvider, ILogicListener, IAutoContainerInventory {

	private static final int SLOTS = 3;
	private static final String NBT_TEMPLATES = "templates";
	private static final String NBT_TEMPLATE_INDEX = "index";
	private static final String NBT_TEMPLATE_ITEM = "item";
	private static final String NBT_LOGIC_COMP = "logic";
	
	private NonNullList<ItemStack> templates;
	private LogisticsItemWithdrawRequester requester;
	private int ticksExisted; // Not persisted
	private final LogisticsLogicComponent logicComp;
	
	public OutputPanelTileEntity() {
		super(FairyTileEntities.OutputPanelTileEntityType);
		templates = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		logicComp = new LogisticsLogicComponent(false, this);
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
	
	@Override
	public double getDefaultLogisticsRange() {
		return 10;
	}

	@Override
	public double getDefaultLinkRange() {
		return 10;
	}
	
	@Override
	public boolean canAccept(List<ItemDeepStack> stacks) {
		return false;
	}

	@Override
	public Collection<ItemStack> getItems() {
		// Output chests don't offer their items to the network
		return LogisticsTileEntity.emptyList;
	}
	
	public void setTemplate(int index, @Nonnull ItemStack template) {
		Validate.notNull(template);
		if (index < 0 || index >=  SLOTS) {
			return;
		}
		
		ItemStack temp = template.isEmpty() ? ItemStack.EMPTY : template.copy();
		templates.set(index, temp);
		this.markDirty();
	}
	
	public @Nonnull ItemStack getTemplate(int index) {
		if (index < 0 || index >=  SLOTS) {
			return ItemStack.EMPTY;
		}
		
		return templates.get(index);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		// Save templates
		ListNBT templates = new ListNBT();
		for (int i = 0; i < SLOTS; i++) {
			ItemStack stack = this.getTemplate(i);
			if (stack.isEmpty()) {
				continue;
			}
			
			CompoundNBT template = new CompoundNBT();
			
			template.putInt(NBT_TEMPLATE_INDEX, i);
			template.put(NBT_TEMPLATE_ITEM, stack.write(new CompoundNBT()));
			
			templates.add(template);
		}
		nbt.put(NBT_TEMPLATES, templates);
		
		nbt.put(NBT_LOGIC_COMP, this.logicComp.write(new CompoundNBT()));
		
		return nbt;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		templates = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		
		// Reload templates
		ListNBT list = nbt.getList(NBT_TEMPLATES, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT template = list.getCompound(i);
			int index = template.getInt(NBT_TEMPLATE_INDEX);
			
			if (index < 0 || index > SLOTS) {
				NostrumFairies.logger.error("Found serialized template with invalid index! " + index + " outside of " + SLOTS);
				continue;
			}
			
			ItemStack stack = ItemStack.read(template.getCompound(NBT_TEMPLATE_ITEM));
			
			templates.set(index, stack);
		}
		
		CompoundNBT tag = nbt.getCompound(NBT_LOGIC_COMP);
		if (tag != null) {
			this.logicComp.read(tag);
		}
		
		// Do super afterwards so taht we have templates already
		super.read(state, nbt);
	}
	
	protected LogisticsItemWithdrawRequester makeRequester(LogisticsNetwork network, LogisticsTileEntityComponent networkComponent) {
		return new LogisticsItemWithdrawRequester(network, true, networkComponent) {
			@Override
			protected List<LogisticsTaskWithdrawItem> filterActiveRequests(final List<LogisticsTaskWithdrawItem> taskList) {
//					List<LogisticsTaskWithdrawItem> list = new ArrayList<>(taskList.size());
//					
//					for (LogisticsTaskWithdrawItem task : taskList) {
//						if (!task.isComplete()) { // Filter out completed tasks
//							list.add(task); 
//						}
//					}
//					
//					return list;
				return super.filterActiveRequests(taskList);
			}
		}; // TODO make using buffer chests configurable!;
	}
	
	@Override
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		super.setNetworkComponent(component);
		logicComp.setNetwork(component.getNetwork());
		
		if (world != null && !world.isRemote && requester == null) {
			requester = makeRequester(this.networkComponent.getNetwork(), this.networkComponent);
			requester.updateRequestedItems(getItemRequests());
		}
	}
	
	@Override
	public void setWorldAndPos(World worldIn, BlockPos pos) {
		super.setWorldAndPos(worldIn, pos);
		logicComp.setLocation(worldIn, pos);
		
		if (this.networkComponent != null && !worldIn.isRemote && requester == null) {
			requester = makeRequester(this.networkComponent.getNetwork(), this.networkComponent);
			//requester.updateRequestedItems(getItemRequests());
		}
	}
	
	@Override
	public void onLeaveNetwork() {
		if (!world.isRemote && requester != null) {
			requester.clearRequests();
			requester.setNetwork(null);
		}
		
		super.onLeaveNetwork();
		logicComp.setNetwork(null);
	}
	
	@Override
	public void onJoinNetwork(LogisticsNetwork network) {
		if (!world.isRemote && requester != null) {
			requester.setNetwork(network);
			requester.updateRequestedItems(getItemRequests());
		}
		
		super.onJoinNetwork(network);
		logicComp.setNetwork(network);
	}
	
	private @Nullable IItemHandler getLinkedInventoryHandler() {
		final Direction direction = getFacing();
		final BlockPos linkPos = pos.offset(direction);
		final TileEntity te = world.getTileEntity(linkPos);
		if (te != null) {
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
				@Nullable IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).orElse(null);
				return handler;
			}
		}
		
		return null;
	}
	
	private @Nullable IInventory getLinkedInventory() {
		final Direction direction = getFacing();
		final BlockPos linkPos = pos.offset(direction);
		final TileEntity te = world.getTileEntity(linkPos);
		if (te != null) {
			if (te instanceof IInventory) {
				return (IInventory) te;
			}
		}
		
		return null;
	}
	
	private final List<ItemDeepStack> linkedItemList = new ArrayList<>();
	
	/**
	 * Get the items in the linked inventory, if any
	 * @return
	 */
	private List<ItemDeepStack> getLinkedItems() {
		linkedItemList.clear();
		final @Nullable IItemHandler handler = getLinkedInventoryHandler();
		if (handler != null) {
			return ItemDeepStack.toDeepList(linkedItemList, () -> {
				return new Iterator<ItemStack>() {
					
					private int i = 0;
					
					@Override
					public boolean hasNext() {
						return i < handler.getSlots();
					}

					@Override
					public ItemStack next() {
						return handler.getStackInSlot(i++);
					}
				};
			});
		} else {
			final @Nullable IInventory inv = getLinkedInventory();
			if (inv != null) {
				return ItemDeepStack.toDeepList(linkedItemList, inv);
			}
		}
		
		return linkedItemList;
	}
	
	private NonNullList<ItemStack> getItemRequests() {
		
		// Globally return 0 requests if logic says we shouldn't run
		if (!logicComp.isActivated()) {
			return NonNullList.create();
		}
		
		final NonNullList<ItemStack> requests = NonNullList.create();
		final List<ItemDeepStack> available = getLinkedItems();
		
		for (int i = 0; i < templates.size(); i++) {
			if (templates.get(i).isEmpty()) {
				continue;
			}
			int wanted = templates.get(i).getCount();
			
			// Find ItemDeepStack for this template
			ItemDeepStack found = null;
			for (ItemDeepStack stack : available) {
				if (stack.canMerge(templates.get(i))) {
					found = stack;
					break;
				}
			}
			
			if (found != null) {
				final int wantedCount = wanted;
				wanted -= found.getCount();
				found.add(-wantedCount);
			}
			
			if (wanted > 0) {
				ItemStack req = templates.get(i).copy();
				req.setCount(wanted);
				requests.add(req);
			}
		}
		
		return requests;
	}
	
	@Override
	public void takeItem(ItemStack stack) {
		// If there's an option, take from slots that don't have templates first
		super.takeItem(stack);
		// TODO
	}
	
	@Override
	public void addItem(ItemStack stack) {
		// Add items to the linked inventory
		final @Nullable IItemHandler handler = getLinkedInventoryHandler();
		ItemStack remaining = stack.copy();
		if (handler != null) {
			remaining = Inventories.addItem(handler, remaining);
		} else {
			final @Nullable IInventory inv = getLinkedInventory();
			if (inv != null) {
				remaining = Inventories.addItem(inv, remaining);
			}
		}
		
		// Any leftover?
		if (!remaining.isEmpty()) {
			ItemEntity item = new ItemEntity(this.world, this.pos.getX() + .5, this.pos.getY() + 1, this.pos.getZ() + .5, remaining);
			world.addEntity(item);
		}
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		tickRequester();
	}
	
	public Direction getFacing() {
		if (world != null) {
			BlockState state = world.getBlockState(pos);
			try {
				return FairyBlocks.outputPanel.getFacing(state);
			} catch (Exception e) {
				;
			}
		}
		
		return Direction.NORTH;
	}
	
	protected void tickRequester() {
		if (world != null && !world.isRemote && requester != null) {
			requester.updateRequestedItems(getItemRequests());
		}
	}
	
	@Override
	public void tick() {
		ticksExisted++;
		if (this.ticksExisted % 8 == 0) {
			tickRequester();
		}
	}
	
	public void notifyNeighborChanged() {
		logicComp.onWorldUpdate();
	}

	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getField(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setField(int index, int val) {
		// TODO Auto-generated method stub
		
	}
}