package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.capabilities.templates.TemplateViewerCapability;
import com.smanzana.nostrumfairies.client.gui.NostrumFairyGui;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.TemplateWandUpdate;
import com.smanzana.nostrumfairies.network.messages.TemplateWandUpdate.WandUpdateType;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Selects areas in the world, writes them to templates, and reads templates to instruct workers to build things.
 * 
 * Three modes encoding in meta:
 *   1) selection mode, which just makes things modify the selection points
 *   2) capture mode. Just tries to write to blueprint
 *   3) spawn mode. Tries to spawn blueprint
 *   
 * While in spawn mode, selection can be cycled through like a spell tome.
 * Can carry a max of 10 templates, so we need a basic inventory gui.
 * @author Skyler
 *
 */
public class TemplateWand extends Item implements ILoreTagged {

	public static enum WandMode {
		SELECTION,
		CAPTURE,
		SPAWN
	}
	
	public static final String ID = "template_wand";
	private static final int MAX_TEMPLATES = 10;
	public static final int MAX_TEMPLATE_BLOCKS = 16 * 16 * 128;
	
	private static TemplateWand instance = null;
	public static TemplateWand instance() {
		if (instance == null)
			instance = new TemplateWand();
		
		return instance;
	}
	
	public TemplateWand() {
		super();
		this.setUnlocalizedName(ID);
		this.setRegistryName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		this.setCreativeTab(NostrumFairies.creativeTab);
	}
	
	protected static final WandMode modeFromMeta(int meta) {
		return WandMode.values()[meta % WandMode.values().length];
	}
	
	public static final int metaFromMode(WandMode mode) {
		return mode.ordinal();
	}
	
	public static final WandMode getModeOf(ItemStack stack) {
		return modeFromMeta(stack.getMetadata());
	}
	
	@Override
	public String getHighlightTip(ItemStack item, String displayName ) {
		return displayName;
	}
	
