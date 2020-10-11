package com.smanzana.nostrumfairies.capabilities.fey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.blocks.TemplateBlock;
import com.smanzana.nostrumfairies.client.gui.container.FairyScreenGui;
import com.smanzana.nostrumfairies.entity.IEntityListener;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy.FairyJob;
import com.smanzana.nostrumfairies.entity.fey.EntityPersonalFairy.IBuildPump;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory.FairyCastTarget;
import com.smanzana.nostrumfairies.inventory.FairyHolderInventory.FairyPlacementTarget;
import com.smanzana.nostrumfairies.items.FairyGael;
import com.smanzana.nostrumfairies.items.FairyGael.FairyGaelType;
import com.smanzana.nostrumfairies.logistics.ILogisticsComponent;
import com.smanzana.nostrumfairies.logistics.LogisticsNetwork;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemDepositRequester;
import com.smanzana.nostrumfairies.logistics.requesters.LogisticsItemWithdrawRequester;
import com.smanzana.nostrumfairies.logistics.task.ILogisticsTask;
import com.smanzana.nostrumfairies.logistics.task.LogisticsTaskDepositItem;
import com.smanzana.nostrumfairies.utils.ItemDeepStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Default implementation of the INostrumFeyCapability interface
 * @author Skyler
 *
 */
public class NostrumFeyCapability implements INostrumFeyCapability {

	private static final String NBT_UNLOCKED = "unlocked";
	private static final String NBT_FAIRY_SLOTS = "fairy_slots";
	private static final String NBT_FAIRY_INVENTORY = "fairy_inventory";
	private static final String NBT_TEMPLATE_SELECTION = "template_selection";
	
	private static final int BUILD_SCAN_RADIUS = 16;
	
	// Capability data
	private boolean isUnlocked;
	private int fairySlots;
	private FairyHolderInventory fairyInventory;
	private MutablePair<BlockPos, BlockPos> templateSelection;
	
	// Operational transient data
	private EntityLivingBase owner;
	private Map<FairyGaelType, List<FairyRecord>> deployedFairies;
	private int disabledTicks;
	private int ticksExisted;
	
	// Transient tick network data
	private List<LogisticsNetwork> tickNetworks; // Networks we're in this tick
	private LogisticsNetwork tickNetworkChoice; // Network that was ultimately picked to talk with
	private List<ItemStack> pullItems;
	private List<ItemStack> pushItems; // null if pull items were found
	
	// Task data (transient, too)
	private BuildTaskPlanner buildPlanner;
	private LogisticsItemDepositRequester depositRequester;
	private LogisticsItemWithdrawRequester withdrawRequester;
	
	public NostrumFeyCapability() {
		this.fairyInventory = new FairyHolderInventory();
		this.templateSelection = new MutablePair<>();
		this.deployedFairies = new EnumMap<>(FairyGaelType.class);
		for (FairyGaelType type : FairyGaelType.values()) {
			deployedFairies.put(type, new LinkedList<>());
		}
		this.tickNetworks = new ArrayList<>(4);
		buildPlanner = new BuildTaskPlanner();
		
		// defaults...
		this.fairySlots = 1;
		this.isUnlocked = false;
	}
	
	@Override
	public boolean isUnlocked() {
		return isUnlocked;
	}

	@Override
	public void unlock() {
		isUnlocked = true;
	}

	@Override
	public int getFairySlots() {
		return fairySlots;
	}

	@Override
	public void addFairySlot() {
		fairySlots++;
	}

	@Override
	public void setFairySlots(int slots) {
		fairySlots = slots;
	}

	@Override
	public FairyHolderInventory getFairyInventory() {
		return fairyInventory;
	}

	@Override
	public void setFairyInventory(FairyHolderInventory inventory) {
		if (inventory != this.fairyInventory) {
			this.retractFairies();
			this.fairyInventory = inventory;
		}
	}

	@Override
	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setBoolean(NBT_UNLOCKED, isUnlocked);
		nbt.setInteger(NBT_FAIRY_SLOTS, fairySlots);
		writeFairies();
		nbt.setTag(NBT_FAIRY_INVENTORY, fairyInventory.toNBT());
		if (templateSelection.left != null) {
			nbt.setLong(NBT_TEMPLATE_SELECTION + "_1", templateSelection.left.toLong());
			if (templateSelection.right != null) {
				nbt.setLong(NBT_TEMPLATE_SELECTION + "_2", templateSelection.right.toLong());
			}
		}
		
