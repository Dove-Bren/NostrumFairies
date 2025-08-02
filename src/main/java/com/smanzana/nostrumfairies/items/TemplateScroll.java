package com.smanzana.nostrumfairies.items;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.world.blueprints.Blueprint;
import com.smanzana.autodungeons.world.blueprints.BlueprintLocation;
import com.smanzana.nostrumfairies.templates.TemplateBlueprint;
import com.smanzana.nostrummagica.loretag.ELoreCategory;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Stores a template
 * @author Skyler
 *
 */
public class TemplateScroll extends Item implements ILoreTagged {

	public static final String ID = "template_scroll";
	private static final String NBT_TEMPLATE = "template";
	
	public TemplateScroll() {
		super(FairyItems.PropUnstackable());
	}
	
	@Override
	public Component getHighlightTip(ItemStack item, Component displayName) {
		return displayName;
	}
	
	public static TemplateBlueprint GetTemplate(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateScroll) || !stack.hasTag()) {
			return null;
		}
		
		CompoundTag nbt = stack.getTag();
		CompoundTag blueprintTag = nbt.getCompound(NBT_TEMPLATE);
		return TemplateBlueprint.fromNBT(blueprintTag);
	}
	
	public static ItemStack Capture(Level world, BlockPos pos1, BlockPos pos2) {
		return Capture(world, pos1, pos2, null);
	}
	
	public static ItemStack Capture(Level world, BlockPos pos1, BlockPos pos2, @Nullable BlueprintLocation origin) {
		Blueprint blueprint = Blueprint.Capture(world, pos1, pos2, origin);
		return Create(blueprint);
	}
	
	public static ItemStack Create(Blueprint blueprint) {
		return Create(new TemplateBlueprint(blueprint));
	}
	
	public static ItemStack Create(TemplateBlueprint blueprint) {
		ItemStack stack = new ItemStack(FairyItems.templateScroll);
		SetTemplate(stack, blueprint);
		stack.setHoverName(new TextComponent("New Template"));
		return stack;
	}
	
	protected static void SetTemplate(ItemStack stack, TemplateBlueprint blueprint) {
		SetTemplate(stack, blueprint.toNBT());
	}
	
	protected static void SetTemplate(ItemStack stack, CompoundTag blueprintTag) {
		if (stack.isEmpty() || !(stack.getItem() instanceof TemplateScroll)) {
			return;
		}
		
		CompoundTag nbt = stack.getTag();
		if (nbt == null) {
			nbt = new CompoundTag();
		}
		
		nbt.put(NBT_TEMPLATE, blueprintTag);
		stack.setTag(nbt);
	}
	
	@Override
	public String getLoreKey() {
		return "template_scroll";
	}

	@Override
	public String getLoreDisplayName() {
		return "Template Slips";
	}
	
	@Override
	public Lore getBasicLore() {
		return new Lore().add("Template slips are created using the template wand." , "They store templates of a structure.");
				
	}
	
	@Override
	public Lore getDeepLore() {
		return new Lore().add("Template slips are created using the template wand." , "They store templates of a structure.", "These slips can be inserted into the template wand to be placed.", "They can also be put into building blocks to have workers attempt to build and maintain a structure.");
	}

	@Override
	public ELoreCategory getCategory() {
		return ELoreCategory.ITEM;
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		return InteractionResult.PASS;
	}
	
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
		return super.interactLivingEntity(stack, playerIn, target, hand);
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}
	
}
