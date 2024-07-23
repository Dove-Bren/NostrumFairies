package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.smanzana.autodungeons.item.IBlueprintHolder;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.nostrumfairies.NostrumFairies;
import com.smanzana.nostrumfairies.capabilities.fey.INostrumFeyCapability;
import com.smanzana.nostrumfairies.client.gui.container.TemplateWandGui;
import com.smanzana.nostrumfairies.network.NetworkHandler;
import com.smanzana.nostrumfairies.network.messages.TemplateWandUpdate;
import com.smanzana.nostrumfairies.network.messages.TemplateWandUpdate.WandUpdateType;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.ISelectionItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
public class TemplateWand extends Item implements ILoreTagged, IBlueprintHolder, ISelectionItem {

	public static enum WandMode {
		SELECTION,
		CAPTURE,
		SPAWN
	}
	
	public static final float ModelMode(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn) {
		final WandMode mode = GetWandMode(stack);
		float val = 0.0F;
		if (mode != null) {
			switch (mode) {
			case SELECTION:
				val = 1.0F;
				break;
			case CAPTURE:
				val = 2.0F;
				break;
			case SPAWN:
				val = 3.0F;
				break;
			}
		}
		
		return val;
	}
	
	public static final String ID = "template_wand";
	private static final int MAX_TEMPLATES = 10;
	public static final int MAX_TEMPLATE_BLOCKS = 16 * 16 * 16;
	private static final String NBT_MODE = "mode";
	private static final String NBT_TEMPLATE_INDEX = "template_index";
	private static final String NBT_TEMPLATE_INV = "templates";
	
	public TemplateWand() {
		super(FairyItems.PropUnstackable());
	}
	
	@Override
	public ITextComponent getHighlightTip(ItemStack item, ITextComponent displayName ) {
		return displayName;
	}
	
	public static @Nullable WandMode GetWandMode(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return null;
		}
		
