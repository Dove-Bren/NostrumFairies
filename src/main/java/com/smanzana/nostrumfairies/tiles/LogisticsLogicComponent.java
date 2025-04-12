package com.smanzana.nostrumfairies.tiles;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Bundles up looking for certain logic network conditions. Intended to be embedded in TileEntities that use this sort of behavior.
 * Hooks to a logistics network and can be configured to look for simple conditions surrounding types of items in the network. For example,
 * a component might be configured to 'activate' when there are <em>fewer</em> than <strong>50</strong> <u>oak saplings</u>.
 * @author Skyler
 *
 */
public class LogisticsLogicComponent {
	
	public static interface ILogicListener {
		
		public void onStateChange(boolean activated);
		
		public void onDirty();
	}
	
	public static enum LogicMode {
		LOGIC,
		ALWAYS,
		REDSTONE_HIGH,
		REDSTONE_LOW,
	}
	
	public static enum LogicOp {
		LESS,
		EQUAL,
		MORE;
	}

	private static final String NBT_LOGIC_MODE = "mode";
	private static final String NBT_LOGIC_ITEM = "logic_item";
	private static final String NBT_LOGIC_OP = "logic_op";
	private static final String NBT_LOGIC_COUNT = "logic_count";
	
	private LogicMode mode;
	private LogicOp op;
	private int count;
	private @Nonnull ItemStack template = ItemStack.EMPTY;
	
	private @Nullable LogisticsNetwork network;
	private final @Nullable ILogicListener listener; 
	private final boolean logicOnly;
	private @Nullable Level world;
	private @Nullable BlockPos pos;
	
	private UUID logicCacheID;
	private boolean redstoneCacheValid;

	private boolean logicValidCache;
	
	public LogisticsLogicComponent(boolean logicOnly, @Nullable ILogicListener listener) {
		this.listener = listener;
		this.logicOnly = logicOnly;
		this.op = LogicOp.EQUAL;
		
		if (this.logicOnly) {
			this.mode = LogicMode.LOGIC;
		} else {
			this.mode = LogicMode.ALWAYS;
		}
	}
	
	protected void dirty() {
		if (listener != null) {
			listener.onDirty();
		}
	}
	
	protected void updateLogic() {
		if (mode == LogicMode.ALWAYS) {
			this.logicValidCache = true;
			return;
		}
		
		if (mode == LogicMode.LOGIC) {
			ItemStack req = this.getLogicTemplate();
			if (req.isEmpty() || network == null) {
				this.logicCacheID = null;
				this.logicValidCache = false;
				return;
			}
			
			if (this.logicCacheID == null || !logicCacheID.equals(network.getCacheKey())) {
				logicCacheID = network.getCacheKey();
				
				long available = network.getItemCount(req);
				
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
			
			return;
		}
		
		if (mode == LogicMode.REDSTONE_HIGH || mode == LogicMode.REDSTONE_LOW) {
			if (this.world == null || this.pos == null) {
				this.redstoneCacheValid = false;
				this.logicValidCache = false;
				return;
			}
			
			if (!redstoneCacheValid) {
				// Check the world
				final boolean redstonePresent = world.hasNeighborSignal(pos);
				this.redstoneCacheValid = true;
				this.logicValidCache = (redstonePresent == (mode == LogicMode.REDSTONE_HIGH));
			}
			
			return;
		}
	}
	
	protected boolean checkConditions() {
		updateLogic();
		return logicValidCache;
	}
	
	protected void bustCache() {
		logicCacheID = null;
		redstoneCacheValid = false;
	}
	
	public void setNetwork(@Nullable LogisticsNetwork network) {
		this.network = network;
		this.logicCacheID = null;
	}
	
	public void setLocation(@Nullable Level world, @Nullable BlockPos pos) {
		this.redstoneCacheValid = false;
		this.world = world;
		this.pos = pos;
		
		if (!this.logicOnly && (world == null || pos == null)) {
			throw new RuntimeException("Logic components can't use redstone unless a world and position are passed");
		}
	}
	
	public boolean isLogicOnly() {
		return this.logicOnly;
	}
	
	public LogicMode getLogicMode() {
		return this.mode;
	}
	
	public int getLogicCount() {
		return this.count;
	}
	
	public LogicOp getLogicOp() {
		return this.op;
	}
	
	public @Nonnull ItemStack getLogicTemplate() {
		return this.template;
	}
	
	public void setLogicMode(LogicMode mode) {
		if (this.logicOnly && mode != LogicMode.LOGIC) {
			throw new RuntimeException("Component is locked to logic mode. Cannot change mode!");
		}
		this.mode = mode;
		bustCache();
		this.dirty();
	}
	
	public void setLogicOp(LogicOp op) {
		this.op = op;
		bustCache();
		this.dirty();
	}
	
	public void setLogicTemplate(@Nonnull ItemStack stack) {
		this.template = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
		bustCache();
		this.dirty();
	}

	public void setLogicCount(int val) {
		this.count = val;
		bustCache();
		this.dirty();
	}
	
	public CompoundTag write(CompoundTag nbt) {
		nbt.putString(NBT_LOGIC_MODE, mode.name());
		nbt.putInt(NBT_LOGIC_COUNT, count);
		nbt.putString(NBT_LOGIC_OP, op.name());
		if (!template.isEmpty()) {
			nbt.put(NBT_LOGIC_ITEM, template.serializeNBT());
		}
		
		return nbt;
	}
	
	public void read(CompoundTag nbt) {
		try {
			this.mode = LogicMode.valueOf(nbt.getString(NBT_LOGIC_MODE).toUpperCase());
		} catch (Exception e) {
			this.mode = LogicMode.ALWAYS;
		}
		
		try {
			this.op = LogicOp.valueOf(nbt.getString(NBT_LOGIC_OP).toUpperCase());
		} catch (Exception e) {
			this.op = LogicOp.EQUAL;
		}
		
		this.template = ItemStack.of(nbt.getCompound(NBT_LOGIC_ITEM));
		this.count = nbt.getInt(NBT_LOGIC_COUNT);
		
		this.logicCacheID = null;
		
		if (this.logicOnly) {
			this.mode = LogicMode.LOGIC;
		}
	}
	
	/**
	 * Check and return whether conditions are satisfied for this logic component.
	 * Logic components do heavy caching, so this should be safe to call very often.
	 * @return
	 */
	public boolean isActivated() {
		return this.checkConditions();
	}
	
	/**
	 * Inform the logic component that the world is changed, and it should clear its caches of world conditions.
	 * If you never call this, you should never rely on the redstone modes
	 */
	public void onWorldUpdate() {
		this.redstoneCacheValid = false;
	}
	
}
