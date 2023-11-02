package com.smanzana.nostrumfairies.tiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.entity.fey.EntityDwarf;
import com.smanzana.nostrumfairies.entity.fey.EntityGnome;
import com.smanzana.nostrumfairies.entity.fey.IFeyWorker;
import com.smanzana.nostrumfairies.entity.fey.IItemCarrierFey;
import com.smanzana.nostrumfairies.inventory.FeySlotType;
import com.smanzana.nostrumfairies.items.FeyStone;
import com.smanzana.nostrumfairies.items.FeyStoneMaterial;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemDepositRequester;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTaskListener;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskWorkBlock;
import com.smanzana.nostrumfairies.tiles.LogisticsLogicComponent.ILogicListener;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.utils.ItemStacks;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class CraftingBlockTileEntity extends LogisticsChestTileEntity
											implements ILogisticsTaskListener, ITickable, ILogisticsLogicProvider, ILogicListener {

	private static final String NBT_TEMPLATES = "templates";
	private static final String NBT_BUILD_POINTS = "build_points";
	private static final String NBT_LOGIC_COMP = "logic";
	
	private static final String NBT_TEMPLATE_INDEX = "index";
	private static final String NBT_TEMPLATE_ITEM = "item";
	
	private String displayName;
	private NonNullList<ItemStack> templates;
	private float buildPoints; // out of 100
	private final LogisticsLogicComponent logicComp;
	private LogisticsItemWithdrawRequester withdrawRequester;
	private LogisticsItemDepositRequester depositRequester;
	
	private final int TEMPLATE_SLOTS;
	
	// Transient validation vars
	private boolean recipeDirty;
	private boolean recipeValidCache;
	private @Nullable IRecipe recipeCache;
	private boolean[] recipeIssuesCache;
	private boolean[] recipeBonusesCache; // like issue cache but positive.
	private float recipeBonusCache; // Total of all bonuses
	private boolean ingredientsDirty;
	private boolean ingredientsValidCache;
	
	// Task data (transient)
	private List<LogisticsTaskWorkBlock> tasks;
	
	private boolean placed = false;
	
	public CraftingBlockTileEntity() {
		super();
		displayName = "Crafting Block";
		this.TEMPLATE_SLOTS = getCraftGridDim() * getCraftGridDim();
		templates = NonNullList.withSize(TEMPLATE_SLOTS, ItemStack.EMPTY);
		recipeDirty = true;
		ingredientsDirty = true;
		recipeIssuesCache = new boolean[TEMPLATE_SLOTS];
		recipeBonusesCache = new boolean[TEMPLATE_SLOTS];
		logicComp = new LogisticsLogicComponent(false, this);
		this.tasks = new ArrayList<>();
		

		// Parent creates an inventory array of our getSizeInventory.
		// Note that the first TEMPLATE_SLOTS are the templates. The one after that
		// is the output
	}
	
	public abstract int getCraftGridDim();
	
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
	public String getName() {
		return displayName;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}
	
	@Override
	public int getSizeInventory() {
		return getCraftGridDim() * getCraftGridDim() + 1 + 1 + 1; // creates an actual inventory in super class
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
	
	public void setTemplate(int index, @Nonnull ItemStack template) {
		if (index < 0 || index >=  TEMPLATE_SLOTS) {
			return;
		}
		
		ItemStack temp = template.isEmpty() ? ItemStack.EMPTY : template.copy();
		templates.set(index, temp);
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
	
	public @Nonnull ItemStack getTemplate(int index) {
		if (index < 0 || index >=  TEMPLATE_SLOTS) {
			return ItemStack.EMPTY;
		}
		
		return templates.get(index);
	}
	
	public @Nonnull ItemStack getOutputStack() {
		return this.getStackInSlot(TEMPLATE_SLOTS);
	}
	
	public @Nonnull ItemStack getUpgrade() {
		return this.getStackInSlot(TEMPLATE_SLOTS + 2);
	}
	
	public @Nonnull ItemStack getCriteriaTemplate() {
		return this.getStackInSlot(TEMPLATE_SLOTS + 1);
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
	
	protected abstract float getCraftBonus(ItemStack item);
	
	protected IRecipe findRecipe() {
		// Have to janki-fy the recipe manager, since the inventories it takes as input require a container to work
		InventoryCrafting inv = new InventoryCrafting(new Container() {

			@Override
			public boolean canInteractWith(PlayerEntity playerIn) {
				return true;
			}
			
			@Override
			public void detectAndSendChanges() {
				;
			}
		}, getCraftGridDim(), getCraftGridDim());
		
		for (int i = 0; i < TEMPLATE_SLOTS; i++) {
			if (!templates.get(i).isEmpty()) {
				inv.setInventorySlotContents(i, templates.get(i));
			}
		}
		
		IRecipe match = CraftingManager.findMatchingRecipe(inv, world);
		if (match != null && canCraft(match)) {
			return match;
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
			recipeBonusCache = 0f;
			for (int i = 0; i < TEMPLATE_SLOTS; i++) {
				ItemStack template = templates.get(i);
				if (template.isEmpty()) {
					recipeIssuesCache[i] = false; // no issue
					recipeBonusesCache[i] = false; // no bonus
				} else if (!this.canCraftWith(template)) {
					recipeIssuesCache[i] = true; // problem here
					recipeBonusesCache[i] = false; // no bonus
					recipeValidCache = false; // overall recipe's bad too
				} else {
					float bonus = this.getCraftBonus(template);
					recipeIssuesCache[i] = false; // no issue
					recipeBonusCache += bonus;
					recipeBonusesCache[i] = (bonus > 0); // no bonus
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
	
	public float getCraftBonus() {
		if (!validateRecipe()) {
			return 0f;
		}
		
		return this.recipeBonusCache;
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
				ItemStack template = templates.get(i);
				if (template.isEmpty()) {
					continue;
				}
				
				ItemStack inSlot = this.getStackInSlot(i);
				if (inSlot.isEmpty()) {
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
		
		if (index == TEMPLATE_SLOTS) {
			// output slot -- can't put anything in it, but any item can be put via code
			return true;
		} else if (index == TEMPLATE_SLOTS + 1) {
			return true; // Anything can go in criteria filter
		} else if (index == TEMPLATE_SLOTS + 2) {
			if (stack.isEmpty()) {
				return true;
			}
			if (!(stack.getItem() instanceof FeyStone)) {
				return false;
			}
			
			FeyStone stone = (FeyStone) stack.getItem();
			FeySlotType slot = stone.getFeySlot(stack);
			return (slot == FeySlotType.UPGRADE || slot == FeySlotType.DOWNGRADE);
		}
		
		ItemStack template = getTemplate(index);
		if (!template.isEmpty()) {
			return ItemStacks.stacksMatch(template, stack);
		}
		
		// No template means we want nothing in that slot
		return false;
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		// Save templates
		NBTTagList templates = new NBTTagList();
		for (int i = 0; i < TEMPLATE_SLOTS; i++) {
			ItemStack stack = this.getTemplate(i);
			if (stack.isEmpty()) {
				continue;
			}
			
			CompoundNBT template = new CompoundNBT();
			
			template.putInt(NBT_TEMPLATE_INDEX, i);
			template.setTag(NBT_TEMPLATE_ITEM, stack.writeToNBT(new CompoundNBT()));
			
			templates.appendTag(template);
		}
		nbt.setTag(NBT_TEMPLATES, templates);
		
		nbt.setTag(NBT_LOGIC_COMP, logicComp.writeToNBT(new CompoundNBT()));
		
		nbt.setFloat(NBT_BUILD_POINTS, buildPoints);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbt) {
		templates = NonNullList.withSize(TEMPLATE_SLOTS, ItemStack.EMPTY);
		
		// Reload templates
		NBTTagList list = nbt.getTagList(NBT_TEMPLATES, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			CompoundNBT template = list.getCompoundTagAt(i);
			int index = template.getInt(NBT_TEMPLATE_INDEX);
			
			if (index < 0 || index > TEMPLATE_SLOTS) {
				NostrumFairies.logger.error("Found serialized template with invalid index! " + index + " outside of " + TEMPLATE_SLOTS);
				continue;
			}
			
			ItemStack stack = new ItemStack(template.getCompoundTag(NBT_TEMPLATE_ITEM));
			
			templates.set(index, stack);
		}
		
		CompoundNBT tag = nbt.getCompoundTag(NBT_LOGIC_COMP);
		if (tag != null) {
			this.logicComp.readFromNBT(tag);
		}
		
		this.buildPoints = nbt.getFloat(NBT_BUILD_POINTS);
		
		// Do super afterwards so taht we have templates already
		super.readFromNBT(nbt);
		
		this.recipeDirty = true;
		this.ingredientsDirty = true;
	}
	
	@Override
	protected void setNetworkComponent(LogisticsTileEntityComponent component) {
		super.setNetworkComponent(component);
		logicComp.setNetwork(component.getNetwork());
		
		if (world != null && !world.isRemote && withdrawRequester == null) {
			withdrawRequester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent);
			withdrawRequester.updateRequestedItems(getItemRequests());
			
			depositRequester = new LogisticsItemDepositRequester(this.networkComponent.getNetwork(), this.networkComponent);
			depositRequester.updateRequestedItems(getPushRequests());
		}
	}
	
	@Override
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		logicComp.setLocation(worldIn, pos);
		
		if (this.networkComponent != null && !worldIn.isRemote) {
			if (withdrawRequester == null) {
				withdrawRequester = new LogisticsItemWithdrawRequester(this.networkComponent.getNetwork(), true, this.networkComponent);
				withdrawRequester.updateRequestedItems(getItemRequests());
				
				depositRequester = new LogisticsItemDepositRequester(this.networkComponent.getNetwork(), this.networkComponent);
				depositRequester.updateRequestedItems(getPushRequests());
			}
			
			//checkTasks();
		}
	}
	
	@Override
	public void onLeaveNetwork() {
		if (!world.isRemote && withdrawRequester != null) {
			withdrawRequester.clearRequests();
			withdrawRequester.setNetwork(null);
			depositRequester.clearRequests();
			depositRequester.setNetwork(null);
		}
		
		super.onLeaveNetwork();
		logicComp.setNetwork(null);
	}
	
	@Override
	public void onJoinNetwork(LogisticsNetwork network) {
		if (!world.isRemote && withdrawRequester != null) {
			withdrawRequester.setNetwork(network);
			withdrawRequester.updateRequestedItems(getItemRequests());
			depositRequester.setNetwork(network);
			depositRequester.updateRequestedItems(getPushRequests());
		}
		
		checkTasks();
		super.onJoinNetwork(network);
		logicComp.setNetwork(network);
	}
	
	private NonNullList<ItemStack> getItemRequests() {
		NonNullList<ItemStack> requests = NonNullList.create();
		
		for (int i = 0; i < templates.size(); i++) {
			if (templates.get(i).isEmpty()) {
				continue;
			}
			
			ItemStack inSlot = this.getStackInSlot(i);
			int desire = templates.get(i).getCount() - (inSlot.isEmpty() ? 0 : inSlot.getCount());
			if (desire > 0) {
				ItemStack req = templates.get(i).copy();
				req.setCount(desire);
				requests.add(req);
			}
		}
		
		return requests;
	}
	
	private NonNullList<ItemStack> getPushRequests() {
		return NonNullList.from(ItemStack.EMPTY, getOutputStack());
	}
	
	@Override
	public void takeItem(ItemStack stack) {
		super.takeItem(stack);
	}
	
	@Override
	public void addItem(ItemStack stack) {
		// Only allow popping items in over templates
		boolean anyChanges = false;
		for (int i = 0; i < templates.size(); i++) {
			if (templates.get(i).isEmpty()) {
				continue;
			}
			
			if (!isItemValidForSlot(i, stack)) {
				// doesn't fit here anyways
				continue;
			}
			
			// if template count != stack count, try to add there
			ItemStack inSlot = this.getStackInSlot(i);
			int desire = templates.get(i).getCount() - (inSlot.isEmpty() ? 0 : inSlot.getCount());
			int amt = Math.min(stack.getCount(), desire);
			if (inSlot.isEmpty()) {
				// take out template desire amount
				this.setInventorySlotContentsDirty(i, stack.splitStack(amt)); // doesn't set dirty
				anyChanges = true;
			} else {
				stack.shrink(amt);
				inSlot.grow(amt);
				anyChanges = true;
			}
			
			if (stack.isEmpty()) {
				break;
			}
		}
		
		if (anyChanges) {
			this.markDirty();
		}
		
		// Any leftover?
		if (!stack.isEmpty()) {
			EntityItem ent = new EntityItem(world, pos.getX() + .5, pos.getY() + 1.2, pos.getZ() + .5, stack);
			world.spawnEntity(ent);
		}
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		if (world != null && !world.isRemote && withdrawRequester != null) {
			withdrawRequester.updateRequestedItems(getItemRequests());
			depositRequester.updateRequestedItems(getPushRequests());
		}
		this.ingredientsDirty = true;
		this.recipeDirty = true;
		this.checkTasks();
	}
	
	@Override
	public int getField(int id) {
		if (id == 0) {
			return (int) (this.getProgress() * 100);
		} else if (id == 1) {
			return 0;// this.criteriaMode.ordinal();
		} else if (id == 2) {
			return 0;//this.criteriaOp.ordinal();
		} else if (id <= TEMPLATE_SLOTS + 2) {
			// if issue, return -1. If bonus, return 1. Else, 0.
			if (recipeIssuesCache[id-3]) {
				return -1;
			} else if (recipeBonusesCache[id-3]) {
				return 1;
			}
			return 0;
		}
		
		return 0;
	}

	@Override
	public void setField(int id, int value) {
		if (id == 0) {
			this.buildPoints = (((float) value / 100f) * 100f); // last 100f is how many build points per build
		} else if (id == 1) {
			//this.criteriaMode = CraftingCriteriaMode.values()[value % CraftingCriteriaMode.values().length];
		} else if (id == 2) {
			//this.criteriaOp = CraftingLogicOp.values()[value % CraftingLogicOp.values().length];
		} else if (id < TEMPLATE_SLOTS) {
			if (value == -1) {
				this.recipeIssuesCache[id - 3] = true;
				this.recipeBonusesCache[id - 3] = false;
			} else if (value == 1) {
				this.recipeIssuesCache[id - 3] = false;
				this.recipeBonusesCache[id - 3] = true;
			} else {
				this.recipeIssuesCache[id - 3] = false;
				this.recipeBonusesCache[id - 3] = false;
			}
		}
	}

	@Override
	public int getFieldCount() {
		return TEMPLATE_SLOTS + 3; // 0 is progress, 1 is mode, 2 is op, and  3-N+2 are slot invalid flags (true = error)
	}
	
	protected float getCraftSpeed(float bonus) {
		float mult = 1f;
		if (!this.getUpgrade().isEmpty()) {
			if (FeyStone.instance().getStoneMaterial(this.getUpgrade()) == FeyStoneMaterial.RUBY) {
				FeySlotType type = FeyStone.instance().getFeySlot(this.getUpgrade()); 
				if (type == FeySlotType.DOWNGRADE) {
					mult = .3f;
				} else if (type == FeySlotType.UPGRADE) {
					mult = 1.5f;
				}
			}
		}
		return (bonus + 1f) * mult;
	}
	
	protected LogisticsTaskWorkBlock createTask() {
		LogisticsTaskWorkBlock task = new LogisticsTaskWorkBlock(this.getNetworkComponent(), "Craft Task", world, pos.toImmutable()) {
			@Override
			protected void workBlock() {
				if (this.getCurrentWorker() == null) {
					return;
				}
				
				final float amt;
				IItemCarrierFey worker = this.getCurrentWorker();
				if (worker instanceof EntityDwarf) {
					amt = 8f; // Fast swings
				} else if (worker instanceof EntityGnome) {
					amt = 35f; // very slow swings
				} else {
					amt = 14f;
				}
				addProgress(amt * getCraftSpeed(recipeBonusCache));
			}
		};
		this.tasks.add(task);
		getNetwork().getTaskRegistry().register(task, this);
		return task;
	}
	
	protected void cleanTask(LogisticsTaskWorkBlock task) {
		getNetwork().getTaskRegistry().revoke(task);
	}
	
	protected int getMaxWorkJobs() {
		return 1;
	}
	
	protected void checkTasks() {
		if (this.validateRecipe() && this.validateIngredients() && this.getNetwork() != null && this.world != null && logicComp.isActivated()) {
			while (this.tasks.size() < getMaxWorkJobs()) {
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
	
	protected ItemStack generateOutput() {
		// Have to janki-fy the recipe manager, since the inventories it takes as input require a container to work
		InventoryCrafting inv = new InventoryCrafting(new Container() {

			@Override
			public boolean canInteractWith(PlayerEntity playerIn) {
				return true;
			}
			
			@Override
			public void detectAndSendChanges() {
				;
			}
		}, getCraftGridDim(), getCraftGridDim());
		
		for (int i = 0; i < TEMPLATE_SLOTS; i++) {
			if (!this.templates.get(i).isEmpty()) {
				inv.setInventorySlotContents(i, templates.get(i));
			}
		}
		
		return this.recipeCache.getCraftingResult(inv).copy();
	}
	
	protected void produceCraft() {
		// Notify tasks that they are done
		// Note: Done before modifying inv to avoid accidentally making it seem like things should
		// be cleared out
		for (LogisticsTaskWorkBlock task : this.tasks) {
			task.markComplete();
		}
		
		this.tasks.clear();
		
		ItemStack output = generateOutput();
		if (this.getOutputStack().isEmpty()) {
			this.setInventorySlotContents(TEMPLATE_SLOTS, output);
		} else {
			ItemStack stack = this.getStackInSlot(TEMPLATE_SLOTS);
			stack.setCount(Math.min(stack.getCount() + output.getCount(), stack.getMaxStackSize()));
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
		if (outputStack.isEmpty()) {
			return true;
		}
		
		ItemStack output = this.recipeCache.getRecipeOutput();
		if (!ItemStacks.stacksMatch(output, outputStack)) {
			return false;
		}
		
		if (output.getCount() + outputStack.getCount() > output.getCount()) {
			return false;
		}
		
		return true;
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
		logicComp.onWorldUpdate();
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
			if (!world.isRemote) {
				// TODO used to update logic
			}
		}
		
		if (world.getTotalWorldTime() % 5 == 0) {
			if (!world.isRemote) {
				this.checkTasks();
			}
		}
	}
}