		return nbt;
	}

	@Override
	public void readNBT(NBTTagCompound nbt) {
		clearFairies();
		this.isUnlocked = nbt.getBoolean(NBT_UNLOCKED);
		this.fairySlots = nbt.getInteger(NBT_FAIRY_SLOTS);
		this.fairyInventory.readNBT(nbt.getCompoundTag(NBT_FAIRY_INVENTORY));
		
		if (nbt.hasKey(NBT_TEMPLATE_SELECTION + "_1")) {
			templateSelection.left = BlockPos.fromLong(nbt.getLong(NBT_TEMPLATE_SELECTION + "_1"));
			if (nbt.hasKey(NBT_TEMPLATE_SELECTION + "_2")) {
				templateSelection.right = BlockPos.fromLong(nbt.getLong(NBT_TEMPLATE_SELECTION + "_2"));
			} else {
				templateSelection.right = null;
			}
		} else {
			templateSelection.left = templateSelection.right = null;
		}
	}

	@Override
	public void provideEntity(EntityLivingBase owner) {
		if (owner != this.owner) {
			if (this.owner == null) {
				MinecraftForge.EVENT_BUS.register(this);
			} else {
				this.depositRequester.clearRequests();
				this.withdrawRequester.clearRequests();
			}
			this.owner = owner;
			if (owner == null) {
				MinecraftForge.EVENT_BUS.unregister(this);
			} else {
				this.depositRequester = new LogisticsItemDepositRequester(null, owner);
				this.withdrawRequester = new LogisticsItemWithdrawRequester(null, true, owner);
				
				if (owner instanceof EntityPlayer) {
					this.buildPlanner.setInventory(((EntityPlayer) owner).inventory);
					this.buildPlanner.setWorld(owner.worldObj);
				}
			}
		}
	}
	
	protected void scanForBuilds() {
		Set<BlockPos> builds = new HashSet<>();
		
		if (owner != null && !owner.isDead) {
			MutableBlockPos cursor = new MutableBlockPos();
			for (int x = -BUILD_SCAN_RADIUS; x <= BUILD_SCAN_RADIUS; x++)
			for (int z = -BUILD_SCAN_RADIUS; z <= BUILD_SCAN_RADIUS; z++)
			for (int y = -BUILD_SCAN_RADIUS; y <= BUILD_SCAN_RADIUS; y++) {
				cursor.setPos(owner.posX + x, owner.posY + y, owner.posZ + z);
				IBlockState state = owner.worldObj.getBlockState(cursor);
				if (state != null && state.getBlock() instanceof TemplateBlock) {
					builds.add(cursor.toImmutable());
				}
			}
		}
		
		List<EntityPersonalFairy> jobless = buildPlanner.resetTaskList(builds);
		if (jobless != null && !jobless.isEmpty()) {
			for (EntityPersonalFairy fairy : jobless) {
				fairy.cancelBuildTask();
			}
		}
	}
	
	@Override
	public void tick() {
		if (owner == null) {
			return;
		}
		
		if (owner.isDead) {
			retractFairies();
			MinecraftForge.EVENT_BUS.unregister(this);
			this.owner = null;
			return;
		}
		
		ticksExisted++;
		
		if (this.disabledTicks > 0) {
			this.disabledTicks--;
		}
		
		if (disabledTicks == 0 && owner instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) owner;
			if (player.openContainer instanceof FairyScreenGui.FairyScreenContainer) {
				this.retractFairies();
				disabledTicks = Math.max(2, disabledTicks);
			}
		}
		
		if (fairiesEnabled()) {
			if (ticksExisted % 40 == 0) {
				scanForBuilds();
			}
			
			if (ticksExisted % 5 == 0) {
				
				//tickNetworkChoice = null;
				//tickNetworks.clear();
				
				buildPlanner.cleanList();
				
				// Check and see if fairies should come out?
				for (FairyGaelType type : FairyGaelType.values()) {
					
					// Prereqs
					switch (type) {
					case ATTACK:
						if (!this.attackFairyUnlocked()) {
							continue;
						}
						break;
					case BUILD:
						if (!this.builderFairyUnlocked()) {
							continue;
						}
						
						// If player has build tasks
						if (!buildPlanner.hasUnclaimedTasks()) {
							continue;
						}
						break;
					case LOGISTICS:
						if (!this.logisticsFairyUnlocked()) {
							continue;
						}
						
						// If player has items to deposit or request, AND there are networks available
						if (owner instanceof EntityPlayer) {
							// If there's a configured anchor, use that network.
							if (fairyInventory.getLogisticsGem() != null) {
								tickNetworks.clear();
								BlockPos pos = PositionCrystal.getBlockPosition(fairyInventory.getLogisticsGem());
								int dim = PositionCrystal.getDimension(fairyInventory.getLogisticsGem());
								
								if (dim == owner.dimension && owner.getDistanceSq(pos) < 1024) {
									World world = NostrumFairies.getWorld(dim);
									LogisticsNetwork network = NostrumFairies.instance.getLogisticsRegistry().findNetwork(world, pos);
									if (network != null) {
										tickNetworks.add(network);
									}
								}
							} else {
								// Otherwise get all networks and use the closest
								NostrumFairies.instance.getLogisticsRegistry().getLogisticsNetworksFor(owner.worldObj, owner.getPosition().toImmutable(), tickNetworks);
							}
						}
						
						// We want to re-figure out network every couple of ticks to keep things updated.
						// However, push requests immediately make push requests look like they aren't needed as soon as the fairy's going.
						// So if there are any push requests and the old network is still connected, don't change
						LogisticsNetwork network = null;
						
						if (tickNetworkChoice != null && tickNetworks.contains(tickNetworkChoice)) { // values from last tick
							
							// See if there are any of our deposit requests going still
							for (ILogisticsTask task : tickNetworkChoice.getTaskRegistry().allTasks()) {
								if (task.getSourceEntity() == this.owner && task instanceof LogisticsTaskDepositItem) {
									if (!task.isComplete()
											&& tickNetworkChoice.getTaskRegistry().getCurrentWorker(task) != null) {
										network = tickNetworkChoice;
										break;
									}
								}
							}
						}
						
						if (!tickNetworks.isEmpty()) {
							pullItems = generatePullRequests();
							pushItems = generatePushRequests();
						} else {
							pullItems = null;
							pushItems = null;
						}
						
						if (network == null) {
							if (pullItems != null || pushItems != null) {
								// Find the network to use this tick
								network = findUsefulNetwork(tickNetworks, pullItems, pushItems, owner.worldObj, owner.getPosition());
							}
						}
						
						if (network != tickNetworkChoice) {
							// Different network than last time
							
							// If previous was empty, don't bother clearing requesters
							if (tickNetworkChoice != null) {
								this.depositRequester.clearRequests();
								this.withdrawRequester.clearRequests();
							}
							
							// In either case, update fairies
							for (FairyRecord record : deployedFairies.get(FairyGaelType.LOGISTICS)) {
								record.fairy.setNetwork(network);
							}
						}
						this.tickNetworkChoice = network;
						
						if (tickNetworkChoice == null) {
							continue;
						}
						
						// Things are going to work. Take a minute to update requesters
						this.depositRequester.setNetwork(tickNetworkChoice);
						this.withdrawRequester.setNetwork(tickNetworkChoice);
						
						withdrawRequester.updateRequestedItems(pullItems);
						depositRequester.updateRequestedItems(pushItems);
						
						break;
					}
					
					// Add more fairies, if needed
					if (deployedFairies.get(type).size() >= fairySlots) {
						continue;
					}
					
					int i = -1;
					while (deployedFairies.get(type).size() < this.fairySlots && i < fairyInventory.getGaelSize()) {
						i++;
						
						ItemStack gael = this.fairyInventory.getGaelByType(type, i);
						if (gael == null) {
							continue;
						}
						
						if (FairyGael.isCracked(gael)) {
							continue;
						}
						
						float reqEnergy = .3f;
						if (type == FairyGaelType.ATTACK) {
							reqEnergy = .95f;
						}
						if (FairyGael.getStoredEnergy(gael) < reqEnergy) {
							continue;
						}
						
						boolean deployed = false;
						for (FairyRecord record : deployedFairies.get(type)) {
							if (record.index == i) {
								deployed = true;
								break;
							}
						}
						if (deployed) {
							continue;
						}
						
						deployFairy(i, type, gael, owner.worldObj, owner.posX, owner.posY, owner.posZ);
					}
				}
			}
			
			Map<FairyGaelType, boolean[]> deployedMap = new EnumMap<>(FairyGaelType.class);
			
			// Check up on all deployed fairies
			List<EntityPersonalFairy> tiredFairies = new LinkedList<>();
			for (FairyGaelType type : FairyGaelType.values()) {
				deployedMap.put(type, new boolean[fairyInventory.getGaelSize()]);
				for (FairyRecord record : deployedFairies.get(type)) {
					if (record.fairy.getEnergy() <= 5f || (type != FairyGaelType.ATTACK && record.fairy.getIdleTicks() > (20 * 5))) {
						tiredFairies.add(record.fairy);
					} else {
						deployedMap.get(type)[record.index] = true;
					}
				}
			}
			for (EntityPersonalFairy fairy : tiredFairies) {
				removeFairy(fairy);
			}
			
			// Recoup lost energies for stored fairies
			// TODO figure out deployed once per tick
			for (FairyGaelType type : FairyGaelType.values()) {
				boolean[] deployed = deployedMap.get(type);
				for (int i = 0; i < fairyInventory.getGaelSize(); i++) {
					ItemStack gael = fairyInventory.getGaelByType(type, i);
					if (gael == null || FairyGael.isCracked(gael) || deployed[i]) {
						continue;
					}
					
					FairyGael.regenFairy(gael, 1f);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityTick(LivingUpdateEvent event) {
		if (event.getEntityLiving() == this.owner && !owner.worldObj.isRemote) {
			this.tick();
		}
	}
	
	protected void clearFairies() {
		for (FairyGaelType type : FairyGaelType.values()) { 
			for (FairyRecord record : this.deployedFairies.get(type)) {
				record.fairy.worldObj.removeEntity(record.fairy);
			}
			this.deployedFairies.get(type).clear();
		}
	}
	
	@Override
	public void retractFairies() {
		writeFairies();
		clearFairies();
	}
	
	public void deployFairy(int index, FairyGaelType type, ItemStack gaelStack, World world, double x, double y, double z) {
		if (gaelStack != null && gaelStack.getItem() instanceof FairyGael) {
			EntityPersonalFairy fairy = FairyGael.spawnStoredEntity(gaelStack, world, x, y, z);
			if (fairy != null) {
				// TODO have FairyGael return a different type of fairy, like a PlayerFairy or whatever.
				// TODO have it set up the type so the fairy knows how to behave
				// TODO make those fairies not persist
				
				fairy.registerListener(new FairyListener());
				fairy.setOwner(owner);
				
				if (type == FairyGaelType.LOGISTICS) {
					fairy.setNetwork(tickNetworkChoice);
				} else if (type == FairyGaelType.ATTACK) {
					ItemStack scrollStack = this.fairyInventory.getScroll(index);
					Spell spell = null;
					if (scrollStack != null) {
						spell = SpellScroll.getSpell(scrollStack);
					}
					
					FairyCastTarget castTarget = fairyInventory.getFairyCastTarget(index);
					FairyPlacementTarget placementTarget = fairyInventory.getFairyPlacementTarget(index);
					
					fairy.setFairyTargets(spell, castTarget, placementTarget);
				} else if (type == FairyGaelType.BUILD) {
					fairy.setBuildPump(new IBuildPump() {
						@Override
						public @Nullable BlockPos claimTask(EntityPersonalFairy fairy) {
							return buildPlanner.claimTask(fairy);
						}
						
						@Override
						public void finishTask(EntityPersonalFairy fairy, BlockPos pos) {
							buildPlanner.finishTask(fairy, pos);
						}

						@Override
						public void abandonTask(EntityPersonalFairy entityPersonalFairy, BlockPos pos) {
							buildPlanner.removeTask(pos);
						}
					});
				}
				
				FairyRecord record = new FairyRecord(fairy, index);
				this.deployedFairies.get(type).add(record);
			}
		}
	}
	
	protected void writeFairies() {
		for (FairyGaelType type : FairyGaelType.values()) {
			for (FairyRecord record : this.deployedFairies.get(type)) {
				ItemStack gael = this.fairyInventory.getGaelByType(type, record.index);
				if (gael == null) {
					NostrumFairies.logger.warn("Tried to save fairy, but housing slot was gael-less!");
					continue; // Fairy lost
				}
				FairyGael.setStoredEntity(gael, record.fairy);
				fairyInventory.setGaelByType(type, record.index, gael);
			}
		}
	}
	
	protected static FairyGaelType getGaelType(FairyJob job) {
		switch (job) {
		case BUILDER:
			return FairyGaelType.BUILD;
		case LOGISTICS:
			return FairyGaelType.LOGISTICS;
		case WARRIOR:
		default:
			return FairyGaelType.ATTACK;
		}
	}
	
	private boolean removeFairy(EntityPersonalFairy fairy) {
		FairyGaelType type = getGaelType(fairy.getJob());
		Iterator<FairyRecord> it = deployedFairies.get(type).iterator();
		while (it.hasNext()) {
			FairyRecord record = it.next();
			if (record.fairy == fairy) {
				it.remove();
				ItemStack gael = fairyInventory.getGaelByType(type, record.index);
				if (gael != null) {
					if (fairy.getHealth() <= 0 || fairy.isDead) {
						// Grab a snapshot of the fairy just before it died
						float health = fairy.getHealth();
						boolean dead = fairy.isDead;
						fairy.setHealth(1f);
						fairy.isDead = false;
						FairyGael.setStoredEntity(gael, fairy);
						FairyGael.crack(gael);
						fairyInventory.markDirty();
						fairy.setHealth(health);
						fairy.isDead = dead;
					} else {
						FairyGael.setStoredEntity(gael, fairy);
						fairyInventory.markDirty();
						fairy.setDead();
						fairy.worldObj.removeEntity(fairy);
					}
				}
				return true;
			}
		}
		
		return false;
	}

	private final class FairyListener implements IEntityListener<EntityPersonalFairy> {
		@Override
		public void onDeath(EntityPersonalFairy entity) {
			if (removeFairy(entity)) {
				; // fairy inventory already updated
			}
		}

		@Override
		public void onDamage(EntityPersonalFairy entity, DamageSource source, double damage) {
			;
		}

		@Override
		public void onHeal(EntityPersonalFairy entity, double amount) {
			;
		}
	}
	
	private static final class FairyRecord {
		public final EntityPersonalFairy fairy;
		public final int index;
		
		public FairyRecord(EntityPersonalFairy fairy, int index) {
			this.fairy = fairy;
			this.index = index;
		}
	}

	@Override
	public void disableFairies(int ticks) {
		if (this.disabledTicks == 0) {
			this.retractFairies();
			this.disabledTicks = ticks;
		}
	}

	@Override
	public void enableFairies() {
		this.disabledTicks = 0;
	}
	
	@Override
	public boolean fairiesEnabled() {
		return this.disabledTicks == 0 && isUnlocked;
	}

	@Override
	public boolean attackFairyUnlocked() {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(owner);
		return attr != null && attr.getCompletedResearches().contains("fairy_gael_aggressive");
	}

	@Override
	public boolean builderFairyUnlocked() {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(owner);
		return attr != null && attr.getCompletedResearches().contains("fairy_gael_construction");
	}

	@Override
	public boolean logisticsFairyUnlocked() {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(owner);
		return attr != null && attr.getCompletedResearches().contains("fairy_gael_logistics");
	}
	
	@Override
	public void addBuildSpot(BlockPos buildSpot) {
		this.buildPlanner.add(buildSpot);
	}
	
	/**
	 * Look through a collection of networks to try and find the first that has any of the items we're looking for, if any.
	 * @param networks
	 * @return
	 */
	protected static @Nullable LogisticsNetwork findUsefulNetwork(Collection<LogisticsNetwork> networks,
			Collection<ItemStack> pulls, Collection<ItemStack> pushes,
			World world, BlockPos pos) {
		if (networks == null || networks.isEmpty()
				|| ((pulls == null || pulls.isEmpty())
				&& (pushes == null || pushes.isEmpty()))) {
			return null;
		}
		
		// Check pull requests first
		if (pulls != null && !pulls.isEmpty()) {
			for (LogisticsNetwork network : networks) {
				List<ItemDeepStack> items = network.getAllCondensedNetworkItems();
				for (ItemDeepStack avail : items) {
					for (ItemStack pull : pulls) {
						if (avail.canMerge(pull)) {
							return network;
						}
					}
				}
			}
		} else {
			for (LogisticsNetwork network : networks) {
				for (ItemStack push : pushes) {
					ILogisticsComponent comp = network.getStorageForItem(world, pos, push);
					if (comp != null) {
						return network;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Looks through logistics templates and returns a list (or null) of all items that we have room for and would like.
	 * @return
	 */
	protected @Nullable List<ItemStack> generatePullRequests() {
		if (!this.logisticsFairyUnlocked() || !(owner instanceof EntityPlayer)) { // player required for inventory
			return null;
		}
		
		InventoryPlayer playerInv = ((EntityPlayer) owner).inventory;
		
		List<ItemStack> templateList = new ArrayList<>(fairyInventory.getPullTemplateSize());
		for (int i = 0; i < fairyInventory.getPullTemplateSize(); i++) {
			ItemStack template = fairyInventory.getPullTemplate(i);
			if (template != null) {
				templateList.add(template.copy());
			}
		}
		
		// Create copy collection of items we already have
		List<ItemDeepStack> owned = ItemDeepStack.toDeepList(playerInv);
		List<ItemDeepStack> wishList = ItemDeepStack.toDeepList(templateList);
		
		// Remove items that we already have
		Iterator<ItemDeepStack> it = wishList.iterator();
		while (it.hasNext()) {
			ItemDeepStack request = it.next();
			Iterator<ItemDeepStack> ownedIt = owned.iterator();
			while (ownedIt.hasNext()) {
				ItemDeepStack existing = ownedIt.next();
				if (existing.canMerge(request)) {
					if (request.getCount() <= existing.getCount()) {
						// Found it, and nothing is required
						request.setCount(0);
					} else {
						request.add(-existing.getCount());
					}
					break;
				}
			}
			
			if (request.getCount() <= 0) {
				it.remove();
			} // else remaining items desired
		}
		
		// Dissolve back into itemstacks
		templateList.clear();
		for (ItemDeepStack req : wishList) {
			while (req.getCount() > 0) {
				templateList.add(req.splitStack(Math.min(64, req.getTemplate().getMaxStackSize())));
			}
		}
		
		// Check for what we have room for
		Iterator<ItemStack> stackIt = templateList.iterator();
		boolean canFit =  false;
		while (stackIt.hasNext()) {
			ItemStack request = stackIt.next();
			if (Inventories.canFit(playerInv, request)) {
				canFit = true;
				break;
			}
		}
		
		if (templateList.isEmpty() || !canFit) {
			templateList = null;
		}
		
		return templateList;
	}
	
	/**
	 * Looks through logistics templates and returns a list (or null) of all items that we have but don't want
	 * @return
	 */
	protected @Nullable List<ItemStack> generatePushRequests() {
		if (!this.logisticsFairyUnlocked() || !(owner instanceof EntityPlayer)) { // player required for inventory
			return null;
		}
		
		InventoryPlayer playerInv = ((EntityPlayer) owner).inventory;
		
		List<ItemStack> templateList = new ArrayList<>(fairyInventory.getPushTemplateSize());
		for (int i = 0; i < fairyInventory.getPushTemplateSize(); i++) {
			ItemStack template = fairyInventory.getPushTemplate(i);
			if (template != null) {
				templateList.add(template.copy());
			}
		}
		
		List<ItemDeepStack> allowed = ItemDeepStack.toDeepList(templateList);
		List<ItemDeepStack> owned = ItemDeepStack.toDeepList(playerInv);
		List<ItemStack> pushList = new ArrayList<>(allowed.size());
		
		// Remove items that we have over the limit
		Iterator<ItemDeepStack> it = allowed.iterator();
		while (it.hasNext()) {
			ItemDeepStack cap = it.next();
			Iterator<ItemDeepStack> ownedIt = owned.iterator();
			while (ownedIt.hasNext()) {
				ItemDeepStack existing = ownedIt.next();
				if (existing.canMerge(cap)) {
					if (cap.getCount() > existing.getCount()) {
						; // nothing to do
					} else {
						existing.add(-cap.getCount());
						while (existing.getCount() > 0) {
							pushList.add(existing.splitStack(Math.min(64, existing.getTemplate().getMaxStackSize())));
						}
					}
					break;
				}
			}
		}
		
		if (pushList.isEmpty()) {
			pushList = null;
		}
		
		return pushList;
	}

	@Override
	public Pair<BlockPos, BlockPos> getTemplateSelection() {
		return this.templateSelection;
	}

	@Override
	public void clearTemplateSelection() {
		templateSelection.left = null;
		templateSelection.right = null;
	}

	@Override
	public void addTemplateSelection(BlockPos pos) {
		if (templateSelection.left == null) {
			templateSelection.left = pos;
		} else {
			templateSelection.right = pos;
		}
	}
}