		WandMode ret;
		try {
			ret = WandMode.valueOf(stack.getTag().getString(NBT_MODE).toUpperCase());
		} catch (Exception e) {
			ret = WandMode.SELECTION;
		}
		return ret;
	}
	
	public static void SetWandMode(ItemStack stack, WandMode mode) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		CompoundNBT tag = stack.getTag();
		if (tag == null) tag = new CompoundNBT();
		tag.putString(NBT_MODE, mode.name());
		stack.setTag(tag);
	}
	
	public static int GetTemplateIndex(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand) || !stack.hasTag()) {
			return 0;
		}
		
		CompoundNBT nbt = stack.getTag();
		return nbt.getInt(NBT_TEMPLATE_INDEX);
	}
	
	public static void SetTemplateIndex(ItemStack stack, int index) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		nbt.putInt(NBT_TEMPLATE_INDEX, index);
		stack.setTag(nbt);
	}
	
	public static IInventory GetTemplateInventory(ItemStack stack) {
		Inventory inv = new Inventory(MAX_TEMPLATES) {
			
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
		
		if (!stack.isEmpty() && stack.getItem() instanceof TemplateWand && stack.hasTag()) {
			ListNBT list = stack.getTag().getList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND);
			for (int i = 0; i < list.size() && i < MAX_TEMPLATES; i++) {
				CompoundNBT tag = list.getCompound(i);
				inv.setInventorySlotContents(i, ItemStack.read(tag));
			}
		}
		
		return inv;
	}
	
	public static void SetTemplateInventory(ItemStack stack, IInventory inv) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		ListNBT list = new ListNBT();
		for (int i = 0; i < Math.min(inv.getSizeInventory(), MAX_TEMPLATES); i++) {
			ItemStack inSlot = inv.getStackInSlot(i);
			if (inSlot.isEmpty()) {
				continue;
			}
			
			CompoundNBT tag = inSlot.serializeNBT();
			list.add(tag);
		}
		
		nbt.put(NBT_TEMPLATE_INV, list);
		
		// also reset selection index
		nbt.putInt(NBT_TEMPLATE_INDEX, 0);
		stack.setTag(nbt);
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
		CompoundNBT tag = wand.getTag();
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		ListNBT list = tag.getList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND);
		if (list == null) {
			list = new ListNBT();
		}
		
		if (list.size() < (MAX_TEMPLATES - 1)) {
			list.add(scroll.serializeNBT());
			scroll = ItemStack.EMPTY;
			tag.put(NBT_TEMPLATE_INV, list);
			wand.setTag(tag);
		}
		
		return scroll;
	}
	
	public static @Nonnull ItemStack GetSelectedTemplate(ItemStack wand) {
		if (wand.isEmpty() || !(wand.getItem() instanceof TemplateWand) || !wand.hasTag()) {
			return ItemStack.EMPTY;
		}
		
		final int index = GetTemplateIndex(wand);
		ListNBT list = wand.getTag().getList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND);
		if (list != null && list.size() > index) {
			CompoundNBT tag = list.getCompound(index);
			return ItemStack.read(tag);
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
			NetworkHandler.sendToServer(new TemplateWandUpdate(WandUpdateType.SCROLL, forward));
			return;
		}
		
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		// could use wrappers but will be efficient
		CompoundNBT nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundNBT();
		}
		
		int index = nbt.getInt(NBT_TEMPLATE_INDEX);
		int templateSize = Math.max(1, nbt.getList(NBT_TEMPLATE_INV, NBT.TAG_COMPOUND).size());
		if (forward) {
			index = (index + 1) % templateSize;
		} else {
			index--;
			if (index < 0) {
				index = templateSize - 1;
			}
		}
		
		nbt.putInt(NBT_TEMPLATE_INDEX, index);
		stack.setTag(nbt);
	}
	
	public static void HandleModeChange(PlayerEntity player, ItemStack stack, boolean forward) {
		if (player.world.isRemote) {
			NetworkHandler.sendToServer(new TemplateWandUpdate(WandUpdateType.MODE, forward));
			return;
		}
		
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		final WandMode mode = GetWandMode(stack);
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
	
	protected ActionResult<ItemStack> capture(ItemStack stack, World worldIn, PlayerEntity playerIn, BlockPos captureOrigin, Direction facing) {
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		
		// Check for blank map and create template scroll
		Pair<BlockPos, BlockPos> selection = attr.getTemplateSelection();
		if (selection == null || selection.getLeft() == null || selection.getRight() == null) {
			playerIn.sendMessage(new TranslationTextComponent("info.templates.capture.nopos"), Util.DUMMY_UUID);
			return ActionResult.resultFail(stack);
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
				playerIn.sendMessage(new TranslationTextComponent("info.templates.capture.toobig"), Util.DUMMY_UUID);
				return ActionResult.resultFail(stack);
			}
		}
		
		if (!playerIn.isCreative()) {
			// TODO try to add to wand automatically. If sneaking though, just drop on ground.
			
			// Find blank map
			ItemStack map = new ItemStack(Items.MAP);
			if (!Inventories.remove(playerIn.inventory, map).isEmpty()) {
				playerIn.sendMessage(new TranslationTextComponent("info.templates.capture.nomap"), Util.DUMMY_UUID);
				return ActionResult.resultFail(stack);
			}
		}
			
		// Have taken map and must succeed now
		BlockPos offset = captureOrigin.subtract(min);
		ItemStack scroll = TemplateScroll.Capture(worldIn, min, max, new BlueprintLocation(offset, facing));
		
		// Try to add to wand if not sneaking
		if (!playerIn.isSneaking()) {
			scroll = AddTemplateToInventory(stack, scroll);
		}
		
		if (scroll.isEmpty()) {
			playerIn.sendMessage(new TranslationTextComponent("info.templates.capture.towand"), Util.DUMMY_UUID);
		} else {
			scroll = Inventories.addItem(playerIn.inventory, scroll); 
			if (!scroll.isEmpty()) {
				playerIn.dropItem(scroll, false);
			}
		}
		
		// Conveniently switch to selection mode to prevent wasting maps
		attr.clearTemplateSelection();
		NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
		return ActionResult.resultSuccess(stack);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
		final ItemStack stack = playerIn.getHeldItem(hand);
		final WandMode mode = GetWandMode(stack);
		if (mode == WandMode.SPAWN) {
			if (!playerIn.isSneaking()) {
				int pos = Inventories.getPlayerHandSlotIndex(playerIn.inventory, Hand.MAIN_HAND);
				ItemStack inHand = playerIn.getHeldItemMainhand();
				if (inHand.isEmpty()) {
					inHand = playerIn.getHeldItemOffhand();
					pos = Inventories.getPlayerHandSlotIndex(playerIn.inventory, Hand.OFF_HAND);
				}
				NostrumMagica.instance.proxy.openContainer(playerIn, TemplateWandGui.TemplateWandContainer.Make(pos));
				return ActionResult.resultSuccess(stack);
			}
		}
		
		if (worldIn.isRemote) {
			return ActionResult.resultSuccess(stack);
		}
		
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		if (attr == null || !(attr.builderFairyUnlocked())) {
			return ActionResult.resultFail(stack);
		}
		
		if (mode == WandMode.SELECTION) {
			// Pass unless they're shifting
			if (playerIn.isSneaking()) {
				attr.clearTemplateSelection();
				NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
				return ActionResult.resultSuccess(stack);
			}
			
			return ActionResult.resultPass(stack);
		} else if (mode == WandMode.CAPTURE) {
			return capture(stack, worldIn, playerIn, playerIn.getPosition(), Direction.fromAngle(playerIn.rotationYaw));
		}
		
		return ActionResult.resultPass(stack);
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		final World worldIn = context.getWorld();
		if (worldIn.isRemote) {
			return ActionResultType.SUCCESS;
		}
		
		final PlayerEntity playerIn = context.getPlayer();
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		if (attr == null) {
			return ActionResultType.FAIL;
		}
		
		final INostrumMagic magic = NostrumMagica.getMagicWrapper(playerIn);
		if (magic == null || !magic.getCompletedResearches().contains("logistics_construction") ) {
			return ActionResultType.FAIL;
		}
		
		final Hand hand = context.getHand();
		final BlockPos pos = context.getPos();
		final ItemStack stack = playerIn.getHeldItem(hand);
		final WandMode mode = GetWandMode(stack);
		final BlockPos playerPos = playerIn.getPosition();
		Direction rotate = Direction.getFacingFromVector((float) (pos.getX() - playerPos.getX()), 0f, (float) (pos.getZ() - playerPos.getZ()));
		if (mode == WandMode.SELECTION) {
			if (playerIn.isSneaking()) {
				attr.clearTemplateSelection();
			} else {
				attr.addTemplateSelection(pos);
			}
			
			NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
			
			return ActionResultType.SUCCESS;
		} else if (mode == WandMode.CAPTURE) {
			return capture(stack, worldIn, playerIn, pos.offset(context.getFace()), rotate).getType();
		} else if (mode == WandMode.SPAWN) {
			// Get selected template. If it's a thing, spawn it but as template blocks
			
			if (playerIn.isSneaking()) {
				ItemStack templateScroll = GetSelectedTemplate(stack);
				if (!templateScroll.isEmpty() && templateScroll.getItem() instanceof TemplateScroll) {
					TemplateBlueprint blueprint = TemplateScroll.GetTemplate(templateScroll);
					if (blueprint != null) {
						List<BlockPos> blocks = blueprint.spawn(worldIn, pos.offset(context.getFace()), rotate);
						for (BlockPos buildSpot : blocks) {
							attr.addBuildSpot(buildSpot);
						}
					}
				}
			}
		}
		
		return ActionResultType.FAIL;
	}
	
	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target, Hand hand) {
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
//		return new ICapabilityProvider() {
//
//			private LazyOptional<TemplateViewerCapability> def = LazyOptional.of(()-> new TemplateViewerCapability());
//			
//			@Override
//			public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
//				if (capability == TemplateViewerCapability.CAPABILITY) {
//					return def.cast();
//				}
//				
//				return LazyOptional.empty();
//			}
//			
//		};
		return super.initCapabilities(stack, nbt);
	}

	@Override
	public TemplateBlueprint getBlueprint(PlayerEntity player, ItemStack stack, BlockPos pos) {
		@Nullable TemplateBlueprint blueprint = null;
		if (!stack.isEmpty() && !GetSelectedTemplate(stack).isEmpty()) {
			blueprint = TemplateScroll.GetTemplate(GetSelectedTemplate(stack));
		}
		return blueprint;
	}

	@Override
	public boolean hasBlueprint(PlayerEntity player, ItemStack stack) {
		return GetWandMode(stack) == WandMode.SPAWN
				&& !stack.isEmpty()
				&& !GetSelectedTemplate(stack).isEmpty()
				&& TemplateScroll.GetTemplate(GetSelectedTemplate(stack)) != null;
	}

	@Override
	public boolean shouldDisplayBlueprint(PlayerEntity player, ItemStack stack, BlockPos pos) {
		return GetWandMode(stack) == WandMode.SPAWN && player.isSneaking();
	}

	@Override
	public BlockPos getAnchor(PlayerEntity player, ItemStack stack) {
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
		if (attr != null) {
			return attr.getTemplateSelection().getLeft();
		}
		return null;
	}

	@Override
	public BlockPos getBoundingPos(PlayerEntity player, ItemStack stack) {
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
		if (attr != null) {
			return attr.getTemplateSelection().getRight();
		}
		return null;
	}

	@Override
	public boolean isSelectionValid(ClientPlayerEntity player, ItemStack stack) {
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
		if (attr != null) {
			Pair<BlockPos, BlockPos> selection = attr.getTemplateSelection();
			BlockPos pos1 = selection.getLeft();
			BlockPos pos2 = selection.getRight();
			
			if (pos1 != null && pos2 != null) {
				// If creative, any size works
				if (player.isCreative()) {
					return true;
				}
				
				// Check size
				final int size = Math.abs(pos1.getX() - pos2.getX())
						* Math.abs(pos1.getY() - pos2.getY())
						* Math.abs(pos1.getZ() - pos2.getZ());
				
				if (size <= TemplateWand.MAX_TEMPLATE_BLOCKS) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean shouldRenderSelection(PlayerEntity player, ItemStack stack) {
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
		if (attr != null) {
			Pair<BlockPos, BlockPos> selection = attr.getTemplateSelection();
			BlockPos pos1 = selection.getLeft();
			BlockPos pos2 = selection.getRight();
			
			if (pos1 != null) {
				double minDist = player.getDistanceSq(pos1.getX(), pos1.getY(), pos1.getZ());
				if (minDist >= 5096 && pos2 != null) {
					minDist = player.getDistanceSq(pos2.getX(), pos2.getY(), pos2.getZ());
				}
				
				if (minDist < 5096) {
					return true;
				}
			}
		}
		
		return true;
	}
}
