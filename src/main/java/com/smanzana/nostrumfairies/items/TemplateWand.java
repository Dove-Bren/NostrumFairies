package com.smanzana.nostrumfairies.items;

import java.util.List;

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
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.utils.Inventories;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
	
	public static void init() {
		;
	}
	
	public TemplateWand() {
		super();
		this.setUnlocalizedName(ID);
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
		if (stack == null || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		stack.setItemDamage(metaFromMode(mode));
	}
	
	private static final String NBT_TEMPLATE_INDEX = "template_index";
	private static final String NBT_TEMPLATE_INV = "templates";
	
	public static int GetTemplateIndex(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof TemplateWand) || !stack.hasTagCompound()) {
			return 0;
		}
		
		NBTTagCompound nbt = stack.getTagCompound();
		return nbt.getInteger(NBT_TEMPLATE_INDEX);
	}
	
	public static void SetTemplateIndex(ItemStack stack, int index) {
		if (stack == null || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		nbt.setInteger(NBT_TEMPLATE_INDEX, index);
		stack.setTagCompound(nbt);
	}
	
	public static IInventory GetTemplateInventory(ItemStack stack) {
		InventoryBasic inv = new InventoryBasic("Template Wand Inventory", false, MAX_TEMPLATES) {
			
			@Override
			public boolean isItemValidForSlot(int index, ItemStack stack) {
				return index < this.getSizeInventory()
						&& (stack == null || stack.getItem() instanceof TemplateScroll);
			}
			
			@Override
			public void markDirty() {
				super.markDirty();
				
				// Bleed changes into wand
				SetTemplateInventory(stack, this);
			}
			
		};
		
		if (stack != null && stack.getItem() instanceof TemplateWand && stack.hasTagCompound()) {
			NBTTagList list = stack.getTagCompound().getTagList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount() && i < MAX_TEMPLATES; i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				inv.setInventorySlotContents(i, ItemStack.loadItemStackFromNBT(tag));
			}
		}
		
		return inv;
	}
	
	public static void SetTemplateInventory(ItemStack stack, IInventory inv) {
		if (stack == null || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < Math.min(inv.getSizeInventory(), MAX_TEMPLATES); i++) {
			ItemStack inSlot = inv.getStackInSlot(i);
			if (inSlot == null) {
				continue;
			}
			
			NBTTagCompound tag = inSlot.serializeNBT();
			list.appendTag(tag);
		}
		
		nbt.setTag(NBT_TEMPLATE_INV, list);
		
		// also reset selection index
		nbt.setInteger(NBT_TEMPLATE_INDEX, 0);
		stack.setTagCompound(nbt);
	}
	
	public static @Nullable ItemStack GetSelectedTemplate(ItemStack wand) {
		if (wand == null || !(wand.getItem() instanceof TemplateWand) || !wand.hasTagCompound()) {
			return null;
		}
		
		final int index = GetTemplateIndex(wand);
		NBTTagList list = wand.getTagCompound().getTagList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND);
		if (list != null && list.tagCount() > index) {
			NBTTagCompound tag = list.getCompoundTagAt(index);
			return ItemStack.loadItemStackFromNBT(tag);
		}
		
		return null;
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
		return new Lore().add("");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	public static void HandleScroll(EntityPlayer player, ItemStack stack, boolean forward) {
		// On client, just send to server
		if (player.worldObj.isRemote) {
			NetworkHandler.getSyncChannel().sendToServer(new TemplateWandUpdate(WandUpdateType.SCROLL, forward));
			return;
		}
		
		if (stack == null || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		// could use wrappers but will be efficient
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
		}
		
		int index = nbt.getInteger(NBT_TEMPLATE_INDEX);
		int templateSize = Math.max(1, nbt.getTagList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND).tagCount());
		if (forward) {
			index = (index + 1) % templateSize;
		} else {
			index--;
			if (index < 0) {
				index = templateSize - 1;
			}
		}
		
		nbt.setInteger(NBT_TEMPLATE_INDEX, index);
		stack.setTagCompound(nbt);
	}
	
	public static void HandleModeChange(EntityPlayer player, ItemStack stack, boolean forward) {
		if (player.worldObj.isRemote) {
			NetworkHandler.getSyncChannel().sendToServer(new TemplateWandUpdate(WandUpdateType.MODE, forward));
			return;
		}
		
		if (stack == null || !(stack.getItem() instanceof TemplateWand)) {
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
	
	protected ActionResult<ItemStack> capture(ItemStack stack, World worldIn, EntityPlayer playerIn, @Nullable BlockPos clickedPos) {
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		
		// Check for blank map and create template scroll
		Pair<BlockPos, BlockPos> selection = attr.getTemplateSelection();
		if (selection == null || selection.getLeft() == null || selection.getRight() == null) {
			playerIn.addChatComponentMessage(new TextComponentTranslation("info.templates.capture.nopos"));
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
				playerIn.addChatComponentMessage(new TextComponentTranslation("info.templates.capture.toobig"));
				return ActionResult.<ItemStack>newResult(EnumActionResult.FAIL, stack);
			}
		}
		
		if (!playerIn.isCreative()) {
			// TODO try to add to wand automatically. If sneaking though, just drop on ground.
			
			// Find blank map
			ItemStack map = new ItemStack(Items.MAP);
			if (null != Inventories.remove(playerIn.inventory, map)) {
				playerIn.addChatComponentMessage(new TextComponentTranslation("info.templates.capture.nomap"));
				return ActionResult.<ItemStack>newResult(EnumActionResult.FAIL, stack);
			}
		}
			
		// Have taken map and must succeed now
		EnumFacing face = null;
		if (clickedPos != null) {
			// Figure out facing by looking at clicked pos vs our pos
			face = EnumFacing.getFacingFromVector((float) (clickedPos.getX() - playerIn.posX), 0f, (float) (clickedPos.getZ() - playerIn.posZ));
		}
		BlockPos offset = (clickedPos == null ? null : clickedPos.subtract(min));
		ItemStack scroll = TemplateScroll.Capture(worldIn, min, max, offset, face);
		scroll = Inventories.addItem(playerIn.inventory, scroll); 
		if (scroll != null) {
			playerIn.dropItem(scroll, false);
		}
		
		// Conveniently switch to selection mode to prevent wasting maps
		attr.clearTemplateSelection();
		NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
		return ActionResult.<ItemStack>newResult(EnumActionResult.SUCCESS, stack);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World worldIn, EntityPlayer playerIn, EnumHand hand) {
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
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return EnumActionResult.SUCCESS;
		}
		
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		if (attr == null || !(attr.builderFairyUnlocked())) {
			return EnumActionResult.FAIL;
		}
		
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
				if (templateScroll != null && templateScroll.getItem() instanceof TemplateScroll) {
					TemplateBlueprint blueprint = TemplateScroll.GetTemplate(templateScroll);
					if (blueprint != null) {
						EnumFacing rotate = EnumFacing.getFacingFromVector((float) (pos.getX() - playerIn.posX), 0f, (float) (pos.getZ() - playerIn.posZ));
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
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
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
	public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new ICapabilityProvider() {

			private TemplateViewerCapability def = new TemplateViewerCapability();
			
			@Override
			public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
				return capability == TemplateViewerCapability.CAPABILITY;
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
				if (capability == TemplateViewerCapability.CAPABILITY) {
					return (T) def;
				}
				
				return null;
			}
			
		};
	}
}
