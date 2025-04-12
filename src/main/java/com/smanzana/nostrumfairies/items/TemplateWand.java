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

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

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
	
	public static final float ModelMode(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn, int entID) {
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
	public Component getHighlightTip(ItemStack item, Component displayName ) {
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
		
		CompoundTag tag = stack.getTag();
		if (tag == null) tag = new CompoundTag();
		tag.putString(NBT_MODE, mode.name());
		stack.setTag(tag);
	}
	
	public static int GetTemplateIndex(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand) || !stack.hasTag()) {
			return 0;
		}
		
		CompoundTag nbt = stack.getTag();
		return nbt.getInt(NBT_TEMPLATE_INDEX);
	}
	
	public static void SetTemplateIndex(ItemStack stack, int index) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		nbt.putInt(NBT_TEMPLATE_INDEX, index);
		stack.setTag(nbt);
	}
	
	public static Container GetTemplateInventory(ItemStack stack) {
		SimpleContainer inv = new SimpleContainer(MAX_TEMPLATES) {
			
			@Override
			public boolean canPlaceItem(int index, ItemStack stack) {
				return index < this.getContainerSize()
						&& (stack.isEmpty() || stack.getItem() instanceof TemplateScroll);
			}
			
			@Override
			public void setChanged() {
				super.setChanged();
				
				// Bleed changes into wand
				SetTemplateInventory(stack, this);
			}
			
		};
		
		if (!stack.isEmpty() && stack.getItem() instanceof TemplateWand && stack.hasTag()) {
			ListTag list = stack.getTag().getList(NBT_TEMPLATE_INV, Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size() && i < MAX_TEMPLATES; i++) {
				CompoundTag tag = list.getCompound(i);
				inv.setItem(i, ItemStack.of(tag));
			}
		}
		
		return inv;
	}
	
	public static void SetTemplateInventory(ItemStack stack, Container inv) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		ListTag list = new ListTag();
		for (int i = 0; i < Math.min(inv.getContainerSize(), MAX_TEMPLATES); i++) {
			ItemStack inSlot = inv.getItem(i);
			if (inSlot.isEmpty()) {
				continue;
			}
			
			CompoundTag tag = inSlot.serializeNBT();
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
		CompoundTag tag = wand.getTag();
		if (tag == null) {
			tag = new CompoundTag();
		}
		
		ListTag list = tag.getList(NBT_TEMPLATE_INV, Tag.TAG_COMPOUND);
		if (list == null) {
			list = new ListTag();
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
		ListTag list = wand.getTag().getList(NBT_TEMPLATE_INV, Tag.TAG_COMPOUND);
		if (list != null && list.size() > index) {
			CompoundTag tag = list.getCompound(index);
			return ItemStack.of(tag);
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
	
	public static void HandleScroll(Player player, ItemStack stack, boolean forward) {
		// On client, just send to server
		if (player.level.isClientSide) {
			NetworkHandler.sendToServer(new TemplateWandUpdate(WandUpdateType.SCROLL, forward));
			return;
		}
		
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateWand)) {
			return;
		}
		
		// could use wrappers but will be efficient
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		int index = nbt.getInt(NBT_TEMPLATE_INDEX);
		int templateSize = Math.max(1, nbt.getList(NBT_TEMPLATE_INV, Tag.TAG_COMPOUND).size());
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
	
	public static void HandleModeChange(Player player, ItemStack stack, boolean forward) {
		if (player.level.isClientSide) {
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
	
	protected InteractionResultHolder<ItemStack> capture(ItemStack stack, Level worldIn, Player playerIn, BlockPos captureOrigin, Direction facing) {
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		
		// Check for blank map and create template scroll
		Pair<BlockPos, BlockPos> selection = attr.getTemplateSelection();
		if (selection == null || selection.getLeft() == null || selection.getRight() == null) {
			playerIn.sendMessage(new TranslatableComponent("info.templates.capture.nopos"), Util.NIL_UUID);
			return InteractionResultHolder.fail(stack);
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
				playerIn.sendMessage(new TranslatableComponent("info.templates.capture.toobig"), Util.NIL_UUID);
				return InteractionResultHolder.fail(stack);
			}
		}
		
		if (!playerIn.isCreative()) {
			// TODO try to add to wand automatically. If sneaking though, just drop on ground.
			
			// Find blank map
			ItemStack map = new ItemStack(Items.MAP);
			if (!Inventories.remove(playerIn.getInventory(), map).isEmpty()) {
				playerIn.sendMessage(new TranslatableComponent("info.templates.capture.nomap"), Util.NIL_UUID);
				return InteractionResultHolder.fail(stack);
			}
		}
			
		// Have taken map and must succeed now
		BlockPos offset = captureOrigin.subtract(min);
		ItemStack scroll = TemplateScroll.Capture(worldIn, min, max, new BlueprintLocation(offset, facing));
		
		// Try to add to wand if not sneaking
		if (!playerIn.isShiftKeyDown()) {
			scroll = AddTemplateToInventory(stack, scroll);
		}
		
		if (scroll.isEmpty()) {
			playerIn.sendMessage(new TranslatableComponent("info.templates.capture.towand"), Util.NIL_UUID);
		} else {
			scroll = Inventories.addItem(playerIn.getInventory(), scroll); 
			if (!scroll.isEmpty()) {
				playerIn.drop(scroll, false);
			}
		}
		
		// Conveniently switch to selection mode to prevent wasting maps
		attr.clearTemplateSelection();
		NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
		return InteractionResultHolder.success(stack);
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		final ItemStack stack = playerIn.getItemInHand(hand);
		final WandMode mode = GetWandMode(stack);
		if (mode == WandMode.SPAWN) {
			if (!playerIn.isShiftKeyDown()) {
				int pos = Inventories.getPlayerHandSlotIndex(playerIn.getInventory(), InteractionHand.MAIN_HAND);
				ItemStack inHand = playerIn.getMainHandItem();
				if (inHand.isEmpty()) {
					inHand = playerIn.getOffhandItem();
					pos = Inventories.getPlayerHandSlotIndex(playerIn.getInventory(), InteractionHand.OFF_HAND);
				}
				NostrumMagica.instance.proxy.openContainer(playerIn, TemplateWandGui.TemplateWandContainer.Make(pos));
				return InteractionResultHolder.success(stack);
			}
		}
		
		if (worldIn.isClientSide) {
			return InteractionResultHolder.success(stack);
		}
		
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		if (attr == null || !(attr.builderFairyUnlocked())) {
			return InteractionResultHolder.fail(stack);
		}
		
		if (mode == WandMode.SELECTION) {
			// Pass unless they're shifting
			if (playerIn.isShiftKeyDown()) {
				attr.clearTemplateSelection();
				NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
				return InteractionResultHolder.success(stack);
			}
			
			return InteractionResultHolder.pass(stack);
		} else if (mode == WandMode.CAPTURE) {
			return capture(stack, worldIn, playerIn, playerIn.blockPosition(), Direction.fromYRot(playerIn.getYRot()));
		}
		
		return InteractionResultHolder.pass(stack);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		
		final Player playerIn = context.getPlayer();
		final INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(playerIn);
		if (attr == null) {
			return InteractionResult.FAIL;
		}
		
		final INostrumMagic magic = NostrumMagica.getMagicWrapper(playerIn);
		if (magic == null || !magic.getCompletedResearches().contains("logistics_construction") ) {
			return InteractionResult.FAIL;
		}
		
		final InteractionHand hand = context.getHand();
		final BlockPos pos = context.getClickedPos();
		final ItemStack stack = playerIn.getItemInHand(hand);
		final WandMode mode = GetWandMode(stack);
		final BlockPos playerPos = playerIn.blockPosition();
		Direction rotate = Direction.getNearest((float) (pos.getX() - playerPos.getX()), 0f, (float) (pos.getZ() - playerPos.getZ()));
		if (mode == WandMode.SELECTION) {
			if (playerIn.isShiftKeyDown()) {
				attr.clearTemplateSelection();
			} else {
				attr.addTemplateSelection(pos);
			}
			
			NostrumFairies.proxy.pushCapabilityRefresh(playerIn);
			
			return InteractionResult.SUCCESS;
		} else if (mode == WandMode.CAPTURE) {
			return capture(stack, worldIn, playerIn, pos.relative(context.getClickedFace()), rotate).getResult();
		} else if (mode == WandMode.SPAWN) {
			// Get selected template. If it's a thing, spawn it but as template blocks
			
			if (playerIn.isShiftKeyDown()) {
				ItemStack templateScroll = GetSelectedTemplate(stack);
				if (!templateScroll.isEmpty() && templateScroll.getItem() instanceof TemplateScroll) {
					TemplateBlueprint blueprint = TemplateScroll.GetTemplate(templateScroll);
					if (blueprint != null) {
						List<BlockPos> blocks = blueprint.spawn(worldIn, pos.relative(context.getClickedFace()), rotate);
						for (BlockPos buildSpot : blocks) {
							attr.addBuildSpot(buildSpot);
						}
					}
				}
			}
		}
		
		return InteractionResult.FAIL;
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		return super.interactLivingEntity(stack, playerIn, target, hand);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
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
	public TemplateBlueprint getBlueprint(Player player, ItemStack stack, BlockPos pos) {
		@Nullable TemplateBlueprint blueprint = null;
		if (!stack.isEmpty() && !GetSelectedTemplate(stack).isEmpty()) {
			blueprint = TemplateScroll.GetTemplate(GetSelectedTemplate(stack));
		}
		return blueprint;
	}

	@Override
	public boolean hasBlueprint(Player player, ItemStack stack) {
		return GetWandMode(stack) == WandMode.SPAWN
				&& !stack.isEmpty()
				&& !GetSelectedTemplate(stack).isEmpty()
				&& TemplateScroll.GetTemplate(GetSelectedTemplate(stack)) != null;
	}

	@Override
	public boolean shouldDisplayBlueprint(Player player, ItemStack stack, BlockPos pos) {
		return GetWandMode(stack) == WandMode.SPAWN && player.isShiftKeyDown();
	}

	@Override
	public BlockPos getAnchor(Player player, ItemStack stack) {
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
		if (attr != null) {
			return attr.getTemplateSelection().getLeft();
		}
		return null;
	}

	@Override
	public BlockPos getBoundingPos(Player player, ItemStack stack) {
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
		if (attr != null) {
			return attr.getTemplateSelection().getRight();
		}
		return null;
	}

	@Override
	public boolean isSelectionValid(Player player, ItemStack stack) {
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
	public boolean shouldRenderSelection(Player player, ItemStack stack) {
		INostrumFeyCapability attr = NostrumFairies.getFeyWrapper(player);
		if (attr != null) {
			Pair<BlockPos, BlockPos> selection = attr.getTemplateSelection();
			BlockPos pos1 = selection.getLeft();
			BlockPos pos2 = selection.getRight();
			
			if (pos1 != null) {
				double minDist = player.distanceToSqr(pos1.getX(), pos1.getY(), pos1.getZ());
				if (minDist >= 5096 && pos2 != null) {
					minDist = player.distanceToSqr(pos2.getX(), pos2.getY(), pos2.getZ());
				}
				
				if (minDist < 5096) {
					return true;
				}
			}
		}
		
		return true;
	}
}