	public static void SetWandMode(ItemStack stack, WandMode mode) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		stack.setItemDamage(metaFromMode(mode));
	}
	
	private static final String NBT_TEMPLATE_INDEX = "template_index";
	private static final String NBT_TEMPLATE_INV = "templates";
	
	public static int GetTemplateIndex(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand) || !stack.hasTagCompound()) {
			return 0;
		}
		
		CompoundNBT nbt = stack.getTagCompound();
		return nbt.getInt(NBT_TEMPLATE_INDEX);
	}
	
	public static void SetTemplateIndex(ItemStack stack, int index) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		CompoundNBT nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.putInt(NBT_TEMPLATE_INDEX, index);
		stack.setTagCompound(nbt);
	}
	
	public static IInventory GetTemplateInventory(ItemStack stack) {
		InventoryBasic inv = new InventoryBasic("Template Wand Inventory", false, MAX_TEMPLATES) {
			
			@Override
			public boolean isItemValidForSlot(int index, ItemStack stack) {
				return index < this.getSizeInventory()
						&& (stack.isEmpty() || stack.getItem() instanceof TemplateScroll);
			}
			
			@Override
			public void markDirty() {
				super.markDirty();
				
				// Bleed changes into wand
				SetTemplateInventory(stack, this);
			}
			
		};
		
		if (!stack.isEmpty() && stack.getItem() instanceof TemplateWand && stack.hasTagCompound()) {
			NBTTagList list = stack.getTagCompound().getTagList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount() && i < MAX_TEMPLATES; i++) {
				CompoundNBT tag = list.getCompoundTagAt(i);
				inv.setInventorySlotContents(i, new ItemStack(tag));
			}
		}
		
		return inv;
	}
	
	public static void SetTemplateInventory(ItemStack stack, IInventory inv) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		CompoundNBT nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < Math.min(inv.getSizeInventory(), MAX_TEMPLATES); i++) {
			ItemStack inSlot = inv.getStackInSlot(i);
			if (inSlot.isEmpty()) {
				continue;
			}
			
			CompoundNBT tag = inSlot.serializeNBT();
			list.appendTag(tag);
		}
		
		nbt.setTag(NBT_TEMPLATE_INV, list);
		
		// also reset selection index
		nbt.putInt(NBT_TEMPLATE_INDEX, 0);
		stack.setTagCompound(nbt);
	}
	
	/**
	 * Attempts to add the scroll to the wand's inventory. Returns null on success, or the
	 * input scroll on failure.
	 * @param wand
	 * @param scroll
	 * @return
	 */
	public static ItemStack AddTemplateToInventory(ItemStack wand, ItemStack scroll) {
		// Try and just add an NBT tag instead of parsing the inventory
		CompoundNBT tag = wand.getTagCompound();
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		NBTTagList list = tag.getTagList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND);
		if (list == null) {
			list = new NBTTagList();
		}
		
		if (list.tagCount() < (MAX_TEMPLATES - 1)) {
			list.appendTag(scroll.serializeNBT());
			scroll = ItemStack.EMPTY;
			tag.setTag(NBT_TEMPLATE_INV, list);
			wand.setTagCompound(tag);
		}
		
		return scroll;
	}
	
	public static @Nonnull ItemStack GetSelectedTemplate(ItemStack wand) {
		if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || !wand.hasTagCompound()) {
			return ItemStack.EMPTY;
		}
		
		final int index = GetTemplateIndex(wand);
		NBTTagList list = wand.getTagCompound().getTagList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND);
		if (list != null && list.tagCount() > index) {
			CompoundNBT tag = list.getCompoundTagAt(index);
			return new ItemStack(tag);
		}
		
		return ItemStack.EMPTY;
	}
	
	@Override
	public String getLoreKey() {
		return "template_wand";
	}

	@Override
	public String getLoreDisplayName() {
		return "Template Wands";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("A magic wand with a geogem inside of it.", "This wand allows you to select regions in the world and save them as templates.", "From there, you can place the template in the world!");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("A magic wand with a geogem inside of it.", "Template wands have three modes.", "The first mode allows you to select a region in space. The first block you click is the anchor, and the second defines the bounds.", "The second mode is for capturing selections. You'll need a blank map in order to capture the template.", "The third mode allows you to place templates. Using the wand in this mode opens up an inventory where all templates in teh wand can be arranged. Shifting while in teh mode allows you to place the template.", "Template blocks don't get built automatically. You'll need workers to do that for you.");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public static void HandleScroll(PlayerEntity player, ItemStack stack, boolean forward) {
		// On client, just send to server
		if (player.world.isRemote) {
			NetworkHandler.getSyncChannel().sendToServer(new TemplateWandUpdate(WandUpdateType.SCROLL, forward));
			return;
		}
		
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		// could use wrappers but will be efficient
		CompoundNBT nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		int index = nbt.getInt(NBT_TEMPLATE_INDEX);
		int templateSize = Math.max(1, nbt.getTagList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND).tagCount());
		if (forward) {
			index = (index + 1) % templateSize;
		} else {
			index--;
			if (index < 0) {
				index = templateSize - 1;
			}
		}
		
		nbt.putInt(NBT_TEMPLATE_INDEX, index);
		stack.setTagCompound(nbt);
	}
	
	public static void HandleModeChange(PlayerEntity player, ItemStack stack, boolean forward) {
		if (player.world.isRemote) {
			NetworkHandler.getSyncChannel().sendToServer(new TemplateWandUpdate(WandUpdateType.MODE, forward));
			return;
		}
		
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		final WandMode mode = getModeOf(stack);
		int index;
		if (forward) {
			index = (mode.ordinal() + 1) % WandMode.values().length;
		} else {
			index = mode.ordinal() - 1;
			if (index < 0) {
				index = WandMode.values().length - 1;
			}
		}
		WandMode to = WandMode.values()[index];
		
		SetWandMode(stack, to);
	}
	
	protected ActionResult<ItemStack> capture(ItemStack stack, World worldIn, PlayerEntity playerIn, @Nullable BlockPos clickedPos) {
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		
		// Check for blank map and create template scroll
		Pair<BlockPos, BlockPos> selection = attr.getTemplateSelection();
		if (selection == null || selection.getLeft() == null || selection.getRight() == null) {
			playerIn.sendMessage(new TextComponentTranslation("info.templates.capture.nopos"));
			return ActionResult.<ItemStack>newResult(EnumActionResult.FAIL, stack);
		}
		
		// Figure out dimensions
		BlockPos pos1 = selection.getLeft();
		BlockPos pos2 = selection.getRight();
		BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ()));
		BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ()));
		if (!playerIn.isCreative()) {
			final int size = Math.abs(min.getX() - max.getX())
					* Math.abs(min.getY() - max.getY())
					* Math.abs(min.getZ() - max.getZ());
			
			if (size > MAX_TEMPLATE_BLOCKS) {
				playerIn.sendMessage(new TextComponentTranslation("info.templates.capture.toobig"));
				return ActionResult.<ItemStack>newResult(EnumActionResult.FAIL, stack);
			}
		}
		
		if (!playerIn.isCreative()) {
			// TODO try to add to wand automatically. If sneaking though, just drop on ground.
			
			// Find blank map
			ItemStack map = new ItemStack(Items.MAP);
			if (!Inventories.remove(playerIn.inventory, map).isEmpty()) {
				playerIn.sendMessage(new TextComponentTranslation("info.templates.capture.nomap"));
				return ActionResult.<ItemStack>newResult(EnumActionResult.FAIL, stack);
			}
		}
			
		// Have taken map and must succeed now
		Direction face = null;
		if (clickedPos != null) {
			// Figure out facing by looking at clicked pos vs our pos
			face = Direction.getFacingFromVector((float) (clickedPos.getX() - playerIn.posX), 0f, (float) (clickedPos.getZ() - playerIn.posZ));
		}
		BlockPos offset = (clickedPos == null ? null : clickedPos.subtract(min));
		ItemStack scroll = TemplateScroll.Capture(worldIn, min, max, offset, face);
		
		// Try to add to wand if not sneaking
		if (!playerIn.isSneaking()) {
			scroll = AddTemplateToInventory(stack, scroll);
		}
		
		if (scroll.isEmpty()) {
			playerIn.sendMessage(new TextComponentTranslation("info.templates.capture.towand"));
		} else {
			scroll = Inventories.addItem(playerIn.inventory, scroll); 
			if (!scroll.isEmpty()) {
				playerIn.dropItem(scroll, false);
			}
		}
		
		// Conveniently switch to selection mode to prevent wasting maps
		attr.clearTemplateSelection();
		NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
		return ActionResult.<ItemStack>newResult(EnumActionResult.SUCCESS, stack);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack stack = playerIn.getHeldItem(hand);
		final WandMode mode = getModeOf(stack);
		if (mode == WandMode.SPAWN) {
			if (!playerIn.isSneaking()) {
				playerIn.openGui(NostrumFairies.instance, NostrumFairyGui.templateWandGuiID, worldIn,
						(int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
				return ActionResult.<ItemStack>newResult(EnumActionResult.SUCCESS, stack);
			}
		}
		
		if (worldIn.isRemote) {
			return ActionResult.<ItemStack>newResult(EnumActionResult.SUCCESS, stack);
		}
		
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		if (attr == null || !(attr.builderFairyUnlocked())) {
			return ActionResult.<ItemStack>newResult(EnumActionResult.FAIL, stack);
		}
		
		if (mode == WandMode.SELECTION) {
			// Pass unless they're shifting
			if (playerIn.isSneaking()) {
				attr.clearTemplateSelection();
				NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
				return ActionResult.<ItemStack>newResult(EnumActionResult.SUCCESS, stack);
			}
			
			return ActionResult.<ItemStack>newResult(EnumActionResult.PASS, stack);
		} else if (mode == WandMode.CAPTURE) {
			return capture(stack, worldIn, playerIn, null);
		}
		
		return ActionResult.<ItemStack>newResult(EnumActionResult.PASS, stack);
	}
	
	@Override
	public EnumActionResult onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		if (attr == null) {
			return EnumActionResult.FAIL;
		}
		
		final INostrumMagic magic = NostrumMagica.getMagicWrapper(playerIn);
		if (magic == null || !magic.getCompletedResearches().contains("logistics_construction") ) {
			return EnumActionResult.FAIL;
		}
		
		final ItemStack stack = playerIn.getHeldItem(hand);
		final WandMode mode = getModeOf(stack);
		if (mode == WandMode.SELECTION) {
			if (playerIn.isSneaking()) {
				attr.clearTemplateSelection();
			} else {
				attr.addTemplateSelection(pos);
			}
			
			NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
			
			return EnumActionResult.SUCCESS;
		} else if (mode == WandMode.CAPTURE) {
			return capture(stack, worldIn, playerIn, pos).getType();
		} else if (mode == WandMode.SPAWN) {
			// Get selected template. If it's a thing, spawn it but as template blocks
			
			if (playerIn.isSneaking()) {
				ItemStack templateScroll = GetSelectedTemplate(stack);
				if (!templateScroll.isEmpty() && templateScroll.getItem() instanceof TemplateScroll) {
					TemplateBlueprint blueprint = TemplateScroll.GetTemplate(templateScroll);
					if (blueprint != null) {
						Direction rotate = Direction.getFacingFromVector((float) (pos.getX() - playerIn.posX), 0f, (float) (pos.getZ() - playerIn.posZ));
						List<BlockPos> blocks = blueprint.spawn(worldIn, pos.offset(facing), rotate);
						for (BlockPos buildSpot : blocks) {
							attr.addBuildSpot(buildSpot);
						}
					}
				}
			}
		}
		
		return EnumActionResult.FAIL;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	public String getModelName(WandMode type) {
		final String suffix;
		
		switch (type) {
		case CAPTURE:
			suffix = "_write";
			break;
		case SELECTION:
		default:
			suffix = "";
			break;
		case SPAWN:
			suffix = "_read";
			break;
		}
		
		return ID + suffix;
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		return new ICapabilityProvider() {

			private TemplateViewerCapability def = new TemplateViewerCapability();
			
			@Override
			public boolean hasCapability(Capability<?> capability, Direction facing) {
				return capability == TemplateViewerCapability.CAPABILITY;
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getCapability(Capability<T> capability, Direction facing) {
				if (capability == TemplateViewerCapability.CAPABILITY) {
					return (T) def;
				}
				
				return null;
			}
			
		};
	}
}
