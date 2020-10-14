package com.smanzana.nostrumfairies.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemDepositRequester;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class CraftingBlockTileEntity extends LogisticsChestTileEntity implements ILogisticsTaskListener, ITickable {

	private static final String NBT_TEMPLATES = "templates";
	private static final String NBT_BUILD_POINTS = "build_points";
	private static final String NBT_CRITERIA_MODE = "criteria_mode";
	private static final String NBT_CRITERIA_OP = "criteria_op";
	private static final String NBT_CRITERIA_COUNT = "criteria_count";
	
	private static final String NBT_TEMPLATE_INDEX = "index";
	private static final String NBT_TEMPLATE_ITEM = "item";
	
	public static enum CraftingCriteriaMode {
		ALWAYS,
		REDSTONE_HIGH,
		REDSTONE_LOW,
		LOGIC;
	}
	
	public static enum CraftingLogicOp {
		LESS,
		EQUAL,
		MORE;
	}
	
	private String displayName;
	private ItemStack[] templates;
	private float buildPoints; // out of 100
	//private ItemStack criteriaItem; item in slot [TEMPLATE_SLOTS + 1]
	private CraftingCriteriaMode criteriaMode;
	private CraftingLogicOp criteriaOp;
	private int criteriaCount;
	private LogisticsItemWithdrawRequester withdrawRequester;
	private LogisticsItemDepositRequester depositRequester;
	
	private final int TEMPLATE_SLOTS;
	
	// Transient validation vars
	private boolean recipeDirty;
	private boolean recipeValidCache;
	private @Nullable IRecipe recipeCache;
	private boolean[] recipeIssuesCache;
	private boolean ingredientsDirty;
	private boolean ingredientsValidCache;
	
	private UUID logicCacheID;
	private boolean logicValidCache;
	
	// Task data (transient)
	private List<LogisticsTaskWorkBlock> tasks;
	
	private boolean placed = false;
	
	public CraftingBlockTileEntity() {
		super();
		displayName = "Crafting Block";
		this.TEMPLATE_SLOTS = getCraftGridDim() * getCraftGridDim();
		templates = new ItemStack[TEMPLATE_SLOTS];
		recipeDirty = true;
		ingredientsDirty = true;
		recipeIssuesCache = new boolean[TEMPLATE_SLOTS];
		this.criteriaOp = CraftingLogicOp.EQUAL;
		this.criteriaMode = CraftingCriteriaMode.ALWAYS;
		this.tasks = new ArrayList<>();
		

		// Parent creates an inventory array of our getSizeInventory.
		// Note that the first TEMPLATE_SLOTS are the templates. The one after that
		// is the output
	}
	
	public abstract int getCraftGridDim();
	
	@Override
	public String getName() {
		return displayName;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}
	
	@Override
	public int getSizeInventory() {
		return getCraftGridDim() * getCraftGridDim() + 1 + 1; // creates an actual inventory in super class
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
		// We ship our output, so we don't offer it here
		return LogisticsTileEntity.emptyList;
	}
	
	public void setTemplate(int index, @Nullable ItemStack template) {
		if (index < 0 || index >=  TEMPLATE_SLOTS) {
			return;
		}
		
		ItemStack temp = template == null ? null : template.copy();
		templates[index] = temp;
		this.markDirty();
		this.ingredientsDirty = true;
		this.recipeDirty = true;
	}
	
	public float getProgress() {
		return this.buildPoints / 100f;
	}
	
	public float getBuildPoints() {
		return this.buildPoints;
	}
	
	public @Nullable ItemStack getTemplate(int index) {
		if (index < 0 || index >=  TEMPLATE_SLOTS) {
			return null;
		}
		
		return templates[index];
	}
	
	public @Nullable ItemStack getOutputStack() {
		return this.getStackInSlot(TEMPLATE_SLOTS);
	}
	
	public int getCriteriaCount() {
		return this.criteriaCount;
	}
	
	public CraftingCriteriaMode getCriteriaMode() {
		return this.criteriaMode;
	}
	
	public CraftingLogicOp getCriteriaOp() {
		return this.criteriaOp;
	}
	
	public @Nullable ItemStack getCriteriaTemplate() {
		return this.getStackInSlot(TEMPLATE_SLOTS + 1);
	}
	
	public void setCriteriaMode(CraftingCriteriaMode mode) {
		this.criteriaMode = mode;
		logicCacheID = null;
		this.markDirty();
	}
	
	public void setCriteriaOp(CraftingLogicOp op) {
		this.criteriaOp = op;
		logicCacheID = null;
	}

	public void setCriteriaCount(int val) {
		this.criteriaCount = val;
		this.markDirty();
	}
	
	/**
	 * Check whether this recipe can be crafted by this block.
	 * Huge note: this does NOT mean check inventory contents and see if ingredents are present.
	 * This simply means make sure the recipe is one that this block doesn't superficially block.
	 * Also note: per-block requirements are checked later, so don't do that here.
	 * @param recipe
	 * @return
	 */
	protected boolean canCraft(IRecipe recipe) {
		return true;
	}
	
	/**
	 * Check whether the provided item is one that this block can use in a recipe.
	 * @param item
	 * @return
	 */
	protected abstract boolean canCraftWith(ItemStack item);
	
	protected IRecipe findRecipe() {
		// Have to janki-fy the recipe manager, since the inventories it takes as input require a container to work
		InventoryCrafting inv = new InventoryCrafting(new Container() {

			@Override
			public boolean canInteractWith(EntityPlayer playerIn) {
				return true;
			}
			
			@Override
			public void detectAndSendChanges() {
				;
			}
		}, getCraftGridDim(), getCraftGridDim());
		
		for (int i = 0; i < TEMPLATE_SLOTS; i++) {
			if (this.templates[i] != null) {
				inv.setInventorySlotContents(i, templates[i]);
			}
		}
		
		for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
			if (canCraft(recipe) && recipe.matches(inv, worldObj)) {
				return recipe;
			}
		}
		
		return null;
	}
	
	/**
	 * Checks the currently-configured recipe and checks whether it exists and is one that this block can craft.
	 * @return
	 */
	public boolean validateRecipe() {
		if (this.recipeDirty) {
			this.recipeCache = findRecipe();
			
			recipeValidCache = (recipeCache != null);
			for (int i = 0; i < TEMPLATE_SLOTS; i++) {
				ItemStack template = templates[i];
				if (template == null) {
					recipeIssuesCache[i] = false; // no issue
				} else if (!this.canCraftWith(template)) {
					recipeIssuesCache[i] = true; // problem here
					recipeValidCache = false; // overall recipe's bad too
				} else {
					recipeIssuesCache[i] = false; // no issue
				}
			}
			
			this.recipeDirty = false;
		}
		
		return this.recipeValidCache;
	}
	
	public @Nullable IRecipe getRecipe() {
		if (!validateRecipe()) {
			return null;
		}
		
		// above call will cache recipe
		return this.recipeCache;
	}
	
	/**
	 * Checks whether all ingredients required are in place, and whether crafting can start.
	 * @return
	 */
	public boolean validateIngredients() {
		if (!validateRecipe()) {
			return false;
		}
		
		if (ingredientsDirty) {
			// Check whether all templates have items underneath them
			ingredientsValidCache = true;
			for (int i = 0; i < TEMPLATE_SLOTS; i++) {
				ItemStack template = templates[i];
				if (template == null) {
					continue;
				}
				
				ItemStack inSlot = this.getStackInSlot(i);
				if (inSlot == null) {
					ingredientsValidCache = false;
					break;
				}
			}
			
			if (ingredientsValidCache) {
				// Check output slot can fit
				// Have to janki-fy the recipe manager, since the inventories it takes as input require a container to work
				ingredientsValidCache = this.canOutput();
			}
			
			if (!ingredientsValidCache) {
				buildPoints = 0f;
			}
			
			ingredientsDirty = false;
			checkTasks();
		}
		
		return ingredientsValidCache;
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		if (!super.isItemValidForSlot(index, stack)) {
			return false;
		}
		
		if (index >= TEMPLATE_SLOTS) {
			// output slot -- can't put anything in it
			return true;
		}
		
		ItemStack template = getTemplate(index);
		if (template != null) {
			return ItemStacks.stacksMatch(template, stack);
		}
		
		// No template means we want nothing in that slot
		return false;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		// Save templates
		NBTTagList templates = new NBTTagList();
		for (int i = 0; i < TEMPLATE_SLOTS; i++) {
			ItemStack stack = this.getTemplate(i);
			if (stack == null) {
				continue;
			}
			
			NBTTagCompound template = new NBTTagCompound();
			
			template.setInteger(NBT_TEMPLATE_INDEX, i);
			template.setTag(NBT_TEMPLATE_ITEM, stack.writeToNBT(new NBTTagCompound()));
			
			templates.appendTag(template);
		}
		nbt.setTag(NBT_TEMPLATES, templates);
		
		nbt.setString(NBT_CRITERIA_MODE, criteriaMode.name());
//		if (criteriaItem != null) { // Handled by inventory saving
//			nbt.setTag(NBT_CRITERIA_TYPE, criteriaItem.serializeNBT());
//		}
		nbt.setInteger(NBT_CRITERIA_COUNT, criteriaCount);
		nbt.setString(NBT_CRITERIA_OP, criteriaOp.name());
		nbt.setFloat(NBT_BUILD_POINTS, buildPoints);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		templates = new ItemStack[TEMPLATE_SLOTS];
		
		// Reload templates
		NBTTagList list = nbt.getTagList(NBT_TEMPLATES, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound template = list.getCompoundTagAt(i);
			int index = template.getInteger(NBT_TEMPLATE_INDEX);
			
			if (index < 0 || index > TEMPLATE_SLOTS) {
				NostrumFairies.logger.error("Found serialized template with invalid index! " + index + " outside of " + TEMPLATE_SLOTS);
				continue;
			}
			
			ItemStack stack = ItemStack.loadItemStackFromNBT(template.getCompoundTag(NBT_TEMPLATE_ITEM));
			
			templates[index] = stack;
		}
		
		try {
			this.criteriaMode = CraftingCriteriaMode.valueOf(nbt.getString(NBT_CRITERIA_MODE).toUpperCase());
		} catch (Exception e) {
			this.criteriaMode = CraftingCriteriaMode.ALWAYS;
		}
		
		try {
			this.criteriaOp = CraftingLogicOp.valueOf(nbt.getString(NBT_CRITERIA_OP).toUpperCase());
		} catch (Exception e) {
			this.criteriaOp = CraftingLogicOp.EQUAL;
		}
		
		//this.criteriaItem = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag(NBT_CRITERIA_TYPE));
		this.criteriaCount = nbt.getInteger(NBT_CRITERIA_COUNT);
		this.buildPoints = nbt.getFloat(NBT_BUILD_POINTS);
		
		// Do super afterwards so taht we have templates already
		super.readFromNBT(nbt);
		
		this.recipeDirty = true;
		this.ingredientsDirty = true;
	}
	
	@Override
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		super.setNetworkComponent(component);
		
		if (worldObj != null && !worldObj.isRemote && withdrawRequester == null) {
			withdrawRequester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent);
			withdrawRequester.updateRequestedItems(getItemRequests());
			
			depositRequester = new LogisticsItemDepositRequester(this.networkComponent.getNetwork(), this.networkComponent);
			depositRequester.updateRequestedItems(getPushRequests());
		}
	}
	
	@Override
	public void setWorldObj(World worldIn) {
		super.setWorldObj(worldIn);
		
		if (this.networkComponent != null && !worldIn.isRemote) {
			if (withdrawRequester == null) {
				withdrawRequester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent);
				withdrawRequester.updateRequestedItems(getItemRequests());
				
				depositRequester = new LogisticsItemDepositRequester(this.networkComponent.getNetwork(), this.networkComponent);
				depositRequester.updateRequestedItems(getPushRequests());
			}
			
			checkTasks();
		}
	}
	
	@Override
	public void onLeaveNetwork() {
		if (!worldObj.isRemote && withdrawRequester != null) {
			withdrawRequester.clearRequests();
			withdrawRequester.setNetwork(null);
			depositRequester.clearRequests();
			depositRequester.setNetwork(null);
		}
		
		super.onLeaveNetwork();
	}
	
	@Override
	public void onJoinNetwork(LogisticsNetwork network) {
		if (!worldObj.isRemote && withdrawRequester != null) {
			withdrawRequester.setNetwork(network);
			withdrawRequester.updateRequestedItems(getItemRequests());
			depositRequester.setNetwork(network);
			depositRequester.updateRequestedItems(getPushRequests());
		}
		
		checkTasks();
		super.onJoinNetwork(network);
	}
	
	private List<ItemStack> getItemRequests() {
		List<ItemStack> requests = new LinkedList<>();
		
		for (int i = 0; i < templates.length; i++) {
			if (templates[i] == null) {
				continue;
			}
			
			ItemStack inSlot = this.getStackInSlot(i);
			int desire = templates[i].stackSize - (inSlot == null ? 0 : inSlot.stackSize);
			if (desire > 0) {
				ItemStack req = templates[i].copy();
				req.stackSize = desire;
				requests.add(req);
			}
		}
		
		return requests;
	}
	
	private List<ItemStack> getPushRequests() {
		return Lists.newArrayList(getOutputStack());
	}
	
	@Override
	public void takeItem(ItemStack stack) {
		super.takeItem(stack);
	}
	
	@Override
	public void addItem(ItemStack stack) {
		// Only allow popping items in over templates
		boolean anyChanges = false;
		for (int i = 0; i < templates.length; i++) {
			if (templates[i] == null) {
				continue;
			}
			
			if (!isItemValidForSlot(i, stack)) {
				// doesn't fit here anyways
				continue;
			}
			
			// if template count != stack count, try to add there
			ItemStack inSlot = this.getStackInSlot(i);
			int desire = templates[i].stackSize - (inSlot == null ? 0 : inSlot.stackSize);
			int amt = Math.min(stack.stackSize, desire);
			if (inSlot == null) {
				// take out template desire amount
				this.setInventorySlotContentsDirty(i, stack.splitStack(amt)); // doesn't set dirty
				anyChanges = true;
			} else {
				stack.stackSize -= amt;
				inSlot.stackSize += amt;
				anyChanges = true;
			}
			
			if (stack.stackSize <= 0) {
				break;
			}
		}
		
		if (anyChanges) {
			this.markDirty();
		}
		
		// Any leftover?
		if (stack != null && stack.stackSize > 0) {
			EntityItem ent = new EntityItem(worldObj, pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5, stack);
			worldObj.spawnEntityInWorld(ent);
		}
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		if (worldObj != null && !worldObj.isRemote && withdrawRequester != null) {
			withdrawRequester.updateRequestedItems(getItemRequests());
			depositRequester.updateRequestedItems(getPushRequests());
		}
		this.ingredientsDirty = true;
		this.logicCacheID = null;
		this.checkTasks();
	}
	
	@Override
	public int getField(int id) {
		if (id == 0) {
			return (int) (this.getProgress() * 100);
		} else if (id == 1) {
			return this.criteriaMode.ordinal();
		} else if (id == 2) {
			return this.criteriaOp.ordinal();
		} else if (id <= TEMPLATE_SLOTS + 2) {
			return recipeIssuesCache[id-3] ? 1 : 0;
		}
		
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		if (id == 0) {
			this.buildPoints = (((float) value / 100f) * 100f); // last 100f is how many build points per build
		} else if (id == 1) {
			this.criteriaMode = CraftingCriteriaMode.values()[value % CraftingCriteriaMode.values().length];
		} else if (id == 2) {
			this.criteriaOp = CraftingLogicOp.values()[value % CraftingLogicOp.values().length];
		} else if (id < TEMPLATE_SLOTS) {
			this.recipeIssuesCache[id - 3] = (value != 0);
		}
	}

	@Override
	public int getFieldCount() {
		return TEMPLATE_SLOTS + 3; // 0 is progress, 1 is mode, 2 is op, and  3-N+2 are slot invalid flags (true = error)
	}
	
	protected LogisticsTaskWorkBlock createTask() {
		LogisticsTaskWorkBlock task = new LogisticsTaskWorkBlock(this.getNetworkComponent(), "Craft Task", worldObj, pos.toImmutable()) {
			@Override
			protected void workBlock() {
				if (this.getCurrentWorker() == null) {
					return;
				}
				
				final float amt;
				IItemCarrierFey worker = this.getCurrentWorker();
				if (worker instanceof EntityDwarf) {
					amt = 18f; // Slow swings
				} else if (worker instanceof EntityGnome) {
					amt = 10f;
				} else {
					amt = 12f;
				}
				addProgress(amt);
			}
		};
		this.tasks.add(task);
		getNetwork().getTaskRegistry().register(task, this);
		return task;
	}
	
	protected void cleanTask(LogisticsTaskWorkBlock task) {
		getNetwork().getTaskRegistry().revoke(task);
	}
	
	protected void checkTasks() {
		if (this.validateRecipe() && this.validateIngredients() && this.getNetwork() != null && this.worldObj != null && checkConditions()) {
			while (this.tasks.size() < 1) { // TODO make max configurable? Like with upgrades?
				createTask();
			}
		} else {
			for (LogisticsTaskWorkBlock task : this.tasks) {
				cleanTask(task);
			}
			tasks.clear();
		}
	}
	
	protected void consumeIngredients() {
		for (int i = 0; i < TEMPLATE_SLOTS; i++) {
			this.decrStackSize(i, 1);
		}
	}
	
	protected void produceCraft() {
		// Notify tasks that they are done
		// Note: Done before modifying inv to avoid accidentally making it seem like things should
		// be cleared out
		for (LogisticsTaskWorkBlock task : this.tasks) {
			task.markComplete();
		}
		
		this.tasks.clear();
		
		// Have to janki-fy the recipe manager, since the inventories it takes as input require a container to work
		InventoryCrafting inv = new InventoryCrafting(new Container() {

			@Override
			public boolean canInteractWith(EntityPlayer playerIn) {
				return true;
			}
			
			@Override
			public void detectAndSendChanges() {
				;
			}
		}, getCraftGridDim(), getCraftGridDim());
		
		for (int i = 0; i < TEMPLATE_SLOTS; i++) {
			if (this.templates[i] != null) {
				inv.setInventorySlotContents(i, templates[i]);
			}
		}
		
		ItemStack output = this.recipeCache.getCraftingResult(inv).copy();
		if (this.getOutputStack() == null) {
			this.setInventorySlotContents(TEMPLATE_SLOTS, output);
		} else {
			ItemStack stack = this.getStackInSlot(TEMPLATE_SLOTS);
			stack.stackSize = Math.min(stack.stackSize + output.stackSize, stack.getMaxStackSize());
		}
	}
	
	protected float getBuildPointsFor(IRecipe recipe) {
		return 100f;
	}
	
	/**
	 * Contributes some amount towards the current build.
	 * Build points are usually out of 100 for a full craft.
	 * @param buildPoints
	 */
	public void addProgress(float buildPoints) {
		if (this.validateRecipe() && this.validateIngredients()) {
			this.buildPoints += buildPoints;
			float cost = getBuildPointsFor(this.recipeCache);
			if (this.buildPoints >= cost) {
				this.buildPoints = 0;
				this.produceCraft();
				this.consumeIngredients();
			}
		}
	}

	private boolean canOutput() {
		ItemStack outputStack = this.getOutputStack();
		if (outputStack == null) {
			return true;
		}
		
		ItemStack output = this.recipeCache.getRecipeOutput();
		if (!ItemStacks.stacksMatch(output, outputStack)) {
			return false;
		}
		
		if (output.stackSize + outputStack.stackSize > output.stackSize) {
			return false;
		}
		
		return true;
	}
	
	protected void runLogic() {
		ItemStack req = getCriteriaTemplate();
		if (!placed || this.getNetwork() == null || req == null) {
			this.logicCacheID = null;
			this.logicValidCache = false;
			return;
		}
		
		if (this.logicCacheID == null || !logicCacheID.equals(getNetwork().getCacheKey())) {
			logicCacheID = getNetwork().getCacheKey();
			
			List<ItemDeepStack> networkItems = getNetwork().getAllCondensedNetworkItems();
			long available = 0;
			for (ItemDeepStack stack : networkItems) {
				if (stack.canMerge(req)) {
					available = stack.getCount();
					break;
				}
			}
			
			switch(getCriteriaOp()) {
				case EQUAL:
				default:
					logicValidCache = (available == this.criteriaCount);
					break;
				case LESS:
					logicValidCache = (available < this.criteriaCount);
					break;
				case MORE:
					logicValidCache = (available > this.criteriaCount);
					break;
			}
				
		}
	}
	
	protected boolean checkConditions() {
		if (worldObj == null || !placed) {
			return false;
		}
		
		CraftingCriteriaMode mode = this.getCriteriaMode();
		final boolean clear;
		switch (mode) {
		case ALWAYS:
		default:
			clear = true;
			break;
		case REDSTONE_HIGH:
			clear = worldObj.isBlockPowered(pos);
			break;
		case REDSTONE_LOW:
			clear = !worldObj.isBlockPowered(pos);
			break;
		case LOGIC:
			runLogic();
			clear = logicValidCache;
			break;
		}
		
		return clear;
	}
	
	@Override
	public void onTaskDrop(ILogisticsTask task, IFeyWorker worker) {
		
	}

	@Override
	public void onTaskAccept(ILogisticsTask task, IFeyWorker worker) {
		
	}
	
	@Override
	public void onTaskComplete(ILogisticsTask task, IFeyWorker worker) {
		
	}
	
	public void notifyNeighborChanged() {
		this.logicCacheID = null;
		this.checkTasks();
	}
	
	@Override
	public void validate() {
		super.validate();
	}
	
	@Override
	public void update() {
		if (!placed) {
			placed = true;
			if (!worldObj.isRemote) {
				this.logicCacheID = null;
				this.runLogic();
			}
		}
		
		if (worldObj.getTotalWorldTime() % 5 == 0) {
			if (!worldObj.isRemote) {
				this.checkTasks();
			}
		}
	}
}